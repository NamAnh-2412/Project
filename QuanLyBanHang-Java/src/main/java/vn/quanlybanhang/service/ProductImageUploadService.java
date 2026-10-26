package vn.quanlybanhang.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import vn.quanlybanhang.config.UploadConfig;
import vn.quanlybanhang.dao.ProductDao;
import vn.quanlybanhang.model.Product;

import jakarta.servlet.http.Part;

public class ProductImageUploadService {

    private final ProductDao productDao = new ProductDao();

    public Map<String, Object> uploadProductImage(Part part, int productId, String contextPath)
            throws IOException, SQLException {
        if (part == null || part.getSize() == 0) {
            return Map.of("success", false, "error", "Không có file được upload");
        }

        String contentType = part.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Map.of("success", false, "error", "File tải lên phải là hình ảnh");
        }

        ImageFiles.Prepared image;
        try {
            image = ImageFiles.prepare(part);
        } catch (IOException ex) {
            return Map.of("success", false, "error", ex.getMessage());
        }

        Path uploadDir = UploadConfig.getProductUploadDir();
        Files.createDirectories(uploadDir);
        String fileName = "product-" + (productId > 0 ? productId + "-" : "") + UUID.randomUUID() + image.extension();
        Path target = uploadDir.resolve(fileName);
        Files.write(target, image.bytes());

        String relativePath = UploadConfig.toStoredPath(target);
        if (productId > 0) {
            Optional<Product> product = productDao.findById(productId);
            if (product.isPresent()) {
                Product updated = product.get();
                updated.setAnhUrl(relativePath);
                productDao.updateProduct(updated);
            }
        }

        return Map.of(
                "success", true,
                "path", relativePath,
                "url", contextPath + "/" + relativePath);
    }
}
