package com.inheritanceofchaos.ui;

import com.inheritanceofchaos.battle.BattleEngine;
import com.inheritanceofchaos.battle.BattleListener;
import com.inheritanceofchaos.battle.BattleOutcome;
import com.inheritanceofchaos.battle.BattleResult;
import com.inheritanceofchaos.battle.BattleSnapshot;
import com.inheritanceofchaos.battle.LogType;
import com.inheritanceofchaos.battle.PlayerCommand;
import com.inheritanceofchaos.db.BattleRecord;
import com.inheritanceofchaos.db.ChronicleRepository;
import com.inheritanceofchaos.db.ChronicleStats;
import com.inheritanceofchaos.db.SaveData;
import com.inheritanceofchaos.db.SaveRepository;
import com.inheritanceofchaos.db.SaveSummary;
import com.inheritanceofchaos.factory.ItemFactory;
import com.inheritanceofchaos.game.ExplorationResult;
import com.inheritanceofchaos.game.ExplorationService;
import com.inheritanceofchaos.game.GameSession;
import com.inheritanceofchaos.game.LocationStatus;
import com.inheritanceofchaos.model.Attribute;
import com.inheritanceofchaos.model.Monster;
import com.inheritanceofchaos.model.Player;
import com.inheritanceofchaos.model.items.Consumable;
import com.inheritanceofchaos.model.items.Item;
import com.inheritanceofchaos.model.skills.Skill;
import com.inheritanceofchaos.model.skills.SkillBook;
import com.inheritanceofchaos.util.GameExecutor;
import com.inheritanceofchaos.world.Location;
import com.inheritanceofchaos.world.World;
import com.inheritanceofchaos.world.WorldRegistry;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// The game window.
//
// It is very simple: a status line at the top, a text log in the middle
// and buttons at the bottom. All menus are normal JOptionPane dialogs.
//
// The window is also a BattleListener, so the battle thread can tell it what is happening.
public class GameWindow extends JFrame implements BattleListener {

    private static final Font MONO = new Font(Font.MONOSPACED, Font.PLAIN, 14);

    private SaveRepository saves = new SaveRepository();
    private ChronicleRepository chronicle = new ChronicleRepository();

    // parts of the window
    private JLabel statusLine = new JLabel(" ");
    private JLabel intentLine = new JLabel(" ");
    private JTextArea log = new JTextArea();
    private CardLayout buttonCards = new CardLayout(); // switches between world buttons and battle buttons
    private JPanel buttonPanel = new JPanel(buttonCards);
    private JComboBox<String> locationBox = new JComboBox<>();
    private List<Location> shownLocations = new ArrayList<>(); // same order as in locationBox
    private JButton attackButton;
    private JButton skillButton;
    private JButton itemButton;
    private JButton defendButton;
    private JButton escapeButton;

    // game state
    private GameSession session;
    private BattleEngine engine;      // not null while a battle is going on
    private Location battleLocation;
    private boolean escapable;

