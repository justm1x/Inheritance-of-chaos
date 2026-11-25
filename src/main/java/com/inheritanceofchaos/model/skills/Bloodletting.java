package com.inheritanceofchaos.model.skills;

import com.inheritanceofchaos.battle.BattleLog;
import com.inheritanceofchaos.battle.LogType;
import com.inheritanceofchaos.model.Monster;
import com.inheritanceofchaos.model.Player;

// Hit the enemy and heal yourself for half of the damage.
public class Bloodletting extends Skill {

    public Bloodletting() {
        super("Bloodletting", "A cruel cut: 160% damage and heals you for half of it.", 14, 9);
    }

    @Override
    public void use(Player user, Monster target, BattleLog log) {
        int dealt = target.takeDamage(user.rollDamage(1.6));
        int healed = user.heal(dealt / 2);
        log.log(target.getName() + " bleeds for " + dealt + " damage; you drink in " + healed + " HP.", LogType.PLAYER);
    }
}
