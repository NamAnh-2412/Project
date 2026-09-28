package vn.quanlybanhang.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import jakarta.servlet.ServletContext;

public final class UploadConfig {

    private static volatile Path uploadRoot;
    private static volatile ServletContext servletContext;

    private UploadConfig() {
    }

    public static synchronized void initialize(ServletContext context) {
        if (uploadRoot != null) {
            return;
        }

        servletContext = context;
        uploadRoot = resolveUploadRoot();
        createStandardDirectories();
        migrateLegacyUploads(context);
    }

    public static Path getUploadRoot() {
        if (uploadRoot == null) {
            uploadRoot = resolveUploadRoot();
            createStandardDirectories();
        }
        return uploadRoot;
    }

    public static Path getProductUploadDir() {
        return getUploadRoot().resolve("images").resolve("products");
    }

    public static Path getShopUploadDir() {
        return getUploadRoot().resolve("images").resolve("shop");
    }

    /**
     * Chuyen duong dan luu trong DB (uploads/images/...) thanh file tren o dia.
     */
    public static Path resolveStoredPath(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) {
            return null;
        }
        String normalized = storedPath.replace('\\', '/').replaceAll("^/+", "");
        if (normalized.startsWith("uploads/")) {
            normalized = normalized.substring("uploads/".length());
        }
        Path resolved = getUploadRoot().resolve(normalized).normalize();
        if (!resolved.startsWith(getUploadRoot())) {
            return null;
        }
        return resolved;
    }

    public static String toStoredPath(Path absoluteFile) {
        Path normalized = absoluteFile.normalize();
        Path root = getUploadRoot().normalize();
        if (!normalized.startsWith(root)) {
            throw new IllegalArgumentException("File nam ngoai thu muc upload.");
        }
        return "uploads/" + root.relativize(normalized).toString().replace('\\', '/');
    }

    public static String buildPublicUrl(String contextPath, String storedPath) {
        if (storedPath == null || storedPath.isBlank()) {
            return "";
        }
        String normalized = storedPath.replace('\\', '/').replaceAll("^/+", "");
        String ctx = contextPath == null ? "" : contextPath;
        return ctx + "/" + normalized;
    }

    private static Path resolveUploadRoot() {
        String configured = getSetting("UPLOAD_DIR");
        if (configured != null && !configured.isBlank()) {
            return Paths.get(configured.trim()).toAbsolutePath().normalize();
        }

        Path projectRoot = findProjectRoot();
        if (projectRoot != null) {
            return projectRoot.resolve("uploads").toAbsolutePath().normalize();
        }

        return Paths.get(System.getProperty("user.home"), "QuanLyBanHang", "uploads")
                .toAbsolutePath()
                .normalize();
    }

    /**
     * Tim thu muc goc Maven (co pom.xml) de dat uploads/ ngay trong project.
     */
    private static Path findProjectRoot() {
        List<Path> candidates = new ArrayList<>();
        if (servletContext != null) {
            String realPath = servletContext.getRealPath("/");
            if (realPath != null && !realPath.isBlank()) {
                candidates.add(Paths.get(realPath).toAbsolutePath().normalize());
            }
        }
        candidates.add(Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath().normalize());

        for (Path start : candidates) {
            Path dir = start;
            while (dir != null) {
                if (Files.isRegularFile(dir.resolve("pom.xml"))) {
                    return dir;
                }
                dir = dir.getParent();
            }
        }
        return null;
    }

    private static void createStandardDirectories() {
        try {
            Files.createDirectories(getProductUploadDir());
            Files.createDirectories(getShopUploadDir());
        } catch (IOException ex) {
            throw new IllegalStateException("Khong tao duoc thu muc upload: " + ex.getMessage(), ex);
        }
    }

    private static void migrateLegacyUploads(ServletContext servletContext) {
        List<Path> legacyRoots = List.of(
                Paths.get(System.getProperty("user.home"), "QuanLyBanHang", "uploads"),
                Paths.get("c:", "xampp", "htdocs", "QuanLyBanHang", "uploads"),
                servletContext.getRealPath("/") == null ? null : Paths.get(servletContext.getRealPath("/"), "uploads"),
                Paths.get(System.getProperty("user.dir", "."), "uploads")
        );

        for (Path legacyRoot : legacyRoots) {
            if (legacyRoot == null || !Files.isDirectory(legacyRoot)) {
                continue;
            }
            copyTreeIfMissing(legacyRoot.resolve("images"), getUploadRoot().resolve("images"));
        }
    }

    private static void copyTreeIfMissing(Path sourceRoot, Path targetRoot) {
        if (!Files.isDirectory(sourceRoot)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(Files::isRegularFile).forEach(source -> {
                Path relative = sourceRoot.relativize(source);
                Path target = targetRoot.resolve(relative);
                try {
                    if (!Files.exists(target)) {
                        Files.createDirectories(target.getParent());
                        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException ex) {
                    // Bo qua file loi, khong chan khoi dong app
                }
            });
        } catch (IOException ex) {
            // Bo qua thu muc legacy loi
        }
    }

    private static String getSetting(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            value = System.getProperty(key);
        }
        return value;
    }
}
