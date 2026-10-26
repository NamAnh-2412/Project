package vn.quanlybanhang.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Map;

import vn.quanlybanhang.service.DeliveryService;
import vn.quanlybanhang.util.JsonUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/integrations/delivery-webhook")
public class DeliveryWebhookController extends BaseController {

    private static final long serialVersionUID = 1L;
    private final DeliveryService deliveryService = new DeliveryService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String body = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            String message = deliveryService.acceptDelivery(request.getHeader("X-Api-Key"), body);
            JsonUtil.writeJson(response, Map.of("success", true, "message", message));
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJson(response, Map.of("success", false, "error", ex.getMessage()));
        }
    }
}
