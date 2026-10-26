package vn.quanlybanhang.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Đọc metadata một lần rồi dùng lại, tránh hỏi information_schema trên từng request.
 */
public final class SchemaCache {

    private static final Object LOCK = new Object();
    private static volatile Snapshot snapshot;

    private SchemaCache() {
    }

    public static void refresh(Connection connection) throws SQLException {
        Snapshot loaded = read(connection);
        synchronized (LOCK) {
            snapshot = loaded;
        }
    }

    public static boolean tableExists(String tableName) throws SQLException {
        return current().tables.contains(normalize(tableName));
    }

    public static boolean columnExists(String tableName, String columnName) throws SQLException {
        return current().columns.contains(key(tableName, columnName));
    }

    public static boolean columnTypeContains(String tableName, String columnName, String fragment) throws SQLException {
        if (fragment == null || fragment.isEmpty()) {
            return false;
        }
        String columnType = current().columnTypes.get(key(tableName, columnName));
        return columnType != null && columnType.contains(fragment);
    }

    private static Snapshot current() throws SQLException {
        Snapshot loaded = snapshot;
        if (loaded != null) {
            return loaded;
        }
        synchronized (LOCK) {
            if (snapshot != null) {
                return snapshot;
            }
            try (Connection connection = DatabaseConfig.getConnection()) {
                snapshot = read(connection);
            }
            return snapshot;
        }
    }

    private static Snapshot read(Connection connection) throws SQLException {
        Set<String> tables = new HashSet<>();
        Set<String> columns = new HashSet<>();
        Map<String, String> columnTypes = new HashMap<>();
        String sql = "SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE FROM information_schema.COLUMNS "
                + "WHERE TABLE_SCHEMA = DATABASE()";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String table = normalize(resultSet.getString("TABLE_NAME"));
                String column = normalize(resultSet.getString("COLUMN_NAME"));
                tables.add(table);
                String key = table + "." + column;
                columns.add(key);
                columnTypes.put(key, resultSet.getString("COLUMN_TYPE"));
            }
        }
        return new Snapshot(Set.copyOf(tables), Set.copyOf(columns), Map.copyOf(columnTypes));
    }

    private static String key(String tableName, String columnName) {
        return normalize(tableName) + "." + normalize(columnName);
    }

    private static String normalize(String name) {
        return name == null ? "" : name.toLowerCase(Locale.ROOT);
    }

    private record Snapshot(Set<String> tables, Set<String> columns, Map<String, String> columnTypes) {
    }
}
