package vn.quanlybanhang.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import vn.quanlybanhang.dao.BranchDao;
import vn.quanlybanhang.dao.DisplayDao;
import vn.quanlybanhang.dao.ForecastDao;
import vn.quanlybanhang.dao.InvoiceDao;
import vn.quanlybanhang.dao.KitchenDao;
import vn.quanlybanhang.dao.ProductDao;
import vn.quanlybanhang.dao.SettingsDao;
import vn.quanlybanhang.dao.TableDao;
import vn.quanlybanhang.model.LuaChon;
import vn.quanlybanhang.model.NhomLuaChon;
import vn.quanlybanhang.model.Product;
import vn.quanlybanhang.model.ProductSize;
import vn.quanlybanhang.service.OrderService.CreateOrderResult;
import vn.quanlybanhang.service.OrderService.IncomingOrderItem;

public class BranchService {

    private final BranchDao branchDao = new BranchDao();

    public List<Map<String, Object>> branches() throws SQLException {
        branchDao.seedBranchStockFromShop();
        return branchDao.listBranches();
    }

    public List<Map<String, Object>> branchStock() throws SQLException {
        branchDao.seedBranchStockFromShop();
        return branchDao.branchStock();
    }

    public Optional<String> addBranch(String code, String name, String address, String phone) throws SQLException {
        if (code == null || code.isBlank() || name == null || name.isBlank()) {
            return Optional.of("Nhập mã và tên chi nhánh.");
        }
        branchDao.insertBranch(code.trim().toUpperCase(), name.trim(), address, phone);
        return Optional.empty();
    }

    public void transfer(int fromId, int toId, int materialId, BigDecimal quantity, int staffId, String note)
            throws SQLException {
        if (fromId <= 0 || toId <= 0 || fromId == toId || materialId <= 0 || quantity == null || quantity.signum() <= 0) {
            throw new SQLException("Phiếu chuyển kho chưa đủ thông tin.");
        }
        branchDao.transferStock(fromId, toId, materialId, quantity, staffId, note);
    }

}
