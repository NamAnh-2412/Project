<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <%@ include file="/WEB-INF/views/sanpham/products-head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/layout/app-shell-start.jspf" %>
            <div class="header-bar-sp">
                <div class="left">
                    <a href="${pageContext.request.contextPath}/san-pham" class="btn-back"><i class="fa fa-arrow-left"></i> Về danh sách sản phẩm</a>
                    <h1><span class="ic"><i class="fas fa-link"></i></span> LIÊN KẾT NHÓM LỰA CHỌN</h1>
                </div>
            </div>

            <div class="content-wrap">
                <div class="tbl-wrap">
                    <c:if test="${not empty flashMessage}">
                        <div class="alert ${flashType == 'error' ? 'alert-danger' : 'alert-success'}">${flashMessage}</div>
                    </c:if>

                    <div style="background:#f7fafc;padding:15px;border-radius:8px;margin-bottom:20px;">
                        <strong>Sản phẩm:</strong> ${product.tenSanPham}
                    </div>

                    <form method="post" action="${pageContext.request.contextPath}/san-pham/lien-ket-lua-chon">
                        <input type="hidden" name="product_id" value="${product.id}">

                        <div style="margin-bottom:20px;">
                            <h3 style="margin-bottom:15px;">Chọn các nhóm lựa chọn cho sản phẩm:</h3>
                            <c:choose>
                                <c:when test="${empty choiceGroups}">
                                    <p style="color:#999;padding:20px;text-align:center;">
                                        Chưa có nhóm lựa chọn nào.
                                        <a href="${pageContext.request.contextPath}/san-pham/nhom-lua-chon">Tạo nhóm lựa chọn</a>
                                    </p>
                                </c:when>
                                <c:otherwise>
                                    <div style="display:grid;grid-template-columns:repeat(auto-fill,minmax(300px,1fr));gap:15px;">
                                        <c:forEach var="group" items="${choiceGroups}">
                                            <div style="border:1px solid #e5e7eb;border-radius:8px;padding:15px;background:#fff;">
                                                <label style="display:flex;align-items:center;cursor:pointer;">
                                                    <input type="checkbox" name="nhom_lua_chon" value="${group.id}"
                                                           ${linkedGroupMap[group.id] ? 'checked' : ''}
                                                           onchange="toggleRequired(this, ${group.id})"
                                                           style="margin-right:10px;width:18px;height:18px;">
                                                    <div style="flex:1;">
                                                        <div style="font-weight:600;margin-bottom:5px;">${group.tenNhom}</div>
                                                        <c:if test="${not empty group.moTa}">
                                                            <div style="font-size:12px;color:#666;">${group.moTa}</div>
                                                        </c:if>
                                                    </div>
                                                </label>
                                                <div style="margin-top:10px;margin-left:28px;">
                                                    <label style="display:flex;align-items:center;cursor:pointer;font-size:13px;">
                                                        <input type="checkbox" name="bat_buoc" value="${group.id}"
                                                               id="required_${group.id}"
                                                               ${requiredGroupMap[group.id] ? 'checked' : ''}
                                                               style="margin-right:5px;width:16px;height:16px;">
                                                        <span style="color:#666;">Bắt buộc phải chọn</span>
                                                    </label>
                                                </div>
                                            </div>
                                        </c:forEach>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <div style="display:flex;gap:10px;margin-top:20px;">
                            <button class="btn btn-primary" type="submit">Lưu liên kết</button>
                            <a href="${pageContext.request.contextPath}/san-pham" class="btn btn-secondary" style="text-decoration:none;">Hủy</a>
                        </div>
                    </form>
                </div>
            </div>
<%@ include file="/WEB-INF/views/layout/app-shell-end.jspf" %>
<script>
function toggleRequired(checkbox, groupId) {
    const requiredCheckbox = document.getElementById('required_' + groupId);
    if (!requiredCheckbox) {
        return;
    }
    if (!checkbox.checked) {
        requiredCheckbox.checked = false;
        requiredCheckbox.disabled = true;
    } else {
        requiredCheckbox.disabled = false;
    }
}
document.querySelectorAll('input[name="nhom_lua_chon"]').forEach(function(cb) {
    toggleRequired(cb, cb.value);
});
</script>
</body>
</html>
