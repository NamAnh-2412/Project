package vn.quanlybanhang.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import vn.quanlybanhang.service.OrderService;
import vn.quanlybanhang.service.OrderService.CreateOrderResult;
import vn.quanlybanhang.service.OrderService.MultipleOrderResult;
import vn.quanlybanhang.util.JsonUtil;
import vn.quanlybanhang.util.RequestUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {
        "/api/don-hang/create-data",
        "/api/don-hang/create",
        "/api/don-hang/create-batch"
})
public class OrderApiController extends BaseApiController {

    private static final long serialVersionUID = 1L;

    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!"/api/don-hang/create-data".equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            Integer activeCat = RequestUtil.getNullableInt(request, "cat");
            Map<String, Object> payload = orderService.buildCreateContextPayload(activeCat);
            writeSuccess(response, payload);
        } catch (SQLException ex) {
            handleSqlError(response, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        try {
            switch (path) {
                case "/api/don-hang/create" -> createSingle(request, response);
                case "/api/don-hang/create-batch" -> createBatch(request, response);
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException ex) {
            handleSqlError(response, ex);
        }
    }

    private void createSingle(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        String action = RequestUtil.paramOrEmpty(request, "action");
        if (action.isBlank()) {
            action = "create_order";
        }
        CreateOrderResult result = orderService.processSingleCreate(
                action,
                RequestUtil.paramOrEmpty(request, "items"),
                RequestUtil.paramOrEmpty(request, "ten_khachhang"),
                RequestUtil.paramOrEmpty(request, "sdt_khachhang"),
                RequestUtil.paramOrEmpty(request, "phuong_thuc_thanhtoan"));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("message", result.message());
        payload.put("redirect", request.getContextPath() + result.redirectPath());
        payload.put("error", result.success() ? "" : result.message());
        if (result.success()) {
            writeSuccess(response, payload);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            payload.put("success", false);
            JsonUtil.writeJson(response, payload);
        }
    }

    private void createBatch(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        MultipleOrderResult result = orderService.processBatchCreate(RequestUtil.paramOrEmpty(request, "orders"));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("message", result.message());
        payload.put("count", result.count());
        payload.put("errors", result.errors());
        payload.put("redirect", request.getContextPath() + "/don-hang");
        payload.put("error", result.success() ? "" : result.message());
        if (result.success()) {
            writeSuccess(response, payload);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            payload.put("success", false);
            JsonUtil.writeJson(response, payload);
        }
    }
}
