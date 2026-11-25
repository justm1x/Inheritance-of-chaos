package com.inheritanceofchaos.model.monsters;

import com.inheritanceofchaos.battle.BattleLog;
import com.inheritanceofchaos.battle.LogType;
import com.inheritanceofchaos.model.Monster;
import com.inheritanceofchaos.model.Player;
import com.inheritanceofchaos.util.Rng;

// Drains life through armor and heals itself with it.
public class Wraith extends Monster {

    private static final String ART =
            "      .-~~~-.      \n" +
            "     /  O O  \\     \n" +
            "    |    o    |    \n" +
            "     \\  ~~~  /     \n" +
            "      ) ) ) )      \n" +
            "     ( ( ( (       ";

    public Wraith(int level) {
        super("Wraith", level, scale(48, 14, level), scale(12, 3.0, level), scale(1, 0.3, level), ART, false);
    }

    @Override
    protected void act(Player target, BattleLog log) {
        if (Rng.chance(0.35)) {
            int dealt = trueAttack(target, 0.8);
            int healed = heal(dealt);
            log.log("Wraith drains your life force: " + dealt + " damage, it heals " + healed + ".", LogType.ENEMY);
        } else {
            attack(target, log, 1.0, "claws at");
        }
    }
}
