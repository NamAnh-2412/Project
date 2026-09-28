<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <%@ include file="/WEB-INF/views/kho/kho-head.jspf" %>
    <style>
        .kiemkho-table input[type="number"] { max-width: 120px; }
        .kiemkho-table input[type="text"] { width: 100%; }
        .kiemkho-table th, .kiemkho-table td { padding: 10px 12px; border-bottom: 1px solid #eef2fa; }
        .kiemkho-table thead tr { background: #eef2ff; }
        .diff-pos { color: #059669; font-weight: 700; }
        .diff-neg { color: #dc2626; font-weight: 700; }
    </style>
</head>
<body>
<%@ include file="/WEB-INF/views/layout/app-shell-start.jspf" %>
            <%@ include file="/WEB-INF/views/layout/page-header.jspf" %>

            <div class="content-card">
                <c:if test="${not empty flashMessage}">
                    <div class="alert ${flashType == 'error' ? 'alert-danger' : 'alert-success'}">${flashMessage}</div>
                </c:if>

                <form method="post" action="${pageContext.request.contextPath}/kho/kiem-kho/create">
                    <div class="form-group">
                        <label>Ghi chu</label>
                        <textarea name="ghi_chu" placeholder="Ghi chu cho phieu kiem kho"></textarea>
                    </div>

                    <table class="kiemkho-table" style="width:100%;border-collapse:collapse;margin-top:14px;">
                        <thead>
                        <tr>
                            <th style="text-align:left;">Ma NL</th>
                            <th style="text-align:left;">Nguyen lieu</th>
                            <th style="text-align:left;">Don vi</th>
                            <th style="text-align:right;">Ton he thong</th>
                            <th style="text-align:right;">Ton thuc te</th>
                            <th style="text-align:right;">Chenh lech</th>
                            <th style="text-align:left;">Ly do</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="m" items="${materials}">
                            <tr data-system="${m.soLuongTon}">
                                <td>${m.maNguyenLieu}</td>
                                <td>
                                    ${m.tenNguyenLieu}
                                    <input type="hidden" name="nguyenlieu_id" value="${m.id}">
                                </td>
                                <td>${m.donViTinh}</td>
                                <td style="text-align:right;"><fmt:formatNumber value="${m.soLuongTon}" pattern="#,##0.##" /></td>
                                <td style="text-align:right;">
                                    <input type="number" step="0.01" min="0" name="ton_thuc_te" value="${m.soLuongTon}" oninput="updateDiff(this)">
                                </td>
                                <td style="text-align:right;" class="chenh-lech">0</td>
                                <td><input type="text" name="ly_do" placeholder="VD: hao hut, do vo..."></td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty materials}">
                            <tr><td colspan="7" style="text-align:center;color:#999;padding:20px;">Không có nguyên liệu nào để kiểm kê.</td></tr>
                        </c:if>
                        </tbody>
                    </table>

                    <div class="form-actions" style="margin-top:16px;">
                        <button type="submit" class="btn btn-primary">Lưu phiếu kiểm kho</button>
                        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/kho?tab=kiemkho">Quay lai</a>
                    </div>
                </form>
            </div>
<%@ include file="/WEB-INF/views/layout/app-shell-end.jspf" %>
<script>
    function updateDiff(input) {
        const row = input.closest('tr');
        const system = Number(row.dataset.system || 0);
        const actual = Number(input.value || 0);
        const diff = actual - system;
        const cell = row.querySelector('.chenh-lech');
        const formatted = new Intl.NumberFormat('vi-VN', { maximumFractionDigits: 2 }).format(diff);
        cell.textContent = diff > 0 ? ('+' + formatted) : formatted;
        cell.className = 'chenh-lech ' + (diff > 0 ? 'diff-pos' : (diff < 0 ? 'diff-neg' : ''));
    }
    document.querySelectorAll('.kiemkho-table input[name="ton_thuc_te"]').forEach(updateDiff);
</script>
</body>
</html>
