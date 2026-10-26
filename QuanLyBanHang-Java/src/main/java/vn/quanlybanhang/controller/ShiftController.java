package vn.quanlybanhang.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

import vn.quanlybanhang.model.CaLam;
import vn.quanlybanhang.model.ShiftTotals;
import vn.quanlybanhang.service.ShiftService;
import vn.quanlybanhang.util.RequestUtil;
import vn.quanlybanhang.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/ca-lam")
public class ShiftController extends BaseController {

    private static final long serialVersionUID = 1L;

    private final ShiftService shiftService = new ShiftService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer staffId = SessionUtil.getCurrentUserId(request);
        if (staffId == null) {
            redirect(request, response, "/login");
            return;
        }
        try {
            exposeFlash(request);
            request.setAttribute("pageTitle", "Ca làm việc");
            applyPageChrome(request, "fas fa-cash-register",
                    "Mở ca với tiền đầu ca. Kết ca đối chiếu tiền mặt trong két.", null);
            Optional<CaLam> open = shiftService.openShift(staffId);
            request.setAttribute("openShift", open.orElse(null));
            if (open.isPresent()) {
                ShiftTotals totals = shiftService.totals(open.get());
                request.setAttribute("totals", totals);
            }
            request.setAttribute("history", shiftService.recent());
            render(request, response, "/WEB-INF/views/ca/index.jsp");
        } catch (SQLException ex) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer staffId = SessionUtil.getCurrentUserId(request);
        if (staffId == null) {
            redirect(request, response, "/login");
            return;
        }
        String action = paramOrEmpty(request, "action");
        try {
            Optional<String> error = switch (action) {
                case "open" -> shiftService.open(staffId, SessionUtil.getDisplayName(request),
                        RequestUtil.getBigDecimal(request, "tien_dau_ca", BigDecimal.ZERO));
                case "close" -> shiftService.close(staffId,
                        RequestUtil.getBigDecimal(request, "tien_thuc_te", BigDecimal.ZERO),
                        paramOrEmpty(request, "ghi_chu"));
                default -> Optional.of("Hành động không hợp lệ.");
            };
            if (error.isPresent()) {
                setFlash(request, "error", error.get());
            } else {
                setFlash(request, "success", "open".equals(action) ? "Đã mở ca." : "Đã kết ca.");
            }
        } catch (SQLException ex) {
            setFlash(request, "error", ex.getMessage());
        }
        redirect(request, response, "/ca-lam");
    }
}
