package com.inheritanceofchaos.db;

import com.inheritanceofchaos.factory.ItemFactory;
import com.inheritanceofchaos.game.GameSession;
import com.inheritanceofchaos.model.Player;
import com.inheritanceofchaos.model.items.Armor;
import com.inheritanceofchaos.model.items.Item;
import com.inheritanceofchaos.model.items.Weapon;

import java.util.HashMap;
import java.util.Map;

// All the numbers of one save, ready to be written to the database.
//
// It is created on the Swing thread and then given to the database thread.
// Because it is a copy, the database thread never reads the real Player while the game changes it.
public record SaveData(
        String id, String heroName, int level, int experience, int health, int maxHealth,
        int baseDamage, int baseDefense, int mana, int maxMana, int gold, int statPoints,
        String weaponKey, String armorKey, int worldIndex, int deaths, int battlesWon, boolean completed,
        Map<String, Integer> inventory, Map<String, Integer> victories) {

    // makes a copy of the current game
    public static SaveData capture(GameSession session) {
        Player player = session.getPlayer();

        // item key -> how many
        Map<String, Integer> inventory = new HashMap<>();
        Map<Item, Integer> bag = player.getInventory().grouped();
        for (Item item : bag.keySet()) {
            inventory.put(item.getKey(), bag.get(item));
        }

        // copy of the map, so later victories do not change this save
        Map<String, Integer> victories = new HashMap<>(session.getAllVictories());

        String weaponKey = null;
        if (player.getWeapon() != null) {
            weaponKey = player.getWeapon().getKey();
        }
        String armorKey = null;
        if (player.getArmor() != null) {
            armorKey = player.getArmor().getKey();
        }

        return new SaveData(
                session.getSaveId(), player.getName(), player.getLevel(), player.getExperience(),
                player.getHealth(), player.getMaxHealth(), player.getBaseDamage(), player.getBaseDefense(),
                player.getMana(), player.getMaxMana(), player.getGold(), player.getStatPoints(),
                weaponKey, armorKey,
                session.getCurrentWorldIndex(), session.getDeaths(), session.getBattlesWon(), session.isCompleted(),
                inventory, victories);
    }

    // builds a real game from the saved numbers
    public GameSession restore() {
        Player player = new Player(heroName);

        Weapon weapon = null;
        Item savedWeapon = ItemFactory.find(weaponKey);
        if (savedWeapon instanceof Weapon) {
            weapon = (Weapon) savedWeapon;
        }
        Armor armor = null;
        Item savedArmor = ItemFactory.find(armorKey);
        if (savedArmor instanceof Armor) {
            armor = (Armor) savedArmor;
        }

        player.restoreState(level, experience, maxHealth, health, baseDamage, baseDefense,
                maxMana, mana, gold, statPoints, weapon, armor);

        for (String key : inventory.keySet()) {
            Item item = ItemFactory.find(key);
            if (item != null) { // skip items that do not exist in the game anymore
                player.getInventory().add(item, inventory.get(key));
            }
        }

        GameSession session = new GameSession(id, player);
        for (String locationId : victories.keySet()) {
            session.setVictories(locationId, victories.get(locationId));
        }
        session.restoreCounters(deaths, battlesWon, completed);
        if (session.isWorldUnlocked(worldIndex)) {
            session.setCurrentWorldIndex(worldIndex);
        }
        return session;
    }
}
