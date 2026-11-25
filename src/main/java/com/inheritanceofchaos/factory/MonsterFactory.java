package com.inheritanceofchaos.factory;

import com.inheritanceofchaos.model.Monster;
import com.inheritanceofchaos.model.MonsterType;
import com.inheritanceofchaos.model.Player;
import com.inheritanceofchaos.model.monsters.ChaosHound;
import com.inheritanceofchaos.model.monsters.CrystalGolem;
import com.inheritanceofchaos.model.monsters.Cultist;
import com.inheritanceofchaos.model.monsters.FallenKnight;
import com.inheritanceofchaos.model.monsters.Goblin;
import com.inheritanceofchaos.model.monsters.HeraldOfChaos;
import com.inheritanceofchaos.model.monsters.Skeleton;
import com.inheritanceofchaos.model.monsters.Slime;
import com.inheritanceofchaos.model.monsters.Wolf;
import com.inheritanceofchaos.model.monsters.Wraith;
import com.inheritanceofchaos.util.Rng;
import com.inheritanceofchaos.world.LevelSystem;
import com.inheritanceofchaos.world.Location;

// Design pattern "Factory".
// The rest of the game never writes "new Goblin(...)" itself.
// It asks the factory for a monster, and the factory decides which class to create.
public class MonsterFactory {

    public static Monster create(MonsterType type, int level) {
        switch (type) {
            case SLIME:
                return new Slime(level);
            case GOBLIN:
                return new Goblin(level);
            case WOLF:
                return new Wolf(level);
            case SKELETON:
                return new Skeleton(level);
            case CULTIST:
                return new Cultist(level);
            case CHAOS_HOUND:
                return new ChaosHound(level);
            case CRYSTAL_GOLEM:
                return new CrystalGolem(level);
            case WRAITH:
                return new Wraith(level);
            case FALLEN_KNIGHT:
                return new FallenKnight(level);
            case HERALD_OF_CHAOS:
                return new HeraldOfChaos(level);
            default:
                throw new IllegalArgumentException("Unknown monster type: " + type);
        }
    }

    // random monster of this location, its level depends on the hero's level
    public static Monster spawn(Location location, Player player) {
        MonsterType type = Rng.pick(location.getMonsterPool());
        int level = LevelSystem.generateMonsterLevel(player, location);
        return create(type, level);
    }

    public static Monster createBoss(Location location) {
        return create(location.getBoss(), location.getMaxLevel());
    }
}
