package vn.quanlybanhang.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import vn.quanlybanhang.service.ImageFiles;
import vn.quanlybanhang.service.LogoUploadService;
import vn.quanlybanhang.service.SettingsService;
import vn.quanlybanhang.util.JsonUtil;
import vn.quanlybanhang.util.RequestUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet(urlPatterns = {
        "/caidat",
        "/api/logo-upload",
        "/api/logo-delete"
})
@MultipartConfig(maxFileSize = ImageFiles.MAX_BYTES, maxRequestSize = ImageFiles.MAX_BYTES + 1_048_576L)
public class SettingsController extends BaseController {

    private static final long serialVersionUID = 1L;

    private final SettingsService settingsService = new SettingsService();
    private final LogoUploadService logoUploadService = new LogoUploadService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!"/caidat".equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            request.setAttribute("pageTitle", "Cài đặt");
            request.setAttribute("pageHeaderTitle", "Cài đặt hệ thống");
            applyPageChrome(request, "fas fa-cog",
                    "Cấu hình thông tin quán, hệ thống, bảo mật, kho và đơn hàng.", List.of("settings.css"));
            request.setAttribute("settings", settingsService.getSettingsWithDefaults());
            render(request, response, "/WEB-INF/views/caidat/index.jsp");
        } catch (SQLException ex) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            switch (request.getServletPath()) {
                case "/caidat" -> saveSettings(request, response);
                case "/api/logo-upload" -> uploadLogo(request, response);
                case "/api/logo-delete" -> deleteLogo(response);
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException ex) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    private void saveSettings(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        String action = RequestUtil.paramOrEmpty(request, "action");
        if (action.isBlank()) {
            action = "save_shop_info";
        }

        switch (action) {
            case "save_shop_info" -> saveShopInfo(request, response);
            case "save_system" -> saveSystem(request, response);
            case "save_security" -> saveSecurity(request, response);
            case "save_inventory" -> saveInventory(request, response);
            case "save_orders" -> saveOrders(request, response);
            default -> flashAndRedirect(request, response, "error", "Hành động không hợp lệ.");
        }
    }

    private void saveShopInfo(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        var error = settingsService.saveShopInfoValidated(
                paramOrEmpty(request, "ten_cua_hang"),
                paramOrEmpty(request, "dia_chi"),
                paramOrEmpty(request, "so_dien_thoai"),
                paramOrEmpty(request, "email"),
                paramOrEmpty(request, "chu_logo_quan"));
        if (error.isPresent()) {
            flashAndRedirect(request, response, "error", error.get());
            return;
        }
        var bankError = settingsService.saveBankSettings(
                paramOrEmpty(request, "bank_name"),
                paramOrEmpty(request, "bank_bin"),
                paramOrEmpty(request, "bank_account_number"),
                paramOrEmpty(request, "bank_account_name"));
        if (bankError.isPresent()) {
            flashAndRedirect(request, response, "error", bankError.get());
            return;
        }
        flashAndRedirect(request, response, "success", "Đã lưu thông tin cửa hàng thành công.");
    }

    private void saveSystem(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        var error = settingsService.saveSystemSettingsValidated(getIntParameter(request, "items_per_page", 20));
        if (error.isPresent()) {
            flashAndRedirect(request, response, "error", error.get());
            return;
        }
        flashAndRedirect(request, response, "success", "Đã lưu cài đặt hệ thống thành công.");
    }

    private void saveSecurity(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        var error = settingsService.saveSecuritySettingsValidated(
                getIntParameter(request, "password_min_length", 6),
                getIntParameter(request, "session_timeout", 60));
        if (error.isPresent()) {
            flashAndRedirect(request, response, "error", error.get());
            return;
        }
        flashAndRedirect(request, response, "success", "Đã lưu cài đặt bảo mật thành công.");
    }

    private void saveInventory(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        var error = settingsService.saveInventorySettingsValidated(
                getIntParameter(request, "expiry_alert_days_7", 7),
                getIntParameter(request, "expiry_alert_days_1", 1),
                RequestUtil.getBigDecimal(request, "low_stock_threshold", BigDecimal.TEN));
        if (error.isPresent()) {
            flashAndRedirect(request, response, "error", error.get());
            return;
        }
        flashAndRedirect(request, response, "success", "Đã lưu cài đặt kho thành công.");
    }

    private void saveOrders(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        settingsService.saveOrderSettingsValidated(
                paramOrEmpty(request, "default_order_status"),
                paramOrEmpty(request, "default_payment_method"),
                "1".equals(request.getParameter("allow_cancel_completed_order")));
        flashAndRedirect(request, response, "success", "Đã lưu cài đặt đơn hàng thành công.");
    }

    private void flashAndRedirect(HttpServletRequest request, HttpServletResponse response, String type, String message)
            throws IOException {
        setFlash(request, type, message);
        redirect(request, response, "/caidat");
    }

    private void uploadLogo(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException, SQLException {
        try {
            Part part = request.getPart("logo");
            Map<String, Object> result = logoUploadService.uploadLogo(part, request.getContextPath());
            JsonUtil.writeJson(response, result);
        } catch (IllegalStateException ex) {
            JsonUtil.writeJson(response, Map.of("success", false, "error", "Ảnh không được lớn hơn 5MB."));
        }
    }

    private void deleteLogo(HttpServletResponse response) throws SQLException, IOException {
        logoUploadService.clearLogo();
        JsonUtil.writeJson(response, Map.of("success", true));
    }
}
