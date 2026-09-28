<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${pageTitle}</title>
    <%@ include file="/WEB-INF/views/layout/base.jspf" %>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/settings.css"/>
    <script defer src="${pageContext.request.contextPath}/js/auth.js"></script>
</head>
<body>
<%@ include file="/WEB-INF/views/layout/app-shell-start.jspf" %>
            <%@ include file="/WEB-INF/views/layout/page-header.jspf" %>

            <div class="content-card">
                <%@ include file="/WEB-INF/views/layout/inline-notice.jspf" %>

                <form method="post" action="${pageContext.request.contextPath}/doi-mat-khau" class="settings-form">
                    <h3 class="section-title"><i class="fas fa-shield-alt"></i> Cập nhật mật khẩu đăng nhập</h3>
                    <div class="form-group">
                        <label class="form-label required">Mật khẩu hiện tại</label>
                        <div class="password-container">
                            <input type="password" name="current_password" class="form-input" required>
                            <span class="toggle-password auth-toggle-inline" role="button" tabindex="0" aria-label="Hiện hoặc ẩn mật khẩu">👁️</span>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-label required">Mật khẩu mới</label>
                        <div class="password-container">
                            <input type="password" name="new_password" class="form-input" required>
                            <span class="toggle-password auth-toggle-inline" role="button" tabindex="0" aria-label="Hiện hoặc ẩn mật khẩu">👁️</span>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-label required">Nhập lại mật khẩu mới</label>
                        <div class="password-container">
                            <input type="password" name="confirm_password" class="form-input" required>
                            <span class="toggle-password auth-toggle-inline" role="button" tabindex="0" aria-label="Hiện hoặc ẩn mật khẩu">👁️</span>
                        </div>
                    </div>
                    <div class="form-actions">
                        <button type="submit" class="btn btn-primary"><i class="fas fa-save"></i> Cập nhật mật khẩu</button>
                        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/home">Quay lại</a>
                    </div>
                </form>
            </div>
<%@ include file="/WEB-INF/views/layout/app-shell-end.jspf" %>
</body>
</html>
