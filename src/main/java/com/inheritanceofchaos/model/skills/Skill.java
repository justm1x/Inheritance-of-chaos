package com.inheritanceofchaos.model.skills;

import com.inheritanceofchaos.battle.BattleLog;
import com.inheritanceofchaos.model.Monster;
import com.inheritanceofchaos.model.Player;

// Design pattern "Strategy".
// Every skill is its own class with the same use() method.
// The battle does not need to know what a skill does, it just calls skill.use(...).
// To add a new skill we only need to write a new class, the battle code stays the same.
public abstract class Skill {

    private String name;
    private String description;
    private int manaCost;
    private int requiredLevel;

    public Skill(String name, String description, int manaCost, int requiredLevel) {
        this.name = name;
        this.description = description;
        this.manaCost = manaCost;
        this.requiredLevel = requiredLevel;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getManaCost() {
        return manaCost;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    // Does the skill. The mana is already paid by the battle before this is called.
    public abstract void use(Player user, Monster target, BattleLog log);

    @Override
    public String toString() {
        return name;
    }
}
