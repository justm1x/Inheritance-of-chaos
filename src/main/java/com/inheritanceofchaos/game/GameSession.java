package com.inheritanceofchaos.game;

import com.inheritanceofchaos.factory.ItemFactory;
import com.inheritanceofchaos.model.Player;
import com.inheritanceofchaos.world.Location;
import com.inheritanceofchaos.world.World;
import com.inheritanceofchaos.world.WorldRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// One playthrough: the hero and how far he got in the worlds.
public class GameSession {

    private String saveId;
    private Player player;
    // location id -> how many battles were won there
    private Map<String, Integer> victories = new HashMap<>();
    private int currentWorldIndex = 0;
    private int deaths = 0;
    private int battlesWon = 0;
    private boolean completed = false;

    public GameSession(String saveId, Player player) {
        this.saveId = saveId;
        this.player = player;
    }

    // a new hero with the starting equipment
    public static GameSession newGame(String heroName) {
        Player player = new Player(heroName);

        player.getInventory().add(ItemFactory.create("rusty_sword"));
        player.equip(ItemFactory.create("rusty_sword"));
        player.getInventory().add(ItemFactory.create("ragged_cloak"));
        player.equip(ItemFactory.create("ragged_cloak"));

        player.getInventory().add(ItemFactory.create("minor_potion"), 3);
        player.getInventory().add(ItemFactory.create("mana_tonic"));
        player.addGold(30);

        // random unique id, so every save has its own row in the database
        String saveId = UUID.randomUUID().toString();
        return new GameSession(saveId, player);
    }

    public String getSaveId() {
        return saveId;
    }

    public Player getPlayer() {
        return player;
    }

    // ===== worlds =====

    public World getCurrentWorld() {
        return WorldRegistry.get(currentWorldIndex);
    }

    public int getCurrentWorldIndex() {
        return currentWorldIndex;
    }

    public void setCurrentWorldIndex(int index) {
        if (!isWorldUnlocked(index)) {
            throw new IllegalStateException("This world is still sealed");
        }
        currentWorldIndex = index;
    }

    // the next world opens after the boss of the previous world is beaten
    public boolean isWorldUnlocked(int index) {
        if (index < 0 || index >= WorldRegistry.count()) {
            return false;
        }
        if (index == 0) {
            return true;
        }
        World previousWorld = WorldRegistry.get(index - 1);
        return isCleared(previousWorld.bossLair());
    }

    // ===== locations =====

    public int getVictories(Location location) {
        Integer count = victories.get(location.getId());
        if (count == null) {
            return 0;
        }
        return count;
    }

    public Map<String, Integer> getAllVictories() {
        return victories;
    }

    public void setVictories(String locationId, int count) {
        victories.put(locationId, count);
    }

    public void recordVictory(Location location) {
        victories.put(location.getId(), getVictories(location) + 1);
        battlesWon++;
    }

    public boolean isCleared(Location location) {
        return getVictories(location) >= location.getVictoriesToClear();
    }

    public LocationStatus getStatus(Location location) {
        World world = WorldRegistry.worldOf(location);
        int index = world.locations().indexOf(location);

        // you have to clear the previous location first
        if (index > 0) {
            Location previous = world.locations().get(index - 1);
            if (!isCleared(previous)) {
                return LocationStatus.LOCKED_PATH;
            }
        }
        if (player.getLevel() < location.getRequiredLevel()) {
            return LocationStatus.LOCKED_LEVEL;
        }
        if (isCleared(location)) {
            if (location.isBossLair()) {
                return LocationStatus.CONQUERED;
            }
            return LocationStatus.CLEARED;
        }
        return LocationStatus.AVAILABLE;
    }

    public boolean canEnter(Location location) {
        LocationStatus status = getStatus(location);
        return status == LocationStatus.AVAILABLE || status == LocationStatus.CLEARED;
    }

    // ===== camp =====

    public int getRestCost() {
        return 4 * player.getLevel();
    }

    // Rest at the camp. If you have no gold you still get half of your HP and MP back.
    public String rest() {
        if (player.getHealth() == player.getMaxHealth() && player.getMana() == player.getMaxMana()) {
            return "You are already fully rested.";
        }
        int cost = getRestCost();
        if (player.spendGold(cost)) {
            player.restoreAll();
            return "You rest by the campfire (-" + cost + " gold). Health and mana are fully restored.";
        }
        player.heal(player.getMaxHealth() / 2);
        player.restoreMana(player.getMaxMana() / 2);
        return "Without coin for a warm bed, you sleep on the cold ground. Half of your strength returns.";
    }

    // The hero died: he loses a quarter of his gold and wakes up at the camp.
    // Returns how much gold was lost.
    public int handleDeath() {
        deaths++;
        int lost = player.loseGold(0.25);
        player.setDefending(false);
        player.setHealth(player.getMaxHealth() / 2);
        player.restoreMana(player.getMaxMana() / 2);
        return lost;
    }

    // ===== statistics =====

    public int getDeaths() {
        return deaths;
    }

    public int getBattlesWon() {
        return battlesWon;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void markCompleted() {
        completed = true;
    }

    // used when a game is loaded from the database
    public void restoreCounters(int deaths, int battlesWon, boolean completed) {
        this.deaths = deaths;
        this.battlesWon = battlesWon;
        this.completed = completed;
    }
}
