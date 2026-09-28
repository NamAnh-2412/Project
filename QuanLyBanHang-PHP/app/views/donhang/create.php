<?php
// Sử dụng getBaseUrl() để tính đường dẫn chính xác từ URL hiện tại
// Hàm này đã được load trong bootstrap.php
if (!function_exists('getBaseUrl')) {
    require_once __DIR__ . '/../../core/view.php';
}
$base_path = getBaseUrl();
?>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tạo đơn hàng - CHILL GUYS</title>
    <link rel="stylesheet" href="<?php echo $base_path; ?>assets/css/style.css">
    <link rel="stylesheet" href="<?php echo $base_path; ?>assets/css/layout.css">
    <link rel="stylesheet" href="<?php echo $base_path; ?>assets/css/ui.css">
    <link rel="stylesheet" href="<?php echo $base_path; ?>assets/css/orders.css">
    <link rel="stylesheet" href="<?php echo $base_path; ?>assets/css/customers.css">
    <link rel="stylesheet" href="<?php echo $base_path; ?>assets/css/dark-mode.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css"/>
</head>
<body class="create-order-page">
<div class="app-wrapper">
    <?php 
    // Tính đường dẫn sidebar từ vị trí view file
    $sidebar_path = __DIR__ . '/../../core/sidebar.php';
    if (file_exists($sidebar_path)) {
        include $sidebar_path;
    }
    ?>
    <div class="main-content">
        <div class="main-inner">
            <div class="header-bar-taodon">
                <div class="left">
                    <a href="<?php echo $base_path; ?>app/donhang/index.php" class="btn-back"><i class="fa fa-arrow-left"></i> Về danh sách đơn</a>
                    <h1><span class="ic"><i class="fas fa-plus-circle"></i></span> TẠO ĐƠN HÀNG</h1>
                </div>
                <button class="btn btn-primary" type="button" id="btnAddNewOrder" style="padding:10px 20px;display:flex;align-items:center;gap:8px">
                    <i class="fas fa-plus"></i> Thêm đơn hàng
                </button>
            </div>
            <?php if(isset($notice) && $notice): ?>
                <div class="alert alert-danger" style="margin:10px 20px 0 20px;flex-shrink:0;"><?php echo htmlspecialchars($notice); ?></div>
            <?php endif; ?>
            <div class="order-cards-container">
                <div class="order-cards" id="orderTabs"></div>
            </div>
            <div class="order-layout">
                <div class="panel panel-categories">
                    <div class="panel-header">
                        <i class="fas fa-list"></i>
                        <strong>Nhóm sản phẩm</strong>
                    </div>
                    <div class="cat-list">
                        <?php if(!empty($cats) && is_array($cats)): foreach($cats as $c): ?>
                            <div class="cat-item <?php echo (isset($activeCat) && $activeCat==$c['id'])?'active':''; ?>" onclick="location.href='<?php echo $base_path; ?>app/donhang/create.php?cat=<?php echo $c['id']; ?>'">
                                <?php echo htmlspecialchars($c['ten_danhmuc']);?>
                            </div>
                        <?php endforeach; else: ?>
                            <div class="empty-state">Chưa có nhóm</div>
                        <?php endif; ?>
                    </div>
                </div>
                <div class="panel panel-products">
                    <div class="panel-header">
                        <i class="fas fa-box"></i>
                        <strong>Sản phẩm</strong>
                        <input type="text" id="searchProd" class="search-input" placeholder="Tìm sản phẩm...">
                    </div>
                    <div class="prod-grid" id="prodGrid">
                        <?php if(!empty($prod) && is_array($prod)): foreach($prod as $p): ?>
                            <div class="prod-card" data-id="<?php echo $p['id']; ?>" data-name="<?php echo htmlspecialchars($p['ten_sanpham']);?>" data-price="<?php echo $p['gia_ban']; ?>">
                                <?php if(!empty($p['anh_url'])): ?>
                                    <div class="prod-image">
                                        <img src="<?php echo $base_path . htmlspecialchars($p['anh_url']); ?>" 
                                             alt="<?php echo htmlspecialchars($p['ten_sanpham']); ?>">
                                    </div>
                                <?php endif; ?>
                                <div class="prod-name"><?php echo htmlspecialchars($p['ten_sanpham']);?></div>
                                <div class="prod-price"><?php echo number_format($p['gia_ban'],0,',','.'); ?>₫</div>
                            </div>
                        <?php endforeach; else: ?>
                            <div class="empty-state">Không có sản phẩm.</div>
                        <?php endif; ?>
                    </div>
                </div>
                <div class="panel panel-cart">
                    <form method="post" id="orderForm">
                        <input type="hidden" name="action" value="create_order">
                        <input type="hidden" name="items" id="itemsInput">
                        <input type="hidden" name="phuong_thuc_thanhtoan" id="phuongThucThanhToan" value="tien_mat">
                        <div class="customer-info">
                            <div class="panel-header customer-panel-header">
                                <div class="customer-panel-title">
                                    <i class="fas fa-user"></i>
                                    <strong>Thông tin khách</strong>
                                </div>
                                <button type="button" class="btn-find-customer" id="btnFindCustomer">
                                    <i class="fas fa-search"></i> Tìm khách
                                </button>
                            </div>
                            <div id="customerMatchedBadge" class="customer-matched-badge" style="display:none;">
                                <i class="fas fa-star"></i>
                                <span>Khách lâu dài</span>
                                <span class="customer-order-count" id="customerOrderCount">0 đơn trước</span>
                            </div>
                            <div class="customer-inputs customer-search-wrapper">
                                <input name="ten_khachhang" id="tenKhachhang" placeholder="Tên khách (mặc định Khách lẻ)" class="input-field" autocomplete="off">
                                <input name="sdt_khachhang" id="sdtKhachhang" placeholder="SĐT" class="input-field input-phone" autocomplete="off">
                                <div id="customerSuggestions" class="autocomplete-suggestions"></div>
                            </div>
                        </div>
                        <div class="cart" id="cart"></div>
                        <div class="sum">
                            <div class="total-row">
                                <span>Tổng tiền:</span>
                                <b id="total">0₫</b>
                            </div>
                            <div class="cart-actions">
                                <button class="btn btn-secondary btn-save-order" type="button" id="btnLuuDon">
                                  <span class="btn-text"><i class="fas fa-save"></i> Lưu đơn</span>
                                  <span class="btn-loading" style="display:none;"></span>
                                </button>
                                <button class="btn btn-primary btn-payment" type="button" id="btnThanhToan">
                                  <span class="btn-text">Thanh toán</span>
                                  <span class="btn-loading" style="display:none;"></span>
                                </button>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Modal Thanh toán -->
