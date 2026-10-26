package vn.quanlybanhang.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import vn.quanlybanhang.service.FloorService;
import vn.quanlybanhang.util.JsonUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {"/goi-mon", "/api/public/orders"})
public class GuestOrderController extends BaseController {

    private static final long serialVersionUID = 1L;
    private final FloorService floorService = new FloorService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("/api/public/orders".equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            var table = floorService.tableByToken(paramOrEmpty(request, "token"));
            if (table.isEmpty()) {
                request.setAttribute("error", "Mã bàn không hợp lệ hoặc đã được làm mới.");
                request.setAttribute("menuJson", "[]");
            } else {
                request.setAttribute("table", table.get());
                request.setAttribute("menuJson", objectMapper.writeValueAsString(floorService.publicMenu())
                        .replace("<", "\\u003c"));
            }
            request.setAttribute("shopName", new vn.quanlybanhang.service.SettingsService().getShopName());
            request.getRequestDispatcher("/WEB-INF/views/goimon/index.jsp").forward(request, response);
        } catch (SQLException ex) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Double lat = coordinate(request, "lat");
            Double lng = coordinate(request, "lng");
            String message = floorService.placeTableOrder(
                    paramOrEmpty(request, "token"),
                    paramOrEmpty(request, "items"),
                    paramOrEmpty(request, "ten_khachhang"),
                    paramOrEmpty(request, "sdt_khachhang"),
                    lat,
                    lng);
            JsonUtil.writeJson(response, Map.of("success", true, "message", message));
        } catch (SQLException ex) {
            JsonUtil.writeJson(response, Map.of("success", false, "error", ex.getMessage()));
        }
    }

    private static Double coordinate(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
