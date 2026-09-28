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

            <div class="content-card">
                <%@ include file="/WEB-INF/views/layout/flash-notice.jspf" %>

                <a href="${pageContext.request.contextPath}/nha-cung-cap/create" class="main-link"><i class="fas fa-plus"></i> Thêm nhà cung cấp</a>

                <form method="get" style="margin:20px 0;display:flex;gap:10px;align-items:center;flex-wrap:wrap;">
                    <input type="text" name="kw" placeholder="Tìm kiếm (tên, SDT, email)..." value="${keyword}"
                           style="flex:1;min-width:220px;padding:8px 12px;border:1px solid #d2d8ea;border-radius:6px;">
                    <select name="status" style="padding:8px 12px;border:1px solid #d2d8ea;border-radius:6px;">
                        <option value="">Tất cả trạng thái</option>
                        <option value="dang_hop_tac" ${status == 'dang_hop_tac' ? 'selected' : ''}>Đang hợp tác</option>
                        <option value="ngung_hop_tac" ${status == 'ngung_hop_tac' ? 'selected' : ''}>Ngừng hợp tác</option>
                    </select>
                    <button type="submit" class="btn btn-primary" style="padding:8px 16px;">Tìm</button>
                    <c:if test="${not empty keyword || not empty status}">
                        <a href="${pageContext.request.contextPath}/nha-cung-cap" class="btn btn-secondary" style="padding:8px 16px;text-decoration:none;">Xóa bộ lọc</a>
                    </c:if>
                </form>

                <table>
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Tên nhà cung cấp</th>
                        <th>SDT</th>
                        <th>Email</th>
                        <th>Trạng thái</th>
                        <th>Ngày tạo</th>
                        <th>Tác vụ</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="supplier" items="${suppliers}">
                        <tr>
                            <td>${supplier.id}</td>
                            <td>${supplier.tenNhaCungCap}</td>
                            <td>${empty supplier.soDienThoai ? '-' : supplier.soDienThoai}</td>
                            <td>${empty supplier.email ? '-' : supplier.email}</td>
                            <td>
                                <span style="padding:4px 12px;border-radius:999px;font-size:.88em;font-weight:600;${supplier.trangThai == 'dang_hop_tac' ? 'background:#d4edda;color:#155724;' : 'background:#f8d7da;color:#721c24;'}">
                                    ${supplier.trangThai == 'dang_hop_tac' ? 'Đang hợp tác' : 'Ngừng hợp tác'}
                                </span>
                            </td>
                            <td>${empty supplier.ngayTao ? '-' : supplier.ngayTao}</td>
                            <td>
                                <a href="${pageContext.request.contextPath}/nha-cung-cap/view?id=${supplier.id}" style="color:#2196f3;">Xem</a> |
                                <a href="${pageContext.request.contextPath}/nha-cung-cap/edit?id=${supplier.id}" style="color:#ff9800;">Sửa</a> |
                                <a href="${pageContext.request.contextPath}/nha-cung-cap/delete?id=${supplier.id}" style="color:#f44336;" onclick="return confirm('Xóa nhà cung cấp?')">Xóa</a>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty suppliers}">
                        <tr><td colspan="7">Không có dữ liệu</td></tr>
                    </c:if>
                    </tbody>
                </table>
            </div>
<%@ include file="/WEB-INF/views/layout/app-shell-end.jspf" %>
</body>
</html>
