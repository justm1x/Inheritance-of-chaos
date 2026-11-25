package com.inheritanceofchaos.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Design pattern "DAO" (Data Access Object): all the SQL for saves is in this one class.
//
// These methods can be slow, so the window always calls them
// on the database thread (GameExecutor.runInBackground), never on the Swing thread.
public class SaveRepository {

    // Saves the game. If a save with this id already exists, it is updated (UPSERT).
    // Everything is done in ONE transaction: either the whole save is written or nothing.
    public void save(SaveData data) throws SQLException {
        String sql = "INSERT INTO saves (id, hero_name, level, experience, health, max_health, base_damage,"
                + " base_defense, mana, max_mana, gold, stat_points, weapon_key, armor_key, world_index, deaths,"
                + " battles_won, completed, created_at, updated_at)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,"
                + " datetime('now', 'localtime'), datetime('now', 'localtime'))"
                + " ON CONFLICT(id) DO UPDATE SET"
                + " hero_name = excluded.hero_name, level = excluded.level, experience = excluded.experience,"
                + " health = excluded.health, max_health = excluded.max_health, base_damage = excluded.base_damage,"
                + " base_defense = excluded.base_defense, mana = excluded.mana, max_mana = excluded.max_mana,"
                + " gold = excluded.gold, stat_points = excluded.stat_points, weapon_key = excluded.weapon_key,"
                + " armor_key = excluded.armor_key, world_index = excluded.world_index, deaths = excluded.deaths,"
                + " battles_won = excluded.battles_won, completed = excluded.completed,"
                + " updated_at = datetime('now', 'localtime')";

        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            connection.setAutoCommit(false); // start the transaction
            try {
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setString(1, data.id());
                statement.setString(2, data.heroName());
                statement.setInt(3, data.level());
                statement.setInt(4, data.experience());
                statement.setInt(5, data.health());
                statement.setInt(6, data.maxHealth());
                statement.setInt(7, data.baseDamage());
                statement.setInt(8, data.baseDefense());
                statement.setInt(9, data.mana());
                statement.setInt(10, data.maxMana());
                statement.setInt(11, data.gold());
                statement.setInt(12, data.statPoints());
                statement.setString(13, data.weaponKey()); // can be null, that is fine
                statement.setString(14, data.armorKey());
                statement.setInt(15, data.worldIndex());
                statement.setInt(16, data.deaths());
                statement.setInt(17, data.battlesWon());
                if (data.completed()) {
                    statement.setInt(18, 1);
                } else {
                    statement.setInt(18, 0);
                }
                statement.executeUpdate();
                statement.close();

                // the easiest way to update the bag: delete the old rows and write the new ones
                deleteRows(connection, "DELETE FROM inventory WHERE save_id = ?", data.id());
                insertRows(connection, "INSERT INTO inventory (save_id, item_key, quantity) VALUES (?, ?, ?)",
                        data.id(), data.inventory());

                deleteRows(connection, "DELETE FROM location_progress WHERE save_id = ?", data.id());
                insertRows(connection, "INSERT INTO location_progress (save_id, location_id, victories) VALUES (?, ?, ?)",
                        data.id(), data.victories());

                connection.commit(); // everything went fine, write it
            } catch (SQLException e) {
                connection.rollback(); // something failed, undo everything
                throw e;
            }
        }
    }

    // Loads a save. Returns null if there is no save with this id.
    public SaveData load(String id) throws SQLException {
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM saves WHERE id = ?");
            statement.setString(1, id);
            ResultSet rs = statement.executeQuery();

            if (!rs.next()) {
                statement.close();
                return null;
            }

            Map<String, Integer> inventory = readMap(connection,
                    "SELECT item_key, quantity FROM inventory WHERE save_id = ?", id);
            Map<String, Integer> victories = readMap(connection,
                    "SELECT location_id, victories FROM location_progress WHERE save_id = ?", id);

            SaveData data = new SaveData(
                    rs.getString("id"), rs.getString("hero_name"), rs.getInt("level"), rs.getInt("experience"),
                    rs.getInt("health"), rs.getInt("max_health"), rs.getInt("base_damage"),
                    rs.getInt("base_defense"), rs.getInt("mana"), rs.getInt("max_mana"), rs.getInt("gold"),
                    rs.getInt("stat_points"), rs.getString("weapon_key"), rs.getString("armor_key"),
                    rs.getInt("world_index"), rs.getInt("deaths"), rs.getInt("battles_won"),
                    rs.getInt("completed") == 1, inventory, victories);
            statement.close();
            return data;
        }
    }

    // all saves, the newest first
    public List<SaveSummary> listSaves() throws SQLException {
        String sql = "SELECT id, hero_name, level, updated_at FROM saves ORDER BY updated_at DESC";
        List<SaveSummary> result = new ArrayList<>();

        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                result.add(new SaveSummary(rs.getString("id"), rs.getString("hero_name"),
                        rs.getInt("level"), rs.getString("updated_at")));
            }
            statement.close();
        }
        return result;
    }

    // Deletes a save. Its inventory and progress rows are deleted automatically (ON DELETE CASCADE).
    public void delete(String id) throws SQLException {
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM saves WHERE id = ?");
            statement.setString(1, id);
            statement.executeUpdate();
            statement.close();
        }
    }

    // ===== helper methods =====

    private void deleteRows(Connection connection, String sql, String saveId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, saveId);
        statement.executeUpdate();
        statement.close();
    }

    // writes every "key -> number" pair of the map as one row
    private void insertRows(Connection connection, String sql, String saveId, Map<String, Integer> rows)
            throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        for (String key : rows.keySet()) {
            statement.setString(1, saveId);
            statement.setString(2, key);
            statement.setInt(3, rows.get(key));
            statement.executeUpdate();
        }
        statement.close();
    }

    // reads rows with two columns (text, number) into a map
    private Map<String, Integer> readMap(Connection connection, String sql, String saveId) throws SQLException {
        Map<String, Integer> result = new HashMap<>();
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, saveId);
        ResultSet rs = statement.executeQuery();
        while (rs.next()) {
            result.put(rs.getString(1), rs.getInt(2));
        }
        statement.close();
        return result;
    }
}
