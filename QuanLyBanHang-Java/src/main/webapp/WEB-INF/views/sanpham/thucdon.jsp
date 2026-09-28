<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <%@ include file="/WEB-INF/views/sanpham/products-head.jspf" %>
    <style>
        .product-form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 14px; }
        .product-form-grid .full { grid-column: 1 / -1; }
        .act-link .linklike { background: none; border: none; color: #f97316; font-weight: 600; cursor: pointer; padding: 0 6px; font-size: 14px; }
        .act-link .linklike:hover { text-decoration: underline; }
    </style>
</head>
<body>
<%@ include file="/WEB-INF/views/layout/app-shell-start.jspf" %>
            <div class="header-bar-sp">
                <div class="left">
                    <a href="${pageContext.request.contextPath}/home" class="btn-back"><i class="fa fa-arrow-left"></i> Về trang chủ</a>
                    <h1><span class="ic"><i class="fas fa-utensils"></i></span> THỰC ĐƠN</h1>
                </div>
                <a class="btn-add" href="${pageContext.request.contextPath}/san-pham/thuc-don"><i class="fa fa-plus"></i> Làm mới</a>
            </div>

            <div class="content-wrap">
                <div class="tbl-wrap">
                    <c:if test="${not empty flashMessage}">
                        <div class="alert ${flashType == 'error' ? 'alert-danger' : 'alert-success'}">${flashMessage}</div>
                    </c:if>
                    <c:if test="${not empty error}">
                        <div class="alert alert-danger">${error}</div>
                    </c:if>

                    <c:set var="categoryFormOpen" value="${category.id > 0 or not empty error}"/>

                    <div class="nl-page-toolbar">
                        <button type="button"
                                class="btn btn-primary nl-toggle-add-btn"
                                id="btnToggleCategoryForm"
                                aria-expanded="${categoryFormOpen ? 'true' : 'false'}"
                                aria-controls="categoryAddPanel">
                            <i class="fas ${categoryFormOpen ? 'fa-chevron-up' : 'fa-plus'}" id="toggleCategoryIcon"></i>
                            <span id="toggleCategoryLabel">${categoryFormOpen ? (category.id > 0 ? 'Ẩn form sửa' : 'Ẩn form thêm') : 'Thêm thực đơn'}</span>
                        </button>
                    </div>

                    <div class="add-form nl-add-panel${categoryFormOpen ? ' is-open' : ''}"
                         id="categoryAddPanel"
                         style="${categoryFormOpen ? '' : 'display:none'}">
                        <h3>${category.id > 0 ? 'Cập nhật thực đơn' : 'Thêm thực đơn'}</h3>
                        <form method="post">
                            <input type="hidden" name="action" value="save">
                            <input type="hidden" name="id" value="${category.id}">
                            <div class="product-form-grid">
                                <div class="form-group">
                                    <label>Tên danh mục *</label>
                                    <input name="ten_danhmuc" value="${category.tenDanhMuc}" required>
                                </div>
                                <div class="form-group">
                                    <label>Thứ tự</label>
                                    <input name="thu_tu" type="number" value="${category.thuTu}">
                                </div>
                                <div class="form-group">
                                    <label>Danh mục cha</label>
                                    <select name="danhmuc_cha">
                                        <option value="">Không có</option>
                                        <c:forEach var="item" items="${categories}">
                                            <option value="${item.id}" ${category.danhMucCha == item.id ? 'selected' : ''}>${item.tenDanhMuc}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="form-group">
                                    <label>Đang hoạt động</label>
                                    <select name="dang_hoat_dong">
                                        <option value="1" ${category.dangHoatDong ? 'selected' : ''}>Có</option>
                                        <option value="0" ${!category.dangHoatDong ? 'selected' : ''}>Không</option>
                                    </select>
                                </div>
                                <div class="form-group full">
                                    <label>Mô tả</label>
                                    <textarea name="mota">${category.moTa}</textarea>
                                </div>
                            </div>
                            <div class="form-actions" style="margin-top:14px;">
                                <button type="submit" class="btn btn-primary">Lưu</button>
                                <a class="btn btn-secondary" href="${pageContext.request.contextPath}/san-pham/thuc-don">Làm mới</a>
                                <button type="button" class="btn btn-secondary nl-cancel-add-btn" id="btnCloseCategoryForm">Đóng</button>
                            </div>
                        </form>
                    </div>

                    <table>
                        <thead>
                        <tr><th>Tên danh mục</th><th>Thứ tự</th><th>Danh mục cha</th><th>Trạng thái</th><th>Tác vụ</th></tr>
                        </thead>
                        <tbody>
                        <c:forEach var="item" items="${categories}">
                            <tr>
                                <td>${item.tenDanhMuc}</td>
                                <td>${item.thuTu}</td>
                                <td>${item.danhMucCha}</td>
                                <td class="status">
                                    <span ${item.dangHoatDong ? '' : 'class="off"'}>${item.dangHoatDong ? 'Đang hoạt động' : 'Đã ẩn'}</span>
                                </td>
                                <td class="act-link">
                                    <form method="post" action="${pageContext.request.contextPath}/san-pham/thuc-don" style="display:inline;">
                                        <input type="hidden" name="action" value="edit">
                                        <input type="hidden" name="id" value="${item.id}">
                                        <button type="submit" class="linklike">Sửa</button>
                                    </form>
                                    <form method="post" action="${pageContext.request.contextPath}/san-pham/thuc-don" style="display:inline;">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="id" value="${item.id}">
                                        <button type="submit" class="linklike" onclick="return confirm('Bạn có chắc muốn xóa danh mục này?')">Xóa</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty categories}">
                            <tr><td colspan="5">Chưa có danh mục nào.</td></tr>
                        </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
<%@ include file="/WEB-INF/views/layout/app-shell-end.jspf" %>
<script src="${pageContext.request.contextPath}/js/products/collapsible-form.js"></script>
<script>
initCollapsibleForm({
    panelId: 'categoryAddPanel',
    toggleBtnId: 'btnToggleCategoryForm',
    closeBtnId: 'btnCloseCategoryForm',
    labelId: 'toggleCategoryLabel',
    iconId: 'toggleCategoryIcon',
    isEdit: ${category.id > 0},
    addLabel: 'Thêm thực đơn',
    startOpen: ${categoryFormOpen},
    scrollOnOpen: ${category.id > 0}
});
</script>
</body>
</html>
