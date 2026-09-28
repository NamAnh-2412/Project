<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <%@ include file="/WEB-INF/views/sanpham/products-head.jspf" %>
    <style>
        .image-preview {
            margin-top: 10px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 120px;
            height: 120px;
            border: 1px dashed #cbd5e1;
            border-radius: 14px;
            background: #f8fafc;
            overflow: hidden;
        }
        .image-preview img { width: 100%; height: 100%; object-fit: cover; }
        .choice-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
            gap: 14px;
            margin-bottom: 16px;
        }
        .choice-card { border: 1px solid #e2e8f0; border-radius: 14px; padding: 14px; background: #fff; }
        .choice-card p { margin: 6px 0 0; color: #64748b; font-size: 13px; }
        .product-thumb { width: 52px; height: 52px; border-radius: 10px; object-fit: cover; border: 1px solid #e2e8f0; }
        .product-form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 14px; }
        .product-form-grid .full { grid-column: 1 / -1; }
        .size-actions { display: flex; gap: 8px; flex-wrap: wrap; }
    </style>
</head>
<body>
<%@ include file="/WEB-INF/views/layout/app-shell-start.jspf" %>
            <div class="header-bar-sp">
                <div class="left">
                    <a href="${pageContext.request.contextPath}/home" class="btn-back"><i class="fa fa-arrow-left"></i> Về trang chủ</a>
                    <h1><span class="ic"><i class="fas fa-box"></i></span> SẢN PHẨM</h1>
                </div>
                <a class="btn-add" href="${pageContext.request.contextPath}/san-pham"><i class="fa fa-plus"></i> Làm mới</a>
            </div>

            <div class="content-wrap">
                <div class="tbl-wrap">
                    <c:if test="${not empty flashMessage}">
                        <div class="alert ${flashType == 'error' ? 'alert-danger' : 'alert-success'}">${flashMessage}</div>
                    </c:if>
                    <c:if test="${not empty error}">
                        <div class="alert alert-danger">${error}</div>
                    </c:if>

                    <c:set var="productFormOpen" value="${product.id > 0 or not empty error}"/>

                    <div class="nl-page-toolbar">
                        <button type="button"
                                class="btn btn-primary nl-toggle-add-btn"
                                id="btnToggleProductForm"
                                aria-expanded="${productFormOpen ? 'true' : 'false'}"
                                aria-controls="productAddPanel">
                            <i class="fas ${productFormOpen ? 'fa-chevron-up' : 'fa-plus'}" id="toggleProductIcon"></i>
                            <span id="toggleProductLabel">${productFormOpen ? (product.id > 0 ? 'Ẩn form sửa' : 'Ẩn form thêm') : 'Thêm sản phẩm'}</span>
                        </button>
                    </div>

                    <div class="add-form nl-add-panel${productFormOpen ? ' is-open' : ''}"
                         id="productAddPanel"
                         style="${productFormOpen ? '' : 'display:none'}">
                        <h3>${product.id > 0 ? 'Cập nhật sản phẩm' : 'Thêm sản phẩm'}</h3>
                        <form method="post">
                            <input type="hidden" name="action" value="save">
                            <input type="hidden" name="id" value="${product.id}">
                            <div class="product-form-grid">
                                <div class="form-group">
                                    <label>Mã sản phẩm</label>
                                    <input name="ma_sanpham" value="${product.maSanPham}" placeholder="Để trống để tự sinh">
                                </div>
                                <div class="form-group">
                                    <label>Tên sản phẩm *</label>
                                    <input name="ten_sanpham" value="${product.tenSanPham}" required>
                                </div>
                                <div class="form-group">
                                    <label>Danh mục *</label>
                                    <select name="danhmuc_id" required>
                                        <option value="">Chọn danh mục</option>
                                        <c:forEach var="item" items="${categories}">
                                            <option value="${item.id}" ${product.danhMucId == item.id ? 'selected' : ''}>${item.tenDanhMuc}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="form-group">
                                    <label>Giá bán cơ bản *</label>
                                    <input type="number" step="0.01" name="gia_ban" value="${product.giaBan}" required>
                                </div>
                                <div class="form-group full">
                                    <label>Ảnh sản phẩm</label>
                                    <input type="hidden" name="anh_url" id="imageUrlInput" value="${product.anhUrl}">
                                    <div style="display:flex;gap:12px;align-items:center;flex-wrap:wrap;">
                                        <label for="productImageFile" class="btn btn-secondary" style="cursor:pointer;margin:0;">
                                            <i class="fas fa-upload"></i> Chọn ảnh
                                        </label>
                                        <input type="file" id="productImageFile" accept="image/*" style="display:none;">
                                        <span id="imageUploadStatus" style="color:#64748b;font-size:13px;"></span>
                                    </div>
                                    <div class="image-preview" id="imagePreviewBox">
                                        <c:choose>
                                            <c:when test="${not empty product.anhUrl}">
                                                <img id="imagePreview" src="${pageContext.request.contextPath}/${product.anhUrl}" alt="Preview">
                                            </c:when>
                                            <c:otherwise>
                                                <span style="color:#94a3b8;">Chưa có ảnh</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label>Đang hoạt động</label>
                                    <select name="dang_hoat_dong">
                                        <option value="1" ${product.dangHoatDong ? 'selected' : ''}>Có</option>
                                        <option value="0" ${!product.dangHoatDong ? 'selected' : ''}>Không</option>
                                    </select>
                                </div>
                                <div class="form-group full">
                                    <label>Mô tả</label>
                                    <textarea name="mota">${product.moTa}</textarea>
                                </div>
                            </div>

                            <h4 style="margin:18px 0 10px;">Nhóm lựa chọn</h4>
                            <div class="choice-grid">
                                <c:forEach var="group" items="${choiceGroups}">
                                    <div class="choice-card">
                                        <label style="display:flex;align-items:flex-start;gap:10px;margin:0;">
                                            <input type="checkbox" name="nhom_lua_chon" value="${group.id}" ${linkedGroupMap[group.id] ? 'checked' : ''}>
                                            <span>
                                                <strong>${group.tenNhom}</strong>
                                                <p>${empty group.moTa ? 'Không có mô tả' : group.moTa}</p>
                                            </span>
                                        </label>
                                        <label style="display:flex;align-items:center;gap:8px;margin-top:10px;">
                                            <input type="checkbox" name="bat_buoc" value="${group.id}" ${requiredGroupMap[group.id] ? 'checked' : ''}>
                                            <span>Bắt buộc chọn</span>
                                        </label>
                                    </div>
                                </c:forEach>
                            </div>

                            <h4 style="margin:18px 0 10px;">Size</h4>
                            <c:choose>
                                <c:when test="${product.id > 0}">
                                    <p style="color:#64748b;">Quản lý size trực tiếp qua API như luồng PHP.</p>
                                    <div class="size-actions">
                                        <button type="button" class="btn btn-primary" onclick="promptAddSize(${product.id})">Thêm size</button>
                                        <button type="button" class="btn btn-secondary" onclick="reloadSizes(${product.id})">Tải lại size</button>
                                    </div>
                                    <table style="margin-top:12px;">
                                        <thead>
                                        <tr><th>Tên size</th><th>Giá tăng</th><th>Trạng thái</th><th>Thao tác</th></tr>
                                        </thead>
                                        <tbody id="sizeTableBody"><tr><td colspan="4">Đang tải size...</td></tr></tbody>
                                    </table>
                                </c:when>
                                <c:otherwise>
                                    <p style="color:#64748b;">Khi tạo mới, bạn có thể khai báo nhanh các size ban đầu.</p>
                                    <div class="product-form-grid">
                                        <div class="form-group"><label>Tên size 1</label><input name="size_name" placeholder="M"></div>
                                        <div class="form-group"><label>Giá size 1</label><input type="number" step="0.01" name="size_price"></div>
                                        <div class="form-group"><label>Tên size 2</label><input name="size_name" placeholder="L"></div>
                                        <div class="form-group"><label>Giá size 2</label><input type="number" step="0.01" name="size_price"></div>
                                    </div>
                                </c:otherwise>
                            </c:choose>

                            <div class="form-actions" style="margin-top:16px;">
                                <button type="submit" class="btn btn-primary">Lưu</button>
                                <a class="btn btn-secondary" href="${pageContext.request.contextPath}/san-pham">Làm mới</a>
                                <button type="button" class="btn btn-secondary nl-cancel-add-btn" id="btnCloseProductForm">Đóng</button>
                            </div>
                        </form>
                    </div>

                    <form method="get" class="filter-box">
                        <div>
                            <label>Danh mục:</label>
                            <select name="cat" onchange="this.form.submit()">
                                <option value="">Tất cả</option>
                                <c:forEach var="item" items="${categories}">
                                    <option value="${item.id}" ${param.cat == item.id.toString() ? 'selected' : ''}>${item.tenDanhMuc}</option>
                                </c:forEach>
                            </select>
                        </div>
                    </form>

                    <div class="searchbar">
                        <form method="get" style="display:inline;">
                            <input type="hidden" name="cat" value="${param.cat}"/>
                            <input type="text" name="kw" placeholder="Tìm tên sản phẩm..." value="${param.kw}"/>
                        </form>
                    </div>

                    <table>
                        <thead>
                        <tr><th>Mã</th><th>Ảnh</th><th>Tên sản phẩm</th><th>Danh mục</th><th>Giá bán</th><th>Trạng thái</th><th>Tác vụ</th></tr>
                        </thead>
                        <tbody>
                        <c:forEach var="item" items="${products}">
                            <tr>
                                <td>${item.maSanPham}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty item.anhUrl}">
                                            <img class="product-thumb" src="${pageContext.request.contextPath}/${item.anhUrl}" alt="${item.tenSanPham}">
                                        </c:when>
                                        <c:otherwise>-</c:otherwise>
                                    </c:choose>
                                </td>
                                <td>${item.tenSanPham}</td>
                                <td>${item.tenDanhMuc}</td>
                                <td><fmt:formatNumber value="${item.giaBan}" pattern="#,##0" />đ</td>
                                <td class="status">
                                    <span ${item.dangHoatDong ? '' : 'class="off"'}>${item.dangHoatDong ? 'Hoạt động' : 'Ngừng'}</span>
                                </td>
                                <td class="act-link">
                                    <a class="linklike" href="${pageContext.request.contextPath}/san-pham/edit?id=${item.id}">Sửa</a>
                                    <a class="linklike" href="${pageContext.request.contextPath}/san-pham/lien-ket-lua-chon?id=${item.id}">Lựa chọn</a>
                                    <form method="post" action="${pageContext.request.contextPath}/san-pham" style="display:inline;">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="id" value="${item.id}">
                                        <button type="submit" class="linklike" onclick="return confirm('Bạn có chắc chắn muốn xóa sản phẩm này?');">Xóa</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty products}">
                            <tr><td colspan="7">Chưa có sản phẩm nào.</td></tr>
                        </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
<%@ include file="/WEB-INF/views/layout/app-shell-end.jspf" %>
<style>
    .act-link .linklike { background: none; border: none; color: #f97316; font-weight: 600; cursor: pointer; padding: 0 6px; font-size: 14px; text-decoration: none; }
    .act-link .linklike:hover { text-decoration: underline; }
</style>
<script src="${pageContext.request.contextPath}/js/products/collapsible-form.js"></script>
<script>
window.PRODUCT_PAGE = {
    contextPath: '${pageContext.request.contextPath}',
    currentProductId: ${product.id != null && product.id > 0 ? product.id : 0}
};
initCollapsibleForm({
    panelId: 'productAddPanel',
    toggleBtnId: 'btnToggleProductForm',
    closeBtnId: 'btnCloseProductForm',
    labelId: 'toggleProductLabel',
    iconId: 'toggleProductIcon',
    isEdit: ${product.id != null && product.id > 0},
    addLabel: 'Thêm sản phẩm',
    startOpen: ${productFormOpen},
    scrollOnOpen: ${product.id != null && product.id > 0}
});
</script>
<script src="${pageContext.request.contextPath}/js/products/index.js"></script>
</body>
</html>
