package com.inheritanceofchaos.model.skills;

import com.inheritanceofchaos.battle.BattleLog;
import com.inheritanceofchaos.battle.LogType;
import com.inheritanceofchaos.model.Monster;
import com.inheritanceofchaos.model.Player;

// Healing skill.
public class SecondWind extends Skill {

    public SecondWind() {
        super("Second Wind", "Catch your breath and restore 35% of max HP.", 10, 3);
    }

    @Override
    public void use(Player user, Monster target, BattleLog log) {
        int amount = (int) Math.round(user.getMaxHealth() * 0.35);
        int healed = user.heal(amount);
        log.log(user.getName() + " steadies their breathing and recovers " + healed + " HP.", LogType.HEAL);
    }
}
