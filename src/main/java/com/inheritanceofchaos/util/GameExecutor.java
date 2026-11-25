package com.inheritanceofchaos.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// Runs database work on a separate thread, so the game window never freezes while saving.
// There is only ONE background thread, so saves are always written one after another in order.
public class GameExecutor {

    private static final ExecutorService databaseThread = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "db-worker");
        thread.setDaemon(true); // does not keep the program alive after the window is closed
        return thread;
    });

    public static void runInBackground(Runnable task) {
        databaseThread.execute(task);
    }

    // Called when the game closes: waits up to 3 seconds so the last save can finish.
    public static void shutdown() {
        databaseThread.shutdown();
        try {
            databaseThread.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
