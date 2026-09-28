package vn.quanlybanhang.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import vn.quanlybanhang.dao.ProductDao;
import vn.quanlybanhang.model.ProductSize;
import vn.quanlybanhang.util.RequestUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/manage-size")
public class SizeApiController extends BaseApiController {

    private static final long serialVersionUID = 1L;

    private final ProductDao productDao = new ProductDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = RequestUtil.paramOrEmpty(request, "action");
        int productId = RequestUtil.getInt(request, "product_id", 0);

        try {
            switch (action) {
                case "list" -> list(productId, response);
                case "add" -> add(request, response, productId);
                case "update" -> update(request, response, productId);
                case "delete" -> delete(request, response, productId);
                default -> writeError(response, "Action không hợp lệ");
            }
        } catch (SQLException ex) {
            handleSqlError(response, ex);
        }
    }

    private void list(int productId, HttpServletResponse response) throws SQLException, IOException {
        List<ProductSize> sizes = productDao.findSizesByProductId(productId);
        writeSuccess(response, Map.of("data", sizes));
    }

    private void add(HttpServletRequest request, HttpServletResponse response, int productId)
            throws SQLException, IOException {
        String tenSize = RequestUtil.paramOrEmpty(request, "ten_size");
        BigDecimal gia = RequestUtil.getBigDecimal(request, "gia", BigDecimal.ZERO);

        if (tenSize.isBlank()) {
            writeError(response, "Tên size không được để trống");
            return;
        }
        if (gia.compareTo(BigDecimal.ZERO) < 0) {
            writeError(response, "Giá không hợp lệ");
            return;
        }
        if (productDao.sizeNameExists(productId, tenSize, null)) {
            writeError(response, "Tên size đã tồn tại");
            return;
        }

        ProductSize size = new ProductSize();
        size.setTenSize(tenSize);
        size.setGia(gia);
        size.setDangHoatDong(true);
        productDao.insertSize(productId, size);
        writeSuccess(response);
    }

    private void update(HttpServletRequest request, HttpServletResponse response, int productId)
            throws SQLException, IOException {
        int id = RequestUtil.getInt(request, "id", 0);
        Optional<ProductSize> existing = productDao.findSizeById(id, productId);
        if (existing.isEmpty()) {
            writeError(response, "Không tìm thấy size");
            return;
        }

        String tenSize = RequestUtil.paramOrEmpty(request, "ten_size");
        BigDecimal gia = RequestUtil.getBigDecimal(request, "gia", BigDecimal.ZERO);
        if (tenSize.isBlank()) {
            writeError(response, "Tên size không được để trống");
            return;
        }
        if (gia.compareTo(BigDecimal.ZERO) < 0) {
            writeError(response, "Giá không hợp lệ");
            return;
        }
        if (productDao.sizeNameExists(productId, tenSize, id)) {
            writeError(response, "Tên size đã tồn tại");
            return;
        }

        ProductSize size = existing.get();
        size.setTenSize(tenSize);
        size.setGia(gia);
        productDao.updateSize(size);
        writeSuccess(response);
    }

    private void delete(HttpServletRequest request, HttpServletResponse response, int productId)
            throws SQLException, IOException {
        int id = RequestUtil.getInt(request, "id", 0);
        Optional<ProductSize> existing = productDao.findSizeById(id, productId);
        if (existing.isEmpty()) {
            writeError(response, "Không tìm thấy size");
            return;
        }
        if (productDao.sizeExistsInOrders(productId, existing.get().getTenSize())) {
            productDao.softDeleteSize(id);
        } else {
            productDao.deleteSize(id);
        }
        writeSuccess(response);
    }
}
