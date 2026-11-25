package com.inheritanceofchaos.model;

import com.inheritanceofchaos.model.items.Armor;
import com.inheritanceofchaos.model.items.Inventory;
import com.inheritanceofchaos.model.items.Item;
import com.inheritanceofchaos.model.items.Weapon;
import com.inheritanceofchaos.model.skills.Skill;
import com.inheritanceofchaos.model.skills.SkillBook;

import java.util.List;

// The hero controlled by the player.
public class Player extends GameCharacter {

    public static final int START_HEALTH = 100;
    public static final int START_DAMAGE = 10;
    public static final int START_DEFENSE = 2;
    public static final int START_MANA = 30;
    public static final int STAT_POINTS_PER_LEVEL = 2;

    private int experience;
    private int gold;
    private int mana;
    private int maxMana;
    private int statPoints;
    private Inventory inventory = new Inventory();
    private Weapon weapon;
    private Armor armor;
    private boolean defending;

    public Player(String name) {
        super(name, 1, START_HEALTH, START_DAMAGE, START_DEFENSE);
        maxMana = START_MANA;
        mana = START_MANA;
    }

    // ----- combat -----

    @Override
    public int getDamage() {
        int damage = getBaseDamage();
        if (weapon != null) {
            damage += weapon.getBonusDamage();
        }
        return damage;
    }

    @Override
    public int getDefense() {
        int defense = getBaseDefense();
        if (armor != null) {
            defense += armor.getBonusDefense();
        }
        return defense;
    }

    // while defending every hit is cut in half
    @Override
    protected int changeIncomingDamage(int damage) {
        if (defending) {
            return Math.max(1, (damage + 1) / 2);
        }
        return damage;
    }

    public boolean isDefending() {
        return defending;
    }

    public void setDefending(boolean defending) {
        this.defending = defending;
    }

    // ----- mana -----

    public int getMana() {
        return mana;
    }

    public int getMaxMana() {
        return maxMana;
    }

    // returns false if there is not enough mana
    public boolean spendMana(int amount) {
        if (mana < amount) {
            return false;
        }
        mana -= amount;
        return true;
    }

    // returns how much mana was really restored
    public int restoreMana(int amount) {
        int oldMana = mana;
        mana += amount;
        if (mana > maxMana) {
            mana = maxMana;
        }
        return mana - oldMana;
    }

    public void restoreAll() {
        restoreHealth();
        mana = maxMana;
    }

    // ----- experience and levels -----

    public int getExperience() {
        return experience;
    }

    public int getExperienceToNextLevel() {
        return experienceRequiredFor(getLevel());
    }

    // how much XP you need to go from this level to the next one
    public static int experienceRequiredFor(int level) {
        return 50 * level;
    }

    // adds XP and levels up as many times as needed, returns how many levels were gained
    public int gainExperience(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Experience cannot be negative");
        }
        experience += amount;
        int levelsGained = 0;
        while (experience >= getExperienceToNextLevel()) {
            experience -= getExperienceToNextLevel();
            levelUp();
            levelsGained++;
        }
        return levelsGained;
    }

    private void levelUp() {
        setLevel(getLevel() + 1);
        setMaxHealth(getMaxHealth() + 12);
        setBaseDamage(getBaseDamage() + 1);
        setBaseDefense(getBaseDefense() + 1);
        maxMana += 5;
        statPoints += STAT_POINTS_PER_LEVEL;
        restoreAll();
    }

    public int getStatPoints() {
        return statPoints;
    }

    public boolean spendStatPoint(Attribute attribute) {
        if (statPoints <= 0) {
            return false;
        }
        statPoints--;
        if (attribute == Attribute.VITALITY) {
            setMaxHealth(getMaxHealth() + 10);
            heal(10);
        } else if (attribute == Attribute.STRENGTH) {
            setBaseDamage(getBaseDamage() + 2);
        } else if (attribute == Attribute.ENDURANCE) {
            setBaseDefense(getBaseDefense() + 1);
        } else if (attribute == Attribute.SPIRIT) {
            maxMana += 8;
            mana += 8;
        }
        return true;
    }

    public List<Skill> getUnlockedSkills() {
        return SkillBook.unlockedAt(getLevel());
    }

    // ----- gold -----

    public int getGold() {
        return gold;
    }

    public void addGold(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Gold cannot be negative");
        }
        gold += amount;
    }

    // returns false if there is not enough gold
    public boolean spendGold(int amount) {
        if (gold < amount) {
            return false;
        }
        gold -= amount;
        return true;
    }

    // loses a part of the gold, for example 0.25 = a quarter. Returns how much was lost.
    public int loseGold(double part) {
        int lost = (int) (gold * part);
        gold -= lost;
        return lost;
    }

    // ----- equipment -----

    public Inventory getInventory() {
        return inventory;
    }

    public Weapon getWeapon() {
        return weapon;
    }

    public Armor getArmor() {
        return armor;
    }

    // Takes a weapon or armor from the bag and puts it on.
    // The old one goes back into the bag. Returns a message for the player.
    public String equip(Item item) {
        if (!inventory.contains(item)) {
            return "You do not have " + item.getName() + ".";
        }
        if (item.getRequiredLevel() > getLevel()) {
            return item.getName() + " requires level " + item.getRequiredLevel() + ".";
        }
        if (item instanceof Weapon) {
            inventory.remove(item);
            if (weapon != null) {
                inventory.add(weapon);
            }
            weapon = (Weapon) item;
            return "You now wield " + item.getName() + ".";
        }
        if (item instanceof Armor) {
            inventory.remove(item);
            if (armor != null) {
                inventory.add(armor);
            }
            armor = (Armor) item;
            return "You now wear " + item.getName() + ".";
        }
        return item.getName() + " cannot be equipped.";
    }

    // Puts back all the values from a save file. Only the save system uses this.
    public void restoreState(int level, int experience, int maxHealth, int health, int baseDamage,
                             int baseDefense, int maxMana, int mana, int gold, int statPoints,
                             Weapon weapon, Armor armor) {
        setLevel(level);
        setMaxHealth(maxHealth);
        setHealth(health);
        setBaseDamage(baseDamage);
        setBaseDefense(baseDefense);
        this.experience = experience;
        this.maxMana = maxMana;
        this.mana = Math.min(mana, maxMana);
        this.gold = gold;
        this.statPoints = statPoints;
        this.weapon = weapon;
        this.armor = armor;
    }
}
