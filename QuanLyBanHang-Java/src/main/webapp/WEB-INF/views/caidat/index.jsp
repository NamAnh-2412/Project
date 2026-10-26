<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <%@ include file="/WEB-INF/views/caidat/settings-head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/layout/app-shell-start.jspf" %>
            <%@ include file="/WEB-INF/views/layout/page-header.jspf" %>

            <%@ include file="/WEB-INF/views/layout/flash-notice.jspf" %>

            <div class="settings-container">
                <div class="settings-tabs">
                    <div class="tabs-header">
                        <h3 class="tabs-title"><i class="fas fa-sliders-h"></i><span>Cài đặt</span></h3>
                    </div>
                    <div class="tabs-list">
                        <button type="button" class="tab-btn active" data-tab="shop-info">
                            <div class="tab-icon-wrapper"><i class="fas fa-store"></i></div>
                            <div class="tab-content-wrapper">
                                <span class="tab-label">Thông tin cửa hàng</span>
                                <span class="tab-desc">Logo, tên, địa chỉ</span>
                            </div>
                            <div class="tab-indicator"></div>
                        </button>
                        <button type="button" class="tab-btn" data-tab="system">
                            <div class="tab-icon-wrapper"><i class="fas fa-cog"></i></div>
                            <div class="tab-content-wrapper">
                                <span class="tab-label">Hệ thống</span>
                                <span class="tab-desc">Phân trang, hiển thị</span>
                            </div>
                            <div class="tab-indicator"></div>
                        </button>
                        <button type="button" class="tab-btn" data-tab="security">
                            <div class="tab-icon-wrapper"><i class="fas fa-shield-alt"></i></div>
                            <div class="tab-content-wrapper">
                                <span class="tab-label">Bảo mật</span>
                                <span class="tab-desc">Mật khẩu, session</span>
                            </div>
                            <div class="tab-indicator"></div>
                        </button>
                        <button type="button" class="tab-btn" data-tab="inventory">
                            <div class="tab-icon-wrapper"><i class="fas fa-boxes"></i></div>
                            <div class="tab-content-wrapper">
                                <span class="tab-label">Kho hàng</span>
                                <span class="tab-desc">Cảnh báo, tồn kho</span>
                            </div>
                            <div class="tab-indicator"></div>
                        </button>
                        <button type="button" class="tab-btn" data-tab="orders">
                            <div class="tab-icon-wrapper"><i class="fas fa-shopping-cart"></i></div>
                            <div class="tab-content-wrapper">
                                <span class="tab-label">Đơn hàng</span>
                                <span class="tab-desc">Trạng thái, thanh toán</span>
                            </div>
                            <div class="tab-indicator"></div>
                        </button>
                    </div>
                </div>

                <div class="settings-content">
                    <div class="tab-content active" id="tab-shop-info">
                        <div class="content-card">
                            <h3 class="section-title"><i class="fas fa-store"></i> Thông tin cửa hàng</h3>
                            <form method="post" id="formShopInfo" class="settings-form">
                                <input type="hidden" name="action" value="save_shop_info">

                                <div class="form-group">
                                    <label class="form-label"><i class="fas fa-image" style="margin-right:8px;color:#f97316;"></i> Logo cửa hàng</label>
                                    <div class="logo-upload-container">
                                        <div class="logo-preview" id="logoPreview">
                                            <c:choose>
                                                <c:when test="${not empty settings.logo_path}">
                                                    <img src="${pageContext.request.contextPath}/${settings.logo_path}" alt="Logo">
                                                    <button type="button" class="btn-delete-logo" id="btnDeleteLogo" title="Xóa logo"><i class="fas fa-times"></i></button>
                                                </c:when>
                                                <c:otherwise>
                                                    <div class="logo-placeholder"><i class="fas fa-image"></i><span>Chưa có logo</span></div>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                        <div class="logo-upload-controls">
                                            <label for="logoFile" class="btn btn-secondary"><i class="fas fa-upload"></i><span>Chọn logo</span></label>
                                            <input type="file" id="logoFile" accept="image/*" style="display:none;">
                                        </div>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="form-label required">Tên cửa hàng</label>
                                    <input type="text" name="ten_cua_hang" class="form-input"
                                           value="${empty settings.ten_cua_hang ? shopName : settings.ten_cua_hang}" required>
                                </div>
                                <div class="form-group">
                                    <label class="form-label">Địa chỉ</label>
                                    <input type="text" name="dia_chi" class="form-input" value="${settings.dia_chi}">
                                </div>
                                <div class="form-group">
                                    <label class="form-label">Số điện thoại</label>
                                    <input type="tel" name="so_dien_thoai" class="form-input" value="${settings.so_dien_thoai}" data-validate="phone">
                                    <small class="form-helper-text">Ví dụ: 0123456789 hoặc +84123456789</small>
                                </div>
                                <div class="form-group">
                                    <label class="form-label">Email</label>
                                    <input type="email" name="email" class="form-input" value="${settings.email}">
                                </div>
                                <h3 class="section-title" style="margin-top:18px;"><i class="fas fa-qrcode"></i> QR chuyển khoản</h3>
                                <div class="form-group">
                                    <label class="form-label">Ngân hàng</label>
                                    <select name="bank_bin" class="form-input" id="bankBin">
                                        <option value="">Chưa dùng QR</option>
                                        <option value="970422" data-name="MB Bank">MB Bank</option>
                                        <option value="970436" data-name="Vietcombank">Vietcombank</option>
                                        <option value="970407" data-name="Techcombank">Techcombank</option>
                                        <option value="970416" data-name="ACB">ACB</option>
                                        <option value="970432" data-name="VPBank">VPBank</option>
                                        <option value="970418" data-name="BIDV">BIDV</option>
                                        <option value="970405" data-name="Agribank">Agribank</option>
                                        <option value="970423" data-name="TPBank">TPBank</option>
                                    </select>
                                    <input type="hidden" name="bank_name" id="bankName" value="${settings.bank_name}">
                                </div>
                                <div class="form-group">
                                    <label class="form-label">Số tài khoản</label>
                                    <input type="text" name="bank_account_number" class="form-input" value="${settings.bank_account_number}">
                                </div>
                                <div class="form-group">
                                    <label class="form-label">Chủ tài khoản</label>
                                    <input type="text" name="bank_account_name" class="form-input" value="${settings.bank_account_name}">
                                </div>
                                <div class="form-group">
                                    <label class="form-label">Chữ viết tắt logo</label>
                                    <input type="text" name="chu_logo_quan" class="form-input" value="${settings.chu_logo_quan}" maxlength="6" placeholder="Ví dụ: CG">
                                    <small class="form-helper-text">Hiển thị khi chưa upload logo hình.</small>
                                </div>
                                <div class="form-actions">
                                    <button type="submit" class="btn btn-primary">
                                        <span class="btn-text"><i class="fas fa-save"></i> Lưu thay đổi</span>
                                        <span class="btn-loading" style="display:none;"></span>
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>

                    <div class="tab-content" id="tab-system">
                        <div class="content-card">
                            <h3 class="section-title"><i class="fas fa-cog"></i> Cài đặt hệ thống</h3>
                            <form method="post" id="formSystem" class="settings-form">
                                <input type="hidden" name="action" value="save_system">
                                <div class="form-group">
                                    <label class="form-label required">Số mục mỗi trang</label>
                                    <input type="number" name="items_per_page" class="form-input"
                                           value="${settings.items_per_page}" min="5" max="200" required>
                                    <small class="form-helper-text">Số lượng mục hiển thị trên mỗi trang (5-200)</small>
                                </div>
                                <div class="form-actions">
                                    <button type="submit" class="btn btn-primary">
                                        <span class="btn-text"><i class="fas fa-save"></i> Lưu thay đổi</span>
                                        <span class="btn-loading" style="display:none;"></span>
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>

                    <div class="tab-content" id="tab-security">
                        <div class="content-card">
                            <h3 class="section-title"><i class="fas fa-shield-alt"></i> Cài đặt bảo mật</h3>
                            <form method="post" id="formSecurity" class="settings-form">
                                <input type="hidden" name="action" value="save_security">
                                <div class="form-group">
                                    <label class="form-label required">Độ dài mật khẩu tối thiểu</label>
                                    <input type="number" name="password_min_length" class="form-input"
                                           value="${settings.password_min_length}" min="4" max="20" required>
                                    <small class="form-helper-text">Số ký tự tối thiểu cho mật khẩu (4-20)</small>
                                </div>
                                <div class="form-group">
                                    <label class="form-label required">Thời gian timeout session (phút)</label>
                                    <input type="number" name="session_timeout" class="form-input"
                                           value="${settings.session_timeout}" min="5" max="1440" required>
                                    <small class="form-helper-text">Thời gian tự động đăng xuất khi không hoạt động (5-1440 phút)</small>
                                </div>
                                <div class="form-actions">
                                    <button type="submit" class="btn btn-primary">
                                        <span class="btn-text"><i class="fas fa-save"></i> Lưu thay đổi</span>
                                        <span class="btn-loading" style="display:none;"></span>
                                    </button>
                                </div>
                            </form>
                            <div style="margin-top:24px;padding-top:20px;border-top:1px solid #e5e7eb;">
                                <p style="color:#64748b;margin-bottom:12px;">Đổi mật khẩu đăng nhập của bạn.</p>
                                <a class="btn btn-secondary" href="${pageContext.request.contextPath}/doi-mat-khau"><i class="fas fa-key"></i> Đổi mật khẩu</a>
                            </div>
                        </div>
                    </div>

                    <div class="tab-content" id="tab-inventory">
                        <div class="content-card">
                            <h3 class="section-title"><i class="fas fa-boxes"></i> Cài đặt kho hàng</h3>
                            <form method="post" id="formInventory" class="settings-form">
                                <input type="hidden" name="action" value="save_inventory">
                                <div class="form-group">
                                    <label class="form-label required">Cảnh báo hết hạn (ngày trước)</label>
                                    <input type="number" name="expiry_alert_days_7" class="form-input"
                                           value="${settings.expiry_alert_days_7}" min="1" max="30" required>
                                    <small class="form-helper-text">Cảnh báo sớm khi còn X ngày trước khi hết hạn</small>
                                </div>
                                <div class="form-group">
                                    <label class="form-label required">Cảnh báo sắp hết hạn (ngày trước)</label>
                                    <input type="number" name="expiry_alert_days_1" class="form-input"
                                           value="${settings.expiry_alert_days_1}" min="1" max="30" required>
                                    <small class="form-helper-text">Cảnh báo gấp khi còn X ngày trước khi hết hạn</small>
                                </div>
                                <div class="form-group">
                                    <label class="form-label required">Ngưỡng tồn kho thấp</label>
                                    <input type="number" name="low_stock_threshold" class="form-input"
                                           value="${settings.low_stock_threshold}" min="0" step="0.01" required>
                                    <small class="form-helper-text">Mặc định cho nguyên liệu mới nếu không nhập</small>
                                </div>
                                <div class="form-actions">
                                    <button type="submit" class="btn btn-primary">
                                        <span class="btn-text"><i class="fas fa-save"></i> Lưu thay đổi</span>
                                        <span class="btn-loading" style="display:none;"></span>
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>

                    <div class="tab-content" id="tab-orders">
                        <div class="content-card">
                            <h3 class="section-title"><i class="fas fa-shopping-cart"></i> Cài đặt đơn hàng</h3>
                            <form method="post" id="formOrders" class="settings-form">
                                <input type="hidden" name="action" value="save_orders">
                                <div class="form-group">
                                    <label class="form-label required">Trạng thái đơn hàng mặc định</label>
                                    <select name="default_order_status" class="form-input" required>
                                        <option value="cho_xac_nhan" ${settings.default_order_status == 'cho_xac_nhan' ? 'selected' : ''}>Chờ xác nhận</option>
                                        <option value="da_xac_nhan" ${settings.default_order_status == 'da_xac_nhan' ? 'selected' : ''}>Đã xác nhận</option>
                                        <option value="dang_chuan_bi" ${settings.default_order_status == 'dang_chuan_bi' ? 'selected' : ''}>Đang chuẩn bị</option>
                                        <option value="san_sang" ${settings.default_order_status == 'san_sang' ? 'selected' : ''}>Sẵn sàng</option>
                                    </select>
                                </div>
                                <div class="form-group">
                                    <label class="form-label required">Phương thức thanh toán mặc định</label>
                                    <select name="default_payment_method" class="form-input" required>
                                        <option value="tien_mat" ${settings.default_payment_method == 'tien_mat' ? 'selected' : ''}>Tiền mặt</option>
                                        <option value="chuyen_khoan" ${settings.default_payment_method == 'chuyen_khoan' ? 'selected' : ''}>Chuyển khoản</option>
                                        <option value="the" ${settings.default_payment_method == 'the' ? 'selected' : ''}>Thẻ</option>
                                        <option value="app_online" ${settings.default_payment_method == 'app_online' ? 'selected' : ''}>App online</option>
                                    </select>
                                </div>
                                <div class="form-group">
                                    <label class="form-label">
                                        <input type="checkbox" name="allow_cancel_completed_order" value="1"
                                               ${settings.allow_cancel_completed_order == '1' ? 'checked' : ''}>
                                        <span>Cho phép hủy đơn hàng đã hoàn thành</span>
                                    </label>
                                    <small class="form-helper-text">Nếu bật, nhân viên có thể hủy đơn đã hoàn thành</small>
                                </div>
                                <div class="form-actions">
                                    <button type="submit" class="btn btn-primary">
                                        <span class="btn-text"><i class="fas fa-save"></i> Lưu thay đổi</span>
                                        <span class="btn-loading" style="display:none;"></span>
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
<script>
    (function () {
        var select = document.getElementById('bankBin');
        var name = document.getElementById('bankName');
        if (!select || !name) return;
        var saved = '${settings.bank_bin}';
        if (saved) select.value = saved;
        function sync() {
            var option = select.options[select.selectedIndex];
            name.value = option && option.getAttribute('data-name') ? option.getAttribute('data-name') : '';
        }
        select.addEventListener('change', sync);
        sync();
    })();
</script>
<%@ include file="/WEB-INF/views/layout/app-shell-end.jspf" %>
</body>
</html>
