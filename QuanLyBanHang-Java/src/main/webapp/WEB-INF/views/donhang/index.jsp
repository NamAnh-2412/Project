<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <%@ include file="/WEB-INF/views/donhang/orders-head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/layout/app-shell-start.jspf" %>
            <div class="header-bar-donhang">
                <div class="left">
                    <a href="${pageContext.request.contextPath}/home" class="btn-back"><i class="fa fa-arrow-left"></i> Về trang chủ</a>
                    <h1><span class="ic"><i class="fas fa-receipt"></i></span> ĐƠN HÀNG</h1>
                </div>
                <a class="btn-add" href="${pageContext.request.contextPath}/don-hang/create"><i class="fa fa-plus"></i> Thêm đơn hàng mới</a>
            </div>
            <div class="content-wrap orders-list">
                <div class="tbl-wrap orders-list" style="width:100%">
                    <c:if test="${not empty flashMessage}">
                        <div class="alert ${flashType == 'error' ? 'alert-danger' : 'alert-success'}">${flashMessage}</div>
                    </c:if>

                    <div class="toolbar">
                        <form method="get" class="order-filters" action="${pageContext.request.contextPath}/don-hang">
                            <div class="order-filter-search">
                                <i class="fas fa-search" aria-hidden="true"></i>
                                <input type="text" name="kw" placeholder="Mã đơn, tên hoặc SĐT khách..." value="${param.kw}">
                            </div>
                            <div class="order-filter-dates">
                                <input type="date" name="from" value="${param.from}" title="Từ ngày" aria-label="Từ ngày">
                                <span class="order-filter-date-sep">—</span>
                                <input type="date" name="to" value="${param.to}" title="Đến ngày" aria-label="Đến ngày">
                            </div>
                            <select name="status" aria-label="Trạng thái">
                                <c:forEach var="opt" items="${statusOptions}">
                                    <option value="${opt.key}" ${param.status == opt.key ? 'selected' : ''}>${opt.value}</option>
                                </c:forEach>
                            </select>
                            <select name="payment_method" aria-label="Phương thức thanh toán">
                                <option value="">Tất cả PTTT</option>
                                <c:forEach var="opt" items="${paymentOptions}">
                                    <option value="${opt.key}" ${param.payment_method == opt.key ? 'selected' : ''}>${opt.value}</option>
                                </c:forEach>
                            </select>
                            <div class="order-filter-actions">
                                <button class="btn btn-primary" type="submit"><i class="fas fa-filter"></i> Lọc</button>
                                <c:if test="${not empty param.kw or not empty param.from or not empty param.to or not empty param.status or not empty param.payment_method}">
                                    <a class="btn btn-filter-reset" href="${pageContext.request.contextPath}/don-hang">Xóa lọc</a>
                                </c:if>
                            </div>
                        </form>
                    </div>

                    <table class="orders-table table-responsive">
                        <thead>
                        <tr>
                            <th>Mã đơn</th><th>Ngày/T.gian</th><th>Khách hàng</th><th>SDT</th><th>Tổng tiền</th><th>Thanh toán</th><th>Trạng thái</th><th>Tác vụ</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="row" items="${list}">
                            <c:set var="st" value="${row.trangThai}"/>
                            <tr class="order-row-clickable" data-href="${pageContext.request.contextPath}/don-hang/view?id=${row.id}">
                                <td>
                                    <a href="${pageContext.request.contextPath}/don-hang/view?id=${row.id}" class="order-code-link">${row.maDonHang}</a>
                                </td>
                                <td>${row.ngayTao}</td>
                                <td>${empty row.tenKhachHang ? 'Khách lẻ' : row.tenKhachHang}</td>
                                <td>${empty row.sdtKhachHang ? '—' : row.sdtKhachHang}</td>
                                <td><fmt:formatNumber value="${row.thanhTien}" pattern="#,##0" />đ</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${st == 'cho_xac_nhan'}">Chưa thanh toán</c:when>
                                        <c:when test="${st == 'da_huy'}">—</c:when>
                                        <c:when test="${row.phuongThucThanhToan == 'chuyen_khoan'}">Chuyển khoản</c:when>
                                        <c:when test="${row.phuongThucThanhToan == 'the'}">Thẻ</c:when>
                                        <c:when test="${row.phuongThucThanhToan == 'app_online'}">App online</c:when>
                                        <c:otherwise>Tiền mặt</c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <span class="order-status-badge order-status-${st == 'cho_xac_nhan' ? 'pending' : (st == 'da_xac_nhan' ? 'confirmed' : (st == 'dang_chuan_bi' ? 'preparing' : (st == 'san_sang' ? 'ready' : (st == 'hoan_thanh' ? 'completed' : (st == 'da_huy' ? 'cancelled' : 'default')))))}">
                                        <c:choose>
                                            <c:when test="${st == 'cho_xac_nhan'}">Chờ xử lý</c:when>
                                            <c:when test="${st == 'da_xac_nhan'}">Đã xác nhận</c:when>
                                            <c:when test="${st == 'dang_chuan_bi'}">Đang chuẩn bị</c:when>
                                            <c:when test="${st == 'san_sang'}">Sẵn sàng</c:when>
                                            <c:when test="${st == 'hoan_thanh'}">Hoàn thành</c:when>
                                            <c:when test="${st == 'da_huy'}">Đã hủy</c:when>
                                            <c:otherwise>${st}</c:otherwise>
                                        </c:choose>
                                    </span>
                                </td>
                                <td class="order-actions-cell" onclick="event.stopPropagation();">
                                    <a href="${pageContext.request.contextPath}/don-hang/view?id=${row.id}" class="btn-view-order">Xem</a>
                                    <c:if test="${st == 'cho_xac_nhan' || st == 'da_xac_nhan' || st == 'dang_chuan_bi' || st == 'san_sang'}">
                                        <a href="${pageContext.request.contextPath}/don-hang/pay?id=${row.id}" class="btn-pay-order-sm">
                                            <i class="fas fa-credit-card"></i> TT
                                        </a>
                                    </c:if>
                                    <c:choose>
                                        <c:when test="${st != 'da_huy'}">
                                            <a href="${pageContext.request.contextPath}/don-hang/cancel?id=${row.id}" class="btn-cancel"
                                               onclick="return confirm('Bạn có chắc chắn muốn hủy đơn hàng này?')">
                                                <i class="fas fa-times"></i> Hủy
                                            </a>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="order-cancelled-label">Đã hủy</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty list}">
                            <tr><td colspan="8">Chưa có dữ liệu</td></tr>
                        </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
<%@ include file="/WEB-INF/views/layout/app-shell-end.jspf" %>
<script>
document.querySelectorAll('.order-row-clickable').forEach(function(row) {
    row.addEventListener('click', function(e) {
        if (e.target.closest('a, button')) return;
        var href = row.getAttribute('data-href');
        if (href) window.location.href = href;
    });
});
</script>
</body>
</html>
