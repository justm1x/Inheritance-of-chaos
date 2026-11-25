package com.inheritanceofchaos.battle;

import com.inheritanceofchaos.factory.ItemFactory;
import com.inheritanceofchaos.model.Monster;
import com.inheritanceofchaos.model.Player;
import com.inheritanceofchaos.model.items.Consumable;
import com.inheritanceofchaos.model.items.Item;
import com.inheritanceofchaos.model.skills.Skill;
import com.inheritanceofchaos.util.Rng;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

// A turn-based battle that runs in its OWN THREAD.
//
// How the threads work together (producer / consumer):
//  - The window (Swing thread) is the producer. When the player clicks a button,
//    it puts a PlayerCommand into the queue.
//  - The battle thread is the consumer. It waits on queue.take() until a command arrives,
//    plays the player's turn, waits a little, plays the enemy's turn,
//    and then tells the window what happened (through BattleListener).
//
// While the battle is running only the battle thread changes the player and the monster.
public class BattleEngine implements Runnable, BattleLog {

    private static final double CRIT_CHANCE = 0.12;
    private static final double CRIT_MULTIPLIER = 1.6;
    private static final int DEFEND_MANA_REGEN = 4;

    // what happened after the player's action
    private enum TurnResult {
        DONE,             // the action was made, now it is the enemy's turn
        NOTHING_HAPPENED, // for example not enough mana, the player has to choose again
        ESCAPED           // the player ran away
    }

    private Player player;
    private Monster monster;
    private boolean escapable;
    private long enemyDelayMs;

    private BlockingQueue<PlayerCommand> commands = new LinkedBlockingQueue<>();
    private List<BattleListener> listeners = new ArrayList<>();
    private Thread thread;
    private int round = 1;

    // true while the battle waits for the player to press a button.
    // volatile because two threads read and write it.
    private volatile boolean waitingForPlayer = false;

    public BattleEngine(Player player, Monster monster, boolean escapable) {
        this(player, monster, escapable, 700);
    }

    // enemyDelayMs is the pause before the enemy moves, so the player can read the log (tests use 0)
    public BattleEngine(Player player, Monster monster, boolean escapable, long enemyDelayMs) {
        this.player = player;
        this.monster = monster;
        this.escapable = escapable;
        this.enemyDelayMs = enemyDelayMs;
    }

    // listeners must be added BEFORE start()
    public void addListener(BattleListener listener) {
        listeners.add(listener);
    }

    public boolean isEscapable() {
        return escapable;
    }

    // starts the battle thread
    public void start() {
        thread = new Thread(this, "battle-thread");
        thread.setDaemon(true);
        thread.start();
    }

    // Called by the window when the player presses a button.
    // Returns false if it is not the player's turn (for example the enemy is still moving).
    // synchronized, so if the player clicks very fast only ONE command is accepted per turn.
    public synchronized boolean submit(PlayerCommand command) {
        if (!waitingForPlayer) {
            return false;
        }
        waitingForPlayer = false;
        commands.add(command);
        return true;
    }

    public boolean isWaitingForPlayer() {
        return waitingForPlayer;
    }

    // stops the battle thread (used when the player quits in the middle of a fight)
    public void stop() {
        if (thread != null) {
            thread.interrupt();
        }
    }

    // ===== this code runs in the battle thread =====

    @Override
    public void run() {
        try {
            log(monster.getIntro(), LogType.SYSTEM);
            BattleResult result = battleLoop();
            finish(result);
        } catch (InterruptedException e) {
            // the battle was stopped, nothing else to do
        }
    }

    private BattleResult battleLoop() throws InterruptedException {
        while (true) {
            // 1. wait for the player
            player.setDefending(false);
            waitingForPlayer = true;
            sendUpdate();

            PlayerCommand command = commands.take(); // the thread sleeps here until a command comes
            sendUpdate();

            // 2. player's turn
            TurnResult turn = playerTurn(command);
            if (turn == TurnResult.NOTHING_HAPPENED) {
                continue; // ask the player again
            }
            if (turn == TurnResult.ESCAPED) {
                return BattleResult.ESCAPED;
            }
            sendUpdate();
            if (!monster.isAlive()) {
                return BattleResult.VICTORY;
            }

            // 3. enemy's turn
            Thread.sleep(enemyDelayMs);
            monster.takeTurn(player, this);
            sendUpdate();
            if (!player.isAlive()) {
                return BattleResult.DEFEAT;
            }

            round++;
            Thread.sleep(enemyDelayMs / 3);
        }
    }

    private TurnResult playerTurn(PlayerCommand command) {
        switch (command.type()) {
            case ATTACK:
                return attack();
            case SKILL:
                return useSkill(command.skill());
            case ITEM:
                return useItem(command.item());
            case DEFEND:
                return defend();
            case ESCAPE:
                return tryEscape();
            default:
                return TurnResult.NOTHING_HAPPENED;
        }
    }

    private TurnResult attack() {
        // 12% chance of a critical hit
        if (Rng.chance(CRIT_CHANCE)) {
            int dealt = monster.takeDamage(player.rollDamage(CRIT_MULTIPLIER));
            log("CRITICAL HIT! " + player.getName() + " strikes " + monster.getName() + " for " + dealt + " damage.",
                    LogType.PLAYER);
        } else {
            int dealt = monster.takeDamage(player.rollDamage(1.0));
            log(player.getName() + " strikes " + monster.getName() + " for " + dealt + " damage.", LogType.PLAYER);
        }
        return TurnResult.DONE;
    }

