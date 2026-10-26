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
                <h3>Tạo mã</h3>
                <form method="post" action="${pageContext.request.contextPath}/ma-giam-gia" style="display:grid;grid-template-columns:repeat(auto-fit,minmax(180px,1fr));gap:12px;">
                    <label>Mã<input name="ma" required maxlength="40" style="display:block;width:100%;padding:8px;border:1px solid #d2d8ea;border-radius:8px;"></label>
                    <label>Loại
                        <select name="loai" style="display:block;width:100%;padding:8px;border:1px solid #d2d8ea;border-radius:8px;">
                            <option value="tien">Số tiền</option>
                            <option value="phan_tram">Phần trăm</option>
                        </select>
                    </label>
                    <label>Giá trị<input type="number" name="gia_tri" min="1" step="1" required style="display:block;width:100%;padding:8px;border:1px solid #d2d8ea;border-radius:8px;"></label>
                    <label>Đơn tối thiểu<input type="number" name="don_toi_thieu" min="0" step="1000" value="0" style="display:block;width:100%;padding:8px;border:1px solid #d2d8ea;border-radius:8px;"></label>
                    <label>Giảm tối đa<input type="number" name="giam_toi_da" min="0" step="1000" style="display:block;width:100%;padding:8px;border:1px solid #d2d8ea;border-radius:8px;"></label>
                    <label>Số lần tối đa<input type="number" name="so_lan_toi_da" min="1" step="1" style="display:block;width:100%;padding:8px;border:1px solid #d2d8ea;border-radius:8px;"></label>
                    <label>Từ ngày<input type="date" name="bat_dau" style="display:block;width:100%;padding:8px;border:1px solid #d2d8ea;border-radius:8px;"></label>
                    <label>Đến ngày<input type="date" name="ket_thuc" style="display:block;width:100%;padding:8px;border:1px solid #d2d8ea;border-radius:8px;"></label>
                    <div style="align-self:end;"><button class="btn btn-primary" type="submit">Lưu mã</button></div>
                </form>
            </div>

            <div class="content-card">
                <div style="overflow-x:auto;">
                    <table>
                        <thead>
                        <tr>
                            <th>Mã</th><th>Loại</th><th>Giá trị</th><th>Đơn tối thiểu</th><th>Đã dùng</th><th>Hạn</th><th>Trạng thái</th><th></th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="row" items="${vouchers}">
                            <tr>
                                <td>${row.ma}</td>
                                <td>${row.loai == 'phan_tram' ? 'Phần trăm' : 'Số tiền'}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${row.loai == 'phan_tram'}">${row.giaTri}%</c:when>
                                        <c:otherwise><fmt:formatNumber value="${row.giaTri}" pattern="#,##0"/>đ</c:otherwise>
                                    </c:choose>
                                </td>
                                <td><fmt:formatNumber value="${row.donToiThieu}" pattern="#,##0"/>đ</td>
                                <td>${row.soLanDaDung}<c:if test="${not empty row.soLanToiDa}"> / ${row.soLanToiDa}</c:if></td>
                                <td>${empty row.batDau ? '...' : row.batDau} - ${empty row.ketThuc ? '...' : row.ketThuc}</td>
                                <td>${row.trangThai == 'hoat_dong' ? 'Đang dùng' : 'Ngừng'}</td>
                                <td>
                                    <form method="post" action="${pageContext.request.contextPath}/ma-giam-gia">
                                        <input type="hidden" name="action" value="toggle">
                                        <input type="hidden" name="id" value="${row.id}">
                                        <input type="hidden" name="trang_thai" value="${row.trangThai}">
                                        <button class="btn btn-secondary" type="submit">${row.trangThai == 'hoat_dong' ? 'Ngừng' : 'Bật lại'}</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
<%@ include file="/WEB-INF/views/layout/app-shell-end.jspf" %>
</body>
</html>
