package com.inheritanceofchaos.db;

import com.inheritanceofchaos.factory.ItemFactory;
import com.inheritanceofchaos.game.GameSession;
import com.inheritanceofchaos.model.Attribute;
import com.inheritanceofchaos.model.Player;
import com.inheritanceofchaos.world.WorldRegistry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Writes to a real SQLite file in a temporary folder and reads it back.
class SaveRepositoryTest {

    @TempDir
    static Path tempDir;

    @BeforeAll
    static void useTemporaryDatabase() {
        // the tests must not touch the real save file
        System.setProperty(DatabaseManager.DB_PATH_PROPERTY, tempDir.resolve("test.db").toString());
    }

    @Test
    void saveLoadAndDelete() throws Exception {
        GameSession session = GameSession.newGame("Tester");
        Player player = session.getPlayer();
        player.gainExperience(160);
        player.spendStatPoint(Attribute.STRENGTH);
        player.getInventory().add(ItemFactory.create("fire_flask"), 2);
        player.takeTrueDamage(30);
        session.recordVictory(WorldRegistry.get(0).locations().get(0));

        SaveRepository repository = new SaveRepository();
        repository.save(SaveData.capture(session));
        // saving twice must update the save, not make a second one
        repository.save(SaveData.capture(session));

        SaveData loaded = repository.load(session.getSaveId());
        assertNotNull(loaded);
        GameSession restored = loaded.restore();
        Player copy = restored.getPlayer();

        assertEquals(player.getName(), copy.getName());
        assertEquals(player.getLevel(), copy.getLevel());
        assertEquals(player.getExperience(), copy.getExperience());
        assertEquals(player.getHealth(), copy.getHealth());
        assertEquals(player.getDamage(), copy.getDamage());
        assertEquals(player.getDefense(), copy.getDefense());
        assertEquals(player.getGold(), copy.getGold());
        assertEquals(player.getStatPoints(), copy.getStatPoints());
        assertEquals(player.getWeapon(), copy.getWeapon());
        assertEquals(player.getInventory().grouped(), copy.getInventory().grouped());
        assertEquals(1, restored.getVictories(WorldRegistry.get(0).locations().get(0)));
        assertEquals(1, restored.getBattlesWon());

        List<SaveSummary> saves = repository.listSaves();
        int found = 0;
        for (SaveSummary save : saves) {
            if (save.id().equals(session.getSaveId())) {
                found++;
            }
        }
        assertEquals(1, found);

        repository.delete(session.getSaveId());
        assertNull(repository.load(session.getSaveId()));
    }

    @Test
    void chronicleCountsBattles() throws Exception {
        ChronicleRepository chronicle = new ChronicleRepository();
        chronicle.recordBattle(new BattleRecord("x", "Tester", 3, "Wolf", 2, false, "VICTORY", 40, 10, 3));
        chronicle.recordBattle(new BattleRecord("x", "Tester", 3, "Wolf", 3, false, "VICTORY", 50, 12, 4));
        chronicle.recordBattle(new BattleRecord("x", "Tester", 3, "Goblin", 3, false, "DEFEAT", 0, 0, 6));
        chronicle.addToHallOfFame("Tester", 12, 40, 2);

        ChronicleStats stats = chronicle.loadStats();
        assertTrue(stats.totalBattles() >= 3);
        assertTrue(stats.defeats() >= 1);
        assertEquals("Wolf: 2", stats.mostSlain().get(0));
        assertFalse(stats.hallOfFame().isEmpty());
    }
}