    private TurnResult useSkill(Skill skill) {
        if (skill == null || !player.getUnlockedSkills().contains(skill)) {
            log("You have not learned that skill yet.", LogType.WARNING);
            return TurnResult.NOTHING_HAPPENED;
        }
        if (!player.spendMana(skill.getManaCost())) {
            log("Not enough mana for " + skill.getName() + " (" + skill.getManaCost() + " MP).", LogType.WARNING);
            return TurnResult.NOTHING_HAPPENED;
        }
        log(player.getName() + " uses " + skill.getName() + "!", LogType.PLAYER);
        skill.use(player, monster, this);
        return TurnResult.DONE;
    }

    private TurnResult useItem(Consumable item) {
        if (item == null || !player.getInventory().contains(item)) {
            log("You do not have that item.", LogType.WARNING);
            return TurnResult.NOTHING_HAPPENED;
        }
        player.getInventory().remove(item);
        String message = item.apply(player, monster);
        if (item.isBattleOnly()) {
            log(message, LogType.PLAYER);
        } else {
            log(message, LogType.HEAL);
        }
        return TurnResult.DONE;
    }

    private TurnResult defend() {
        player.setDefending(true);
        int mana = player.restoreMana(DEFEND_MANA_REGEN);
        String message = player.getName() + " raises their guard: incoming damage is halved this turn";
        if (mana > 0) {
            message += " (+" + mana + " MP).";
        } else {
            message += ".";
        }
        log(message, LogType.PLAYER);
        return TurnResult.DONE;
    }

    private TurnResult tryEscape() {
        if (!escapable) {
            log("There is no escape from " + monster.getName() + "!", LogType.WARNING);
            return TurnResult.NOTHING_HAPPENED;
        }
        // 50% base chance, +8% for every level you are above the monster, but between 20% and 90%
        double chance = 0.5 + 0.08 * (player.getLevel() - monster.getLevel());
        if (chance < 0.2) {
            chance = 0.2;
        }
        if (chance > 0.9) {
            chance = 0.9;
        }
        if (Rng.chance(chance)) {
            log("You slip away into the shadows.", LogType.INFO);
            return TurnResult.ESCAPED;
        }
        log("You try to flee, but " + monster.getName() + " cuts you off!", LogType.WARNING);
        return TurnResult.DONE;
    }

    // gives rewards and tells the window that the battle is over
    private void finish(BattleResult result) {
        player.setDefending(false);
        int experience = 0;
        int gold = 0;
        int levelsGained = 0;
        List<Item> loot = new ArrayList<>();

        if (result == BattleResult.VICTORY) {
            log(monster.getName() + " is defeated!", LogType.REWARD);
            experience = monster.getXpReward();
            gold = monster.getGoldReward();
            player.addGold(gold);
            loot = ItemFactory.rollLoot(monster);
            for (Item item : loot) {
                player.getInventory().add(item);
            }

            int skillsBefore = player.getUnlockedSkills().size();
            levelsGained = player.gainExperience(experience);

            log("You gain " + experience + " XP and " + gold + " gold.", LogType.REWARD);
            for (Item item : loot) {
                log("Loot: " + item.getName() + " (" + item.getStatsText() + ")", LogType.REWARD);
            }
            if (levelsGained > 0) {
                log("LEVEL UP! You are now level " + player.getLevel() + ". Unspent stat points: "
                        + player.getStatPoints() + ".", LogType.SYSTEM);
                // skills that were added to the list after the level up are new
                List<Skill> skills = player.getUnlockedSkills();
                for (int i = skillsBefore; i < skills.size(); i++) {
                    log("New skill learned: " + skills.get(i).getName() + "!", LogType.SYSTEM);
                }
            }
        } else if (result == BattleResult.DEFEAT) {
            log(player.getName() + " falls... The darkness takes you.", LogType.WARNING);
        }

        waitingForPlayer = false;
        sendUpdate();
        BattleOutcome outcome = new BattleOutcome(result, monster.getName(), monster.getLevel(), monster.isBoss(),
                experience, gold, loot, levelsGained, round);
        for (BattleListener listener : listeners) {
            listener.onBattleEnd(outcome);
        }
    }

    // ===== messages to the window (Observer pattern) =====

    @Override
    public void log(String message, LogType type) {
        for (BattleListener listener : listeners) {
            listener.onLog(message, type);
        }
    }

    // Sends a copy of the current numbers to the window.
    // A copy is used so the Swing thread never reads the player while the battle thread changes it.
    private void sendUpdate() {
        BattleSnapshot snapshot = new BattleSnapshot(
                player.getName(), player.getLevel(),
                player.getHealth(), player.getMaxHealth(),
                player.getMana(), player.getMaxMana(),
                player.isDefending(),
                monster.getName(), monster.getLevel(),
                monster.getHealth(), monster.getMaxHealth(),
                monster.isBoss(), monster.getIntent(),
                waitingForPlayer, round);
        for (BattleListener listener : listeners) {
            listener.onUpdate(snapshot);
        }
    }
}
