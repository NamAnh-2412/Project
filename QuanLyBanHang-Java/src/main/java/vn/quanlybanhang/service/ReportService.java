package vn.quanlybanhang.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import vn.quanlybanhang.dao.ReportDao;

public class ReportService {

    private final ReportDao reportDao = new ReportDao();

    public record DateRange(String from, String to) {
    }

    public record ReportPageData(
            String tab,
            String from,
            String to,
            List<Map<String, Object>> tongHopData,
            List<Map<String, Object>> chitietData,
            List<Map<String, Object>> hourlyData,
            List<Map<String, Object>> hourlyChart,
            List<Map<String, Object>> dailyRevenueData,
            String dailyRevenueError,
            boolean hasAppOnline,
            BigDecimal tongDoanhThu,
            int tongSoDon) {
    }

    public record RebuildResult(boolean success, String message, int inserted, String timestamp, String error) {
        public static RebuildResult ok(int inserted) {
            return new RebuildResult(true, "Rebuild thành công!", inserted, LocalDateTime.now().toString(), null);
        }

        public static RebuildResult fail(String error) {
            return new RebuildResult(false, null, 0, null, error);
        }
    }

    public DateRange resolveDateRange(String from, String to) {
        String resolvedFrom = (from == null || from.isBlank())
                ? LocalDate.now().withDayOfMonth(1).toString()
                : from;
        String resolvedTo = (to == null || to.isBlank())
                ? LocalDate.now().toString()
                : to;
        return new DateRange(resolvedFrom, resolvedTo);
    }

    public String resolveTab(String tab) {
        if (tab == null || tab.isBlank()) {
            return "tonghop";
        }
        return tab;
    }

    public ReportPageData loadReportPage(String tab, String from, String to) throws SQLException {
        DateRange range = resolveDateRange(from, to);
        String resolvedTab = resolveTab(tab);

        List<Map<String, Object>> tongHop = reportDao.getTongHop(range.from(), range.to());
        List<Map<String, Object>> chitietData = "chitiet".equals(resolvedTab)
                ? reportDao.getPaymentDetails(range.from(), range.to())
                : List.of();
        List<Map<String, Object>> hourlyData = reportDao.getHourly(range.from(), range.to());

        List<Map<String, Object>> dailyRevenueData = List.of();
        String dailyRevenueError = null;
        if ("daily_revenue".equals(resolvedTab)) {
            try {
                dailyRevenueData = reportDao.getDailyRevenue(range.from(), range.to());
            } catch (SQLException ex) {
                dailyRevenueData = List.of();
                dailyRevenueError = ex.getMessage();
            }
        }

        BigDecimal tongDoanhThu = BigDecimal.ZERO;
        int tongSoDon = 0;
        for (Map<String, Object> row : tongHop) {
            tongDoanhThu = tongDoanhThu.add(toBigDecimal(row.get("tong_tien")));
            tongSoDon += ((Number) row.getOrDefault("so_don", 0)).intValue();
        }

        return new ReportPageData(
                resolvedTab,
                range.from(),
                range.to(),
                tongHop,
                chitietData,
                hourlyData,
                buildHourlyChart(hourlyData),
                dailyRevenueData,
                dailyRevenueError,
                reportDao.hasPaymentMethod("app_online"),
                tongDoanhThu,
                tongSoDon);
    }

    public RebuildResult rebuildDailyRevenue() {
        try {
            int inserted = reportDao.rebuildDailyRevenue();
            return RebuildResult.ok(inserted);
        } catch (SQLException ex) {
            return RebuildResult.fail(ex.getMessage());
        }
    }

    public List<Map<String, Object>> buildHourlyChart(List<Map<String, Object>> hourlyData) {
        BigDecimal[] hourlyValues = new BigDecimal[24];
        for (int i = 0; i < hourlyValues.length; i++) {
            hourlyValues[i] = BigDecimal.ZERO;
        }

        for (Map<String, Object> row : hourlyData) {
            int hour = ((Number) row.getOrDefault("gio", 0)).intValue();
            if (hour >= 0 && hour < 24) {
                hourlyValues[hour] = toBigDecimal(row.get("doanh_thu"));
            }
        }

        BigDecimal max = BigDecimal.ONE;
        for (BigDecimal value : hourlyValues) {
            if (value.compareTo(max) > 0) {
                max = value;
            }
        }

        List<Map<String, Object>> chart = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            BigDecimal value = hourlyValues[i];
            int height = 30;
            if (max.compareTo(BigDecimal.ZERO) > 0) {
                height = 30 + (int) Math.round(value.doubleValue() / max.doubleValue() * 170);
            }
            chart.add(Map.of(
                    "label", i + "h",
                    "value", value,
                    "height", height));
        }
        return chart;
    }

    private static BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal amount) {
            return amount;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        try {
            return new BigDecimal(value.toString());
        } catch (Exception ex) {
            return BigDecimal.ZERO;
        }
    }
}
