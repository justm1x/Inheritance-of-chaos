package com.inheritanceofchaos.model.monsters;

import com.inheritanceofchaos.battle.BattleLog;
import com.inheritanceofchaos.battle.LogType;
import com.inheritanceofchaos.model.Monster;
import com.inheritanceofchaos.model.Player;
import com.inheritanceofchaos.util.Rng;

// FINAL BOSS.
// Every fourth turn he casts Chaos Storm, which ignores armor (he warns one turn before).
// Below 50% HP the second phase starts: stronger attacks and life drain.
public class HeraldOfChaos extends Monster {

    private static final String ART =
            "    \\   |   /   |   /    \n" +
            "    .--'''''''''--.      \n" +
            "   /   @       @   \\     \n" +
            "  |  ~~~~~~~~~~~~~  |    \n" +
            "   \\  \\/\\/\\/\\/\\/  /     \n" +
            "    '-._________.-'      \n" +
            "     ~~~ CHAOS ~~~       ";

    private boolean awakened = false;

    public HeraldOfChaos(int level) {
        super("Herald of Chaos", level,
                700 + 50 * (level - 13),
                46 + 3 * (level - 13),
                12 + (level - 13),
                ART, true);
    }

    @Override
    protected void act(Player target, BattleLog log) {
        if (!awakened && getHealthRatio() <= 0.5) {
            awakened = true;
            log.log("THE CHAOS WITHIN AWAKENS! The Herald tears reality open around you!", LogType.WARNING);
        }

        double power = 1.0;
        if (awakened) {
            power = 1.25;
        }

        if (getTurn() % 4 == 0) {
            int dealt = trueAttack(target, 2.2 * power);
            log.log("Herald of Chaos unleashes CHAOS STORM! Raw chaos tears through you: " + dealt + " damage.",
                    LogType.ENEMY);
        } else if (awakened && Rng.chance(0.3)) {
            int dealt = trueAttack(target, 0.8);
            int healed = heal(dealt);
            log.log("Herald of Chaos feeds on your despair: " + dealt + " damage, it heals " + healed + ".",
                    LogType.ENEMY);
        } else if (awakened) {
            attack(target, log, power, "rends");
        } else {
            attack(target, log, power, "strikes");
        }
    }

    @Override
    public String getIntent() {
        int nextTurn = getTurn() + 1;
        if (nextTurn % 4 == 0) {
            return "Reality twists around the Herald... CHAOS STORM incoming! (Defend!)";
        }
        if (awakened) {
            return "Chaos awakened: stronger attacks, life drain";
        }
        return "";
    }

    @Override
    public String getIntro() {
        return "The Herald of Chaos turns its gaze upon you. \"So, the last heir has come to inherit the end.\"";
    }
}
