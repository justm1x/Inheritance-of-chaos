package com.inheritanceofchaos.world;

import com.inheritanceofchaos.model.MonsterType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// A place the hero can explore.
// Monsters here are always between minLevel and maxLevel,
// and the hero needs at least requiredLevel to come in.
public class Location {

    private String id;
    private String name;
    private String description;
    private int difficulty;
    private int minLevel;
    private int maxLevel;
    private int requiredLevel;
    private int victoriesToClear;
    private List<MonsterType> monsterPool;
    private MonsterType boss; // null for normal areas

    private Location(String id, String name, String description, int difficulty, int minLevel, int maxLevel,
                     int requiredLevel, int victoriesToClear, List<MonsterType> monsterPool, MonsterType boss) {
        if (difficulty < 0) {
            throw new IllegalArgumentException("Difficulty cannot be negative");
        }
        if (minLevel < 1) {
            throw new IllegalArgumentException("Min level must be at least 1");
        }
        if (maxLevel < minLevel) {
            throw new IllegalArgumentException("Max level cannot be smaller than min level");
        }
        this.id = id;
        this.name = name;
        this.description = description;
        this.difficulty = difficulty;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.requiredLevel = requiredLevel;
        this.victoriesToClear = victoriesToClear;
        this.monsterPool = monsterPool;
        this.boss = boss;
    }

    // a normal area with random monsters
    public static Location area(String id, String name, String description, int difficulty, int minLevel,
                                int maxLevel, int requiredLevel, int victoriesToClear, MonsterType... monsters) {
        return new Location(id, name, description, difficulty, minLevel, maxLevel, requiredLevel,
                victoriesToClear, Arrays.asList(monsters), null);
    }

    // a boss lair: only one fight, against the boss
    public static Location bossLair(String id, String name, String description, int bossLevel, int requiredLevel,
                                    MonsterType boss) {
        return new Location(id, name, description, 0, bossLevel, bossLevel, requiredLevel, 1,
                new ArrayList<MonsterType>(), boss);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    // how many battles you need to win here to open the next location
    public int getVictoriesToClear() {
        return victoriesToClear;
    }

    public List<MonsterType> getMonsterPool() {
        return monsterPool;
    }

    public MonsterType getBoss() {
        return boss;
    }

    public boolean isBossLair() {
        return boss != null;
    }

    @Override
    public String toString() {
        return name;
    }
}
