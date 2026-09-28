package vn.quanlybanhang.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import vn.quanlybanhang.dao.ChoiceGroupDao;
import vn.quanlybanhang.dao.DanhMucDao;
import vn.quanlybanhang.dao.ProductDao;
import vn.quanlybanhang.model.NhomLuaChon;
import vn.quanlybanhang.model.Product;
import vn.quanlybanhang.model.ProductSize;

public class ProductService {

    private static final DateTimeFormatter PRODUCT_CODE_FORMATTER = DateTimeFormatter.ofPattern("yyMMddHHmmss");

    private final ProductDao productDao = new ProductDao();
    private final DanhMucDao danhMucDao = new DanhMucDao();
    private final ChoiceGroupDao choiceGroupDao = new ChoiceGroupDao();

    public ProductPageData loadProductPage(String keyword, String category) throws SQLException {
        return new ProductPageData(
                productDao.search(keyword, category),
                danhMucDao.findActiveThucDon(),
                filterActiveChoiceGroups(choiceGroupDao.findAllWithChoices()),
                new HashMap<>(),
                new HashMap<>());
    }

    public Optional<ProductEditData> loadProductEditPage(int productId) throws SQLException {
        Optional<Product> product = productDao.findById(productId);
        if (product.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new ProductEditData(
                product.get(),
                productDao.search(null, null),
                danhMucDao.findActiveThucDon(),
                filterActiveChoiceGroups(choiceGroupDao.findAllWithChoices()),
                toFlagMap(productDao.findLinkedChoiceGroupIds(productId)),
                toFlagMap(productDao.findRequiredChoiceGroupIds(productId)),
                productDao.findSizesByProductId(productId)));
    }

    public Optional<ChoiceLinkPageData> loadChoiceLinkPage(int productId) throws SQLException {
        Optional<Product> product = productDao.findById(productId);
        if (product.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new ChoiceLinkPageData(
                product.get(),
                filterActiveChoiceGroups(choiceGroupDao.findAllWithChoices()),
                toFlagMap(productDao.findLinkedChoiceGroupIds(productId)),
                toFlagMap(productDao.findRequiredChoiceGroupIds(productId))));
    }

    public Product bindProduct(String code, String name, int categoryId, String priceRaw, String activeRaw,
                               String imageUrl, String description) {
        Product product = new Product();
        product.setMaSanPham(trimOrNull(code));
        product.setTenSanPham(trimOrNull(name));
        product.setDanhMucId(categoryId);
        product.setGiaBan(toBigDecimal(priceRaw));
        product.setDangHoatDong(!"0".equals(activeRaw));
        product.setAnhUrl(trimOrNull(imageUrl));
        product.setMoTa(trimOrNull(description));
        return product;
    }

    public List<ProductSize> bindSizes(String[] names, String[] prices) {
        List<ProductSize> sizes = new ArrayList<>();
        if (names == null || prices == null) {
            return sizes;
        }
        for (int i = 0; i < Math.min(names.length, prices.length); i++) {
            if (names[i] != null && !names[i].isBlank()) {
                ProductSize size = new ProductSize();
                size.setTenSize(names[i].trim());
                size.setGia(toBigDecimal(prices[i]));
                size.setDangHoatDong(true);
                sizes.add(size);
            }
        }
        return sizes;
    }

    public String validate(Product product) {
        if (product.getTenSanPham() == null || product.getTenSanPham().isBlank()) {
            return "Vui lòng nhập tên sản phẩm.";
        }
        if (product.getDanhMucId() <= 0) {
            return "Vui lòng chọn thực đơn.";
        }
        if (product.getGiaBan() == null || product.getGiaBan().compareTo(BigDecimal.ZERO) < 0) {
            return "Giá bán không hợp lệ.";
        }
        return null;
    }

    public Optional<String> saveProduct(Product product, int id, List<ProductSize> sizes,
                                        List<Integer> linkedGroups, List<Integer> requiredGroups) throws SQLException {
        String error = validate(product);
        if (error != null) {
            return Optional.of(error);
        }

        if (id > 0) {
            product.setId(id);
            productDao.updateProduct(product);
            productDao.replaceChoiceLinks(id, linkedGroups, requiredGroups);
            return Optional.empty();
        }

        if (product.getMaSanPham() == null || product.getMaSanPham().isBlank()) {
            product.setMaSanPham("SP" + LocalDateTime.now().format(PRODUCT_CODE_FORMATTER));
        }
        int productId = productDao.insertProduct(product);
        for (ProductSize size : sizes) {
            productDao.insertSize(productId, size);
        }
        productDao.replaceChoiceLinks(productId, linkedGroups, requiredGroups);
        return Optional.empty();
    }

    public String deleteProduct(int id) throws SQLException {
        if (productDao.productExistsInOrders(id)) {
            productDao.deactivateProduct(id);
            return "Sản phẩm đã được chuyển sang không hoạt động.";
        }
        productDao.deleteChoiceLinks(id);
        productDao.deleteProduct(id);
        return "Đã xóa sản phẩm.";
    }

    public void updateChoiceLinks(int productId, List<Integer> linkedGroups, List<Integer> requiredGroups)
            throws SQLException {
        productDao.replaceChoiceLinks(productId, linkedGroups, requiredGroups);
    }

    public List<Integer> parseIntegerList(String[] values) {
        List<Integer> result = new ArrayList<>();
        if (values == null) {
            return result;
        }
        for (String value : values) {
            try {
                int parsed = Integer.parseInt(value);
                if (parsed > 0) {
                    result.add(parsed);
                }
            } catch (NumberFormatException ignored) {
                // Bo qua id khong hop le tu form.
            }
        }
        return result;
    }

    static List<NhomLuaChon> filterActiveChoiceGroups(List<NhomLuaChon> groups) {
        List<NhomLuaChon> activeGroups = new ArrayList<>();
        for (NhomLuaChon group : groups) {
            if (group.isDangHoatDong()) {
                activeGroups.add(group);
            }
        }
        return activeGroups;
    }

    static Map<Integer, Boolean> toFlagMap(List<Integer> ids) {
        Map<Integer, Boolean> flags = new HashMap<>();
        for (Integer id : ids) {
            flags.put(id, true);
        }
        return flags;
    }

    private static BigDecimal toBigDecimal(String value) {
        try {
            return value == null || value.isBlank() ? BigDecimal.ZERO : new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }

    private static String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public record ProductEditData(
            Product product,
            List<Product> products,
            List<?> categories,
            List<NhomLuaChon> choiceGroups,
            Map<Integer, Boolean> linkedGroupMap,
            Map<Integer, Boolean> requiredGroupMap,
            List<ProductSize> sizes) {
    }

    public record ProductPageData(
            List<Product> products,
            List<?> categories,
            List<NhomLuaChon> choiceGroups,
            Map<Integer, Boolean> linkedGroupMap,
            Map<Integer, Boolean> requiredGroupMap,
            List<ProductSize> sizes) {

        public ProductPageData(List<Product> products, List<?> categories, List<NhomLuaChon> choiceGroups,
                               Map<Integer, Boolean> linkedGroupMap, Map<Integer, Boolean> requiredGroupMap) {
            this(products, categories, choiceGroups, linkedGroupMap, requiredGroupMap, List.of());
        }
    }

    public record ChoiceLinkPageData(
            Product product,
            List<NhomLuaChon> choiceGroups,
            Map<Integer, Boolean> linkedGroupMap,
            Map<Integer, Boolean> requiredGroupMap) {
    }
}
