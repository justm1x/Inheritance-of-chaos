package com.inheritanceofchaos.model.skills;

import com.inheritanceofchaos.battle.BattleLog;
import com.inheritanceofchaos.battle.LogType;
import com.inheritanceofchaos.model.Monster;
import com.inheritanceofchaos.model.Player;

// First skill: just a strong hit.
public class PowerStrike extends Skill {

    public PowerStrike() {
        super("Power Strike", "A heavy two-handed blow: 180% damage.", 6, 1);
    }

    @Override
    public void use(Player user, Monster target, BattleLog log) {
        int dealt = target.takeDamage(user.rollDamage(1.8));
        log.log("The blow lands hard! " + target.getName() + " takes " + dealt + " damage.", LogType.PLAYER);
    }
}
