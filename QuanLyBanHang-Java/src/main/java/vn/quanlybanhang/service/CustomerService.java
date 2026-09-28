package vn.quanlybanhang.service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import vn.quanlybanhang.dao.KhachHangDao;
import vn.quanlybanhang.dao.KhachHangDao.CustomerStats;
import vn.quanlybanhang.model.DonHang;
import vn.quanlybanhang.model.KhachHang;
import vn.quanlybanhang.util.ValidationUtil;

public class CustomerService {

    private final KhachHangDao khachHangDao = new KhachHangDao();

    public List<KhachHang> listAll() throws SQLException {
        return khachHangDao.findAll();
    }

    public Optional<KhachHang> findById(int id) throws SQLException {
        return khachHangDao.findById(id);
    }

    public Optional<CustomerDetailView> getCustomerDetail(int id) throws SQLException {
        Optional<KhachHang> customerOpt = khachHangDao.findById(id);
        if (customerOpt.isEmpty()) {
            return Optional.empty();
        }

        KhachHang customer = customerOpt.get();
        CustomerStats stats = khachHangDao.getCustomerStats(customer.getId());
        if (customer.getTongTienDaChi() != null && customer.getTongTienDaChi().signum() > 0) {
            stats.setTotalSpent(customer.getTongTienDaChi());
        }

        List<DonHang> orders = khachHangDao.findRecentOrdersByCustomerId(customer.getId(), 10);
        return Optional.of(new CustomerDetailView(customer, stats, orders));
    }

    public KhachHang bindCustomer(String fullName, String phone, String email, String address,
                                  String birthDateRaw, String note) {
        KhachHang customer = new KhachHang();
        customer.setTenKhachHang(trimOrNull(fullName));
        customer.setSoDienThoai(trimOrNull(phone));
        customer.setEmail(trimOrNull(email));
        customer.setDiaChi(trimOrNull(address));
        customer.setNgaySinh(parseDate(birthDateRaw));
        customer.setGhiChu(trimOrNull(note));
        return customer;
    }

    public String validate(KhachHang customer) {
        if (customer.getTenKhachHang() == null || customer.getTenKhachHang().isBlank()) {
            return "Vui lòng nhập tên khách hàng.";
        }
        if (customer.getSoDienThoai() != null && !customer.getSoDienThoai().isBlank()
                && !ValidationUtil.isValidPhone(customer.getSoDienThoai())) {
            return "Số điện thoại không hợp lệ.";
        }
        if (customer.getEmail() != null && !customer.getEmail().isBlank()
                && !ValidationUtil.isValidEmail(customer.getEmail())) {
            return "Email không hợp lệ.";
        }
        return null;
    }

    public Optional<String> createCustomer(KhachHang customer) throws SQLException {
        String error = validate(customer);
        if (error != null) {
            return Optional.of(error);
        }
        khachHangDao.insert(customer);
        return Optional.empty();
    }

    public Optional<String> updateCustomer(KhachHang customer) throws SQLException {
        String error = validate(customer);
        if (error != null) {
            return Optional.of(error);
        }
        khachHangDao.update(customer);
        return Optional.empty();
    }

    public void deleteCustomer(int id) throws SQLException {
        khachHangDao.delete(id);
    }

    public List<Map<String, Object>> searchCustomersForApi(String query, String contextPath) throws SQLException {
        List<KhachHang> customers = khachHangDao.searchCustomers(query);
        List<Map<String, Object>> payload = new ArrayList<>();
        for (KhachHang customer : customers) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", customer.getId());
            item.put("ten_khachhang", customer.getTenKhachHang());
            item.put("so_dien_thoai", customer.getSoDienThoai());
            item.put("email", customer.getEmail());
            item.put("dia_chi", customer.getDiaChi());
            item.put("ghi_chu", customer.getGhiChu());
            item.put("loai_thanh_vien", customer.getLoaiThanhVien());
            item.put("tong_tien_da_chi", customer.getTongTienDaChi());
            item.put("tong_donhang", customer.getTongDonHang());
            item.put("ngay_sinh", customer.getNgaySinh() == null ? null : customer.getNgaySinh().toString());
            item.put("ngay_tao", customer.getNgayTao() == null ? null : customer.getNgayTao().toString());
            item.put("viewUrl", contextPath + "/khach-hang/view?id=" + customer.getId());
            payload.add(item);
        }
        return payload;
    }

    private static LocalDate parseDate(String value) {
        try {
            return value == null || value.isBlank() ? null : LocalDate.parse(value.trim());
        } catch (Exception ex) {
            return null;
        }
    }

    private static String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public record CustomerDetailView(KhachHang customer, CustomerStats stats, List<DonHang> orders) {
    }
}
