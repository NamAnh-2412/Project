package vn.quanlybanhang.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import vn.quanlybanhang.dao.DashboardDao;

public class DashboardService {

    private static final int CHART_MIN_HEIGHT = 30;
    private static final int CHART_MAX_HEIGHT = 200;
    private static final String[] WEEKDAY_LABELS = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};

    private final DashboardDao dashboardDao = new DashboardDao();

    public HomeDashboardData loadHomeDashboard() {
        try {
            Map<String, Object> stats = dashboardDao.getTodayStats();
            int totalOrders = ((Number) stats.getOrDefault("totalOrders", 0)).intValue();
            BigDecimal totalRevenue = (BigDecimal) stats.getOrDefault("totalRevenue", BigDecimal.ZERO);

            return new HomeDashboardData(
                    totalOrders,
                    totalOrders,
                    totalRevenue,
                    dashboardDao.getTopProductsToday(3),
                    buildWeeklyChart(dashboardDao.getWeeklyOrderCounts()));
        } catch (SQLException ex) {
            return HomeDashboardData.empty();
        }
    }

    public Map<String, Object> getTodayStatsApiPayload() throws SQLException {
        Map<String, Object> stats = dashboardDao.getTodayStats();
        int orders = ((Number) stats.getOrDefault("totalOrders", 0)).intValue();
        BigDecimal revenue = (BigDecimal) stats.getOrDefault("totalRevenue", BigDecimal.ZERO);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("status", "success");
        payload.put("orders", orders);
        payload.put("customers", orders);
        payload.put("revenue", formatRevenueVnd(revenue));
        return payload;
    }

    public static String formatRevenueVnd(BigDecimal revenue) {
        BigDecimal value = revenue == null ? BigDecimal.ZERO : revenue;
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("vi-VN"));
        symbols.setGroupingSeparator('.');
        DecimalFormat formatter = new DecimalFormat("#,##0", symbols);
        return formatter.format(value) + "\u0111";
    }

    static List<Map<String, Object>> buildWeeklyChart(List<Integer> counts) {
        int max = counts.stream().mapToInt(Integer::intValue).max().orElse(0);
        if (max == 0) {
            max = 1;
        }

        List<Map<String, Object>> chart = new ArrayList<>();
        for (int i = 0; i < WEEKDAY_LABELS.length; i++) {
            int count = i < counts.size() ? counts.get(i) : 0;
            int height = CHART_MIN_HEIGHT + (int) Math.round((count / (double) max) * (CHART_MAX_HEIGHT - CHART_MIN_HEIGHT));

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("label", WEEKDAY_LABELS[i]);
            item.put("value", count);
            item.put("height", height);
            chart.add(item);
        }
        return chart;
    }

    public record HomeDashboardData(
            int todayOrders,
            int todayCustomers,
            BigDecimal todayRevenue,
            List<Map<String, Object>> topProducts,
            List<Map<String, Object>> weeklyOrders) {

        public static HomeDashboardData empty() {
            return new HomeDashboardData(0, 0, BigDecimal.ZERO, List.of(), List.of());
        }
    }
}
