<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <%@ include file="/WEB-INF/views/kho/kho-head.jspf" %>
    <style>
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

                <div style="display:flex;justify-content:flex-end;gap:10px;flex-wrap:wrap;margin-bottom:16px;">
                    <c:if test="${phieu.trangThai == 'cho_duyet'}">
                        <c:if test="${canApprove}">
                            <form method="post" action="${pageContext.request.contextPath}/kho/kiem-kho/approve" style="display:inline;"
                                  onsubmit="return confirm('Duyet phieu se cap nhat ton kho theo ton thuc te. Tiep tuc?');">
                                <input type="hidden" name="id" value="${phieu.id}">
                                <button type="submit" class="btn btn-primary">Duyet phieu</button>
                            </form>
                        </c:if>
                        <form method="post" action="${pageContext.request.contextPath}/kho/kiem-kho/cancel" style="display:inline;"
                              onsubmit="return confirm('Ban co chac muon huy phieu kiem kho nay?');">
                            <input type="hidden" name="id" value="${phieu.id}">
                            <button type="submit" class="btn btn-danger">Huy phieu</button>
                        </form>
                    </c:if>
                    <a class="btn btn-secondary" href="${pageContext.request.contextPath}/kho?tab=kiemkho">Về danh sách</a>
                </div>

                <div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(200px,1fr));gap:12px;margin-bottom:18px;">
                    <div><strong>Ma phieu:</strong><br>${phieu.maPhieuKiem}</div>
                    <div><strong>Ngay kiem:</strong><br>${phieu.ngayKiem}</div>
                    <div><strong>Nhân viên:</strong><br>${empty phieu.tenNhanVien ? '—' : phieu.tenNhanVien}</div>
                    <div><strong>Trang thai:</strong><br>
                        <c:choose>
                            <c:when test="${phieu.trangThai == 'cho_duyet'}"><span style="display:inline-block;padding:4px 12px;border-radius:999px;font-size:.88em;font-weight:600;background:#fff3cd;color:#856404;">Cho duyet</span></c:when>
                            <c:when test="${phieu.trangThai == 'da_duyet'}"><span style="display:inline-block;padding:4px 12px;border-radius:999px;font-size:.88em;font-weight:600;background:#d1ecf1;color:#0c5460;">Da duyet</span></c:when>
                            <c:otherwise><span style="display:inline-block;padding:4px 12px;border-radius:999px;font-size:.88em;font-weight:600;background:#f8d7da;color:#721c24;">Da huy</span></c:otherwise>
                        </c:choose>
                    </div>
                    <div style="grid-column:1/-1;"><strong>Ghi chu:</strong><br>${empty phieu.ghiChu ? '—' : phieu.ghiChu}</div>
                </div>

                <table style="width:100%;border-collapse:collapse;">
                    <thead>
                    <tr style="background:#eef2ff;">
                        <th style="padding:12px;text-align:left;">Ma NL</th>
                        <th style="padding:12px;text-align:left;">Nguyen lieu</th>
                        <th style="padding:12px;text-align:left;">Don vi</th>
                        <th style="padding:12px;text-align:right;">Ton he thong</th>
                        <th style="padding:12px;text-align:right;">Ton thuc te</th>
                        <th style="padding:12px;text-align:right;">Chenh lech</th>
                        <th style="padding:12px;text-align:left;">Ly do</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="item" items="${phieu.items}">
                        <tr style="border-bottom:1px solid #eef2fa;">
                            <td style="padding:12px;">${item.maNguyenLieu}</td>
                            <td style="padding:12px;">${item.tenNguyenLieu}</td>
                            <td style="padding:12px;">${item.donViTinh}</td>
                            <td style="padding:12px;text-align:right;"><fmt:formatNumber value="${item.tonHeThong}" pattern="#,##0.##" /></td>
                            <td style="padding:12px;text-align:right;"><fmt:formatNumber value="${item.tonThucTe}" pattern="#,##0.##" /></td>
                            <td style="padding:12px;text-align:right;" class="${item.chenhLech gt 0 ? 'diff-pos' : (item.chenhLech lt 0 ? 'diff-neg' : '')}">
                                <c:if test="${item.chenhLech gt 0}">+</c:if><fmt:formatNumber value="${item.chenhLech}" pattern="#,##0.##" />
                            </td>
                            <td style="padding:12px;">${empty item.lyDo ? '—' : item.lyDo}</td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty phieu.items}">
                        <tr><td colspan="7" style="padding:20px;text-align:center;color:#999;">Phieu khong co dong chi tiet.</td></tr>
                    </c:if>
                    </tbody>
                </table>
            </div>
<%@ include file="/WEB-INF/views/layout/app-shell-end.jspf" %>
</body>
</html>
