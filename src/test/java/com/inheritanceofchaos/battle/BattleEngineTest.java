package com.inheritanceofchaos.battle;

import com.inheritanceofchaos.model.Player;
import com.inheritanceofchaos.model.monsters.FallenKnight;
import com.inheritanceofchaos.model.monsters.Slime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Runs real battles in the battle thread. The test plays the role of the game window.
class BattleEngineTest {

    // a fake window that just remembers what the battle sent
    private static class Recorder implements BattleListener {
        volatile BattleOutcome outcome;
        volatile BattleSnapshot last;

        @Override
        public void onLog(String message, LogType type) {
        }

        @Override
        public void onUpdate(BattleSnapshot snapshot) {
            last = snapshot;
        }

        @Override
        public void onBattleEnd(BattleOutcome result) {
            outcome = result;
        }
    }

    // waits (max 5 seconds) until the battle asks the player for a command
    private static void waitForPlayerTurn(BattleEngine engine) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5000;
        while (!engine.isWaitingForPlayer() && System.currentTimeMillis() < deadline) {
            Thread.sleep(2);
        }
    }

    @Test
    void strongHeroBeatsSlimeAndGetsRewards() throws Exception {
        Player hero = new Player("Hero");
        int goldBefore = hero.getGold();
        BattleEngine engine = new BattleEngine(hero, new Slime(1), true, 0);
        Recorder recorder = new Recorder();
        engine.addListener(recorder);
        engine.start();

        // keep pressing Attack until the battle is over
        long deadline = System.currentTimeMillis() + 10000;
        while (recorder.outcome == null && System.currentTimeMillis() < deadline) {
            engine.submit(PlayerCommand.attack()); // returns false when it is not our turn
            Thread.sleep(2);
        }

        BattleOutcome outcome = recorder.outcome;
        assertNotNull(outcome);
        assertEquals(BattleResult.VICTORY, outcome.result());
        assertTrue(outcome.experience() > 0);
        assertEquals(goldBefore + outcome.gold(), hero.getGold());
        assertFalse(recorder.last.waitingForPlayer());
    }

    @Test
    void cannotEscapeFromBoss() throws Exception {
        Player hero = new Player("Hero");
        BattleEngine engine = new BattleEngine(hero, new FallenKnight(6), false, 0);
        Recorder recorder = new Recorder();
        engine.addListener(recorder);
        engine.start();

        // try to escape: the battle must say no and ask again
        waitForPlayerTurn(engine);
        assertTrue(engine.submit(PlayerCommand.escape()));
        Thread.sleep(50);
        waitForPlayerTurn(engine);

        assertTrue(engine.isWaitingForPlayer());
        assertNull(recorder.outcome);
        assertEquals(hero.getMaxHealth(), hero.getHealth(), "the boss must not get a free turn");
        engine.stop();
    }

    @Test
    void onlyOneCommandIsAcceptedPerTurn() throws Exception {
        Player hero = new Player("Hero");
        BattleEngine engine = new BattleEngine(hero, new FallenKnight(6), false, 200);
        engine.start();

        waitForPlayerTurn(engine);
        assertTrue(engine.submit(PlayerCommand.defend()));
        assertFalse(engine.submit(PlayerCommand.attack()), "a second click in the same turn must be ignored");
        engine.stop();
    }
}
