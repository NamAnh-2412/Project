package vn.quanlybanhang.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import vn.quanlybanhang.service.FloorService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/ban")
public class FloorController extends BaseController {

    private static final long serialVersionUID = 1L;
    private final FloorService floorService = new FloorService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            exposeFlash(request);
            String base = request.getRequestURL().toString().replace(request.getRequestURI(), request.getContextPath());
            List<Map<String, Object>> tables = floorService.tables();
            for (Map<String, Object> table : tables) {
                String link = base + "/goi-mon?token=" + table.get("qrToken");
                table.put("link", link);
                table.put("qr", "https://api.qrserver.com/v1/create-qr-code/?size=160x160&data="
                        + URLEncoder.encode(link, StandardCharsets.UTF_8));
            }
            request.setAttribute("pageTitle", "Bàn");
            applyPageChrome(request, "fas fa-chair", "Khu vực, mã QR gọi món và trạng thái bàn.", null);
            request.setAttribute("areas", floorService.areas());
            request.setAttribute("tables", tables);
            render(request, response, "/WEB-INF/views/ban/index.jsp");
        } catch (SQLException ex) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = paramOrEmpty(request, "action");
        try {
            save(action, request);
        } catch (SQLException ex) {
            setFlash(request, "error", ex.getMessage());
        }
        redirect(request, response, "/ban");
    }

    private void save(String action, HttpServletRequest request) throws SQLException {
        switch (action) {
            case "area" -> flash(request, floorService.addArea(paramOrEmpty(request, "ten_khu_vuc")), "Đã thêm khu vực.");
            case "table" -> flash(request, floorService.addTable(
                    getIntParameter(request, "khu_vuc_id", 0),
                    paramOrEmpty(request, "ten_ban"),
                    getIntParameter(request, "so_cho", 4)), "Đã thêm bàn.");
            case "refresh" -> {
                floorService.refreshQr(getIntParameter(request, "id", 0));
                setFlash(request, "success", "Đã làm mới mã QR. Mã cũ không gọi món được nữa.");
            }
            default -> setFlash(request, "error", "Hành động không hợp lệ.");
        }
    }

    private void flash(HttpServletRequest request, Optional<String> error, String success) {
        if (error.isPresent()) {
            setFlash(request, "error", error.get());
        } else {
            setFlash(request, "success", success);
        }
    }
}
