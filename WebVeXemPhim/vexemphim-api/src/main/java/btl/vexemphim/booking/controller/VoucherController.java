package btl.vexemphim.booking.controller;

import btl.vexemphim.booking.dto.VoucherOffer;
import btl.vexemphim.booking.dto.VoucherRequest;
import btl.vexemphim.booking.entity.Voucher;
import btl.vexemphim.booking.service.VoucherService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
public class VoucherController {

    private final VoucherService voucherService;

    public VoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @GetMapping("/available")
    @PreAuthorize("hasRole('CUSTOMER')")
    public List<VoucherOffer> available(@RequestParam(defaultValue = "0") BigDecimal orderAmount) {
        return voucherService.available(orderAmount);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Voucher> list() {
        return voucherService.list();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Voucher create(@Valid @RequestBody VoucherRequest req) {
        return voucherService.create(req);
    }
}
