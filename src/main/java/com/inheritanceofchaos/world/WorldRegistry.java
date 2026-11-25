package com.inheritanceofchaos.world;

import com.inheritanceofchaos.model.MonsterType;

import java.util.ArrayList;
import java.util.List;

// All the game content in one place: worlds, locations and what the merchants sell.
public class WorldRegistry {

    private static List<World> worlds = new ArrayList<>();

    static {
        // ===== World 1 =====
        List<Location> valeLocations = new ArrayList<>();
        valeLocations.add(Location.area("whispering_woods", "Whispering Woods",
                "Dead trees whisper names of the fallen. Slimes and wolves roam between them.",
                0, 1, 3, 1, 4,
                MonsterType.SLIME, MonsterType.WOLF, MonsterType.GOBLIN));
        valeLocations.add(Location.area("rotting_marsh", "Rotting Marsh",
                "A swamp that swallowed an entire village. The drowned do not stay dead.",
                1, 2, 5, 2, 5,
                MonsterType.SLIME, MonsterType.GOBLIN, MonsterType.SKELETON, MonsterType.WOLF));
        valeLocations.add(Location.bossLair("ruined_keep", "Ruined Keep",
                "The last fortress of the old kingdom. Its sworn knight still guards the gate.",
                6, 5, MonsterType.FALLEN_KNIGHT));

        List<String> valeShop = new ArrayList<>();
        valeShop.add("minor_potion");
        valeShop.add("healing_potion");
        valeShop.add("mana_tonic");
        valeShop.add("fire_flask");
        valeShop.add("hunter_axe");
        valeShop.add("knight_blade");
        valeShop.add("leather_armor");
        valeShop.add("chainmail");

        worlds.add(new World(0, "Ashen Vale",
                "Once a land of farms and chapels, now buried under grey ash. The old kingdom fell here first.",
                "Ashen Camp", valeLocations, valeShop));

        // ===== World 2 =====
        List<Location> wastesLocations = new ArrayList<>();
        wastesLocations.add(Location.area("bone_desert", "Bone Desert",
                "Dunes of bleached bones. Cultists sing to the storm, and hounds answer.",
                0, 6, 9, 6, 5,
                MonsterType.CULTIST, MonsterType.CHAOS_HOUND, MonsterType.SKELETON));
        wastesLocations.add(Location.area("crystal_caverns", "Crystal Caverns",
                "Crystals grown from solid chaos hum with stolen voices.",
                1, 8, 11, 8, 5,
                MonsterType.CRYSTAL_GOLEM, MonsterType.WRAITH, MonsterType.CULTIST));
        wastesLocations.add(Location.bossLair("throne_of_chaos", "Throne of Chaos",
                "The wound in the world. Something waits on the throne at its heart.",
                13, 10, MonsterType.HERALD_OF_CHAOS));

        List<String> wastesShop = new ArrayList<>();
        wastesShop.add("healing_potion");
        wastesShop.add("greater_potion");
        wastesShop.add("mana_draught");
        wastesShop.add("elixir");
        wastesShop.add("chaos_shard");
        wastesShop.add("bone_cleaver");
        wastesShop.add("edge_of_chaos");
        wastesShop.add("bone_mail");
        wastesShop.add("voidforged_plate");

        worlds.add(new World(1, "Chaos Wastes",
                "Beyond the Keep the sky is torn. Chaos leaks into the world and reshapes everything it touches.",
                "Last Bastion", wastesLocations, wastesShop));
    }

    public static List<World> all() {
        return worlds;
    }

    public static int count() {
        return worlds.size();
    }

    public static World get(int index) {
        return worlds.get(index);
    }

    // finds the world that has this location
    public static World worldOf(Location location) {
        for (World world : worlds) {
            if (world.locations().contains(location)) {
                return world;
            }
        }
        throw new IllegalArgumentException("Unknown location: " + location.getId());
    }

    // true if this is the boss of the LAST world (beating him finishes the game)
    public static boolean isFinalBoss(Location location) {
        World lastWorld = worlds.get(worlds.size() - 1);
        return lastWorld.bossLair() == location;
    }
}
