package com.inheritanceofchaos.model;

import com.inheritanceofchaos.factory.ItemFactory;
import com.inheritanceofchaos.model.items.Item;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    @Test
    void newPlayerHasStartingStats() {
        Player player = new Player("Hero");
        assertEquals(1, player.getLevel());
        assertEquals(Player.START_HEALTH, player.getHealth());
        assertEquals(Player.START_HEALTH, player.getMaxHealth());
        assertEquals(Player.START_MANA, player.getMana());
        assertEquals(0, player.getStatPoints());
    }

    @Test
    void blankNameIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Player("   "));
    }

    @Test
    void experienceLevelsUpAndRestoresHero() {
        Player player = new Player("Hero");
        player.takeTrueDamage(50);
        int levels = player.gainExperience(Player.experienceRequiredFor(1));
        assertEquals(1, levels);
        assertEquals(2, player.getLevel());
        assertEquals(Player.STAT_POINTS_PER_LEVEL, player.getStatPoints());
        assertEquals(player.getMaxHealth(), player.getHealth());
        assertEquals(Player.START_HEALTH + 12, player.getMaxHealth());
        assertEquals(0, player.getExperience());
    }

    @Test
    void bigExperienceGivesSeveralLevelsAndKeepsRemainder() {
        Player player = new Player("Hero");
        int levels = player.gainExperience(50 + 100 + 7);
        assertEquals(2, levels);
        assertEquals(3, player.getLevel());
        assertEquals(7, player.getExperience());
    }

    @Test
    void defenseReducesDamageButAtLeastOneGetsThrough() {
        Player player = new Player("Hero");
        int before = player.getHealth();
        assertEquals(1, player.takeDamage(1));
        assertEquals(before - 1, player.getHealth());
        assertEquals(10 - player.getDefense(), player.takeDamage(10));
    }

    @Test
    void defendingHalvesDamage() {
        Player player = new Player("Hero");
        player.setDefending(true);
        int taken = player.takeTrueDamage(20);
        assertEquals(10, taken);
    }

    @Test
    void healthNeverGoesBelowZeroOrAboveMax() {
        Player player = new Player("Hero");
        player.takeTrueDamage(10_000);
        assertEquals(0, player.getHealth());
        assertFalse(player.isAlive());
        player.heal(10_000);
        assertEquals(player.getMaxHealth(), player.getHealth());
    }

    @Test
    void sacrificeHealthIsNeverLethal() {
        Player player = new Player("Hero");
        player.sacrificeHealth(10_000);
        assertEquals(1, player.getHealth());
    }

    @Test
    void spendingStatPointsImprovesAttributes() {
        Player player = new Player("Hero");
        assertFalse(player.spendStatPoint(Attribute.STRENGTH));
        player.gainExperience(50);
        int damage = player.getBaseDamage();
        assertTrue(player.spendStatPoint(Attribute.STRENGTH));
        assertEquals(damage + 2, player.getBaseDamage());
        assertTrue(player.spendStatPoint(Attribute.VITALITY));
        assertEquals(0, player.getStatPoints());
    }

    @Test
    void equippingSwapsWeaponsThroughTheBag() {
        Player player = new Player("Hero");
        Item sword = ItemFactory.create("rusty_sword");
        Item axe = ItemFactory.create("hunter_axe");
        player.getInventory().add(sword);
        player.getInventory().add(axe);
        player.equip(sword);
        assertEquals(sword, player.getWeapon());
        assertEquals(Player.START_DAMAGE + 3, player.getDamage());

        // the axe needs level 2
        assertTrue(player.equip(axe).contains("requires level"));
        player.gainExperience(50);
        player.equip(axe);
        assertEquals(axe, player.getWeapon());
        assertTrue(player.getInventory().contains(sword));
        assertFalse(player.getInventory().contains(axe));
    }

    @Test
    void goldCannotGoNegative() {
        Player player = new Player("Hero");
        player.addGold(10);
        assertFalse(player.spendGold(11));
        assertTrue(player.spendGold(10));
        assertEquals(0, player.getGold());
    }
}