<div class="payment-modal" id="paymentModal">
    <div class="payment-dialog">
        <div class="payment-header">
            <h2>Thanh toán</h2>
            <button class="payment-close" onclick="orderCreate.closePaymentModal()">×</button>
        </div>
        <div class="payment-body">
            <div class="payment-left">
                <div class="payment-methods">
                    <button class="payment-method active" data-method="tien_mat" onclick="orderCreate.selectPaymentMethod('tien_mat')">
                        <i class="fas fa-money-bill-wave"></i> Tiền mặt
                    </button>
                    <button class="payment-method" data-method="chuyen_khoan" onclick="orderCreate.selectPaymentMethod('chuyen_khoan')">
                        <i class="fas fa-university"></i> Chuyển khoản
                    </button>
                    <button class="payment-method" data-method="the" onclick="orderCreate.selectPaymentMethod('the')">
                        <i class="fas fa-credit-card"></i> Thẻ
                    </button>
                    <button class="payment-method" data-method="app_online" onclick="orderCreate.selectPaymentMethod('app_online')">
                        <i class="fas fa-mobile-alt"></i> App online
                    </button>
                </div>
            </div>
            <div class="payment-right">
                <div class="payment-info">
                    <div class="payment-row">
                        <span>Tổng tiền (1)</span>
                        <strong id="paymentTotal" style="color:#f44336;">0₫</strong>
                    </div>
                    <div class="payment-row" id="customerPaidRow" style="display:none;">
                        <label>Khách trả *</label>
                        <input type="text" id="customerPaid" placeholder="0" readonly style="text-align:right;font-size:18px;font-weight:600;padding:8px;border:1px solid #d2d8ea;border-radius:6px;width:150px;">
                    </div>
                    <div class="payment-row" id="changeRow" style="display:none;">
                        <span>Tiền thừa</span>
                        <strong id="paymentChange" style="color:#4caf50;">0₫</strong>
                    </div>
                </div>
                <div class="payment-numpad" id="paymentNumpad" style="display:none;">
                    <div class="numpad-grid">
                        <button class="numpad-btn" onclick="orderCreate.numpadInput('7')">7</button>
                        <button class="numpad-btn" onclick="orderCreate.numpadInput('8')">8</button>
                        <button class="numpad-btn" onclick="orderCreate.numpadInput('9')">9</button>
                        <button class="numpad-btn" onclick="orderCreate.numpadInput('4')">4</button>
                        <button class="numpad-btn" onclick="orderCreate.numpadInput('5')">5</button>
                        <button class="numpad-btn" onclick="orderCreate.numpadInput('6')">6</button>
                        <button class="numpad-btn" onclick="orderCreate.numpadInput('1')">1</button>
                        <button class="numpad-btn" onclick="orderCreate.numpadInput('2')">2</button>
                        <button class="numpad-btn" onclick="orderCreate.numpadInput('3')">3</button>
                        <button class="numpad-btn" onclick="orderCreate.numpadInput('0')">0</button>
                        <button class="numpad-btn" onclick="orderCreate.numpadInput('00')">00</button>
                        <button class="numpad-btn" onclick="orderCreate.numpadInput('000')">000</button>
                        <button class="numpad-btn numpad-clear" onclick="orderCreate.numpadClear()">C</button>
                    </div>
                    <div class="quick-amounts">
                        <button class="quick-btn" onclick="orderCreate.setQuickAmount(10000)">10,000</button>
                        <button class="quick-btn" onclick="orderCreate.setQuickAmount(20000)">20,000</button>
                        <button class="quick-btn" onclick="orderCreate.setQuickAmount(50000)">50,000</button>
                        <button class="quick-btn" onclick="orderCreate.setQuickAmount(100000)">100,000</button>
                        <button class="quick-btn" onclick="orderCreate.setQuickAmount(200000)">200,000</button>
                        <button class="quick-btn" onclick="orderCreate.setQuickAmount(500000)">500,000</button>
                    </div>
                </div>
            </div>
        </div>
        <div class="payment-footer">
            <button class="btn btn-secondary" onclick="orderCreate.closePaymentModal()">ĐÓNG</button>
            <button class="btn btn-primary" id="btnConfirmPayment" onclick="orderCreate.confirmPayment()">
              <span class="btn-text">XÁC NHẬN</span>
              <span class="btn-loading" style="display:none;"></span>
            </button>
        </div>
        <input type="hidden" id="selectedPaymentMethod" value="tien_mat">
    </div>
