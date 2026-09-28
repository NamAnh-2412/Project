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
                        <div class="add-form">
                            <h3>Xóa nhà cung cấp</h3>
                            <div style="padding:15px;background:#f8d7da;border:1px solid #dc3545;border-radius:8px;margin-bottom:20px;">
                                <strong style="color:#721c24;">Bạn có chắc chắn muốn xử lý nhà cung cấp này?</strong>
                                <p style="margin:10px 0 0 0;color:#721c24;">
                                    Tên: <strong>${supplier.tenNhaCungCap}</strong><br>
                                    Nếu đã phát sinh phiếu nhập, hệ thống sẽ chuyển sang trạng thái ngừng hợp tác.
                                </p>
                            </div>
                            <form method="post">
                                <input type="hidden" name="id" value="${supplier.id}">
                                <div style="margin-top:20px;">
                                    <button class="btn btn-danger" type="submit">Xác nhận</button>
                                    <a class="btn btn-secondary" href="${pageContext.request.contextPath}/nha-cung-cap">Hủy</a>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
<%@ include file="/WEB-INF/views/layout/app-shell-end.jspf" %>
</body>
</html>
