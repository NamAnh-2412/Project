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
                    <a href="${pageContext.request.contextPath}/don-hang/view?id=${order.id}" class="btn-back"><i class="fa fa-arrow-left"></i> Về chi tiết đơn</a>
                    <h1><span class="ic"><i class="fas fa-credit-card"></i></span> THANH TOÁN ĐƠN HÀNG</h1>
                </div>
            </div>

            <div class="content-wrap">
                <div class="tbl-wrap">
                    <c:if test="${not empty flashMessage}">
                        <div class="alert ${flashType == 'error' ? 'alert-danger' : 'alert-success'}">${flashMessage}</div>
                    </c:if>

                    <c:set var="totalQty" value="0"/>
                    <c:forEach var="line" items="${order.items}"><c:set var="totalQty" value="${totalQty + line.soLuong}"/></c:forEach>

                    <div class="order-pay-card">
                        <h3 class="order-pay-title"><i class="fas fa-credit-card"></i> Thanh toán đơn hàng</h3>

                        <div class="order-pay-summary">
                            <div class="order-pay-row">
                                <span>Mã đơn</span>
                                <strong>${order.maDonHang}</strong>
                            </div>
                            <div class="order-pay-row">
                                <span>Khách hàng</span>
                                <strong>${empty order.tenKhachHang ? 'Khách lẻ' : order.tenKhachHang}</strong>
                            </div>
                            <div class="order-pay-row">
                                <span>Trạng thái</span>
                                <span class="order-status-badge order-status-pending">Chờ xử lý</span>
                            </div>
                            <div class="order-pay-row">
                                <span>Số lượng</span>
                                <strong>${totalQty} món</strong>
                            </div>
                            <div class="order-pay-row order-pay-total">
                                <span>Tổng thanh toán</span>
                                <strong><fmt:formatNumber value="${order.thanhTien}" pattern="#,##0" />đ</strong>
                            </div>
                        </div>

                        <c:if test="${not empty order.items}">
                            <div class="order-pay-items">
                                <div class="order-pay-items-title">Chi tiết món</div>
                                <ul>
                                    <c:forEach var="line" items="${order.items}">
                                        <li>
                                            <span>${line.tenSanPham}</span>
                                            <span>x${line.soLuong}</span>
                                            <span><fmt:formatNumber value="${line.thanhTien}" pattern="#,##0" />đ</span>
                                        </li>
                                    </c:forEach>
                                </ul>
                            </div>
                        </c:if>

                        <form method="post" action="${pageContext.request.contextPath}/don-hang/pay?id=${order.id}" class="order-pay-form">
                            <label class="form-label required">Phương thức thanh toán</label>
                            <div class="order-pay-methods">
                                <c:forEach var="opt" items="${paymentOptions}">
                                    <label class="order-pay-method-option">
                                        <input type="radio" name="phuong_thuc_thanhtoan" value="${opt.key}"
                                               ${defaultPaymentMethod == opt.key ? 'checked' : ''} required>
                                        <span>${opt.value}</span>
                                    </label>
                                </c:forEach>
                            </div>

                            <div class="order-pay-actions">
                                <button type="submit" class="btn btn-primary" onclick="return confirm('Xác nhận thanh toán đơn hàng này?');">
                                    <i class="fas fa-check"></i> Xác nhận thanh toán
                                </button>
                                <a href="${pageContext.request.contextPath}/don-hang/view?id=${order.id}" class="btn btn-secondary">Quay lại</a>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
<%@ include file="/WEB-INF/views/layout/app-shell-end.jspf" %>
</body>
</html>
