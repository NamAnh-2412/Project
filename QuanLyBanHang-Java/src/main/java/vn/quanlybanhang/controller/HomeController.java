package vn.quanlybanhang.controller;

import java.io.IOException;

import vn.quanlybanhang.service.DashboardService;
import vn.quanlybanhang.service.DashboardService.HomeDashboardData;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "homeController", urlPatterns = {"", "/home"})
public class HomeController extends BaseController {

    private static final long serialVersionUID = 1L;

    private final DashboardService dashboardService = new DashboardService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("pageTitle", "Quản lý tổng");
        request.setAttribute("pageHeaderTitle", "Trang Quản Lý Tổng");
        applyPageChrome(request, "fas fa-home", "Số liệu trong ngày đọc trực tiếp từ database.", null);

        HomeDashboardData data = dashboardService.loadHomeDashboard();
        request.setAttribute("todayOrders", data.todayOrders());
        request.setAttribute("todayCustomers", data.todayCustomers());
        request.setAttribute("todayRevenue", data.todayRevenue());
        request.setAttribute("topProducts", data.topProducts());
        request.setAttribute("weeklyOrders", data.weeklyOrders());

        render(request, response, "/WEB-INF/views/home.jsp");
    }
}
