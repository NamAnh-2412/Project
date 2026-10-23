package btl.vexemphim.booking.service;

import btl.vexemphim.booking.dto.StaffNoticeResponse;
import btl.vexemphim.booking.entity.Reservation;
import btl.vexemphim.booking.entity.StaffNotice;
import btl.vexemphim.booking.entity.Ticket;
import btl.vexemphim.booking.repository.StaffNoticeRepository;
import btl.vexemphim.common.exception.ApiException;
import btl.vexemphim.common.security.AuthContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class RefundNoticeService {

    private final StaffNoticeRepository staffNoticeRepository;
    private final TicketMailer ticketMailer;

    public RefundNoticeService(StaffNoticeRepository staffNoticeRepository, TicketMailer ticketMailer) {
        this.staffNoticeRepository = staffNoticeRepository;
        this.ticketMailer = ticketMailer;
    }

    @Transactional
    public void notifyOnlineRefund(Reservation res) {
        String title = "Khách hủy vé online";
        String body = body(res);
        save("ADMIN", title, body, res.getId());
        save("CASHIER", title, body, res.getId());
        ticketMailer.sendAdminRefund(title, body);
    }

    @Transactional(readOnly = true)
    public List<StaffNoticeResponse> listForCurrentRole() {
        String role = AuthContext.role();
        if (!"ADMIN".equals(role) && !"CASHIER".equals(role)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Không xem được thông báo này");
        }
        return staffNoticeRepository.findTop30ByAudienceOrderByCreatedAtDesc(role).stream()
                .map(n -> new StaffNoticeResponse(n.getId(), n.getTitle(), n.getBody(), n.getReservationId(), n.getCreatedAt()))
                .toList();
    }

    private void save(String audience, String title, String body, Long reservationId) {
        StaffNotice notice = new StaffNotice();
        notice.setAudience(audience);
        notice.setTitle(title);
        notice.setBody(body);
        notice.setReservationId(reservationId);
        staffNoticeRepository.save(notice);
    }

    private static String body(Reservation res) {
        BigDecimal amount = res.getTotalAmount() == null ? BigDecimal.ZERO : res.getTotalAmount();
        StringBuilder text = new StringBuilder();
        text.append("Phim: ").append(nullToEmpty(res.getMovieTitle())).append('\n');
        text.append("Suất: ").append(res.getStartAt() == null ? "" : res.getStartAt().toString().replace('T', ' ')).append('\n');
        text.append("Phòng: ").append(nullToEmpty(res.getRoomName())).append('\n');
        text.append("Hoàn: ").append(amount.toPlainString()).append("đ. Mã giảm giá không hoàn lại lượt.\n");
        text.append("Mã vé: ");
        boolean first = true;
        for (Ticket ticket : res.getTickets()) {
            if (!first) {
                text.append(", ");
            }
            text.append(ticket.getTicketCode());
            first = false;
        }
        return text.toString();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
