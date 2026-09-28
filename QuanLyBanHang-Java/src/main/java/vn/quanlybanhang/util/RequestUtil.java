package vn.quanlybanhang.util;

import java.math.BigDecimal;

import jakarta.servlet.http.HttpServletRequest;

public final class RequestUtil {

    private RequestUtil() {
    }

    public static String trimOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    public static String paramOrEmpty(HttpServletRequest request, String name) {
        return trimOrEmpty(request.getParameter(name));
    }

    public static String getTrimmed(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? null : value.trim();
    }

    public static int getInt(HttpServletRequest request, String name, int defaultValue) {
        try {
            return Integer.parseInt(paramOrEmpty(request, name));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public static int parseInt(String value, int defaultValue) {
        try {
            return value == null || value.isBlank() ? defaultValue : Integer.parseInt(value.trim());
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public static Integer getNullableInt(HttpServletRequest request, String name) {
        try {
            String value = getTrimmed(request, name);
            return (value == null || value.isBlank()) ? null : Integer.parseInt(value);
        } catch (Exception ex) {
            return null;
        }
    }

    public static BigDecimal getBigDecimal(HttpServletRequest request, String name, BigDecimal defaultValue) {
        return parseBigDecimal(request.getParameter(name), defaultValue);
    }

    public static BigDecimal parseBigDecimal(String value, BigDecimal defaultValue) {
        try {
            if (value == null || value.isBlank()) {
                return defaultValue;
            }
            return new BigDecimal(value.trim());
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public static boolean wantsJson(HttpServletRequest request) {
        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            return true;
        }
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains("application/json");
    }
}
