package com.inheritanceofchaos.model.monsters;

import com.inheritanceofchaos.battle.BattleLog;
import com.inheritanceofchaos.battle.LogType;
import com.inheritanceofchaos.model.Monster;
import com.inheritanceofchaos.model.Player;
import com.inheritanceofchaos.util.Rng;

// Sometimes bites twice in one turn.
public class Wolf extends Monster {

    private static final String ART =
            "      /\\    /\\     \n" +
            "     /  \\__/  \\    \n" +
            "    |  o    o  |   \n" +
            "     \\   /\\   /    \n" +
            "      \\_/__\\_/     \n" +
            "       VVVVVV      ";

    public Wolf(int level) {
        super("Wolf", level, scale(44, 13, level), scale(12, 2.9, level), scale(1, 0.3, level), ART, false);
    }

    @Override
    protected void act(Player target, BattleLog log) {
        if (Rng.chance(0.3)) {
            log.log("Wolf lunges in a frenzy!", LogType.ENEMY);
            attack(target, log, 0.65, "bites");
            if (target.isAlive()) {
                attack(target, log, 0.65, "bites");
            }
        } else {
            attack(target, log, 1.0, "bites");
        }
    }
}
