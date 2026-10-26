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

public class DeliveryService {

    private static final ObjectMapper JSON = new ObjectMapper();
    private final ForecastDao forecastDao = new ForecastDao();
    private final TableDao tableDao = new TableDao();
    private final OrderService orderService = new OrderService();
    private final SettingsService settingsService = new SettingsService();

    public String acceptDelivery(String apiKey, String body) throws SQLException {
        String expected = settingsService.getSettingsWithDefaults().getOrDefault("delivery_api_key", "");
        if (expected.isBlank() || apiKey == null || !MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8), apiKey.getBytes(StandardCharsets.UTF_8))) {
            throw new SQLException("Khóa kết nối sàn không đúng.");
        }
        try {
            JsonNode root = JSON.readTree(body == null ? "" : body);
            String source = text(root, "nguon_don");
            if (!source.equals("shopeefood") && !source.equals("grabfood") && !source.equals("befood")) {
                throw new SQLException("Nguồn đơn phải là shopeefood, grabfood hoặc befood.");
            }
            List<IncomingOrderItem> items = new ArrayList<>();
            for (JsonNode node : root.withArray("items")) {
                String name = text(node, "ten");
                Optional<Integer> productId = forecastDao.findProductIdByName(name);
                if (productId.isEmpty()) {
                    throw new SQLException("Không tìm thấy món trên thực đơn: " + name);
                }
                IncomingOrderItem item = new IncomingOrderItem();
                item.id = productId.get();
                item.ten = name;
                item.sl = Math.max(node.path("sl").asInt(1), 1);
                item.gia = node.has("gia") ? node.get("gia").decimalValue() : BigDecimal.ZERO;
                items.add(item);
            }
            if (items.isEmpty()) {
                throw new SQLException("Đơn sàn không có món.");
            }
            CreateOrderResult result = orderService.processSingleCreate(
                    "create_order",
                    JSON.writeValueAsString(items),
                    text(root, "ten_khachhang"),
                    text(root, "sdt_khachhang"),
                    "app_online",
                    null,
                    "");
            if (!result.success()) {
                throw new SQLException(result.message());
            }
            tableDao.tagSource(result.orderId(), source);
            return result.message();
        } catch (SQLException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new SQLException("Dữ liệu đơn sàn không hợp lệ.");
        }
    }

    private static String text(JsonNode node, String field) {
        return node.path(field).asText("").trim();
    }

}
