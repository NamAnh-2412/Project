package vn.quanlybanhang;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import vn.quanlybanhang.config.DatabaseConfig;
import vn.quanlybanhang.dao.KhachHangDao;
import vn.quanlybanhang.dao.NhanVienDao;
import vn.quanlybanhang.dao.NguyenVatLieuDao;
import vn.quanlybanhang.dao.OrderDao;
import vn.quanlybanhang.dao.ProductDao;
import vn.quanlybanhang.dao.ReportDao;
import vn.quanlybanhang.dao.SettingsDao;
import vn.quanlybanhang.dao.SupplierDao;
import vn.quanlybanhang.dao.WarehouseDao;
import vn.quanlybanhang.model.DonHang;
import vn.quanlybanhang.model.NhomLuaChon;
import vn.quanlybanhang.model.NguyenVatLieu;
import vn.quanlybanhang.model.NhanVien;
import vn.quanlybanhang.model.OrderChoice;
import vn.quanlybanhang.model.OrderItem;
import vn.quanlybanhang.model.Product;
import vn.quanlybanhang.model.ProductSize;
import vn.quanlybanhang.model.Supplier;
import vn.quanlybanhang.service.OrderService;
import vn.quanlybanhang.service.WarehouseService;

class RuntimeSmokeTest {

    @Test
    void databaseConnectionWorks() {
        assertDoesNotThrow(() -> {
            try (Connection connection = DatabaseConfig.getConnection()) {
                assertNotNull(connection);
                assertFalse(connection.isClosed());
            }
        });
    }

    @Test
    void settingsModuleWorks() {
        assertDoesNotThrow(() -> {
            SettingsDao dao = new SettingsDao();
            Map<String, String> settings = dao.getAllSettings();
            assertNotNull(settings);
            assertTrue(settings.containsKey("ten_cua_hang"));
        });
    }

    @Test
    void employeeModuleWorks() {
        assertDoesNotThrow(() -> {
            NhanVienDao dao = new NhanVienDao();
            List<NhanVien> employees = dao.findAll();
            assertNotNull(employees);
            assertFalse(employees.isEmpty(), "Expected at least one employee from the real database");
            assertNotNull(dao.findById(employees.get(0).getId()));
        });
    }

    @Test
    void productModuleWorks() {
        assertDoesNotThrow(() -> {
            ProductDao dao = new ProductDao();
            List<Product> products = dao.search(null, null);
            assertNotNull(products);
            assertFalse(products.isEmpty(), "Expected at least one product from the real database");

            Product first = products.get(0);
            assertTrue(dao.findById(first.getId()).isPresent());
            assertNotNull(dao.findSizesByProductId(first.getId()));
            assertNotNull(dao.findActiveSizesByProductId(first.getId()));
            assertNotNull(dao.findChoiceGroupsByProductId(first.getId()));
        });
    }

    @Test
    void orderModuleWorks() {
        assertDoesNotThrow(() -> {
            OrderDao dao = new OrderDao();
            List<DonHang> orders = dao.searchOrders(null, null, null, null, null);
            assertNotNull(orders);
            if (!orders.isEmpty()) {
                assertTrue(dao.findById(orders.get(0).getId()).isPresent());
            }
        });
    }

