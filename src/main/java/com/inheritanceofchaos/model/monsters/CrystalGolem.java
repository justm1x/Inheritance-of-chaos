package com.inheritanceofchaos.model.monsters;

import com.inheritanceofchaos.battle.BattleLog;
import com.inheritanceofchaos.model.Monster;
import com.inheritanceofchaos.model.Player;

// Very high defense. Hits weakly on odd turns and SLAMS on even turns,
// so the player should Defend at the right moment.
public class CrystalGolem extends Monster {

    private static final String ART =
            "       /\\  /\\       \n" +
            "      /  \\/  \\      \n" +
            "     | <>  <> |     \n" +
            "   /\\|   ==   |/\\   \n" +
            "   \\/|________|\\/   \n" +
            "      |__|  |__|    ";

    public CrystalGolem(int level) {
        super("Crystal Golem", level, scale(70, 18, level), scale(12, 3.0, level), scale(6, 0.8, level), ART, false);
    }

    @Override
    protected void act(Player target, BattleLog log) {
        if (getTurn() % 2 == 0) {
            attack(target, log, 2.0, "brings down a CRYSTAL SLAM on");
        } else {
            attack(target, log, 0.6, "grinds its crystal fists against");
        }
    }

    @Override
    public String getIntent() {
        int nextTurn = getTurn() + 1;
        if (nextTurn % 2 == 0) {
            return "Crystal energy hums... a heavy slam is coming! (Defend!)";
        }
        return "";
    }
}
