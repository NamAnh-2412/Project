package vn.quanlybanhang.controller;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import vn.quanlybanhang.service.ReportService;
import vn.quanlybanhang.service.ReportService.RebuildResult;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = "/api/rebuild-daily-revenue")
public class ReportApiController extends BaseApiController {

    private static final long serialVersionUID = 1L;

    private final ReportService reportService = new ReportService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RebuildResult result = reportService.rebuildDailyRevenue();
        if (result.success()) {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("message", result.message());
            payload.put("inserted", result.inserted());
            payload.put("timestamp", result.timestamp());
            writeSuccess(response, payload);
            return;
        }
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, result.error());
    }
}
