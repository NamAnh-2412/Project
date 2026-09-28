<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <%@ include file="/WEB-INF/views/kho/kho-head.jspf" %>
    <c:set var="cp" value="${pageContext.request.contextPath}"/>
    <c:set var="tabv" value="${empty tab ? 'phieu' : tab}"/>
</head>
<body>
<%@ include file="/WEB-INF/views/layout/app-shell-start.jspf" %>
            <%@ include file="/WEB-INF/views/layout/page-header.jspf" %>

            <div class="content-card">
                <c:if test="${not empty flashMessage}">
                    <div class="alert ${flashType == 'error' ? 'alert-danger' : 'alert-success'}">${flashMessage}</div>
                </c:if>

                <div style="display:flex;gap:8px;margin-bottom:24px;border-bottom:2px solid #eef2fa;">
                    <a href="${cp}/kho?tab=phieu" style="padding:12px 24px;text-decoration:none;color:#22334b;font-weight:600;border-bottom:3px solid ${tabv == 'phieu' ? '#2196f3' : 'transparent'};margin-bottom:-2px;">
                        <i class="fas fa-file-invoice"></i> Phiếu nhập kho
                    </a>
                    <a href="${cp}/kho?tab=tonkho" style="padding:12px 24px;text-decoration:none;color:#22334b;font-weight:600;border-bottom:3px solid ${tabv == 'tonkho' ? '#2196f3' : 'transparent'};margin-bottom:-2px;">
                        <i class="fas fa-boxes"></i> Tồn kho
                    </a>
                    <a href="${cp}/kho?tab=kiemkho" style="padding:12px 24px;text-decoration:none;color:#22334b;font-weight:600;border-bottom:3px solid ${tabv == 'kiemkho' ? '#2196f3' : 'transparent'};margin-bottom:-2px;">
                        <i class="fas fa-clipboard-check"></i> Phiếu kiểm kho
                    </a>
                    <a href="${cp}/kho?tab=expiry" style="padding:12px 24px;text-decoration:none;color:#22334b;font-weight:600;border-bottom:3px solid ${tabv == 'expiry' ? '#2196f3' : 'transparent'};margin-bottom:-2px;">
                        <i class="fas fa-calendar-times"></i> Hạn sử dụng
                    </a>
                </div>

                <c:choose>
                    <%-- ===================== TAB PHIEU NHAP ===================== --%>
                    <c:when test="${tabv == 'phieu'}">
                        <div style="margin-bottom:20px;">
                            <a href="${cp}/kho/create" class="main-link" style="display:inline-block;"><i class="fas fa-plus"></i> Tạo phiếu nhập kho mới</a>
                        </div>

                        <div style="margin-bottom:20px;padding:16px;background:#f8fafc;border-radius:8px;">
                            <form method="get" style="display:grid;grid-template-columns:1fr 1fr auto;gap:12px;align-items:center;">
                                <input type="hidden" name="tab" value="phieu">
                                <select name="status" style="padding:9px 12px;border:1px solid #d2d8ea;border-radius:8px;background:#fff;">
                                    <option value="">Tất cả trạng thái</option>
                                    <option value="cho_duyet" ${status == 'cho_duyet' ? 'selected' : ''}>Chờ duyệt</option>
                                    <option value="da_duyet" ${status == 'da_duyet' ? 'selected' : ''}>Đã duyệt</option>
                                    <option value="da_huy" ${status == 'da_huy' ? 'selected' : ''}>Đã hủy</option>
                                </select>
                                <input type="date" name="from" value="${from}" style="padding:9px 12px;border:1px solid #d2d8ea;border-radius:8px;background:#fff;">
                                <input type="date" name="to" value="${to}" style="padding:9px 12px;border:1px solid #d2d8ea;border-radius:8px;background:#fff;">
                                <button type="submit" class="btn btn-primary" style="padding:9px 20px;white-space:nowrap;">Lọc</button>
                            </form>
                        </div>

                        <div style="overflow-x:auto;">
                            <table style="width:100%;border-collapse:collapse;">
                                <thead>
                                <tr style="background:#eef2ff;">
                                    <th style="padding:12px;text-align:left;">Mã phiếu</th>
                                    <th style="padding:12px;text-align:left;">Ngày nhập</th>
                                    <th style="padding:12px;text-align:left;">Nhà cung cấp</th>
                                    <th style="padding:12px;text-align:left;">Nhân viên</th>
                                    <th style="padding:12px;text-align:right;">Tổng tiền</th>
                                    <th style="padding:12px;text-align:left;">Trạng thái</th>
                                    <th style="padding:12px;text-align:left;">Tác vụ</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach var="row" items="${receipts}">
                                    <tr style="border-bottom:1px solid #eef2fa;">
                                        <td style="padding:12px;">${row.maPhieuNhap}</td>
                                        <td style="padding:12px;">${row.ngayNhap}</td>
                                        <td style="padding:12px;">${empty row.tenNhaCungCap ? 'Không có' : row.tenNhaCungCap}</td>
                                        <td style="padding:12px;">${empty row.tenNhanVienNhap ? 'N/A' : row.tenNhanVienNhap}</td>
                                        <td style="padding:12px;text-align:right;font-weight:600;"><fmt:formatNumber value="${row.tongTien}" pattern="#,##0" />d</td>
                                        <td style="padding:12px;">
                                            <c:choose>
                                                <c:when test="${row.trangThai == 'cho_duyet'}"><span style="display:inline-block;padding:4px 12px;border-radius:999px;font-size:.88em;font-weight:600;background:#fff3cd;color:#856404;">Chờ duyệt</span></c:when>
                                                <c:when test="${row.trangThai == 'da_duyet'}"><span style="display:inline-block;padding:4px 12px;border-radius:999px;font-size:.88em;font-weight:600;background:#d1ecf1;color:#0c5460;">Đã duyệt</span></c:when>
                                                <c:otherwise><span style="display:inline-block;padding:4px 12px;border-radius:999px;font-size:.88em;font-weight:600;background:#f8d7da;color:#721c24;">Đã hủy</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="padding:12px;">
                                            <a href="${cp}/kho/view?id=${row.id}" style="padding:6px 12px;background:#2196f3;color:#fff;text-decoration:none;border-radius:4px;font-size:13px;display:inline-block;margin-right:4px;"><i class="fas fa-eye"></i> Xem</a>
                                            <c:if test="${row.trangThai == 'cho_duyet'}">
                                                <form method="post" action="${cp}/kho/approve" style="display:inline;" onsubmit="return confirm('Duyệt phiếu nhập kho này?')">
                                                    <input type="hidden" name="id" value="${row.id}">
                                                    <button type="submit" style="padding:6px 12px;background:#28a745;color:#fff;border:none;border-radius:4px;font-size:13px;cursor:pointer;"><i class="fas fa-check"></i> Duyệt</button>
                                                </form>
                                                <form method="post" action="${cp}/kho/cancel" style="display:inline;" onsubmit="return confirm('Hủy phiếu nhập kho này?')">
                                                    <input type="hidden" name="id" value="${row.id}">
                                                    <button type="submit" style="padding:6px 12px;background:#dc3545;color:#fff;border:none;border-radius:4px;font-size:13px;cursor:pointer;"><i class="fas fa-times"></i> Hủy</button>
                                                </form>
                                            </c:if>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty receipts}">
                                    <tr><td colspan="7" style="padding:20px;text-align:center;color:#999;">Chưa có phieu nhap kho nao</td></tr>
                                </c:if>
                                </tbody>
                            </table>
                        </div>
                    </c:when>

                    <%-- ===================== TAB TON KHO ===================== --%>
                    <c:when test="${tabv == 'tonkho'}">
                        <div style="margin-bottom:20px;padding:16px;background:#f8fafc;border-radius:8px;">
                            <form method="get" style="display:grid;grid-template-columns:1fr 1fr auto;gap:12px;align-items:center;">
                                <input type="hidden" name="tab" value="tonkho">
                                <input type="text" name="kw" value="${kw}" placeholder="Tìm theo tên nguyên liệu..." style="padding:9px 12px;border:1px solid #d2d8ea;border-radius:8px;background:#fff;width:100%;">
                                <select name="loai" style="padding:9px 12px;border:1px solid #d2d8ea;border-radius:8px;background:#fff;">
                                    <option value="">Tất cả danh mục</option>
                                    <c:forEach var="dm" items="${danhMucMap}">
                                        <option value="${dm.key}" ${loai == dm.key ? 'selected' : ''}>${dm.value}</option>
                                    </c:forEach>
                                </select>
                                <button type="submit" class="btn btn-primary" style="padding:9px 20px;white-space:nowrap;"><i class="fas fa-search"></i> Tìm kiếm</button>
                            </form>
                        </div>

                        <div style="overflow-x:auto;">
                            <table style="width:100%;border-collapse:collapse;background:#fff;">
                                <thead>
                                <tr style="background:#eef2ff;">
                                    <th style="padding:12px;text-align:left;">Mã nguyên liệu</th>
                                    <th style="padding:12px;text-align:left;">Tên nguyên liệu</th>
                                    <th style="padding:12px;text-align:left;">Danh mục</th>
                                    <th style="padding:12px;text-align:right;">Số lượng tồn</th>
                                    <th style="padding:12px;text-align:left;">Đơn vị tính</th>
                                    <th style="padding:12px;text-align:center;">Trạng thái</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach var="m" items="${tonKhoList}">
                                    <tr style="border-bottom:1px solid #eef2fa;">
                                        <td style="padding:12px;">${m.maNguyenLieu}</td>
                                        <td style="padding:12px;font-weight:600;color:#1e293b;">${m.tenNguyenLieu}</td>
                                        <td style="padding:12px;">${empty danhMucMap[m.danhMucNl] ? m.danhMucNl : danhMucMap[m.danhMucNl]}</td>
                                        <td style="padding:12px;text-align:right;font-size:16px;font-weight:bold;color:${m.soLuongTon lt 5 ? '#dc3545' : (m.soLuongTon lt 10 ? '#ff9800' : '#28a745')};">
                                            <fmt:formatNumber value="${m.soLuongTon}" pattern="#,##0.##" />
                                        </td>
                                        <td style="padding:12px;color:#64748b;">${m.donViTinh}</td>
                                        <td style="padding:12px;text-align:center;">
                                            <c:choose>
                                                <c:when test="${m.soLuongTon lt 5}"><span style="display:inline-block;padding:4px 12px;border-radius:999px;font-size:12px;font-weight:600;background:#f8d7da;color:#721c24;">Sắp hết (&lt;5)</span></c:when>
                                                <c:when test="${m.soLuongTon lt 10}"><span style="display:inline-block;padding:4px 12px;border-radius:999px;font-size:12px;font-weight:600;background:#fff3cd;color:#856404;">Sắp hết (&lt;10)</span></c:when>
                                                <c:otherwise><span style="display:inline-block;padding:4px 12px;border-radius:999px;font-size:12px;font-weight:600;background:#d4edda;color:#155724;">Còn hàng</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty tonKhoList}">
                                    <tr><td colspan="6" style="padding:32px;text-align:center;color:#64748b;">
                                        <i class="fas fa-box-open" style="font-size:32px;color:#cbd5e1;display:block;margin-bottom:12px;"></i>
                                        Không tìm thấy nguyên vật liệu nào
                                    </td></tr>
                                </c:if>
                                </tbody>
                            </table>
                        </div>
                    </c:when>

                    <%-- ===================== TAB KIEM KHO ===================== --%>
                    <c:when test="${tabv == 'kiemkho'}">
                        <div style="margin-bottom:20px;">
                            <a href="${cp}/kho/kiem-kho/create" class="main-link" style="display:inline-block;"><i class="fas fa-plus"></i> Tạo phiếu kiểm kho mới</a>
                        </div>

                        <div style="display:grid;grid-template-columns:repeat(auto-fit, minmax(200px, 1fr));gap:16px;margin-bottom:24px;">
                            <div style="background:#fff3cd;padding:16px;border-radius:8px;border-left:4px solid #ffc107;">
                                <div style="font-size:14px;color:#856404;margin-bottom:4px;">Chờ duyệt</div>
                                <div style="font-size:24px;font-weight:bold;color:#856404;">${kiemKhoStats.cho_duyet}</div>
                            </div>
                            <div style="background:#d1ecf1;padding:16px;border-radius:8px;border-left:4px solid #17a2b8;">
                                <div style="font-size:14px;color:#0c5460;margin-bottom:4px;">Đã duyệt</div>
                                <div style="font-size:24px;font-weight:bold;color:#0c5460;">${kiemKhoStats.da_duyet}</div>
                            </div>
                            <div style="background:#f8d7da;padding:16px;border-radius:8px;border-left:4px solid #dc3545;">
                                <div style="font-size:14px;color:#721c24;margin-bottom:4px;">Đã hủy</div>
                                <div style="font-size:24px;font-weight:bold;color:#721c24;">${kiemKhoStats.da_huy}</div>
                            </div>
                        </div>

                        <div style="margin-bottom:20px;padding:16px;background:#f8fafc;border-radius:8px;">
                            <form method="get" style="display:grid;grid-template-columns:1fr 1fr 1fr auto;gap:12px;align-items:center;">
                                <input type="hidden" name="tab" value="kiemkho">
                                <select name="status" style="padding:9px 12px;border:1px solid #d2d8ea;border-radius:8px;background:#fff;">
                                    <option value="">Tất cả trạng thái</option>
                                    <option value="cho_duyet" ${status == 'cho_duyet' ? 'selected' : ''}>Chờ duyệt</option>
                                    <option value="da_duyet" ${status == 'da_duyet' ? 'selected' : ''}>Đã duyệt</option>
                                    <option value="da_huy" ${status == 'da_huy' ? 'selected' : ''}>Đã hủy</option>
                                </select>
                                <input type="date" name="from" value="${from}" style="padding:9px 12px;border:1px solid #d2d8ea;border-radius:8px;background:#fff;">
                                <input type="date" name="to" value="${to}" style="padding:9px 12px;border:1px solid #d2d8ea;border-radius:8px;background:#fff;">
                                <button type="submit" class="btn btn-primary" style="padding:9px 20px;white-space:nowrap;">Lọc</button>
                            </form>
                        </div>

                        <div style="overflow-x:auto;">
                            <table style="width:100%;border-collapse:collapse;">
                                <thead>
                                <tr style="background:#eef2ff;">
                                    <th style="padding:12px;text-align:left;">Mã phiếu</th>
                                    <th style="padding:12px;text-align:left;">Ngày kiểm</th>
                                    <th style="padding:12px;text-align:left;">Nhân viên</th>
                                    <th style="padding:12px;text-align:left;">Trạng thái</th>
                                    <th style="padding:12px;text-align:left;">Tác vụ</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach var="kk" items="${kiemKhoList}">
                                    <tr style="border-bottom:1px solid #eef2fa;">
                                        <td style="padding:12px;font-weight:bold;">${kk.maPhieuKiem}</td>
                                        <td style="padding:12px;">${kk.ngayKiem}</td>
                                        <td style="padding:12px;">${empty kk.tenNhanVien ? 'N/A' : kk.tenNhanVien}</td>
                                        <td style="padding:12px;">
                                            <c:choose>
                                                <c:when test="${kk.trangThai == 'cho_duyet'}"><span style="display:inline-block;padding:4px 12px;border-radius:999px;font-size:.88em;font-weight:600;background:#fff3cd;color:#856404;">Chờ duyệt</span></c:when>
                                                <c:when test="${kk.trangThai == 'da_duyet'}"><span style="display:inline-block;padding:4px 12px;border-radius:999px;font-size:.88em;font-weight:600;background:#d1ecf1;color:#0c5460;">Đã duyệt</span></c:when>
                                                <c:otherwise><span style="display:inline-block;padding:4px 12px;border-radius:999px;font-size:.88em;font-weight:600;background:#f8d7da;color:#721c24;">Đã hủy</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="padding:12px;">
                                            <a href="${cp}/kho/kiem-kho/view?id=${kk.id}" style="padding:6px 12px;background:#2196f3;color:#fff;text-decoration:none;border-radius:4px;font-size:13px;display:inline-block;"><i class="fas fa-eye"></i> Xem</a>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty kiemKhoList}">
                                    <tr><td colspan="5" style="padding:20px;text-align:center;color:#999;">Chưa có phieu kiem kho nao</td></tr>
                                </c:if>
                                </tbody>
                            </table>
                        </div>
                    </c:when>

                    <%-- ===================== TAB HAN SU DUNG ===================== --%>
                    <c:otherwise>
                        <c:choose>
                            <c:when test="${not hasKhoLotTable}">
                                <div style="padding:24px;text-align:center;background:#fff3cd;border-radius:8px;color:#856404;">
                                    <i class="fas fa-exclamation-triangle" style="font-size:48px;margin-bottom:16px;"></i>
                                    <h3 style="margin:0 0 8px 0;">Chưa có du lieu han su dung</h3>
                                    <p style="margin:0;">Bang <code>kho_lot</code> chua ton tai.</p>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div style="display:grid;grid-template-columns:repeat(auto-fit, minmax(200px, 1fr));gap:16px;margin-bottom:24px;">
                                    <div style="background:#fff3cd;padding:16px;border-radius:8px;border-left:4px solid #ffc107;">
                                        <div style="font-size:14px;color:#856404;margin-bottom:4px;">Sắp hết han (≤7 ngay)</div>
                                        <div style="font-size:24px;font-weight:bold;color:#856404;">${expiryStats.sap_het_han}</div>
                                    </div>
                                    <div style="background:#f8d7da;padding:16px;border-radius:8px;border-left:4px solid #dc3545;">
                                        <div style="font-size:14px;color:#721c24;margin-bottom:4px;">Da het han</div>
                                        <div style="font-size:24px;font-weight:bold;color:#721c24;">${expiryStats.het_han}</div>
                                    </div>
                                    <div style="background:#d1ecf1;padding:16px;border-radius:8px;border-left:4px solid #17a2b8;">
                                        <div style="font-size:14px;color:#0c5460;margin-bottom:4px;">Đã hủy</div>
                                        <div style="font-size:24px;font-weight:bold;color:#0c5460;">${expiryStats.da_huy}</div>
                                    </div>
                                </div>

                                <div style="margin-bottom:20px;padding:16px;background:#f8fafc;border-radius:8px;display:flex;justify-content:space-between;gap:12px;flex-wrap:wrap;align-items:center;">
                                    <form method="get" style="display:grid;grid-template-columns:1fr 1fr 1fr auto;gap:12px;align-items:center;flex:1;">
                                        <input type="hidden" name="tab" value="expiry">
                                        <select name="expiry_filter" style="padding:9px 12px;border:1px solid #d2d8ea;border-radius:8px;background:#fff;">
                                            <option value="all" ${expiryFilter == 'all' ? 'selected' : ''}>Tất cả</option>
                                            <option value="sap_het_han" ${expiryFilter == 'sap_het_han' ? 'selected' : ''}>Sắp hết han</option>
                                            <option value="het_han" ${expiryFilter == 'het_han' ? 'selected' : ''}>Hết hạn</option>
                                            <option value="da_huy" ${expiryFilter == 'da_huy' ? 'selected' : ''}>Đã hủy</option>
                                        </select>
                                        <input type="date" name="from" value="${from}" style="padding:9px 12px;border:1px solid #d2d8ea;border-radius:8px;background:#fff;">
                                        <input type="date" name="to" value="${to}" style="padding:9px 12px;border:1px solid #d2d8ea;border-radius:8px;background:#fff;">
                                        <button type="submit" class="btn btn-primary" style="padding:9px 20px;white-space:nowrap;">Lọc</button>
                                    </form>
                                    <button type="button" class="btn btn-secondary" onclick="checkExpiryAlerts()" style="padding:9px 16px;">Tạo cảnh báo hạn dùng</button>
                                </div>

                                <div style="overflow-x:auto;">
                                    <table style="width:100%;border-collapse:collapse;">
                                        <thead>
                                        <tr style="background:#eef2ff;">
                                            <th style="padding:12px;text-align:left;">Ma NL</th>
                                            <th style="padding:12px;text-align:left;">Tên nguyên liệu</th>
                                            <th style="padding:12px;text-align:left;">Mã phiếu nhap</th>
                                            <th style="padding:12px;text-align:right;">SL nhap</th>
                                            <th style="padding:12px;text-align:right;">Con lai</th>
                                            <th style="padding:12px;text-align:left;">Don vi</th>
                                            <th style="padding:12px;text-align:left;">Hạn sử dụng</th>
                                            <th style="padding:12px;text-align:center;">So ngay con lai</th>
                                            <th style="padding:12px;text-align:left;">Trạng thái</th>
                                            <th style="padding:12px;text-align:left;">Tác vụ</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <c:forEach var="lot" items="${expiryLots}">
                                            <tr style="border-bottom:1px solid #eef2fa;">
                                                <td style="padding:12px;">${lot.ma_nguyenlieu}</td>
                                                <td style="padding:12px;font-weight:600;">${lot.ten_nguyenlieu}</td>
                                                <td style="padding:12px;">${lot.ma_phieu_nhap}</td>
                                                <td style="padding:12px;text-align:right;"><fmt:formatNumber value="${lot.so_luong_nhap}" pattern="#,##0.##" /></td>
                                                <td style="padding:12px;text-align:right;font-weight:600;"><fmt:formatNumber value="${lot.so_luong_con_lai}" pattern="#,##0.##" /></td>
                                                <td style="padding:12px;">${lot.don_vi_tinh}</td>
                                                <td style="padding:12px;">${lot.han_su_dung}</td>
                                                <td style="padding:12px;text-align:center;font-weight:600;color:${lot.so_ngay_con_lai lt 0 ? '#dc3545' : (lot.so_ngay_con_lai le 7 ? '#ffc107' : '#28a745')};">
                                                    <c:choose>
                                                        <c:when test="${lot.so_ngay_con_lai lt 0}">Da het ${-lot.so_ngay_con_lai} ngay</c:when>
                                                        <c:otherwise>${lot.so_ngay_con_lai} ngay</c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td style="padding:12px;">
                                                    <c:choose>
                                                        <c:when test="${lot.trang_thai == 'da_huy'}"><span style="display:inline-block;padding:4px 12px;border-radius:999px;font-size:.88em;font-weight:600;background:#d1ecf1;color:#0c5460;">Đã hủy</span></c:when>
                                                        <c:when test="${lot.so_ngay_con_lai lt 0}"><span style="display:inline-block;padding:4px 12px;border-radius:999px;font-size:.88em;font-weight:600;background:#f8d7da;color:#721c24;">Da het han</span></c:when>
                                                        <c:when test="${lot.so_ngay_con_lai le 7}"><span style="display:inline-block;padding:4px 12px;border-radius:999px;font-size:.88em;font-weight:600;background:#fff3cd;color:#856404;">Sắp hết han</span></c:when>
                                                        <c:otherwise><span style="display:inline-block;padding:4px 12px;border-radius:999px;font-size:.88em;font-weight:600;background:#d4edda;color:#155724;">Con han</span></c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td style="padding:12px;">
                                                    <c:if test="${lot.trang_thai != 'da_huy'}">
                                                        <button onclick="cancelExpiredLot(${lot.id})" style="padding:6px 12px;background:#dc3545;color:#fff;border:none;border-radius:4px;font-size:13px;cursor:pointer;"><i class="fas fa-trash"></i> Hủy lô</button>
                                                    </c:if>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                        <c:if test="${empty expiryLots}">
                                            <tr><td colspan="10" style="padding:20px;text-align:center;color:#999;">Chưa có du lieu han su dung</td></tr>
                                        </c:if>
                                        </tbody>
                                    </table>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </c:otherwise>
                </c:choose>
            </div>
<%@ include file="/WEB-INF/views/layout/app-shell-end.jspf" %>
<script>
const ctx = '${cp}';
async function cancelExpiredLot(lotId) {
    const lyDo = prompt('Nhập lý do hủy lô:', 'Hết hạn sử dụng');
    if (lyDo === null) { return; }
    const response = await fetch(ctx + '/api/kho/expiry-cancel', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' },
        body: new URLSearchParams({ lot_id: String(lotId), ly_do: lyDo })
    });
    const data = await response.json();
    if (!data.success) { alert(data.error || 'Không thể hủy lô'); return; }
    window.location.reload();
}
async function checkExpiryAlerts() {
    const response = await fetch(ctx + '/api/kho/check-expiry-alerts', { method: 'POST' });
    const data = await response.json();
    if (!data.success) { alert(data.error || 'Không thể tạo cảnh báo'); return; }
    alert('Đã tạo ' + data.inserted + ' cảnh báo hạn dùng.');
}
</script>
</body>
</html>
