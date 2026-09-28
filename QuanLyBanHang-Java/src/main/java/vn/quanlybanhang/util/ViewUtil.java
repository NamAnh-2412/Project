package vn.quanlybanhang.util;

import java.sql.SQLException;

import vn.quanlybanhang.service.SettingsService;

import jakarta.servlet.http.HttpServletRequest;

public final class ViewUtil {

    private static final SettingsService SETTINGS_SERVICE = new SettingsService();

    private ViewUtil() {
    }

    public static void applySharedAttributes(HttpServletRequest request) {
        request.setAttribute("currentUsername", SessionUtil.getCurrentUsername(request));
        request.setAttribute("displayName", SessionUtil.getDisplayName(request));
        request.setAttribute("firstChar", SessionUtil.getFirstChar(request));
        request.setAttribute("currentRole", SessionUtil.getCurrentRole(request));
        request.setAttribute("contextPath", request.getContextPath());
        applyBranding(request);
    }

    public static void applyBranding(HttpServletRequest request) {
        try {
            request.setAttribute("shopName", SETTINGS_SERVICE.getShopName());
            request.setAttribute("shopLogoInitials", SETTINGS_SERVICE.getShopLogoInitials());
            request.setAttribute("logoPath", SETTINGS_SERVICE.getLogoPath());
        } catch (SQLException ex) {
            request.setAttribute("shopName", "CHILL GUYS");
            request.setAttribute("shopLogoInitials", "CG");
            request.setAttribute("logoPath", null);
        }
    }
}
