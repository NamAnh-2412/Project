package vn.quanlybanhang.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Set;

import javax.imageio.ImageIO;

import jakarta.servlet.http.Part;

/**
 * Chỉ nhận JPG, PNG, WEBP. Đuôi file lấy từ nội dung ảnh, không lấy từ tên người dùng gửi.
 */
public final class ImageFiles {

    public static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final long MAX_PIXELS = 20_000_000L;
    private static final Set<String> ALLOWED = Set.of("jpg", "jpeg", "png", "webp");

    private ImageFiles() {
    }

    public record Prepared(byte[] bytes, String extension) {
    }

    public static Prepared prepare(Part part) throws IOException {
        if (part == null || part.getSize() <= 0) {
            throw new IOException("Không có file được upload");
        }
        if (part.getSize() > MAX_BYTES) {
            throw new IOException("Ảnh không được lớn hơn 5MB.");
        }
        String submitted = extensionOf(part.getSubmittedFileName());
        if (!ALLOWED.contains(submitted)) {
            throw new IOException("Chỉ nhận ảnh JPG, PNG hoặc WEBP.");
        }
        byte[] data = part.getInputStream().readAllBytes();
        if (data.length == 0 || data.length > MAX_BYTES) {
            throw new IOException("Ảnh không được lớn hơn 5MB.");
        }
        String extension = detect(data);
        if (extension == null) {
            throw new IOException("File không phải ảnh hợp lệ.");
        }
        if (!".webp".equals(extension)) {
            assertRaster(data);
        }
        return new Prepared(data, extension);
    }

    private static String extensionOf(String fileName) {
        if (fileName == null) {
            return "";
        }
        int slash = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
        String base = slash >= 0 ? fileName.substring(slash + 1) : fileName;
        int dot = base.lastIndexOf('.');
        if (dot < 0 || dot == base.length() - 1) {
            return "";
        }
        return base.substring(dot + 1).toLowerCase();
    }

    private static String detect(byte[] data) {
        if (data.length >= 3 && (data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xD8 && (data[2] & 0xFF) == 0xFF) {
            return ".jpg";
        }
        if (data.length >= 8
                && (data[0] & 0xFF) == 0x89
                && data[1] == 0x50
                && data[2] == 0x4E
                && data[3] == 0x47
                && data[4] == 0x0D
                && data[5] == 0x0A
                && data[6] == 0x1A
                && data[7] == 0x0A) {
            return ".png";
        }
        if (data.length >= 12
                && data[0] == 'R' && data[1] == 'I' && data[2] == 'F' && data[3] == 'F'
                && data[8] == 'W' && data[9] == 'E' && data[10] == 'B' && data[11] == 'P') {
            return ".webp";
        }
        return null;
    }

    private static void assertRaster(byte[] data) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(data));
        if (image == null) {
            throw new IOException("File không phải ảnh hợp lệ.");
        }
        long pixels = (long) image.getWidth() * image.getHeight();
        if (pixels <= 0 || pixels > MAX_PIXELS) {
            throw new IOException("Ảnh quá lớn.");
        }
    }
}
