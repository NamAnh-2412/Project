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
<%@ include file="/WEB-INF/views/layout/flash-notice.jspf" %>
<div class="content-card section-gap">
    <h3>Thêm khu vực</h3>
    <form method="post" action="${pageContext.request.contextPath}/ban" class="toolbar-form">
        <input type="hidden" name="action" value="area">
        <input name="ten_khu_vuc" required placeholder="Tầng 1, sân vườn...">
        <button class="btn btn-primary" type="submit">Lưu khu vực</button>
    </form>
    <h3 class="heading-gap">Thêm bàn</h3>
    <form method="post" action="${pageContext.request.contextPath}/ban" class="toolbar-form">
        <input type="hidden" name="action" value="table">
        <select name="khu_vuc_id" required>
            <c:forEach var="area" items="${areas}">
                <option value="${area.id}">${area.tenKhuVuc}</option>
            </c:forEach>
        </select>
        <input name="ten_ban" required placeholder="Bàn 01">
        <input name="so_cho" type="number" min="1" value="4">
        <button class="btn btn-primary" type="submit">Lưu bàn</button>
    </form>
</div>
<div class="content-card">
    <div class="card-grid">
        <c:forEach var="row" items="${tables}">
            <div class="floor-card">
                <strong>${row.tenKhuVuc} · ${row.tenBan}</strong>
                <div class="muted">${row.soCho} chỗ · ${row.trangThai}</div>
                <img src="${row.qr}" alt="QR ${row.tenBan}" width="160" height="160">
                <div class="break-link"><a href="${row.link}">${row.link}</a></div>
                <form method="post" action="${pageContext.request.contextPath}/ban">
                    <input type="hidden" name="action" value="refresh">
                    <input type="hidden" name="id" value="${row.id}">
                    <button class="btn btn-secondary" type="submit">Làm mới mã QR</button>
                </form>
            </div>
        </c:forEach>
    </div>
</div>
<%@ include file="/WEB-INF/views/layout/app-shell-end.jspf" %>
</body>
</html>
