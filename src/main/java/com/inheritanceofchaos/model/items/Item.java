package com.inheritanceofchaos.model.items;

// Base class for every item.
// Every item has a unique key (for example "iron_sword"). The key is what we store in the database.
public abstract class Item {

    private String key;
    private String name;
    private String description;
    private int price;

    public Item(String key, String name, String description, int price) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Item key cannot be empty");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Item name cannot be empty");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.key = key;
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getPrice() {
        return price;
    }

    // the merchant buys items for half of the price
    public int getSellPrice() {
        int sellPrice = price / 2;
        if (sellPrice < 1) {
            sellPrice = 1;
        }
        return sellPrice;
    }

    public int getRequiredLevel() {
        return 1;
    }

    // short text about what the item does, for example "+6 damage"
    public abstract String getStatsText();

    // Two items are the same if they have the same key.
    // This is needed so the inventory can find and count items correctly.
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Item)) {
            return false;
        }
        Item otherItem = (Item) other;
        return key.equals(otherItem.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
