package vn.quanlybanhang.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import vn.quanlybanhang.config.UploadConfig;
import vn.quanlybanhang.dao.SettingsDao;

import jakarta.servlet.http.Part;

public class LogoUploadService {

    private final SettingsDao settingsDao = new SettingsDao();

    public Map<String, Object> uploadLogo(Part part, String contextPath) throws IOException, SQLException {
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

        Path uploadDir = UploadConfig.getShopUploadDir();
        Files.createDirectories(uploadDir);
        String fileName = "logo-" + UUID.randomUUID() + image.extension();
        Path target = uploadDir.resolve(fileName);
        Files.write(target, image.bytes());

        String relativePath = UploadConfig.toStoredPath(target);
        settingsDao.setSetting("logo_path", relativePath);

        return Map.of(
                "success", true,
                "path", relativePath,
                "url", contextPath + "/" + relativePath);
    }

    public void clearLogo() throws SQLException {
        settingsDao.setSetting("logo_path", "");
    }
}
