<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <%@ include file="/WEB-INF/views/sanpham/products-head.jspf" %>
    <style>
        .product-form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 14px; }
        .act-link .linklike { background: none; border: none; color: #f97316; font-weight: 600; cursor: pointer; padding: 0 6px; font-size: 14px; }
        .act-link .linklike:hover { text-decoration: underline; }
    </style>
</head>
<body>
<%@ include file="/WEB-INF/views/layout/app-shell-start.jspf" %>
            <div class="header-bar-sp">
                <div class="left">
                    <a href="${pageContext.request.contextPath}/home" class="btn-back"><i class="fa fa-arrow-left"></i> Về trang chủ</a>
                    <h1><span class="ic"><i class="fas fa-flask"></i></span> NGUYÊN LIỆU</h1>
                </div>
                <a class="btn-add" href="${pageContext.request.contextPath}/san-pham/nguyen-lieu"><i class="fa fa-plus"></i> Làm mới</a>
            </div>

            <div class="content-wrap">
                <form method="get" action="${pageContext.request.contextPath}/san-pham/nguyen-lieu" class="filter-box">
                    <div>
                        <label>Loại nguyên liệu:</label>
                        <select name="loai" onchange="this.form.submit()">
                            <option value="" ${empty filterLoai ? 'selected' : ''}>Tất cả</option>
                            <c:forEach var="e" items="${danhMucNlLabels}">
                                <option value="${e.key}" ${filterLoai eq e.key ? 'selected' : ''}>${e.value}</option>
                            </c:forEach>
                        </select>
                    </div>
                </form>

                <div class="tbl-wrap">
                    <c:if test="${not empty flashMessage}">
                        <div class="alert ${flashType == 'error' ? 'alert-danger' : 'alert-success'}">${flashMessage}</div>
                    </c:if>
                    <c:if test="${not empty error}">
                        <div class="alert alert-danger">${error}</div>
                    </c:if>

                    <c:set var="materialFormOpen" value="${material.id > 0 or not empty error}"/>

                    <div class="nl-page-toolbar">
                        <button type="button"
                                class="btn btn-primary nl-toggle-add-btn"
                                id="btnToggleMaterialForm"
                                aria-expanded="${materialFormOpen ? 'true' : 'false'}"
                                aria-controls="materialAddPanel">
                            <i class="fas ${materialFormOpen ? 'fa-chevron-up' : 'fa-plus'}" id="toggleMaterialIcon"></i>
                            <span id="toggleMaterialLabel">${materialFormOpen ? (material.id > 0 ? 'Ẩn form sửa' : 'Ẩn form thêm') : 'Thêm nguyên liệu'}</span>
                        </button>
                    </div>

                    <div class="add-form nl-add-panel${materialFormOpen ? ' is-open' : ''}"
                         id="materialAddPanel"
                         style="${materialFormOpen ? '' : 'display:none'}">
                        <h3>${material.id > 0 ? 'Cập nhật nguyên liệu' : 'Thêm nguyên liệu'}</h3>
                        <form method="post">
                            <input type="hidden" name="action" value="save">
                            <input type="hidden" name="id" value="${material.id}">
                            <input type="hidden" name="kw" value="${kw}">
                            <input type="hidden" name="loai" value="${filterLoai}">
                            <div class="product-form-grid">
                                <div class="form-group">
                                    <label>Mã nguyên liệu</label>
                                    <input name="ma_nguyenlieu" value="${material.maNguyenLieu}" placeholder="Bỏ trống khi thêm để tự sinh" autocomplete="off">
                                </div>
                                <div class="form-group">
                                    <label>Tên nguyên liệu *</label>
                                    <input name="ten_nguyenlieu" value="${material.tenNguyenLieu}" required>
                                </div>
                                <div class="form-group">
                                    <label>Danh mục</label>
                                    <select name="danhmuc_nl">
                                        <option value="">-- Chọn nhóm --</option>
                                        <c:forEach var="e" items="${danhMucNlLabels}">
                                            <option value="${e.key}" ${material.danhMucNl eq e.key ? 'selected' : ''}>${e.value}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="form-group">
                                    <label>Đơn vị tính</label>
                                    <input name="don_vi_tinh" value="${material.donViTinh}">
                                </div>
                                <div class="form-group">
                                    <label>Số lượng tồn</label>
                                    <input name="so_luong_ton" type="number" step="0.01" value="${material.soLuongTon}">
                                </div>
                                <div class="form-group">
                                    <label>Số lượng tối thiểu</label>
                                    <input name="so_luong_toi_thieu" type="number" step="0.01" value="${material.soLuongToiThieu}">
                                </div>
                                <div class="form-group">
                                    <label>Giá nhập</label>
                                    <input name="gia_nhap" type="number" step="0.01" value="${material.giaNhap}">
                                </div>
                                <div class="form-group">
                                    <label>Nhà cung cấp</label>
                                    <select name="nha_cung_cap_id">
                                        <option value="">-- Không chọn --</option>
                                        <c:forEach var="s" items="${suppliers}">
                                            <option value="${s.id}"<c:if test="${material.nhaCungCapId ne null and material.nhaCungCapId eq s.id}"> selected</c:if>>${s.tenNhaCungCap}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="form-group">
                                    <label>Trạng thái</label>
                                    <select name="trang_thai">
                                        <c:set var="tt" value="${empty material.trangThai ? 'con_hang' : material.trangThai}"/>
                                        <option value="con_hang" ${tt eq 'con_hang' ? 'selected' : ''}>Còn hàng</option>
                                        <option value="het_hang" ${tt eq 'het_hang' ? 'selected' : ''}>Hết hàng</option>
                                        <option value="ngung_nhap" ${tt eq 'ngung_nhap' ? 'selected' : ''}>Ngừng nhập</option>
                                        <c:if test="${tt ne 'con_hang' and tt ne 'het_hang' and tt ne 'ngung_nhap'}">
                                            <option value="${fn:escapeXml(tt)}" selected>${fn:escapeXml(tt)} (legacy)</option>
                                        </c:if>
                                    </select>
                                </div>
                            </div>
                            <div class="form-actions" style="margin-top:14px;">
                                <button type="submit" class="btn btn-primary">Lưu</button>
                                <a class="btn btn-secondary" href="${pageContext.request.contextPath}/san-pham/nguyen-lieu">Làm mới</a>
                                <button type="button" class="btn btn-secondary nl-cancel-add-btn" id="btnCloseMaterialForm">Đóng</button>
                            </div>
                        </form>
                    </div>

                    <div class="searchbar">
                        <form method="get" style="display:inline;">
                            <input type="hidden" name="loai" value="${filterLoai}"/>
                            <input type="text" name="kw" placeholder="Tìm tên nguyên liệu..." value="${kw}"/>
                        </form>
                    </div>

                    <table>
                        <thead>
                        <tr><th>Mã</th><th>Tên</th><th>Nhóm</th><th>NCC</th><th>Đơn vị</th><th>Tồn kho</th><th>Trạng thái</th><th>Tác vụ</th></tr>
                        </thead>
                        <tbody>
                        <c:forEach var="item" items="${materials}">
                            <tr>
                                <td>${item.maNguyenLieu}</td>
                                <td>${item.tenNguyenLieu}</td>
                                <td><c:choose>
                                    <c:when test="${empty item.danhMucNl}">—</c:when>
                                    <c:when test="${not empty danhMucNlLabels[item.danhMucNl]}">${danhMucNlLabels[item.danhMucNl]}</c:when>
                                    <c:otherwise>${fn:escapeXml(item.danhMucNl)}</c:otherwise>
                                </c:choose></td>
                                <td>${empty item.tenNhaCungCap ? '—' : item.tenNhaCungCap}</td>
                                <td>${item.donViTinh}</td>
                                <td>${item.soLuongTon}</td>
                                <td class="status">
                                    <span ${item.trangThai == 'con_hang' ? '' : 'class="off"'}>${item.trangThai == 'con_hang' ? 'Hoạt động' : 'Ngừng'}</span>
                                </td>
                                <td class="act-link">
                                    <form method="post" action="${pageContext.request.contextPath}/san-pham/nguyen-lieu" style="display:inline;">
                                        <input type="hidden" name="action" value="edit">
                                        <input type="hidden" name="id" value="${item.id}">
                                        <input type="hidden" name="kw" value="${kw}">
                                        <input type="hidden" name="loai" value="${filterLoai}">
                                        <button type="submit" class="linklike">Sửa</button>
                                    </form>
                                    <form method="post" action="${pageContext.request.contextPath}/san-pham/nguyen-lieu" style="display:inline;">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="id" value="${item.id}">
                                        <input type="hidden" name="kw" value="${kw}">
                                        <input type="hidden" name="loai" value="${filterLoai}">
                                        <button type="submit" class="linklike" onclick="return confirm('Bạn có chắc muốn xóa nguyên liệu này?')">Xóa</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty materials}">
                            <tr><td colspan="8">Chưa có nguyên liệu nào.</td></tr>
                        </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
<%@ include file="/WEB-INF/views/layout/app-shell-end.jspf" %>
<script src="${pageContext.request.contextPath}/js/products/collapsible-form.js"></script>
<script>
initCollapsibleForm({
    panelId: 'materialAddPanel',
    toggleBtnId: 'btnToggleMaterialForm',
    closeBtnId: 'btnCloseMaterialForm',
    labelId: 'toggleMaterialLabel',
    iconId: 'toggleMaterialIcon',
    isEdit: ${material.id > 0},
    addLabel: 'Thêm nguyên liệu',
    startOpen: ${materialFormOpen},
    scrollOnOpen: ${material.id > 0}
});
</script>
</body>
</html>
