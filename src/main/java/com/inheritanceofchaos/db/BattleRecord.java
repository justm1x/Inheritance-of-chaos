package com.inheritanceofchaos.db;

// One row of the battle_log table.
public record BattleRecord(String saveId, String heroName, int heroLevel, String monsterName, int monsterLevel,
                           boolean boss, String result, int experience, int gold, int rounds) {
}
