package com.inheritanceofchaos.model.monsters;

import com.inheritanceofchaos.battle.BattleLog;
import com.inheritanceofchaos.battle.LogType;
import com.inheritanceofchaos.model.Monster;
import com.inheritanceofchaos.model.Player;

// Tough monster. Once per fight it puts its bones back together and heals.
public class Skeleton extends Monster {

    private static final String ART =
            "      .-----.      \n" +
            "     | x   x |     \n" +
            "     |   ^   |     \n" +
            "      '|||||'      \n" +
            "    ---|---|---    \n" +
            "       |   |       \n" +
            "      _|   |_      ";

    private boolean reassembled = false;

    public Skeleton(int level) {
        super("Skeleton", level, scale(54, 15, level), scale(12, 2.9, level), scale(3, 0.6, level), ART, false);
    }

    @Override
    protected void act(Player target, BattleLog log) {
        if (!reassembled && getHealthRatio() < 0.35) {
            reassembled = true;
            int healed = heal((int) Math.round(getMaxHealth() * 0.35));
            log.log("Skeleton's bones rattle and snap back into place! It restores " + healed + " HP.", LogType.ENEMY);
            return;
        }
        attack(target, log, 1.0, "swings a rusty blade at");
    }
}
