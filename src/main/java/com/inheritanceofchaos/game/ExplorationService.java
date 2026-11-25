package com.inheritanceofchaos.game;

import com.inheritanceofchaos.factory.ItemFactory;
import com.inheritanceofchaos.factory.MonsterFactory;
import com.inheritanceofchaos.model.Monster;
import com.inheritanceofchaos.model.Player;
import com.inheritanceofchaos.model.items.Item;
import com.inheritanceofchaos.util.Rng;
import com.inheritanceofchaos.world.Location;

// Random events that can happen when the hero explores a location.
public class ExplorationService {

    private static String[] quietEvents = {
            "Ash falls like snow. For a moment, the world is silent.",
            "You find an old campfire, still warm. Someone was here before you.",
            "A crow watches you from a broken signpost, then flies away without a sound.",
            "Faint bells ring somewhere far away. There are no churches left.",
            "You find a child's wooden sword in the dust. You leave it be."
    };

    public static ExplorationResult explore(GameSession session, Location location) {
        Player player = session.getPlayer();

        // in a boss lair you always meet the boss
        if (location.isBossLair()) {
            Monster boss = MonsterFactory.createBoss(location);
            return new ExplorationResult(boss.getIntro(), boss);
        }

        // roll a number from 0 to 100 and see what happens:
        //  0-62 battle, 62-76 treasure, 76-86 shrine, 86-95 trap, 95-100 nothing
        double roll = Rng.roll() * 100;

        if (roll < 62) {
            Monster monster = MonsterFactory.spawn(location, player);
            return new ExplorationResult(location.getName() + ": " + monster.getIntro(), monster);
        }
        if (roll < 76) {
            return findTreasure(player, location);
        }
        if (roll < 86) {
            int hp = player.heal((int) Math.round(player.getMaxHealth() * 0.3));
            int mp = player.restoreMana((int) Math.round(player.getMaxMana() * 0.3));
            return new ExplorationResult("You find a forgotten shrine and pray. (+" + hp + " HP, +" + mp + " MP)", null);
        }
        if (roll < 95) {
            int trapDamage = (int) Math.round(player.getMaxHealth() * 0.08);
            if (trapDamage < 3) {
                trapDamage = 3;
            }
            int lost = player.sacrificeHealth(trapDamage);
            return new ExplorationResult("A hidden snare cuts into your leg! (-" + lost + " HP)", null);
        }
        String text = quietEvents[Rng.between(0, quietEvents.length - 1)];
        return new ExplorationResult(text, null);
    }

    private static ExplorationResult findTreasure(Player player, Location location) {
        int gold = Rng.between(4, 8) + location.getMaxLevel() * 3;
        player.addGold(gold);
        String message = "You find a rotting chest with " + gold + " gold";

        // 40% chance that there is also a potion inside
        if (Rng.chance(0.4)) {
            Item item = ItemFactory.randomConsumable(location.getMaxLevel());
            player.getInventory().add(item);
            message += " and " + item.getName();
        }
        return new ExplorationResult(message + ".", null);
    }
}
