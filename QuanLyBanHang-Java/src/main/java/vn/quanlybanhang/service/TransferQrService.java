package vn.quanlybanhang.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Map;

public class TransferQrService {

    private final SettingsService settingsService = new SettingsService();

    public TransferQr forAmount(String orderCode, BigDecimal amount) throws SQLException {
        Map<String, String> settings = settingsService.getSettingsWithDefaults();
        String bankName = blankToEmpty(settings.get("bank_name"));
        String accountName = blankToEmpty(settings.get("bank_account_name"));
        String accountNumber = blankToEmpty(settings.get("bank_account_number")).replaceAll("\\s+", "");
        String bin = blankToEmpty(settings.get("bank_bin"));
        BigDecimal payable = amount == null ? BigDecimal.ZERO : amount.setScale(0, RoundingMode.HALF_UP);
        String content = contentOf(orderCode);
        if (bin.isBlank() || accountNumber.isBlank()) {
            return new TransferQr(bankName, accountName, accountNumber, content, payable, "", false,
                    "Vào Cài đặt, mục thông tin cửa hàng, và nhập ngân hàng cùng số tài khoản để hiện QR.");
        }
        String imageUrl = "https://img.vietqr.io/image/" + bin + "-" + accountNumber + "-compact2.png"
                + "?amount=" + payable.toPlainString()
                + "&addInfo=" + URLEncoder.encode(content, StandardCharsets.UTF_8)
                + "&accountName=" + URLEncoder.encode(accountName, StandardCharsets.UTF_8);
        return new TransferQr(bankName, accountName, accountNumber, content, payable, imageUrl, true, "");
    }

    private static String contentOf(String orderCode) {
        String raw = orderCode == null ? "" : orderCode.replaceAll("\\s+", "").toUpperCase();
        if (raw.isBlank()) {
            return "THANHTOAN";
        }
        return raw.length() > 25 ? raw.substring(0, 25) : raw;
    }

    private static String blankToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
