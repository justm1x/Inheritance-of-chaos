package com.inheritanceofchaos.model.items;

// Armor adds defense to the hero.
public class Armor extends Item {

    private int bonusDefense;
    private int requiredLevel;

    public Armor(String key, String name, String description, int price, int bonusDefense, int requiredLevel) {
        super(key, name, description, price);
        this.bonusDefense = bonusDefense;
        this.requiredLevel = requiredLevel;
    }

    public int getBonusDefense() {
        return bonusDefense;
    }

    @Override
    public int getRequiredLevel() {
        return requiredLevel;
    }

    @Override
    public String getStatsText() {
        String text = "+" + bonusDefense + " defense";
        if (requiredLevel > 1) {
            text += ", requires Lv " + requiredLevel;
        }
        return text;
    }
}
