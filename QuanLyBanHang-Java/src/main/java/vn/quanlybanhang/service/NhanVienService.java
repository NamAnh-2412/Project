package vn.quanlybanhang.service;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import vn.quanlybanhang.constants.AppConstants;
import vn.quanlybanhang.dao.AuditDao;
import vn.quanlybanhang.dao.NhanVienDao;
import vn.quanlybanhang.model.NhanVien;
import vn.quanlybanhang.util.ValidationUtil;

public class NhanVienService {

    private static final DateTimeFormatter CODE_FORMATTER = DateTimeFormatter.ofPattern("ddMMyy");

    private final NhanVienDao nhanVienDao = new NhanVienDao();
    private final AuditDao auditDao = new AuditDao();
    private final PasswordService passwordService = new PasswordService();
    private final SettingsService settingsService = new SettingsService();
    private final Random random = new Random();

    public record RoleChangeResult(boolean success, String message, String role, Integer id) {
        public static RoleChangeResult ok(int id, String role) {
            return new RoleChangeResult(true, "Cập nhật vai trò thành công.", role, id);
        }

        public static RoleChangeResult fail(String message) {
            return new RoleChangeResult(false, message, null, null);
        }
    }

    public List<NhanVien> listAll() throws SQLException {
        return nhanVienDao.findAll();
    }

    public Optional<NhanVien> findById(int id) throws SQLException {
        return nhanVienDao.findById(id);
    }

    public NhanVien bindEmployee(String maNhanVien, String tenDangNhap, String tenNhanVien, String chucVu,
            String soDienThoai, String email, String trangThai, String ngaySinh, String ngayVaoLam,
            boolean creating) {
        NhanVien employee = new NhanVien();
        employee.setMaNhanVien(trimOrNull(maNhanVien));
        employee.setTenDangNhap(trimOrNull(tenDangNhap));
        employee.setTenNhanVien(trimOrNull(tenNhanVien));
        employee.setChucVu(chucVu == null || chucVu.isBlank() ? AppConstants.ROLE_STAFF : chucVu.trim());
        employee.setSoDienThoai(trimOrNull(soDienThoai));
        employee.setEmail(trimOrNull(email));
        employee.setTrangThai(trangThai == null || trangThai.isBlank() ? "dang_lam" : trangThai.trim());
        employee.setNgaySinh(parseDate(ngaySinh));
        employee.setNgayVaoLam(parseDate(ngayVaoLam));
        if (creating && (employee.getTenDangNhap() == null || employee.getTenDangNhap().isBlank())) {
            employee.setTenDangNhap(employee.getMaNhanVien());
        }
        return employee;
    }

    public Optional<String> createEmployee(NhanVien employee, String password, String confirmPassword)
            throws SQLException {
        String error = validateCreate(employee, password, confirmPassword);
        if (error != null) {
            return Optional.of(error);
        }

        employee.setMaNhanVien(generateEmployeeCode(employee));
        if (employee.getTenDangNhap() == null || employee.getTenDangNhap().isBlank()) {
            employee.setTenDangNhap(employee.getMaNhanVien().toLowerCase());
        }
        employee.setTenDangNhap(employee.getTenDangNhap().trim());
        employee.setMatKhau(passwordService.hash(password));
        nhanVienDao.insert(employee);
        return Optional.empty();
    }

    public Optional<String> updateEmployee(NhanVien employee) throws SQLException {
        String error = validateUpdate(employee);
        if (error != null) {
            return Optional.of(error);
        }
        nhanVienDao.update(employee);
        return Optional.empty();
    }

    public Optional<String> deleteEmployee(int id, Integer currentUserId) throws SQLException {
        if (currentUserId != null && currentUserId == id) {
            return Optional.of("Không thể xóa tài khoản đang đăng nhập.");
        }
        nhanVienDao.delete(id);
        return Optional.empty();
    }

    public RoleChangeResult changeRole(int id, Integer currentUserId, Integer actorId) throws SQLException {
        if (currentUserId != null && currentUserId == id) {
            return RoleChangeResult.fail("Không thể đổi vai trò của chính mình.");
        }

        Optional<NhanVien> employee = nhanVienDao.findById(id);
        if (employee.isEmpty()) {
            return RoleChangeResult.fail("Không tìm thấy nhân viên.");
        }

        String currentRole = employee.get().getChucVu();
        String nextRole = AppConstants.ROLE_MANAGER.equals(currentRole)
                ? AppConstants.ROLE_STAFF
                : AppConstants.ROLE_MANAGER;
        nhanVienDao.updateRole(id, nextRole);
        try {
            auditDao.logRoleChange(null, id, actorId, currentRole, nextRole);
        } catch (SQLException ex) {
            // Không chặn cập nhật vai trò nếu bảng audit chưa có
        }
        return RoleChangeResult.ok(id, nextRole);
    }

