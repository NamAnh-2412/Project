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

            <div class="content-card">
                <div style="background:#fff;padding:20px;border-radius:8px;margin-bottom:20px;box-shadow:0 2px 8px rgba(0,0,0,0.1);">
                    <h3 style="margin-top:0;color:#22334b;display:flex;align-items:center;gap:10px;">
                        <i class="fas fa-info-circle" style="color:#2196f3;"></i> Thông tin nhà cung cấp
                    </h3>
                    <table style="width:100%;border-collapse:collapse;">
                        <tr><td style="padding:10px;width:150px;font-weight:600;color:#64748b;">Tên:</td><td style="padding:10px;font-weight:600;">${supplier.tenNhaCungCap}</td></tr>
                        <tr><td style="padding:10px;font-weight:600;color:#64748b;">SDT:</td><td style="padding:10px;">${empty supplier.soDienThoai ? '-' : supplier.soDienThoai}</td></tr>
                        <tr><td style="padding:10px;font-weight:600;color:#64748b;">Email:</td><td style="padding:10px;">${empty supplier.email ? '-' : supplier.email}</td></tr>
                        <tr><td style="padding:10px;font-weight:600;color:#64748b;">Địa chỉ:</td><td style="padding:10px;">${empty supplier.diaChi ? '-' : supplier.diaChi}</td></tr>
                        <tr><td style="padding:10px;font-weight:600;color:#64748b;">Mã số thuế:</td><td style="padding:10px;">${empty supplier.maSoThue ? '-' : supplier.maSoThue}</td></tr>
                        <tr><td style="padding:10px;font-weight:600;color:#64748b;">Người liên hệ:</td><td style="padding:10px;">${empty supplier.nguoiLienHe ? '-' : supplier.nguoiLienHe}</td></tr>
                        <tr>
                            <td style="padding:10px;font-weight:600;color:#64748b;">Trạng thái:</td>
                            <td style="padding:10px;">
                                <span style="padding:4px 12px;border-radius:999px;font-size:.88em;font-weight:600;${supplier.trangThai == 'dang_hop_tac' ? 'background:#d4edda;color:#155724;' : 'background:#f8d7da;color:#721c24;'}">
                                    ${supplier.trangThai == 'dang_hop_tac' ? 'Đang hợp tác' : 'Ngừng hợp tác'}
                                </span>
                            </td>
                        </tr>
                        <c:if test="${not empty supplier.ghiChu}">
                            <tr><td style="padding:10px;font-weight:600;color:#64748b;">Ghi chú:</td><td style="padding:10px;">${supplier.ghiChu}</td></tr>
                        </c:if>
                    </table>
                    <div style="margin-top:15px;padding-top:15px;border-top:1px solid #e0e0e0;">
                        <a href="${pageContext.request.contextPath}/nha-cung-cap/edit?id=${supplier.id}" class="btn btn-primary" style="text-decoration:none;display:inline-block;margin-right:8px;">Sửa</a>
                        <a href="${pageContext.request.contextPath}/nha-cung-cap/delete?id=${supplier.id}" class="btn btn-danger" style="text-decoration:none;display:inline-block;" onclick="return confirm('Xóa nhà cung cấp?')">Xóa</a>
                    </div>
                </div>

                <div style="background:#fff;padding:20px;border-radius:8px;margin-bottom:20px;box-shadow:0 2px 8px rgba(0,0,0,0.1);">
                    <h3 style="margin-top:0;color:#22334b;display:flex;align-items:center;gap:10px;">
                        <i class="fas fa-box" style="color:#28a745;"></i> Nguyên vật liệu cung cấp (${stats.total_materials})
                    </h3>
                    <c:choose>
                        <c:when test="${empty materials}">
                            <p style="color:#64748b;padding:20px;text-align:center;">Chưa có nguyên vật liệu nào được gán cho nhà cung cấp này.</p>
                            <p style="color:#64748b;padding:0 20px 20px;text-align:center;font-size:14px;">
                                Bạn có thể gán nhà cung cấp khi <a href="${pageContext.request.contextPath}/san-pham/nguyen-lieu">thêm/sửa nguyên liệu</a>.
                            </p>
                        </c:when>
                        <c:otherwise>
                            <table style="width:100%;border-collapse:collapse;margin-top:15px;">
                                <thead>
                                <tr style="background:#f7fafc;border-bottom:2px solid #e0e0e0;">
                                    <th style="padding:12px;text-align:left;">Tên nguyên liệu</th>
                                    <th style="padding:12px;text-align:left;">Mã</th>
                                    <th style="padding:12px;text-align:left;">Đơn vị</th>
                                    <th style="padding:12px;text-align:right;">Giá nhập</th>
                                    <th style="padding:12px;text-align:center;">Trạng thái</th>
                                    <th style="padding:12px;text-align:center;">Tác vụ</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach var="mat" items="${materials}">
                                    <tr style="border-bottom:1px solid #f0f0f0;">
                                        <td style="padding:12px;font-weight:600;">${mat.ten_nguyenlieu}</td>
                                        <td style="padding:12px;color:#64748b;">${mat.ma_nguyenlieu}</td>
                                        <td style="padding:12px;color:#64748b;">${mat.don_vi_tinh}</td>
                                        <td style="padding:12px;text-align:right;color:#28a745;font-weight:600;">
                                            <c:choose>
                                                <c:when test="${not empty mat.gia_nhap}"><fmt:formatNumber value="${mat.gia_nhap}" pattern="#,###"/>₫</c:when>
                                                <c:otherwise>-</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="padding:12px;text-align:center;">
                                            <span style="padding:4px 12px;border-radius:999px;font-size:.88em;font-weight:600;${mat.trang_thai == 'con_hang' ? 'background:#d4edda;color:#155724;' : (mat.trang_thai == 'het_hang' ? 'background:#fff3cd;color:#856404;' : 'background:#f8d7da;color:#721c24;')}">
                                                ${mat.trang_thai == 'con_hang' ? 'Còn hàng' : (mat.trang_thai == 'het_hang' ? 'Hết hàng' : 'Ngừng nhập')}
                                            </span>
                                        </td>
                                        <td style="padding:12px;text-align:center;">
                                            <form method="post" action="${pageContext.request.contextPath}/san-pham/nguyen-lieu" style="display:inline;margin:0;">
                                                <input type="hidden" name="action" value="edit">
                                                <input type="hidden" name="id" value="${mat.id}">
                                                <button type="submit" style="background:none;border:none;color:#2196f3;cursor:pointer;padding:0;">Sửa</button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div style="background:#fff;padding:20px;border-radius:8px;margin-bottom:20px;box-shadow:0 2px 8px rgba(0,0,0,0.1);">
                    <h3 style="margin-top:0;color:#22334b;display:flex;align-items:center;gap:10px;">
                        <i class="fas fa-history" style="color:#ff9800;"></i> Lịch sử phiếu nhập (${stats.total_imports})
                    </h3>
                    <c:choose>
                        <c:when test="${empty importHistory}">
                            <p style="color:#64748b;padding:20px;text-align:center;">Chưa có phiếu nhập từ NCC này.</p>
                        </c:when>
                        <c:otherwise>
                            <table style="width:100%;border-collapse:collapse;margin-top:15px;">
                                <thead>
                                <tr style="background:#f7fafc;border-bottom:2px solid #e0e0e0;">
                                    <th style="padding:12px;text-align:left;">Mã phiếu</th>
                                    <th style="padding:12px;text-align:left;">Ngày nhập</th>
                                    <th style="padding:12px;text-align:left;">Nhân viên</th>
                                    <th style="padding:12px;text-align:right;">Tổng tiền</th>
                                    <th style="padding:12px;text-align:center;">Trạng thái</th>
                                    <th style="padding:12px;text-align:center;">Tác vụ</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach var="imp" items="${importHistory}">
                                    <tr style="border-bottom:1px solid #f0f0f0;">
                                        <td style="padding:12px;font-weight:600;">${imp.ma_phieu_nhap}</td>
                                        <td style="padding:12px;color:#64748b;"><fmt:formatDate value="${imp.ngay_nhap}" pattern="dd/MM/yyyy"/></td>
                                        <td style="padding:12px;color:#64748b;">${empty imp.ten_nhanvien ? '-' : imp.ten_nhanvien}</td>
                                        <td style="padding:12px;text-align:right;color:#28a745;font-weight:600;"><fmt:formatNumber value="${imp.tong_tien_nhap}" pattern="#,###"/>₫</td>
                                        <td style="padding:12px;text-align:center;">
                                            <span style="padding:4px 12px;border-radius:999px;font-size:.88em;font-weight:600;${imp.trang_thai == 'da_duyet' ? 'background:#d4edda;color:#155724;' : (imp.trang_thai == 'cho_duyet' ? 'background:#fff3cd;color:#856404;' : 'background:#f8d7da;color:#721c24;')}">
                                                ${imp.trang_thai == 'da_duyet' ? 'Đã duyệt' : (imp.trang_thai == 'cho_duyet' ? 'Chờ duyệt' : 'Đã hủy')}
                                            </span>
                                        </td>
                                        <td style="padding:12px;text-align:center;">
                                            <a href="${pageContext.request.contextPath}/kho/view?id=${imp.id}" style="color:#2196f3;">Xem</a>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div style="background:#fff;padding:20px;border-radius:8px;margin-bottom:20px;box-shadow:0 2px 8px rgba(0,0,0,0.1);">
                    <h3 style="margin-top:0;color:#22334b;display:flex;align-items:center;gap:10px;">
                        <i class="fas fa-chart-bar" style="color:#9c27b0;"></i> Thống kê
                    </h3>
                    <div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(200px,1fr));gap:15px;margin-top:15px;">
                        <div style="padding:15px;background:#f7fafc;border-radius:8px;border-left:4px solid #2196f3;">
                            <div style="font-size:14px;color:#64748b;margin-bottom:5px;">Tổng số nguyên liệu</div>
                            <div style="font-size:24px;font-weight:700;color:#2196f3;">${stats.total_materials}</div>
                        </div>
                        <div style="padding:15px;background:#f7fafc;border-radius:8px;border-left:4px solid #28a745;">
                            <div style="font-size:14px;color:#64748b;margin-bottom:5px;">Tổng số phiếu nhập</div>
                            <div style="font-size:24px;font-weight:700;color:#28a745;">${stats.total_imports}</div>
                        </div>
                        <div style="padding:15px;background:#f7fafc;border-radius:8px;border-left:4px solid #ff9800;">
                            <div style="font-size:14px;color:#64748b;margin-bottom:5px;">Tổng giá trị nhập</div>
                            <div style="font-size:24px;font-weight:700;color:#ff9800;"><fmt:formatNumber value="${stats.total_value}" pattern="#,###"/>₫</div>
                        </div>
                    </div>
                </div>

                <div style="margin-top:20px;">
                    <a href="${pageContext.request.contextPath}/nha-cung-cap" class="btn btn-secondary" style="text-decoration:none;display:inline-block;">
                        <i class="fas fa-arrow-left"></i> Về danh sách
                    </a>
                </div>
            </div>
<%@ include file="/WEB-INF/views/layout/app-shell-end.jspf" %>
</body>
</html>
