package vn.quanlybanhang.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import vn.quanlybanhang.model.KiemKho;
import vn.quanlybanhang.model.KiemKhoChiTiet;

public class KiemKhoDao extends BaseDao {

    /**
     * Tao bang kiemkho + chitiet_kiemkho neu chua co (giong migration PHP create_kiemkho_tables.php).
     */
    public void ensureTables() throws SQLException {
        String createKiemKho = "CREATE TABLE IF NOT EXISTS kiemkho ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "ma_phieu_kiem VARCHAR(50) NOT NULL UNIQUE, "
                + "ngay_kiem DATE NOT NULL, "
                + "nhanvien_kiem INT NOT NULL, "
                + "trang_thai VARCHAR(20) DEFAULT 'cho_duyet', "
                + "ghi_chu TEXT, "
                + "ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        String createChiTiet = "CREATE TABLE IF NOT EXISTS chitiet_kiemkho ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "kiemkho_id INT NOT NULL, "
                + "nguyenlieu_id INT NOT NULL, "
                + "ton_he_thong DECIMAL(10,2) DEFAULT 0, "
                + "ton_thuc_te DECIMAL(10,2) DEFAULT 0, "
                + "chenh_lech DECIMAL(10,2) DEFAULT 0, "
                + "ly_do VARCHAR(255))";
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(createKiemKho);
            statement.executeUpdate(createChiTiet);
        }
    }

    public List<KiemKho> search(String status, String fromDate, String toDate) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT kk.*, nv.ten_nhanvien FROM kiemkho kk "
                        + "LEFT JOIN nhanvien nv ON kk.nhanvien_kiem = nv.id WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (status != null && !status.isBlank() && !"all".equals(status)) {
            sql.append("AND kk.trang_thai = ? ");
            params.add(status);
        }
        if (fromDate != null && !fromDate.isBlank()) {
            sql.append("AND DATE(kk.ngay_kiem) >= ? ");
            params.add(Date.valueOf(fromDate));
        }
        if (toDate != null && !toDate.isBlank()) {
            sql.append("AND DATE(kk.ngay_kiem) <= ? ");
            params.add(Date.valueOf(toDate));
        }
        sql.append("ORDER BY kk.ngay_tao DESC LIMIT 200");

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                List<KiemKho> list = new ArrayList<>();
                while (resultSet.next()) {
                    list.add(mapKiemKho(resultSet));
                }
                return list;
            }
        }
    }

    public Map<String, Integer> getStats() throws SQLException {
        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("cho_duyet", countByStatus("cho_duyet"));
        stats.put("da_duyet", countByStatus("da_duyet"));
        stats.put("da_huy", countByStatus("da_huy"));
        return stats;
    }

    private int countByStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM kiemkho WHERE trang_thai = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        }
    }

    public Optional<KiemKho> findById(int id) throws SQLException {
        String sql = "SELECT kk.*, nv.ten_nhanvien FROM kiemkho kk "
                + "LEFT JOIN nhanvien nv ON kk.nhanvien_kiem = nv.id WHERE kk.id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    KiemKho phieu = mapKiemKho(resultSet);
                    phieu.setItems(findDetails(connection, id));
                    return Optional.of(phieu);
                }
                return Optional.empty();
            }
        }
    }

    private List<KiemKhoChiTiet> findDetails(Connection connection, int kiemKhoId) throws SQLException {
        String sql = "SELECT ck.*, nl.ma_nguyenlieu, nl.ten_nguyenlieu, nl.don_vi_tinh "
                + "FROM chitiet_kiemkho ck "
                + "JOIN nguyenvatlieu nl ON ck.nguyenlieu_id = nl.id "
                + "WHERE ck.kiemkho_id = ? ORDER BY nl.ten_nguyenlieu";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, kiemKhoId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<KiemKhoChiTiet> items = new ArrayList<>();
                while (resultSet.next()) {
                    KiemKhoChiTiet item = new KiemKhoChiTiet();
                    item.setId(resultSet.getInt("id"));
                    item.setKiemKhoId(resultSet.getInt("kiemkho_id"));
                    item.setNguyenLieuId(resultSet.getInt("nguyenlieu_id"));
                    item.setMaNguyenLieu(resultSet.getString("ma_nguyenlieu"));
                    item.setTenNguyenLieu(resultSet.getString("ten_nguyenlieu"));
                    item.setDonViTinh(resultSet.getString("don_vi_tinh"));
                    item.setTonHeThong(resultSet.getBigDecimal("ton_he_thong"));
                    item.setTonThucTe(resultSet.getBigDecimal("ton_thuc_te"));
                    item.setChenhLech(resultSet.getBigDecimal("chenh_lech"));
                    item.setLyDo(resultSet.getString("ly_do"));
                    items.add(item);
                }
                return items;
            }
        }
    }

    public int insertPhieu(Connection connection, KiemKho phieu) throws SQLException {
        String sql = "INSERT INTO kiemkho (ma_phieu_kiem, ngay_kiem, nhanvien_kiem, ghi_chu, trang_thai) "
                + "VALUES (?, ?, ?, ?, 'cho_duyet')";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, phieu.getMaPhieuKiem());
            statement.setDate(2, Date.valueOf(phieu.getNgayKiem()));
            statement.setInt(3, phieu.getNhanVienKiem());
            statement.setString(4, phieu.getGhiChu());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return 0;
    }

    public void insertChiTiet(Connection connection, int kiemKhoId, KiemKhoChiTiet item) throws SQLException {
        String sql = "INSERT INTO chitiet_kiemkho (kiemkho_id, nguyenlieu_id, ton_he_thong, ton_thuc_te, chenh_lech, ly_do) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, kiemKhoId);
            statement.setInt(2, item.getNguyenLieuId());
            statement.setBigDecimal(3, item.getTonHeThong());
            statement.setBigDecimal(4, item.getTonThucTe());
            statement.setBigDecimal(5, item.getChenhLech());
            statement.setString(6, item.getLyDo());
            statement.executeUpdate();
        }
    }

    public BigDecimal getCurrentStock(Connection connection, int nguyenLieuId) throws SQLException {
        String sql = "SELECT so_luong_ton FROM nguyenvatlieu WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, nguyenLieuId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    BigDecimal value = resultSet.getBigDecimal("so_luong_ton");
                    return value == null ? BigDecimal.ZERO : value;
                }
                return BigDecimal.ZERO;
            }
        }
    }

    public void updateStatus(Connection connection, int id, String status) throws SQLException {
        String sql = "UPDATE kiemkho SET trang_thai = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, id);
            statement.executeUpdate();
        }
    }

    private KiemKho mapKiemKho(ResultSet resultSet) throws SQLException {
        KiemKho phieu = new KiemKho();
        phieu.setId(resultSet.getInt("id"));
        phieu.setMaPhieuKiem(resultSet.getString("ma_phieu_kiem"));
        Date ngayKiem = resultSet.getDate("ngay_kiem");
        if (ngayKiem != null) {
            phieu.setNgayKiem(ngayKiem.toLocalDate());
        }
        phieu.setNhanVienKiem(resultSet.getInt("nhanvien_kiem"));
        phieu.setTenNhanVien(resultSet.getString("ten_nhanvien"));
        phieu.setTrangThai(resultSet.getString("trang_thai"));
        phieu.setGhiChu(resultSet.getString("ghi_chu"));
        Timestamp ngayTao = resultSet.getTimestamp("ngay_tao");
        if (ngayTao != null) {
            phieu.setNgayTao(ngayTao.toLocalDateTime());
        }
        return phieu;
    }
}
