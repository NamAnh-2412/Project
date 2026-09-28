<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <%@ include file="/WEB-INF/views/layout/module-head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/layout/app-shell-start.jspf" %>
            <%@ include file="/WEB-INF/views/layout/page-header.jspf" %>

            <div class="content-card">
                <div class="alert alert-danger">
                    <strong><i class="fas fa-exclamation-triangle"></i> Bạn có chắc chắn muốn xóa khách hàng này?</strong>
                    <div style="margin-top:12px;">Khách hàng: <strong>${customer.tenKhachHang}</strong></div>
                </div>
                <form method="post">
                    <input type="hidden" name="id" value="${customer.id}">
                    <div style="display:flex;gap:10px;margin-top:16px;">
                        <button type="submit" class="btn btn-danger">Xác nhận xóa</button>
                        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/khach-hang">Hủy</a>
                    </div>
                </form>
            </div>
<%@ include file="/WEB-INF/views/layout/app-shell-end.jspf" %>
</body>
</html>
