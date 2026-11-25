package com.inheritanceofchaos.model.monsters;

import com.inheritanceofchaos.battle.BattleLog;
import com.inheritanceofchaos.battle.LogType;
import com.inheritanceofchaos.model.Monster;
import com.inheritanceofchaos.model.Player;

// BOSS of the first world.
// Every third turn he does a Crushing Blow. The turn before he warns about it, so you can Defend.
// When his HP drops to 50% he gets angry and hits 20% harder.
public class FallenKnight extends Monster {

    private static final String ART =
            "         _||_          \n" +
            "        / __ \\     |   \n" +
            "       | |__| |    |   \n" +
            "    ___|  ==  |____|   \n" +
            "   |   |______|    |   \n" +
            "       /  ||  \\    |   \n" +
            "      /___||___\\       ";

    private boolean enraged = false;

    public FallenKnight(int level) {
        super("Fallen Knight", level,
                300 + 30 * (level - 6),
                28 + 3 * (level - 6),
                8 + (level - 6),
                ART, true);
    }

    @Override
    protected void act(Player target, BattleLog log) {
        if (!enraged && getHealthRatio() <= 0.5) {
            enraged = true;
            log.log("The Fallen Knight's eyes ignite with hatred. His strikes grow fiercer!", LogType.WARNING);
        }

        double fury = 1.0;
        if (enraged) {
            fury = 1.2;
        }

        if (getTurn() % 3 == 0) {
            attack(target, log, 2.3 * fury, "brings Oathbreaker down in a CRUSHING BLOW on");
        } else {
            attack(target, log, fury, "slashes at");
        }
    }

    @Override
    public String getIntent() {
        int nextTurn = getTurn() + 1;
        if (nextTurn % 3 == 0) {
            return "The Fallen Knight raises his blade high... (Defend!)";
        }
        if (enraged) {
            return "Enraged: attacks deal 20% more damage";
        }
        return "";
    }

    // he always drops his sword
    @Override
    public String getSignatureLoot() {
        return "oathbreaker";
    }

    @Override
    public String getIntro() {
        return "The Fallen Knight rises from his ruined throne. \"None shall pass the Keep.\"";
    }
}
