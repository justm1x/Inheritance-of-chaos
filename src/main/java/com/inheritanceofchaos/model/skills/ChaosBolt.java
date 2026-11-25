package com.inheritanceofchaos.model.skills;

import com.inheritanceofchaos.battle.BattleLog;
import com.inheritanceofchaos.battle.LogType;
import com.inheritanceofchaos.model.Monster;
import com.inheritanceofchaos.model.Player;

// The chaos in the hero's blood: huge damage, but it also hurts the hero a bit (it can never kill).
public class ChaosBolt extends Skill {

    public ChaosBolt() {
        super("Chaos Bolt", "Unleash the chaos in your blood: 260% damage, ignores defense, costs 8% of your max HP.", 16, 7);
    }

    @Override
    public void use(Player user, Monster target, BattleLog log) {
        int dealt = target.takeTrueDamage(user.rollDamage(2.6));
        int recoil = user.sacrificeHealth((int) Math.round(user.getMaxHealth() * 0.08));
        log.log("Raw chaos erupts from your hands! " + target.getName() + " takes " + dealt + " damage.", LogType.PLAYER);
        if (recoil > 0) {
            log.log("The chaos burns you as well: -" + recoil + " HP.", LogType.WARNING);
        }
    }
}
