package vn.quanlybanhang.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import vn.quanlybanhang.model.KhachHang;
import vn.quanlybanhang.service.CustomerService;
import vn.quanlybanhang.service.CustomerService.CustomerDetailView;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {
        "/khach-hang",
        "/khach-hang/create",
        "/khach-hang/edit",
        "/khach-hang/delete",
        "/khach-hang/view"
})
public class KhachHangController extends BaseController {

    private static final long serialVersionUID = 1L;

    private final CustomerService customerService = new CustomerService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        try {
            switch (path) {
                case "/khach-hang" -> showList(request, response);
                case "/khach-hang/create" -> showForm(request, response, new KhachHang(), null, true);
                case "/khach-hang/edit" -> showEditForm(request, response);
                case "/khach-hang/delete" -> showDeleteForm(request, response);
                case "/khach-hang/view" -> showDetail(request, response);
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException ex) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        try {
            switch (path) {
                case "/khach-hang/create" -> create(request, response);
                case "/khach-hang/edit" -> update(request, response);
                case "/khach-hang/delete" -> delete(request, response);
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException ex) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    private void showList(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        request.setAttribute("pageTitle", "Khách hàng");
        request.setAttribute("pageHeaderTitle", "Quản lý khách hàng");
        applyPageChrome(request, "fas fa-users", "Quản lý khách hàng trên database hiện tại.", null);
        request.setAttribute("customers", customerService.listAll());
        render(request, response, "/WEB-INF/views/khachhang/index.jsp");
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        var customer = customerService.findById(getIntParameter(request, "id", 0));
        if (customer.isEmpty()) {
            flashAndRedirect(request, response, "error", "Không tìm thấy khách hàng.");
            return;
        }
        showForm(request, response, customer.get(), null, false);
    }

    private void showDeleteForm(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        var customer = customerService.findById(getIntParameter(request, "id", 0));
        if (customer.isEmpty()) {
            flashAndRedirect(request, response, "error", "Không tìm thấy khách hàng.");
            return;
        }
        request.setAttribute("pageTitle", "Xóa khách hàng");
        request.setAttribute("pageHeaderTitle", "Xóa khách hàng");
        applyPageChrome(request, "fas fa-user-times", "Xác nhận trước khi xóa khách hàng.", null);
        request.setAttribute("customer", customer.get());
        render(request, response, "/WEB-INF/views/khachhang/delete.jsp");
    }

    private void showDetail(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        var detail = customerService.getCustomerDetail(getIntParameter(request, "id", 0));
        if (detail.isEmpty()) {
            flashAndRedirect(request, response, "error", "Không tìm thấy khách hàng.");
            return;
        }

        CustomerDetailView view = detail.get();
        request.setAttribute("pageTitle", "Chi tiết khách hàng");
        request.setAttribute("pageHeaderTitle", "Chi tiết khách hàng");
        applyPageChrome(request, "fas fa-user", "Thông tin khách hàng, thống kê chi tiêu và lịch sử đơn hàng.",
                List.of("orders.css"));
        request.setAttribute("customer", view.customer());
        request.setAttribute("stats", view.stats());
        request.setAttribute("orders", view.orders());
        render(request, response, "/WEB-INF/views/khachhang/view.jsp");
    }

    private void create(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        KhachHang customer = bindCustomer(request);
        var error = customerService.createCustomer(customer);
        if (error.isPresent()) {
            showForm(request, response, customer, error.get(), true);
            return;
        }
        flashAndRedirect(request, response, "success", "Thêm khách hàng thành công.");
    }

    private void update(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        KhachHang customer = bindCustomer(request);
        customer.setId(getIntParameter(request, "id", 0));
        var error = customerService.updateCustomer(customer);
        if (error.isPresent()) {
            showForm(request, response, customer, error.get(), false);
            return;
        }
        flashAndRedirect(request, response, "success", "Cập nhật khách hàng thành công.");
    }

    private void delete(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        customerService.deleteCustomer(getIntParameter(request, "id", 0));
        flashAndRedirect(request, response, "success", "Đã xóa khách hàng.");
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response, KhachHang customer, String error,
                          boolean creating) throws ServletException, IOException {
        request.setAttribute("pageTitle", creating ? "Thêm khách hàng" : "Sửa khách hàng");
        request.setAttribute("pageHeaderTitle", creating ? "Thêm khách hàng" : "Sửa khách hàng");
        applyPageChrome(request, creating ? "fas fa-user-plus" : "fas fa-user-edit",
                creating ? "Nhập thông tin khách hàng mới." : "Cập nhật thông tin khách hàng.", null);
        request.setAttribute("customer", customer);
        request.setAttribute("error", error);
        render(request, response, "/WEB-INF/views/khachhang/form.jsp");
    }

    private KhachHang bindCustomer(HttpServletRequest request) {
        return customerService.bindCustomer(
                paramOrEmpty(request, "ten_khachhang"),
                paramOrEmpty(request, "so_dien_thoai"),
                paramOrEmpty(request, "email"),
                paramOrEmpty(request, "dia_chi"),
                paramOrEmpty(request, "ngay_sinh"),
                paramOrEmpty(request, "ghi_chu"));
    }

    private void flashAndRedirect(HttpServletRequest request, HttpServletResponse response, String type, String message)
            throws IOException {
        setFlash(request, type, message);
        redirect(request, response, "/khach-hang");
    }
}
