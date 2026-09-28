<div class="container-main">
  <div class="header-bar-sp">
    <div class="left">
      <a href="index.php" class="btn-back"><i class="fa fa-arrow-left"></i> Về danh sách sản phẩm</a>
      <h1><span class="ic"><i class="fas fa-trash"></i></span> XÓA SẢN PHẨM</h1>
    </div>
  </div>
  <div class="content-wrap">
    <div class="tbl-wrap">
      <?php 
      $flashMessages = getFlashMessages();
      foreach ($flashMessages as $msg): 
      ?>
        <div class="alert alert-<?php echo $msg['type'] === 'success' ? 'success' : 'danger'; ?>">
          <?php echo htmlspecialchars($msg['message']); ?>
        </div>
      <?php endforeach; ?>
      
      <div class="alert alert-<?php echo $hasOrders ? 'warning' : 'danger'; ?>">
        <strong>
          <?php if ($hasOrders): ?>
            <i class="fa fa-exclamation-triangle"></i> Cảnh báo: Sản phẩm này đã được sử dụng trong đơn hàng!
          <?php else: ?>
            <i class="fa fa-exclamation-circle"></i> Bạn có chắc chắn muốn xóa sản phẩm này?
          <?php endif; ?>
        </strong>
        <div style="margin-top:15px;padding:15px;background:#fff;border-radius:8px">
          <div><strong>Mã sản phẩm:</strong> <?php echo htmlspecialchars($product['ma_sanpham']); ?></div>
          <div style="margin-top:8px"><strong>Tên sản phẩm:</strong> <?php echo htmlspecialchars($product['ten_sanpham']); ?></div>
          <div style="margin-top:8px"><strong>Giá bán:</strong> <?php echo number_format($product['gia_ban'], 0, ',', '.'); ?>₫</div>
          <?php if ($hasOrders): ?>
            <div style="margin-top:15px;padding:10px;background:#fff3cd;border-left:4px solid #ffc107;border-radius:4px">
              <strong>Lưu ý:</strong> Sản phẩm này đã có trong đơn hàng, hệ thống sẽ <strong>ngừng bán</strong> thay vì xóa hoàn toàn để đảm bảo tính toàn vẹn dữ liệu.
            </div>
          <?php else: ?>
            <div style="margin-top:15px;padding:10px;background:#f8d7da;border-left:4px solid #dc3545;border-radius:4px">
              <strong>Lưu ý:</strong> Hành động này không thể hoàn tác. Tất cả liên kết với nhóm lựa chọn cũng sẽ bị xóa.
            </div>
          <?php endif; ?>
        </div>
      </div>
      
      <form method="post">
        <input type="hidden" name="confirm" value="1">
        <div style="margin-top:20px;display:flex;gap:10px">
          <button class="btn btn-<?php echo $hasOrders ? 'warning' : 'danger'; ?>" type="submit">
            <?php echo $hasOrders ? 'Ngừng bán sản phẩm' : 'Xác nhận xóa'; ?>
          </button>
          <a href="index.php" class="btn btn-secondary" style="text-decoration:none;display:inline-block;">Hủy</a>
        </div>
      </form>
    </div>
  </div>
</div>

