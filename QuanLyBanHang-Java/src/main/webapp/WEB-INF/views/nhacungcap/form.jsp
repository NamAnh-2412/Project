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

            <div class="container-main">
                <div class="content-wrap">
                    <div class="tbl-wrap">
                        <%@ include file="/WEB-INF/views/layout/inline-notice.jspf" %>
                        <form method="post">
                            <input type="hidden" name="id" value="${supplier.id}">
                            <div class="add-form">
                                <h3>Thông tin nhà cung cấp</h3>
                                <label>Tên nhà cung cấp <span style="color:red">*</span></label>
                                <input type="text" name="ten_nhacungcap" value="${supplier.tenNhaCungCap}" required placeholder="Nhập tên nhà cung cấp">
                                <label>Số điện thoại</label>
                                <input type="text" name="so_dien_thoai" value="${supplier.soDienThoai}" placeholder="0123456789">
                                <label>Email</label>
                                <input type="email" name="email" value="${supplier.email}" placeholder="contact@example.com">
                                <label>Địa chỉ</label>
                                <textarea name="dia_chi" rows="2" placeholder="Nhập địa chỉ...">${supplier.diaChi}</textarea>
                                <label>Mã số thuế</label>
                                <input type="text" name="ma_so_thue" value="${supplier.maSoThue}" placeholder="Mã số thuế (nếu có)">
                                <label>Người liên hệ</label>
                                <input type="text" name="nguoi_lien_he" value="${supplier.nguoiLienHe}" placeholder="Tên người liên hệ">
                                <label>Trạng thái</label>
                                <select name="trang_thai">
                                    <option value="dang_hop_tac" ${supplier.trangThai == 'dang_hop_tac' ? 'selected' : ''}>Đang hợp tác</option>
                                    <option value="ngung_hop_tac" ${supplier.trangThai == 'ngung_hop_tac' ? 'selected' : ''}>Ngừng hợp tác</option>
                                </select>
                                <label>Ghi chú</label>
                                <textarea name="ghi_chu" rows="3" placeholder="Ghi chú về nhà cung cấp...">${supplier.ghiChu}</textarea>
                                <div style="margin-top:12px;">
                                    <button class="btn btn-primary" type="submit">Lưu</button>
                                    <a class="btn btn-secondary" href="${pageContext.request.contextPath}/nha-cung-cap">Hủy</a>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
<%@ include file="/WEB-INF/views/layout/app-shell-end.jspf" %>
</body>
</html>
