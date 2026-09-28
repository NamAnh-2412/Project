package vn.quanlybanhang.constants;

public final class AppConstants {

    public static final String SESSION_USER_ID = "nhanvien_id";
    public static final String SESSION_USERNAME = "ten_dang_nhap";
    public static final String SESSION_ROLE = "chuc_vu";

    public static final String ROLE_STAFF = "nhan_vien";
    public static final String ROLE_MANAGER = "cua_hang_truong";
    public static final String ROLE_ADMIN = "cua_hang_truong";

    public static final String ORDER_STATUS_PENDING = "cho_xac_nhan";
    public static final String ORDER_STATUS_CONFIRMED = "da_xac_nhan";
    public static final String ORDER_STATUS_PREPARING = "dang_chuan_bi";
    public static final String ORDER_STATUS_READY = "san_sang";
    public static final String ORDER_STATUS_COMPLETED = "hoan_thanh";
    public static final String ORDER_STATUS_CANCELLED = "da_huy";

    public static final String PAYMENT_CASH = "tien_mat";
    public static final String PAYMENT_BANK = "chuyen_khoan";
    public static final String PAYMENT_CARD = "the";
    public static final String PAYMENT_APP_ONLINE = "app_online";

    private AppConstants() {
    }
}