</div>

<!-- Modal Tìm khách -->
<div class="customer-find-modal" id="customerFindModal">
    <div class="customer-find-dialog">
        <div class="customer-find-header">
            <h3><i class="fas fa-users"></i> Tìm khách hàng</h3>
            <button type="button" class="customer-find-close" id="btnCloseFindCustomer">&times;</button>
        </div>
        <div class="customer-find-body">
            <input type="text" id="customerFindInput" class="input-field" placeholder="Nhập tên hoặc SĐT..." autocomplete="off">
            <div id="customerFindResults" class="customer-find-results"></div>
        </div>
    </div>
</div>

<div class="options-modal" id="optionsModal">
    <div class="options-card">
        <h3>Tùy chọn món</h3>
        <div class="row">
            <select id="optSugar"><option value="0">Đường bình thường</option><option value="-30">Đường 70%</option><option value="-50">Đường 50%</option><option value="100">Đường 100%</option></select>
            <select id="optIce"><option value="0">Đá bình thường</option><option value="-30">Ít đá</option><option value="-100">Không đá</option></select>
        </div>
        <div style="text-align:right"><button class="btn btn-secondary" onclick="closeOptions()">Đóng</button> <button class="btn btn-primary" onclick="applyOptions()">Áp dụng</button></div>
    </div>
</div>

<script src="<?php echo $base_path; ?>js/main.js"></script>
<script>
  // Set base path for API calls
  var basePath = '<?php echo rtrim($base_path, '/'); ?>';
  window.BASE_PATH = basePath ? basePath + '/' : '/';
</script>
<script src="<?php echo $base_path; ?>js/orders/create.js?v=<?php echo time(); ?>"></script>
</body>
</html>