    @Test
    void orderCreationPersistsToDatabase() {
        assertDoesNotThrow(() -> {
            ProductDao productDao = new ProductDao();
            OrderService orderService = new OrderService();

            List<Product> products = productDao.search(null, null);
            assertFalse(products.isEmpty(), "Expected at least one product to create an order");
            Product product = products.get(0);

            OrderItem item = new OrderItem();
            item.setSanPhamId(product.getId());
            item.setTenSanPham(product.getTenSanPham());
            item.setSoLuong(1);
            item.setDonGia(product.getGiaBan());

            List<ProductSize> sizes = productDao.findActiveSizesByProductId(product.getId());
            if (!sizes.isEmpty()) {
                item.setSizeId(sizes.get(0).getId());
            }

            List<NhomLuaChon> groups = productDao.findChoiceGroupsByProductId(product.getId());
            for (NhomLuaChon group : groups) {
                if (group.getLuaChonList() != null && !group.getLuaChonList().isEmpty()) {
                    OrderChoice choice = new OrderChoice();
                    choice.setNhomLuaChonId(group.getId());
                    choice.setLuaChonId(group.getLuaChonList().get(0).getId());
                    choice.setTenNhom(group.getTenNhom());
                    choice.setTenLuaChon(group.getLuaChonList().get(0).getTenLuaChon());
                    choice.setGiaTangThem(group.getLuaChonList().get(0).getGiaTangThem());
                    item.getLuaChonList().add(choice);
                    break;
                }
            }

            DonHang order = new DonHang();
            order.setMaDonHang("TEST" + System.currentTimeMillis());
            order.setTenKhachHang("Khach le");
            order.setSdtKhachHang("");
            order.setPhuongThucThanhToan("tien_mat");
            order.setHinhThucNhan("tai_cho");
            order.setTrangThai("hoan_thanh");
            order.setGiamGia(java.math.BigDecimal.ZERO);
            order.setItems(List.of(item));
            order.setTongTien(orderService.calculateTotal(order.getItems()));
            order.setThanhTien(order.getTongTien());

            orderService.createOrder(order);

            Integer orderId = null;
            Integer detailId = null;
            try (Connection connection = DatabaseConfig.getConnection()) {
                try (PreparedStatement orderStatement = connection.prepareStatement(
                        "SELECT id FROM donhang WHERE ma_donhang = ?")) {
                    orderStatement.setString(1, order.getMaDonHang());
                    try (ResultSet resultSet = orderStatement.executeQuery()) {
                        assertTrue(resultSet.next(), "Inserted order should exist in database");
                        orderId = resultSet.getInt("id");
                    }
                }

                try (PreparedStatement detailStatement = connection.prepareStatement(
                        "SELECT id FROM chitiet_donhang WHERE donhang_id = ?")) {
                    detailStatement.setInt(1, orderId);
                    try (ResultSet resultSet = detailStatement.executeQuery()) {
                        assertTrue(resultSet.next(), "Inserted order item should exist in database");
                        detailId = resultSet.getInt("id");
                    }
                }

                if (!item.getLuaChonList().isEmpty()) {
                    try (PreparedStatement choiceStatement = connection.prepareStatement(
                            "SELECT COUNT(*) FROM chitiet_donhang_lua_chon WHERE chitiet_donhang_id = ?")) {
                        choiceStatement.setInt(1, detailId);
                        try (ResultSet resultSet = choiceStatement.executeQuery()) {
                            assertTrue(resultSet.next());
                            assertTrue(resultSet.getInt(1) > 0, "Inserted order choice should exist in database");
                        }
                    }
                }
            } finally {
                if (orderId != null) {
                    try (Connection cleanupConnection = DatabaseConfig.getConnection()) {
                        cleanupConnection.setAutoCommit(false);
                        try (PreparedStatement deleteChoices = cleanupConnection.prepareStatement(
                                "DELETE FROM chitiet_donhang_lua_chon WHERE chitiet_donhang_id IN (SELECT id FROM chitiet_donhang WHERE donhang_id = ?)");
                             PreparedStatement deleteItems = cleanupConnection.prepareStatement(
                                     "DELETE FROM chitiet_donhang WHERE donhang_id = ?");
                             PreparedStatement deleteOrder = cleanupConnection.prepareStatement(
                                     "DELETE FROM donhang WHERE id = ?")) {
                            deleteChoices.setInt(1, orderId);
                            deleteChoices.executeUpdate();
                            deleteItems.setInt(1, orderId);
                            deleteItems.executeUpdate();
                            deleteOrder.setInt(1, orderId);
                            deleteOrder.executeUpdate();
                            cleanupConnection.commit();
                        } catch (Exception ex) {
                            cleanupConnection.rollback();
                            throw ex;
                        } finally {
                            cleanupConnection.setAutoCommit(true);
                        }
                    }
                }
            }
        });
    }

    @Test
    void reportModuleWorks() {
        assertDoesNotThrow(() -> {
            ReportDao dao = new ReportDao();
            String from = LocalDate.now().minusYears(5).toString();
            String to = LocalDate.now().plusDays(1).toString();

            assertNotNull(dao.getTongHop(from, to));
            assertNotNull(dao.getHourly(from, to));
            assertNotNull(dao.getPaymentDetails(from, to));
            assertNotNull(dao.getDailyRevenue(from, to));
        });
    }

    /**
     * Xoa va tinh lai bang daily_revenue tu don hoan thanh (giong POST PHP rebuild API / script CLI).
     * Chi chay khi bang ton tai; bo qua neu DB dev khong co migration.
     */
    @Test
    void reportDailyRevenueRebuildRunsWhenTableExists() {
        assertDoesNotThrow(() -> {
            ReportDao dao = new ReportDao();
            if (!dao.tableExists("daily_revenue")) {
                return;
            }
            int inserted = dao.rebuildDailyRevenue();
            assertTrue(inserted >= 0);
            String from = LocalDate.now().minusYears(5).toString();
            String to = LocalDate.now().plusDays(1).toString();
            assertNotNull(dao.getDailyRevenue(from, to));
        });
    }

