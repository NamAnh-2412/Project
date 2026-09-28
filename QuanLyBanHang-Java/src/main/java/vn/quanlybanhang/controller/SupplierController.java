package vn.quanlybanhang.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import vn.quanlybanhang.model.Supplier;
import vn.quanlybanhang.service.SupplierService;
import vn.quanlybanhang.service.SupplierService.SupplierDetailView;
import vn.quanlybanhang.util.RequestUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {
        "/nha-cung-cap",
        "/nha-cung-cap/create",
        "/nha-cung-cap/edit",
        "/nha-cung-cap/delete",
        "/nha-cung-cap/view"
})
public class SupplierController extends BaseController {

    private static final long serialVersionUID = 1L;

    private final SupplierService supplierService = new SupplierService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        try {
            switch (path) {
                case "/nha-cung-cap" -> showList(request, response);
                case "/nha-cung-cap/create" -> showForm(request, response, new Supplier(), null, true);
                case "/nha-cung-cap/edit" -> showEditForm(request, response);
                case "/nha-cung-cap/delete" -> showDeleteForm(request, response);
                case "/nha-cung-cap/view" -> showDetail(request, response);
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
                case "/nha-cung-cap/create" -> create(request, response);
                case "/nha-cung-cap/edit" -> update(request, response);
                case "/nha-cung-cap/delete" -> delete(request, response);
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException ex) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    private void showList(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        String keyword = RequestUtil.getTrimmed(request, "kw");
        String status = RequestUtil.getTrimmed(request, "status");

        request.setAttribute("pageTitle", "Nhà cung cấp");
        request.setAttribute("pageHeaderTitle", "Quản lý nhà cung cấp");
        applyPageChrome(request, "fas fa-truck", "Lọc theo từ khóa và trạng thái hợp tác.", null);
        request.setAttribute("keyword", keyword);
        request.setAttribute("status", status);
        request.setAttribute("suppliers", supplierService.search(keyword, status));
        render(request, response, "/WEB-INF/views/nhacungcap/index.jsp");
    }

    private void showDetail(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        int id = getIntParameter(request, "id", 0);
        var detail = supplierService.getSupplierDetail(id);
        if (detail.isEmpty()) {
            flashAndRedirect(request, response, "error", "Không tìm thấy nhà cung cấp.");
            return;
        }

        SupplierDetailView view = detail.get();
        Supplier supplier = view.supplier();
        request.setAttribute("pageTitle", "Chi tiết nhà cung cấp");
        request.setAttribute("pageHeaderTitle", "Chi tiết nhà cung cấp");
        applyPageChrome(request, "fas fa-truck-loading",
                supplier.getTenNhaCungCap() + " — Thống kê, nguyên liệu liên kết và lịch sử phiếu nhập.", null);
        request.setAttribute("supplier", supplier);
        request.setAttribute("materials", view.materials());
        request.setAttribute("importHistory", view.importHistory());
        request.setAttribute("stats", view.stats());
        render(request, response, "/WEB-INF/views/nhacungcap/view.jsp");
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        var supplier = supplierService.findById(getIntParameter(request, "id", 0));
        if (supplier.isEmpty()) {
            flashAndRedirect(request, response, "error", "Không tìm thấy nhà cung cấp.");
            return;
        }
        showForm(request, response, supplier.get(), null, false);
    }

    private void showDeleteForm(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        var supplier = supplierService.findById(getIntParameter(request, "id", 0));
        if (supplier.isEmpty()) {
            flashAndRedirect(request, response, "error", "Không tìm thấy nhà cung cấp.");
            return;
        }
        request.setAttribute("pageTitle", "Xóa nhà cung cấp");
        request.setAttribute("pageHeaderTitle", "Xóa nhà cung cấp");
        applyPageChrome(request, "fas fa-truck", "Xác nhận trước khi xóa nhà cung cấp.", null);
        request.setAttribute("supplier", supplier.get());
        render(request, response, "/WEB-INF/views/nhacungcap/delete.jsp");
    }

    private void create(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        Supplier supplier = bindSupplier(request);
        var error = supplierService.createSupplier(supplier);
        if (error.isPresent()) {
            showForm(request, response, supplier, error.get(), true);
            return;
        }
        flashAndRedirect(request, response, "success", "Thêm nhà cung cấp thành công.");
    }

    private void update(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        Supplier supplier = bindSupplier(request);
        supplier.setId(getIntParameter(request, "id", 0));
        var error = supplierService.updateSupplier(supplier);
        if (error.isPresent()) {
            showForm(request, response, supplier, error.get(), false);
            return;
        }
        flashAndRedirect(request, response, "success", "Cập nhật nhà cung cấp thành công.");
    }

    private void delete(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        String message = supplierService.deleteSupplier(getIntParameter(request, "id", 0));
        flashAndRedirect(request, response, "success", message);
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response, Supplier supplier, String error,
                          boolean creating) throws ServletException, IOException {
        request.setAttribute("pageTitle", creating ? "Thêm nhà cung cấp" : "Sửa nhà cung cấp");
        request.setAttribute("pageHeaderTitle", creating ? "Thêm nhà cung cấp" : "Sửa nhà cung cấp");
        applyPageChrome(request, creating ? "fas fa-plus-circle" : "fas fa-edit",
                creating ? "Nhập thông tin đối tác cung cấp." : "Cập nhật thông tin nhà cung cấp.",
                List.of("products.css"));
        request.setAttribute("supplier", supplier);
        request.setAttribute("error", error);
        render(request, response, "/WEB-INF/views/nhacungcap/form.jsp");
    }

    private Supplier bindSupplier(HttpServletRequest request) {
        return supplierService.bindSupplier(
                paramOrEmpty(request, "ten_nhacungcap"),
                paramOrEmpty(request, "so_dien_thoai"),
                paramOrEmpty(request, "email"),
                paramOrEmpty(request, "dia_chi"),
                paramOrEmpty(request, "ma_so_thue"),
                paramOrEmpty(request, "nguoi_lien_he"),
                paramOrEmpty(request, "trang_thai"),
                paramOrEmpty(request, "ghi_chu"));
    }

    private void flashAndRedirect(HttpServletRequest request, HttpServletResponse response, String type, String message)
            throws IOException {
        setFlash(request, type, message);
        redirect(request, response, "/nha-cung-cap");
    }
}
