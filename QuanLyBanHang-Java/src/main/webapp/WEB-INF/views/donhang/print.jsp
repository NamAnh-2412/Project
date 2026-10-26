<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hóa đơn ${order.maDonHang}</title>
    <style>
        body { font-family: "Segoe UI", sans-serif; color: #111; margin: 0; background: #f8fafc; }
        .bill { max-width: 420px; margin: 24px auto; background: #fff; padding: 24px; }
        h1 { font-size: 22px; text-align: center; margin: 0 0 4px; }
        .muted { color: #64748b; text-align: center; font-size: 13px; }
        .choices { color: #64748b; text-align: left; font-size: 12px; margin-top: 4px; }
        table { width: 100%; border-collapse: collapse; margin-top: 16px; }
        td { padding: 6px 0; border-bottom: 1px dashed #e2e8f0; vertical-align: top; }
        .right { text-align: right; }
        .total { font-size: 18px; font-weight: 700; }
        .qr { text-align: center; margin-top: 16px; }
        .qr img { width: 220px; height: auto; }
        .actions { display: flex; gap: 8px; justify-content: center; margin-top: 16px; }
        button, a.btn { border: 0; background: #f97316; color: #fff; padding: 8px 14px; border-radius: 8px; text-decoration: none; }
        @media print {
            body { background: #fff; }
            .actions { display: none; }
            .bill { margin: 0; max-width: none; }
        }
    </style>
</head>
<body>
<div class="bill">
    <h1>${shopName}</h1>
    <div class="muted">${shopSettings.dia_chi}</div>
    <div class="muted">${shopSettings.so_dien_thoai}</div>
    <div class="muted" style="margin-top:10px;">${order.maDonHang} · ${order.ngayTao}</div>
    <table>
        <c:forEach var="line" items="${order.items}">
            <tr>
                <td>
                    <c:out value="${line.tenSanPham}"/><c:if test="${not empty line.size}"> (<c:out value="${line.size}"/>)</c:if>
                    <c:if test="${not empty line.luaChonList}">
                        <div class="choices">
                            <c:forEach var="ch" items="${line.luaChonList}">
                                <div><c:out value="${ch.tenNhom}"/>: <c:out value="${ch.tenLuaChon}"/></div>
                            </c:forEach>
                        </div>
                    </c:if>
                    <br><span class="muted">x${line.soLuong}</span>
                </td>
                <td class="right"><fmt:formatNumber value="${line.thanhTien}" pattern="#,##0"/>đ</td>
            </tr>
        </c:forEach>
        <tr>
            <td>Tạm tính</td>
            <td class="right"><fmt:formatNumber value="${order.tongTien}" pattern="#,##0"/>đ</td>
        </tr>
        <c:if test="${order.giamGia > 0}">
            <tr>
                <td>Giảm ${order.maGiam}</td>
                <td class="right">-<fmt:formatNumber value="${order.giamGia}" pattern="#,##0"/>đ</td>
            </tr>
        </c:if>
        <tr>
            <td class="total">Thanh toán</td>
            <td class="right total"><fmt:formatNumber value="${order.thanhTien}" pattern="#,##0"/>đ</td>
        </tr>
    </table>
    <div class="muted" style="margin-top:8px;">
        <c:choose>
            <c:when test="${order.phuongThucThanhToan == 'chuyen_khoan'}">Chuyển khoản</c:when>
            <c:when test="${order.phuongThucThanhToan == 'the'}">Thẻ</c:when>
            <c:when test="${order.phuongThucThanhToan == 'app_online'}">App online</c:when>
            <c:otherwise>Tiền mặt</c:otherwise>
        </c:choose>
        · ${empty order.tenKhachHang ? 'Khách lẻ' : order.tenKhachHang}
    </div>
    <c:if test="${order.phuongThucThanhToan == 'chuyen_khoan' && transferQr.ready}">
        <div class="qr">
            <img src="${transferQr.imageUrl}" alt="QR chuyển khoản">
            <div>${transferQr.bankName} · ${transferQr.accountNumber}</div>
            <div>${transferQr.accountName}</div>
            <div>Nội dung: <b>${transferQr.content}</b></div>
        </div>
    </c:if>
    <div class="actions">
        <button type="button" onclick="window.print()">In hóa đơn</button>
        <a class="btn" href="${pageContext.request.contextPath}/don-hang/view?id=${order.id}">Về đơn</a>
    </div>
</div>
</body>
</html>
