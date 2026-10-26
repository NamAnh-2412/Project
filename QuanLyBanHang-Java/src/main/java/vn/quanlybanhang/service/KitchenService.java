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

public class KitchenService {

    private final KitchenDao kitchenDao = new KitchenDao();

    public List<Map<String, Object>> kitchen() throws SQLException {
        return kitchenDao.kitchenTickets();
    }

    public void kitchenAction(String action, int orderId, int itemId) throws SQLException {
        switch (action) {
            case "start" -> kitchenDao.setOrderKitchen(orderId, "dang_lam");
            case "item" -> kitchenDao.setItemKitchen(itemId, "xong");
            case "finish" -> kitchenDao.finishTicket(orderId);
            default -> throw new SQLException("Hành động bếp không hợp lệ.");
        }
    }

}
