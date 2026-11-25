package com.inheritanceofchaos.factory;

import com.inheritanceofchaos.model.Monster;
import com.inheritanceofchaos.model.items.Armor;
import com.inheritanceofchaos.model.items.Consumable;
import com.inheritanceofchaos.model.items.Item;
import com.inheritanceofchaos.model.items.Weapon;
import com.inheritanceofchaos.util.Rng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Design pattern "Factory".
// Every item of the game is described here only once and can be found by its key.
// The database only stores keys like "iron_sword", and when a game is loaded
// this factory turns the keys back into items.
public class ItemFactory {

    private static Map<String, Item> items = new HashMap<>();

    static {
        // potions and bombs
        add(new Consumable("minor_potion", "Minor Healing Potion",
                "A murky red draught brewed by camp healers.", 15, Consumable.Effect.HEAL, 40));
        add(new Consumable("healing_potion", "Healing Potion",
                "Thick and bitter. Closes wounds in seconds.", 40, Consumable.Effect.HEAL, 90));
        add(new Consumable("greater_potion", "Greater Healing Potion",
                "Distilled from the hearts of crystal moths.", 90, Consumable.Effect.HEAL, 180));
        add(new Consumable("mana_tonic", "Mana Tonic",
                "Smells of ozone and old books.", 20, Consumable.Effect.MANA, 25));
        add(new Consumable("mana_draught", "Mana Draught",
                "Glows faintly blue in the dark.", 50, Consumable.Effect.MANA, 60));
        add(new Consumable("elixir", "Elixir of the Old Kings",
                "The last bottles from the royal cellar.", 150, Consumable.Effect.RESTORE_ALL, 0));
        add(new Consumable("fire_flask", "Fire Flask",
                "Shatter it and step back.", 30, Consumable.Effect.DAMAGE, 45));
        add(new Consumable("chaos_shard", "Chaos Shard",
                "A sliver of the torn sky. It wants to explode.", 80, Consumable.Effect.DAMAGE, 120));

        // weapons: key, name, description, price, bonus damage, required level
        add(new Weapon("rusty_sword", "Rusty Sword",
                "Your father's sword. It has seen better days.", 20, 3, 1));
        add(new Weapon("hunter_axe", "Hunter's Axe",
                "Heavy, honest and sharp.", 60, 6, 2));
        add(new Weapon("knight_blade", "Knight's Blade",
                "Standard issue of a kingdom that no longer exists.", 140, 10, 4));
        add(new Weapon("oathbreaker", "Oathbreaker",
                "The Fallen Knight's greatsword. It remembers every broken vow.", 300, 14, 5));
        add(new Weapon("bone_cleaver", "Bone Cleaver",
                "Forged by desert nomads from a giant's rib.", 260, 15, 6));
        add(new Weapon("edge_of_chaos", "Edge of Chaos",
                "The blade flickers between this world and another.", 450, 22, 9));

        // armor: key, name, description, price, bonus defense, required level
        add(new Armor("ragged_cloak", "Ragged Cloak",
                "Keeps the ash off. Mostly.", 10, 1, 1));
        add(new Armor("leather_armor", "Leather Armor",
                "Boiled leather, patched many times.", 50, 3, 2));
        add(new Armor("chainmail", "Chainmail",
                "Rings of iron that sing when you move.", 130, 6, 4));
        add(new Armor("bone_mail", "Bone Mail",
                "Ugly, light and surprisingly strong.", 260, 10, 6));
        add(new Armor("voidforged_plate", "Voidforged Plate",
                "Black plate that drinks the light around it.", 460, 14, 9));
    }

    // what monsters can drop. Minor potion is written twice so it drops more often.
    private static String[] earlyConsumables = {"minor_potion", "minor_potion", "healing_potion", "mana_tonic", "fire_flask"};
    private static String[] lateConsumables = {"healing_potion", "healing_potion", "greater_potion", "mana_draught", "chaos_shard"};
    private static String[] earlyGear = {"hunter_axe", "leather_armor", "knight_blade", "chainmail"};
    private static String[] lateGear = {"bone_cleaver", "bone_mail", "edge_of_chaos"};

    private static void add(Item item) {
        items.put(item.getKey(), item);
    }

    // returns the item with this key, throws an error if there is no such item
    public static Item create(String key) {
        Item item = items.get(key);
        if (item == null) {
            throw new IllegalArgumentException("Unknown item: " + key);
        }
        return item;
    }

    // same as create(), but returns null if the item does not exist
    // (used when loading old saves, so a removed item does not crash the game)
    public static Item find(String key) {
        return items.get(key);
    }

    public static List<Item> createAll(List<String> keys) {
        List<Item> result = new ArrayList<>();
        for (String key : keys) {
            result.add(create(key));
        }
        return result;
    }

    public static Item randomConsumable(int level) {
        String[] list = earlyConsumables;
        if (level >= 7) {
            list = lateConsumables;
        }
        return create(randomKey(list));
    }

    private static String randomKey(String[] keys) {
        return keys[Rng.between(0, keys.length - 1)];
    }

    // Loot after a won battle.
    // Bosses always drop their own item and a potion.
    // Normal monsters: 30% chance of a potion and 5% chance of a weapon or armor.
    public static List<Item> rollLoot(Monster monster) {
        List<Item> loot = new ArrayList<>();

        if (monster.getSignatureLoot() != null) {
            loot.add(create(monster.getSignatureLoot()));
        }

        if (monster.isBoss()) {
            loot.add(randomConsumable(monster.getLevel()));
        } else {
            if (Rng.chance(0.3)) {
                loot.add(randomConsumable(monster.getLevel()));
            }
            if (Rng.chance(0.05)) {
                if (monster.getLevel() >= 7) {
                    loot.add(create(randomKey(lateGear)));
                } else {
                    loot.add(create(randomKey(earlyGear)));
                }
            }
        }
        return loot;
    }
}
