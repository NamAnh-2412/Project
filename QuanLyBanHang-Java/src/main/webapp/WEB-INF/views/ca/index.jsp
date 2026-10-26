<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <%@ include file="/WEB-INF/views/layout/module-head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/layout/app-shell-start.jspf" %>
            <%@ include file="/WEB-INF/views/layout/page-header.jspf" %>
            <%@ include file="/WEB-INF/views/layout/flash-notice.jspf" %>

            <div class="content-card" style="margin-bottom:20px;">
                <c:choose>
                    <c:when test="${empty openShift}">
                        <h3>Mở ca</h3>
                        <p style="color:#64748b;">Nhập số tiền mặt đang có trong két trước khi bán.</p>
                        <form method="post" action="${pageContext.request.contextPath}/ca-lam" style="display:flex;gap:12px;flex-wrap:wrap;align-items:end;">
                            <input type="hidden" name="action" value="open">
                            <label>Tiền đầu ca
                                <input type="number" name="tien_dau_ca" min="0" step="1000" value="0" required
                                       style="display:block;padding:8px 12px;border:1px solid #d2d8ea;border-radius:8px;margin-top:6px;">
                            </label>
                            <button class="btn btn-primary" type="submit">Mở ca</button>
                        </form>
                    </c:when>
                    <c:otherwise>
                        <h3>Ca đang mở</h3>
                        <p style="color:#64748b;">Mở lúc ${openShift.moLuc}. Tiền đầu ca
                            <fmt:formatNumber value="${openShift.tienDauCa}" pattern="#,##0"/>đ.</p>
                        <div class="stats-row" style="display:grid;grid-template-columns:repeat(auto-fit,minmax(160px,1fr));gap:12px;margin:16px 0;">
                            <div><div>Tiền mặt</div><b><fmt:formatNumber value="${totals.tienMat}" pattern="#,##0"/>đ</b></div>
                            <div><div>Chuyển khoản</div><b><fmt:formatNumber value="${totals.tienChuyenKhoan}" pattern="#,##0"/>đ</b></div>
                            <div><div>Thẻ</div><b><fmt:formatNumber value="${totals.tienThe}" pattern="#,##0"/>đ</b></div>
                            <div><div>App</div><b><fmt:formatNumber value="${totals.tienKhac}" pattern="#,##0"/>đ</b></div>
                            <div><div>Số đơn</div><b>${totals.soDon}</b></div>
                            <div><div>Tiền mặt kỳ vọng trong két</div><b><fmt:formatNumber value="${totals.tienKyVong}" pattern="#,##0"/>đ</b></div>
                        </div>
                        <form method="post" action="${pageContext.request.contextPath}/ca-lam" style="display:flex;gap:12px;flex-wrap:wrap;align-items:end;">
                            <input type="hidden" name="action" value="close">
                            <label>Tiền mặt đếm được
                                <input type="number" name="tien_thuc_te" min="0" step="1000" required
                                       style="display:block;padding:8px 12px;border:1px solid #d2d8ea;border-radius:8px;margin-top:6px;">
                            </label>
                            <label>Ghi chú
                                <input type="text" name="ghi_chu" maxlength="500"
                                       style="display:block;padding:8px 12px;border:1px solid #d2d8ea;border-radius:8px;margin-top:6px;">
                            </label>
                            <button class="btn btn-primary" type="submit">Kết ca</button>
                        </form>
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="content-card">
                <h3>Ca gần đây</h3>
                <div style="overflow-x:auto;">
                    <table>
                        <thead>
                        <tr>
                            <th>Nhân viên</th><th>Mở</th><th>Đóng</th><th>Đầu ca</th><th>Tiền mặt</th>
                            <th>Kỳ vọng</th><th>Đếm được</th><th>Chênh lệch</th><th>Đơn</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="row" items="${history}">
                            <tr>
                                <td>${row.tenNhanVien}</td>
                                <td>${row.moLuc}</td>
                                <td>${empty row.dongLuc ? 'Đang mở' : row.dongLuc}</td>
                                <td><fmt:formatNumber value="${row.tienDauCa}" pattern="#,##0"/></td>
                                <td><fmt:formatNumber value="${row.tienMat}" pattern="#,##0"/></td>
                                <td><fmt:formatNumber value="${row.tienKyVong}" pattern="#,##0"/></td>
                                <td><fmt:formatNumber value="${row.tienThucTe}" pattern="#,##0"/></td>
                                <td><fmt:formatNumber value="${row.chenhLech}" pattern="#,##0"/></td>
                                <td>${row.soDon}</td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
<%@ include file="/WEB-INF/views/layout/app-shell-end.jspf" %>
</body>
</html>
