<div class="header-bar">
    <h2><i class="fas fa-cog"></i> Cài đặt hệ thống</h2>
    <div class="accbox">
        <div class="acc-ava"><?php echo isset($first_char) ? htmlspecialchars($first_char) : 'A'; ?></div>
        <div class="accbox-name">Xin chào, <?php echo isset($ten_nv) ? htmlspecialchars($ten_nv) : 'Admin'; ?></div>
    </div>
</div>

<?php if(isset($notice) && $notice): ?>
<div class="notice notice-<?php echo $noticeType; ?>">
    <i class="fas <?php echo $noticeType === 'success' ? 'fa-check-circle' : 'fa-exclamation-circle'; ?>"></i>
    <?php echo htmlspecialchars($notice); ?>
</div>
<?php endif; ?>

<div class="settings-container">
    <!-- Tabs Navigation -->
    <div class="settings-tabs">
        <div class="tabs-header">
            <h3 class="tabs-title">
                <i class="fas fa-sliders-h"></i>
                <span>Cài đặt</span>
            </h3>
        </div>
        <div class="tabs-list">
            <button class="tab-btn active" data-tab="shop-info">
                <div class="tab-icon-wrapper">
                    <i class="fas fa-store"></i>
                </div>
                <div class="tab-content-wrapper">
                    <span class="tab-label">Thông tin cửa hàng</span>
                    <span class="tab-desc">Logo, tên, địa chỉ</span>
                </div>
                <div class="tab-indicator"></div>
            </button>
            <button class="tab-btn" data-tab="system">
                <div class="tab-icon-wrapper">
                    <i class="fas fa-cog"></i>
                </div>
                <div class="tab-content-wrapper">
                    <span class="tab-label">Hệ thống</span>
                    <span class="tab-desc">Phân trang, hiển thị</span>
                </div>
                <div class="tab-indicator"></div>
            </button>
            <button class="tab-btn" data-tab="security">
                <div class="tab-icon-wrapper">
                    <i class="fas fa-shield-alt"></i>
                </div>
                <div class="tab-content-wrapper">
                    <span class="tab-label">Bảo mật</span>
                    <span class="tab-desc">Mật khẩu, session</span>
                </div>
                <div class="tab-indicator"></div>
            </button>
            <button class="tab-btn" data-tab="inventory">
                <div class="tab-icon-wrapper">
                    <i class="fas fa-boxes"></i>
                </div>
                <div class="tab-content-wrapper">
                    <span class="tab-label">Kho hàng</span>
                    <span class="tab-desc">Cảnh báo, tồn kho</span>
                </div>
                <div class="tab-indicator"></div>
            </button>
            <button class="tab-btn" data-tab="orders">
                <div class="tab-icon-wrapper">
                    <i class="fas fa-shopping-cart"></i>
                </div>
                <div class="tab-content-wrapper">
                    <span class="tab-label">Đơn hàng</span>
                    <span class="tab-desc">Trạng thái, thanh toán</span>
                </div>
                <div class="tab-indicator"></div>
            </button>
        </div>
    </div>

    <!-- Tab Contents -->
    <div class="settings-content">
        <!-- Tab 1: Thông tin cửa hàng -->
        <div class="tab-content active" id="tab-shop-info">
