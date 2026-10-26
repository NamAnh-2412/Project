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

public class InvoiceService {

    private final InvoiceDao invoiceDao = new InvoiceDao();
    private final SettingsService settingsService = new SettingsService();

    public String requestInvoice(int orderId) throws SQLException {
        Map<String, String> settings = settingsService.getSettingsWithDefaults();
        String provider = settings.getOrDefault("einvoice_provider", "");
        String username = settings.getOrDefault("einvoice_username", "");
        if (provider.isBlank() || username.isBlank()) {
            invoiceDao.insertInvoice(orderId, provider.isBlank() ? "chua_chon" : provider, "chua_ket_noi",
                    "Chưa nhập nhà cung cấp và tài khoản hóa đơn điện tử trong mục Mở rộng.");
            return "Đã ghi nhận yêu cầu. Chưa kết nối Viettel, VNPT hoặc MISA nên chưa có mã cơ quan thuế.";
        }
        invoiceDao.insertInvoice(orderId, provider, "chua_ket_noi",
                "Đã có tài khoản nhà cung cấp, nhưng cổng hóa đơn điện tử chưa được nối API thật.");
        return "Chưa gửi lên cơ quan thuế. Hệ thống không tạo mã tra cứu giả.";
    }

    public Optional<Map<String, Object>> latestInvoice(int orderId) throws SQLException {
        return invoiceDao.latestInvoice(orderId);
    }

}
