<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <%@ include file="/WEB-INF/views/sanpham/products-head.jspf" %>
    <style>
        .product-form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 14px; }
        .product-form-grid .full { grid-column: 1 / -1; }
        .group-block { border: 1px solid #e2e8f0; border-radius: 14px; padding: 18px; background: #fff; margin-bottom: 16px; }
        .act-link .linklike { background: none; border: none; color: #f97316; font-weight: 600; cursor: pointer; padding: 0 6px; font-size: 14px; }
        .act-link .linklike:hover { text-decoration: underline; }
        .nl-form-tabs { display: flex; gap: 8px; margin-bottom: 14px; border-bottom: 1px solid #e2e8f0; padding-bottom: 10px; }
        .nl-form-tab {
            border: 1px solid #d2d8ea;
            background: #fff;
            color: #64748b;
            border-radius: 8px;
            padding: 8px 14px;
            font-size: 13px;
            font-weight: 600;
            cursor: pointer;
        }
        .nl-form-tab.is-active { background: #fff7ed; border-color: #fdba74; color: #c2410c; }
        .nl-form-tab-panel { display: none; }
        .nl-form-tab-panel.is-active { display: block; }
    </style>
</head>
<body>
<%@ include file="/WEB-INF/views/layout/app-shell-start.jspf" %>
            <div class="header-bar-sp">
                <div class="left">
                    <a href="${pageContext.request.contextPath}/home" class="btn-back"><i class="fa fa-arrow-left"></i> Về trang chủ</a>
                    <h1><span class="ic"><i class="fas fa-layer-group"></i></span> NHÓM LỰA CHỌN</h1>
                </div>
            </div>

            <div class="content-wrap">
                <div class="tbl-wrap">
                    <c:if test="${not empty flashMessage}">
                        <div class="alert ${flashType == 'error' ? 'alert-danger' : 'alert-success'}">${flashMessage}</div>
                    </c:if>

                    <div class="nl-page-toolbar">
                        <button type="button"
                                class="btn btn-primary nl-toggle-add-btn"
                                id="btnToggleChoiceGroupForm"
                                aria-expanded="false"
                                aria-controls="choiceGroupAddPanel">
                            <i class="fas fa-plus" id="toggleChoiceGroupIcon"></i>
                            <span id="toggleChoiceGroupLabel">Thêm nhóm / lựa chọn</span>
                        </button>
                    </div>

                    <div class="add-form nl-add-panel" id="choiceGroupAddPanel" style="display:none">
                        <div class="nl-form-tabs" role="tablist">
                            <button type="button" class="nl-form-tab is-active" data-tab="group" id="tabBtnGroup">Thêm nhóm</button>
                            <button type="button" class="nl-form-tab" data-tab="choice" id="tabBtnChoice">Thêm lựa chọn</button>
                        </div>

                        <div class="nl-form-tab-panel is-active" id="tabPanelGroup" data-tab-panel="group">
                            <h3 style="margin:0 0 12px;">Thêm / cập nhật nhóm</h3>
                            <form method="post">
                                <input type="hidden" name="action" value="save-group">
                                <div class="product-form-grid">
                                    <div class="form-group"><label>ID nhóm (để sửa)</label><input type="number" name="group_id"></div>
                                    <div class="form-group"><label>Tên nhóm *</label><input name="ten_nhom" required></div>
                                    <div class="form-group"><label>Thứ tự</label><input type="number" name="thu_tu" value="0"></div>
                                    <div class="form-group">
                                        <label>Đang hoạt động</label>
                                        <select name="dang_hoat_dong"><option value="1">Có</option><option value="0">Không</option></select>
                                    </div>
                                    <div class="form-group full"><label>Mô tả</label><textarea name="mota"></textarea></div>
                                </div>
                                <div class="form-actions" style="margin-top:14px;">
                                    <button type="submit" class="btn btn-primary">Lưu nhóm</button>
                                </div>
                            </form>
                        </div>

                        <div class="nl-form-tab-panel" id="tabPanelChoice" data-tab-panel="choice">
                            <h3 style="margin:0 0 12px;">Thêm / cập nhật lựa chọn</h3>
                            <form method="post">
                                <input type="hidden" name="action" value="save-choice">
                                <div class="product-form-grid">
                                    <div class="form-group"><label>ID lựa chọn (để sửa)</label><input type="number" name="choice_id"></div>
                                    <div class="form-group">
                                        <label>Nhóm lựa chọn *</label>
                                        <select name="nhom_lua_chon_id" id="choiceGroupSelect" required>
                                            <option value="">Chọn nhóm</option>
                                            <c:forEach var="group" items="${groups}">
                                                <option value="${group.id}">${group.tenNhom}</option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                    <div class="form-group"><label>Tên lựa chọn *</label><input name="ten_lua_chon" required></div>
                                    <div class="form-group"><label>Giá tăng thêm</label><input type="number" step="0.01" name="gia_tang_them" value="0"></div>
                                    <div class="form-group"><label>Thứ tự</label><input type="number" name="choice_thu_tu" value="0"></div>
                                    <div class="form-group">
                                        <label>Đang hoạt động</label>
                                        <select name="choice_dang_hoat_dong"><option value="1">Có</option><option value="0">Không</option></select>
                                    </div>
                                </div>
                                <div class="form-actions" style="margin-top:14px;">
                                    <button type="submit" class="btn btn-primary">Lưu lựa chọn</button>
                                </div>
                            </form>
                        </div>

                        <div class="form-actions" style="margin-top:14px;padding-top:10px;border-top:1px solid #e2e8f0;">
                            <button type="button" class="btn btn-secondary nl-cancel-add-btn" id="btnCloseChoiceGroupForm">Đóng</button>
                        </div>
                    </div>

                    <h3 style="margin:18px 0 12px;">Danh sách nhóm lựa chọn</h3>
                    <c:forEach var="group" items="${groups}">
                        <div class="group-block">
                            <div style="display:flex;justify-content:space-between;align-items:center;gap:12px;flex-wrap:wrap;">
                                <div>
                                    <h3 style="margin:0;">${group.tenNhom}</h3>
                                    <p style="margin:6px 0 0;color:#64748b;">Mô tả: ${group.moTa} | Thứ tự: ${group.thuTu} | Trạng thái: ${group.dangHoatDong ? 'Hoạt động' : 'Đã ẩn'}</p>
                                </div>
                                <div style="display:flex;gap:8px;flex-wrap:wrap;">
                                    <button type="button"
                                            class="btn btn-secondary btn-add-choice-for-group"
                                            data-group-id="${group.id}">
                                        <i class="fas fa-plus"></i> Thêm lựa chọn
                                    </button>
                                    <form method="post" action="${pageContext.request.contextPath}/san-pham/nhom-lua-chon" style="display:inline;">
                                        <input type="hidden" name="action" value="delete-group">
                                        <input type="hidden" name="group_id" value="${group.id}">
                                        <button type="submit" class="btn btn-danger">Xóa nhóm</button>
                                    </form>
                                </div>
                            </div>
                            <table>
                                <thead>
                                <tr><th>Lựa chọn</th><th>Giá tăng thêm</th><th>Thứ tự</th><th>Trạng thái</th><th>Tác vụ</th></tr>
                                </thead>
                                <tbody>
                                <c:forEach var="choice" items="${group.luaChonList}">
                                    <tr>
                                        <td>${choice.tenLuaChon}</td>
                                        <td>${choice.giaTangThem}</td>
                                        <td>${choice.thuTu}</td>
                                        <td class="status"><span ${choice.dangHoatDong ? '' : 'class="off"'}>${choice.dangHoatDong ? 'Hoạt động' : 'Đã ẩn'}</span></td>
                                        <td class="act-link">
                                            <form method="post" action="${pageContext.request.contextPath}/san-pham/nhom-lua-chon" style="display:inline;">
                                                <input type="hidden" name="action" value="delete-choice">
                                                <input type="hidden" name="choice_id" value="${choice.id}">
                                                <button type="submit" class="linklike" onclick="return confirm('Ẩn lựa chọn này?')">Ẩn</button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty group.luaChonList}">
                                    <tr><td colspan="5">Chưa có lựa chọn.</td></tr>
                                </c:if>
                                </tbody>
                            </table>
                        </div>
                    </c:forEach>
                    <c:if test="${empty groups}">
                        <p>Chưa có nhóm lựa chọn nào.</p>
                    </c:if>
                </div>
            </div>
<%@ include file="/WEB-INF/views/layout/app-shell-end.jspf" %>
<script src="${pageContext.request.contextPath}/js/products/collapsible-form.js"></script>
<script>
(function () {
    var panel = document.getElementById('choiceGroupAddPanel');
    var toggleBtn = document.getElementById('btnToggleChoiceGroupForm');
    var closeBtn = document.getElementById('btnCloseChoiceGroupForm');
    var label = document.getElementById('toggleChoiceGroupLabel');
    var icon = document.getElementById('toggleChoiceGroupIcon');
    var groupSelect = document.getElementById('choiceGroupSelect');
    var tabButtons = document.querySelectorAll('.nl-form-tab');
    var tabPanels = document.querySelectorAll('.nl-form-tab-panel');

    function setPanelOpen(open, scroll) {
        if (!panel || !toggleBtn) return;
        panel.classList.toggle('is-open', open);
        panel.style.display = open ? 'block' : 'none';
        toggleBtn.setAttribute('aria-expanded', open ? 'true' : 'false');
        if (label) label.textContent = open ? 'Ẩn form thêm' : 'Thêm nhóm / lựa chọn';
        if (icon) icon.className = open ? 'fas fa-chevron-up' : 'fas fa-plus';
        if (open && scroll) panel.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }

    function activateTab(tabName) {
        tabButtons.forEach(function (btn) {
            btn.classList.toggle('is-active', btn.getAttribute('data-tab') === tabName);
        });
        tabPanels.forEach(function (p) {
            p.classList.toggle('is-active', p.getAttribute('data-tab-panel') === tabName);
        });
    }

    tabButtons.forEach(function (btn) {
        btn.addEventListener('click', function () {
            activateTab(btn.getAttribute('data-tab'));
        });
    });

    if (toggleBtn) {
        toggleBtn.addEventListener('click', function () {
            var open = !panel.classList.contains('is-open');
            if (open) activateTab('group');
            setPanelOpen(open, true);
        });
    }
    if (closeBtn) {
        closeBtn.addEventListener('click', function () { setPanelOpen(false, false); });
    }

    document.querySelectorAll('.btn-add-choice-for-group').forEach(function (btn) {
        btn.addEventListener('click', function () {
            var groupId = btn.getAttribute('data-group-id');
            activateTab('choice');
            if (groupSelect && groupId) groupSelect.value = groupId;
            setPanelOpen(true, true);
        });
    });
})();
</script>
</body>
</html>
