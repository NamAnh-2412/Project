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
                    <h1><span class="ic"><i class="fas fa-ban"></i></span> HỦY ĐƠN HÀNG</h1>
                </div>
            </div>

            <div class="content-wrap">
                <div class="tbl-wrap">
                    <div class="alert alert-danger">
                        <strong><i class="fas fa-exclamation-triangle"></i> Bạn có chắc chắn muốn hủy đơn hàng này?</strong>
                        <div style="margin-top:15px;padding:15px;background:#fff;border-radius:8px">
                            <div style="margin-bottom:10px"><strong>Mã đơn:</strong> ${order.maDonHang}</div>
                            <div style="margin-bottom:10px"><strong>Ngày tạo:</strong> ${order.ngayTao}</div>
                            <div style="margin-bottom:10px"><strong>Khách hàng:</strong> ${empty order.tenKhachHang ? 'Khách lẻ' : order.tenKhachHang}</div>
                            <div style="margin-bottom:10px"><strong>SDT:</strong> ${empty order.sdtKhachHang ? '—' : order.sdtKhachHang}</div>
                            <div style="margin-bottom:10px"><strong>Tổng tiền:</strong> <span style="color:#f44336;font-weight:700"><fmt:formatNumber value="${order.thanhTien}" pattern="#,##0" />đ</span></div>
                        </div>
                        <div style="margin-top:15px;padding:10px;background:#fff3cd;border-left:4px solid #ffc107;border-radius:4px">
                            <strong>Lưu ý:</strong>
                            <ul style="margin:10px 0 0 20px;padding:0">
                                <li>Đơn hàng sẽ được đánh dấu là "Đã hủy"</li>
                                <c:if test="${order.khachHangId != null && order.trangThai == 'hoan_thanh'}">
                                    <li>Tổng chi tiêu và hạng thành viên của khách sẽ được điều chỉnh lại</li>
                                </c:if>
                                <li>Hành động này không thể hoàn tác</li>
                            </ul>
                        </div>
                    </div>
                    <form method="post" style="margin-top:20px">
                        <input type="hidden" name="id" value="${order.id}">
                        <div style="display:flex;gap:10px">
                            <button class="btn btn-danger" type="submit"><i class="fas fa-times"></i> Xác nhận hủy đơn hàng</button>
                            <a class="btn btn-secondary" href="${pageContext.request.contextPath}/don-hang"><i class="fas fa-arrow-left"></i> Quay lại</a>
                        </div>
                    </form>
                </div>
            </div>
<%@ include file="/WEB-INF/views/layout/app-shell-end.jspf" %>
</body>
</html>
