package vn.quanlybanhang.util;

import java.util.regex.Pattern;

public final class ValidationUtil {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^(0|\\+84)[0-9]{9,10}$");

    private ValidationUtil() {
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return false;
        }
        String normalized = phone.replaceAll("[^0-9+]", "");
        return PHONE_PATTERN.matcher(normalized).matches();
    }

    public static boolean isValidEmail(String email) {
        return email != null && !email.isBlank() && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }
}
