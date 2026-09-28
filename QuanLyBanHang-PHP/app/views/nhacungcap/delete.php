<div class="container-main">
  <div class="content-wrap">
    <div class="tbl-wrap">
      <div class="add-form">
        <h3>Xóa nhà cung cấp</h3>
        <?php if($hasImports): ?>
          <div style="padding:15px;background:#fff3cd;border:1px solid #ffc107;border-radius:8px;margin-bottom:20px;">
            <strong style="color:#856404;">⚠️ Không thể xóa nhà cung cấp này!</strong>
            <p style="margin:10px 0 0 0;color:#856404;">
              Nhà cung cấp này đã được sử dụng trong <?php echo $usedInImports['count']; ?> phiếu nhập kho. 
              Bạn chỉ có thể đổi trạng thái thành "Ngừng hợp tác".
            </p>
          </div>
        <?php else: ?>
          <div style="padding:15px;background:#f8d7da;border:1px solid #dc3545;border-radius:8px;margin-bottom:20px;">
            <strong style="color:#721c24;">⚠️ Bạn có chắc chắn muốn xóa nhà cung cấp này?</strong>
            <p style="margin:10px 0 0 0;color:#721c24;">
              Tên: <strong><?php echo htmlspecialchars($row['ten_nhacungcap']); ?></strong><br>
              Hành động này không thể hoàn tác!
            </p>
          </div>
        <?php endif; ?>
        
        <form method="post">
          <input type="hidden" name="confirm" value="1">
          <div style="margin-top:20px;">
            <?php if($hasImports): ?>
              <button class="btn btn-warning" type="submit">Đổi trạng thái thành "Ngừng hợp tác"</button>
            <?php else: ?>
              <button class="btn btn-danger" type="submit">Xác nhận xóa</button>
            <?php endif; ?>
            <a class="btn btn-secondary" href="index.php">Hủy</a>
          </div>
        </form>
      </div>
    </div>
  </div>
</div>

