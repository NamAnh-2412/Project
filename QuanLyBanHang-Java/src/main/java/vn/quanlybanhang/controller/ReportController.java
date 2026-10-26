package vn.quanlybanhang.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Map;

import vn.quanlybanhang.service.ReportService;
import vn.quanlybanhang.service.ReportService.ReportPageData;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {"/bao-cao", "/bao-cao/xuat"})
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

            if ("/bao-cao/xuat".equals(request.getServletPath())) {
                writeCsv(response, data);
                return;
            }

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

    private void writeCsv(HttpServletResponse response, ReportPageData data) throws IOException {
        String tab = data.tab() == null ? "tonghop" : data.tab();
        String filename = "bao-cao-" + tab + "-" + data.from() + "-" + data.to() + ".csv";
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        StringBuilder csv = new StringBuilder();
        csv.append('\uFEFF');
        switch (tab) {
            case "chitiet" -> {
                csv.append("Ngay,Tien mat,Chuyen khoan,The,App online,Tong tien,So don\n");
                for (Map<String, Object> row : data.chitietData()) {
                    csv.append(cell(row.get("ngay"))).append(',')
                            .append(cell(row.get("tien_mat"))).append(',')
                            .append(cell(row.get("chuyen_khoan"))).append(',')
                            .append(cell(row.get("the"))).append(',')
                            .append(cell(row.get("app_online"))).append(',')
                            .append(cell(row.get("tong_tien"))).append(',')
                            .append(cell(row.get("so_don"))).append('\n');
                }
            }
            case "daily_revenue" -> {
                csv.append("Ngay,Doanh thu,So don\n");
                for (Map<String, Object> row : data.dailyRevenueData()) {
                    csv.append(cell(row.get("day_date"))).append(',')
                            .append(cell(row.get("total_amount"))).append(',')
                            .append(cell(row.get("order_count"))).append('\n');
                }
            }
            default -> {
                csv.append("Ngay,Tong tien,So don\n");
                for (Map<String, Object> row : data.tongHopData()) {
                    csv.append(cell(row.get("ngay"))).append(',')
                            .append(cell(row.get("tong_tien"))).append(',')
                            .append(cell(row.get("so_don"))).append('\n');
                }
            }
        }
        response.getOutputStream().write(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    private String cell(Object value) {
        if (value == null) {
            return "";
        }
        String text = value instanceof BigDecimal amount ? amount.toPlainString() : value.toString();
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }
}
