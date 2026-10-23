package btl.vexemphim.booking.controller;

import btl.vexemphim.booking.dto.*;
import btl.vexemphim.booking.service.PosShiftService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pos")
@PreAuthorize("hasRole('CASHIER')")
public class PosShiftController {

    private final PosShiftService posShiftService;

    public PosShiftController(PosShiftService posShiftService) {
        this.posShiftService = posShiftService;
    }

    @GetMapping("/shifts/current")
    public PosShiftResponse current() {
        return posShiftService.current();
    }

    @PostMapping("/shifts/open")
    public PosShiftResponse open(@Valid @RequestBody OpenShiftRequest req) {
        return posShiftService.open(req);
    }

    @PostMapping("/shifts/close")
    public PosShiftResponse close(@Valid @RequestBody CloseShiftRequest req) {
        return posShiftService.close(req);
    }

    @PostMapping("/snacks")
    public SnackSaleResponse snacks(@Valid @RequestBody SnackSaleRequest req) {
        return posShiftService.sellSnacks(req);
    }
}
