// ============================================
// FORMS.JS - Form Input Utilities
// ============================================

(function() {
  'use strict';

  // ===== FLOATING LABELS INITIALIZATION =====
  function initFloatingLabels() {
    // Auto-detect inputs with placeholder=" " and add floating label behavior
    const floatingInputs = document.querySelectorAll('.form-input-floating, .form-control-floating');
    
    floatingInputs.forEach(function(input) {
      // Check if has value on load (for edit forms)
      if (input.value && input.value.trim() !== '') {
        input.classList.add('has-value');
      }

      // Update on input
      input.addEventListener('input', function() {
        if (this.value && this.value.trim() !== '') {
          this.classList.add('has-value');
        } else {
          this.classList.remove('has-value');
        }
      });

      // Update on blur (fallback)
      input.addEventListener('blur', function() {
        if (this.value && this.value.trim() !== '') {
          this.classList.add('has-value');
        }
      });
    });
  }

  // ===== FORM VALIDATION HELPERS =====
  
  // Show error state
  window.showInputError = function(inputElement, message) {
    if (!inputElement) return;
    
    // Remove success state if exists
    inputElement.classList.remove('success');
    const successIcon = inputElement.parentElement.querySelector('.form-success-icon');
    if (successIcon) successIcon.remove();
    const successMsg = inputElement.parentElement.querySelector('.form-success-message');
    if (successMsg) successMsg.remove();

    // Add error state
    inputElement.classList.add('error');
    
    // Remove existing error icon/message
    const existingIcon = inputElement.parentElement.querySelector('.form-error-icon');
    if (existingIcon) existingIcon.remove();
    const existingMsg = inputElement.parentElement.querySelector('.form-error-message');
    if (existingMsg) existingMsg.remove();

    // Create error icon
    const errorIcon = document.createElement('span');
    errorIcon.className = 'form-error-icon';
    errorIcon.innerHTML = '<i class="fas fa-exclamation-circle"></i>';
    
    // Create error message
    if (message) {
      const errorMsg = document.createElement('span');
      errorMsg.className = 'form-error-message';
      errorMsg.textContent = message;
      
      // Insert after input
      inputElement.parentElement.insertBefore(errorIcon, inputElement.nextSibling);
      inputElement.parentElement.insertBefore(errorMsg, errorIcon.nextSibling);
    } else {
      inputElement.parentElement.insertBefore(errorIcon, inputElement.nextSibling);
    }

    // Focus input for better UX
    inputElement.focus();
  };

  // Show success state
  window.showInputSuccess = function(inputElement, message) {
    if (!inputElement) return;
    
    // Remove error state if exists
    inputElement.classList.remove('error');
    const errorIcon = inputElement.parentElement.querySelector('.form-error-icon');
    if (errorIcon) errorIcon.remove();
    const errorMsg = inputElement.parentElement.querySelector('.form-error-message');
    if (errorMsg) errorMsg.remove();

    // Add success state
    inputElement.classList.add('success');
    
    // Remove existing success icon/message
    const existingIcon = inputElement.parentElement.querySelector('.form-success-icon');
    if (existingIcon) existingIcon.remove();
    const existingMsg = inputElement.parentElement.querySelector('.form-success-message');
    if (existingMsg) existingMsg.remove();

    // Create success icon
    const successIcon = document.createElement('span');
    successIcon.className = 'form-success-icon';
    successIcon.innerHTML = '<i class="fas fa-check-circle"></i>';
    
    // Create success message
    if (message) {
      const successMsg = document.createElement('span');
      successMsg.className = 'form-success-message';
      successMsg.textContent = message;
      
      inputElement.parentElement.insertBefore(successIcon, inputElement.nextSibling);
      inputElement.parentElement.insertBefore(successMsg, successIcon.nextSibling);
    } else {
      inputElement.parentElement.insertBefore(successIcon, inputElement.nextSibling);
    }
  };

  // Clear error/success state
  window.clearInputState = function(inputElement) {
    if (!inputElement) return;
    
    inputElement.classList.remove('error', 'success');
    
    const errorIcon = inputElement.parentElement.querySelector('.form-error-icon');
    if (errorIcon) errorIcon.remove();
    const errorMsg = inputElement.parentElement.querySelector('.form-error-message');
    if (errorMsg) errorMsg.remove();
    const successIcon = inputElement.parentElement.querySelector('.form-success-icon');
    if (successIcon) successIcon.remove();
    const successMsg = inputElement.parentElement.querySelector('.form-success-message');
    if (successMsg) successMsg.remove();
  };

  // Shake input (for errors)
  window.shakeInput = function(inputElement) {
    if (!inputElement) return;
    
    inputElement.classList.add('error');
    
    // Trigger shake animation
    void inputElement.offsetWidth; // Force reflow
    
    setTimeout(function() {
      inputElement.classList.remove('error');
    }, 400);
  };

  // ===== EMAIL VALIDATION =====
  window.isValidEmail = function(email) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
  };

  // ===== PHONE VALIDATION =====
  window.isValidPhone = function(phone) {
    // Vietnamese phone numbers: 10 digits, may start with 0 or +84
    const re = /^(0|\+84)[1-9][0-9]{8,9}$/;
    return re.test(phone.replace(/\s/g, ''));
  };

  // ===== REAL-TIME VALIDATION =====
  // Auto validate email inputs
  function initEmailValidation() {
    const emailInputs = document.querySelectorAll('input[type="email"]');
    
    emailInputs.forEach(function(input) {
      input.addEventListener('blur', function() {
        const value = this.value.trim();
        if (value === '') {
          clearInputState(this);
          return;
        }
        
        if (isValidEmail(value)) {
          showInputSuccess(this);
        } else {
          showInputError(this, 'Email không hợp lệ');
        }
      });

      input.addEventListener('input', function() {
        // Clear state khi user đang gõ
        if (this.classList.contains('error')) {
          clearInputState(this);
        }
      });
    });
  }

  // Auto validate phone inputs (nếu có data-validate="phone")
  function initPhoneValidation() {
    const phoneInputs = document.querySelectorAll('input[type="tel"], input[data-validate="phone"]');
    
    phoneInputs.forEach(function(input) {
      if (!input.hasAttribute('data-validate') && input.type !== 'tel') return;
      
      input.addEventListener('blur', function() {
        const value = this.value.trim();
        if (value === '') {
          clearInputState(this);
          return;
        }
        
        if (isValidPhone(value)) {
          showInputSuccess(this);
        } else {
          showInputError(this, 'Số điện thoại không hợp lệ');
        }
      });

      input.addEventListener('input', function() {
        if (this.classList.contains('error')) {
          clearInputState(this);
        }
      });
    });
  }

  // ===== PASSWORD TOGGLE =====
  function initPasswordToggle() {
    const passwordToggles = document.querySelectorAll('.input-icon-suffix[data-toggle="password"]');
    
    passwordToggles.forEach(function(toggle) {
      toggle.addEventListener('click', function() {
        const input = this.parentElement.querySelector('input');
        if (!input) return;
        
        if (input.type === 'password') {
          input.type = 'text';
          this.innerHTML = '<i class="fas fa-eye-slash"></i>';
        } else {
          input.type = 'password';
          this.innerHTML = '<i class="fas fa-eye"></i>';
        }
      });
    });
  }

  // ===== INITIALIZE =====
  function init() {
    initFloatingLabels();
    initEmailValidation();
    initPhoneValidation();
    initPasswordToggle();
  }

  // Run on DOM ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }

})();

