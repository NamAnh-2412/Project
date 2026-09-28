package vn.quanlybanhang.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import vn.quanlybanhang.config.DatabaseConfig;
import vn.quanlybanhang.constants.AppConstants;
import vn.quanlybanhang.dao.DanhMucDao;
import vn.quanlybanhang.dao.KhachHangDao;
import vn.quanlybanhang.dao.OrderDao;
import vn.quanlybanhang.dao.ProductDao;
import vn.quanlybanhang.model.DanhMuc;
import vn.quanlybanhang.model.DonHang;
import vn.quanlybanhang.model.KhachHang;
import vn.quanlybanhang.model.OrderChoice;
import vn.quanlybanhang.model.OrderItem;
import vn.quanlybanhang.model.Product;
import vn.quanlybanhang.model.ProductSize;

public class OrderService {

    private static final DateTimeFormatter SINGLE_ORDER_CODE = DateTimeFormatter.ofPattern("yyMMddHHmmss");

    private final OrderDao orderDao = new OrderDao();
    private final KhachHangDao khachHangDao = new KhachHangDao();
    private final ProductDao productDao = new ProductDao();
    private final DanhMucDao danhMucDao = new DanhMucDao();
    private final SettingsService settingsService = new SettingsService();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    public record MultipleOrderResult(boolean success, int count, String message, List<String> errors) {
    }

    public record CreateOrderResult(boolean success, String message, String redirectPath) {
    }

    public record CreatePageData(List<DanhMuc> categories, Integer activeCat, List<Product> products,
            String defaultPaymentMethod) {
    }

    public List<DonHang> searchOrders(String keyword, String from, String to, String paymentMethod, String status)
            throws SQLException {
        return orderDao.searchOrders(keyword, from, to, paymentMethod, status);
    }

    public Optional<DonHang> findOrderById(int id) throws SQLException {
        return orderDao.findById(id);
    }

    public CreatePageData loadCreatePageData(Integer activeCat) throws SQLException {
        List<DanhMuc> categories = danhMucDao.findActiveThucDon();
        Integer resolvedCat = activeCat;
        if (resolvedCat == null && !categories.isEmpty()) {
            resolvedCat = categories.get(0).getId();
        }
        List<Product> products = productDao.findActiveByCategory(resolvedCat);
        return new CreatePageData(categories, resolvedCat, products, settingsService.getDefaultPaymentMethod());
    }

    public String getDefaultPaymentMethod() throws SQLException {
        return settingsService.getDefaultPaymentMethod();
    }

    public Map<String, String> getPaymentOptionsMap() {
        Map<String, String> options = new LinkedHashMap<>();
        options.put(AppConstants.PAYMENT_CASH, "Tiền mặt");
        options.put(AppConstants.PAYMENT_BANK, "Chuyển khoản");
        options.put(AppConstants.PAYMENT_CARD, "Thẻ");
        options.put(AppConstants.PAYMENT_APP_ONLINE, "App online");
        return options;
    }

    public Map<String, String> getStatusOptionsMap() {
        Map<String, String> options = new LinkedHashMap<>();
        options.put("", "Tất cả trạng thái");
        options.put(AppConstants.ORDER_STATUS_PENDING, "Chờ xử lý");
        options.put(AppConstants.ORDER_STATUS_CONFIRMED, "Đã xác nhận");
        options.put(AppConstants.ORDER_STATUS_PREPARING, "Đang chuẩn bị");
        options.put(AppConstants.ORDER_STATUS_READY, "Sẵn sàng");
        options.put(AppConstants.ORDER_STATUS_COMPLETED, "Hoàn thành");
        options.put(AppConstants.ORDER_STATUS_CANCELLED, "Đã hủy");
        return options;
    }

