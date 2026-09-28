package vn.quanlybanhang.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import vn.quanlybanhang.model.DanhMuc;
import vn.quanlybanhang.service.MenuCategoryService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/san-pham/thuc-don")
public class ThucDonController extends BaseController {

    private static final long serialVersionUID = 1L;

    private final MenuCategoryService menuCategoryService = new MenuCategoryService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            showPage(request, response, new DanhMuc(), null);
        } catch (SQLException ex) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        try {
            switch (action) {
                case "save" -> save(request, response);
                case "edit" -> edit(request, response);
                case "delete" -> delete(request, response);
                default -> redirect(request, response, "/san-pham/thuc-don");
            }
        } catch (SQLException ex) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    private void showPage(HttpServletRequest request, HttpServletResponse response, DanhMuc category, String error)
            throws SQLException, ServletException, IOException {
        request.setAttribute("pageTitle", "Thực đơn");
        applyPageChrome(request, "fas fa-book-open", "Quản lý danh mục hiện trong màn tạo đơn.", List.of("products.css"));
        request.setAttribute("category", category);
        request.setAttribute("error", error);
        request.setAttribute("categories", menuCategoryService.listAll());
        render(request, response, "/WEB-INF/views/sanpham/thucdon.jsp");
    }

    private void save(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        DanhMuc category = bindCategory(request);
        int id = getIntParameter(request, "id", 0);
        var error = menuCategoryService.saveCategory(category, id);
        if (error.isPresent()) {
            showPage(request, response, category, error.get());
            return;
        }

        setFlash(request, "success", id > 0 ? "Cập nhật thực đơn thành công." : "Thêm thực đơn thành công.");
        redirect(request, response, "/san-pham/thuc-don");
    }

    private void edit(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        var category = menuCategoryService.findById(getIntParameter(request, "id", 0));
        if (category.isEmpty()) {
            setFlash(request, "error", "Không tìm thấy thực đơn.");
            redirect(request, response, "/san-pham/thuc-don");
            return;
        }
        showPage(request, response, category.get(), null);
    }

    private void delete(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        var error = menuCategoryService.deleteCategory(getIntParameter(request, "id", 0));
        if (error.isPresent()) {
            setFlash(request, "error", error.get());
        } else {
            setFlash(request, "success", "Đã xóa thực đơn.");
        }
        redirect(request, response, "/san-pham/thuc-don");
    }

    private DanhMuc bindCategory(HttpServletRequest request) {
        return menuCategoryService.bindCategory(
                paramOrEmpty(request, "ten_danhmuc"),
                paramOrEmpty(request, "mota"),
                getNullableIntParameter(request, "danhmuc_cha"),
                getNullableIntParameter(request, "thu_tu"),
                request.getParameter("dang_hoat_dong"));
    }
}
