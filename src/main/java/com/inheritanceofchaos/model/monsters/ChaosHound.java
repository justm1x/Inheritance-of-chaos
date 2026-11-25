package com.inheritanceofchaos.model.monsters;

import com.inheritanceofchaos.battle.BattleLog;
import com.inheritanceofchaos.model.Monster;
import com.inheritanceofchaos.model.Player;

// Gets stronger every turn, so it is better to kill it fast.
public class ChaosHound extends Monster {

    private static final String ART =
            "    ,_        _,     \n" +
            "    |\\\\______//|     \n" +
            "     \\ @    @ /__    \n" +
            "      \\  VV  /   \\~  \n" +
            "       |_||_|___|    ";

    public ChaosHound(int level) {
        super("Chaos Hound", level, scale(46, 14, level), scale(11, 3.0, level), scale(2, 0.4, level), ART, false);
    }

    // turn 1 = x1.00, turn 2 = x1.12, turn 3 = x1.24 ... but never more than x1.6
    private double frenzyFor(int turnNumber) {
        double frenzy = 1.0 + 0.12 * (turnNumber - 1);
        if (frenzy > 1.6) {
            frenzy = 1.6;
        }
        return frenzy;
    }

    @Override
    protected void act(Player target, BattleLog log) {
        if (getTurn() > 2) {
            attack(target, log, frenzyFor(getTurn()), "savages");
        } else {
            attack(target, log, frenzyFor(getTurn()), "bites");
        }
    }

    @Override
    public String getIntent() {
        double next = frenzyFor(getTurn() + 1);
        return "Frenzy grows: next bite x" + Math.round(next * 100) / 100.0;
    }
}
