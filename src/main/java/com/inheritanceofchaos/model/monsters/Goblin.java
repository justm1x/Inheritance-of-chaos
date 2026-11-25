package com.inheritanceofchaos.model.monsters;

import com.inheritanceofchaos.battle.BattleLog;
import com.inheritanceofchaos.battle.LogType;
import com.inheritanceofchaos.model.Monster;
import com.inheritanceofchaos.model.Player;
import com.inheritanceofchaos.util.Rng;

// Sometimes steals health from the hero (this was the stealHealth idea from our first version).
public class Goblin extends Monster {

    private static final String ART =
            "      _.---._      \n" +
            "     / >   < \\     \n" +
            "    |    ^    |    \n" +
            "  --|  \\___/  |--  \n" +
            "     \\_______/     ";

    public Goblin(int level) {
        super("Goblin", level, scale(48, 14, level), scale(11, 2.9, level), scale(1, 0.4, level), ART, false);
    }

    @Override
    protected void act(Player target, BattleLog log) {
        if (Rng.chance(0.3)) {
            stealHealth(target, log);
        } else {
            attack(target, log, 1.0, "stabs");
        }
    }

    private void stealHealth(Player target, BattleLog log) {
        int dealt = target.takeDamage(rollDamage(0.9));
        int healed = heal(dealt);
        log.log("Goblin steals health from " + target.getName() + ": " + dealt + " damage, heals itself for "
                + healed + ".", LogType.ENEMY);
    }
}
