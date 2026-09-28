package vn.quanlybanhang.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import vn.quanlybanhang.dao.ProductDao;
import vn.quanlybanhang.model.LuaChon;
import vn.quanlybanhang.model.NhomLuaChon;
import vn.quanlybanhang.model.ProductSize;
import vn.quanlybanhang.util.RequestUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {
        "/api/product-choices",
        "/api/product-sizes"
})
public class ProductApiController extends BaseApiController {

    private static final long serialVersionUID = 1L;

    private final ProductDao productDao = new ProductDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        try {
            switch (path) {
                case "/api/product-choices" -> getChoices(request, response);
                case "/api/product-sizes" -> getSizes(request, response);
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException ex) {
            handleSqlError(response, ex);
        }
    }

    private void getChoices(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        int productId = RequestUtil.getInt(request, "id", 0);
        if (productId <= 0) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid product ID");
            return;
        }

        List<Map<String, Object>> choices = new ArrayList<>();
        for (NhomLuaChon group : productDao.findChoiceGroupsByProductId(productId)) {
            Map<String, Object> groupMap = new LinkedHashMap<>();
            groupMap.put("id", group.getId());
            groupMap.put("ten_nhom", group.getTenNhom());
            groupMap.put("mota", group.getMoTa());
            groupMap.put("bat_buoc", group.isBatBuoc());

            List<Map<String, Object>> luaChonList = new ArrayList<>();
            for (LuaChon choice : group.getLuaChonList()) {
                Map<String, Object> choiceMap = new LinkedHashMap<>();
                choiceMap.put("id", choice.getId());
                choiceMap.put("ten_lua_chon", choice.getTenLuaChon());
                choiceMap.put("gia_tang_them", choice.getGiaTangThem() == null ? 0 : choice.getGiaTangThem());
                choiceMap.put("thu_tu", choice.getThuTu());
                luaChonList.add(choiceMap);
            }
            groupMap.put("lua_chon", luaChonList);
            choices.add(groupMap);
        }

        writeSuccess(response, Map.of("choices", choices));
    }

    private void getSizes(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        int productId = RequestUtil.getInt(request, "product_id", 0);
        if (productId <= 0) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "ID sản phẩm không hợp lệ");
            return;
        }

        List<ProductSize> sizes = productDao.findActiveSizesByProductId(productId);
        List<Map<String, Object>> payload = new ArrayList<>();
        for (ProductSize size : sizes) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", size.getId());
            row.put("ten_size", size.getTenSize());
            row.put("gia", size.getGia() == null ? 0 : size.getGia());
            row.put("dang_hoat_dong", size.isDangHoatDong() ? 1 : 0);
            payload.add(row);
        }

        writeSuccess(response, Map.of(
                "sizes", payload,
                "has_sizes", !payload.isEmpty()));
    }
}
