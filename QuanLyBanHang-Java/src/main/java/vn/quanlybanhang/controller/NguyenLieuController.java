package vn.quanlybanhang.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import vn.quanlybanhang.model.NguyenVatLieu;
import vn.quanlybanhang.service.MaterialService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/san-pham/nguyen-lieu")
public class NguyenLieuController extends BaseController {

    private static final long serialVersionUID = 1L;

    private final MaterialService materialService = new MaterialService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            showPage(request, response, new NguyenVatLieu(), null);
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
                default -> redirect(request, response, "/san-pham/nguyen-lieu");
            }
        } catch (SQLException ex) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    private void showPage(HttpServletRequest request, HttpServletResponse response, NguyenVatLieu material, String error)
            throws SQLException, ServletException, IOException {
        String keyword = paramOrEmpty(request, "kw");
        String category = paramOrEmpty(request, "loai");
        var pageData = materialService.loadPage(keyword, category);

        request.setAttribute("pageTitle", "Nguyên liệu");
        applyPageChrome(request, "fas fa-flask", "Lọc, tìm và liên kết nhà cung cấp như bản PHP.", List.of("products.css"));
        request.setAttribute("material", material);
        request.setAttribute("error", error);
        request.setAttribute("kw", keyword);
        request.setAttribute("filterLoai", category);
        request.setAttribute("danhMucNlLabels", materialService.getDanhMucNlLabels());
        request.setAttribute("materials", pageData.materials());
        request.setAttribute("suppliers", pageData.suppliers());
        render(request, response, "/WEB-INF/views/sanpham/nguyenlieu.jsp");
    }

    private void save(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        NguyenVatLieu material = bindMaterial(request);
        int id = getIntParameter(request, "id", 0);
        var error = materialService.saveMaterial(material, id);
        if (error.isPresent()) {
            showPage(request, response, material, error.get());
            return;
        }

        setFlash(request, "success", id > 0 ? "Cập nhật nguyên liệu thành công." : "Thêm nguyên liệu thành công.");
        redirect(request, response, "/san-pham/nguyen-lieu" + MaterialService.buildListQuery(
                paramOrEmpty(request, "kw"), paramOrEmpty(request, "loai")));
    }

    private void edit(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        var material = materialService.findById(getIntParameter(request, "id", 0));
        if (material.isEmpty()) {
            setFlash(request, "error", "Không tìm thấy nguyên liệu.");
            redirect(request, response, "/san-pham/nguyen-lieu");
            return;
        }
        showPage(request, response, material.get(), null);
    }

    private void delete(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        materialService.deleteMaterial(getIntParameter(request, "id", 0));
        setFlash(request, "success", "Đã xóa nguyên liệu.");
        redirect(request, response, "/san-pham/nguyen-lieu" + MaterialService.buildListQuery(
                paramOrEmpty(request, "kw"), paramOrEmpty(request, "loai")));
    }

    private NguyenVatLieu bindMaterial(HttpServletRequest request) {
        return materialService.bindMaterial(
                paramOrEmpty(request, "ma_nguyenlieu"),
                paramOrEmpty(request, "ten_nguyenlieu"),
                paramOrEmpty(request, "danhmuc_nl"),
                paramOrEmpty(request, "don_vi_tinh"),
                paramOrEmpty(request, "trang_thai"),
                paramOrEmpty(request, "so_luong_ton"),
                paramOrEmpty(request, "so_luong_toi_thieu"),
                paramOrEmpty(request, "gia_nhap"),
                getIntParameter(request, "nha_cung_cap_id", 0));
    }
}
