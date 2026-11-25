package com.inheritanceofchaos.model.items;

// A weapon adds damage to the hero.
public class Weapon extends Item {

    private int bonusDamage;
    private int requiredLevel;

    public Weapon(String key, String name, String description, int price, int bonusDamage, int requiredLevel) {
        super(key, name, description, price);
        this.bonusDamage = bonusDamage;
        this.requiredLevel = requiredLevel;
    }

    public int getBonusDamage() {
        return bonusDamage;
    }

    @Override
    public int getRequiredLevel() {
        return requiredLevel;
    }

    @Override
    public String getStatsText() {
        String text = "+" + bonusDamage + " damage";
        if (requiredLevel > 1) {
            text += ", requires Lv " + requiredLevel;
        }
        return text;
    }
}
