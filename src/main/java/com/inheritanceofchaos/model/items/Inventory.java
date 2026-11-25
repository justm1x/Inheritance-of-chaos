package com.inheritanceofchaos.model.items;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// The hero's bag. The weapon and armor the hero is wearing are NOT in here,
// they are stored in Player.
public class Inventory {

    private List<Item> items = new ArrayList<>();

    public void add(Item item) {
        items.add(item);
    }

    // adds several copies of the same item, for example 3 potions
    public void add(Item item, int count) {
        for (int i = 0; i < count; i++) {
            items.add(item);
        }
    }

    // removes one copy, returns false if there was no such item
    public boolean remove(Item item) {
        return items.remove(item);
    }

    public boolean contains(Item item) {
        return items.contains(item);
    }

    // how many copies of this item are in the bag
    public int count(Item item) {
        int count = 0;
        for (Item i : items) {
            if (i.equals(item)) {
                count++;
            }
        }
        return count;
    }

    // Item -> how many of them. LinkedHashMap keeps the order in which items were found.
    public Map<Item, Integer> grouped() {
        Map<Item, Integer> result = new LinkedHashMap<>();
        for (Item item : items) {
            if (result.containsKey(item)) {
                result.put(item, result.get(item) + 1);
            } else {
                result.put(item, 1);
            }
        }
        return result;
    }

    // all potions and bombs, each kind only once
    public List<Consumable> getConsumables() {
        List<Consumable> result = new ArrayList<>();
        for (Item item : grouped().keySet()) {
            if (item instanceof Consumable) {
                result.add((Consumable) item);
            }
        }
        return result;
    }
}
