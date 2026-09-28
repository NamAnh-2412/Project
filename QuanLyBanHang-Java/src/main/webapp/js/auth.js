document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.toggle-password').forEach(function (toggle) {
        toggle.addEventListener('click', function () {
            var container = this.closest('.password-container');
            if (!container) {
                return;
            }
            var passwordInput = container.querySelector('input[type="password"], input[type="text"]');
            if (!passwordInput) {
                return;
            }
            if (passwordInput.type === 'password') {
                passwordInput.type = 'text';
                this.textContent = '🙈';
            } else {
                passwordInput.type = 'password';
                this.textContent = '👁️';
            }
        });
    });
});
