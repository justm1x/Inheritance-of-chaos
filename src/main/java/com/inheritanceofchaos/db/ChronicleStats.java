package com.inheritanceofchaos.db;

import java.util.List;

// Statistics for the "Chronicle" screen.
// mostSlain is a list of lines like "Goblin: 12", hallOfFame is a list of lines like "Alex - level 14, deaths: 2".
public record ChronicleStats(int totalBattles, int victories, int defeats, int escapes,
                             List<String> mostSlain, List<String> hallOfFame) {
}
