package vn.quanlybanhang.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import vn.quanlybanhang.constants.AppConstants;
import vn.quanlybanhang.dao.SettingsDao;
import vn.quanlybanhang.util.ValidationUtil;

public class SettingsService {

    private static final int LOGO_INITIALS_MAX = 6;

    private static final Map<String, String> DEFAULTS;
    private static final Set<String> VALID_ORDER_STATUSES = Set.of(
            AppConstants.ORDER_STATUS_PENDING,
            AppConstants.ORDER_STATUS_CONFIRMED,
            AppConstants.ORDER_STATUS_PREPARING,
            AppConstants.ORDER_STATUS_READY);
    private static final Set<String> VALID_PAYMENT_METHODS = Set.of(
            AppConstants.PAYMENT_CASH,
            AppConstants.PAYMENT_BANK,
            AppConstants.PAYMENT_CARD,
            AppConstants.PAYMENT_APP_ONLINE);

    static {
        Map<String, String> defaults = new LinkedHashMap<>();
        defaults.put("ten_cua_hang", "CHILL GUYS");
        defaults.put("dia_chi", "");
        defaults.put("so_dien_thoai", "");
        defaults.put("email", "");
        defaults.put("logo_path", "");
        defaults.put("chu_logo_quan", "");
        defaults.put("items_per_page", "20");
        defaults.put("password_min_length", "6");
        defaults.put("session_timeout", "60");
        defaults.put("expiry_alert_days_7", "7");
        defaults.put("expiry_alert_days_1", "1");
        defaults.put("low_stock_threshold", "10");
        defaults.put("default_order_status", AppConstants.ORDER_STATUS_PENDING);
        defaults.put("default_payment_method", AppConstants.PAYMENT_CASH);
        defaults.put("allow_cancel_completed_order", "0");
        defaults.put("bank_name", "");
        defaults.put("bank_bin", "");
        defaults.put("bank_account_number", "");
        defaults.put("bank_account_name", "");
        DEFAULTS = Collections.unmodifiableMap(defaults);
    }

    private final SettingsDao settingsDao = new SettingsDao();

    public Map<String, String> getAllSettings() throws SQLException {
        return getSettingsWithDefaults();
    }

    public Map<String, String> getSettingsWithDefaults() throws SQLException {
        Map<String, String> stored = settingsDao.getAllSettings();
        Map<String, String> merged = new LinkedHashMap<>(DEFAULTS);
        for (Map.Entry<String, String> entry : stored.entrySet()) {
            if (entry.getValue() != null) {
                merged.put(entry.getKey(), entry.getValue());
            }
        }
        if (merged.get("ten_cua_hang") == null || merged.get("ten_cua_hang").isBlank()) {
            String legacy = stored.get("shop_name");
            if (legacy != null && !legacy.isBlank()) {
                merged.put("ten_cua_hang", legacy);
            }
        }
        return merged;
    }

    public String getShopName() throws SQLException {
        return getSettingsWithDefaults().get("ten_cua_hang");
    }

    public String getShopLogoInitials() throws SQLException {
        Optional<String> stored = Optional.ofNullable(getSettingsWithDefaults().get("chu_logo_quan"));
        if (stored.isPresent() && !stored.get().isBlank()) {
            String normalized = normalizeLogoInitials(stored.get());
            if (!normalized.isEmpty()) {
                return normalized;
            }
        }
        return deriveInitialsFromShopName(getShopName());
    }

    public void setShopLogoInitials(String raw) throws SQLException {
        if (raw == null || raw.isBlank()) {
            settingsDao.setSetting("chu_logo_quan", "");
            return;
        }
        settingsDao.setSetting("chu_logo_quan", normalizeLogoInitials(raw));
    }

    public String getLogoPath() throws SQLException {
        String value = getSettingsWithDefaults().get("logo_path");
        return value == null || value.isBlank() ? null : value;
    }

    public int getItemsPerPage() throws SQLException {
        return clampInt(getSettingsWithDefaults().get("items_per_page"), 20, 5, 200);
    }

    public int getPasswordMinLength() throws SQLException {
        return clampInt(getSettingsWithDefaults().get("password_min_length"), 6, 4, 20);
    }

    public int getSessionTimeoutMinutes() throws SQLException {
        return clampInt(getSettingsWithDefaults().get("session_timeout"), 60, 5, 1440);
    }

    public int getExpiryAlertDays7() throws SQLException {
        return clampInt(getSettingsWithDefaults().get("expiry_alert_days_7"), 7, 1, 30);
    }

    public int getExpiryAlertDays1() throws SQLException {
        return clampInt(getSettingsWithDefaults().get("expiry_alert_days_1"), 1, 1, 30);
    }

