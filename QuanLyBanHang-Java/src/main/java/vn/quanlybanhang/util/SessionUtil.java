package vn.quanlybanhang.util;

import vn.quanlybanhang.constants.AppConstants;
import vn.quanlybanhang.model.NhanVien;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class SessionUtil {

    public static final String SESSION_LAST_ACTIVITY = "last_activity_time";

    private SessionUtil() {
    }

    public static void login(HttpServletRequest request, NhanVien employee) {
        HttpSession session = request.getSession(true);
        session.setAttribute(AppConstants.SESSION_USER_ID, employee.getId());
        session.setAttribute(AppConstants.SESSION_USERNAME, employee.getTenDangNhap());
        session.setAttribute(AppConstants.SESSION_ROLE, employee.getChucVu());
        session.setAttribute("ten_nhanvien", employee.getDisplayName());
        session.setAttribute(SESSION_LAST_ACTIVITY, System.currentTimeMillis());
    }

    public static void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    public static boolean isLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute(AppConstants.SESSION_USER_ID) != null;
    }

    public static Integer getCurrentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(AppConstants.SESSION_USER_ID);
        if (value instanceof Integer integer) {
            return integer;
        }
        return value != null ? Integer.parseInt(value.toString()) : null;
    }

    public static String getCurrentUsername(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (String) session.getAttribute(AppConstants.SESSION_USERNAME);
    }

    public static String getCurrentRole(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (String) session.getAttribute(AppConstants.SESSION_ROLE);
    }

    public static String getDisplayName(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object displayName = session.getAttribute("ten_nhanvien");
        if (displayName instanceof String value && !value.isBlank()) {
            return value;
        }
        return getCurrentUsername(request);
    }

    public static String getFirstChar(HttpServletRequest request) {
        String displayName = getDisplayName(request);
        if (displayName == null || displayName.isBlank()) {
            return "?";
        }
        return displayName.substring(0, 1).toUpperCase();
    }

    public static boolean isSessionTimedOut(HttpServletRequest request, int timeoutMinutes) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return true;
        }
        Object value = session.getAttribute(SESSION_LAST_ACTIVITY);
        if (!(value instanceof Long lastActivity)) {
            session.setAttribute(SESSION_LAST_ACTIVITY, System.currentTimeMillis());
            return false;
        }
        long idleMs = timeoutMinutes * 60L * 1000L;
        return System.currentTimeMillis() - lastActivity > idleMs;
    }

    public static void touchSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.setAttribute(SESSION_LAST_ACTIVITY, System.currentTimeMillis());
        }
    }
}
