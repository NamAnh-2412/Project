package btl.vexemphim.booking.service;

import btl.vexemphim.booking.dto.*;
import btl.vexemphim.booking.entity.Payment;
import btl.vexemphim.booking.entity.Reservation;
import btl.vexemphim.booking.entity.ReservationConcession;
import btl.vexemphim.booking.entity.ReservationSeat;
import btl.vexemphim.booking.entity.Ticket;
import btl.vexemphim.booking.repository.PaymentRepository;
import btl.vexemphim.booking.repository.ReservationRepository;
import btl.vexemphim.booking.repository.ReservationSeatRepository;
import btl.vexemphim.booking.repository.TicketRepository;
import btl.vexemphim.catalog.entity.ConcessionProduct;
import btl.vexemphim.catalog.entity.Seat;
import btl.vexemphim.catalog.entity.Showtime;
import btl.vexemphim.catalog.repository.SeatRepository;
import btl.vexemphim.catalog.service.ConcessionService;
import btl.vexemphim.catalog.service.ShowtimeService;
import btl.vexemphim.booking.event.SeatChangedEvent;
import btl.vexemphim.common.exception.ApiException;
import btl.vexemphim.common.security.AuthContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class BookingService {

    private final ShowtimeService showtimeService;
    private final ConcessionService concessionService;
    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationSeatRepository reservationSeatRepository;
    private final TicketRepository ticketRepository;
    private final PaymentRepository paymentRepository;
    private final int holdMinutes;
    private final VoucherService voucherService;
    private final SeatHoldSupport seatHoldSupport;
    private final ReservationViews views;
    private final TicketMailer ticketMailer;
    private final RefundNoticeService refundNoticeService;
    private final ApplicationEventPublisher events;
    private final int cancelBeforeMinutes;
    private final PosShiftService posShiftService;

    @PersistenceContext
    private EntityManager em;

    public BookingService(ShowtimeService showtimeService,
                          ConcessionService concessionService,
                          SeatRepository seatRepository,
                          ReservationRepository reservationRepository,
                          ReservationSeatRepository reservationSeatRepository,
                          TicketRepository ticketRepository,
                          PaymentRepository paymentRepository,
                          @Value("${app.hold-minutes:8}") int holdMinutes,
                          VoucherService voucherService,
                          TicketMailer ticketMailer,
                          RefundNoticeService refundNoticeService,
                          ApplicationEventPublisher events,
                          @Value("${app.cancel-before-minutes:30}") int cancelBeforeMinutes,
                          PosShiftService posShiftService,
                          SeatHoldSupport seatHoldSupport,
                          ReservationViews views) {
        this.showtimeService = showtimeService;
        this.concessionService = concessionService;
        this.seatRepository = seatRepository;
        this.reservationRepository = reservationRepository;
        this.reservationSeatRepository = reservationSeatRepository;
        this.ticketRepository = ticketRepository;
        this.paymentRepository = paymentRepository;
        this.holdMinutes = holdMinutes;
        this.voucherService = voucherService;
        this.seatHoldSupport = seatHoldSupport;
        this.views = views;
        this.ticketMailer = ticketMailer;
        this.refundNoticeService = refundNoticeService;
        this.events = events;
        this.cancelBeforeMinutes = cancelBeforeMinutes;
        this.posShiftService = posShiftService;
    }

    public SeatMapResponse seatMap(Long showtimeId) {
        Showtime st = showtimeService.get(showtimeId);
        Long me = AuthContext.userId();
        Map<Long, ReservationSeat> taken = new HashMap<>();
        for (ReservationSeat rs : reservationSeatRepository.findByShowtimeId(showtimeId)) {
            taken.put(rs.getSeatId(), rs);
        }
        List<Seat> seats = seatRepository.findByRoom_IdOrderByRowLabelAscNumberAsc(st.getRoom().getId());
        List<SeatMapResponse.SeatCell> cells = new ArrayList<>();
        for (Seat seat : seats) {
            BigDecimal price = seatHoldSupport.priceOf(st, seat);
            String status = "AVAILABLE";
            ReservationSeat rs = taken.get(seat.getId());
            if (rs != null) {
                Reservation r = rs.getReservation();
                if ("CONFIRMED".equals(r.getStatus())) {
                    status = "BOOKED";
                } else if ("HOLD".equals(r.getStatus())) {
                    boolean mine = ("POS".equals(r.getChannel()) && me != null && me.equals(r.getCashierUserId()))
                            || ("ONLINE".equals(r.getChannel()) && me != null && me.equals(r.getCustomerUserId()));
                    status = mine ? "HOLD_MINE" : "HOLD";
                }
            }
            cells.add(new SeatMapResponse.SeatCell(
                    seat.getId(), seat.label(), seat.getRowLabel(), seat.getNumber(),
                    seat.getType(), status, price, seat.getPairSeatId()));
        }
        return new SeatMapResponse(st.getId(), st.getMovie().getTitle(), st.getStartAt(),
                st.getRoom().getName(), holdMinutes, cells);
    }

    @Transactional
    public HoldResponse hold(HoldRequest req) {
        String role = AuthContext.role();
        Long userId = AuthContext.userId();
        if ("ONLINE".equals(req.channel()) && !"CUSTOMER".equals(role)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "CUSTOMER mới hold ONLINE");
        }
        if ("POS".equals(req.channel()) && !"CASHIER".equals(role)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "CASHIER mới hold POS");
        }
        Showtime st = showtimeService.get(req.showtimeId());
        List<Long> seatIds = seatHoldSupport.expandCouples(st.getRoom().getId(), req.seatIds());
        if (seatIds.size() > 8) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION", "Tối đa 8 ghế / lần giữ");
        }
        em.lock(st, LockModeType.PESSIMISTIC_WRITE);
        LocalDateTime now = LocalDateTime.now();
        if (!"SCHEDULED".equals(st.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION", "Suất không còn mở bán");
        }
        boolean posWalkIn = "POS".equals(req.channel()) && "CASHIER".equals(role);
        if (posWalkIn) {
            if (st.getEndAt() != null && !st.getEndAt().isAfter(now)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION", "Suất đã kết thúc");
            }
        } else if (!st.getStartAt().isAfter(now)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION", "Suất không còn mở bán");
        }
        if (!"ACTIVE".equals(st.getRoom().getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "ROOM_INACTIVE", "Phòng đang dừng, không bán vé suất này");
        }
        if ("COMING".equals(st.getMovie().getStatus()) || "HIDDEN".equals(st.getMovie().getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "MOVIE_NOT_ON_SALE", "Phim chưa mở bán vé");
        }
        Reservation mine = findMyOpenHold(st.getId(), userId, req.channel());
        seatHoldSupport.assertNoSingleGapBesidePaid(st, seatIds, mine);
        try {
            if (mine != null) {
                return seatHoldSupport.replaceHoldSeats(mine, st, req, seatIds, now);
            }
            return seatHoldSupport.createHold(st, req, seatIds, userId, now);
        } catch (DataIntegrityViolationException ex) {
            throw new ApiException(HttpStatus.CONFLICT, "SEAT_TAKEN", "Ghế đã được giữ hoặc bán");
        }
    }

    @Transactional
    public ReservationResponse setConcessions(Long id, ConcessionCartRequest req) {
        Reservation res = get(id);
        assertHoldOwner(res);
        assertHoldOpen(res);
        res.getConcessions().clear();
        List<ConcessionCartRequest.Line> items = req.items() == null ? List.of() : req.items();
        for (ConcessionCartRequest.Line line : items) {
            if (line.qty() == null || line.qty() <= 0) {
                continue;
            }
            ConcessionProduct p = concessionService.get(line.productId());
            if (!"ACTIVE".equals(p.getStatus())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "CONCESSION_HIDDEN", "Món đã ngừng bán");
            }
            ReservationConcession row = new ReservationConcession();
            row.setReservation(res);
            row.setProductId(p.getId());
            row.setName(p.getName());
            row.setUnitPrice(p.getPrice());
            row.setQty(line.qty());
            res.getConcessions().add(row);
        }
        recalcTotal(res);
        return views.toResponse(res);
    }

    @Transactional
    public ReservationResponse confirmOnline(Long id, ConfirmRequest req) {
        Reservation res = get(id);
        Long userId = AuthContext.userId();
        if (!"CUSTOMER".equals(AuthContext.role()) || !userId.equals(res.getCustomerUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Không phải chủ giữ chỗ");
        }
        if (!"ONLINE".equals(res.getChannel())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Sai kênh");
        }
        return finalizePayment(res, req == null || req.paymentMethod() == null ? "ONLINE_MOCK" : req.paymentMethod());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ReservationResponse confirmGateway(Long id, String method) {
        Reservation res = get(id);
        return finalizePayment(res, method);
    }

    @Transactional
    public ReservationResponse applyVoucher(Long id, String code) {
        Reservation res = get(id);
        assertHoldOwner(res);
        assertHoldOpen(res);
        if (!"ONLINE".equals(res.getChannel())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION", "Mã giảm giá chỉ áp dụng khi đặt online");
        }
        BigDecimal gross = ReservationViews.seatsSubtotal(res).add(ReservationViews.concessionsSubtotal(res));
        voucherService.apply(res, code, gross);
        recalcTotal(res);
        return views.toResponse(res);
    }

    @Transactional
    public ReservationResponse confirmCash(Long id) {
        Reservation res = get(id);
        Long userId = AuthContext.userId();
        if (!"CASHIER".equals(AuthContext.role()) || !userId.equals(res.getCashierUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Không phải thu ngân của hold này");
        }
        if (!"POS".equals(res.getChannel())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Sai kênh");
        }
        res.setShiftId(posShiftService.openShiftId(userId));
        return finalizePayment(res, "CASH");
    }

    @Transactional
    public void cancelHold(Long id) {
        Reservation res = get(id);
        Long userId = AuthContext.userId();
        String role = AuthContext.role();
        boolean owner = ("CUSTOMER".equals(role) && userId.equals(res.getCustomerUserId()))
                || ("CASHIER".equals(role) && userId.equals(res.getCashierUserId()))
                || "ADMIN".equals(role);
        if (!owner) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Không hủy được hold này");
        }
        if (!"HOLD".equals(res.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION", "Chỉ hủy khi đang HOLD");
        }
        expire(res, "CANCELLED");
    }

    @Transactional
    public ReservationResponse cancelOnlineTicket(Long id) {
        Reservation res = get(id);
        Long userId = AuthContext.userId();
        if (!"CUSTOMER".equals(AuthContext.role()) || userId == null || !userId.equals(res.getCustomerUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Không phải chủ vé");
        }
        if (!"ONLINE".equals(res.getChannel())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "NOT_ONLINE", "Chỉ hủy vé đặt online");
        }
        if (!"CONFIRMED".equals(res.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION", "Chỉ hủy vé đã thanh toán");
        }
        for (Ticket ticket : res.getTickets()) {
            if ("CHECKED_IN".equals(ReservationViews.ticketStatus(ticket))) {
                throw new ApiException(HttpStatus.CONFLICT, "CHECKED_IN_BLOCK", "Vé đã soát, không hủy được");
            }
        }
        Showtime st = showtimeService.get(res.getShowtimeId());
        em.lock(st, LockModeType.PESSIMISTIC_WRITE);
        LocalDateTime start = st.getStartAt() != null ? st.getStartAt() : res.getStartAt();
        if (start == null || !LocalDateTime.now().isBefore(start.minusMinutes(cancelBeforeMinutes))) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "TOO_LATE",
                    "Chỉ hủy trước giờ chiếu " + cancelBeforeMinutes + " phút");
        }
        BigDecimal refund = res.getTotalAmount() == null ? BigDecimal.ZERO : res.getTotalAmount();
        Payment payment = new Payment();
        payment.setReservationId(res.getId());
        payment.setMethod("REFUND");
        payment.setStatus("REFUNDED");
        payment.setAmount(refund);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);
        for (Ticket ticket : res.getTickets()) {
            ticket.setStatus("CANCELLED");
        }
        publish(res, "AVAILABLE");
        res.getSeats().clear();
        res.setStatus("REFUNDED");
        reservationRepository.saveAndFlush(res);
        refundNoticeService.notifyOnlineRefund(res);
        return views.toResponse(res);
    }

    public List<ReservationResponse> myBookings() {
        Long userId = AuthContext.userId();
        return reservationRepository
                .findByCustomerUserIdAndStatusInOrderByCreatedAtDesc(userId, List.of("CONFIRMED", "REFUNDED"))
                .stream().map(views::toResponse).toList();
    }

    public ReservationResponse getOne(Long id) {
        Reservation res = get(id);
        Long userId = AuthContext.userId();
        String role = AuthContext.role();
        boolean owner = userId != null && (userId.equals(res.getCustomerUserId()) || userId.equals(res.getCashierUserId()));
        boolean cashierReprint = "CASHIER".equals(role) && "CONFIRMED".equals(res.getStatus());
        boolean ok = "ADMIN".equals(role) || owner || cashierReprint;
        if (!ok) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Không xem được vé người khác");
        }
        return views.toResponse(res);
    }

    public OccupancyResponse occupancy(Long showtimeId) {
        return new OccupancyResponse(
                reservationSeatRepository.countHold(showtimeId),
                reservationSeatRepository.countBooked(showtimeId));
    }

    public TicketLookupResponse lookupTicket(String code) {
        return views.toLookup(ticketByCode(code));
    }

    @Transactional
    public TicketLookupResponse checkIn(String code) {
        Ticket t = ticketByCode(code);
        Reservation r = t.getReservation();
        if ("REFUNDED".equals(r.getStatus()) || "CANCELLED".equals(ReservationViews.ticketStatus(t))) {
            throw new ApiException(HttpStatus.CONFLICT, "TICKET_CANCELLED", "Vé đã hủy");
        }
        if (!"CONFIRMED".equals(r.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "NOT_PAID", "Vé chưa thanh toán");
        }
        if ("CHECKED_IN".equals(ReservationViews.ticketStatus(t))) {
            throw new ApiException(HttpStatus.CONFLICT, "ALREADY_CHECKED_IN", "Vé đã soát");
        }
        t.setStatus("CHECKED_IN");
        t.setCheckedInAt(LocalDateTime.now());
        return views.toLookup(t);
    }

    @Transactional
    public void expireHolds() {
        List<Reservation> expired = reservationRepository.findByStatusAndHoldExpiresAtBefore("HOLD", LocalDateTime.now());
        for (Reservation r : expired) {
            expire(r, "EXPIRED");
        }
    }

    private ReservationResponse finalizePayment(Reservation res, String method) {
        if ("CONFIRMED".equals(res.getStatus())) {
            return views.toResponse(res);
        }
        if (!"HOLD".equals(res.getStatus())) {
            throw new ApiException(HttpStatus.CONFLICT, "HOLD_EXPIRED", "Giữ chỗ không còn hiệu lực");
        }
        if (res.getHoldExpiresAt() != null && res.getHoldExpiresAt().isBefore(LocalDateTime.now())) {
            expire(res, "EXPIRED");
            throw new ApiException(HttpStatus.CONFLICT, "HOLD_EXPIRED", "Hết thời gian giữ ghế");
        }
        assertShowtimeSellable(res);
        if (voucherService.consume(res.getVoucherId()) == 0) {
            voucherService.clear(res);
            recalcTotal(res);
            throw new ApiException(HttpStatus.CONFLICT, "VOUCHER_LIMIT_REACHED", "Mã giảm giá đã hết lượt sử dụng");
        }
        res.setStatus("CONFIRMED");
        res.setHoldExpiresAt(null);
        if (!"PAYOS".equals(method) && !"MOMO".equals(method)) {
            Payment p = new Payment();
            p.setReservationId(res.getId());
            p.setMethod(method);
            p.setStatus("SUCCESS");
            p.setAmount(res.getTotalAmount());
            p.setPaidAt(LocalDateTime.now());
            paymentRepository.save(p);
        }
        int i = 1;
        for (ReservationSeat seat : res.getSeats()) {
            Ticket t = new Ticket();
            t.setReservation(res);
            String code = String.format("VX-%d-%06d", LocalDateTime.now().getYear(), res.getId() * 10 + i);
            t.setTicketCode(code);
            t.setQrPayload(code + "|" + seat.getSeatLabel());
            t.setStatus("ISSUED");
            res.getTickets().add(t);
            i++;
        }
        publish(res, "BOOKED");
        ticketMailer.sendOnlineTickets(res);
        return views.toResponse(res);
    }

    private void expire(Reservation res, String status) {
        publish(res, "AVAILABLE");
        res.getSeats().clear();
        res.getConcessions().clear();
        res.setStatus(status);
    }

    private void assertHoldOwner(Reservation res) {
        Long userId = AuthContext.userId();
        String role = AuthContext.role();
        boolean owner = ("CUSTOMER".equals(role) && userId.equals(res.getCustomerUserId()))
                || ("CASHIER".equals(role) && userId.equals(res.getCashierUserId()));
        if (!owner) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Không sửa đồ ăn vặt của giữ chỗ này");
        }
    }

    private void assertHoldOpen(Reservation res) {
        if (!"HOLD".equals(res.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION", "Chỉ thêm đồ ăn vặt khi đang giữ ghế");
        }
        if (res.getHoldExpiresAt() != null && res.getHoldExpiresAt().isBefore(LocalDateTime.now())) {
            expire(res, "EXPIRED");
            throw new ApiException(HttpStatus.CONFLICT, "HOLD_EXPIRED", "Hết thời gian giữ ghế");
        }
    }

    private void recalcTotal(Reservation res) {
        voucherService.reprice(res, ReservationViews.seatsSubtotal(res).add(ReservationViews.concessionsSubtotal(res)));
    }

    private Reservation findMyOpenHold(Long showtimeId, Long userId, String channel) {
        List<Reservation> old = "POS".equals(channel)
                ? reservationRepository.findByShowtimeIdAndCashierUserIdAndStatus(showtimeId, userId, "HOLD")
                : reservationRepository.findByShowtimeIdAndCustomerUserIdAndStatus(showtimeId, userId, "HOLD");
        Reservation open = null;
        LocalDateTime now = LocalDateTime.now();
        for (Reservation r : old) {
            if (r.getHoldExpiresAt() != null && r.getHoldExpiresAt().isBefore(now)) {
                expire(r, "EXPIRED");
            } else if (open == null) {
                open = r;
            } else {
                expire(r, "CANCELLED");
            }
        }
        reservationRepository.flush();
        return open;
    }

    private void assertShowtimeSellable(Reservation res) {
        Showtime st = showtimeService.get(res.getShowtimeId());
        LocalDateTime now = LocalDateTime.now();
        if (!"SCHEDULED".equals(st.getStatus())) {
            expire(res, "EXPIRED");
            throw new ApiException(HttpStatus.CONFLICT, "SHOWTIME_CLOSED", "Suất không còn mở bán");
        }
        if ("POS".equals(res.getChannel()) && st.getEndAt() != null && !st.getEndAt().isAfter(now)) {
            expire(res, "EXPIRED");
            throw new ApiException(HttpStatus.CONFLICT, "SHOWTIME_ENDED", "Suất đã kết thúc");
        }
    }

    @Transactional
    public byte[] mockPaymentQr(Long id) {
        Reservation res = get(id);
        Long userId = AuthContext.userId();
        if (!"CUSTOMER".equals(AuthContext.role()) || userId == null || !userId.equals(res.getCustomerUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Không phải chủ giữ chỗ");
        }
        if (!"ONLINE".equals(res.getChannel())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION", "QR ảo chỉ dùng cho đặt vé online");
        }
        assertHoldOpen(res);
        long amount = res.getTotalAmount() == null ? 0 : res.getTotalAmount().longValue();
        return QrCodes.png("CINEVE|" + res.getId() + "|" + amount);
    }

    private Reservation get(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không tìm thấy đặt chỗ"));
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

    private Ticket ticketByCode(String code) {
        return ticketRepository.findByTicketCode(code)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không có mã vé"));
    }

}
