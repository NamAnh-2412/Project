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
            <c:set var="pageHeaderTitle" value="Quản lý nhân viên"/>
            <%@ include file="/WEB-INF/views/layout/page-header.jspf" %>

            <div class="content-card">
                <c:if test="${not empty flashMessage}">
                    <div class="alert ${flashType == 'error' ? 'alert-danger' : 'alert-success'}">${flashMessage}</div>
                </c:if>

                <c:if test="${currentRole == 'cua_hang_truong'}">
                    <a href="${pageContext.request.contextPath}/nhan-vien/create" class="main-link"><i class="fas fa-plus"></i> Thêm nhân viên mới</a>
                </c:if>

                <table class="table-responsive">
                    <thead>
                    <tr>
                        <th>ID</th>
                        <c:if test="${currentRole == 'cua_hang_truong'}"><th>Mã NV</th></c:if>
                        <th>Tên NV</th>
                        <th>Chức vụ</th>
                        <th>SDT</th>
                        <c:if test="${currentRole == 'cua_hang_truong'}"><th>Email</th></c:if>
                        <c:if test="${currentRole == 'cua_hang_truong'}"><th>Ngày vào</th></c:if>
                        <c:if test="${currentRole == 'cua_hang_truong'}"><th>Ngày sinh</th></c:if>
                        <th>Trạng thái</th>
                        <th>Tác vụ</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="employee" items="${employees}">
                        <tr>
                            <td>${employee.id}</td>
                            <c:if test="${currentRole == 'cua_hang_truong'}"><td>${employee.maNhanVien}</td></c:if>
                            <td>${employee.tenNhanVien}</td>
                            <td>
                                <span id="role-${employee.id}">
                                    <c:choose>
                                        <c:when test="${employee.chucVu == 'cua_hang_truong'}">Cửa hàng trưởng</c:when>
                                        <c:otherwise>Nhân viên</c:otherwise>
                                    </c:choose>
                                </span>
                                <c:if test="${currentRole == 'cua_hang_truong' && employee.id != sessionScope.nhanvien_id}">
                                    <button type="button" id="btn-role-${employee.id}"
                                            onclick="changeRole(${employee.id}, '${employee.tenNhanVien}', '${employee.chucVu}', this)"
                                            style="padding:4px 8px;background:#f97316;color:#fff;border:none;border-radius:4px;font-size:12px;cursor:pointer;margin-left:8px;"
                                            title="Đổi chức vụ">
                                        <i class="fas fa-exchange-alt"></i> Đổi
                                    </button>
                                </c:if>
                            </td>
                            <td>${employee.soDienThoai}</td>
                            <c:if test="${currentRole == 'cua_hang_truong'}"><td>${employee.email}</td></c:if>
                            <c:if test="${currentRole == 'cua_hang_truong'}"><td>${employee.ngayVaoLam}</td></c:if>
                            <c:if test="${currentRole == 'cua_hang_truong'}"><td>${employee.ngaySinh}</td></c:if>
                            <td>${employee.trangThai}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${currentRole == 'cua_hang_truong'}">
                                        <a href="${pageContext.request.contextPath}/nhan-vien/edit?id=${employee.id}">Sửa</a>
                                        |
                                        <a href="${pageContext.request.contextPath}/nhan-vien/delete?id=${employee.id}" onclick="return confirm('Xóa nhân viên?')">Xóa</a>
                                    </c:when>
                                    <c:otherwise><span style="color:#999;">Không có quyền</span></c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty employees}">
                        <tr><td colspan="${currentRole == 'cua_hang_truong' ? 10 : 6}">Không có dữ liệu</td></tr>
                    </c:if>
                    </tbody>
                </table>
            </div>
<%@ include file="/WEB-INF/views/layout/app-shell-end.jspf" %>
<script>
function changeRole(employeeId, employeeName, currentRole, buttonElement) {
    const currentRoleText = currentRole === 'cua_hang_truong' ? 'Cửa hàng trưởng' : 'Nhân viên';
    const newRoleText = currentRole === 'cua_hang_truong' ? 'Nhân viên' : 'Cửa hàng trưởng';
    if (!confirm('Bạn có chắc muốn đổi chức vụ của "' + employeeName + '" từ "' + currentRoleText + '" sang "' + newRoleText + '"?')) {
        return;
    }

    const button = buttonElement || document.getElementById('btn-role-' + employeeId);
    if (button) {
        button.disabled = true;
        button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang xử lý...';
    }

    fetch('${pageContext.request.contextPath}/nhan-vien/change-role', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
        body: JSON.stringify({ id: employeeId })
    })
    .then(response => response.json())
    .then(data => {
        if (!data.success) {
            throw new Error(data.message || data.error || 'Không thể đổi chức vụ');
        }
        const roleElement = document.getElementById('role-' + employeeId);
        if (roleElement) {
            roleElement.textContent = data.new_role === 'cua_hang_truong' ? 'Cửa hàng trưởng' : 'Nhân viên';
        }
        if (button) {
            button.disabled = false;
            button.innerHTML = '<i class="fas fa-exchange-alt"></i> Đổi';
            button.setAttribute('onclick', "changeRole(" + employeeId + ", '" + employeeName.replace(/'/g, "\\'") + "', '" + data.new_role + "', this)");
        }
        alert('Đã đổi chức vụ thành công!');
    })
    .catch(error => {
        alert(error.message || 'Có lỗi xảy ra khi đổi chức vụ');
        if (button) {
            button.disabled = false;
            button.innerHTML = '<i class="fas fa-exchange-alt"></i> Đổi';
        }
    });
}
</script>
</body>
</html>
