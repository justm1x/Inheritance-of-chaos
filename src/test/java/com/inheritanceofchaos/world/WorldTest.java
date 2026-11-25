package com.inheritanceofchaos.world;

import com.inheritanceofchaos.factory.ItemFactory;
import com.inheritanceofchaos.factory.MonsterFactory;
import com.inheritanceofchaos.game.GameSession;
import com.inheritanceofchaos.game.LocationStatus;
import com.inheritanceofchaos.model.Monster;
import com.inheritanceofchaos.model.MonsterType;
import com.inheritanceofchaos.model.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorldTest {

    @Test
    void monsterLevelStaysInsideLocationRange() {
        Location woods = WorldRegistry.get(0).locations().get(0);
        Player player = new Player("Hero");
        player.gainExperience(5000);    // way over the woods' max level
        for (int i = 0; i < 200; i++) {
            int level = LevelSystem.generateMonsterLevel(player, woods);
            assertTrue(level >= woods.getMinLevel() && level <= woods.getMaxLevel());
        }
    }

    @Test
    void everyMonsterTypeCanBeCreatedAtEveryLevel() {
        for (MonsterType type : MonsterType.values()) {
            for (int level = 1; level <= 15; level++) {
                Monster monster = MonsterFactory.create(type, level);
                assertTrue(monster.getMaxHealth() > 0);
                assertEquals(level, monster.getLevel());
            }
        }
    }

    @Test
    void everyShopItemExists() {
        for (World world : WorldRegistry.all()) {
            for (String key : world.shopStock()) {
                assertNotNull(ItemFactory.create(key));
            }
        }
    }

    @Test
    void invalidLocationsAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> Location.area("x", "X", "", -1, 1, 3, 1, 1, MonsterType.SLIME));
        assertThrows(IllegalArgumentException.class,
                () -> Location.area("x", "X", "", 0, 5, 3, 1, 1, MonsterType.SLIME));
    }

    @Test
    void locationsUnlockInOrder() {
        GameSession session = GameSession.newGame("Hero");
        World vale = WorldRegistry.get(0);
        Location woods = vale.locations().get(0);
        Location marsh = vale.locations().get(1);

        assertEquals(LocationStatus.AVAILABLE, session.getStatus(woods));
        assertEquals(LocationStatus.LOCKED_PATH, session.getStatus(marsh));

        for (int i = 0; i < woods.getVictoriesToClear(); i++) {
            session.recordVictory(woods);
        }
        assertEquals(LocationStatus.CLEARED, session.getStatus(woods));
        assertEquals(LocationStatus.LOCKED_LEVEL, session.getStatus(marsh));

        session.getPlayer().gainExperience(50);
        assertEquals(LocationStatus.AVAILABLE, session.getStatus(marsh));
        assertFalse(session.isWorldUnlocked(1));
    }
}
