package btl.vexemphim.booking.service;

import btl.vexemphim.booking.dto.HoldRequest;
import btl.vexemphim.booking.dto.HoldResponse;
import btl.vexemphim.booking.entity.Reservation;
import btl.vexemphim.booking.entity.ReservationSeat;
import btl.vexemphim.booking.event.SeatChangedEvent;
import btl.vexemphim.booking.repository.ReservationRepository;
import btl.vexemphim.booking.repository.ReservationSeatRepository;
import btl.vexemphim.catalog.entity.Seat;
import btl.vexemphim.catalog.entity.Showtime;
import btl.vexemphim.catalog.repository.SeatRepository;
import btl.vexemphim.common.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class SeatHoldSupport {

    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationSeatRepository reservationSeatRepository;
    private final ApplicationEventPublisher events;
    private final VoucherService voucherService;
    private final ReservationViews views;
    private final int holdMinutes;
    private final BigDecimal vipMultiplier;

    public SeatHoldSupport(SeatRepository seatRepository,
                           ReservationRepository reservationRepository,
                           ReservationSeatRepository reservationSeatRepository,
                           ApplicationEventPublisher events,
                           VoucherService voucherService,
                           ReservationViews views,
                           @Value("${app.hold-minutes:8}") int holdMinutes,
                           @Value("${app.vip-multiplier:1.2}") BigDecimal vipMultiplier) {
        this.seatRepository = seatRepository;
        this.reservationRepository = reservationRepository;
        this.reservationSeatRepository = reservationSeatRepository;
        this.events = events;
        this.voucherService = voucherService;
        this.views = views;
        this.holdMinutes = holdMinutes;
        this.vipMultiplier = vipMultiplier;
    }

    public List<Long> expandCouples(Long roomId, List<Long> ids) {
        Map<Long, Seat> byId = new HashMap<>();
        for (Seat seat : seatRepository.findByRoom_IdOrderByRowLabelAscNumberAsc(roomId)) {
            byId.put(seat.getId(), seat);
        }
        LinkedHashSet<Long> out = new LinkedHashSet<>();
        for (Long id : ids) {
            out.add(id);
            Seat seat = byId.get(id);
            if (seat != null && seat.getPairSeatId() != null) {
                out.add(seat.getPairSeatId());
            }
        }
        return new ArrayList<>(out);
    }

    public BigDecimal priceOf(Showtime st, Seat seat) {
        BigDecimal base = st.getBasePrice();
        if ("VIP".equalsIgnoreCase(seat.getType())) {
            return base.multiply(vipMultiplier).setScale(0, RoundingMode.HALF_UP);
        }
        return base;
    }

    /**
     * Ghế đã thanh toán (CONFIRMED) không được để đúng một ghế trống bên cạnh ghế đang giữ.
     * Ghế kề bên, hoặc cách từ hai ghế trống trở lên, vẫn được. Ghế chỉ đang HOLD không kích hoạt luật này.
     * Bỏ bớt ghế trên hold đang mở thì cho qua, để khách gỡ ghế đã chọn trước khi ghế kia được thanh toán.
     */
    public void assertNoSingleGapBesidePaid(Showtime st, List<Long> seatIds, Reservation currentHold) {
        Set<Long> wanted = new LinkedHashSet<>(seatIds);
        Set<Long> paid = new HashSet<>();
        for (ReservationSeat rs : reservationSeatRepository.findByShowtimeId(st.getId())) {
            Reservation reservation = rs.getReservation();
            if (reservation != null && "CONFIRMED".equals(reservation.getStatus())) {
                paid.add(rs.getSeatId());
            }
        }
        if (paid.isEmpty()) {
            return;
        }
        String blocked = firstSingleGapLabel(st, wanted, paid);
        if (blocked == null) {
            return;
        }
        if (currentHold != null) {
            Set<Long> have = new HashSet<>();
            for (ReservationSeat rs : currentHold.getSeats()) {
                have.add(rs.getSeatId());
            }
            if (have.containsAll(wanted) && wanted.size() < have.size()) {
                return;
            }
        }
        throw new ApiException(HttpStatus.BAD_REQUEST, "SEAT_GAP",
                "Không thể đặt ghế " + blocked + " vì cách ghế đã thanh toán đúng 1 ghế. Chọn ghế kề bên hoặc cách ít nhất 2 ghế trống.");
    }

    public HoldResponse replaceHoldSeats(Reservation mine, Showtime st, HoldRequest req, List<Long> seatIds, LocalDateTime now) {
        Set<Long> wanted = new LinkedHashSet<>(seatIds);
        List<SeatChangedEvent.SeatPatch> dropped = new ArrayList<>();
        mine.getSeats().removeIf(rs -> {
            if (wanted.contains(rs.getSeatId())) {
                return false;
            }
            dropped.add(new SeatChangedEvent.SeatPatch(rs.getSeatId(), "AVAILABLE"));
            return true;
        });
        if (!dropped.isEmpty()) {
            events.publishEvent(new SeatChangedEvent(st.getId(), dropped));
        }
        reservationRepository.flush();
        Set<Long> have = new HashSet<>();
        for (ReservationSeat rs : mine.getSeats()) {
            have.add(rs.getSeatId());
        }
        for (Long seatId : wanted) {
            if (have.contains(seatId)) {
                continue;
            }
            attachSeat(mine, st, seatId);
        }
        mine.setGuestName(req.guestName());
        if (mine.getHoldExpiresAt() == null) {
            mine.setHoldExpiresAt(now.plusMinutes(holdMinutes));
        }
        recalcTotal(mine);
        reservationRepository.saveAndFlush(mine);
        publish(mine, "HOLD");
        return views.toHoldResponse(mine);
    }

    public HoldResponse createHold(Showtime st, HoldRequest req, List<Long> seatIds, Long userId, LocalDateTime now) {
        Reservation res = new Reservation();
        res.setShowtimeId(st.getId());
        res.setMovieId(st.getMovie().getId());
        res.setMovieTitle(st.getMovie().getTitle());
        res.setStartAt(st.getStartAt());
        res.setRoomName(st.getRoom().getName());
        res.setChannel(req.channel());
        res.setGuestName(req.guestName());
        res.setStatus("HOLD");
        res.setHoldExpiresAt(now.plusMinutes(holdMinutes));
        if ("ONLINE".equals(req.channel())) {
            res.setCustomerUserId(userId);
        } else {
            res.setCashierUserId(userId);
        }
        for (Long seatId : seatIds) {
            attachSeat(res, st, seatId);
        }
        recalcTotal(res);
        reservationRepository.saveAndFlush(res);
        publish(res, "HOLD");
        return views.toHoldResponse(res);
    }

    private void attachSeat(Reservation res, Showtime st, Long seatId) {
        if (reservationSeatRepository.existsByShowtimeIdAndSeatId(st.getId(), seatId)) {
            throw new ApiException(HttpStatus.CONFLICT, "SEAT_TAKEN", "Ghế đã được giữ hoặc bán");
        }
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION", "Ghế không tồn tại"));
        if (!seat.getRoom().getId().equals(st.getRoom().getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION", "Ghế không thuộc phòng suất này");
        }
        ReservationSeat rs = new ReservationSeat();
        rs.setReservation(res);
        rs.setShowtimeId(st.getId());
        rs.setSeatId(seatId);
        rs.setSeatLabel(seat.label());
        rs.setPrice(priceOf(st, seat));
        res.getSeats().add(rs);
    }

    private String firstSingleGapLabel(Showtime st, Set<Long> wanted, Set<Long> paid) {
        Map<String, List<Seat>> rows = new LinkedHashMap<>();
        for (Seat seat : seatRepository.findByRoom_IdOrderByRowLabelAscNumberAsc(st.getRoom().getId())) {
            rows.computeIfAbsent(seat.getRowLabel(), key -> new ArrayList<>()).add(seat);
        }
        for (List<Seat> row : rows.values()) {
            row.sort(Comparator.comparingInt(Seat::getNumber));
            for (int i = 0; i < row.size(); i++) {
                Seat seat = row.get(i);
                if (!wanted.contains(seat.getId())) {
                    continue;
                }
                if (singleGapBesidePaid(row, i, -1, wanted, paid) || singleGapBesidePaid(row, i, 1, wanted, paid)) {
                    return seat.label();
                }
            }
        }
        return null;
    }

    private boolean singleGapBesidePaid(List<Seat> row, int index, int dir, Set<Long> wanted, Set<Long> paid) {
        int mid = index + dir;
        int far = index + dir * 2;
        if (mid < 0 || far < 0 || mid >= row.size() || far >= row.size()) {
            return false;
        }
        Seat seat = row.get(index);
        Seat middle = row.get(mid);
        Seat other = row.get(far);
        if (middle.getNumber() != seat.getNumber() + dir || other.getNumber() != seat.getNumber() + dir * 2) {
            return false;
        }
        if (paid.contains(middle.getId()) || wanted.contains(middle.getId())) {
            return false;
        }
        return paid.contains(other.getId());
    }

    private void recalcTotal(Reservation res) {
        voucherService.reprice(res, ReservationViews.seatsSubtotal(res).add(ReservationViews.concessionsSubtotal(res)));
    }

    private void publish(Reservation res, String status) {
        if (res.getShowtimeId() == null || res.getSeats().isEmpty()) {
            return;
        }
        List<SeatChangedEvent.SeatPatch> seats = res.getSeats().stream()
                .map(s -> new SeatChangedEvent.SeatPatch(s.getSeatId(), status))
                .toList();
        events.publishEvent(new SeatChangedEvent(res.getShowtimeId(), seats));
    }
}
