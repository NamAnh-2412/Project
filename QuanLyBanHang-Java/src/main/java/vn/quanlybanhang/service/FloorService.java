package vn.quanlybanhang.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import vn.quanlybanhang.dao.BranchDao;
import vn.quanlybanhang.dao.DisplayDao;
import vn.quanlybanhang.dao.ForecastDao;
import vn.quanlybanhang.dao.InvoiceDao;
import vn.quanlybanhang.dao.KitchenDao;
import vn.quanlybanhang.dao.ProductDao;
import vn.quanlybanhang.dao.SettingsDao;
import vn.quanlybanhang.dao.TableDao;
import vn.quanlybanhang.model.LuaChon;
import vn.quanlybanhang.model.NhomLuaChon;
import vn.quanlybanhang.model.Product;
import vn.quanlybanhang.model.ProductSize;
import vn.quanlybanhang.service.OrderService.CreateOrderResult;
import vn.quanlybanhang.service.OrderService.IncomingOrderItem;

public class FloorService {

    private final TableDao tableDao = new TableDao();
    private final ProductDao productDao = new ProductDao();
    private final OrderService orderService = new OrderService();
    private final SettingsService settingsService = new SettingsService();

    public List<Map<String, Object>> areas() throws SQLException {
        return tableDao.listAreas();
    }

    public List<Map<String, Object>> tables() throws SQLException {
        return tableDao.listTables();
    }

    public Optional<String> addArea(String name) throws SQLException {
        if (name == null || name.isBlank()) {
            return Optional.of("Nhập tên khu vực.");
        }
        tableDao.insertArea(name.trim());
        return Optional.empty();
    }

    public Optional<String> addTable(int areaId, String name, int seats) throws SQLException {
        if (areaId <= 0 || name == null || name.isBlank()) {
            return Optional.of("Chọn khu vực và nhập tên bàn.");
        }
        tableDao.insertTable(areaId, name.trim(), seats);
        return Optional.empty();
    }

    public void refreshQr(int tableId) throws SQLException {
        tableDao.refreshQr(tableId);
    }

    public Optional<Map<String, Object>> tableByToken(String token) throws SQLException {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return tableDao.findTableByToken(token.trim());
    }

    public List<Map<String, Object>> publicMenu() throws SQLException {
        List<Map<String, Object>> menu = new ArrayList<>();
        for (Product product : productDao.findActiveByCategory(null)) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", product.getId());
            row.put("ten", product.getTenSanPham());
            row.put("gia", product.getGiaBan() == null ? BigDecimal.ZERO : product.getGiaBan());
            row.put("anh", product.getAnhUrl() == null ? "" : product.getAnhUrl());
            row.put("nhom", product.getTenDanhMuc() == null ? "" : product.getTenDanhMuc());
            List<Map<String, Object>> sizes = new ArrayList<>();
            for (ProductSize size : productDao.findActiveSizesByProductId(product.getId())) {
                Map<String, Object> sizeRow = new LinkedHashMap<>();
                sizeRow.put("id", size.getId());
                sizeRow.put("ten", size.getTenSize());
                sizeRow.put("gia", size.getGia() == null ? BigDecimal.ZERO : size.getGia());
                sizes.add(sizeRow);
            }
            row.put("sizes", sizes);
            List<Map<String, Object>> groups = new ArrayList<>();
            for (NhomLuaChon group : productDao.findChoiceGroupsByProductId(product.getId())) {
                Map<String, Object> groupRow = new LinkedHashMap<>();
                groupRow.put("id", group.getId());
                groupRow.put("ten", group.getTenNhom());
                groupRow.put("batBuoc", group.isBatBuoc());
                List<Map<String, Object>> choices = new ArrayList<>();
                for (LuaChon choice : group.getLuaChonList()) {
                    Map<String, Object> choiceRow = new LinkedHashMap<>();
                    choiceRow.put("id", choice.getId());
                    choiceRow.put("ten", choice.getTenLuaChon());
                    choiceRow.put("gia", choice.getGiaTangThem() == null ? BigDecimal.ZERO : choice.getGiaTangThem());
                    choices.add(choiceRow);
                }
                groupRow.put("luaChon", choices);
                groups.add(groupRow);
            }
            row.put("nhomLuaChon", groups);
            menu.add(row);
        }
        return menu;
    }

    public String placeTableOrder(String token, String itemsJson, String customerName, String phone,
            Double latitude, Double longitude) throws SQLException {
        Map<String, Object> table = tableByToken(token).orElseThrow(() -> new SQLException("Mã bàn không còn hiệu lực."));
        assertInsideShop(latitude, longitude);
        CreateOrderResult result = orderService.processSingleCreate(
                "save_pending_order", itemsJson, customerName, phone, "tien_mat", null, "");
        if (!result.success()) {
            throw new SQLException(result.message());
        }
        int orderId = result.orderId();
        int tableId = (Integer) table.get("id");
        int sessionId = tableDao.openSession(tableId);
        tableDao.attachOrder(orderId, tableId, sessionId, "qr_ban");
        return result.message();
    }

    private void assertInsideShop(Double latitude, Double longitude) throws SQLException {
        Map<String, String> settings = settingsService.getSettingsWithDefaults();
        String latText = settings.getOrDefault("geofence_lat", "");
        String lngText = settings.getOrDefault("geofence_lng", "");
        if (latText.isBlank() || lngText.isBlank()) {
            return;
        }
        if (latitude == null || longitude == null) {
            throw new SQLException("Quán yêu cầu bật vị trí điện thoại khi gọi món.");
        }
        double radius = 50;
        try {
            if (!settings.getOrDefault("geofence_met", "").isBlank()) {
                radius = Double.parseDouble(settings.get("geofence_met"));
            }
            double shopLat = Double.parseDouble(latText);
            double shopLng = Double.parseDouble(lngText);
            if (distanceMeters(shopLat, shopLng, latitude, longitude) > radius) {
                throw new SQLException("Bạn đang ở ngoài phạm vi quán nên chưa gọi món được.");
            }
        } catch (NumberFormatException ex) {
            throw new SQLException("Tọa độ quán trong phần Mở rộng chưa đúng.");
        }
    }

    private static double distanceMeters(double lat1, double lng1, double lat2, double lng2) {
        double earth = 6_371_000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return earth * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

}
