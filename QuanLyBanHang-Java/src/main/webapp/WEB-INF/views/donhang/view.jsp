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
                    <a href="${pageContext.request.contextPath}/don-hang" class="btn-back"><i class="fa fa-arrow-left"></i> Về danh sách đơn</a>
                    <h1><span class="ic"><i class="fas fa-receipt"></i></span> CHI TIẾT ĐƠN HÀNG</h1>
                </div>
            </div>

            <div class="content-wrap order-detail-wrap">
                <c:if test="${not empty flashMessage}">
                    <div class="alert ${flashType == 'error' ? 'alert-danger' : 'alert-success'}" style="max-width:960px;margin:0 auto 16px;">${flashMessage}</div>
                </c:if>

                <c:set var="st" value="${order.trangThai}"/>
                <c:set var="totalQty" value="0"/>
                <c:forEach var="line" items="${order.items}"><c:set var="totalQty" value="${totalQty + line.soLuong}"/></c:forEach>

                <div class="order-detail-card">
                    <div class="order-detail-header">
                        <div>
                            <div class="order-detail-code">${order.maDonHang}</div>
                            <div class="order-detail-time"><i class="fas fa-clock"></i> ${order.ngayTao}</div>
                        </div>
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
                    </div>

                    <div class="order-detail-grid">
                        <div class="order-detail-item">
                            <span class="label">Khách hàng</span>
                            <span class="value">${empty order.tenKhachHang ? 'Khách lẻ' : order.tenKhachHang}</span>
                        </div>
                        <div class="order-detail-item">
                            <span class="label">SDT</span>
                            <span class="value">${empty order.sdtKhachHang ? '—' : order.sdtKhachHang}</span>
                        </div>
                        <div class="order-detail-item">
                            <span class="label">Số lượng sản phẩm</span>
                            <span class="value">${totalQty} món</span>
                        </div>
                        <div class="order-detail-item">
                            <span class="label">Hình thức nhận</span>
                            <span class="value">${order.hinhThucNhan == 'tai_cho' ? 'Tại quầy' : (empty order.hinhThucNhan ? '—' : order.hinhThucNhan)}</span>
                        </div>
                        <div class="order-detail-item">
                            <span class="label">Thanh toán</span>
                            <span class="value">
                                <c:choose>
                                    <c:when test="${st == 'cho_xac_nhan'}">Chưa thanh toán</c:when>
                                    <c:when test="${st == 'da_huy'}">—</c:when>
                                    <c:when test="${order.phuongThucThanhToan == 'chuyen_khoan'}">Chuyển khoản</c:when>
                                    <c:when test="${order.phuongThucThanhToan == 'the'}">Thẻ</c:when>
                                    <c:when test="${order.phuongThucThanhToan == 'app_online'}">App online</c:when>
                                    <c:otherwise>Tiền mặt</c:otherwise>
                                </c:choose>
                            </span>
                        </div>
                    </div>

                    <h3 class="order-detail-section-title"><i class="fas fa-list"></i> Chi tiết sản phẩm</h3>
                    <c:choose>
                        <c:when test="${empty order.items}">
                            <p class="order-detail-empty">Không có sản phẩm trong đơn.</p>
                        </c:when>
                        <c:otherwise>
                            <table class="order-detail-table table-responsive">
                                <thead>
                                <tr>
                                    <th>Sản phẩm</th>
                                    <th>Size</th>
                                    <th>SL</th>
                                    <th>Đơn giá</th>
                                    <th>Thành tiền</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach var="line" items="${order.items}">
                                    <tr>
                                        <td>
                                            <div class="order-item-name">${line.tenSanPham}</div>
                                            <c:if test="${not empty line.luaChonList}">
                                                <div class="order-item-choices">
                                                    <c:forEach var="ch" items="${line.luaChonList}" varStatus="loop">${ch.tenNhom}: ${ch.tenLuaChon}<c:if test="${!loop.last}"> &middot; </c:if></c:forEach>
                                                </div>
                                            </c:if>
                                        </td>
                                        <td>${empty line.size ? '—' : line.size}</td>
                                        <td>${line.soLuong}</td>
                                        <td><fmt:formatNumber value="${line.donGia}" pattern="#,##0" />đ</td>
                                        <td class="order-item-total"><fmt:formatNumber value="${line.thanhTien}" pattern="#,##0" />đ</td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </c:otherwise>
                    </c:choose>

                    <c:if test="${order.giamGia > 0}">
                        <div class="order-detail-total">
                            <span>Giảm ${order.maGiam}</span>
                            <strong>-<fmt:formatNumber value="${order.giamGia}" pattern="#,##0" />đ</strong>
                        </div>
                    </c:if>
                    <div class="order-detail-total">
                        <span>Tổng tiền đơn hàng</span>
                        <strong><fmt:formatNumber value="${order.thanhTien}" pattern="#,##0" />đ</strong>
                    </div>
                    <c:if test="${order.phuongThucThanhToan == 'chuyen_khoan' && transferQr.ready}">
                        <div style="text-align:center;margin-top:16px;">
                            <img src="${transferQr.imageUrl}" alt="QR chuyển khoản" style="width:180px;height:auto;">
                            <div>Nội dung: <b>${transferQr.content}</b></div>
                        </div>
                    </c:if>

                    <div class="order-detail-actions">
                        <a href="${pageContext.request.contextPath}/don-hang/in?id=${order.id}" class="btn btn-secondary">
                            <i class="fas fa-print"></i> In hóa đơn
                        </a>
                        <form method="post" action="${pageContext.request.contextPath}/don-hang/hoa-don" style="display:inline;">
                            <input type="hidden" name="id" value="${order.id}">
                            <button class="btn btn-secondary" type="submit">Xuất hóa đơn điện tử</button>
                        </form>
                        <c:if test="${not empty invoice}">
                            <span class="muted">${invoice.ghiChu}</span>
                        </c:if>
                        <c:if test="${canPayOrder}">
                            <a href="${pageContext.request.contextPath}/don-hang/pay?id=${order.id}" class="btn btn-pay-order">
                                <i class="fas fa-credit-card"></i> Thanh toán
                            </a>
                        </c:if>
                        <c:if test="${st != 'da_huy'}">
                            <a href="${pageContext.request.contextPath}/don-hang/cancel?id=${order.id}" class="btn btn-danger"
                               onclick="return confirm('Bạn có chắc chắn muốn hủy đơn hàng này?')">
                                <i class="fas fa-times"></i> Hủy đơn
                            </a>
                        </c:if>
                        <a href="${pageContext.request.contextPath}/don-hang" class="btn btn-secondary">Về danh sách</a>
                    </div>
                </div>
            </div>
<%@ include file="/WEB-INF/views/layout/app-shell-end.jspf" %>
</body>
</html>
