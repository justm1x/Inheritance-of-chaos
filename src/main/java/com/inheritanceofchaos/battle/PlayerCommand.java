package com.inheritanceofchaos.battle;

import com.inheritanceofchaos.model.items.Consumable;
import com.inheritanceofchaos.model.skills.Skill;

// Design pattern "Command".
// The window does not call the battle code directly. It packs the player's choice
// into a PlayerCommand object and puts it into the battle thread's queue.
// skill is only filled for SKILL, item is only filled for ITEM, otherwise they are null.
public record PlayerCommand(ActionType type, Skill skill, Consumable item) {

    public static PlayerCommand attack() {
        return new PlayerCommand(ActionType.ATTACK, null, null);
    }

    public static PlayerCommand skill(Skill skill) {
        return new PlayerCommand(ActionType.SKILL, skill, null);
    }

    public static PlayerCommand item(Consumable item) {
        return new PlayerCommand(ActionType.ITEM, null, item);
    }

    public static PlayerCommand defend() {
        return new PlayerCommand(ActionType.DEFEND, null, null);
    }

    public static PlayerCommand escape() {
        return new PlayerCommand(ActionType.ESCAPE, null, null);
    }
}
