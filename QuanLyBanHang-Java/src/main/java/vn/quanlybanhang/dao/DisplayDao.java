package vn.quanlybanhang.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DisplayDao extends BaseDao {

    public void saveDisplay(String json) throws SQLException {
        String sql = "UPDATE cfd_hien_thi SET noi_dung = ?, cap_nhat = NOW() WHERE id = 1";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, json);
            statement.executeUpdate();
        }
    }

    public String loadDisplay() throws SQLException {
        String sql = "SELECT noi_dung FROM cfd_hien_thi WHERE id = 1";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                String body = resultSet.getString("noi_dung");
                return body == null ? "{}" : body;
            }
            return "{}";
        }
    }
}
