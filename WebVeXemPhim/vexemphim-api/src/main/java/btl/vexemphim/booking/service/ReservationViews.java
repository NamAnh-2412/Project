package btl.vexemphim.booking.service;

import btl.vexemphim.booking.dto.HoldResponse;
import btl.vexemphim.booking.dto.ReservationResponse;
import btl.vexemphim.booking.dto.TicketLookupResponse;
import btl.vexemphim.booking.entity.Reservation;
import btl.vexemphim.booking.entity.ReservationConcession;
import btl.vexemphim.booking.entity.ReservationSeat;
import btl.vexemphim.booking.entity.Ticket;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class ReservationViews {

    public ReservationResponse toResponse(Reservation r) {
        List<ReservationResponse.TicketDto> tickets = r.getTickets().stream()
                .map(t -> new ReservationResponse.TicketDto(t.getTicketCode(),
                        seatFromTicket(t, r), t.getQrPayload(), ticketStatus(t)))
                .toList();
        List<String> labels = r.getSeats().stream().map(ReservationSeat::getSeatLabel).toList();
        List<ReservationResponse.ConcessionDto> concessions = r.getConcessions().stream()
                .map(c -> new ReservationResponse.ConcessionDto(
                        c.getProductId(), c.getName(), c.getUnitPrice(), c.getQty(), c.lineTotal()))
                .toList();
        BigDecimal seats = seatsSubtotal(r);
        BigDecimal fnb = concessionsSubtotal(r);
        return new ReservationResponse(r.getId(), r.getShowtimeId(), r.getStatus(), r.getChannel(), r.getMovieTitle(),
                r.getStartAt(), r.getRoomName(), r.getGuestName(), seats, fnb, r.getTotalAmount(),
                r.getHoldExpiresAt(), tickets, labels, concessions,
                r.getDiscountAmount() == null ? BigDecimal.ZERO : r.getDiscountAmount(),
                r.getVoucherCode());
    }

    public HoldResponse toHoldResponse(Reservation res) {
        List<HoldResponse.HeldSeat> held = res.getSeats().stream()
                .map(s -> new HoldResponse.HeldSeat(s.getSeatId(), s.getSeatLabel(), s.getPrice()))
                .toList();
        return new HoldResponse(res.getId(), res.getStatus(), res.getHoldExpiresAt(), res.getTotalAmount(), held);
    }

    public TicketLookupResponse toLookup(Ticket t) {
        Reservation r = t.getReservation();
        return new TicketLookupResponse(
                r.getId(),
                t.getTicketCode(),
                r.getMovieTitle(),
                r.getRoomName(),
                r.getStartAt(),
                seatFromTicket(t, r),
                ticketStatus(t),
                t.getCheckedInAt());
    }

    public static String ticketStatus(Ticket t) {
        return t.getStatus() == null || t.getStatus().isBlank() ? "ISSUED" : t.getStatus();
    }

    public static BigDecimal seatsSubtotal(Reservation r) {
        return r.getSeats().stream()
                .map(s -> s.getPrice() == null ? BigDecimal.ZERO : s.getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static BigDecimal concessionsSubtotal(Reservation r) {
        return r.getConcessions().stream()
                .map(ReservationConcession::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static String seatFromTicket(Ticket t, Reservation r) {
        if (t.getQrPayload() != null && t.getQrPayload().contains("|")) {
            return t.getQrPayload().substring(t.getQrPayload().indexOf('|') + 1);
        }
        return r.getSeats().isEmpty() ? "" : r.getSeats().get(0).getSeatLabel();
    }
}
