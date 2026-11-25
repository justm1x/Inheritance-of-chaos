package com.inheritanceofchaos.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// DAO for the battle history and the Hall of Fame (heroes who finished the game).
public class ChronicleRepository {

    public void recordBattle(BattleRecord record) throws SQLException {
        String sql = "INSERT INTO battle_log (save_id, hero_name, hero_level, monster_name, monster_level,"
                + " is_boss, result, xp_gained, gold_gained, rounds) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, record.saveId());
            statement.setString(2, record.heroName());
            statement.setInt(3, record.heroLevel());
            statement.setString(4, record.monsterName());
            statement.setInt(5, record.monsterLevel());
            statement.setBoolean(6, record.boss());
            statement.setString(7, record.result());
            statement.setInt(8, record.experience());
            statement.setInt(9, record.gold());
            statement.setInt(10, record.rounds());
            statement.executeUpdate();
            statement.close();
        }
    }

    public void addToHallOfFame(String heroName, int level, int battlesWon, int deaths) throws SQLException {
        String sql = "INSERT INTO hall_of_fame (hero_name, level, battles_won, deaths) VALUES (?, ?, ?, ?)";

        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, heroName);
            statement.setInt(2, level);
            statement.setInt(3, battlesWon);
            statement.setInt(4, deaths);
            statement.executeUpdate();
            statement.close();
        }
    }

    public ChronicleStats loadStats() throws SQLException {
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            int total = countBattles(connection, "SELECT COUNT(*) FROM battle_log");
            int victories = countBattles(connection, "SELECT COUNT(*) FROM battle_log WHERE result = 'VICTORY'");
            int defeats = countBattles(connection, "SELECT COUNT(*) FROM battle_log WHERE result = 'DEFEAT'");
            int escapes = countBattles(connection, "SELECT COUNT(*) FROM battle_log WHERE result = 'ESCAPED'");

            // top 5 monsters the player killed most often (GROUP BY counts them per name)
            List<String> mostSlain = new ArrayList<>();
            String slainSql = "SELECT monster_name, COUNT(*) AS kills FROM battle_log"
                    + " WHERE result = 'VICTORY'"
                    + " GROUP BY monster_name ORDER BY kills DESC LIMIT 5";
            PreparedStatement slainStatement = connection.prepareStatement(slainSql);
            ResultSet slain = slainStatement.executeQuery();
            while (slain.next()) {
                mostSlain.add(slain.getString("monster_name") + ": " + slain.getInt("kills"));
            }
            slainStatement.close();

            // best heroes: fewer deaths is better
            List<String> hallOfFame = new ArrayList<>();
            String hallSql = "SELECT hero_name, level, deaths FROM hall_of_fame"
                    + " ORDER BY deaths ASC, battles_won ASC LIMIT 10";
            PreparedStatement hallStatement = connection.prepareStatement(hallSql);
            ResultSet hall = hallStatement.executeQuery();
            while (hall.next()) {
                hallOfFame.add(hall.getString("hero_name") + " - level " + hall.getInt("level")
                        + ", deaths: " + hall.getInt("deaths"));
            }
            hallStatement.close();

            return new ChronicleStats(total, victories, defeats, escapes, mostSlain, hallOfFame);
        }
    }

    private int countBattles(Connection connection, String sql) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet rs = statement.executeQuery();
        int count = 0;
        if (rs.next()) {
            count = rs.getInt(1);
        }
        statement.close();
        return count;
    }
}
