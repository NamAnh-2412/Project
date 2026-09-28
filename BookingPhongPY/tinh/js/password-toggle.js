/**
 * Hàm togglePassword - Hiển thị/ẩn mật khẩu
 * @param {string} inputId - ID của input password
 */
function togglePassword(inputId) {
    const input = document.getElementById(inputId);
    if (!input) {
        console.warn('Password toggle: Input element not found');
        return;
    }
    
    const button = input.nextElementSibling;
    const icon = button ? button.querySelector('.password-icon') : null;
    
    if (input.type === 'password') {
        input.type = 'text';
        if (icon) {
            icon.className = 'password-icon show';
        }
        if (button) {
            button.title = 'Ẩn mật khẩu';
        }
    } else {
        input.type = 'password';
        if (icon) {
            icon.className = 'password-icon hide';
        }
        if (button) {
            button.title = 'Hiển thị mật khẩu';
        }
    }
}

