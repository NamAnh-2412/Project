package vn.quanlybanhang.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Map;

import vn.quanlybanhang.service.BranchService;
import vn.quanlybanhang.service.ChannelService;
import vn.quanlybanhang.service.DisplayService;
import vn.quanlybanhang.service.InsightService;
import vn.quanlybanhang.service.InvoiceService;
import vn.quanlybanhang.service.SettingsService;
import vn.quanlybanhang.util.JsonUtil;
import vn.quanlybanhang.util.RequestUtil;
import vn.quanlybanhang.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {"/du-bao", "/api/goi-y", "/chi-nhanh", "/mo-rong", "/don-hang/hoa-don"})
public class EnterpriseController extends BaseController {

    private static final long serialVersionUID = 1L;
    private final InsightService insightService = new InsightService();
    private final BranchService branchService = new BranchService();
    private final ChannelService channelService = new ChannelService();
    private final DisplayService displayService = new DisplayService();
    private final InvoiceService invoiceService = new InvoiceService();
    private final SettingsService settingsService = new SettingsService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            switch (request.getServletPath()) {
                case "/api/goi-y" -> JsonUtil.writeJson(response, Map.of(
                        "success", true,
                        "goiY", insightService.suggestions(getIntParameter(request, "sanpham_id", 0))));
                case "/du-bao" -> {
                    exposeFlash(request);
                    request.setAttribute("pageTitle", "Dự báo");
                    applyPageChrome(request, "fas fa-chart-area",
                            "Ước lượng món bán theo cùng thứ trong 8 tuần gần nhất.", null);
                    request.setAttribute("forecast", insightService.forecastTomorrow());
                    request.setAttribute("reportText", insightService.dailyReportText());
                    render(request, response, "/WEB-INF/views/dubao/index.jsp");
                }
                case "/chi-nhanh" -> {
                    exposeFlash(request);
                    request.setAttribute("pageTitle", "Chi nhánh");
                    applyPageChrome(request, "fas fa-store", "Kho từng chi nhánh và phiếu chuyển hàng.", null);
                    request.setAttribute("branches", branchService.branches());
                    request.setAttribute("stock", branchService.branchStock());
                    render(request, response, "/WEB-INF/views/chinhanh/index.jsp");
                }
                case "/mo-rong" -> {
                    exposeFlash(request);
                    request.setAttribute("pageTitle", "Mở rộng");
                    applyPageChrome(request, "fas fa-plug",
                            "Telegram, sàn giao đồ ăn, hóa đơn điện tử và màn hình khách.", null);
                    request.setAttribute("settings", settingsService.getSettingsWithDefaults());
                    request.setAttribute("cfdToken", displayService.displayToken());
                    render(request, response, "/WEB-INF/views/morong/index.jsp");
                }
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException ex) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getServletPath();
        try {
            switch (path) {
                case "/du-bao" -> {
                    var error = insightService.sendTelegramNow();
                    setFlash(request, error.isPresent() ? "error" : "success",
                            error.orElse("Đã gửi báo cáo qua Telegram."));
                    redirect(request, response, "/du-bao");
                }
                case "/chi-nhanh" -> {
                    if ("transfer".equals(paramOrEmpty(request, "action"))) {
                        int staffId = SessionUtil.getCurrentUserId(request) == null ? 0 : SessionUtil.getCurrentUserId(request);
                        branchService.transfer(
                                getIntParameter(request, "tu_chi_nhanh_id", 0),
                                getIntParameter(request, "den_chi_nhanh_id", 0),
                                getIntParameter(request, "nguyenlieu_id", 0),
                                RequestUtil.getBigDecimal(request, "so_luong", BigDecimal.ZERO),
                                staffId,
                                paramOrEmpty(request, "ghi_chu"));
                        setFlash(request, "success", "Đã chuyển kho giữa các chi nhánh.");
                    } else {
                        var error = branchService.addBranch(
                                paramOrEmpty(request, "ma_chi_nhanh"),
                                paramOrEmpty(request, "ten_chi_nhanh"),
                                paramOrEmpty(request, "dia_chi"),
                                paramOrEmpty(request, "so_dien_thoai"));
                        setFlash(request, error.isPresent() ? "error" : "success", error.orElse("Đã thêm chi nhánh."));
                    }
                    redirect(request, response, "/chi-nhanh");
                }
                case "/mo-rong" -> {
                    channelService.saveChannelSettings(
                            paramOrEmpty(request, "telegram_bot_token"),
                            paramOrEmpty(request, "telegram_chat_id"),
                            paramOrEmpty(request, "delivery_api_key"),
                            paramOrEmpty(request, "einvoice_provider"),
                            paramOrEmpty(request, "einvoice_username"),
                            paramOrEmpty(request, "einvoice_password"),
                            paramOrEmpty(request, "geofence_lat"),
                            paramOrEmpty(request, "geofence_lng"),
                            paramOrEmpty(request, "geofence_met"));
                    setFlash(request, "success", "Đã lưu kết nối.");
                    redirect(request, response, "/mo-rong");
                }
                case "/don-hang/hoa-don" -> {
                    int id = getIntParameter(request, "id", 0);
                    setFlash(request, "success", invoiceService.requestInvoice(id));
                    redirect(request, response, "/don-hang/view?id=" + id);
                }
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException ex) {
            setFlash(request, "error", ex.getMessage());
            redirect(request, response, path);
        }
    }
}
