package com.inheritanceofchaos.model.items;

import com.inheritanceofchaos.model.Monster;
import com.inheritanceofchaos.model.Player;

// Potions, bombs and other items that disappear after one use.
public class Consumable extends Item {

    // what the item does
    public enum Effect {
        HEAL, MANA, RESTORE_ALL, DAMAGE
    }

    private Effect effect;
    private int power;

    public Consumable(String key, String name, String description, int price, Effect effect, int power) {
        super(key, name, description, price);
        this.effect = effect;
        this.power = power;
    }

    public Effect getEffect() {
        return effect;
    }

    public int getPower() {
        return power;
    }

    // a bomb needs an enemy, so it can only be used in battle
    public boolean isBattleOnly() {
        return effect == Effect.DAMAGE;
    }

    @Override
    public String getStatsText() {
        switch (effect) {
            case HEAL:
                return "Restores " + power + " HP";
            case MANA:
                return "Restores " + power + " MP";
            case RESTORE_ALL:
                return "Fully restores HP and MP";
            case DAMAGE:
                return "Deals " + power + " damage, ignores armor (battle only)";
            default:
                return "";
        }
    }

    // Uses the item and returns a message about what happened.
    // target is the enemy, or null when we are not in a battle.
    // The item is removed from the bag by the code that calls this method.
    public String apply(Player player, Monster target) {
        switch (effect) {
            case HEAL: {
                int healed = player.heal(power);
                return player.getName() + " drinks " + getName() + " and restores " + healed + " HP.";
            }
            case MANA: {
                int restored = player.restoreMana(power);
                return player.getName() + " drinks " + getName() + " and restores " + restored + " MP.";
            }
            case RESTORE_ALL: {
                player.restoreAll();
                return player.getName() + " drinks " + getName() + ". Body and spirit are fully restored!";
            }
            case DAMAGE: {
                if (target == null) {
                    throw new IllegalStateException(getName() + " can only be used in battle");
                }
                int dealt = target.takeTrueDamage(power);
                return player.getName() + " hurls " + getName() + "! " + target.getName() + " takes " + dealt + " damage.";
            }
            default:
                return "Nothing happens.";
        }
    }
}
