package vn.quanlybanhang.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import vn.quanlybanhang.service.AuthService;
import vn.quanlybanhang.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "authController", urlPatterns = {"/login", "/logout", "/register", "/doi-mat-khau"})
public class AuthController extends BaseController {

    private static final long serialVersionUID = 1L;

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String servletPath = request.getServletPath();
        if ("/logout".equals(servletPath)) {
            SessionUtil.logout(request);
            redirect(request, response, "/login");
            return;
        }

        if ("/doi-mat-khau".equals(servletPath)) {
            if (!SessionUtil.isLoggedIn(request)) {
                redirect(request, response, "/login");
                return;
            }
            showChangePasswordPage(request, response, null, null);
            return;
        }

        if (SessionUtil.isLoggedIn(request) && !"/register".equals(servletPath)) {
            redirect(request, response, "/home");
            return;
        }

        if ("/register".equals(servletPath)) {
            showRegisterPage(request, response, null, null, null, null, null, null);
            return;
        }

        showLoginPage(request, response, null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String servletPath = request.getServletPath();

        if ("/register".equals(servletPath)) {
            register(request, response);
            return;
        }

        if ("/doi-mat-khau".equals(servletPath)) {
            changePassword(request, response);
            return;
        }

        login(request, response);
    }

    private void login(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = paramOrEmpty(request, "username");
        String password = paramOrEmpty(request, "password");

        if (username.isBlank() || password.isBlank()) {
            request.setAttribute("username", username);
            showLoginPage(request, response, "Vui lòng nhập đầy đủ thông tin.");
            return;
        }

        try {
            var employee = authService.login(username, password, request.getRemoteAddr());
            if (employee.isPresent()) {
                SessionUtil.login(request, employee.get());
                redirect(request, response, "/home");
                return;
            }

            request.setAttribute("username", username);
            showLoginPage(request, response, "Sai tên đăng nhập hoặc mật khẩu.");
        } catch (SQLException ex) {
            request.setAttribute("username", username);
            showLoginPage(request, response, "Có lỗi xảy ra. Vui lòng thử lại sau.");
        }
    }

    private void showLoginPage(HttpServletRequest request, HttpServletResponse response, String error)
            throws ServletException, IOException {
        request.setAttribute("pageTitle", "Đăng nhập");
        request.setAttribute("error", error);
        render(request, response, "/WEB-INF/views/auth/login.jsp");
    }

    private void showRegisterPage(HttpServletRequest request, HttpServletResponse response, String error, String success,
                                  String fullName, String username, String email, String phone)
            throws ServletException, IOException {
        request.setAttribute("pageTitle", "Đăng ký");
        request.setAttribute("error", error);
        request.setAttribute("success", success);
        request.setAttribute("passwordMinLength", authService.getPasswordMinLength());
        request.setAttribute("ten_nv", trimOrEmpty(fullName));
        request.setAttribute("username", trimOrEmpty(username));
        request.setAttribute("email", trimOrEmpty(email));
        request.setAttribute("sdt", trimOrEmpty(phone));
        render(request, response, "/WEB-INF/views/auth/register.jsp");
    }

    private void register(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String fullName = paramOrEmpty(request, "ten_nv");
        String username = paramOrEmpty(request, "username");
        String email = paramOrEmpty(request, "email");
        String phone = paramOrEmpty(request, "sdt");
        String password = paramOrEmpty(request, "password");
        String confirmPassword = paramOrEmpty(request, "password2");

        try {
            var error = authService.registerEmployee(fullName, username, email, phone, password, confirmPassword);
            if (error.isPresent()) {
                showRegisterPage(request, response, error.get(), null, fullName, username, email, phone);
                return;
            }

            showRegisterPage(request, response,
                    null,
                    "Đăng ký thành công. Bạn có thể đăng nhập ngay bây giờ.",
                    "", "", "", "");
        } catch (SQLException ex) {
            showRegisterPage(request, response, "Có lỗi xảy ra khi tạo tài khoản.", null,
                    fullName, username, email, phone);
        }
    }

    private void showChangePasswordPage(HttpServletRequest request, HttpServletResponse response, String error, String success)
            throws ServletException, IOException {
        request.setAttribute("pageTitle", "Đổi mật khẩu");
        request.setAttribute("pageHeaderTitle", "Đổi mật khẩu");
        applyPageChrome(request, "fas fa-key", null, List.of("settings.css"));
        request.setAttribute("error", error);
        request.setAttribute("success", success);
        render(request, response, "/WEB-INF/views/auth/change-password.jsp");
    }

    private void changePassword(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!SessionUtil.isLoggedIn(request)) {
            redirect(request, response, "/login");
            return;
        }

        String currentPassword = paramOrEmpty(request, "current_password");
        String newPassword = paramOrEmpty(request, "new_password");
        String confirmPassword = paramOrEmpty(request, "confirm_password");

        String validationError = authService.validatePasswordChange(currentPassword, newPassword, confirmPassword);
        if (validationError != null) {
            showChangePasswordPage(request, response, validationError, null);
            return;
        }

        Integer currentUserId = SessionUtil.getCurrentUserId(request);
        if (currentUserId == null) {
            redirect(request, response, "/login");
            return;
        }

        try {
            var error = authService.changePasswordForUser(currentUserId, currentPassword, newPassword);
            if (error.isPresent()) {
                showChangePasswordPage(request, response, error.get(), null);
                return;
            }
            showChangePasswordPage(request, response, null, "Đổi mật khẩu thành công.");
        } catch (SQLException ex) {
            showChangePasswordPage(request, response, "Có lỗi xảy ra khi đổi mật khẩu.", null);
        }
    }
}
