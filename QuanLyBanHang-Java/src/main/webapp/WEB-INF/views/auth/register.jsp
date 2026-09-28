<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <%@ include file="/WEB-INF/views/auth/auth-head.jspf" %>
</head>
<body>
<div class="auth-wrap">
    <div class="logo">${shopName}</div>
    <h2>Đăng ký tài khoản</h2>
    <%@ include file="/WEB-INF/views/auth/auth-messages.jspf" %>
    <form method="post" action="${pageContext.request.contextPath}/register" autocomplete="off">
        <input type="text" name="ten_nv" placeholder="Tên nhân viên" value="${ten_nv}" required>
        <input type="text" name="username" placeholder="Tên đăng nhập" value="${username}" required>
        <input type="email" name="email" placeholder="Email" value="${email}" required>
        <input type="text" name="sdt" placeholder="Số điện thoại" value="${sdt}" required>
        <div class="password-container">
            <input type="password" name="password" id="password"
                   placeholder="Mật khẩu (ít nhất ${passwordMinLength} ký tự)" required>
            <span class="toggle-password" role="button" tabindex="0" aria-label="Hiện hoặc ẩn mật khẩu">👁️</span>
        </div>
        <div class="password-container">
            <input type="password" name="password2" id="password2" placeholder="Nhập lại mật khẩu" required>
            <span class="toggle-password" role="button" tabindex="0" aria-label="Hiện hoặc ẩn mật khẩu">👁️</span>
        </div>
        <button type="submit" class="btn">ĐĂNG KÝ</button>
    </form>
    <div class="small">Đã có tài khoản? <a href="${pageContext.request.contextPath}/login">Đăng nhập</a></div>
</div>
</body>
</html>
