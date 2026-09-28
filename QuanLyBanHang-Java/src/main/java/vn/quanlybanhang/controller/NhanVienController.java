package vn.quanlybanhang.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import vn.quanlybanhang.model.NhanVien;
import vn.quanlybanhang.service.NhanVienService;
import vn.quanlybanhang.service.NhanVienService.RoleChangeResult;
import vn.quanlybanhang.util.JsonUtil;
import vn.quanlybanhang.util.RequestUtil;
import vn.quanlybanhang.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {
        "/nhan-vien",
        "/nhan-vien/create",
        "/nhan-vien/edit",
        "/nhan-vien/delete",
        "/nhan-vien/change-role"
})
public class NhanVienController extends BaseController {

    private static final long serialVersionUID = 1L;

    private final NhanVienService nhanVienService = new NhanVienService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        try {
            switch (path) {
                case "/nhan-vien" -> showList(request, response);
                case "/nhan-vien/create" -> showCreateForm(request, response, new NhanVien(), null);
                case "/nhan-vien/edit" -> showEditForm(request, response);
                case "/nhan-vien/delete" -> showDeleteForm(request, response);
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
                case "/nhan-vien/create" -> createEmployee(request, response);
                case "/nhan-vien/edit" -> updateEmployee(request, response);
                case "/nhan-vien/delete" -> deleteEmployee(request, response);
                case "/nhan-vien/change-role" -> changeRole(request, response);
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException ex) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    private void showList(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        request.setAttribute("pageTitle", "Nhân viên");
        applyPageChrome(request, "fas fa-user-tie", "Quản lý tài khoản và quyền truy cập.", null);
        request.setAttribute("employees", nhanVienService.listAll());
        render(request, response, "/WEB-INF/views/nhanvien/index.jsp");
    }

    private void showCreateForm(HttpServletRequest request, HttpServletResponse response, NhanVien employee,
            String error) throws ServletException, IOException {
        request.setAttribute("pageTitle", "Thêm nhân viên");
        applyPageChrome(request, "fas fa-user-plus", "Tạo tài khoản nhân viên mới.", null);
        request.setAttribute("employee", employee);
        request.setAttribute("error", error);
        render(request, response, "/WEB-INF/views/nhanvien/form.jsp");
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        Optional<NhanVien> employee = nhanVienService.findById(getIntParameter(request, "id", 0));
        if (employee.isEmpty()) {
            setFlash(request, "error", "Không tìm thấy nhân viên.");
            redirect(request, response, "/nhan-vien");
            return;
        }
        request.setAttribute("pageTitle", "Sửa nhân viên");
        applyPageChrome(request, "fas fa-user-edit", "Cập nhật thông tin nhân viên.", null);
        request.setAttribute("employee", employee.get());
        request.setAttribute("error", null);
        render(request, response, "/WEB-INF/views/nhanvien/form.jsp");
    }

    private void showDeleteForm(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        Optional<NhanVien> employee = nhanVienService.findById(getIntParameter(request, "id", 0));
        if (employee.isEmpty()) {
            setFlash(request, "error", "Không tìm thấy nhân viên.");
            redirect(request, response, "/nhan-vien");
            return;
        }
        request.setAttribute("pageTitle", "Xóa nhân viên");
        applyPageChrome(request, "fas fa-user-times", "Xác nhận trước khi xóa nhân viên.", null);
        request.setAttribute("employee", employee.get());
        render(request, response, "/WEB-INF/views/nhanvien/delete.jsp");
    }

    private void createEmployee(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        NhanVien employee = bindEmployee(request, true);
        Optional<String> error = nhanVienService.createEmployee(
                employee,
                request.getParameter("mat_khau"),
                request.getParameter("xac_nhan_mat_khau"));
        if (error.isPresent()) {
            showCreateForm(request, response, employee, error.get());
            return;
        }
        setFlash(request, "success", "Thêm nhân viên thành công.");
        redirect(request, response, "/nhan-vien");
    }

    private void updateEmployee(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        NhanVien employee = bindEmployee(request, false);
        employee.setId(getIntParameter(request, "id", 0));
        Optional<String> error = nhanVienService.updateEmployee(employee);
        if (error.isPresent()) {
            request.setAttribute("pageTitle", "Sửa nhân viên");
            applyPageChrome(request, "fas fa-user-edit", "Cập nhật thông tin nhân viên.", null);
            request.setAttribute("employee", employee);
            request.setAttribute("error", error.get());
            render(request, response, "/WEB-INF/views/nhanvien/form.jsp");
            return;
        }
        setFlash(request, "success", "Cập nhật nhân viên thành công.");
        redirect(request, response, "/nhan-vien");
    }

    private void deleteEmployee(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        Optional<String> error = nhanVienService.deleteEmployee(
                getIntParameter(request, "id", 0),
                SessionUtil.getCurrentUserId(request));
        if (error.isPresent()) {
            setFlash(request, "error", error.get());
            redirect(request, response, "/nhan-vien");
            return;
        }
        setFlash(request, "success", "Đã xóa nhân viên.");
        redirect(request, response, "/nhan-vien");
    }

    private void changeRole(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        int id = getIntParameter(request, "id", 0);
        if (id <= 0 && wantsJson(request)) {
            id = readJsonId(request);
        }

        RoleChangeResult result = nhanVienService.changeRole(
                id,
                SessionUtil.getCurrentUserId(request),
                SessionUtil.getCurrentUserId(request));

        if (wantsJson(request)) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("success", result.success());
            payload.put("message", result.message());
            if (result.success()) {
                payload.put("role", result.role());
                payload.put("new_role", result.role());
                payload.put("id", result.id());
            }
            JsonUtil.writeJson(response, payload);
            return;
        }

        if (result.success()) {
            setFlash(request, "success", result.message());
        } else {
            setFlash(request, "error", result.message());
        }
        redirect(request, response, "/nhan-vien");
    }

    private NhanVien bindEmployee(HttpServletRequest request, boolean creating) {
        return nhanVienService.bindEmployee(
                request.getParameter("ma_nhanvien"),
                request.getParameter("ten_dang_nhap"),
                request.getParameter("ten_nhanvien"),
                request.getParameter("chuc_vu"),
                request.getParameter("so_dien_thoai"),
                request.getParameter("email"),
                request.getParameter("trang_thai"),
                request.getParameter("ngay_sinh"),
                request.getParameter("ngay_vao_lam"),
                creating);
    }

    private boolean wantsJson(HttpServletRequest request) {
        return RequestUtil.wantsJson(request);
    }

    private int readJsonId(HttpServletRequest request) {
        try {
            String body = request.getReader().lines().reduce("", (a, b) -> a + b);
            if (body == null || body.isBlank()) {
                return 0;
            }
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(body);
            if (node.has("id")) {
                return node.get("id").asInt(0);
            }
        } catch (Exception ignored) {
            // Fall back to form parameter.
        }
        return 0;
    }
}
