package vn.quanlybanhang.controller;

import java.io.IOException;
import java.sql.SQLException;

import vn.quanlybanhang.service.ReportService;
import vn.quanlybanhang.service.ReportService.ReportPageData;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = "/bao-cao")
public class ReportController extends BaseController {

    private static final long serialVersionUID = 1L;

    private final ReportService reportService = new ReportService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            ReportPageData data = reportService.loadReportPage(
                    request.getParameter("tab"),
                    request.getParameter("ngay_bd"),
                    request.getParameter("ngay_kt"));

            request.setAttribute("pageTitle", "Báo cáo");
            applyPageChrome(request, "fas fa-chart-line",
                    "Đồng bộ theo các tab tổng hợp, chi tiết thanh toán và daily revenue từ bản PHP.", null);
            request.setAttribute("tab", data.tab());
            request.setAttribute("ngay_bd", data.from());
            request.setAttribute("ngay_kt", data.to());
            request.setAttribute("tonghop_data", data.tongHopData());
            request.setAttribute("chitiet_data", data.chitietData());
            request.setAttribute("hourly_data", data.hourlyData());
            request.setAttribute("hourly_chart", data.hourlyChart());
            request.setAttribute("daily_revenue_data", data.dailyRevenueData());
            request.setAttribute("daily_revenue_error", data.dailyRevenueError());
            request.setAttribute("has_app_online", data.hasAppOnline());
            request.setAttribute("tong_doanh_thu", data.tongDoanhThu());
            request.setAttribute("tong_so_don", data.tongSoDon());
            render(request, response, "/WEB-INF/views/baocao/index.jsp");
        } catch (SQLException ex) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }
}
