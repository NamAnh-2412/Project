package btl.vexemphim.booking.controller;

import btl.vexemphim.booking.dto.*;
import btl.vexemphim.booking.service.BookingService;
import btl.vexemphim.booking.service.OnlinePaymentService;
import btl.vexemphim.common.exception.ApiException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Dịch vụ đặt chỗ / vé — nguồn sự thật ghế. */
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final OnlinePaymentService onlinePaymentService;

    public BookingController(BookingService bookingService, OnlinePaymentService onlinePaymentService) {
        this.bookingService = bookingService;
        this.onlinePaymentService = onlinePaymentService;
    }

    @GetMapping("/showtimes/{showtimeId}/seat-map")
    public SeatMapResponse seatMap(@PathVariable Long showtimeId) {
        return bookingService.seatMap(showtimeId);
    }

    @PostMapping("/hold")
    @PreAuthorize("hasAnyRole('CUSTOMER','CASHIER')")
    public ResponseEntity<HoldResponse> hold(@Valid @RequestBody HoldRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.hold(req));
    }

    @PostMapping("/{id}/apply-voucher")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ReservationResponse applyVoucher(@PathVariable Long id, @Valid @RequestBody ApplyVoucherRequest req) {
        return bookingService.applyVoucher(id, req.code());
    }

    @PostMapping("/{id}/create-payment-link")
    @PreAuthorize("hasRole('CUSTOMER')")
    public PaymentLinkResponse paymentLink(@PathVariable Long id) {
        return onlinePaymentService.createLink(id);
    }

    @PostMapping("/{id}/momo")
    @PreAuthorize("hasRole('CUSTOMER')")
    public PaymentLinkResponse momo(@PathVariable Long id) {
        return onlinePaymentService.createMomo(id);
    }

    @GetMapping(value = "/{id}/mock-qr", produces = MediaType.IMAGE_PNG_VALUE)
    @PreAuthorize("hasRole('CUSTOMER')")
    public byte[] mockQr(@PathVariable Long id) {
        return bookingService.mockPaymentQr(id);
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ReservationResponse confirm(@PathVariable Long id, @RequestBody(required = false) ConfirmRequest req) {
        ConfirmRequest body = req == null ? new ConfirmRequest("ONLINE_MOCK") : req;
        String method = body.paymentMethod() == null ? "ONLINE_MOCK" : body.paymentMethod();
        if (onlinePaymentService.momoConfigured() && "ONLINE_MOCK".equals(method)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "USE_MOMO", "Vé online thanh toán bằng MoMo");
        }
        return bookingService.confirmOnline(id, body);
    }

    @PutMapping("/{id}/concessions")
    @PreAuthorize("hasAnyRole('CUSTOMER','CASHIER')")
    public ReservationResponse concessions(@PathVariable Long id, @Valid @RequestBody ConcessionCartRequest req) {
        return bookingService.setConcessions(id, req);
    }

    @PostMapping("/{id}/cancel-tickets")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ReservationResponse cancelTickets(@PathVariable Long id) {
        return bookingService.cancelOnlineTicket(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER','CASHIER','ADMIN')")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        bookingService.cancelHold(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public List<ReservationResponse> my() {
        return bookingService.myBookings();
    }

    @GetMapping("/{id}")
    public ReservationResponse one(@PathVariable Long id) {
        return bookingService.getOne(id);
    }

    @GetMapping("/occupancy")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    public OccupancyResponse occupancy(@RequestParam Long showtimeId) {
        return bookingService.occupancy(showtimeId);
    }
}
