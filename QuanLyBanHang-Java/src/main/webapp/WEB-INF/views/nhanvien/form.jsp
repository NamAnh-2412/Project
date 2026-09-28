<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <%@ include file="/WEB-INF/views/nhanvien/employees-head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/layout/app-shell-start.jspf" %>
            <c:set var="pageHeaderIcon" value="fas fa-user-tie"/>
            <c:set var="pageHeaderTitle" value="${empty employee.id ? 'Thêm nhân viên' : 'Sửa nhân viên'}"/>
            <%@ include file="/WEB-INF/views/layout/page-header.jspf" %>

            <div class="container-main">
                <div class="content-wrap">
                    <div class="tbl-wrap">
                        <c:if test="${not empty error}">
                            <div class="alert alert-danger">${error}</div>
                        </c:if>
                        <form method="post">
                            <input type="hidden" name="id" value="${employee.id}">
                            <div class="add-form">
                                <h3>Thông tin nhân viên</h3>
                                <c:if test="${not empty employee.id}">
                                    <label>Mã nhân viên</label>
                                    <input type="text" name="ma_nhanvien" value="${employee.maNhanVien}" placeholder="Mã nhân viên">
                                    <label>Tên đăng nhập</label>
                                    <input type="text" name="ten_dang_nhap" value="${employee.tenDangNhap}">
                                </c:if>
                                <label>Tên nhân viên</label>
                                <input type="text" name="ten_nhanvien" value="${employee.tenNhanVien}" required>
                                <label>Chức vụ</label>
                                <select name="chuc_vu">
                                    <option value="nhan_vien" ${employee.chucVu == 'nhan_vien' ? 'selected' : ''}>Nhân viên</option>
                                    <option value="cua_hang_truong" ${employee.chucVu == 'cua_hang_truong' ? 'selected' : ''}>Cửa hàng trưởng</option>
                                </select>
                                <label>Số điện thoại</label>
                                <input type="text" name="so_dien_thoai" value="${employee.soDienThoai}">
                                <label>Email</label>
                                <input type="email" name="email" value="${employee.email}">
                                <c:if test="${empty employee.id}">
                                    <label>Mật khẩu</label>
                                    <input type="password" name="mat_khau" placeholder="Ít nhất 6 ký tự" required>
                                    <label>Nhập lại mật khẩu</label>
                                    <input type="password" name="xac_nhan_mat_khau" required>
                                </c:if>
                                <label>Ngày vào làm <span style="color:red;">*</span></label>
                                <input type="date" name="ngay_vao_lam" value="${employee.ngayVaoLam}" required>
                                <label>Ngày sinh</label>
                                <input type="date" name="ngay_sinh" value="${employee.ngaySinh}">
                                <label>Trạng thái</label>
                                <select name="trang_thai">
                                    <option value="dang_lam" ${employee.trangThai == 'dang_lam' ? 'selected' : ''}>Đang làm việc</option>
                                    <option value="da_nghi" ${employee.trangThai == 'da_nghi' ? 'selected' : ''}>Đã nghỉ việc</option>
                                </select>
                                <div style="margin-top:12px;">
                                    <button class="btn btn-primary" type="submit">Lưu</button>
                                    <a class="btn btn-secondary" href="${pageContext.request.contextPath}/nhan-vien">Hủy</a>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
<%@ include file="/WEB-INF/views/layout/app-shell-end.jspf" %>
</body>
</html>
