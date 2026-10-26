<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
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
    <form method="post" action="${pageContext.request.contextPath}/chi-nhanh" class="toolbar-form">
        <input name="ma_chi_nhanh" required placeholder="CN02">
        <input name="ten_chi_nhanh" required placeholder="Tên chi nhánh">
        <input name="dia_chi" placeholder="Địa chỉ">
        <input name="so_dien_thoai" placeholder="Điện thoại">
        <button class="btn btn-primary" type="submit">Thêm chi nhánh</button>
    </form>
</div>
<div class="content-card section-gap">
    <h3>Chuyển kho</h3>
    <form method="post" action="${pageContext.request.contextPath}/chi-nhanh" class="toolbar-form">
        <input type="hidden" name="action" value="transfer">
        <select name="tu_chi_nhanh_id"><c:forEach var="branch" items="${branches}"><option value="${branch.id}">${branch.ten}</option></c:forEach></select>
        <select name="den_chi_nhanh_id"><c:forEach var="branch" items="${branches}"><option value="${branch.id}">${branch.ten}</option></c:forEach></select>
        <select name="nguyenlieu_id"><c:forEach var="row" items="${stock}"><option value="${row.nguyenLieuId}">${row.nguyenLieu}</option></c:forEach></select>
        <input name="so_luong" type="number" min="0.01" step="0.01" required placeholder="Số lượng">
        <input name="ghi_chu" placeholder="Ghi chú">
        <button class="btn btn-primary" type="submit">Chuyển</button>
    </form>
</div>
<div class="content-card">
    <table>
        <thead><tr><th>Chi nhánh</th><th>Nguyên liệu</th><th>Tồn</th><th>Tối thiểu</th></tr></thead>
        <tbody>
        <c:forEach var="row" items="${stock}">
            <tr>
                <td>${row.chiNhanh}</td><td>${row.nguyenLieu}</td>
                <td><fmt:formatNumber value="${row.ton}" pattern="#,##0.###"/> ${row.donVi}</td>
                <td><fmt:formatNumber value="${row.toiThieu}" pattern="#,##0.###"/></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>
<%@ include file="/WEB-INF/views/layout/app-shell-end.jspf" %>
</body>
</html>
