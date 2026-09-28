package vn.quanlybanhang.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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

        String extension = getExtension(part);
        Path uploadDir = UploadConfig.getShopUploadDir();
        Files.createDirectories(uploadDir);
        String fileName = "logo-" + UUID.randomUUID() + extension;
        Path target = uploadDir.resolve(fileName);
        Files.copy(part.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

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

    private static String getExtension(Part part) {
        String submitted = part.getSubmittedFileName();
        if (submitted == null || !submitted.contains(".")) {
            return ".png";
        }
        return submitted.substring(submitted.lastIndexOf('.'));
    }
}
