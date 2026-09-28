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
                <%@ include file="/WEB-INF/views/layout/inline-notice.jspf" %>

                <form method="post">
                    <div class="add-form">
                        <h3>Thông tin khách hàng</h3>
                        <input type="hidden" name="id" value="${customer.id}">
                        <div class="form-group">
                            <label class="form-label required">Tên khách hàng</label>
                            <input type="text" name="ten_khachhang" class="form-input" required value="${customer.tenKhachHang}">
                        </div>
                        <div class="form-group">
                            <label class="form-label">Số điện thoại</label>
                            <input type="tel" name="so_dien_thoai" class="form-input" value="${customer.soDienThoai}">
                        </div>
                        <div class="form-group">
                            <label class="form-label">Email (tùy chọn)</label>
                            <input type="email" name="email" class="form-input" value="${customer.email}">
                        </div>
                        <div class="form-group">
                            <label class="form-label">Ngày sinh</label>
                            <input type="date" name="ngay_sinh" class="form-input" value="${customer.ngaySinh}">
                        </div>
                        <div class="form-group">
                            <label class="form-label">Địa chỉ</label>
                            <input type="text" name="dia_chi" class="form-input" value="${customer.diaChi}">
                        </div>
                        <div class="form-group">
                            <label class="form-label">Ghi chú</label>
                            <textarea name="ghi_chu" class="form-input">${customer.ghiChu}</textarea>
                        </div>
                        <div style="margin-top:12px;display:flex;gap:8px;">
                            <button class="btn btn-primary" type="submit">Lưu</button>
                            <c:if test="${customer.id > 0}">
                                <a class="btn btn-secondary" href="${pageContext.request.contextPath}/khach-hang/view?id=${customer.id}">Xem chi tiết</a>
                            </c:if>
                            <a class="btn btn-secondary" href="${pageContext.request.contextPath}/khach-hang">Hủy</a>
                        </div>
                    </div>
                </form>
            </div>
<%@ include file="/WEB-INF/views/layout/app-shell-end.jspf" %>
</body>
</html>
