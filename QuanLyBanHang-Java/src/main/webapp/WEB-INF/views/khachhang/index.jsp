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

                <p style="color:#64748b;font-size:14px;margin:0 0 16px 0;">
                    <i class="fas fa-info-circle"></i> Khách hàng được tạo tự động khi đặt hàng. Trang này dùng để xem và phân tích.
                </p>

                <div style="margin-bottom:16px;">
                    <a class="btn btn-primary" href="${pageContext.request.contextPath}/khach-hang/create"><i class="fa fa-plus"></i> Thêm khách hàng</a>
                </div>

                <table class="table-responsive">
                    <thead>
                    <tr>
                        <th>Khách hàng</th>
                        <th>SDT</th>
                        <th>Hạng TV</th>
                        <th>Tổng tiền đã chi</th>
                        <th>Tổng đơn</th>
                        <th>Ngày tạo</th>
                        <th>Tác vụ</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="row" items="${customers}">
                        <c:set var="rk" value="${empty row.loaiThanhVien ? 'thuong' : row.loaiThanhVien}"/>
                        <tr>
                            <td>
                                <div style="display:flex;align-items:center;gap:12px;">
                                    <div style="width:40px;height:40px;border-radius:50%;background:linear-gradient(135deg,#f97316,#ea580c);color:#fff;display:flex;align-items:center;justify-content:center;font-weight:700;font-size:16px;flex-shrink:0;">
                                        ${empty row.tenKhachHang ? '?' : row.tenKhachHang.substring(0, 1)}
                                    </div>
                                    <div>
                                        <div style="font-weight:600;color:#1e293b;">${row.tenKhachHang}</div>
                                        <c:if test="${not empty row.email}">
                                            <div style="font-size:12px;color:#64748b;">${row.email}</div>
                                        </c:if>
                                    </div>
                                </div>
                            </td>
                            <td>${empty row.soDienThoai ? '-' : row.soDienThoai}</td>
                            <td>
                                <span class="rank-badge rank-${rk}">
                                    <c:choose>
                                        <c:when test="${rk == 'vip'}">VIP</c:when>
                                        <c:when test="${rk == 'vang'}">Vàng</c:when>
                                        <c:when test="${rk == 'bac'}">Bạc</c:when>
                                        <c:otherwise>Thường</c:otherwise>
                                    </c:choose>
                                </span>
                            </td>
                            <td style="font-weight:600;color:#059669;"><fmt:formatNumber value="${row.tongTienDaChi}" pattern="#,##0" />đ</td>
                            <td>${row.tongDonHang}</td>
                            <td>${empty row.ngayTao ? '-' : row.ngayTao}</td>
                            <td>
                                <a href="${pageContext.request.contextPath}/khach-hang/view?id=${row.id}" style="color:#2196f3;">Xem</a> |
                                <a href="${pageContext.request.contextPath}/khach-hang/edit?id=${row.id}" style="color:#ff9800;">Sửa</a> |
                                <a href="${pageContext.request.contextPath}/khach-hang/delete?id=${row.id}" style="color:#f44336;" onclick="return confirm('Xóa khách hàng?')">Xóa</a>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty customers}">
                        <tr><td colspan="7" style="text-align:center;padding:40px;color:#94a3b8;">Không có khách hàng nào</td></tr>
                    </c:if>
                    </tbody>
                </table>
            </div>
<%@ include file="/WEB-INF/views/layout/app-shell-end.jspf" %>
</body>
</html>
