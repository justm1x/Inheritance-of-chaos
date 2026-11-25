package com.inheritanceofchaos.model;

import com.inheritanceofchaos.battle.BattleLog;
import com.inheritanceofchaos.battle.LogType;
import com.inheritanceofchaos.util.Rng;

// Base class for all enemies.
//
// Design pattern "Template Method": the battle always calls takeTurn().
// takeTurn() counts the turn and then calls act().
// Every monster overrides act() with its own special attacks.
public class Monster extends GameCharacter {

    private String art;
    private boolean boss;
    private int xpReward;
    private int goldReward;
    private int turn;

    protected Monster(String name, int level, int maxHealth, int damage, int defense, String art, boolean boss) {
        super(name, level, maxHealth, damage, defense);
        this.art = art;
        this.boss = boss;

        // stronger monsters give more XP and gold, bosses give a lot more
        int xp = 10 + level * 6 + level * level / 2;
        int gold = 4 + level * 4 + Rng.between(0, level * 2);
        if (boss) {
            xp = xp * 3;
            gold = gold * 4;
        }
        this.xpReward = xp;
        this.goldReward = gold;
    }

    // Stat for a given level: value at level 1 plus a bonus for every next level.
    // Example: scale(42, 13, 3) = 42 + 13 * 2 = 68
    protected static int scale(double base, double perLevel, int level) {
        long value = Math.round(base + perLevel * (level - 1));
        if (value < 0) {
            value = 0;
        }
        return (int) value;
    }

    // The battle calls this on every enemy turn. It is final so monsters cannot change it.
    public final void takeTurn(Player target, BattleLog log) {
        turn++;
        act(target, log);
    }

    // Default behaviour is a simple attack. Monsters override this.
    protected void act(Player target, BattleLog log) {
        attack(target, log, 1.0, "attacks");
    }

    // Normal attack, the hero's defense makes it weaker. Returns the damage dealt.
    protected int attack(Player target, BattleLog log, double multiplier, String verb) {
        int dealt = target.takeDamage(rollDamage(multiplier));
        log.log(getName() + " " + verb + " " + target.getName() + " for " + dealt + " damage.", LogType.ENEMY);
        return dealt;
    }

    // Attack that ignores the hero's defense. Returns the damage dealt.
    protected int trueAttack(Player target, double multiplier) {
        return target.takeTrueDamage(rollDamage(multiplier));
    }

    // Number of the current enemy turn: 1, 2, 3...
    protected int getTurn() {
        return turn;
    }

    // A hint about the NEXT move, for example "a heavy slam is coming! (Defend!)".
    // Empty text means nothing special is coming.
    public String getIntent() {
        return "";
    }

    // Key of an item this monster always drops (only bosses have one), or null.
    public String getSignatureLoot() {
        return null;
    }

    public String getIntro() {
        return "A " + getName() + " (Lv " + getLevel() + ") blocks your path!";
    }

    public String getArt() {
        return art;
    }

    public boolean isBoss() {
        return boss;
    }

    public int getXpReward() {
        return xpReward;
    }

    public int getGoldReward() {
        return goldReward;
    }
}
