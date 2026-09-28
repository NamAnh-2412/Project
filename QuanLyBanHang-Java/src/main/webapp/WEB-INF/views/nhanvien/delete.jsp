<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <%@ include file="/WEB-INF/views/nhanvien/employees-head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/layout/app-shell-start.jspf" %>
            <c:set var="pageHeaderIcon" value="fas fa-user-times"/>
            <c:set var="pageHeaderTitle" value="Xóa nhân viên"/>
            <%@ include file="/WEB-INF/views/layout/page-header.jspf" %>

            <div class="container-main">
                <div class="content-wrap">
                    <div class="tbl-wrap">
                        <div class="add-form">
                            <h3>Xác nhận xóa</h3>
                            <div class="alert alert-danger">
                                Bạn có chắc chắn muốn xóa nhân viên <strong>${employee.tenNhanVien}</strong>?
                                Hành động này không thể hoàn tác.
                            </div>
                            <form method="post">
                                <input type="hidden" name="id" value="${employee.id}">
                                <div style="margin-top:20px;">
                                    <button class="btn btn-danger" type="submit">Xác nhận xóa</button>
                                    <a class="btn btn-secondary" href="${pageContext.request.contextPath}/nhan-vien">Hủy</a>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
<%@ include file="/WEB-INF/views/layout/app-shell-end.jspf" %>
</body>
</html>
