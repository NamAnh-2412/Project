<div class="auth-wrap">
  <div class="logo"><?php echo htmlspecialchars($shop_name ?? 'CHILL GUYS'); ?></div>
  <h2>Đăng Ký Tài Khoản</h2>
  <?php if(isset($error) && $error): ?>
    <div class="error"><?php echo htmlspecialchars($error); ?></div>
  <?php endif; ?>
  <?php if(isset($success) && $success): ?>
    <div class="success"><?php echo $success; ?></div>
  <?php endif; ?>
  <form method="post" autocomplete="off">
    <input type="text" name="ten_nv" placeholder="Tên nhân viên" required>
    <input type="text" name="username" placeholder="Tên đăng nhập" required>
    <input type="email" name="email" placeholder="Email" required>
    <input type="text" name="sdt" placeholder="Số điện thoại" required>
    <div class="password-container">
      <input type="password" name="password" id="password" placeholder="Mật khẩu (ít nhất 6 ký tự)" required>
      <span class="toggle-password">👁️</span>
    </div>
    <div class="password-container">
      <input type="password" name="password2" id="password2" placeholder="Nhập lại mật khẩu" required>
      <span class="toggle-password">👁️</span>
    </div>
    <button type="submit" class="btn">ĐĂNG KÝ</button>
  </form>
  <div class="small">Đã có tài khoản? <a href="dangnhap.php">Đăng nhập</a></div>
</div>
<script>
  // Toggle hiển thị/ẩn mật khẩu
  document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('.toggle-password').forEach(function(toggle) {
      toggle.addEventListener('click', function() {
        const container = this.closest('.password-container');
        const passwordInput = container.querySelector('input[type="password"], input[type="text"]');
        if (passwordInput) {
          if (passwordInput.type === 'password') {
            passwordInput.type = 'text';
            this.textContent = '🙈';
          } else {
            passwordInput.type = 'password';
            this.textContent = '👁️';
          }
        }
      });
    });
  });
</script>

