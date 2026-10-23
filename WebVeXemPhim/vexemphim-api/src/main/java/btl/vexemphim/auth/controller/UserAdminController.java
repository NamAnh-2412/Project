package btl.vexemphim.auth.controller;

import btl.vexemphim.auth.dto.AdminUserResponse;
import btl.vexemphim.auth.dto.CreateCashierRequest;
import btl.vexemphim.auth.dto.PasswordResetRequest;
import btl.vexemphim.auth.dto.UserStatusRequest;
import btl.vexemphim.auth.service.UserAdminService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {

    private final UserAdminService userAdminService;

    public UserAdminController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    @GetMapping
    public Page<AdminUserResponse> list(@RequestParam(required = false) String q,
                                        @RequestParam(required = false) String role,
                                        Pageable pageable) {
        return userAdminService.search(q, role, pageable);
    }

    @PostMapping("/cashiers")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminUserResponse createCashier(@Valid @RequestBody CreateCashierRequest req) {
        return userAdminService.createCashier(req);
    }

    @PatchMapping("/{id}/status")
    public AdminUserResponse status(@PathVariable Long id, @Valid @RequestBody UserStatusRequest req) {
        return userAdminService.setStatus(id, req.enabled());
    }

    @PatchMapping("/{id}/password")
    public AdminUserResponse password(@PathVariable Long id, @Valid @RequestBody PasswordResetRequest req) {
        return userAdminService.resetPassword(id, req.password());
    }
}