    public Map<String, Object> buildCreateContextPayload(Integer activeCat) throws SQLException {
        CreatePageData pageData = loadCreatePageData(activeCat);

        List<Map<String, Object>> categories = new ArrayList<>();
        for (DanhMuc category : pageData.categories()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", category.getId());
            row.put("ten_danh_muc", category.getTenDanhMuc());
            categories.add(row);
        }

        List<Map<String, Object>> products = new ArrayList<>();
        for (Product product : pageData.products()) {
            products.add(serializeProduct(product));
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("categories", categories);
        payload.put("active_cat", pageData.activeCat());
        payload.put("products", products);
        payload.put("default_payment_method", pageData.defaultPaymentMethod());
        payload.put("payment_options", getPaymentOptionsMap());
        return payload;
    }

    public Optional<String> validateCancelAccess(DonHang order) throws SQLException {
        if (AppConstants.ORDER_STATUS_CANCELLED.equals(order.getTrangThai())) {
            return Optional.of("Đơn hàng này đã hủy trước đó.");
        }
        if (AppConstants.ORDER_STATUS_COMPLETED.equals(order.getTrangThai())
                && !settingsService.isAllowCancelCompletedOrder()) {
            return Optional.of("Hệ thống không cho phép hủy đơn hàng đã hoàn thành.");
        }
        return Optional.empty();
    }

    public Optional<String> validatePayAccess(DonHang order) {
        if (AppConstants.ORDER_STATUS_CANCELLED.equals(order.getTrangThai())) {
            return Optional.of("Đơn hàng đã bị hủy, không thể thanh toán.");
        }
        if (AppConstants.ORDER_STATUS_COMPLETED.equals(order.getTrangThai())) {
            return Optional.of("Đơn hàng này đã được thanh toán.");
        }
        if (!canCompleteOrder(order.getTrangThai())) {
            return Optional.of("Đơn hàng không thể thanh toán ở trạng thái hiện tại.");
        }
        return Optional.empty();
    }

    public CreateOrderResult processSingleCreate(String action, String itemsJson, String customerName,
            String customerPhone, String paymentMethod) throws SQLException {
        if (itemsJson == null || itemsJson.isBlank()) {
            return new CreateOrderResult(false, "Không nhận được dữ liệu giỏ hàng.", "/don-hang/create");
        }
        List<IncomingOrderItem> incomingItems = parseItems(itemsJson);
        if (incomingItems.isEmpty()) {
            return new CreateOrderResult(false, "Dữ liệu giỏ hàng không hợp lệ.", "/don-hang/create");
        }

        DonHang order = buildOrderFromItems(incomingItems);
        order.setTenKhachHang(defaultCustomerName(customerName));
        order.setSdtKhachHang(customerPhone);

        if ("save_pending_order".equals(action)) {
            order.setMaDonHang(generateSingleOrderCode());
            createPendingOrder(order);
            return new CreateOrderResult(true, "Đã lưu đơn chờ xử lý.", "/don-hang");
        }

        order.setMaDonHang(generateSingleOrderCode());
        order.setTrangThai(AppConstants.ORDER_STATUS_COMPLETED);
        String resolvedPayment = paymentMethod;
        if (resolvedPayment == null || resolvedPayment.isBlank()) {
            resolvedPayment = settingsService.getDefaultPaymentMethod();
        }
        order.setPhuongThucThanhToan(validatePaymentMethod(resolvedPayment));
        createOrder(order);
        return new CreateOrderResult(true, "Tạo đơn hàng thành công.", "/don-hang");
    }

    public MultipleOrderResult processBatchCreate(String ordersJson) throws SQLException {
        if (ordersJson == null || ordersJson.isBlank()) {
            return new MultipleOrderResult(false, 0, "Không nhận được danh sách đơn hàng.", List.of());
        }

        List<IncomingBatchOrder> batchOrders;
        try {
            batchOrders = objectMapper.readValue(ordersJson, new TypeReference<List<IncomingBatchOrder>>() {});
        } catch (IOException ex) {
            return new MultipleOrderResult(false, 0, "Dữ liệu đơn hàng không hợp lệ.", List.of());
        }

        List<DonHang> orders = new ArrayList<>();
        for (IncomingBatchOrder batch : batchOrders) {
            List<IncomingOrderItem> items = batch.items == null ? List.of() : batch.items;
            if (items.isEmpty()) {
                continue;
            }
            DonHang order = buildOrderFromItems(items);
            order.setTenKhachHang(defaultCustomerName(batch.ten_khachhang));
            order.setSdtKhachHang(batch.sdt_khachhang == null ? "" : batch.sdt_khachhang);
            order.setPhuongThucThanhToan(validatePaymentMethod(batch.phuong_thuc_thanhtoan));
            orders.add(order);
        }

        if (orders.isEmpty()) {
            return new MultipleOrderResult(false, 0, "Không có đơn hàng hợp lệ để tạo.", List.of());
        }
        return createMultipleOrders(orders);
    }

    public Optional<String> cancelOrderById(int id) throws SQLException {
        Optional<DonHang> order = orderDao.findById(id);
        if (order.isEmpty()) {
            return Optional.of("Không tìm thấy đơn hàng.");
        }
        Optional<String> accessError = validateCancelAccess(order.get());
        if (accessError.isPresent()) {
            return accessError;
        }
        cancelOrder(order.get());
        return Optional.empty();
    }

    public Optional<String> completeOrderById(int id, String paymentMethod) throws SQLException {
        Optional<DonHang> order = orderDao.findById(id);
        if (order.isEmpty()) {
            return Optional.of("Không tìm thấy đơn hàng.");
        }
        Optional<String> accessError = validatePayAccess(order.get());
        if (accessError.isPresent()) {
            return accessError;
        }
        completePendingOrder(order.get(), validatePaymentMethod(paymentMethod));
        return Optional.empty();
    }

    public DonHang buildOrderFromItems(List<IncomingOrderItem> incomingItems) {
        DonHang order = new DonHang();
        order.setMaDonHang(generateBatchOrderCode());
        order.setHinhThucNhan("tai_cho");
        order.setGiamGia(BigDecimal.ZERO);
        order.setTrangThai(AppConstants.ORDER_STATUS_COMPLETED);

        List<OrderItem> items = new ArrayList<>();
        for (IncomingOrderItem incoming : incomingItems) {
            OrderItem item = new OrderItem();
            item.setSanPhamId(incoming.id);
            item.setTenSanPham(incoming.ten);
            item.setSize(incoming.size);
            item.setSizeId(incoming.getSizeId());
            item.setSoLuong(Math.max(incoming.sl, 1));
            item.setDonGia(incoming.gia == null ? BigDecimal.ZERO : incoming.gia);
            item.setLuaChonList(incoming.toChoices());
            items.add(item);
        }
        order.setItems(items);
        BigDecimal total = calculateTotal(items);
        order.setTongTien(total);
        order.setThanhTien(total);
        return order;
    }

    public List<IncomingOrderItem> parseItems(String itemsJson) {
        if (itemsJson == null || itemsJson.isBlank()) {
            return new ArrayList<>();
        }
        try {
            List<IncomingOrderItem> items = objectMapper.readValue(itemsJson,
                    new TypeReference<List<IncomingOrderItem>>() {});
            return items == null ? new ArrayList<>() : items;
        } catch (IOException ex) {
            return new ArrayList<>();
        }
    }

    public String defaultCustomerName(String name) {
        return (name == null || name.isBlank()) ? "Khách lẻ" : name.trim();
    }

    public String validatePaymentMethod(String method) {
        return normalizePaymentMethod(method);
    }

    public void createOrder(DonHang order) throws SQLException {
        persistOrder(order, true);
    }

    public void createPendingOrder(DonHang order) throws SQLException {
        order.setTrangThai(settingsService.getDefaultOrderStatus());
        order.setPhuongThucThanhToan(settingsService.getDefaultPaymentMethod());
        persistOrder(order, false);
    }

    public MultipleOrderResult createMultipleOrders(List<DonHang> orders) throws SQLException {
        if (orders == null || orders.isEmpty()) {
            return new MultipleOrderResult(false, 0, "Không có đơn hàng nào", List.of("Không có đơn hàng nào"));
        }

        List<String> errors = new ArrayList<>();
        int successCount = 0;

        try (Connection connection = DatabaseConfig.getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            try {
                for (DonHang order : orders) {
                    if (order.getItems() == null || order.getItems().isEmpty()) {
                        errors.add("Đơn hàng thiếu sản phẩm");
                        continue;
                    }
                    try {
                        prepareCompletedOrder(order);
                        persistOrderInConnection(connection, order, true);
                        successCount++;
                    } catch (SQLException ex) {
                        errors.add(ex.getMessage());
                    }
                }

                if (successCount > 0) {
                    connection.commit();
                    return new MultipleOrderResult(true, successCount,
                            "Đã tạo thành công " + successCount + " đơn hàng", errors);
                }
                connection.rollback();
                return new MultipleOrderResult(false, 0, "Không thể tạo đơn hàng nào", errors);
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private void prepareCompletedOrder(DonHang order) throws SQLException {
        if (order.getMaDonHang() == null || order.getMaDonHang().isBlank()) {
            order.setMaDonHang(generateBatchOrderCode());
        }
        if (order.getHinhThucNhan() == null || order.getHinhThucNhan().isBlank()) {
            order.setHinhThucNhan("tai_cho");
        }
        if (order.getGiamGia() == null) {
            order.setGiamGia(BigDecimal.ZERO);
        }
        order.setTrangThai(AppConstants.ORDER_STATUS_COMPLETED);
        order.setPhuongThucThanhToan(normalizePaymentMethod(order.getPhuongThucThanhToan()));
        BigDecimal total = calculateTotal(order.getItems());
        order.setTongTien(total);
        order.setThanhTien(total);
    }

    public void completePendingOrder(DonHang order, String paymentMethod) throws SQLException {
        try (Connection connection = DatabaseConfig.getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (AppConstants.ORDER_STATUS_CANCELLED.equals(order.getTrangThai())) {
                    throw new SQLException("Đơn hàng đã bị hủy, không thể thanh toán.");
                }
                if (AppConstants.ORDER_STATUS_COMPLETED.equals(order.getTrangThai())) {
                    throw new SQLException("Đơn hàng đã được thanh toán trước đó.");
                }
                if (!canCompleteOrder(order.getTrangThai())) {
                    throw new SQLException("Đơn hàng không thể thanh toán ở trạng thái hiện tại.");
                }
                String normalizedPayment = normalizePaymentMethod(paymentMethod);
                orderDao.completePendingOrder(connection, order.getId(), normalizedPayment, order.getTrangThai());
                order.setPhuongThucThanhToan(normalizedPayment);
                order.setTrangThai(AppConstants.ORDER_STATUS_COMPLETED);
                resolveCustomerForCompletedOrder(connection, order, order.getId());
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void cancelOrder(DonHang order) throws SQLException {
        try (Connection connection = DatabaseConfig.getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            try {
                orderDao.cancelOrder(connection, order.getId());
                if (order.getKhachHangId() != null && AppConstants.ORDER_STATUS_COMPLETED.equals(order.getTrangThai())) {
                    khachHangDao.reverseCustomerSpendingOnCancel(connection, order.getKhachHangId(), order.getThanhTien());
                    khachHangDao.recalculateCustomerRank(connection, order.getKhachHangId());
                }
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public String generateSingleOrderCode() {
        return "HD" + LocalDateTime.now().format(SINGLE_ORDER_CODE);
    }

    public String generateBatchOrderCode() {
        return "HD" + LocalDateTime.now().format(SINGLE_ORDER_CODE) + (100 + random.nextInt(900));
    }

    public BigDecimal calculateTotal(List<OrderItem> items) {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : items) {
            try {
                normalizeItemPricing(item);
            } catch (SQLException ignored) {
                BigDecimal fallbackUnitPrice = item.getDonGia() == null ? BigDecimal.ZERO : item.getDonGia();
                item.setDonGia(fallbackUnitPrice);
                item.setThanhTien(fallbackUnitPrice.multiply(BigDecimal.valueOf(item.getSoLuong())));
            }
            total = total.add(item.getThanhTien());
        }
        return total;
    }

    public boolean canCompleteOrder(String status) {
        return AppConstants.ORDER_STATUS_PENDING.equals(status)
                || AppConstants.ORDER_STATUS_CONFIRMED.equals(status)
                || AppConstants.ORDER_STATUS_PREPARING.equals(status)
                || AppConstants.ORDER_STATUS_READY.equals(status);
    }

    public List<String> getOrderStatusOptions() {
        List<String> statuses = new ArrayList<>();
        statuses.add("");
        statuses.add(AppConstants.ORDER_STATUS_PENDING);
        statuses.add(AppConstants.ORDER_STATUS_CONFIRMED);
        statuses.add(AppConstants.ORDER_STATUS_PREPARING);
        statuses.add(AppConstants.ORDER_STATUS_READY);
        statuses.add(AppConstants.ORDER_STATUS_COMPLETED);
        statuses.add(AppConstants.ORDER_STATUS_CANCELLED);
        return statuses;
    }

    private Map<String, Object> serializeProduct(Product product) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", product.getId());
        row.put("ten_san_pham", product.getTenSanPham());
        row.put("gia_ban", product.getGiaBan() == null ? 0 : product.getGiaBan());
        row.put("anh_url", product.getAnhUrl());
        row.put("danh_muc_id", product.getDanhMucId());
        return row;
    }

    private void persistOrder(DonHang order, boolean updateCustomerStats) throws SQLException {
        try (Connection connection = DatabaseConfig.getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            try {
                persistOrderInConnection(connection, order, updateCustomerStats);
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private void persistOrderInConnection(Connection connection, DonHang order, boolean updateCustomerStats)
            throws SQLException {
        int orderId = orderDao.insertOrder(connection, order);
        if (updateCustomerStats) {
            resolveCustomerForCompletedOrder(connection, order, orderId);
        } else {
            linkExistingCustomer(connection, order, orderId);
        }
        for (OrderItem item : order.getItems()) {
            normalizeItemPricing(item);
            int detailId = orderDao.insertOrderItem(connection, orderId, item);
            for (OrderChoice choice : item.getLuaChonList()) {
                if (choice.getNhomLuaChonId() > 0 && choice.getLuaChonId() > 0) {
                    orderDao.insertOrderChoice(connection, detailId, choice);
                }
            }
        }
    }

    private void linkExistingCustomer(Connection connection, DonHang order, int orderId) throws SQLException {
        String customerName = order.getTenKhachHang();
        String customerPhone = order.getSdtKhachHang();

        if (customerName == null || customerName.isBlank() || "Khach le".equalsIgnoreCase(customerName)
                || "Khách lẻ".equalsIgnoreCase(customerName)) {
            return;
        }

        Optional<Integer> customerId = Optional.empty();
        if (customerPhone != null && !customerPhone.isBlank()) {
            customerId = orderDao.findCustomerIdByPhone(connection, customerPhone);
        }
        if (customerId.isEmpty()) {
            customerId = orderDao.findCustomerIdByName(connection, customerName);
        }

        if (customerId.isPresent()) {
            orderDao.updateKhachHangId(connection, orderId, customerId.get());
        }
    }

    private void resolveCustomerForCompletedOrder(Connection connection, DonHang order, int orderId) throws SQLException {
        String customerName = order.getTenKhachHang();
        String customerPhone = order.getSdtKhachHang();

        if (customerName == null || customerName.isBlank() || "Khach le".equalsIgnoreCase(customerName)
                || "Khách lẻ".equalsIgnoreCase(customerName)) {
            return;
        }

        Optional<Integer> customerId = Optional.empty();
        if (customerPhone != null && !customerPhone.isBlank()) {
            customerId = orderDao.findCustomerIdByPhone(connection, customerPhone);
        }
        if (customerId.isEmpty()) {
            customerId = orderDao.findCustomerIdByName(connection, customerName);
        }

        if (customerId.isPresent()) {
            khachHangDao.incrementSpendingAndOrderCount(connection, customerId.get(), order.getThanhTien());
            khachHangDao.recalculateCustomerRank(connection, customerId.get());
            orderDao.updateKhachHangId(connection, orderId, customerId.get());
            return;
        }

        if (customerPhone != null && !customerPhone.isBlank()) {
            KhachHang customer = new KhachHang();
            customer.setTenKhachHang(customerName);
            customer.setSoDienThoai(customerPhone);
            customer.setTongDonHang(1);
            customer.setTongTienDaChi(order.getThanhTien());
            customer.setLoaiThanhVien(getCustomerRankFromAmount(order.getThanhTien()));
            int newCustomerId = khachHangDao.insert(connection, customer);
            orderDao.updateKhachHangId(connection, orderId, newCustomerId);
        }
    }

    private String normalizePaymentMethod(String method) {
        if (AppConstants.PAYMENT_BANK.equals(method) || AppConstants.PAYMENT_CARD.equals(method)
                || AppConstants.PAYMENT_APP_ONLINE.equals(method)) {
            return method;
        }
        return AppConstants.PAYMENT_CASH;
    }

    private String getCustomerRankFromAmount(BigDecimal amount) {
        BigDecimal safeAmount = amount == null ? BigDecimal.ZERO : amount;
        if (safeAmount.compareTo(BigDecimal.valueOf(5_000_000)) >= 0) {
            return "vip";
        }
        if (safeAmount.compareTo(BigDecimal.valueOf(2_000_000)) >= 0) {
            return "vang";
        }
        if (safeAmount.compareTo(BigDecimal.valueOf(500_000)) >= 0) {
            return "bac";
        }
        return "thuong";
    }

    private void normalizeItemPricing(OrderItem item) throws SQLException {
        BigDecimal unitPrice = BigDecimal.ZERO;

        Optional<Product> product = productDao.findById(item.getSanPhamId());
        if (product.isPresent()) {
            Product found = product.get();
            item.setTenSanPham(found.getTenSanPham());
            unitPrice = found.getGiaBan() == null ? BigDecimal.ZERO : found.getGiaBan();
        } else if (item.getDonGia() != null) {
            unitPrice = item.getDonGia();
        }

        if (item.getSizeId() != null) {
            Optional<ProductSize> size = productDao.findSizeById(item.getSizeId(), item.getSanPhamId());
            if (size.isPresent()) {
                ProductSize productSize = size.get();
                item.setSize(productSize.getTenSize());
                unitPrice = unitPrice.add(productSize.getGia() == null ? BigDecimal.ZERO : productSize.getGia());
            }
        }

        if (item.getLuaChonList() != null) {
            for (OrderChoice choice : item.getLuaChonList()) {
                unitPrice = unitPrice.add(choice.getGiaTangThem() == null ? BigDecimal.ZERO : choice.getGiaTangThem());
            }
        }

        item.setDonGia(unitPrice);
        item.setThanhTien(unitPrice.multiply(BigDecimal.valueOf(item.getSoLuong())));
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IncomingBatchOrder {
        public String ten_khachhang;
        public String sdt_khachhang;
        public String phuong_thuc_thanhtoan;
        public List<IncomingOrderItem> items = new ArrayList<>();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IncomingOrderItem {
        public int id;
        public String ten;
        public String size;
        public Integer size_id;
        public int sl;
        public BigDecimal gia;
        public List<IncomingChoice> choices = new ArrayList<>();

        public Integer getSizeId() {
            return size_id;
        }

        public List<OrderChoice> toChoices() {
            List<OrderChoice> result = new ArrayList<>();
            if (choices == null) {
                return result;
            }
            for (IncomingChoice choice : choices) {
                OrderChoice item = new OrderChoice();
                item.setNhomLuaChonId(choice.nhom_id);
                item.setLuaChonId(choice.lua_chon_id);
                item.setTenNhom(choice.nhom_ten);
                item.setTenLuaChon(choice.lua_chon_ten);
                item.setGiaTangThem(choice.gia_tang_them == null ? BigDecimal.ZERO : choice.gia_tang_them);
                result.add(item);
            }
            return result;
        }
    }

    public static class IncomingChoice {
        public int nhom_id;
        public int lua_chon_id;
        public String nhom_ten;
        public String lua_chon_ten;
        public BigDecimal gia_tang_them;
    }
}
