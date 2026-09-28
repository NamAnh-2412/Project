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
    <h2>Đăng nhập</h2>
    <%@ include file="/WEB-INF/views/auth/auth-messages.jspf" %>
    <form method="post" action="${pageContext.request.contextPath}/login" autocomplete="off">
        <input type="text" name="username" placeholder="Tên đăng nhập" value="${username}" required>
        <div class="password-container">
            <input type="password" name="password" id="password" placeholder="Mật khẩu" required>
            <span class="toggle-password" role="button" tabindex="0" aria-label="Hiện hoặc ẩn mật khẩu">👁️</span>
        </div>
        <button type="submit" class="btn">ĐĂNG NHẬP</button>
    </form>
    <div class="small">Chưa có tài khoản? <a href="${pageContext.request.contextPath}/register">Đăng ký</a></div>
</div>
</body>
</html>
