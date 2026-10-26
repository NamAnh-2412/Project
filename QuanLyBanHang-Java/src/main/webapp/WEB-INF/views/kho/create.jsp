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
                <div style="margin-bottom:20px;">
                    <a href="${pageContext.request.contextPath}/kho" class="main-link" style="display:inline-block;margin-bottom:16px;">
                        <i class="fas fa-arrow-left"></i> Quay lại danh sách
                    </a>
                </div>

                <div style="display:grid;grid-template-columns:1fr 400px;gap:24px;">
                    <div>
                        <h3 style="margin-bottom:16px;color:#22334b;">Danh sách nguyên liệu</h3>
                        <div style="margin-bottom:16px;">
                            <input type="text" id="searchMaterial" placeholder="Tìm nguyên liệu..." style="width:100%;padding:10px 12px;border:1px solid #d2d8ea;border-radius:8px;">
                        </div>
                        <div style="max-height:600px;overflow-y:auto;border:1px solid #e5e7eb;border-radius:8px;padding:8px;">
                            <table style="width:100%;border-collapse:collapse;">
                                <thead style="position:sticky;top:0;background:#eef2ff;z-index:10;">
                                <tr>
                                    <th style="padding:10px;text-align:left;font-weight:600;color:#22334b;">Ten nguyen lieu</th>
                                    <th style="padding:10px;text-align:left;font-weight:600;color:#22334b;">Don vi</th>
                                    <th style="padding:10px;text-align:right;font-weight:600;color:#22334b;">SL ton</th>
                                    <th style="padding:10px;text-align:center;font-weight:600;color:#22334b;">Thao tac</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach var="item" items="${materials}">
                                    <tr class="material-row" data-name="${item.tenNguyenLieu.toLowerCase()}">
                                        <td style="padding:10px;">${item.tenNguyenLieu}</td>
                                        <td style="padding:10px;">${item.donViTinh}</td>
                                        <td style="padding:10px;text-align:right;"><fmt:formatNumber value="${item.soLuongTon}" pattern="#,##0.##"/></td>
                                        <td style="padding:10px;text-align:center;">
                                            <button type="button" class="btn-add-material"
                                                    data-id="${item.id}"
                                                    data-name="${item.tenNguyenLieu}"
                                                    data-donvi="${item.donViTinh}"
                                                    data-gianhap="${item.giaNhap != null ? item.giaNhap : 0}"
                                                    style="padding:6px 12px;background:#28a745;color:#fff;border:none;border-radius:4px;cursor:pointer;font-size:13px;">
                                                <i class="fas fa-plus"></i> Them
                                            </button>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty materials}">
                                    <tr><td colspan="4" style="padding:20px;text-align:center;color:#999;">Không có nguyên liệu</td></tr>
                                </c:if>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <div>
                        <h3 style="margin-bottom:16px;color:#22334b;">Gio hang nhap kho</h3>

                        <div style="background:#f8fafc;padding:16px;border-radius:8px;margin-bottom:16px;">
                            <div style="margin-bottom:12px;">
                                <label style="display:block;margin-bottom:6px;font-weight:600;color:#22334b;font-size:14px;">Nha cung cap (tuy chon)</label>
                                <select id="nha_cung_cap_id" style="width:100%;padding:10px 12px;border:1px solid #d2d8ea;border-radius:8px;background:#fff;color:#22334b;">
                                    <option value="">-- Chon nha cung cap --</option>
                                    <c:forEach var="sup" items="${suppliers}">
                                        <option value="${sup.id}">${sup.tenNhaCungCap}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div style="margin-bottom:12px;">
                                <label style="display:block;margin-bottom:6px;font-weight:600;color:#22334b;font-size:14px;">Ghi chu (tuy chon)</label>
                                <textarea id="ghi_chu" rows="3" style="width:100%;padding:10px 12px;border:1px solid #d2d8ea;border-radius:8px;background:#fff;color:#22334b;resize:vertical;" placeholder="Nhap ghi chu..."></textarea>
                            </div>
                        </div>

                        <div id="cart-container" style="background:#fff;border:1px solid #e5e7eb;border-radius:8px;padding:16px;max-height:400px;overflow-y:auto;margin-bottom:16px;">
                            <div id="cart-items">
                                <c:choose>
                                    <c:when test="${not empty cart}">
                                        <c:forEach var="item" items="${cart}">
                                            <div class="cart-item" data-id="${item.id}" style="display:flex;justify-content:space-between;align-items:center;padding:12px;border-bottom:1px solid #e5e7eb;margin-bottom:8px;">
                                                <div style="flex:1;">
                                                    <div style="font-weight:600;color:#22334b;margin-bottom:4px;">${item.name}</div>
                                                    <div style="font-size:13px;color:#64748b;">
                                                        SL: <fmt:formatNumber value="${item.qty}" pattern="#,##0.##"/> ${item.don_vi}
                                                        <c:if test="${item.don_gia != null && item.don_gia > 0}">
                                                            | Gia: <fmt:formatNumber value="${item.don_gia}" pattern="#,###"/>₫
                                                        </c:if>
                                                    </div>
                                                </div>
                                                <button type="button" class="btn-remove-item" data-id="${item.id}" style="padding:6px 10px;background:#dc3545;color:#fff;border:none;border-radius:4px;cursor:pointer;">
                                                    <i class="fas fa-times"></i>
                                                </button>
                                            </div>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <div style="text-align:center;padding:40px;color:#999;">Gio hang trong</div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>

                        <div style="background:#f8fafc;padding:16px;border-radius:8px;margin-bottom:16px;">
                            <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px;">
                                <span style="font-weight:600;color:#22334b;">Tong tien:</span>
                                <span id="total-amount" style="font-size:20px;font-weight:700;color:#28a745;">0₫</span>
                            </div>
                            <button type="button" id="btn-commit" style="width:100%;padding:12px;background:#28a745;color:#fff;border:none;border-radius:8px;font-weight:600;cursor:pointer;font-size:16px;" ${empty cart ? 'disabled' : ''}>
                                <i class="fas fa-check"></i> Tạo phiếu nhập kho
                            </button>
                        </div>
                    </div>
                </div>
            </div>
