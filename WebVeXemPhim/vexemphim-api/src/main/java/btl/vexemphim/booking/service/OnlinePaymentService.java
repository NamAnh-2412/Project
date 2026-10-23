package btl.vexemphim.booking.service;

import btl.vexemphim.booking.dto.PaymentLinkResponse;
import btl.vexemphim.booking.entity.Payment;
import btl.vexemphim.booking.entity.Reservation;
import btl.vexemphim.booking.repository.PaymentRepository;
import btl.vexemphim.booking.repository.ReservationRepository;
import btl.vexemphim.common.exception.ApiException;
import btl.vexemphim.common.security.AuthContext;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
public class OnlinePaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final BookingService bookingService;
    private final PayOsClient payOsClient;
    private final MomoClient momoClient;
    private final int waitMinutes;

    public OnlinePaymentService(PaymentRepository paymentRepository,
                                ReservationRepository reservationRepository,
                                BookingService bookingService,
                                PayOsClient payOsClient,
                                MomoClient momoClient,
                                @Value("${app.payment-wait-minutes:10}") int waitMinutes) {
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
        this.bookingService = bookingService;
        this.payOsClient = payOsClient;
        this.momoClient = momoClient;
        this.waitMinutes = waitMinutes;
    }

    public boolean momoConfigured() {
        return momoClient.configured();
    }

    @Transactional
    public PaymentLinkResponse createLink(Long reservationId) {
        Reservation res = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không tìm thấy đặt chỗ"));
        Long userId = AuthContext.userId();
        if (!"CUSTOMER".equals(AuthContext.role()) || userId == null || !userId.equals(res.getCustomerUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Không phải chủ giữ chỗ");
        }
        if (!"ONLINE".equals(res.getChannel()) || !"HOLD".equals(res.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION", "Đơn không còn chờ thanh toán");
        }
        var existing = paymentRepository.findFirstByReservationIdAndMethodAndStatusOrderByIdDesc(reservationId, "PAYOS", "PENDING");
        if (existing.isPresent() && existing.get().getCheckoutUrl() != null) {
            Payment p = existing.get();
            return new PaymentLinkResponse(p.getId(), p.getOrderCode(), p.getAmount(), p.getCheckoutUrl(), p.getQrCode());
        }
        if (res.getHoldExpiresAt() != null) {
            res.setHoldExpiresAt(res.getHoldExpiresAt().plusMinutes(waitMinutes));
        }
        Payment payment = new Payment();
        payment.setReservationId(res.getId());
        payment.setMethod("PAYOS");
        payment.setStatus("PENDING");
        payment.setAmount(res.getTotalAmount() == null ? BigDecimal.ZERO : res.getTotalAmount());
        paymentRepository.saveAndFlush(payment);
        long orderCode = payment.getId();
        payment.setOrderCode(orderCode);
        String description = ("Ve " + orderCode);
        if (description.length() > 25) {
            description = description.substring(0, 25);
        }
        PayOsClient.Checkout checkout = payOsClient.createLink(orderCode, payment.getAmount().longValue(), description);
        payment.setCheckoutUrl(checkout.checkoutUrl());
        payment.setQrCode(checkout.qrCode());
        return new PaymentLinkResponse(payment.getId(), orderCode, payment.getAmount(), checkout.checkoutUrl(), checkout.qrCode());
    }

    public void handleWebhook(JsonNode root) {
        JsonNode data = root.path("data");
        String signature = root.path("signature").asText("");
        if (!payOsClient.validWebhook(data, signature)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "PAYMENT_SIGNATURE", "Chữ ký thanh toán không hợp lệ");
        }
        long orderCode = data.path("orderCode").asLong();
        Payment payment = paymentRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không tìm thấy thanh toán"));
        if ("SUCCESS".equals(payment.getStatus())) {
            return;
        }
        boolean paid = "00".equals(root.path("code").asText()) && "00".equals(data.path("code").asText("00"));
        long amount = data.path("amount").asLong();
        if (!paid || payment.getAmount() == null || payment.getAmount().longValue() != amount) {
            mark(payment.getId(), "FAILED");
            return;
        }
        try {
            bookingService.confirmGateway(payment.getReservationId(), "PAYOS");
            mark(payment.getId(), "SUCCESS");
        } catch (ApiException ex) {
            if ("VOUCHER_LIMIT_REACHED".equals(ex.getCode())) {
                mark(payment.getId(), "VOUCHER_LIMIT_REACHED");
            } else {
                mark(payment.getId(), "LATE_PAYMENT");
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void mark(Long paymentId, String status) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow();
        if ("SUCCESS".equals(payment.getStatus())) {
            return;
        }
        payment.setStatus(status);
        if ("SUCCESS".equals(status)) {
            payment.setPaidAt(java.time.LocalDateTime.now());
        }
    }

    @Transactional
    public PaymentLinkResponse createMomo(Long reservationId) {
        Reservation res = ownedOnlineHold(reservationId);
        long amount = (res.getTotalAmount() == null ? BigDecimal.ZERO : res.getTotalAmount())
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();
        var existing = paymentRepository.findFirstByReservationIdAndMethodAndStatusOrderByIdDesc(reservationId, "MOMO", "PENDING");
        if (existing.isPresent() && existing.get().getCheckoutUrl() != null
                && existing.get().getAmount() != null
                && existing.get().getAmount().setScale(0, RoundingMode.HALF_UP).longValue() == amount) {
            extendHold(res);
            Payment p = existing.get();
            return new PaymentLinkResponse(p.getId(), p.getOrderCode(), p.getAmount(), p.getCheckoutUrl(), p.getQrCode());
        }
        if (existing.isPresent()) {
            existing.get().setStatus("FAILED");
        }
        if (amount < 1000) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION", "MoMo yêu cầu số tiền tối thiểu 1.000đ");
        }
        extendHold(res);
        Payment payment = new Payment();
        payment.setReservationId(res.getId());
        payment.setMethod("MOMO");
        payment.setStatus("PENDING");
        payment.setAmount(BigDecimal.valueOf(amount));
        paymentRepository.saveAndFlush(payment);
        String orderId = "VX" + payment.getId();
        payment.setOrderCode(9_000_000_000_000L + payment.getId());
        payment.setGatewayOrderId(orderId);
        String extra = Base64.getEncoder().encodeToString(String.valueOf(res.getId()).getBytes(StandardCharsets.UTF_8));
        MomoClient.Session session = momoClient.create(
                orderId, amount, "Thanh toan ve " + res.getId(), extra, UUID.randomUUID().toString());
        payment.setCheckoutUrl(session.payUrl());
        payment.setQrCode(session.qrCodeUrl());
        return new PaymentLinkResponse(payment.getId(), payment.getOrderCode(), payment.getAmount(),
                session.payUrl(), session.qrCodeUrl());
    }

    @Transactional
    public MomoOutcome handleMomo(Map<String, String> payload) {
        if (!momoClient.validResult(payload)) {
            return new MomoOutcome("invalid", null);
        }
        String orderId = payload.getOrDefault("orderId", "");
        Payment payment = paymentRepository.findByGatewayOrderId(orderId).orElse(null);
        if (payment == null) {
            return new MomoOutcome("invalid", null);
        }
        if ("SUCCESS".equals(payment.getStatus())) {
            return new MomoOutcome("already_paid", payment.getReservationId());
        }
        if (!"0".equals(payload.get("resultCode"))) {
            payment.setStatus("FAILED");
            return new MomoOutcome("failed", payment.getReservationId());
        }
        long amount;
        try {
            amount = Long.parseLong(payload.getOrDefault("amount", ""));
        } catch (NumberFormatException ex) {
            payment.setStatus("FAILED");
            return new MomoOutcome("invalid", payment.getReservationId());
        }
        if (payment.getAmount() == null || payment.getAmount().setScale(0, RoundingMode.HALF_UP).longValue() != amount) {
            payment.setStatus("FAILED");
            return new MomoOutcome("invalid", payment.getReservationId());
        }
        try {
            bookingService.confirmGateway(payment.getReservationId(), "MOMO");
            payment.setStatus("SUCCESS");
            payment.setPaidAt(LocalDateTime.now());
            return new MomoOutcome("paid", payment.getReservationId());
        } catch (ApiException ex) {
            payment.setStatus("VOUCHER_LIMIT_REACHED".equals(ex.getCode()) ? "VOUCHER_LIMIT_REACHED" : "LATE_PAYMENT");
            return new MomoOutcome("late", payment.getReservationId());
        }
    }

    private Reservation ownedOnlineHold(Long reservationId) {
        Reservation res = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không tìm thấy đặt chỗ"));
        Long userId = AuthContext.userId();
        if (!"CUSTOMER".equals(AuthContext.role()) || userId == null || !userId.equals(res.getCustomerUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Không phải chủ giữ chỗ");
        }
        if (!"ONLINE".equals(res.getChannel()) || !"HOLD".equals(res.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION", "Đơn không còn chờ thanh toán");
        }
        return res;
    }

    private void extendHold(Reservation res) {
        LocalDateTime floor = LocalDateTime.now().plusMinutes(waitMinutes);
        if (res.getHoldExpiresAt() == null || res.getHoldExpiresAt().isBefore(floor)) {
            res.setHoldExpiresAt(floor);
        }
    }

    public record MomoOutcome(String code, Long reservationId) {
    }
}
