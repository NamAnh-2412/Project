package btl.vexemphim.booking.controller;

import btl.vexemphim.booking.dto.StaffNoticeResponse;
import btl.vexemphim.booking.service.RefundNoticeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notices")
public class NoticeController {

    private final RefundNoticeService refundNoticeService;

    public NoticeController(RefundNoticeService refundNoticeService) {
        this.refundNoticeService = refundNoticeService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    public List<StaffNoticeResponse> list() {
        return refundNoticeService.listForCurrentRole();
    }
}
