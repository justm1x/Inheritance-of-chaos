package com.inheritanceofchaos.model.monsters;

import com.inheritanceofchaos.battle.BattleLog;
import com.inheritanceofchaos.battle.LogType;
import com.inheritanceofchaos.model.Monster;
import com.inheritanceofchaos.model.Player;
import com.inheritanceofchaos.util.Rng;

// Weak monster, but its acid goes through armor.
public class Slime extends Monster {

    private static final String ART =
            "     .-~~~~-.     \n" +
            "   .'  o  o  '.   \n" +
            "  (     __     )  \n" +
            "   '-.______.-'   ";

    public Slime(int level) {
        super("Slime", level, scale(42, 13, level), scale(10, 2.7, level), scale(0, 0.3, level), ART, false);
    }

    @Override
    protected void act(Player target, BattleLog log) {
        if (Rng.chance(0.3)) {
            int dealt = trueAttack(target, 0.8);
            log.log("Slime spits corrosive acid! It burns through your armor: " + dealt + " damage.", LogType.ENEMY);
        } else {
            attack(target, log, 1.0, "slams into");
        }
    }
}
