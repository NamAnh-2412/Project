package btl.vexemphim.booking.service;

import btl.vexemphim.booking.dto.*;
import btl.vexemphim.booking.entity.Payment;
import btl.vexemphim.booking.entity.PosShift;
import btl.vexemphim.booking.entity.PosSnackOrder;
import btl.vexemphim.booking.entity.Reservation;
import btl.vexemphim.booking.repository.PaymentRepository;
import btl.vexemphim.booking.repository.PosShiftRepository;
import btl.vexemphim.booking.repository.PosSnackOrderRepository;
import btl.vexemphim.booking.repository.ReservationRepository;
import btl.vexemphim.catalog.entity.ConcessionProduct;
import btl.vexemphim.catalog.service.ConcessionService;
import btl.vexemphim.common.exception.ApiException;
import btl.vexemphim.common.security.AuthContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PosShiftService {

    private final PosShiftRepository shiftRepository;
    private final PosSnackOrderRepository snackRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final ConcessionService concessionService;

    public PosShiftService(PosShiftRepository shiftRepository,
                           PosSnackOrderRepository snackRepository,
                           ReservationRepository reservationRepository,
                           PaymentRepository paymentRepository,
                           ConcessionService concessionService) {
        this.shiftRepository = shiftRepository;
        this.snackRepository = snackRepository;
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
        this.concessionService = concessionService;
    }

    public PosShiftResponse current() {
        return openOf(cashierId()).map(PosShiftService::toResponse).orElse(null);
    }

    public Long openShiftId(Long cashierId) {
        return shiftRepository.findFirstByCashierUserIdAndStatusOrderByIdDesc(cashierId, "OPEN")
                .map(PosShift::getId)
                .orElse(null);
    }

    @Transactional
    public PosShiftResponse open(OpenShiftRequest req) {
        Long cashierId = cashierId();
        if (openOf(cashierId).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "SHIFT_OPEN", "Ca đang mở. Hãy kết ca trước khi mở ca mới");
        }
        PosShift shift = new PosShift();
        shift.setCashierUserId(cashierId);
        shift.setStatus("OPEN");
        shift.setOpeningCash(req.openingCash());
        shift.setOpenedAt(LocalDateTime.now());
        shiftRepository.save(shift);
        return toResponse(shift);
    }

    @Transactional
    public PosShiftResponse close(CloseShiftRequest req) {
        PosShift shift = openOf(cashierId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "NO_SHIFT", "Chưa mở ca"));
        LocalDateTime now = LocalDateTime.now();
        Totals totals = totals(shift.getCashierUserId(), shift.getOpenedAt(), now);
        BigDecimal expected = shift.getOpeningCash().add(totals.cash).add(totals.snacks);
        shift.setCashSales(totals.cash);
        shift.setCardSales(totals.card);
        shift.setSnackSales(totals.snacks);
        shift.setTicketCount(totals.tickets);
        shift.setExpectedCash(expected);
        shift.setActualCash(req.actualCash());
        shift.setDifference(req.actualCash().subtract(expected));
        shift.setStatus("CLOSED");
        shift.setClosedAt(now);
        return toResponse(shift);
    }

    @Transactional
    public SnackSaleResponse sellSnacks(SnackSaleRequest req) {
        Long cashierId = cashierId();
        BigDecimal total = BigDecimal.ZERO;
        StringBuilder detail = new StringBuilder();
        for (SnackLine line : req.items()) {
            ConcessionProduct product = concessionService.get(line.productId());
            if (!"ACTIVE".equals(product.getStatus())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "CONCESSION_HIDDEN", "Món đã ngừng bán");
            }
            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(line.qty()));
            total = total.add(lineTotal);
            if (!detail.isEmpty()) {
                detail.append("; ");
            }
            detail.append(product.getName()).append(" x").append(line.qty());
        }
        PosSnackOrder order = new PosSnackOrder();
        order.setCashierUserId(cashierId);
        order.setShiftId(openShiftId(cashierId));
        order.setTotalAmount(total);
        order.setDetail(detail.toString());
        snackRepository.save(order);
        return new SnackSaleResponse(order.getId(), order.getTotalAmount(), order.getDetail(), order.getCreatedAt());
    }

    private Totals totals(Long cashierId, LocalDateTime from, LocalDateTime to) {
        List<Reservation> sales = reservationRepository
                .findByCashierUserIdAndChannelAndStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                        cashierId, "POS", "CONFIRMED", from, to);
        BigDecimal cash = BigDecimal.ZERO;
        BigDecimal card = BigDecimal.ZERO;
        int tickets = 0;
        if (!sales.isEmpty()) {
            List<Long> ids = sales.stream().map(Reservation::getId).toList();
            for (Payment payment : paymentRepository.findByReservationIdIn(ids)) {
                if (!"SUCCESS".equals(payment.getStatus())) {
                    continue;
                }
                BigDecimal amount = payment.getAmount() == null ? BigDecimal.ZERO : payment.getAmount();
                if ("CASH".equals(payment.getMethod())) {
                    cash = cash.add(amount);
                } else if (!"REFUND".equals(payment.getMethod())) {
                    card = card.add(amount);
                }
            }
            tickets = sales.stream().mapToInt(r -> r.getTickets() == null ? 0 : r.getTickets().size()).sum();
        }
        BigDecimal snacks = snackRepository
                .findByCashierUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(cashierId, from, to)
                .stream()
                .map(o -> o.getTotalAmount() == null ? BigDecimal.ZERO : o.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Totals(cash, card, snacks, tickets);
    }

    private java.util.Optional<PosShift> openOf(Long cashierId) {
        return shiftRepository.findFirstByCashierUserIdAndStatusOrderByIdDesc(cashierId, "OPEN");
    }

    private static Long cashierId() {
        if (!"CASHIER".equals(AuthContext.role()) || AuthContext.userId() == null) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Chỉ thu ngân mở ca");
        }
        return AuthContext.userId();
    }

    private static PosShiftResponse toResponse(PosShift shift) {
        return new PosShiftResponse(shift.getId(), shift.getStatus(), shift.getOpeningCash(),
                shift.getCashSales(), shift.getCardSales(), shift.getSnackSales(), shift.getTicketCount(),
                shift.getExpectedCash(), shift.getActualCash(), shift.getDifference(),
                shift.getOpenedAt(), shift.getClosedAt());
    }

    private record Totals(BigDecimal cash, BigDecimal card, BigDecimal snacks, int tickets) {
    }
}