<%@ include file="/WEB-INF/views/layout/app-shell-end.jspf" %>

<div id="addMaterialModal" style="display:none;position:fixed;inset:0;background:rgba(0,0,0,0.5);z-index:1000;align-items:center;justify-content:center;">
    <div style="background:#fff;border-radius:12px;padding:24px;width:min(440px, calc(100vw - 32px));max-width:100%;">
        <h3 style="margin-top:0;margin-bottom:16px;color:#22334b;">Thêm nguyên liệu vào giỏ</h3>
        <form id="addMaterialForm">
            <input type="hidden" id="modal-material-id">
            <div style="margin-bottom:12px;">
                <label style="display:block;margin-bottom:6px;font-weight:600;color:#22334b;font-size:14px;">Ten nguyen lieu</label>
                <input type="text" id="modal-material-name" readonly style="width:100%;padding:10px 12px;border:1px solid #d2d8ea;border-radius:8px;background:#f8fafc;color:#22334b;">
            </div>
            <div style="margin-bottom:12px;">
                <label style="display:block;margin-bottom:6px;font-weight:600;color:#22334b;font-size:14px;">So luong *</label>
                <input type="number" id="modal-material-qty" step="0.01" min="0.01" required style="width:100%;padding:10px 12px;border:1px solid #d2d8ea;border-radius:8px;background:#fff;color:#22334b;">
            </div>
            <div style="margin-bottom:12px;">
                <label style="display:block;margin-bottom:6px;font-weight:600;color:#22334b;font-size:14px;">Don gia (₫)</label>
                <input type="number" id="modal-material-price" step="0.01" min="0" style="width:100%;padding:10px 12px;border:1px solid #d2d8ea;border-radius:8px;background:#fff;color:#22334b;">
            </div>
            <div style="margin-bottom:16px;">
                <label style="display:block;margin-bottom:6px;font-weight:600;color:#22334b;font-size:14px;">Han su dung (tuy chon)</label>
                <input type="date" id="modal-material-expiry" style="width:100%;padding:10px 12px;border:1px solid #d2d8ea;border-radius:8px;background:#fff;color:#22334b;">
            </div>
            <div style="display:flex;gap:12px;">
                <button type="button" id="btn-cancel-modal" style="flex:1;padding:10px;background:#e5e7eb;color:#22334b;border:none;border-radius:8px;font-weight:600;cursor:pointer;">Huy</button>
                <button type="submit" style="flex:1;padding:10px;background:#28a745;color:#fff;border:none;border-radius:8px;font-weight:600;cursor:pointer;">Thêm vào giỏ</button>
            </div>
        </form>
    </div>
</div>

<script src="${pageContext.request.contextPath}/js/inventory/create.js"></script>
</body>
</html>