    public BigDecimal getLowStockThreshold() throws SQLException {
        return parseDecimal(getSettingsWithDefaults().get("low_stock_threshold"), BigDecimal.TEN);
    }

    public String getDefaultOrderStatus() throws SQLException {
        String value = getSettingsWithDefaults().get("default_order_status");
        return VALID_ORDER_STATUSES.contains(value) ? value : AppConstants.ORDER_STATUS_PENDING;
    }

    public String getDefaultPaymentMethod() throws SQLException {
        String value = getSettingsWithDefaults().get("default_payment_method");
        return VALID_PAYMENT_METHODS.contains(value) ? value : AppConstants.PAYMENT_CASH;
    }

    public boolean isAllowCancelCompletedOrder() throws SQLException {
        return "1".equals(getSettingsWithDefaults().get("allow_cancel_completed_order"));
    }

    public void saveShopInfo(String shopName, String address, String phone, String email, String logoInitials)
            throws SQLException {
        settingsDao.setSetting("ten_cua_hang", shopName.trim());
        settingsDao.setSetting("dia_chi", normalizeOptional(address));
        settingsDao.setSetting("so_dien_thoai", normalizeOptional(phone));
        settingsDao.setSetting("email", normalizeOptional(email));
        setShopLogoInitials(logoInitials);
    }

    public Optional<String> saveBankSettings(String bankName, String bankBin, String accountNumber, String accountName)
            throws SQLException {
        String number = normalizeOptional(accountNumber).replaceAll("\\s+", "");
        String bin = normalizeOptional(bankBin);
        if (!number.isBlank() && !number.chars().allMatch(Character::isDigit)) {
            return Optional.of("Số tài khoản chỉ được gồm chữ số.");
        }
        if (!number.isBlank() && bin.isBlank()) {
            return Optional.of("Hãy chọn ngân hàng khi nhập số tài khoản.");
        }
        settingsDao.setSetting("bank_name", normalizeOptional(bankName));
        settingsDao.setSetting("bank_bin", bin);
        settingsDao.setSetting("bank_account_number", number);
        settingsDao.setSetting("bank_account_name", normalizeOptional(accountName));
        return Optional.empty();
    }

    public String validateShopInfo(String shopName, String address, String phone, String email) {
        if (shopName == null || shopName.isBlank()) {
            return "Vui lòng nhập tên cửa hàng.";
        }
        String normalizedEmail = normalizeOptional(email);
        if (!normalizedEmail.isBlank() && !ValidationUtil.isValidEmail(normalizedEmail)) {
            return "Email không hợp lệ.";
        }
        String normalizedPhone = normalizeOptional(phone);
        if (!normalizedPhone.isBlank() && !ValidationUtil.isValidPhone(normalizedPhone)) {
            return "Số điện thoại không hợp lệ.";
        }
        return null;
    }

    public Optional<String> saveShopInfoValidated(String shopName, String address, String phone, String email,
                                                  String logoInitials) throws SQLException {
        String error = validateShopInfo(shopName, address, phone, email);
        if (error != null) {
            return Optional.of(error);
        }
        saveShopInfo(shopName, address, phone, email, logoInitials);
        return Optional.empty();
    }

    public String validateSystemSettings(int itemsPerPage) {
        if (itemsPerPage < 5 || itemsPerPage > 200) {
            return "Số mục mỗi trang phải từ 5 đến 200.";
        }
        return null;
    }

    public Optional<String> saveSystemSettingsValidated(int itemsPerPage) throws SQLException {
        String error = validateSystemSettings(itemsPerPage);
        if (error != null) {
            return Optional.of(error);
        }
        saveSystemSettings(itemsPerPage);
        return Optional.empty();
    }

    public String validateSecuritySettings(int passwordMinLength, int sessionTimeoutMinutes) {
        if (passwordMinLength < 4 || passwordMinLength > 20) {
            return "Độ dài mật khẩu tối thiểu phải từ 4 đến 20 ký tự.";
        }
        if (sessionTimeoutMinutes < 5 || sessionTimeoutMinutes > 1440) {
            return "Thời gian timeout session phải từ 5 đến 1440 phút.";
        }
        return null;
    }

    public Optional<String> saveSecuritySettingsValidated(int passwordMinLength, int sessionTimeoutMinutes)
            throws SQLException {
        String error = validateSecuritySettings(passwordMinLength, sessionTimeoutMinutes);
        if (error != null) {
            return Optional.of(error);
        }
        saveSecuritySettings(passwordMinLength, sessionTimeoutMinutes);
        return Optional.empty();
    }

