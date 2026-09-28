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

            <%@ include file="/WEB-INF/views/layout/flash-alert.jspf" %>

            <c:set var="rk" value="${empty customer.loaiThanhVien ? 'thuong' : customer.loaiThanhVien}"/>

            <div class="content-card">
                <div class="customer-detail-grid">
                    <div class="customer-info-card">
                        <div class="customer-avatar-large">${empty customer.tenKhachHang ? '?' : customer.tenKhachHang.substring(0, 1)}</div>
                        <div class="customer-profile-name">${customer.tenKhachHang}</div>
                        <div class="customer-profile-rank">
                            <span class="rank-badge rank-${rk}">
                                <c:choose>
                                    <c:when test="${rk == 'vip'}">VIP</c:when>
                                    <c:when test="${rk == 'vang'}">Vàng</c:when>
                                    <c:when test="${rk == 'bac'}">Bạc</c:when>
                                    <c:otherwise>Thường</c:otherwise>
                                </c:choose>
                            </span>
                        </div>

                        <div class="customer-info-list">
                            <div class="info-item">
                                <span class="info-label">Số điện thoại</span>
                                <span class="info-value">${empty customer.soDienThoai ? '-' : customer.soDienThoai}</span>
                            </div>
                            <c:if test="${not empty customer.email}">
                                <div class="info-item">
                                    <span class="info-label">Email</span>
                                    <span class="info-value">${customer.email}</span>
                                </div>
                            </c:if>
                            <c:if test="${not empty customer.ngaySinh}">
                                <div class="info-item">
                                    <span class="info-label">Ngày sinh</span>
                                    <span class="info-value">${customer.ngaySinh}</span>
                                </div>
                            </c:if>
                            <c:if test="${not empty customer.diaChi}">
                                <div class="info-item">
                                    <span class="info-label">Địa chỉ</span>
                                    <span class="info-value">${customer.diaChi}</span>
                                </div>
                            </c:if>
                            <div class="info-item">
                                <span class="info-label">Ngày tạo</span>
                                <span class="info-value">${empty customer.ngayTao ? '-' : customer.ngayTao}</span>
                            </div>
                            <c:if test="${not empty customer.ghiChu}">
                                <div class="info-item">
                                    <span class="info-label">Ghi chú</span>
                                    <span class="info-value" style="white-space:pre-wrap;">${customer.ghiChu}</span>
                                </div>
                            </c:if>
                        </div>

                        <div style="margin-top:20px;display:flex;gap:8px;flex-wrap:wrap;">
                            <a href="${pageContext.request.contextPath}/khach-hang/edit?id=${customer.id}" class="btn btn-primary">Sửa thông tin</a>
                            <a href="${pageContext.request.contextPath}/khach-hang" class="btn btn-secondary">Về danh sách</a>
                        </div>
                    </div>

                    <div>
                        <div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(160px,1fr));gap:16px;margin-bottom:24px;">
                            <div style="background:#fff;border:1px solid #e2e8f0;border-radius:12px;padding:20px;">
                                <div style="font-size:13px;color:#64748b;margin-bottom:6px;">Tổng tiền đã chi</div>
                                <div style="font-size:22px;font-weight:700;color:#059669;"><fmt:formatNumber value="${stats.totalSpent}" pattern="#,##0" />đ</div>
                            </div>
                            <div style="background:#fff;border:1px solid #e2e8f0;border-radius:12px;padding:20px;">
                                <div style="font-size:13px;color:#64748b;margin-bottom:6px;">Số đơn hàng</div>
                                <div style="font-size:22px;font-weight:700;color:#2196f3;">${stats.totalOrders}</div>
                            </div>
                            <div style="background:#fff;border:1px solid #e2e8f0;border-radius:12px;padding:20px;">
                                <div style="font-size:13px;color:#64748b;margin-bottom:6px;">Đơn trung bình</div>
                                <div style="font-size:22px;font-weight:700;color:#f97316;"><fmt:formatNumber value="${stats.avgOrder}" pattern="#,##0" />đ</div>
                            </div>
                        </div>

                        <div class="customer-history-card">
                            <div class="card-title"><i class="fas fa-receipt"></i> Lịch sử đơn hàng (10 gần nhất)</div>
                            <c:choose>
                                <c:when test="${empty orders}">
                                    <p style="color:#94a3b8;text-align:center;padding:30px 0;">Chưa có đơn hàng nào</p>
                                </c:when>
                                <c:otherwise>
                                    <table class="table-responsive">
                                        <thead>
                                        <tr>
                                            <th>Mã đơn</th>
                                            <th>Ngày</th>
                                            <th>Thanh toán</th>
                                            <th>Trạng thái</th>
                                            <th>Thành tiền</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <c:forEach var="order" items="${orders}">
                                            <tr>
                                                <td style="font-weight:600;"><a href="${pageContext.request.contextPath}/don-hang/view?id=${order.id}">${order.maDonHang}</a></td>
                                                <td>${order.ngayTao}</td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${order.phuongThucThanhToan == 'chuyen_khoan'}">Chuyển khoản</c:when>
                                                        <c:when test="${order.phuongThucThanhToan == 'the'}">Thẻ</c:when>
                                                        <c:when test="${order.phuongThucThanhToan == 'app_online'}">App online</c:when>
                                                        <c:when test="${order.trangThai != 'hoan_thanh'}">Chưa thanh toán</c:when>
                                                        <c:otherwise>Tiền mặt</c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>
                                                    <span class="order-status-badge ${order.trangThai == 'da_huy' ? 'order-status-cancelled' : 'order-status-completed'}">
                                                        ${order.trangThai == 'da_huy' ? 'Đã hủy' : 'Hoàn thành'}
                                                    </span>
                                                </td>
                                                <td style="font-weight:600;color:#059669;"><fmt:formatNumber value="${order.thanhTien}" pattern="#,##0" />đ</td>
                                            </tr>
                                        </c:forEach>
                                        </tbody>
                                    </table>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>
            </div>
<%@ include file="/WEB-INF/views/layout/app-shell-end.jspf" %>
</body>
</html>
