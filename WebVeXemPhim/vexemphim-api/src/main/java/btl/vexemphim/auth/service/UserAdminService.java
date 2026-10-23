package btl.vexemphim.auth.service;

import btl.vexemphim.auth.dto.AdminUserResponse;
import btl.vexemphim.auth.dto.CreateCashierRequest;
import btl.vexemphim.auth.entity.Role;
import btl.vexemphim.auth.entity.User;
import btl.vexemphim.auth.repository.UserRepository;
import btl.vexemphim.common.exception.ApiException;
import btl.vexemphim.common.security.AuthContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAdminService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<AdminUserResponse> search(String q, String role, Pageable pageable) {
        String query = q == null ? "" : q.trim().toLowerCase();
        Role roleEnum = null;
        if (role != null && !role.isBlank()) {
            try {
                roleEnum = Role.valueOf(role.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION", "Vai trò không hợp lệ");
            }
        }
        return userRepository.search(query, roleEnum, pageable).map(this::toResponse);
    }

    @Transactional
    public AdminUserResponse createCashier(CreateCashierRequest req) {
        if (userRepository.existsByUsername(req.username().trim())) {
            throw new ApiException(HttpStatus.CONFLICT, "USERNAME_TAKEN", "Tên đăng nhập đã có người dùng");
        }
        String email = req.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_TAKEN", "Email đã được dùng");
        }
        User user = new User();
        user.setUsername(req.username().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setRole(Role.CASHIER);
        user.setEnabled(true);
        userRepository.save(user);
        return toResponse(user);
    }

    @Transactional
    public AdminUserResponse setStatus(Long id, boolean enabled) {
        User user = get(id);
        if (!enabled && user.getRole() == Role.ADMIN && userRepository.countByRoleAndEnabled(Role.ADMIN, true) <= 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "LAST_ADMIN", "Không khóa quản trị cuối cùng");
        }
        if (AuthContext.userId() != null && AuthContext.userId().equals(id) && !enabled) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION", "Không tự khóa tài khoản đang đăng nhập");
        }
        user.setEnabled(enabled);
        return toResponse(user);
    }

    @Transactional
    public AdminUserResponse resetPassword(Long id, String password) {
        User user = get(id);
        user.setPassword(passwordEncoder.encode(password));
        return toResponse(user);
    }

    private User get(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không tìm thấy tài khoản"));
    }

    private AdminUserResponse toResponse(User user) {
        return new AdminUserResponse(user.getId(), user.getUsername(), user.getEmail(),
                user.getRole().name(), user.isEnabled(), user.getCreatedAt());
    }
}
