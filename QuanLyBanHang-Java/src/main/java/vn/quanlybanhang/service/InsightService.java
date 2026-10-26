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

public class InsightService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private final ForecastDao forecastDao = new ForecastDao();
    private final SettingsDao settingsDao = new SettingsDao();
    private final SettingsService settingsService = new SettingsService();

    public List<Map<String, Object>> forecastTomorrow() throws SQLException {
        DayOfWeek target = LocalDate.now(ZONE).plusDays(1).getDayOfWeek();
        Map<String, List<BigDecimal>> buckets = new LinkedHashMap<>();
        for (Map<String, Object> row : forecastDao.salesByDay(56)) {
            LocalDate day = (LocalDate) row.get("ngay");
            if (day.getDayOfWeek() != target) {
                continue;
            }
            String name = String.valueOf(row.get("ten"));
            buckets.computeIfAbsent(name, ignored -> new ArrayList<>()).add((BigDecimal) row.get("sl"));
        }
        List<Map<String, Object>> forecast = new ArrayList<>();
        for (Map.Entry<String, List<BigDecimal>> entry : buckets.entrySet()) {
            BigDecimal sum = BigDecimal.ZERO;
            for (BigDecimal value : entry.getValue()) {
                sum = sum.add(value);
            }
            BigDecimal average = sum.divide(BigDecimal.valueOf(entry.getValue().size()), 1, RoundingMode.HALF_UP);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("ten", entry.getKey());
            row.put("duBao", average);
            forecast.add(row);
        }
        forecast.sort(Comparator.comparing((Map<String, Object> row) -> (BigDecimal) row.get("duBao")).reversed());
        return forecast.size() > 8 ? forecast.subList(0, 8) : forecast;
    }

    public List<Map<String, Object>> suggestions(int productId) throws SQLException {
        return forecastDao.crossSell(productId);
    }

    public String dailyReportText() throws SQLException {
        Map<String, Object> today = forecastDao.todaySummary();
        BigDecimal tong = (BigDecimal) today.getOrDefault("tong", BigDecimal.ZERO);
        BigDecimal homQua = (BigDecimal) today.getOrDefault("homQua", BigDecimal.ZERO);
        String delta = homQua.signum() == 0 ? "" : " (" + percent(tong, homQua) + " so với hôm qua)";
        StringBuilder text = new StringBuilder();
        text.append("BÁO CÁO KINH DOANH ").append(LocalDate.now(ZONE)).append(" - ")
                .append(settingsService.getShopName()).append('\n');
        text.append("Tổng doanh thu: ").append(money(tong)).append(delta).append('\n');
        text.append("Số đơn: ").append(today.getOrDefault("soDon", 0)).append('\n');
        text.append("Tiền mặt: ").append(money((BigDecimal) today.get("tienMat")))
                .append(" | Chuyển khoản: ").append(money((BigDecimal) today.get("chuyenKhoan"))).append('\n');
        text.append("Top món:\n");
        int index = 1;
        for (Map<String, Object> product : forecastDao.topProductsToday()) {
            text.append(index++).append(". ").append(product.get("ten")).append(": ")
                    .append(product.get("sl")).append(" (").append(money((BigDecimal) product.get("tien"))).append(")\n");
        }
        List<Map<String, Object>> low = forecastDao.lowStock();
        if (!low.isEmpty()) {
            text.append("Cảnh báo kho:\n");
            for (Map<String, Object> row : low) {
                text.append("• ").append(row.get("ten")).append(": còn ").append(row.get("ton"))
                        .append(' ').append(row.get("donVi")).append('\n');
            }
        }
        return text.toString();
    }

    public Optional<String> sendTelegramNow() throws SQLException {
        return postTelegram(dailyReportText());
    }

    public void sendScheduledReport() throws SQLException {
        LocalTime now = LocalTime.now(ZONE);
        if (now.getHour() != 22 || now.getMinute() >= 15) {
            return;
        }
        String today = LocalDate.now(ZONE).toString();
        String sent = settingsService.getSettingsWithDefaults().getOrDefault("telegram_last_report", "");
        if (today.equals(sent)) {
            return;
        }
        Optional<String> error = postTelegram(dailyReportText());
        if (error.isEmpty()) {
            settingsDao.setSetting("telegram_last_report", today);
        }
    }

    private Optional<String> postTelegram(String text) throws SQLException {
        Map<String, String> settings = settingsService.getSettingsWithDefaults();
        String token = settings.getOrDefault("telegram_bot_token", "");
        String chatId = settings.getOrDefault("telegram_chat_id", "");
        if (token.isBlank() || chatId.isBlank()) {
            return Optional.of("Nhập token bot và chat id trong mục Mở rộng.");
        }
        try {
            String form = "chat_id=" + URLEncoder.encode(chatId, StandardCharsets.UTF_8)
                    + "&text=" + URLEncoder.encode(text, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.telegram.org/bot" + token + "/sendMessage"))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form))
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                return Optional.of("Telegram không nhận báo cáo.");
            }
            return Optional.empty();
        } catch (Exception ex) {
            return Optional.of("Không gửi được Telegram.");
        }
    }

    private static String money(BigDecimal value) {
        if (value == null) {
            return "0";
        }
        return value.setScale(0, RoundingMode.HALF_UP).toPlainString() + "đ";
    }

    private static String percent(BigDecimal today, BigDecimal yesterday) {
        if (yesterday == null || yesterday.signum() == 0) {
            return "";
        }
        BigDecimal rate = today.subtract(yesterday).multiply(BigDecimal.valueOf(100))
                .divide(yesterday, 0, RoundingMode.HALF_UP);
        return (rate.signum() > 0 ? "+" : "") + rate.toPlainString() + "%";
    }

}
