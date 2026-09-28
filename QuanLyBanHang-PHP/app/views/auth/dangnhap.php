<div class="auth-wrap">
  <div class="logo"><?php echo htmlspecialchars($shop_name ?? 'CHILL GUYS'); ?></div>
  <h2>Đăng Nhập</h2>
  <?php if(isset($error) && $error): ?>
    <div class="error"><?php echo htmlspecialchars($error); ?></div>
  <?php endif; ?>
  <form method="post" autocomplete="off">
    <input type="text" name="username" placeholder="Tên đăng nhập" required>
    <div class="password-container">
      <input type="password" name="password" id="password" placeholder="Mật khẩu" required>
      <span class="toggle-password">👁️</span>
    </div>
    <button type="submit" class="btn">ĐĂNG NHẬP</button>
  </form>
  <div class="small">Chưa có tài khoản? <a href="<?php echo getBaseUrl(); ?>app/auth/dangky.php">Đăng ký</a></div>
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

