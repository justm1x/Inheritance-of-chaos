package com.inheritanceofchaos.world;

import java.util.List;

// One world of the game: a camp with a merchant and a few locations.
// The last location is always the boss lair.
public record World(int index, String name, String description, String campName,
                    List<Location> locations, List<String> shopStock) {

    public Location bossLair() {
        return locations.get(locations.size() - 1);
    }
}
