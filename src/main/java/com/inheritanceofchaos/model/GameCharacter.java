package com.inheritanceofchaos.model;

import com.inheritanceofchaos.util.Rng;

// Base class for everyone who can fight: the player and all the monsters.
// (We called it GameCharacter because Java already has a class named Character.)
public abstract class GameCharacter {

    private String name;
    private int level;
    private int health;
    private int maxHealth;
    private int baseDamage;
    private int baseDefense;

    public GameCharacter(String name, int level, int maxHealth, int baseDamage, int baseDefense) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (level < 1) {
            throw new IllegalArgumentException("Level must be at least 1");
        }
        if (maxHealth <= 0) {
            throw new IllegalArgumentException("Max health must be positive");
        }
        if (baseDamage < 0 || baseDefense < 0) {
            throw new IllegalArgumentException("Damage and defense cannot be negative");
        }
        this.name = name.trim();
        this.level = level;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.baseDamage = baseDamage;
        this.baseDefense = baseDefense;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    protected void setLevel(int level) {
        if (level < 1) {
            throw new IllegalArgumentException("Level must be at least 1");
        }
        this.level = level;
    }

    public int getHealth() {
        return health;
    }

    // health always stays between 0 and maxHealth
    public void setHealth(int health) {
        if (health < 0) {
            health = 0;
        }
        if (health > maxHealth) {
            health = maxHealth;
        }
        this.health = health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    protected void setMaxHealth(int maxHealth) {
        if (maxHealth <= 0) {
            throw new IllegalArgumentException("Max health must be positive");
        }
        this.maxHealth = maxHealth;
        if (health > maxHealth) {
            health = maxHealth;
        }
    }

    public int getBaseDamage() {
        return baseDamage;
    }

    protected void setBaseDamage(int baseDamage) {
        if (baseDamage < 0) {
            throw new IllegalArgumentException("Damage cannot be negative");
        }
        this.baseDamage = baseDamage;
    }

    public int getBaseDefense() {
        return baseDefense;
    }

    protected void setBaseDefense(int baseDefense) {
        if (baseDefense < 0) {
            throw new IllegalArgumentException("Defense cannot be negative");
        }
        this.baseDefense = baseDefense;
    }

    // Total damage. Player overrides this to add the weapon bonus.
    public int getDamage() {
        return baseDamage;
    }

    // Total defense. Player overrides this to add the armor bonus.
    public int getDefense() {
        return baseDefense;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public double getHealthRatio() {
        return (double) health / maxHealth;
    }

    // Damage of one attack: damage * multiplier, plus or minus 15% so fights are less predictable.
    public int rollDamage(double multiplier) {
        double randomFactor = 0.85 + Rng.roll() * 0.3;
        int damage = (int) Math.round(getDamage() * multiplier * randomFactor);
        if (damage < 1) {
            damage = 1;
        }
        return damage;
    }

    // Normal hit: defense is subtracted, but at least 1 damage always goes through.
    // Returns how much health was really lost.
    public int takeDamage(int damage) {
        if (damage < 0) {
            throw new IllegalArgumentException("Damage cannot be negative");
        }
        int afterDefense = damage - getDefense();
        if (afterDefense < 1) {
            afterDefense = 1;
        }
        return loseHealth(changeIncomingDamage(afterDefense));
    }

    // Hit that ignores defense (curses, acid, chaos magic).
    public int takeTrueDamage(int damage) {
        if (damage < 0) {
            throw new IllegalArgumentException("Damage cannot be negative");
        }
        return loseHealth(changeIncomingDamage(damage));
    }

    // Lose health but never die from it (traps, skill costs). Always leaves at least 1 HP.
    public int sacrificeHealth(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        int canLose = Math.min(amount, health - 1);
        if (canLose < 0) {
            canLose = 0;
        }
        health -= canLose;
        return canLose;
    }

    // Player overrides this to take half damage while defending.
    protected int changeIncomingDamage(int damage) {
        return damage;
    }

    private int loseHealth(int damage) {
        int oldHealth = health;
        health -= damage;
        if (health < 0) {
            health = 0;
        }
        return oldHealth - health;
    }

    // Returns how much health was really restored.
    public int heal(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Healing cannot be negative");
        }
        int oldHealth = health;
        health += amount;
        if (health > maxHealth) {
            health = maxHealth;
        }
        return health - oldHealth;
    }

    public void restoreHealth() {
        health = maxHealth;
    }

    @Override
    public String toString() {
        return name + " (Lv " + level + ", HP " + health + "/" + maxHealth + ")";
    }
}
