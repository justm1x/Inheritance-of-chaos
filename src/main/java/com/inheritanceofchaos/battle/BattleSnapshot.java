package com.inheritanceofchaos.battle;

// A copy of the battle numbers at one moment.
// The battle thread creates it, the window only reads it.
// A record cannot be changed after it is created, so it is safe to pass it between threads.
public record BattleSnapshot(
        String playerName, int playerLevel,
        int playerHealth, int playerMaxHealth,
        int playerMana, int playerMaxMana,
        boolean playerDefending,
        String monsterName, int monsterLevel,
        int monsterHealth, int monsterMaxHealth,
        boolean boss, String monsterIntent,
        boolean waitingForPlayer, int round) {
}
