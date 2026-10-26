package vn.quanlybanhang.controller;

import java.io.IOException;
import java.sql.SQLException;

import vn.quanlybanhang.service.DisplayService;
import vn.quanlybanhang.util.JsonUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {"/man-hinh-phu", "/api/cfd"})
public class DisplayController extends BaseController {

    private static final long serialVersionUID = 1L;
    private final DisplayService displayService = new DisplayService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("/api/cfd".equals(request.getServletPath())) {
            try {
                String body = displayService.loadDisplay(paramOrEmpty(request, "token"));
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(body);
            } catch (SQLException ex) {
                JsonUtil.writeJson(response, java.util.Map.of("success", false, "error", ex.getMessage()));
            }
            return;
        }
        request.setAttribute("token", paramOrEmpty(request, "token"));
        try {
            request.setAttribute("shopName", new vn.quanlybanhang.service.SettingsService().getShopName());
        } catch (SQLException ex) {
            request.setAttribute("shopName", "Quán");
        }
        request.getRequestDispatcher("/WEB-INF/views/cfd/index.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            displayService.saveDisplay(paramOrEmpty(request, "payload"));
            JsonUtil.writeJson(response, java.util.Map.of("success", true));
        } catch (SQLException ex) {
            JsonUtil.writeJson(response, java.util.Map.of("success", false, "error", ex.getMessage()));
        }
    }
}
