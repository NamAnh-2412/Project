<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <%@ include file="/WEB-INF/views/kho/kho-head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/layout/app-shell-start.jspf" %>
            <%@ include file="/WEB-INF/views/layout/page-header.jspf" %>

            <div class="content-card">
                <c:if test="${not empty flashMessage}">
                    <div class="alert ${flashType == 'error' ? 'alert-danger' : 'alert-success'}">${flashMessage}</div>
                </c:if>

                <div style="display:flex;justify-content:flex-end;gap:10px;flex-wrap:wrap;margin-bottom:16px;">
                    <c:if test="${receipt.trangThai == 'cho_duyet' && currentRole == 'cua_hang_truong'}">
                        <form method="post" action="${pageContext.request.contextPath}/kho/approve" style="display:inline;" onsubmit="return confirm('Duyet phieu nhap kho nay?')">
                            <input type="hidden" name="id" value="${receipt.id}">
                            <button type="submit" class="btn btn-primary">Duyet phieu</button>
                        </form>
                        <form method="post" action="${pageContext.request.contextPath}/kho/cancel" style="display:inline;" onsubmit="return confirm('Huy phieu nhap kho nay?')">
                            <input type="hidden" name="id" value="${receipt.id}">
                            <button type="submit" class="btn btn-danger">Huy phieu</button>
                        </form>
                    </c:if>
                    <a class="btn btn-secondary" href="${pageContext.request.contextPath}/kho">Quay lai</a>
                </div>

                <div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(200px,1fr));gap:12px;margin-bottom:18px;">
                    <div><strong>Ma phieu:</strong><br>${receipt.maPhieuNhap}</div>
                    <div><strong>Nhà cung cấp:</strong><br>${empty receipt.tenNhaCungCap ? 'Không có' : receipt.tenNhaCungCap}</div>
                    <div><strong>Nhân viên:</strong><br>${receipt.tenNhanVienNhap}</div>
                    <div><strong>Ngay nhap:</strong><br>${receipt.ngayNhap}</div>
                    <div><strong>Tong tien:</strong><br><fmt:formatNumber value="${receipt.tongTien}" pattern="#,##0" />d</div>
                    <div><strong>Trang thai:</strong><br>${receipt.trangThai}</div>
                    <div style="grid-column:1/-1;"><strong>Ghi chu:</strong><br>${empty receipt.ghiChu ? '—' : receipt.ghiChu}</div>
                </div>

                <table style="width:100%;border-collapse:collapse;">
                    <thead>
                    <tr style="background:#eef2ff;">
                        <th style="padding:12px;text-align:left;">Nguyen lieu</th>
                        <th style="padding:12px;text-align:right;">So luong</th>
                        <th style="padding:12px;text-align:left;">Don vi</th>
                        <th style="padding:12px;text-align:right;">Don gia</th>
                        <th style="padding:12px;text-align:right;">Thanh tien</th>
                        <th style="padding:12px;text-align:left;">Han su dung</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="item" items="${receipt.items}">
                        <tr style="border-bottom:1px solid #eef2fa;">
                            <td style="padding:12px;">${item.tenNguyenVatLieu}</td>
                            <td style="padding:12px;text-align:right;">${item.soLuongNhap}</td>
                            <td style="padding:12px;">${item.donViTinh}</td>
                            <td style="padding:12px;text-align:right;"><fmt:formatNumber value="${item.donGia}" pattern="#,##0" />d</td>
                            <td style="padding:12px;text-align:right;"><fmt:formatNumber value="${item.thanhTien}" pattern="#,##0" />d</td>
                            <td style="padding:12px;">${item.hanSuDung}</td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty receipt.items}">
                        <tr><td colspan="6" style="padding:20px;text-align:center;color:#999;">Không có dòng chi tiết.</td></tr>
                    </c:if>
                    </tbody>
                </table>
            </div>
<%@ include file="/WEB-INF/views/layout/app-shell-end.jspf" %>
</body>
</html>