<div class="content-card">
                <h3 class="section-title">
                    <i class="fas fa-store"></i>
                    Thông tin cửa hàng
                </h3>
                
                <form method="post" id="formShopInfo" class="settings-form">
                    <input type="hidden" name="action" value="save_shop_info">
                    
                    <!-- Logo Upload -->
                    <div class="form-group">
                        <label class="form-label">
                            <i class="fas fa-image" style="margin-right: 8px; color: #f97316;"></i>
                            Logo cửa hàng
                        </label>
                        <div class="logo-upload-container">
                            <div class="logo-preview" id="logoPreview">
                                <?php 
                                $logoExists = false;
                                if (!empty($settings['logo_path'])) {
                                    $logoFullPath = defined('ROOT_PATH') ? ROOT_PATH . '/' . $settings['logo_path'] : dirname(dirname(dirname(__DIR__))) . '/' . $settings['logo_path'];
                                    $logoExists = file_exists($logoFullPath);
                                }
                                if ($logoExists): ?>
                                    <img src="<?php echo getBaseUrl() . htmlspecialchars($settings['logo_path']); ?>" alt="Logo">
                                    <button type="button" class="btn-delete-logo" id="btnDeleteLogo" title="Xóa logo">
                                        <i class="fas fa-times"></i>
                                    </button>
                                <?php else: ?>
                                    <div class="logo-placeholder">
                                        <i class="fas fa-image"></i>
                                        <span>Chưa có logo</span>
                                    </div>
  <?php endif; ?>
                            </div>
                            <div class="logo-upload-controls">
                                <label for="logoFile" class="btn btn-secondary">
                                    <i class="fas fa-upload"></i>
                                    <span>Chọn logo</span>
                                </label>
                                <input type="file" id="logoFile" name="logo" accept="image/jpeg,image/jpg,image/png,image/gif,image/webp" style="display:none;">
                            </div>
                        </div>
                    </div>
                    
    <div class="form-group">
      <label class="form-label required">Tên cửa hàng</label>
                        <input type="text" name="ten_cua_hang" class="form-input" 
                               value="<?php echo htmlspecialchars($settings['ten_cua_hang']); ?>" required>
    </div>
                    
    <div class="form-group">
      <label class="form-label">Địa chỉ</label>
                        <input type="text" name="dia_chi" class="form-input" 
                               value="<?php echo htmlspecialchars($settings['dia_chi']); ?>">
    </div>
                    
    <div class="form-group">
      <label class="form-label">Số điện thoại</label>
                        <input type="tel" name="so_dien_thoai" class="form-input" 
                               value="<?php echo htmlspecialchars($settings['so_dien_thoai']); ?>" 
                               data-validate="phone">
      <small class="form-helper-text">Ví dụ: 0123456789 hoặc +84123456789</small>
    </div>
                    
                    <div class="form-group">
                        <label class="form-label">Email</label>
                        <input type="email" name="email" class="form-input" 
                               value="<?php echo htmlspecialchars($settings['email']); ?>">
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

        <!-- Tab 2: Hệ thống -->
        <div class="tab-content" id="tab-system">
            <div class="content-card">
                <h3 class="section-title">
                    <i class="fas fa-cog"></i>
                    Cài đặt hệ thống
                </h3>
                
                <form method="post" id="formSystem" class="settings-form">
                    <input type="hidden" name="action" value="save_system">
                    
                    <div class="form-group">
                        <label class="form-label required">Số mục mỗi trang</label>
                        <input type="number" name="items_per_page" class="form-input" 
                               value="<?php echo htmlspecialchars($settings['items_per_page']); ?>" 
                               min="5" max="200" required>
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

        <!-- Tab 3: Bảo mật -->
        <div class="tab-content" id="tab-security">
            <div class="content-card">
                <h3 class="section-title">
                    <i class="fas fa-shield-alt"></i>
                    Cài đặt bảo mật
                </h3>
                
                <form method="post" id="formSecurity" class="settings-form">
                    <input type="hidden" name="action" value="save_security">
                    
                    <div class="form-group">
                        <label class="form-label required">Độ dài mật khẩu tối thiểu</label>
                        <input type="number" name="password_min_length" class="form-input" 
                               value="<?php echo htmlspecialchars($settings['password_min_length']); ?>" 
                               min="4" max="20" required>
                        <small class="form-helper-text">Số ký tự tối thiểu cho mật khẩu (4-20)</small>
                    </div>
                    
                    <div class="form-group">
                        <label class="form-label required">Thời gian timeout session (phút)</label>
                        <input type="number" name="session_timeout" class="form-input" 
                               value="<?php echo htmlspecialchars($settings['session_timeout']); ?>" 
                               min="5" max="1440" required>
                        <small class="form-helper-text">Thời gian tự động đăng xuất khi không hoạt động (5-1440 phút)</small>
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

        <!-- Tab 4: Kho hàng -->
        <div class="tab-content" id="tab-inventory">
            <div class="content-card">
                <h3 class="section-title">
                    <i class="fas fa-boxes"></i>
                    Cài đặt kho hàng
                </h3>
                
                <form method="post" id="formInventory" class="settings-form">
                    <input type="hidden" name="action" value="save_inventory">
                    
                    <div class="form-group">
                        <label class="form-label required">Cảnh báo hết hạn (ngày trước)</label>
                        <input type="number" name="expiry_alert_days_7" class="form-input" 
                               value="<?php echo htmlspecialchars($settings['expiry_alert_days_7']); ?>" 
                               min="1" max="30" required>
                        <small class="form-helper-text">Cảnh báo sớm khi còn X ngày trước khi hết hạn (1-30 ngày)</small>
                    </div>
                    
                    <div class="form-group">
                        <label class="form-label required">Cảnh báo sắp hết hạn (ngày trước)</label>
                        <input type="number" name="expiry_alert_days_1" class="form-input" 
                               value="<?php echo htmlspecialchars($settings['expiry_alert_days_1']); ?>" 
                               min="1" max="30" required>
                        <small class="form-helper-text">Cảnh báo gấp khi còn X ngày trước khi hết hạn (1-30 ngày)</small>
                    </div>
                    
                    <div class="form-group">
                        <label class="form-label required">Ngưỡng tồn kho thấp</label>
                        <input type="number" name="low_stock_threshold" class="form-input" 
                               value="<?php echo htmlspecialchars($settings['low_stock_threshold']); ?>" 
                               min="0" step="0.01" required>
                        <small class="form-helper-text">Số lượng tối thiểu để cảnh báo tồn kho thấp (mặc định cho nguyên liệu mới)</small>
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

        <!-- Tab 5: Đơn hàng -->
        <div class="tab-content" id="tab-orders">
            <div class="content-card">
                <h3 class="section-title">
                    <i class="fas fa-shopping-cart"></i>
                    Cài đặt đơn hàng
                </h3>
                
                <form method="post" id="formOrders" class="settings-form">
                    <input type="hidden" name="action" value="save_orders">
                    
                    <div class="form-group">
                        <label class="form-label required">Trạng thái đơn hàng mặc định</label>
                        <select name="default_order_status" class="form-input" required>
                            <option value="cho_xac_nhan" <?php echo $settings['default_order_status'] === 'cho_xac_nhan' ? 'selected' : ''; ?>>Chờ xác nhận</option>
                            <option value="da_xac_nhan" <?php echo $settings['default_order_status'] === 'da_xac_nhan' ? 'selected' : ''; ?>>Đã xác nhận</option>
                            <option value="dang_chuan_bi" <?php echo $settings['default_order_status'] === 'dang_chuan_bi' ? 'selected' : ''; ?>>Đang chuẩn bị</option>
                            <option value="san_sang" <?php echo $settings['default_order_status'] === 'san_sang' ? 'selected' : ''; ?>>Sẵn sàng</option>
                        </select>
                    </div>
                    
                    <div class="form-group">
                        <label class="form-label required">Phương thức thanh toán mặc định</label>
                        <select name="default_payment_method" class="form-input" required>
                            <option value="tien_mat" <?php echo $settings['default_payment_method'] === 'tien_mat' ? 'selected' : ''; ?>>Tiền mặt</option>
                            <option value="chuyen_khoan" <?php echo $settings['default_payment_method'] === 'chuyen_khoan' ? 'selected' : ''; ?>>Chuyển khoản</option>
                            <option value="the" <?php echo $settings['default_payment_method'] === 'the' ? 'selected' : ''; ?>>Thẻ</option>
                            <option value="app_online" <?php echo $settings['default_payment_method'] === 'app_online' ? 'selected' : ''; ?>>App online</option>
                        </select>
</div>

                    <div class="form-group">
                        <label class="form-label">
                            <input type="checkbox" name="allow_cancel_completed_order" value="1" 
                                   <?php echo $settings['allow_cancel_completed_order'] === '1' ? 'checked' : ''; ?>>
                            <span>Cho phép hủy đơn hàng đã hoàn thành</span>
                        </label>
                        <small class="form-helper-text">Nếu bật, nhân viên có thể hủy đơn hàng đã hoàn thành</small>
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
