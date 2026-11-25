package com.inheritanceofchaos.battle;

import com.inheritanceofchaos.model.items.Item;

import java.util.List;

// Everything the window needs to know after a battle is over.
public record BattleOutcome(BattleResult result, String monsterName, int monsterLevel, boolean boss,
                            int experience, int gold, List<Item> loot, int levelsGained, int rounds) {
}
