package vn.quanlybanhang.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

import vn.quanlybanhang.model.DonHang;
import vn.quanlybanhang.service.OrderService;
import vn.quanlybanhang.service.OrderService.CreateOrderResult;
import vn.quanlybanhang.service.OrderService.CreatePageData;
import vn.quanlybanhang.service.OrderService.MultipleOrderResult;
import vn.quanlybanhang.service.SettingsService;
import vn.quanlybanhang.service.TransferQrService;
import vn.quanlybanhang.util.JsonUtil;
import vn.quanlybanhang.util.RequestUtil;
import vn.quanlybanhang.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {
        "/don-hang",
        "/don-hang/create",
        "/don-hang/cancel",
        "/don-hang/view",
        "/don-hang/pay",
        "/don-hang/in"
})
public class OrderController extends BaseController {

    private static final long serialVersionUID = 1L;

    private final OrderService orderService = new OrderService();
    private final TransferQrService transferQrService = new TransferQrService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        try {
            switch (path) {
                case "/don-hang" -> showOrderList(request, response);
                case "/don-hang/create" -> showCreatePage(request, response);
                case "/don-hang/cancel" -> showCancelPage(request, response);
                case "/don-hang/view" -> showOrderDetail(request, response);
                case "/don-hang/pay" -> showPayPage(request, response);
                case "/don-hang/in" -> showPrintPage(request, response);
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException ex) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        try {
            switch (path) {
                case "/don-hang/create" -> createOrder(request, response);
                case "/don-hang/cancel" -> cancelOrder(request, response);
                case "/don-hang/pay" -> completePendingOrder(request, response);
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException ex) {
            if (RequestUtil.wantsJson(request)) {
                JsonUtil.writeJson(response, Map.of("success", false, "error", ex.getMessage()));
                return;
            }
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    private void showOrderList(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        request.setAttribute("pageTitle", "Đơn hàng");
        applyPageChrome(request, "fas fa-receipt",
                "Danh sách đơn hàng đã thanh toán, lọc theo ngày và hình thức thanh toán.", null);
        request.setAttribute("statusOptions", orderService.getStatusOptionsMap());
        request.setAttribute("paymentOptions", orderService.getPaymentOptionsMap());
        request.setAttribute("list", orderService.searchOrders(
                request.getParameter("kw"),
                request.getParameter("from"),
                request.getParameter("to"),
                request.getParameter("payment_method"),
                request.getParameter("status")));
        render(request, response, "/WEB-INF/views/donhang/index.jsp");
    }

    private void showCreatePage(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        CreatePageData pageData = orderService.loadCreatePageData(getNullableIntParameter(request, "cat"));
        request.setAttribute("pageTitle", "Tạo đơn hàng");
        request.setAttribute("cats", pageData.categories());
        request.setAttribute("activeCat", pageData.activeCat());
        request.setAttribute("prod", pageData.products());
        request.setAttribute("defaultPaymentMethod", pageData.defaultPaymentMethod());
        render(request, response, "/WEB-INF/views/donhang/create.jsp");
    }

    private void showCancelPage(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        int id = getIntParameter(request, "id", 0);
        Optional<DonHang> order = orderService.findOrderById(id);
        if (order.isEmpty()) {
            setFlash(request, "error", "Không tìm thấy đơn hàng.");
            redirect(request, response, "/don-hang");
            return;
        }
        Optional<String> accessError = orderService.validateCancelAccess(order.get());
        if (accessError.isPresent()) {
            setFlash(request, "error", accessError.get());
            redirect(request, response, "/don-hang");
            return;
        }
        request.setAttribute("pageTitle", "Hủy đơn hàng");
        applyPageChrome(request, "fas fa-ban", "Xác nhận hủy đơn hàng trong hệ thống.", null);
        request.setAttribute("order", order.get());
        render(request, response, "/WEB-INF/views/donhang/cancel.jsp");
    }

    private void showOrderDetail(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        int id = getIntParameter(request, "id", 0);
        Optional<DonHang> order = orderService.findOrderById(id);
        if (order.isEmpty()) {
            setFlash(request, "error", "Không tìm thấy đơn hàng.");
            redirect(request, response, "/don-hang");
            return;
        }
        request.setAttribute("pageTitle", "Chi tiết đơn hàng");
        applyPageChrome(request, "fas fa-file-invoice", "Chi tiết món, size và nhóm lựa chọn.", null);
        request.setAttribute("order", order.get());
        request.setAttribute("invoice", new vn.quanlybanhang.service.InvoiceService()
                .latestInvoice(order.get().getId()).orElse(null));
        request.setAttribute("canPayOrder", orderService.canCompleteOrder(order.get().getTrangThai()));
        request.setAttribute("paymentOptions", orderService.getPaymentOptionsMap());
        request.setAttribute("transferQr", transferQrService.forAmount(order.get().getMaDonHang(), order.get().getThanhTien()));
        render(request, response, "/WEB-INF/views/donhang/view.jsp");
    }

    private void showPayPage(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        int id = getIntParameter(request, "id", 0);
        Optional<DonHang> order = orderService.findOrderById(id);
        if (order.isEmpty()) {
            setFlash(request, "error", "Không tìm thấy đơn hàng.");
            redirect(request, response, "/don-hang");
            return;
        }
        Optional<String> accessError = orderService.validatePayAccess(order.get());
        if (accessError.isPresent()) {
            setFlash(request, "error", accessError.get());
            redirect(request, response, "/don-hang/view?id=" + id);
            return;
        }

        request.setAttribute("pageTitle", "Thanh toán đơn hàng");
        applyPageChrome(request, "fas fa-credit-card", "Xác nhận thanh toán cho đơn hàng đang chờ xử lý.", null);
        request.setAttribute("order", order.get());
        request.setAttribute("paymentOptions", orderService.getPaymentOptionsMap());
        request.setAttribute("defaultPaymentMethod", orderService.getDefaultPaymentMethod());
        request.setAttribute("transferQr", transferQrService.forAmount(order.get().getMaDonHang(), order.get().getThanhTien()));
        render(request, response, "/WEB-INF/views/donhang/pay.jsp");
    }

    private void showPrintPage(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        int id = getIntParameter(request, "id", 0);
        Optional<DonHang> order = orderService.findOrderById(id);
        if (order.isEmpty()) {
            setFlash(request, "error", "Không tìm thấy đơn hàng.");
            redirect(request, response, "/don-hang");
            return;
        }
        request.setAttribute("order", order.get());
        request.setAttribute("transferQr", transferQrService.forAmount(order.get().getMaDonHang(), order.get().getThanhTien()));
        request.setAttribute("shopSettings", new SettingsService().getSettingsWithDefaults());
        render(request, response, "/WEB-INF/views/donhang/print.jsp");
    }

    private void createOrder(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        String action = request.getParameter("action");
        if ("create_multiple_orders".equals(action)) {
            MultipleOrderResult result = orderService.processBatchCreate(
                    request.getParameter("orders"), SessionUtil.getCurrentUserId(request));
            respondCreateOrder(request, response, result.success(), result.message(), "/don-hang", result);
            return;
        }

        CreateOrderResult result = orderService.processSingleCreate(
                action,
                request.getParameter("items"),
                request.getParameter("ten_khachhang"),
                request.getParameter("sdt_khachhang"),
                request.getParameter("phuong_thuc_thanhtoan"),
                SessionUtil.getCurrentUserId(request),
                request.getParameter("ma_giam"));
        respondCreateOrder(request, response, result.success(), result.message(), result.redirectPath(), null);
    }

    private void respondCreateOrder(HttpServletRequest request, HttpServletResponse response, boolean success,
            String message, String redirectPath, MultipleOrderResult batchResult) throws IOException {
        if (RequestUtil.wantsJson(request)) {
            if (success) {
                setFlash(request, "success", message);
            } else {
                setFlash(request, "error", message);
            }
            Map<String, Object> payload = new java.util.LinkedHashMap<>();
            payload.put("success", success);
            payload.put("message", message);
            payload.put("redirect", request.getContextPath() + redirectPath);
            payload.put("error", success ? "" : message);
            if (batchResult != null) {
                payload.put("count", batchResult.count());
                payload.put("errors", batchResult.errors());
            }
            JsonUtil.writeJson(response, payload);
            return;
        }

        if (success) {
            setFlash(request, "success", message);
        } else {
            setFlash(request, "error", message);
        }
        redirect(request, response, redirectPath);
    }

    private void cancelOrder(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        int id = getIntParameter(request, "id", 0);
        Optional<String> error = orderService.cancelOrderById(id);
        if (error.isPresent()) {
            setFlash(request, "error", error.get());
            redirect(request, response, "/don-hang");
            return;
        }
        setFlash(request, "success", "Hủy đơn hàng thành công.");
        redirect(request, response, "/don-hang");
    }

    private void completePendingOrder(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        int id = getIntParameter(request, "id", 0);
        Optional<String> error = orderService.completeOrderById(
                id, request.getParameter("phuong_thuc_thanhtoan"), SessionUtil.getCurrentUserId(request));
        if (error.isPresent()) {
            setFlash(request, "error", error.get());
            redirect(request, response, "/don-hang");
            return;
        }
        setFlash(request, "success", "Thanh toán thành công. Đơn hàng đã hoàn thành.");
        String method = request.getParameter("phuong_thuc_thanhtoan");
        if ("chuyen_khoan".equals(method)) {
            redirect(request, response, "/don-hang/in?id=" + id);
            return;
        }
        redirect(request, response, "/don-hang/view?id=" + id);
    }
}
