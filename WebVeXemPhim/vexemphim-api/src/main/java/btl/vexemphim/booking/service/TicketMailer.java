package btl.vexemphim.booking.service;

import btl.vexemphim.auth.entity.Role;
import btl.vexemphim.auth.entity.User;
import btl.vexemphim.auth.repository.UserRepository;
import btl.vexemphim.booking.entity.Reservation;
import btl.vexemphim.booking.entity.Ticket;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class TicketMailer {

    private static final Logger log = LoggerFactory.getLogger(TicketMailer.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final UserRepository userRepository;
    private final String host;
    private final String from;

    public TicketMailer(ObjectProvider<JavaMailSender> mailSender,
                        UserRepository userRepository,
                        @Value("${spring.mail.host:}") String host,
                        @Value("${app.mail.from:CineVe <noreply@localhost>}") String from) {
        this.mailSender = mailSender;
        this.userRepository = userRepository;
        this.host = host;
        this.from = from;
    }

    public void sendAdminRefund(String title, String body) {
        if (host == null || host.isBlank()) {
            return;
        }
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            return;
        }
        for (User admin : userRepository.findByRoleAndEnabled(Role.ADMIN, true)) {
            if (admin.getEmail() == null || admin.getEmail().isBlank()) {
                continue;
            }
            try {
                var message = sender.createMimeMessage();
                var helper = new MimeMessageHelper(message, false, "UTF-8");
                helper.setFrom(from);
                helper.setTo(admin.getEmail());
                helper.setSubject(title);
                helper.setText(body, false);
                sender.send(message);
            } catch (Exception ex) {
                log.warn("Không gửi được mail hủy vé tới {}: {}", admin.getEmail(), ex.getMessage());
            }
        }
    }

    public void sendOnlineTickets(Reservation res) {
        if (host == null || host.isBlank() || !"ONLINE".equals(res.getChannel()) || res.getCustomerUserId() == null) {
            return;
        }
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            return;
        }
        User user = userRepository.findById(res.getCustomerUserId()).orElse(null);
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            return;
        }
        try {
            var message = sender.createMimeMessage();
            var helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");
            helper.setFrom(from);
            helper.setTo(user.getEmail());
            helper.setSubject("Vé " + res.getMovieTitle());
            List<InlineQr> images = new ArrayList<>();
            helper.setText(html(res, images), true);
            for (InlineQr image : images) {
                helper.addInline(image.cid(), image.resource(), "image/png");
            }
            sender.send(message);
        } catch (Exception ex) {
            log.warn("Không gửi được mail vé reservation {}: {}", res.getId(), ex.getMessage());
        }
    }

    private static String html(Reservation res, List<InlineQr> images) throws Exception {
        StringBuilder body = new StringBuilder();
        body.append("<p><strong>").append(escape(res.getMovieTitle())).append("</strong></p>");
        body.append("<p>").append(escape(res.getRoomName())).append(" · ")
                .append(res.getStartAt() == null ? "" : escape(res.getStartAt().toString().replace('T', ' ')))
                .append("</p>");
        body.append("<p>Tổng tiền: ").append(res.getTotalAmount()).append("đ</p>");
        int n = 0;
        for (Ticket ticket : res.getTickets()) {
            String cid = "qr" + n;
            String seat = seatOf(ticket);
            body.append("<p>Mã vé <strong>").append(escape(ticket.getTicketCode())).append("</strong>");
            if (!seat.isEmpty()) {
                body.append(" · ghế ").append(escape(seat));
            }
            body.append("</p>");
            body.append("<img alt=\"QR\" src=\"cid:").append(cid).append("\" width=\"200\" height=\"200\"/>");
            images.add(new InlineQr(cid, pngResource(cid, qrPng(ticket.getTicketCode()))));
            n++;
        }
        return body.toString();
    }

    private static byte[] qrPng(String ticketCode) throws Exception {
        BitMatrix matrix = new QRCodeWriter().encode(ticketCode, BarcodeFormat.QR_CODE, 240, 240);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", out);
        return out.toByteArray();
    }

    private static ByteArrayResource pngResource(String cid, byte[] png) {
        return new ByteArrayResource(png) {
            @Override
            public String getFilename() {
                return cid + ".png";
            }
        };
    }

    private static String seatOf(Ticket ticket) {
        String payload = ticket.getQrPayload();
        if (payload == null) {
            return "";
        }
        int bar = payload.indexOf('|');
        if (bar < 0 || bar >= payload.length() - 1) {
            return "";
        }
        return payload.substring(bar + 1);
    }

    private static String escape(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private record InlineQr(String cid, ByteArrayResource resource) {
    }
}
