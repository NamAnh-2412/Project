package vn.quanlybanhang.service;

import java.sql.SQLException;
import java.util.Optional;
import java.util.Random;

import vn.quanlybanhang.constants.AppConstants;
import vn.quanlybanhang.dao.NhanVienDao;
import vn.quanlybanhang.model.NhanVien;
import vn.quanlybanhang.util.ValidationUtil;

public class AuthService {

    private final NhanVienDao nhanVienDao = new NhanVienDao();
    private final PasswordService passwordService = new PasswordService();
    private final SettingsService settingsService = new SettingsService();
    private final Random random = new Random();

    public Optional<NhanVien> login(String username, String password, String remoteIp) throws SQLException {
        Optional<NhanVien> employeeOpt = nhanVienDao.findByUsername(username);
        NhanVien employee = employeeOpt.orElse(null);
        String status = "that_bai";

        if (employee != null
                && "dang_lam".equalsIgnoreCase(employee.getTrangThai())
                && passwordService.verify(password, employee.getMatKhau())) {
            status = "thanh_cong";
            tryInsertLoginHistory(employee.getId(), username, remoteIp, status);
            return employeeOpt;
        }

        tryInsertLoginHistory(employee != null ? employee.getId() : null, username, remoteIp, status);
        return Optional.empty();
    }

    public boolean isManager(String role) {
        return AppConstants.ROLE_MANAGER.equals(role);
    }

    public boolean isAdmin(String role) {
        return AppConstants.ROLE_ADMIN.equals(role);
    }

    public int getPasswordMinLength() {
        try {
            return settingsService.getPasswordMinLength();
        } catch (SQLException ex) {
            return 6;
        }
    }

    public String validateRegistration(String fullName, String username, String email, String phone,
                                       String password, String confirmPassword) {
        if (isBlank(fullName) || isBlank(username) || isBlank(email) || isBlank(phone)
                || isBlank(password) || isBlank(confirmPassword)) {
            return "Vui lòng nhập đầy đủ thông tin.";
        }
        if (!ValidationUtil.isValidEmail(email)) {
            return "Email không hợp lệ.";
        }
        if (!ValidationUtil.isValidPhone(phone)) {
            return "Số điện thoại không hợp lệ.";
        }
        int minLength = getPasswordMinLength();
        if (password.length() < minLength) {
            return "Mật khẩu phải có ít nhất " + minLength + " ký tự.";
        }
        if (!password.equals(confirmPassword)) {
            return "Nhập lại mật khẩu không khớp.";
        }
        return null;
    }

    public Optional<String> registerEmployee(String fullName, String username, String email, String phone,
                                             String password, String confirmPassword) throws SQLException {
        String validationError = validateRegistration(fullName, username, email, phone, password, confirmPassword);
        if (validationError != null) {
            return Optional.of(validationError);
        }
        if (nhanVienDao.existsUsername(username, null) || nhanVienDao.existsEmail(email, null)) {
            return Optional.of("Tên đăng nhập hoặc email đã tồn tại.");
        }

        register(fullName, username, email, phone, password);
        return Optional.empty();
    }

    public String validatePasswordChange(String currentPassword, String newPassword, String confirmPassword) {
        if (isBlank(currentPassword) || isBlank(newPassword) || isBlank(confirmPassword)) {
            return "Vui lòng nhập đầy đủ thông tin.";
        }
        int minLength = getPasswordMinLength();
        if (newPassword.length() < minLength) {
            return "Mật khẩu mới phải có ít nhất " + minLength + " ký tự.";
        }
        if (!newPassword.equals(confirmPassword)) {
            return "Nhập lại mật khẩu mới không khớp.";
        }
        return null;
    }

    public Optional<String> changePasswordForUser(int employeeId, String currentPassword, String newPassword)
            throws SQLException {
        Optional<NhanVien> employeeOpt = nhanVienDao.findById(employeeId);
        if (employeeOpt.isEmpty() || !verifyCurrentPassword(employeeOpt.get(), currentPassword)) {
            return Optional.of("Mật khẩu hiện tại không đúng.");
        }
        changePassword(employeeId, newPassword);
        return Optional.empty();
    }

    public String register(String fullName, String username, String email, String phone, String password) throws SQLException {
        NhanVien employee = new NhanVien();
        employee.setMaNhanVien(generateEmployeeCode());
        employee.setTenNhanVien(fullName);
        employee.setTenDangNhap(username);
        employee.setEmail(email);
        employee.setSoDienThoai(phone);
        employee.setChucVu(AppConstants.ROLE_STAFF);
        employee.setTrangThai("dang_lam");
        employee.setMatKhau(passwordService.hash(password));
        nhanVienDao.insert(employee);
        return employee.getMaNhanVien();
    }

    public boolean verifyCurrentPassword(NhanVien employee, String rawPassword) {
        return employee != null && passwordService.verify(rawPassword, employee.getMatKhau());
    }

    public void changePassword(int employeeId, String newPassword) throws SQLException {
        nhanVienDao.updatePassword(employeeId, passwordService.hash(newPassword));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void tryInsertLoginHistory(Integer nhanVienId, String username, String remoteIp, String status) {
        try {
            nhanVienDao.insertLoginHistory(nhanVienId, username, remoteIp, status);
        } catch (SQLException ignored) {
            // Logging history should not break login flow.
        }
    }

    private String generateEmployeeCode() throws SQLException {
        String code;
        do {
            code = "NV" + String.format("%05d", random.nextInt(100_000));
        } while (nhanVienDao.existsMaNhanVien(code, null));
        return code;
    }
}
