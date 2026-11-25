package com.inheritanceofchaos.game;

// Can the hero go to a location right now?
public enum LocationStatus {
    LOCKED_PATH,   // the previous location is not cleared yet
    LOCKED_LEVEL,  // the hero's level is too low
    AVAILABLE,     // you can go there
    CLEARED,       // enough battles won here, but you can still come back to farm XP
    CONQUERED      // the boss is dead
}
