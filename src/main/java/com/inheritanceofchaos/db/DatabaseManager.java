package com.inheritanceofchaos.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

// Connects to the SQLite database file and creates the tables on the first start.
//
// Design pattern "Singleton": there is only one DatabaseManager in the whole program,
// everyone gets it with DatabaseManager.getInstance().
//
// The database file is saved in <user folder>/.inheritance-of-chaos/game.db
// Tests use their own file with the setting -Dioc.db=path
public class DatabaseManager {

    public static final String DB_PATH_PROPERTY = "ioc.db";

    private static DatabaseManager instance;

    private String url;

    private DatabaseManager() {
        String path = System.getProperty(DB_PATH_PROPERTY);
        if (path == null) {
            path = System.getProperty("user.home") + File.separator + ".inheritance-of-chaos"
                    + File.separator + "game.db";
        }
        File file = new File(path).getAbsoluteFile();
        file.getParentFile().mkdirs(); // create the folder if it does not exist
        url = "jdbc:sqlite:" + file.getPath();

        try {
            createTables();
        } catch (SQLException e) {
            throw new RuntimeException("Cannot open the database " + file + ": " + e.getMessage(), e);
        }
    }

    // synchronized so two threads can never create two managers at the same time
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    // Opens a new connection. It must be closed after use (we always use try-with-resources).
    public Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(url);
        // SQLite has foreign keys turned off by default, so we turn them on
        Statement statement = connection.createStatement();
        statement.execute("PRAGMA foreign_keys = ON");
        statement.close();
        return connection;
    }

    private void createTables() throws SQLException {
        // the hero and his progress, one row per save
        String saves = "CREATE TABLE IF NOT EXISTS saves ("
                + " id TEXT PRIMARY KEY,"
                + " hero_name TEXT NOT NULL,"
                + " level INTEGER NOT NULL CHECK (level >= 1),"
                + " experience INTEGER NOT NULL,"
                + " health INTEGER NOT NULL,"
                + " max_health INTEGER NOT NULL,"
                + " base_damage INTEGER NOT NULL,"
                + " base_defense INTEGER NOT NULL,"
                + " mana INTEGER NOT NULL,"
                + " max_mana INTEGER NOT NULL,"
                + " gold INTEGER NOT NULL,"
                + " stat_points INTEGER NOT NULL,"
                + " weapon_key TEXT,"
                + " armor_key TEXT,"
                + " world_index INTEGER NOT NULL DEFAULT 0,"
                + " deaths INTEGER NOT NULL DEFAULT 0,"
                + " battles_won INTEGER NOT NULL DEFAULT 0,"
                + " completed INTEGER NOT NULL DEFAULT 0,"
                + " created_at TEXT NOT NULL,"
                + " updated_at TEXT NOT NULL)";

        // items in the bag. When a save is deleted, its items are deleted too (ON DELETE CASCADE)
        String inventory = "CREATE TABLE IF NOT EXISTS inventory ("
                + " save_id TEXT NOT NULL REFERENCES saves(id) ON DELETE CASCADE,"
                + " item_key TEXT NOT NULL,"
                + " quantity INTEGER NOT NULL CHECK (quantity > 0),"
                + " PRIMARY KEY (save_id, item_key))";

        // how many battles were won in each location
        String progress = "CREATE TABLE IF NOT EXISTS location_progress ("
                + " save_id TEXT NOT NULL REFERENCES saves(id) ON DELETE CASCADE,"
                + " location_id TEXT NOT NULL,"
                + " victories INTEGER NOT NULL,"
                + " PRIMARY KEY (save_id, location_id))";

        // history of all battles, used for the statistics screen
        String battleLog = "CREATE TABLE IF NOT EXISTS battle_log ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " save_id TEXT,"
                + " hero_name TEXT NOT NULL,"
                + " hero_level INTEGER NOT NULL,"
                + " monster_name TEXT NOT NULL,"
                + " monster_level INTEGER NOT NULL,"
                + " is_boss INTEGER NOT NULL,"
                + " result TEXT NOT NULL,"
                + " xp_gained INTEGER NOT NULL,"
                + " gold_gained INTEGER NOT NULL,"
                + " rounds INTEGER NOT NULL,"
                + " fought_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime')))";

        // heroes who finished the game
        String hallOfFame = "CREATE TABLE IF NOT EXISTS hall_of_fame ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " hero_name TEXT NOT NULL,"
                + " level INTEGER NOT NULL,"
                + " battles_won INTEGER NOT NULL,"
                + " deaths INTEGER NOT NULL,"
                + " finished_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime')))";

        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.execute(saves);
            statement.execute(inventory);
            statement.execute(progress);
            statement.execute(battleLog);
            statement.execute(hallOfFame);
        }
    }
}
