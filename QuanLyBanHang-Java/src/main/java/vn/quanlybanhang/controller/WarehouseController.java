package vn.quanlybanhang.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import vn.quanlybanhang.constants.AppConstants;
import vn.quanlybanhang.model.KiemKho;
import vn.quanlybanhang.model.NhapKho;
import vn.quanlybanhang.service.KiemKhoService;
import vn.quanlybanhang.service.WarehouseCartService;
import vn.quanlybanhang.service.WarehouseService;
import vn.quanlybanhang.service.WarehouseService.CreateReceiptPageData;
import vn.quanlybanhang.service.WarehouseService.KiemKhoTabData;
import vn.quanlybanhang.service.WarehouseService.PhieuTabData;
import vn.quanlybanhang.service.WarehouseService.TonKhoTabData;
import vn.quanlybanhang.service.WarehouseService.WarehouseContext;
import vn.quanlybanhang.util.JsonUtil;
import vn.quanlybanhang.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {
        "/kho",
        "/kho/create",
        "/kho/cart/add",
        "/kho/cart/remove",
        "/kho/cart/commit",
        "/kho/view",
        "/kho/approve",
        "/kho/cancel",
        "/kho/kiem-kho/create",
        "/kho/kiem-kho/view",
        "/kho/kiem-kho/approve",
        "/kho/kiem-kho/cancel",
        "/api/kho/expiry-cancel",
        "/api/kho/check-expiry-alerts"
})
public class WarehouseController extends BaseController {

    private static final long serialVersionUID = 1L;

