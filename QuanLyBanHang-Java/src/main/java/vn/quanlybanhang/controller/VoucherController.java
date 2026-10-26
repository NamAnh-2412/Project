package vn.quanlybanhang.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

import vn.quanlybanhang.service.VoucherService;
import vn.quanlybanhang.util.JsonUtil;
import vn.quanlybanhang.util.RequestUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {"/ma-giam-gia", "/api/ma-giam-gia/kiem-tra"})
public class VoucherController extends BaseController {

    private static final long serialVersionUID = 1L;

    private final VoucherService voucherService = new VoucherService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("/api/ma-giam-gia/kiem-tra".equals(request.getServletPath())) {
            preview(request, response);
            return;
        }
        try {
            exposeFlash(request);
            request.setAttribute("pageTitle", "Mã giảm giá");
            applyPageChrome(request, "fas fa-ticket",
                    "Mã có hạn, đơn tối thiểu và số lần dùng. Thu ngân nhập mã khi thanh toán.", null);
            request.setAttribute("vouchers", voucherService.list());
            render(request, response, "/WEB-INF/views/giamgia/index.jsp");
        } catch (SQLException ex) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = paramOrEmpty(request, "action");
        try {
            if ("toggle".equals(action)) {
                voucherService.toggle(getIntParameter(request, "id", 0), paramOrEmpty(request, "trang_thai"));
                setFlash(request, "success", "Đã cập nhật trạng thái mã.");
            } else {
                Optional<String> error = voucherService.create(
                        paramOrEmpty(request, "ma"),
                        paramOrEmpty(request, "loai"),
                        RequestUtil.getBigDecimal(request, "gia_tri", BigDecimal.ZERO),
                        RequestUtil.getBigDecimal(request, "don_toi_thieu", BigDecimal.ZERO),
                        RequestUtil.getBigDecimal(request, "giam_toi_da", BigDecimal.ZERO),
                        getNullableIntParameter(request, "so_lan_toi_da"),
                        paramOrEmpty(request, "bat_dau"),
                        paramOrEmpty(request, "ket_thuc"));
                if (error.isPresent()) {
                    setFlash(request, "error", error.get());
                } else {
                    setFlash(request, "success", "Đã tạo mã giảm giá.");
                }
            }
        } catch (SQLException ex) {
            setFlash(request, "error", ex.getMessage());
        }
        redirect(request, response, "/ma-giam-gia");
    }

    private void preview(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            VoucherService.Quote quote = voucherService.preview(
                    paramOrEmpty(request, "ma"),
                    RequestUtil.getBigDecimal(request, "tong", BigDecimal.ZERO));
            JsonUtil.writeJson(response, Map.of(
                    "success", true,
                    "ma", quote.code(),
                    "giam", quote.discount()));
        } catch (SQLException ex) {
            JsonUtil.writeJson(response, Map.of("success", false, "error", ex.getMessage()));
        }
    }
}