    /**
     * Dong bo canh bao HSD (cung y nghia scripts/check_expiry_alerts.php).
     */
    @Test
    void expiryAlertSyncRunsWhenTablesExist() {
        assertDoesNotThrow(() -> {
            WarehouseDao dao = new WarehouseDao();
            if (!dao.tableExists("kho_lot") || !dao.tableExists("expiry_alerts")) {
                return;
            }
            WarehouseService service = new WarehouseService();
            int inserted = service.syncExpiryAlerts();
            assertTrue(inserted >= 0);
        });
    }

    @Test
    void customerAndSupplierModulesWork() {
        assertDoesNotThrow(() -> {
            KhachHangDao khachHangDao = new KhachHangDao();
            assertNotNull(khachHangDao.findAll());

            SupplierDao supplierDao = new SupplierDao();
            assertNotNull(supplierDao.search(null, null));
            assertNotNull(supplierDao.search("", "dang_hop_tac"));

            List<Supplier> suppliers = supplierDao.search(null, null);
            if (!suppliers.isEmpty()) {
                int id = suppliers.get(0).getId();
                assertTrue(supplierDao.findById(id).isPresent());
                assertNotNull(supplierDao.findMaterialsBySupplierId(id));
                assertNotNull(supplierDao.findImportHistoryBySupplierId(id));
                assertNotNull(supplierDao.getSupplierViewStats(id));
            }
        });
    }

    /**
     * CRUD nguyen lieu + lien ket NCC: insert/update xoa {@code nha_cung_cap_id}, join ten NCC.
     * Bo qua neu bang khong doc duoc, khong co cot {@code nha_cung_cap_id}, hoac khong co NCC.
     */
    @Test
    void materialCrudLinksSupplierWhenColumnExists() throws SQLException {
        NguyenVatLieuDao materialDao = new NguyenVatLieuDao();
        try {
            materialDao.findAll();
        } catch (SQLException ex) {
            assumeTrue(false, "Bo qua: bang nguyenvatlieu — " + ex.getMessage());
        }
        assumeTrue(materialDao.hasSupplierColumn(), "Bo qua: khong co cot nha_cung_cap_id");
        SupplierDao supplierDao = new SupplierDao();
        List<Supplier> suppliers = supplierDao.search(null, null);
        assumeFalse(suppliers.isEmpty(), "Bo qua: khong co nha cung cap trong DB");

        String ma = "SMK_NL_" + System.currentTimeMillis();
        NguyenVatLieu material = new NguyenVatLieu();
        material.setMaNguyenLieu(ma);
        material.setTenNguyenLieu("Smoke test NL");
        material.setDanhMucNl("khác");
        material.setDonViTinh("g");
        material.setTrangThai("con_hang");
        material.setSoLuongTon(BigDecimal.ZERO);
        material.setSoLuongToiThieu(new BigDecimal("10"));
        material.setGiaNhap(BigDecimal.ZERO);
        material.setNhaCungCapId(suppliers.get(0).getId());

        materialDao.insert(material);

        NguyenVatLieu loaded = null;
        for (NguyenVatLieu row : materialDao.findAll()) {
            if (ma.equals(row.getMaNguyenLieu())) {
                loaded = row;
                break;
            }
        }
        assertNotNull(loaded, "Phai tim thay ban ghi vua them theo ma");
        assertNotNull(loaded.getNhaCungCapId());
        assertEquals(suppliers.get(0).getId(), loaded.getNhaCungCapId());
        assertNotNull(loaded.getTenNhaCungCap());

        loaded.setTenNguyenLieu("Smoke test NL (updated)");
        loaded.setNhaCungCapId(null);
        materialDao.update(loaded);

        assertTrue(materialDao.findById(loaded.getId()).isPresent());
        assertNull(materialDao.findById(loaded.getId()).get().getNhaCungCapId());

        int idToDelete = loaded.getId();
        materialDao.delete(idToDelete);
        assertTrue(materialDao.findById(idToDelete).isEmpty());
    }

    @Test
    void warehouseModuleWorks() {
        assertDoesNotThrow(() -> {
            WarehouseDao dao = new WarehouseDao();
            assertNotNull(dao.search(null, null, null));
            assertNotNull(dao.getExpiryStats());
            if (dao.tableExists("kho_lot")) {
                assertNotNull(dao.getExpiryLots("all", null, null));
            }
        });
    }
}
