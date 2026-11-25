package com.inheritanceofchaos.battle;

// Something that can receive battle messages. BattleEngine implements it,
// so monsters and skills can write "Goblin stabs you for 5 damage" into the log.
public interface BattleLog {
    void log(String message, LogType type);
}
