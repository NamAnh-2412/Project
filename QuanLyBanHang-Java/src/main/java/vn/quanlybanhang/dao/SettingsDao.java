package vn.quanlybanhang.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SettingsDao extends BaseDao {

    public Optional<String> getSetting(String key) throws SQLException {
        String sql = "SELECT value FROM settings WHERE `key` = ? LIMIT 1";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, key);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.ofNullable(resultSet.getString("value"));
                }
                return Optional.empty();
            }
        }
    }

    public Map<String, String> getAllSettings() throws SQLException {
        String sql = "SELECT `key`, value FROM settings";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            Map<String, String> settings = new HashMap<>();
            while (resultSet.next()) {
                settings.put(resultSet.getString("key"), resultSet.getString("value"));
            }
            return settings;
        }
    }

    public void setSetting(String key, String value) throws SQLException {
        String sql = "INSERT INTO settings (`key`, value) VALUES (?, ?) ON DUPLICATE KEY UPDATE value = VALUES(value)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, key);
            statement.setString(2, value);
            statement.executeUpdate();
        }
    }

    public void deleteSetting(String key) throws SQLException {
        String sql = "DELETE FROM settings WHERE `key` = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, key);
            statement.executeUpdate();
        }
    }
}
