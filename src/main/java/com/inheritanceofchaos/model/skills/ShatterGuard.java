package com.inheritanceofchaos.model.skills;

import com.inheritanceofchaos.battle.BattleLog;
import com.inheritanceofchaos.battle.LogType;
import com.inheritanceofchaos.model.Monster;
import com.inheritanceofchaos.model.Player;

// Hit that ignores the enemy's defense. Good against the Crystal Golem.
public class ShatterGuard extends Skill {

    public ShatterGuard() {
        super("Shatter Guard", "A precise strike through the armor: 130% damage, ignores defense.", 9, 5);
    }

    @Override
    public void use(Player user, Monster target, BattleLog log) {
        int dealt = target.takeTrueDamage(user.rollDamage(1.3));
        log.log("The strike pierces right through! " + target.getName() + " takes " + dealt + " damage.", LogType.PLAYER);
    }
}
