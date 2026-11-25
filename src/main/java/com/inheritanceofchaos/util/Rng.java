package com.inheritanceofchaos.util;

import java.util.List;
import java.util.Random;

// Small helper for random numbers that are used all over the game.
public class Rng {

    private static final Random random = new Random();

    // random whole number from min to max (both included)
    public static int between(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    // random number from 0.0 to 1.0
    public static double roll() {
        return random.nextDouble();
    }

    // returns true with the given chance, for example chance(0.3) is true 30% of the time
    public static boolean chance(double probability) {
        return random.nextDouble() < probability;
    }

    // random element of a list
    public static <T> T pick(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }
}