    private final WarehouseService warehouseService = new WarehouseService();
    private final WarehouseCartService warehouseCartService = new WarehouseCartService();
    private final KiemKhoService kiemKhoService = new KiemKhoService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        try {
            switch (path) {
                case "/kho" -> showList(request, response);
                case "/kho/create" -> showCreate(request, response);
                case "/kho/view" -> showView(request, response);
                case "/kho/kiem-kho/create" -> showKiemKhoCreate(request, response);
                case "/kho/kiem-kho/view" -> showKiemKhoView(request, response);
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
                case "/kho/create" -> createReceipt(request, response);
                case "/kho/cart/add" -> addCartItem(request, response);
                case "/kho/cart/remove" -> removeCartItem(request, response);
                case "/kho/cart/commit" -> commitCart(request, response);
                case "/kho/approve" -> approveReceipt(request, response);
                case "/kho/cancel" -> cancelReceipt(request, response);
                case "/kho/kiem-kho/create" -> createKiemKho(request, response);
                case "/kho/kiem-kho/approve" -> approveKiemKho(request, response);
                case "/kho/kiem-kho/cancel" -> cancelKiemKho(request, response);
                case "/api/kho/expiry-cancel" -> cancelExpiryLot(request, response);
                case "/api/kho/check-expiry-alerts" -> checkExpiryAlerts(response);
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException ex) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    private void showList(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        String tab = paramOrEmpty(request, "tab");
        if (tab.isBlank()) {
            tab = "phieu";
        }

        WarehouseContext context = warehouseService.prepareContext();
        request.setAttribute("pageTitle", "Kho hàng");
        request.setAttribute("pageHeaderTitle", "Quản lý nhập kho");
        applyPageChrome(request, "fas fa-warehouse",
                "Quản lý phiếu nhập, lô hàng và hạn sử dụng sát hơn với nghiệp vụ PHP.", null);
        request.setAttribute("tab", tab);
        request.setAttribute("status", request.getParameter("status"));
        request.setAttribute("from", request.getParameter("from"));
        request.setAttribute("to", request.getParameter("to"));
        request.setAttribute("hasKhoLotTable", context.hasKhoLotTable());
        request.setAttribute("expiryStats", context.expiryStats());

        if ("tonkho".equals(tab)) {
            TonKhoTabData data = warehouseService.loadTonKhoTab(request.getParameter("kw"), request.getParameter("loai"));
            request.setAttribute("tonKhoList", data.tonKhoList());
            request.setAttribute("lowStock5", data.lowStock5());
            request.setAttribute("lowStock10", data.lowStock10());
            request.setAttribute("kw", request.getParameter("kw"));
            request.setAttribute("loai", request.getParameter("loai"));
            request.setAttribute("danhMucMap", data.danhMucMap());
            render(request, response, "/WEB-INF/views/kho/index.jsp");
            return;
        }

        if ("kiemkho".equals(tab)) {
            KiemKhoTabData data = warehouseService.loadKiemKhoTab(
                    request.getParameter("status"), request.getParameter("from"), request.getParameter("to"));
            request.setAttribute("kiemKhoList", data.kiemKhoList());
            request.setAttribute("kiemKhoStats", data.kiemKhoStats());
            render(request, response, "/WEB-INF/views/kho/index.jsp");
            return;
        }

        PhieuTabData data = warehouseService.loadPhieuTab(
                request.getParameter("status"),
                request.getParameter("from"),
                request.getParameter("to"),
                request.getParameter("expiry_filter"));
        request.setAttribute("receipts", data.receipts());
        request.setAttribute("expiryFilter", data.expiryFilter());
        request.setAttribute("expiryLots", data.expiryLots());
        render(request, response, "/WEB-INF/views/kho/index.jsp");
    }

    private void showKiemKhoCreate(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        request.setAttribute("pageTitle", "Tạo phiếu kiểm kho");
        request.setAttribute("pageHeaderTitle", "Tạo phiếu kiểm kho");
        applyPageChrome(request, "fas fa-clipboard-check", "Nhập tồn thực tế để đối chiếu với tồn hệ thống.", null);
        request.setAttribute("materials", kiemKhoService.listMaterialsForAudit());
        render(request, response, "/WEB-INF/views/kho/kiemkho_create.jsp");
    }

    private void showKiemKhoView(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        Optional<KiemKho> phieu = kiemKhoService.findById(getIntParameter(request, "id", 0));
        if (phieu.isEmpty()) {
            flashAndRedirect(request, response, "/kho?tab=kiemkho", "error", "Không tìm thấy phiếu kiểm kho.");
            return;
        }
        request.setAttribute("pageTitle", "Chi tiết phiếu kiểm kho");
        request.setAttribute("pageHeaderTitle", "Chi tiết phiếu kiểm kho");
        applyPageChrome(request, "fas fa-clipboard-list", "Đối chiếu tồn hệ thống và tồn thực tế.", null);
        request.setAttribute("phieu", phieu.get());
        request.setAttribute("canApprove", AppConstants.ROLE_MANAGER.equals(SessionUtil.getCurrentRole(request)));
        render(request, response, "/WEB-INF/views/kho/kiemkho_view.jsp");
    }

    private void createKiemKho(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        int employeeId = SessionUtil.getCurrentUserId(request) == null ? 0 : SessionUtil.getCurrentUserId(request);
        KiemKho phieu = kiemKhoService.bindPhieu(
                employeeId,
                paramOrEmpty(request, "ghi_chu"),
                request.getParameterValues("nguyenlieu_id"),
                request.getParameterValues("ton_thuc_te"),
                request.getParameterValues("ly_do"));

        try {
            kiemKhoService.createPhieu(phieu);
            flashAndRedirect(request, response, "/kho?tab=kiemkho", "success", "Đã lưu phiếu kiểm kho chờ duyệt.");
        } catch (SQLException ex) {
            setFlash(request, "error", ex.getMessage());
            redirect(request, response, "/kho/kiem-kho/create");
        }
    }

    private void approveKiemKho(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        int id = getIntParameter(request, "id", 0);
        Optional<KiemKho> phieu = kiemKhoService.findById(id);
        if (phieu.isEmpty()) {
            flashAndRedirect(request, response, "/kho?tab=kiemkho", "error", "Không tìm thấy phiếu kiểm kho.");
            return;
        }
        try {
            kiemKhoService.approvePhieu(phieu.get());
            setFlash(request, "success", "Đã duyệt phiếu kiểm kho. Tồn kho đã được cập nhật.");
        } catch (SQLException ex) {
            setFlash(request, "error", ex.getMessage());
        }
        redirect(request, response, "/kho/kiem-kho/view?id=" + id);
    }

    private void cancelKiemKho(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        int id = getIntParameter(request, "id", 0);
        Optional<KiemKho> phieu = kiemKhoService.findById(id);
        if (phieu.isEmpty()) {
            flashAndRedirect(request, response, "/kho?tab=kiemkho", "error", "Không tìm thấy phiếu kiểm kho.");
            return;
        }
        try {
            kiemKhoService.cancelPhieu(phieu.get());
            setFlash(request, "success", "Đã hủy phiếu kiểm kho.");
        } catch (SQLException ex) {
            setFlash(request, "error", ex.getMessage());
        }
        redirect(request, response, "/kho/kiem-kho/view?id=" + id);
    }

    private void showCreate(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        CreateReceiptPageData pageData = warehouseService.loadCreatePage();
        request.setAttribute("pageTitle", "Tạo phiếu nhập kho");
        request.setAttribute("pageHeaderTitle", "Tạo phiếu nhập kho");
        applyPageChrome(request, "fas fa-file-circle-plus", "Tạo phiếu nhập mới, chọn NCC và nguyên liệu.", null);
        request.setAttribute("materials", pageData.materials());
        request.setAttribute("suppliers", pageData.suppliers());
        request.setAttribute("cart", warehouseCartService.getCart(request.getSession()));
        render(request, response, "/WEB-INF/views/kho/create.jsp");
    }

    private void showView(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        Optional<NhapKho> receipt = warehouseService.findReceiptById(getIntParameter(request, "id", 0));
        if (receipt.isEmpty()) {
            flashAndRedirect(request, response, "/kho", "error", "Không tìm thấy phiếu nhập.");
            return;
        }
        request.setAttribute("pageTitle", "Chi tiết phiếu nhập");
        request.setAttribute("pageHeaderTitle", "Chi tiết phiếu nhập");
        applyPageChrome(request, "fas fa-file-lines", "Chi tiết phiếu nhập kho và dòng lô.", null);
        request.setAttribute("receipt", receipt.get());
        render(request, response, "/WEB-INF/views/kho/view.jsp");
    }

    private void createReceipt(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        var error = warehouseService.createReceiptFromForm(
                getIntParameter(request, "nha_cung_cap_id", 0),
                SessionUtil.getCurrentUserId(request),
                paramOrEmpty(request, "ghi_chu"),
                request.getParameterValues("nguyenlieu_id"),
                request.getParameterValues("so_luong_nhap"),
                request.getParameterValues("don_gia"),
                request.getParameterValues("han_su_dung"));
        if (error.isPresent()) {
            setFlash(request, "error", error.get());
            redirect(request, response, "/kho/create");
            return;
        }
        setFlash(request, "success", "Tạo phiếu nhập kho thành công.");
        redirect(request, response, "/kho");
    }

    private void approveReceipt(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        Optional<NhapKho> receipt = warehouseService.findReceiptById(getIntParameter(request, "id", 0));
        if (receipt.isEmpty()) {
            flashAndRedirect(request, response, "/kho", "error", "Không tìm thấy phiếu nhập.");
            return;
        }
        try {
            warehouseService.approveReceipt(receipt.get(), SessionUtil.getCurrentUserId(request));
            setFlash(request, "success", "Đã duyệt phiếu nhập thành công.");
        } catch (SQLException ex) {
            setFlash(request, "error", ex.getMessage());
        }
        redirect(request, response, "/kho/view?id=" + receipt.get().getId());
    }

    private void cancelReceipt(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        int id = getIntParameter(request, "id", 0);
        try {
            warehouseService.cancelReceipt(id);
            setFlash(request, "success", "Đã hủy phiếu nhập.");
        } catch (SQLException ex) {
            setFlash(request, "error", ex.getMessage());
        }
        redirect(request, response, "/kho");
    }

    private void cancelExpiryLot(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        int lotId = getIntParameter(request, "lot_id", 0);
        String reason = request.getParameter("ly_do");
        Integer employeeId = SessionUtil.getCurrentUserId(request);
        if (employeeId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.writeJson(response, Map.of("success", false, "error", "Chưa đăng nhập."));
            return;
        }
        try {
            warehouseService.cancelExpiredLot(lotId, employeeId, reason);
            JsonUtil.writeJson(response, Map.of("success", true));
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJson(response, Map.of("success", false, "error", ex.getMessage()));
        }
    }

    private void checkExpiryAlerts(HttpServletResponse response) throws SQLException, IOException {
        try {
            int inserted = warehouseService.syncExpiryAlerts();
            JsonUtil.writeJson(response, Map.of("success", true, "inserted", inserted));
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJson(response, Map.of("success", false, "error", ex.getMessage()));
        }
    }

    private void addCartItem(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Map<String, Object> body = objectMapper.readValue(request.getInputStream(), new TypeReference<>() {});
            int id = body.get("id") == null ? 0 : ((Number) body.get("id")).intValue();
            BigDecimal qty = body.get("qty") == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(body.get("qty")));
            BigDecimal donGia = body.get("don_gia") == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(body.get("don_gia")));
            LocalDate hanSuDung = null;
            Object expiry = body.get("han_su_dung");
            if (expiry != null && !String.valueOf(expiry).isBlank()) {
                hanSuDung = LocalDate.parse(String.valueOf(expiry));
            }
            JsonUtil.writeJson(response, warehouseCartService.addItem(request.getSession(), id, qty, donGia, hanSuDung));
        } catch (IllegalArgumentException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJson(response, Map.of("success", false, "error", ex.getMessage()));
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.writeJson(response, Map.of("success", false, "error", ex.getMessage()));
        }
    }

    private void removeCartItem(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Map<String, Object> body = objectMapper.readValue(request.getInputStream(), new TypeReference<>() {});
            int id = body.get("id") == null ? 0 : ((Number) body.get("id")).intValue();
            JsonUtil.writeJson(response, warehouseCartService.removeItem(request.getSession(), id));
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.writeJson(response, Map.of("success", false, "error", ex.getMessage()));
        }
    }

    private void commitCart(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Map<String, Object> body = objectMapper.readValue(request.getInputStream(), new TypeReference<>() {});
            Integer supplierId = null;
            Object supplierRaw = body.get("nha_cung_cap_id");
            if (supplierRaw != null && !String.valueOf(supplierRaw).isBlank()) {
                supplierId = ((Number) supplierRaw).intValue();
                if (supplierId <= 0) {
                    supplierId = null;
                }
            }
            String ghiChu = body.get("ghi_chu") == null ? null : String.valueOf(body.get("ghi_chu"));
            JsonUtil.writeJson(response, warehouseCartService.commitCart(
                    request.getSession(),
                    supplierId,
                    ghiChu,
                    SessionUtil.getCurrentUserId(request)));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJson(response, Map.of("success", false, "error", ex.getMessage()));
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.writeJson(response, Map.of("success", false, "error", ex.getMessage()));
        }
    }

    private void flashAndRedirect(HttpServletRequest request, HttpServletResponse response, String path,
                                  String type, String message) throws IOException {
        setFlash(request, type, message);
        redirect(request, response, path);
    }
}
