package com.inheritanceofchaos.world;

import com.inheritanceofchaos.model.Player;
import com.inheritanceofchaos.util.Rng;

// Decides the level of a new monster (same idea as LevelSystem in our first version).
// Monster level = hero level + location difficulty + random -1..+1,
// but it always stays inside the location's level range.
public class LevelSystem {

    public static int generateMonsterLevel(Player player, Location location) {
        int randomValue = Rng.between(-1, 1);
        int monsterLevel = player.getLevel() + location.getDifficulty() + randomValue;

        if (monsterLevel < location.getMinLevel()) {
            monsterLevel = location.getMinLevel();
        }
        if (monsterLevel > location.getMaxLevel()) {
            monsterLevel = location.getMaxLevel();
        }
        return monsterLevel;
    }
}
