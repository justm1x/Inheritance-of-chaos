package com.inheritanceofchaos.game;

import com.inheritanceofchaos.model.Monster;

// What happened after one click on "Explore".
// monster is only set when a battle starts, otherwise it is null.
public record ExplorationResult(String message, Monster monster) {

    public boolean isBattle() {
        return monster != null;
    }
}