    public GameWindow() {
        super("Inheritance of Chaos");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                quit();
            }
        });

        // top: two lines of text
        statusLine.setFont(MONO.deriveFont(Font.BOLD));
        intentLine.setFont(MONO.deriveFont(Font.BOLD));
        intentLine.setForeground(new Color(176, 64, 0));
        JPanel top = new JPanel(new GridLayout(2, 1));
        top.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        top.add(statusLine);
        top.add(intentLine);

        // middle: the game log
        log.setEditable(false);
        log.setLineWrap(true);
        log.setWrapStyleWord(true);
        log.setFont(MONO);
        log.setMargin(new Insets(8, 8, 8, 8));

        // bottom: buttons
        buttonPanel.add(createWorldButtons(), "world");
        buttonPanel.add(createBattleButtons(), "battle");
        buttonPanel.setVisible(false); // hidden until a game is started or loaded

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(log), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        setSize(980, 660);
        setLocationRelativeTo(null); // center of the screen
    }

    // ==================== creating the buttons ====================

    private JPanel createWorldButtons() {
        locationBox.setPreferredSize(new Dimension(420, 28));

        JButton exploreButton = new JButton("Explore");
        exploreButton.addActionListener(e -> explore());

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row1.add(new JLabel("Location:"));
        row1.add(locationBox);
        row1.add(exploreButton);

        JButton restButton = new JButton("Rest");
        restButton.addActionListener(e -> rest());
        JButton shopButton = new JButton("Merchant");
        shopButton.addActionListener(e -> shop());
        JButton inventoryButton = new JButton("Inventory");
        inventoryButton.addActionListener(e -> inventory());
        JButton characterButton = new JButton("Character");
        characterButton.addActionListener(e -> character());
        JButton travelButton = new JButton("Travel");
        travelButton.addActionListener(e -> travel());
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> save());
        JButton menuButton = new JButton("Main menu");
        menuButton.addActionListener(e -> backToMenu());

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row2.add(restButton);
        row2.add(shopButton);
        row2.add(inventoryButton);
        row2.add(characterButton);
        row2.add(travelButton);
        row2.add(saveButton);
        row2.add(menuButton);

        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(row1);
        panel.add(row2);
        return panel;
    }

    private JPanel createBattleButtons() {
        attackButton = new JButton("Attack");
        attackButton.addActionListener(e -> sendCommand(PlayerCommand.attack()));
        skillButton = new JButton("Skill");
        skillButton.addActionListener(e -> chooseSkill());
        itemButton = new JButton("Item");
        itemButton.addActionListener(e -> chooseItem());
        defendButton = new JButton("Defend");
        defendButton.addActionListener(e -> sendCommand(PlayerCommand.defend()));
        escapeButton = new JButton("Escape");
        escapeButton.addActionListener(e -> sendCommand(PlayerCommand.escape()));

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton[] buttons = {attackButton, skillButton, itemButton, defendButton, escapeButton};
        for (JButton button : buttons) {
            button.setPreferredSize(new Dimension(130, 36));
            panel.add(button);
        }
        return panel;
    }

    // ==================== main menu ====================

    public void showStartMenu() {
        String[] options = {"New game", "Load game", "Chronicle", "Quit"};
        int choice = JOptionPane.showOptionDialog(this,
                "INHERITANCE OF CHAOS\n\nThe old kingdom is ash. Its last heir awakens.",
                "Main menu", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (choice == 0) {
            newGame();
        } else if (choice == 1) {
            loadGame();
        } else if (choice == 2) {
            showChronicle();
        } else {
            quit();
        }
    }

    private void newGame() {
        String name = JOptionPane.showInputDialog(this, "What is your name, heir?", "Wanderer");
        if (name == null) { // the player pressed Cancel
            showStartMenu();
            return;
        }
        name = name.trim();
        if (name.isEmpty()) {
            name = "Wanderer";
        }
        if (name.length() > 20) {
            name = name.substring(0, 20);
        }

        session = GameSession.newGame(name);
        log.setText("");
        write("You wake in the " + session.getCurrentWorld().campName() + ". Your father's rusty sword lies beside you.");
        write("Goal: clear the locations, grow stronger and defeat the boss of each world.");
        write("");
        showWorld();
        autosave();
    }

    // Loading has 2 steps. Both database calls run on the database thread,
    // and the dialogs are shown on the Swing thread with invokeLater.
    private void loadGame() {
        GameExecutor.runInBackground(() -> {
            try {
                List<SaveSummary> list = saves.listSaves();
                SwingUtilities.invokeLater(() -> chooseSave(list));
            } catch (Exception e) {
                showDatabaseError(e);
            }
        });
    }

    private void chooseSave(List<SaveSummary> list) {
        if (list.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No saves yet.");
            showStartMenu();
            return;
        }
        String[] labels = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            SaveSummary s = list.get(i);
            labels[i] = (i + 1) + ". " + s.heroName() + " - level " + s.level() + " - " + s.updatedAt();
        }
        int index = choose("Load game", "Choose a save:", labels);
        if (index < 0) {
            showStartMenu();
            return;
        }

        String id = list.get(index).id();
        GameExecutor.runInBackground(() -> {
            try {
                SaveData data = saves.load(id);
                SwingUtilities.invokeLater(() -> startLoadedGame(data));
            } catch (Exception e) {
                showDatabaseError(e);
            }
        });
    }

    private void startLoadedGame(SaveData data) {
        if (data == null) {
            showStartMenu();
            return;
        }
        session = data.restore();
        log.setText("");
        write("Welcome back, " + session.getPlayer().getName() + ".");
        showWorld();
    }

    private void showChronicle() {
        GameExecutor.runInBackground(() -> {
            try {
                ChronicleStats stats = chronicle.loadStats();
                SwingUtilities.invokeLater(() -> showChronicleDialog(stats));
            } catch (Exception e) {
                showDatabaseError(e);
            }
        });
    }

    private void showChronicleDialog(ChronicleStats stats) {
        String text = "Battles: " + stats.totalBattles()
                + "   Victories: " + stats.victories()
                + "   Defeats: " + stats.defeats()
                + "   Escapes: " + stats.escapes() + "\n\n";

        text += "Most slain:\n";
        for (String line : stats.mostSlain()) {
            text += "  " + line + "\n";
        }

        text += "\nHall of Fame:\n";
        if (stats.hallOfFame().isEmpty()) {
            text += "  nobody yet\n";
        }
        for (String line : stats.hallOfFame()) {
            text += "  " + line + "\n";
        }

        JOptionPane.showMessageDialog(this, textBlock(text), "Chronicle", JOptionPane.PLAIN_MESSAGE);
        showStartMenu();
    }

    private void backToMenu() {
        if (confirm("Return to the main menu? The game will be saved.")) {
            autosave();
            session = null;
            buttonPanel.setVisible(false);
            log.setText("");
            statusLine.setText(" ");
            intentLine.setText(" ");
            showStartMenu();
        }
    }

    // ==================== world ====================

    private void showWorld() {
        buttonPanel.setVisible(true);
        buttonCards.show(buttonPanel, "world");
        intentLine.setText(" ");
        refreshWorld();
    }

    // updates the status line and the list of locations
    private void refreshWorld() {
        Player p = session.getPlayer();
        World world = session.getCurrentWorld();

        String status = p.getName() + "  Lv " + p.getLevel()
                + " | HP " + p.getHealth() + "/" + p.getMaxHealth()
                + " | MP " + p.getMana() + "/" + p.getMaxMana()
                + " | XP " + p.getExperience() + "/" + p.getExperienceToNextLevel()
                + " | Gold " + p.getGold()
                + " | " + world.name();
        if (p.getStatPoints() > 0) {
            status += " | stat points: " + p.getStatPoints();
        }
        statusLine.setText(status);

        shownLocations.clear();
        locationBox.removeAllItems();
        int lastOpen = 0; // select the last location the player can enter
        for (Location location : world.locations()) {
            if (session.canEnter(location)) {
                lastOpen = shownLocations.size();
            }
            shownLocations.add(location);
            locationBox.addItem(describe(location));
        }
        locationBox.setSelectedIndex(lastOpen);
    }

    // text for one line in the location list, for example "Whispering Woods (Lv 1-3) - 2/4 victories"
    private String describe(Location location) {
        String levels;
        if (location.isBossLair()) {
            levels = "BOSS, Lv " + location.getMaxLevel();
        } else {
            levels = "Lv " + location.getMinLevel() + "-" + location.getMaxLevel();
        }

        LocationStatus status = session.getStatus(location);
        String state;
        if (status == LocationStatus.LOCKED_PATH) {
            state = "locked";
        } else if (status == LocationStatus.LOCKED_LEVEL) {
            state = "needs Lv " + location.getRequiredLevel();
        } else if (status == LocationStatus.CLEARED) {
            state = "cleared";
        } else if (status == LocationStatus.CONQUERED) {
            state = "boss defeated";
        } else if (location.isBossLair()) {
            state = "ready";
        } else {
            state = session.getVictories(location) + "/" + location.getVictoriesToClear() + " victories";
        }
        return location.getName() + " (" + levels + ") - " + state;
    }

    private void explore() {
        int index = locationBox.getSelectedIndex();
        if (index < 0) {
            return;
        }
        Location location = shownLocations.get(index);
        if (!session.canEnter(location)) {
            write("You cannot go there yet: " + describe(location));
            return;
        }
        if (location.isBossLair()) {
            if (!confirm("There is no escape from the boss. Enter " + location.getName() + "?")) {
                return;
            }
        }

        ExplorationResult result = ExplorationService.explore(session, location);
        write(result.message());
        if (result.isBattle()) {
            startBattle(result.monster(), location);
        } else {
            refreshWorld();
            autosave();
        }
    }

    private void rest() {
        write(session.rest());
        refreshWorld();
        autosave();
    }

    private void travel() {
        List<World> unlocked = new ArrayList<>();
        for (World world : WorldRegistry.all()) {
            if (session.isWorldUnlocked(world.index())) {
                unlocked.add(world);
            }
        }
        if (unlocked.size() < 2) {
            write("The other worlds are still sealed. Defeat the boss of this world first.");
            return;
        }

        String[] labels = new String[unlocked.size()];
        for (int i = 0; i < unlocked.size(); i++) {
            labels[i] = unlocked.get(i).name();
        }
        int index = choose("Travel", "Where do you want to go?", labels);
        if (index >= 0) {
            World world = unlocked.get(index);
            session.setCurrentWorldIndex(world.index());
            write("You travel to the " + world.name() + ".");
            refreshWorld();
        }
    }

    private void shop() {
        Player p = session.getPlayer();
        World world = session.getCurrentWorld();

        while (true) {
            String[] options = {"Buy", "Sell", "Leave"};
            int choice = JOptionPane.showOptionDialog(this,
                    "Merchant of " + world.campName() + "\nYour gold: " + p.getGold(), "Merchant",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

            if (choice == 0) {
                buy(p, world);
            } else if (choice == 1) {
                sell(p);
            } else {
                break;
            }
            refreshWorld();
        }
        autosave();
    }

    private void buy(Player p, World world) {
        List<Item> stock = ItemFactory.createAll(world.shopStock());
        String[] labels = new String[stock.size()];
        for (int i = 0; i < stock.size(); i++) {
            Item item = stock.get(i);
            labels[i] = item.getName() + " - " + item.getPrice() + " gold (" + item.getStatsText() + ")";
        }

        int index = choose("Buy", "Your gold: " + p.getGold(), labels);
        if (index < 0) {
            return;
        }
        Item item = stock.get(index);
        if (p.spendGold(item.getPrice())) {
            p.getInventory().add(item);
            write("Bought " + item.getName() + ".");
        } else {
            write("Not enough gold for " + item.getName() + ".");
        }
    }

    private void sell(Player p) {
        Map<Item, Integer> bag = p.getInventory().grouped();
        if (bag.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Your bag is empty.");
            return;
        }

        List<Item> items = new ArrayList<>(bag.keySet());
        String[] labels = new String[items.size()];
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            labels[i] = item.getName() + " x" + bag.get(item) + " - sells for " + item.getSellPrice() + " gold";
        }

        int index = choose("Sell", "What do you want to sell?", labels);
        if (index < 0) {
            return;
        }
        Item item = items.get(index);
        p.getInventory().remove(item);
        p.addGold(item.getSellPrice());
        write("Sold " + item.getName() + " for " + item.getSellPrice() + " gold.");
    }

    private void inventory() {
        Player p = session.getPlayer();

        String weaponName = "-";
        if (p.getWeapon() != null) {
            weaponName = p.getWeapon().getName();
        }
        String armorName = "-";
        if (p.getArmor() != null) {
            armorName = p.getArmor().getName();
        }
        String equipped = "Weapon: " + weaponName + "   Armor: " + armorName;

        Map<Item, Integer> bag = p.getInventory().grouped();
        if (bag.isEmpty()) {
            JOptionPane.showMessageDialog(this, equipped + "\n\nYour bag is empty.");
            return;
        }

        List<Item> items = new ArrayList<>(bag.keySet());
        String[] labels = new String[items.size()];
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            labels[i] = item.getName() + " x" + bag.get(item) + " (" + item.getStatsText() + ")";
        }

        int index = choose("Inventory", equipped + "\n\nChoose an item to use or equip:", labels);
        if (index < 0) {
            return;
        }

        Item item = items.get(index);
        if (item instanceof Consumable) {
            Consumable potion = (Consumable) item;
            if (potion.isBattleOnly()) {
                write(potion.getName() + " can only be used in battle.");
            } else {
                p.getInventory().remove(potion);
                write(potion.apply(p, null));
            }
        } else {
            write(p.equip(item)); // weapon or armor
        }
        refreshWorld();
        autosave();
    }

    private void character() {
        Player p = session.getPlayer();

        while (true) {
            String text = "Level " + p.getLevel() + "   XP " + p.getExperience() + "/" + p.getExperienceToNextLevel() + "\n"
                    + "HP " + p.getHealth() + "/" + p.getMaxHealth() + "   MP " + p.getMana() + "/" + p.getMaxMana() + "\n"
                    + "Damage " + p.getDamage() + "   Defense " + p.getDefense() + "\n\n"
                    + "Skills:\n";
            for (Skill skill : SkillBook.all()) {
                if (skill.getRequiredLevel() <= p.getLevel()) {
                    text += "  [+] " + skill.getName() + " (" + skill.getManaCost() + " MP)\n";
                } else {
                    text += "  [ ] " + skill.getName() + " (" + skill.getManaCost() + " MP) - level "
                            + skill.getRequiredLevel() + "\n";
                }
            }

            if (p.getStatPoints() == 0) {
                text += "\nNo stat points to spend.";
                JOptionPane.showMessageDialog(this, textBlock(text), "Character", JOptionPane.PLAIN_MESSAGE);
                break;
            }

            text += "\nStat points: " + p.getStatPoints() + ". What do you want to improve?";
            Attribute[] attributes = Attribute.values();
            String[] options = new String[attributes.length + 1];
            for (int i = 0; i < attributes.length; i++) {
                options[i] = attributes[i].getTitle() + " (" + attributes[i].getEffect() + ")";
            }
            options[attributes.length] = "Done";

            int choice = JOptionPane.showOptionDialog(this, textBlock(text), "Character",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            if (choice < 0 || choice >= attributes.length) {
                break; // "Done" or the window was closed
            }
            p.spendStatPoint(attributes[choice]);
        }
        refreshWorld();
        autosave();
    }

    // ==================== battle ====================

    private void startBattle(Monster monster, Location location) {
        battleLocation = location;
        escapable = !monster.isBoss();
        write("");
        write(monster.getArt());
        buttonCards.show(buttonPanel, "battle");
        setBattleButtonsEnabled(false);

        engine = new BattleEngine(session.getPlayer(), monster, escapable);
        engine.addListener(this);
        engine.start(); // the battle runs in its own thread
    }

    private void sendCommand(PlayerCommand command) {
        if (engine != null && engine.submit(command)) {
            setBattleButtonsEnabled(false); // wait until the enemy has moved
        }
    }

    // The battle thread is waiting for our command right now, so it is safe to read the player here.
    private void chooseSkill() {
        Player p = session.getPlayer();
        List<Skill> skills = p.getUnlockedSkills();
        String[] labels = new String[skills.size()];
        for (int i = 0; i < skills.size(); i++) {
            Skill skill = skills.get(i);
            labels[i] = skill.getName() + " (" + skill.getManaCost() + " MP) - " + skill.getDescription();
        }

        int index = choose("Skill", "Mana: " + p.getMana(), labels);
        if (index >= 0) {
            sendCommand(PlayerCommand.skill(skills.get(index)));
        }
    }

    private void chooseItem() {
        Player p = session.getPlayer();
        List<Consumable> items = p.getInventory().getConsumables();
        if (items.isEmpty()) {
            write("Your bag holds no usable items.");
            return;
        }

        String[] labels = new String[items.size()];
        for (int i = 0; i < items.size(); i++) {
            Consumable item = items.get(i);
            labels[i] = item.getName() + " x" + p.getInventory().count(item) + " (" + item.getStatsText() + ")";
        }

        int index = choose("Item", "Use which item?", labels);
        if (index >= 0) {
            sendCommand(PlayerCommand.item(items.get(index)));
        }
    }

    private void setBattleButtonsEnabled(boolean enabled) {
        attackButton.setEnabled(enabled);
        skillButton.setEnabled(enabled);
        itemButton.setEnabled(enabled);
        defendButton.setEnabled(enabled);
        escapeButton.setEnabled(enabled && escapable); // you cannot run from a boss
    }

    // ----- BattleListener -----
    // These 3 methods are called from the BATTLE thread.
    // Swing components may only be changed from the Swing thread,
    // so everything goes through SwingUtilities.invokeLater.

    @Override
    public void onLog(String message, LogType type) {
        String arrow = "  > ";
        if (type == LogType.ENEMY) {
            arrow = "  < ";
        }
        String line = arrow + message;
        SwingUtilities.invokeLater(() -> write(line));
    }

    @Override
    public void onUpdate(BattleSnapshot s) {
        SwingUtilities.invokeLater(() -> {
            String guard = "";
            if (s.playerDefending()) {
                guard = " [guard]";
            }
            statusLine.setText(s.playerName() + " Lv " + s.playerLevel()
                    + "  HP " + s.playerHealth() + "/" + s.playerMaxHealth()
                    + "  MP " + s.playerMana() + "/" + s.playerMaxMana() + guard
                    + "     vs     "
                    + s.monsterName() + " Lv " + s.monsterLevel()
                    + "  HP " + s.monsterHealth() + "/" + s.monsterMaxHealth());

            String intent = s.monsterIntent();
            if (intent.isEmpty()) {
                intentLine.setText(" ");
            } else {
                intentLine.setText(">> " + intent);
            }
            setBattleButtonsEnabled(s.waitingForPlayer());
        });
    }

    @Override
    public void onBattleEnd(BattleOutcome outcome) {
        SwingUtilities.invokeLater(() -> finishBattle(outcome));
    }

    private void finishBattle(BattleOutcome outcome) {
        engine = null;
        Location location = battleLocation;
        recordBattle(outcome);

        if (outcome.result() == BattleResult.VICTORY) {
            boolean wasCleared = session.isCleared(location);
            session.recordVictory(location);

            if (location.isBossLair()) {
                if (WorldRegistry.isFinalBoss(location)) {
                    completeGame();
                    return;
                }
                // the boss is dead, go to the next world
                int nextIndex = WorldRegistry.worldOf(location).index() + 1;
                World next = WorldRegistry.get(nextIndex);
                session.setCurrentWorldIndex(nextIndex);
                write("*** " + outcome.monsterName() + " has fallen! The way to the " + next.name() + " is open. ***");
                JOptionPane.showMessageDialog(this, outcome.monsterName() + " has fallen!\nYou travel to the "
                        + next.name() + ".");
            } else if (!wasCleared && session.isCleared(location)) {
                write("*** " + location.getName() + " is cleared! A new path opens. ***");
            }

            if (outcome.levelsGained() > 0) {
                write("Press Character to spend your stat points.");
            }
        } else if (outcome.result() == BattleResult.DEFEAT) {
            int lost = session.handleDeath();
            write("You wake up at " + session.getCurrentWorld().campName() + ". You lost " + lost + " gold.");
        }

        write("");
        showWorld();
        autosave();
    }

    private void completeGame() {
        session.markCompleted();
        Player p = session.getPlayer();
        String name = p.getName();
        int level = p.getLevel();
        int won = session.getBattlesWon();
        int deaths = session.getDeaths();

        GameExecutor.runInBackground(() -> {
            try {
                chronicle.addToHallOfFame(name, level, won, deaths);
            } catch (Exception e) {
                showDatabaseError(e);
            }
        });

        write("");
        write("=== THE CHAOS RECEDES ===");
        write("You defeated the Herald of Chaos. Battles won: " + won + ", deaths: " + deaths + ".");
        write("Your name is written in the Hall of Fame. You may keep exploring.");
        write("");
        showWorld();
        autosave();
        JOptionPane.showMessageDialog(this, "You defeated the Herald of Chaos!\nThank you for playing.", "Victory",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void recordBattle(BattleOutcome o) {
        Player p = session.getPlayer();
        BattleRecord record = new BattleRecord(session.getSaveId(), p.getName(), p.getLevel(), o.monsterName(),
                o.monsterLevel(), o.boss(), o.result().name(), o.experience(), o.gold(), o.rounds());

        GameExecutor.runInBackground(() -> {
            try {
                chronicle.recordBattle(record);
            } catch (Exception e) {
                showDatabaseError(e);
            }
        });
    }

    // ==================== saving ====================

    // The copy of the game (SaveData) is made here on the Swing thread,
    // and only this copy goes to the database thread.
    private void autosave() {
        if (session == null || engine != null) {
            return; // no game, or a battle is going on
        }
        SaveData data = SaveData.capture(session);
        GameExecutor.runInBackground(() -> {
            try {
                saves.save(data);
            } catch (Exception e) {
                showDatabaseError(e);
            }
        });
    }

    private void save() {
        autosave();
        // the database thread does tasks in order, so this runs after the save is written
        GameExecutor.runInBackground(() -> SwingUtilities.invokeLater(() -> write("Game saved.")));
    }

    // can be called from any thread
    private void showDatabaseError(Exception error) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "Database error:\n" + error.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            if (session == null) {
                showStartMenu();
            }
        });
    }

    private void quit() {
        if (engine != null) {
            engine.stop(); // in the middle of a battle we keep the save from before the battle
        } else {
            autosave();
        }
        GameExecutor.shutdown(); // waits for the last save to finish
        dispose();
        System.exit(0);
    }

    // ==================== small helpers ====================

    // adds a line to the log and scrolls down
    private void write(String text) {
        if (log.getDocument().getLength() > 0) {
            log.append("\n");
        }
        log.append(text);
        log.setCaretPosition(log.getDocument().getLength());
    }

    private boolean confirm(String question) {
        int answer = JOptionPane.showConfirmDialog(this, question, "Confirm", JOptionPane.YES_NO_OPTION);
        return answer == JOptionPane.YES_OPTION;
    }

    // Shows a list to choose from. Returns the number of the chosen line, or -1 if the player pressed Cancel.
    private int choose(String title, String message, String[] labels) {
        Object picked = JOptionPane.showInputDialog(this, message, title, JOptionPane.PLAIN_MESSAGE, null,
                labels, labels[0]);
        if (picked == null) {
            return -1;
        }
        for (int i = 0; i < labels.length; i++) {
            if (labels[i].equals(picked)) {
                return i;
            }
        }
        return -1;
    }

    // a text area that looks like a normal label, used inside dialogs
    private JTextArea textBlock(String text) {
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setOpaque(false);
        area.setFont(MONO);
        return area;
    }
}
