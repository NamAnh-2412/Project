package vn.quanlybanhang.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import vn.quanlybanhang.service.DashboardService;
import vn.quanlybanhang.util.JsonUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = "/api/dashboard-stats")
public class DashboardApiController extends BaseApiController {

    private static final long serialVersionUID = 1L;

    private final DashboardService dashboardService = new DashboardService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Map<String, Object> payload = dashboardService.getTodayStatsApiPayload();
            JsonUtil.writeJson(response, payload);
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.writeJson(response, Map.of(
                    "status", "error",
                    "message", ex.getMessage() == null ? "unknown" : ex.getMessage()));
        }
    }
}