    public int getPasswordMinLength() {
        try {
            return settingsService.getPasswordMinLength();
        } catch (SQLException ex) {
            return 6;
        }
    }

    private String validateCreate(NhanVien employee, String password, String confirmPassword) throws SQLException {
        if (employee.getTenNhanVien() == null || employee.getTenNhanVien().isBlank()) {
            return "Vui lòng nhập tên nhân viên.";
        }
        if (employee.getNgayVaoLam() == null) {
            return "Vui lòng chọn ngày vào làm.";
        }
        if (employee.getTenDangNhap() == null || employee.getTenDangNhap().isBlank()) {
            return "Vui lòng nhập tên đăng nhập.";
        }
        if (nhanVienDao.existsUsername(employee.getTenDangNhap().trim(), null)) {
            return "Tên đăng nhập đã tồn tại.";
        }
        if (employee.getSoDienThoai() != null && !employee.getSoDienThoai().isBlank()
                && !ValidationUtil.isValidPhone(employee.getSoDienThoai())) {
            return "Số điện thoại không hợp lệ.";
        }
        if (employee.getEmail() != null && !employee.getEmail().isBlank()
                && !ValidationUtil.isValidEmail(employee.getEmail())) {
            return "Email không hợp lệ.";
        }
        if (employee.getEmail() != null && !employee.getEmail().isBlank()
                && nhanVienDao.existsEmail(employee.getEmail().trim(), null)) {
            return "Email đã tồn tại.";
        }
        int minLength = getPasswordMinLength();
        if (password == null || password.length() < minLength) {
            return "Mật khẩu khởi tạo phải có ít nhất " + minLength + " ký tự.";
        }
        if (!password.equals(confirmPassword)) {
            return "Nhập lại mật khẩu không khớp.";
        }
        if (nhanVienDao.existsSameNgayVaoLam(Date.valueOf(employee.getNgayVaoLam())) && employee.getNgaySinh() == null) {
            return "Đã có nhân viên vào cùng ngày này. Vui lòng nhập ngày sinh để sinh mã.";
        }
        return null;
    }

    private String validateUpdate(NhanVien employee) throws SQLException {
        if (employee.getMaNhanVien() == null || employee.getMaNhanVien().isBlank()) {
            return "Vui lòng nhập mã nhân viên.";
        }
        if (employee.getTenNhanVien() == null || employee.getTenNhanVien().isBlank()) {
            return "Vui lòng nhập tên nhân viên.";
        }
        if (employee.getSoDienThoai() != null && !employee.getSoDienThoai().isBlank()
                && !ValidationUtil.isValidPhone(employee.getSoDienThoai())) {
            return "Số điện thoại không hợp lệ.";
        }
        if (employee.getEmail() != null && !employee.getEmail().isBlank()
                && !ValidationUtil.isValidEmail(employee.getEmail())) {
            return "Email không hợp lệ.";
        }
        if (employee.getTenDangNhap() == null || employee.getTenDangNhap().isBlank()) {
            return "Vui lòng nhập tên đăng nhập.";
        }
        if (nhanVienDao.existsUsername(employee.getTenDangNhap().trim(), employee.getId())) {
            return "Tên đăng nhập đã tồn tại.";
        }
        if (employee.getEmail() != null && !employee.getEmail().isBlank()
                && nhanVienDao.existsEmail(employee.getEmail().trim(), employee.getId())) {
            return "Email đã tồn tại.";
        }
        if (nhanVienDao.existsMaNhanVien(employee.getMaNhanVien(), employee.getId())) {
            return "Mã nhân viên đã tồn tại.";
        }
        return null;
    }

    private String generateEmployeeCode(NhanVien employee) throws SQLException {
        LocalDate dateForCode = employee.getNgayVaoLam();
        if (nhanVienDao.existsSameNgayVaoLam(Date.valueOf(employee.getNgayVaoLam())) && employee.getNgaySinh() != null) {
            dateForCode = employee.getNgaySinh();
        }

        String baseCode = "NV" + dateForCode.format(CODE_FORMATTER);
        if (!nhanVienDao.existsMaNhanVien(baseCode, null)) {
            return baseCode;
        }

        String code;
        do {
            code = "NV" + String.format("%06d", random.nextInt(1_000_000));
        } while (nhanVienDao.existsMaNhanVien(code, null));
        return code;
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value);
    }

    private static String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