    public String validateInventorySettings(int expiryAlertDays7, int expiryAlertDays1, BigDecimal lowStockThreshold) {
        if (expiryAlertDays7 < 1 || expiryAlertDays7 > 30) {
            return "Cảnh báo hết hạn phải từ 1 đến 30 ngày.";
        }
        if (expiryAlertDays1 < 1 || expiryAlertDays1 > 30) {
            return "Cảnh báo sắp hết hạn phải từ 1 đến 30 ngày.";
        }
        if (lowStockThreshold != null && lowStockThreshold.compareTo(BigDecimal.ZERO) < 0) {
            return "Ngưỡng tồn kho thấp không được âm.";
        }
        return null;
    }

    public Optional<String> saveInventorySettingsValidated(int expiryAlertDays7, int expiryAlertDays1,
                                                           BigDecimal lowStockThreshold) throws SQLException {
        String error = validateInventorySettings(expiryAlertDays7, expiryAlertDays1, lowStockThreshold);
        if (error != null) {
            return Optional.of(error);
        }
        saveInventorySettings(expiryAlertDays7, expiryAlertDays1, lowStockThreshold);
        return Optional.empty();
    }

    public Optional<String> saveOrderSettingsValidated(String defaultOrderStatus, String defaultPaymentMethod,
                                                       boolean allowCancelCompleted) throws SQLException {
        saveOrderSettings(defaultOrderStatus, defaultPaymentMethod, allowCancelCompleted);
        return Optional.empty();
    }

    public void saveSystemSettings(int itemsPerPage) throws SQLException {
        settingsDao.setSetting("items_per_page", String.valueOf(clampInt(String.valueOf(itemsPerPage), 20, 5, 200)));
    }

    public void saveSecuritySettings(int passwordMinLength, int sessionTimeoutMinutes) throws SQLException {
        settingsDao.setSetting("password_min_length", String.valueOf(clampInt(String.valueOf(passwordMinLength), 6, 4, 20)));
        settingsDao.setSetting("session_timeout", String.valueOf(clampInt(String.valueOf(sessionTimeoutMinutes), 60, 5, 1440)));
    }

    public void saveInventorySettings(int expiryAlertDays7, int expiryAlertDays1, BigDecimal lowStockThreshold)
            throws SQLException {
        settingsDao.setSetting("expiry_alert_days_7", String.valueOf(clampInt(String.valueOf(expiryAlertDays7), 7, 1, 30)));
        settingsDao.setSetting("expiry_alert_days_1", String.valueOf(clampInt(String.valueOf(expiryAlertDays1), 1, 1, 30)));
        BigDecimal threshold = lowStockThreshold == null ? BigDecimal.ZERO : lowStockThreshold;
        if (threshold.compareTo(BigDecimal.ZERO) < 0) {
            threshold = BigDecimal.ZERO;
        }
        settingsDao.setSetting("low_stock_threshold", threshold.stripTrailingZeros().toPlainString());
    }

    public void saveOrderSettings(String defaultOrderStatus, String defaultPaymentMethod, boolean allowCancelCompleted)
            throws SQLException {
        settingsDao.setSetting("default_order_status",
                VALID_ORDER_STATUSES.contains(defaultOrderStatus) ? defaultOrderStatus : AppConstants.ORDER_STATUS_PENDING);
        settingsDao.setSetting("default_payment_method",
                VALID_PAYMENT_METHODS.contains(defaultPaymentMethod) ? defaultPaymentMethod : AppConstants.PAYMENT_CASH);
        settingsDao.setSetting("allow_cancel_completed_order", allowCancelCompleted ? "1" : "0");
    }

    static String normalizeLogoInitials(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String s = raw.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
        if (s.length() > LOGO_INITIALS_MAX) {
            return s.substring(0, LOGO_INITIALS_MAX);
        }
        return s;
    }

    static String deriveInitialsFromShopName(String name) {
        if (name == null || name.isBlank()) {
            return "Q";
        }
        String[] parts = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) {
                continue;
            }
            int cp = p.codePointAt(0);
            sb.appendCodePoint(Character.toUpperCase(cp));
            if (sb.length() >= LOGO_INITIALS_MAX) {
                break;
            }
        }
        return sb.length() > 0 ? sb.toString() : "Q";
    }

    private static String normalizeOptional(String value) {
        return value == null ? "" : value.trim();
    }

    private static int clampInt(String raw, int fallback, int min, int max) {
        try {
            int value = raw == null || raw.isBlank() ? fallback : Integer.parseInt(raw.trim());
            return Math.max(min, Math.min(max, value));
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private static BigDecimal parseDecimal(String raw, BigDecimal fallback) {
        try {
            if (raw == null || raw.isBlank()) {
                return fallback;
            }
            return new BigDecimal(raw.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}
