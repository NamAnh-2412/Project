package vn.quanlybanhang.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import vn.quanlybanhang.model.LuaChon;
import vn.quanlybanhang.model.NhomLuaChon;
import vn.quanlybanhang.service.ChoiceGroupService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/san-pham/nhom-lua-chon")
public class ChoiceGroupController extends BaseController {

    private static final long serialVersionUID = 1L;

    private final ChoiceGroupService choiceGroupService = new ChoiceGroupService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("pageTitle", "Nhóm lựa chọn");
            applyPageChrome(request, "fas fa-list-ul", "Quản lý nhóm topping / lựa chọn món.", List.of("products.css"));
            request.setAttribute("groups", choiceGroupService.listAllWithChoices());
            request.setAttribute("group", new NhomLuaChon());
            request.setAttribute("choice", new LuaChon());
            render(request, response, "/WEB-INF/views/sanpham/nhomluachon.jsp");
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
                case "save-group" -> saveGroup(request, response);
                case "save-choice" -> saveChoice(request, response);
                case "delete-group" -> deleteGroup(request, response);
                case "delete-choice" -> deleteChoice(request, response);
                default -> redirect(request, response, "/san-pham/nhom-lua-chon");
            }
        } catch (SQLException ex) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    private void saveGroup(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        NhomLuaChon group = choiceGroupService.bindGroup(
                getIntParameter(request, "group_id", 0),
                paramOrEmpty(request, "ten_nhom"),
                paramOrEmpty(request, "mota"),
                getIntParameter(request, "thu_tu", 0),
                request.getParameter("dang_hoat_dong"));

        var error = choiceGroupService.saveGroup(group);
        if (error.isPresent()) {
            setFlash(request, "error", error.get());
            redirect(request, response, "/san-pham/nhom-lua-chon");
            return;
        }

        setFlash(request, "success", group.getId() > 0 ? "Cập nhật nhóm lựa chọn thành công." : "Thêm nhóm lựa chọn thành công.");
        redirect(request, response, "/san-pham/nhom-lua-chon");
    }

    private void saveChoice(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        LuaChon choice = choiceGroupService.bindChoice(
                getIntParameter(request, "choice_id", 0),
                getIntParameter(request, "nhom_lua_chon_id", 0),
                paramOrEmpty(request, "ten_lua_chon"),
                paramOrEmpty(request, "gia_tang_them"),
                getIntParameter(request, "choice_thu_tu", 0),
                request.getParameter("choice_dang_hoat_dong"));

        var error = choiceGroupService.saveChoice(choice);
        if (error.isPresent()) {
            setFlash(request, "error", error.get());
            redirect(request, response, "/san-pham/nhom-lua-chon");
            return;
        }

        setFlash(request, "success", choice.getId() > 0 ? "Cập nhật lựa chọn thành công." : "Thêm lựa chọn thành công.");
        redirect(request, response, "/san-pham/nhom-lua-chon");
    }

    private void deleteGroup(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        var error = choiceGroupService.deleteGroup(getIntParameter(request, "group_id", 0));
        if (error.isPresent()) {
            setFlash(request, "error", error.get());
        } else {
            setFlash(request, "success", "Đã xóa nhóm lựa chọn.");
        }
        redirect(request, response, "/san-pham/nhom-lua-chon");
    }

    private void deleteChoice(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        if (choiceGroupService.softDeleteChoice(getIntParameter(request, "choice_id", 0))) {
            setFlash(request, "success", "Đã ẩn lựa chọn.");
        }
        redirect(request, response, "/san-pham/nhom-lua-chon");
    }
}
