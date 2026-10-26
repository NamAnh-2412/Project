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

public class ChannelService {

    private final SettingsDao settingsDao = new SettingsDao();

    public void saveChannelSettings(String botToken, String chatId, String deliveryKey, String provider,
            String invoiceUser, String invoicePassword, String lat, String lng, String radius) throws SQLException {
        putIfPresent("telegram_bot_token", botToken);
        putIfPresent("telegram_chat_id", chatId);
        putIfPresent("delivery_api_key", deliveryKey);
        settingsDao.setSetting("einvoice_provider", provider == null ? "" : provider.trim());
        putIfPresent("einvoice_username", invoiceUser);
        if (invoicePassword != null && !invoicePassword.isBlank()) {
            settingsDao.setSetting("einvoice_password", invoicePassword.trim());
        }
        settingsDao.setSetting("geofence_lat", blank(lat));
        settingsDao.setSetting("geofence_lng", blank(lng));
        settingsDao.setSetting("geofence_met", blank(radius));
    }

    private void putIfPresent(String key, String value) throws SQLException {
        if (value != null && !value.isBlank()) {
            settingsDao.setSetting(key, value.trim());
        }
    }

    private static String blank(String value) {
        return value == null ? "" : value.trim();
    }

}
