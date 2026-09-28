<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <%@ include file="/WEB-INF/views/baocao/reports-head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/layout/app-shell-start.jspf" %>
            <c:set var="pageHeaderIcon" value="fas fa-chart-line"/>
            <c:set var="pageHeaderTitle" value="Báo cáo doanh thu"/>
            <%@ include file="/WEB-INF/views/layout/page-header.jspf" %>

            <div class="content-card">
                <form method="get" style="margin-bottom:30px;padding:20px;background:#f7fafc;border-radius:8px;display:flex;align-items:center;gap:15px;flex-wrap:wrap;">
                    <input type="hidden" name="tab" value="${tab}">
                    <label style="font-weight:600;">Tu ngay:</label>
                    <input type="date" name="ngay_bd" value="${ngay_bd}" required style="padding:8px 12px;border:1px solid #d2d8ea;border-radius:8px;">
                    <label style="font-weight:600;">Den ngay:</label>
                    <input type="date" name="ngay_kt" value="${ngay_kt}" required style="padding:8px 12px;border:1px solid #d2d8ea;border-radius:8px;">
                    <button type="submit" class="btn btn-primary" style="padding:8px 20px;">
                        <i class="fas fa-filter"></i> Loc
                    </button>
                </form>

                <div class="stats-row" style="display:grid;grid-template-columns:repeat(auto-fit,minmax(250px,1fr));gap:20px;margin-bottom:30px;">
                    <div class="stat-card" style="padding:20px;background:#f7fafc;border-radius:8px;border:1px solid #e5e7eb;">
                        <div style="font-size:14px;color:#666;margin-bottom:10px;">Tong doanh thu</div>
                        <b style="font-size:28px;color:#2196f3;"><fmt:formatNumber value="${tong_doanh_thu}" pattern="#,###"/>₫</b>
                    </div>
                    <div class="stat-card" style="padding:20px;background:#f7fafc;border-radius:8px;border:1px solid #e5e7eb;">
                        <div style="font-size:14px;color:#666;margin-bottom:10px;">Tong so don</div>
                        <b style="font-size:28px;color:#4caf50;"><fmt:formatNumber value="${tong_so_don}" pattern="#,###"/></b>
                    </div>
                    <div class="stat-card" style="padding:20px;background:#f7fafc;border-radius:8px;border:1px solid #e5e7eb;">
                        <div style="font-size:14px;color:#666;margin-bottom:10px;">Đơn hàng trung bình</div>
                        <b style="font-size:28px;color:#ff9800;">
                            <fmt:formatNumber value="${tong_so_don > 0 ? tong_doanh_thu / tong_so_don : 0}" pattern="#,###"/>₫
                        </b>
                    </div>
                </div>

                <div style="display:flex;gap:8px;margin-bottom:24px;border-bottom:2px solid #eef2fa;flex-wrap:wrap;">
                    <a href="${pageContext.request.contextPath}/bao-cao?tab=tonghop&ngay_bd=${ngay_bd}&ngay_kt=${ngay_kt}"
                       style="padding:12px 24px;text-decoration:none;color:#22334b;font-weight:600;border-bottom:3px solid ${tab == 'tonghop' ? '#2196f3' : 'transparent'};margin-bottom:-2px;">
                        <i class="fas fa-list"></i> Báo cáo tổng hợp
                    </a>
                    <a href="${pageContext.request.contextPath}/bao-cao?tab=chitiet&ngay_bd=${ngay_bd}&ngay_kt=${ngay_kt}"
                       style="padding:12px 24px;text-decoration:none;color:#22334b;font-weight:600;border-bottom:3px solid ${tab == 'chitiet' ? '#2196f3' : 'transparent'};margin-bottom:-2px;">
                        <i class="fas fa-table"></i> Báo cáo chi tiết
                    </a>
                    <a href="${pageContext.request.contextPath}/bao-cao?tab=daily_revenue&ngay_bd=${ngay_bd}&ngay_kt=${ngay_kt}"
                       style="padding:12px 24px;text-decoration:none;color:#22334b;font-weight:600;border-bottom:3px solid ${tab == 'daily_revenue' ? '#2196f3' : 'transparent'};margin-bottom:-2px;">
                        <i class="fas fa-calendar-day"></i> Doanh thu theo ngay
                    </a>
                </div>

                <c:choose>
                    <c:when test="${tab == 'chitiet'}">
                        <div style="margin-bottom:30px;">
                            <h3 style="margin-bottom:16px;color:#22334b;">Báo cáo chi tiết theo phuong thuc thanh toan</h3>
                            <div style="overflow-x:auto;">
                                <table style="width:100%;border-collapse:collapse;background:#fff;border-radius:8px;overflow:hidden;">
                                    <thead>
                                    <tr style="background:#eef2ff;">
                                        <th style="padding:12px;text-align:left;">Ngay</th>
                                        <th style="padding:12px;text-align:right;">Tien mat</th>
                                        <th style="padding:12px;text-align:right;">Chuyen khoan</th>
                                        <th style="padding:12px;text-align:right;">The</th>
                                        <c:if test="${has_app_online}">
                                            <th style="padding:12px;text-align:right;">App online</th>
                                        </c:if>
                                        <th style="padding:12px;text-align:right;">Tong tien</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <c:forEach var="row" items="${chitiet_data}">
                                        <tr style="border-bottom:1px solid #eef2fa;">
                                            <td style="padding:12px;">${row.ngay}</td>
                                            <td style="padding:12px;text-align:right;"><fmt:formatNumber value="${row.tien_mat}" pattern="#,###"/>₫</td>
                                            <td style="padding:12px;text-align:right;"><fmt:formatNumber value="${row.chuyen_khoan}" pattern="#,###"/>₫</td>
                                            <td style="padding:12px;text-align:right;"><fmt:formatNumber value="${row.the}" pattern="#,###"/>₫</td>
                                            <c:if test="${has_app_online}">
                                                <td style="padding:12px;text-align:right;"><fmt:formatNumber value="${row.app_online}" pattern="#,###"/>₫</td>
                                            </c:if>
                                            <td style="padding:12px;text-align:right;font-weight:600;color:#2196f3;"><fmt:formatNumber value="${row.tong_tien}" pattern="#,###"/>₫</td>
                                        </tr>
                                    </c:forEach>
                                    <c:if test="${empty chitiet_data}">
                                        <tr><td colspan="${has_app_online ? 6 : 5}" style="padding:20px;text-align:center;color:#999;">Chưa có dữ liệu trong khoảng thời gian này</td></tr>
                                    </c:if>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </c:when>
                    <c:when test="${tab == 'daily_revenue'}">
                        <div style="margin-bottom:30px;">
                            <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;flex-wrap:wrap;gap:12px;">
                                <h3 style="margin:0;color:#22334b;">Doanh thu theo ngay (tu bang daily_revenue)</h3>
                                <button id="btnRebuild" type="button" onclick="rebuildDailyRevenue()" class="btn btn-primary" style="padding:8px 16px;background:#ff9800;border:none;">
                                    <i class="fas fa-sync-alt"></i> Rebuild du lieu
                                </button>
                            </div>
                            <div style="padding:12px;background:#fff3cd;border:1px solid #ffc107;border-radius:8px;margin-bottom:16px;color:#856404;font-size:14px;">
                                <i class="fas fa-info-circle"></i>
                                Du lieu nay doc tu bang <code>daily_revenue</code>. Neu chua cap nhat, hay rebuild lai tu bang don hang.
                            </div>
                            <c:if test="${not empty daily_revenue_error}">
                                <div class="alert alert-danger">${daily_revenue_error}</div>
                            </c:if>
                            <div style="overflow-x:auto;">
                                <table style="width:100%;border-collapse:collapse;background:#fff;border-radius:8px;overflow:hidden;">
                                    <thead>
                                    <tr style="background:#eef2ff;">
                                        <th style="padding:12px;text-align:left;">Ngay</th>
                                        <th style="padding:12px;text-align:right;">So don</th>
                                        <th style="padding:12px;text-align:right;">Tong tien</th>
                                        <th style="padding:12px;text-align:right;">Don trung binh</th>
                                        <th style="padding:12px;text-align:left;">Cap nhat luc</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <c:set var="totalRevenue" value="0"/>
                                    <c:set var="totalOrders" value="0"/>
                                    <c:forEach var="row" items="${daily_revenue_data}">
                                        <c:set var="totalRevenue" value="${totalRevenue + row.total_amount}"/>
                                        <c:set var="totalOrders" value="${totalOrders + row.order_count}"/>
                                        <tr style="border-bottom:1px solid #eef2fa;">
                                            <td style="padding:12px;font-weight:600;">${row.day_date}</td>
                                            <td style="padding:12px;text-align:right;">${row.order_count}</td>
                                            <td style="padding:12px;text-align:right;font-weight:600;color:#2196f3;"><fmt:formatNumber value="${row.total_amount}" pattern="#,###"/>₫</td>
                                            <td style="padding:12px;text-align:right;color:#64748b;">
                                                <fmt:formatNumber value="${row.order_count > 0 ? row.total_amount / row.order_count : 0}" pattern="#,###"/>₫
                                            </td>
                                            <td style="padding:12px;color:#64748b;font-size:13px;">${empty row.updated_at ? '-' : row.updated_at}</td>
                                        </tr>
                                    </c:forEach>
                                    <c:if test="${not empty daily_revenue_data}">
                                        <tr style="background:#f7fafc;font-weight:600;border-top:2px solid #e0e0e0;">
                                            <td style="padding:12px;">Tong cong</td>
                                            <td style="padding:12px;text-align:right;color:#4caf50;">${totalOrders}</td>
                                            <td style="padding:12px;text-align:right;color:#2196f3;"><fmt:formatNumber value="${totalRevenue}" pattern="#,###"/>₫</td>
                                            <td style="padding:12px;text-align:right;color:#ff9800;">
                                                <fmt:formatNumber value="${totalOrders > 0 ? totalRevenue / totalOrders : 0}" pattern="#,###"/>₫
                                            </td>
                                            <td style="padding:12px;"></td>
                                        </tr>
                                    </c:if>
                                    <c:if test="${empty daily_revenue_data}">
                                        <tr><td colspan="5" style="padding:20px;text-align:center;color:#999;">
                                            Chưa có dữ liệu. <a href="javascript:void(0)" onclick="rebuildDailyRevenue()" style="color:#2196f3;">Click để rebuild dữ liệu</a>
                                        </td></tr>
                                    </c:if>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div style="margin-bottom:30px;">
                            <h3 style="margin-bottom:16px;color:#22334b;">Báo cáo tổng hợp theo ngay</h3>
                            <div style="overflow-x:auto;">
                                <table style="width:100%;border-collapse:collapse;background:#fff;border-radius:8px;overflow:hidden;">
                                    <thead>
                                    <tr style="background:#eef2ff;">
                                        <th style="padding:12px;text-align:left;">Ngay</th>
                                        <th style="padding:12px;text-align:right;">So don</th>
                                        <th style="padding:12px;text-align:right;">Tong tien</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <c:forEach var="row" items="${tonghop_data}">
                                        <tr style="border-bottom:1px solid #eef2fa;">
                                            <td style="padding:12px;">${row.ngay}</td>
                                            <td style="padding:12px;text-align:right;">${row.so_don}</td>
                                            <td style="padding:12px;text-align:right;font-weight:600;color:#2196f3;"><fmt:formatNumber value="${row.tong_tien}" pattern="#,###"/>₫</td>
                                        </tr>
                                    </c:forEach>
                                    <c:if test="${empty tonghop_data}">
                                        <tr><td colspan="3" style="padding:20px;text-align:center;color:#999;">Chưa có dữ liệu trong khoảng thời gian này</td></tr>
                                    </c:if>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>

                <div class="chart-card" style="margin-top:30px;">
                    <div class="chart-title">Doanh thu theo gio trong ngay</div>
                    <div class="chart-placeholder">
                        <c:forEach var="bar" items="${hourly_chart}">
                            <div class="chart-bar-wrapper">
                                <div class="chart-bar" style="height:${bar.height}px;" title="${bar.label}: ${bar.value}₫">
                                    <c:if test="${bar.value > 0}">
                                        <span class="bar-value"><fmt:formatNumber value="${bar.value / 1000}" pattern="#,###"/>k</span>
                                    </c:if>
                                </div>
                                <div class="bar-label">${bar.label}</div>
                            </div>
                        </c:forEach>
                    </div>
                </div>
            </div>
<%@ include file="/WEB-INF/views/layout/app-shell-end.jspf" %>
<script>
async function rebuildDailyRevenue() {
    if (!confirm('Ban co chac muon rebuild du lieu daily_revenue? Hanh dong nay se xoa va tinh lai toan bo tu bang don hang.')) {
        return;
    }
    const btn = document.getElementById('btnRebuild');
    if (!btn) {
        return;
    }
    const originalText = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Dang xu ly...';
    try {
        const response = await fetch('${pageContext.request.contextPath}/api/rebuild-daily-revenue', {
            method: 'POST',
            headers: { 'Accept': 'application/json' }
        });
        const data = await response.json();
        if (!data.success) {
            throw new Error(data.error || 'Rebuild that bai');
        }
        alert('Rebuild thanh cong! Da cap nhat ' + (data.inserted != null ? data.inserted : 0) + ' ban ghi.\nThoi gian: ' + (data.timestamp || ''));
        window.location.reload();
    } catch (error) {
        alert(error.message || 'Không thể rebuild dữ liệu');
        btn.disabled = false;
        btn.innerHTML = originalText;
    }
}
</script>
</body>
</html>
