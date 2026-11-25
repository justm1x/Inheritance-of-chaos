package com.inheritanceofchaos.model.monsters;

import com.inheritanceofchaos.battle.BattleLog;
import com.inheritanceofchaos.battle.LogType;
import com.inheritanceofchaos.model.Monster;
import com.inheritanceofchaos.model.Player;
import com.inheritanceofchaos.util.Rng;

// Casts curses that ignore armor.
public class Cultist extends Monster {

    private static final String ART =
            "        /\\         \n" +
            "       /  \\        \n" +
            "      / ** \\       \n" +
            "     /  ''  \\      \n" +
            "    /________\\     \n" +
            "     |  ||  |      ";

    public Cultist(int level) {
        super("Cultist", level, scale(50, 15, level), scale(12, 3.0, level), scale(2, 0.5, level), ART, false);
    }

    @Override
    protected void act(Player target, BattleLog log) {
        if (Rng.chance(0.3)) {
            int dealt = trueAttack(target, 1.0);
            log.log("Cultist chants a curse of Chaos! Your veins burn: " + dealt + " damage.", LogType.ENEMY);
        } else {
            attack(target, log, 1.0, "strikes");
        }
    }
}
