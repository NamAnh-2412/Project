package vn.quanlybanhang.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import vn.quanlybanhang.model.Product;
import vn.quanlybanhang.service.ImageFiles;
import vn.quanlybanhang.service.ProductImageUploadService;
import vn.quanlybanhang.service.ProductService;
import vn.quanlybanhang.service.ProductService.ChoiceLinkPageData;
import vn.quanlybanhang.service.ProductService.ProductEditData;
import vn.quanlybanhang.service.ProductService.ProductPageData;
import vn.quanlybanhang.util.JsonUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet(urlPatterns = {
        "/san-pham",
        "/san-pham/edit",
        "/san-pham/lien-ket-lua-chon",
        "/api/product-image-upload"
})
@MultipartConfig(maxFileSize = ImageFiles.MAX_BYTES, maxRequestSize = ImageFiles.MAX_BYTES + 1_048_576L)
public class ProductController extends BaseController {

    private static final long serialVersionUID = 1L;

    private final ProductService productService = new ProductService();
    private final ProductImageUploadService productImageUploadService = new ProductImageUploadService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        try {
            if ("/san-pham/lien-ket-lua-chon".equals(path)) {
                showChoiceLinkPage(request, response);
            } else if ("/san-pham/edit".equals(path)) {
                showEditPage(request, response);
            } else {
                showPage(request, response, new Product(), null);
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
            if ("/api/product-image-upload".equals(path)) {
                uploadProductImage(request, response);
                return;
            }
            if ("/san-pham/lien-ket-lua-chon".equals(path)) {
                updateChoiceLinks(request, response);
                return;
            }

            String action = request.getParameter("action");
            switch (action) {
                case "save" -> save(request, response);
                case "delete" -> delete(request, response);
                default -> redirect(request, response, "/san-pham");
            }
        } catch (SQLException ex) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    private void showPage(HttpServletRequest request, HttpServletResponse response, Product product, String error)
            throws SQLException, ServletException, IOException {
        ProductPageData pageData = productService.loadProductPage(request.getParameter("kw"), request.getParameter("cat"));
        applyProductPageAttributes(request, product, error, pageData, null);
        render(request, response, "/WEB-INF/views/sanpham/index.jsp");
    }

    private void showEditPage(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        Optional<ProductEditData> pageData = productService.loadProductEditPage(getIntParameter(request, "id", 0));
        if (pageData.isEmpty()) {
            flashAndRedirect(request, response, "error", "Không tìm thấy sản phẩm.");
            return;
        }
        ProductEditData data = pageData.get();
        applyProductPageAttributes(request, data.product(), null, data, data.sizes());
        render(request, response, "/WEB-INF/views/sanpham/index.jsp");
    }

    private void showChoiceLinkPage(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        Optional<ChoiceLinkPageData> pageData = productService.loadChoiceLinkPage(getIntParameter(request, "id", 0));
        if (pageData.isEmpty()) {
            flashAndRedirect(request, response, "error", "Không tìm thấy sản phẩm.");
            return;
        }

        request.setAttribute("pageTitle", "Liên kết nhóm lựa chọn");
        applyPageChrome(request, "fas fa-link", "Gán nhóm lựa chọn cho sản phẩm.", List.of("products.css"));
        ChoiceLinkPageData data = pageData.get();
        request.setAttribute("product", data.product());
        request.setAttribute("choiceGroups", data.choiceGroups());
        request.setAttribute("linkedGroupMap", data.linkedGroupMap());
        request.setAttribute("requiredGroupMap", data.requiredGroupMap());
        render(request, response, "/WEB-INF/views/sanpham/lienket_luachon.jsp");
    }

    private void updateChoiceLinks(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        int productId = getIntParameter(request, "product_id", 0);
        if (productId <= 0) {
            productId = getIntParameter(request, "id", 0);
        }
        Optional<ChoiceLinkPageData> pageData = productService.loadChoiceLinkPage(productId);
        if (pageData.isEmpty()) {
            flashAndRedirect(request, response, "error", "Không tìm thấy sản phẩm.");
            return;
        }

        productService.updateChoiceLinks(productId,
                productService.parseIntegerList(request.getParameterValues("nhom_lua_chon")),
                productService.parseIntegerList(request.getParameterValues("bat_buoc")));
        setFlash(request, "success", "Cập nhật liên kết nhóm lựa chọn thành công.");
        redirect(request, response, "/san-pham/lien-ket-lua-chon?id=" + productId);
    }

    private void uploadProductImage(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException, SQLException {
        try {
            int productId = getIntParameter(request, "product_id", 0);
            Part part = request.getPart("anh_url");
            if (part == null) {
                part = request.getPart("product_image");
            }
            Map<String, Object> result = productImageUploadService.uploadProductImage(part, productId, request.getContextPath());
            JsonUtil.writeJson(response, result);
        } catch (IllegalStateException ex) {
            JsonUtil.writeJson(response, Map.of("success", false, "error", "Ảnh không được lớn hơn 5MB."));
        }
    }

    private void save(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        Product product = bindProduct(request);
        int id = getIntParameter(request, "id", 0);
        var error = productService.saveProduct(
                product,
                id,
                productService.bindSizes(request.getParameterValues("size_name"), request.getParameterValues("size_price")),
                productService.parseIntegerList(request.getParameterValues("nhom_lua_chon")),
                productService.parseIntegerList(request.getParameterValues("bat_buoc")));
        if (error.isPresent()) {
            showPage(request, response, product, error.get());
            return;
        }

        setFlash(request, "success", id > 0 ? "Cập nhật sản phẩm thành công." : "Thêm sản phẩm thành công.");
        redirect(request, response, "/san-pham");
    }

    private void delete(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        String message = productService.deleteProduct(getIntParameter(request, "id", 0));
        setFlash(request, "success", message);
        redirect(request, response, "/san-pham");
    }

    private Product bindProduct(HttpServletRequest request) {
        return productService.bindProduct(
                paramOrEmpty(request, "ma_sanpham"),
                paramOrEmpty(request, "ten_sanpham"),
                getIntParameter(request, "danhmuc_id", 0),
                paramOrEmpty(request, "gia_ban"),
                request.getParameter("dang_hoat_dong"),
                paramOrEmpty(request, "anh_url"),
                paramOrEmpty(request, "mota"));
    }

    private void applyProductPageAttributes(HttpServletRequest request, Product product, String error,
                                            ProductPageData pageData, List<?> sizes) {
        applyProductPageAttributes(request, product, error,
                pageData.products(),
                pageData.categories(),
                pageData.choiceGroups(),
                pageData.linkedGroupMap(),
                pageData.requiredGroupMap(),
                sizes);
    }

    private void applyProductPageAttributes(HttpServletRequest request, Product product, String error,
                                            ProductEditData pageData, List<?> sizes) {
        applyProductPageAttributes(request, product, error,
                pageData.products(),
                pageData.categories(),
                pageData.choiceGroups(),
                pageData.linkedGroupMap(),
                pageData.requiredGroupMap(),
                sizes);
    }

    private void applyProductPageAttributes(HttpServletRequest request, Product product, String error,
                                            List<?> products, List<?> categories, List<?> choiceGroups,
                                            Map<Integer, Boolean> linkedGroupMap,
                                            Map<Integer, Boolean> requiredGroupMap, List<?> sizes) {
        request.setAttribute("pageTitle", "Sản phẩm");
        applyPageChrome(request, "fas fa-box-open",
                "Quản lý sản phẩm, size và liên kết nhóm lựa chọn sát hơn với bản PHP.", List.of("products.css"));
        request.setAttribute("product", product);
        request.setAttribute("error", error);
        request.setAttribute("products", products);
        request.setAttribute("categories", categories);
        request.setAttribute("choiceGroups", choiceGroups);
        request.setAttribute("linkedGroupMap", linkedGroupMap);
        request.setAttribute("requiredGroupMap", requiredGroupMap);
        if (sizes != null) {
            request.setAttribute("sizes", sizes);
        }
    }

    private void flashAndRedirect(HttpServletRequest request, HttpServletResponse response, String type, String message)
            throws IOException {
        setFlash(request, type, message);
        redirect(request, response, "/san-pham");
    }
}
