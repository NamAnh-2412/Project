/* ===== js/utils/micro-interactions.js ===== */
// ============================================
// MICRO-INTERACTIONS.JS
// 4 Micro-interactions chính:
// 1. Button Click Effects (Ripple + Scale)
// 2. Form Validation Feedback (Icons)
// 3. Toast Animations (Slide in/out)
// 4. Loading Button States (Shrink + Spinner)
// ============================================

(function() {
  'use strict';

  // ===== 1. BUTTON RIPPLE EFFECT =====
  function initButtonRipple() {
    document.addEventListener('click', function(e) {
      const button = e.target.closest('.btn, button[type="submit"], button[type="button"], .main-link, a.btn');
      
      if (!button) return;

      // Skip nếu button đang loading
      if (button.classList.contains('loading') || button.classList.contains('btn-loading')) {
        return;
      }

      // Create ripple effect
      button.classList.add('ripple');

      // Remove ripple class sau animation
      setTimeout(function() {
        button.classList.remove('ripple');
      }, 600);
    });
  }

  // ===== 2. FORM VALIDATION FEEDBACK =====
  function initFormValidation() {
    // Tìm tất cả inputs có validation
    const inputs = document.querySelectorAll('input[type="text"], input[type="email"], input[type="tel"], input[type="number"], input[type="password"], textarea, select');
    
    inputs.forEach(function(input) {
      // Wrap input nếu chưa có wrapper
      if (!input.parentElement.classList.contains('form-input-wrapper')) {
        const wrapper = document.createElement('div');
        wrapper.className = 'form-input-wrapper';
        input.parentNode.insertBefore(wrapper, input);
        wrapper.appendChild(input);
      }

      const wrapper = input.parentElement;

      // Real-time validation (optional - có thể custom)
      if (input.hasAttribute('required') || input.hasAttribute('pattern')) {
        input.addEventListener('blur', function() {
          validateInput(this);
        });

        input.addEventListener('input', function() {
          // Clear previous states khi user đang type
          if (wrapper.classList.contains('invalid')) {
            wrapper.classList.remove('invalid', 'shake');
          }
        });
      }
    });
  }

  // Validate input
  function validateInput(input) {
    const wrapper = input.parentElement;
    
    if (!wrapper || !wrapper.classList.contains('form-input-wrapper')) {
      return;
    }

    // Remove previous states
    wrapper.classList.remove('valid', 'invalid', 'shake', 'success-animation');

    // Check validity
    if (input.checkValidity() && input.value.trim() !== '') {
      wrapper.classList.add('valid');
      // Success animation
      setTimeout(function() {
        wrapper.classList.add('success-animation');
        setTimeout(function() {
          wrapper.classList.remove('success-animation');
        }, 600);
      }, 100);
    } else if (input.hasAttribute('required') && input.value.trim() === '') {
      // Empty required field - không show error ngay
      wrapper.classList.remove('invalid');
    } else if (!input.checkValidity()) {
      // Invalid format
      wrapper.classList.add('invalid', 'shake');
      setTimeout(function() {
        wrapper.classList.remove('shake');
      }, 500);
    }
  }

  // ===== 3. TOAST NOTIFICATIONS =====
  const Toast = {
    container: null,

    // Initialize toast container
    init: function() {
      if (this.container) return;

      this.container = document.querySelector('.toast-container');
      if (!this.container) {
        this.container = document.createElement('div');
        this.container.className = 'toast-container';
        document.body.appendChild(this.container);
      }
    },

    // Show toast
    show: function(message, type, duration, title) {
      this.init();

      type = type || 'info';
      duration = duration !== undefined ? duration : 3000;

      const toast = document.createElement('div');
      toast.className = 'toast toast-' + type;
      
      // Icon based on type (using FontAwesome icons if available)
      const icons = {
        success: '<i class="fas fa-check-circle"></i>',
        error: '<i class="fas fa-times-circle"></i>',
        warning: '<i class="fas fa-exclamation-triangle"></i>',
        info: '<i class="fas fa-info-circle"></i>'
      };

      // Toast HTML
      toast.innerHTML = `
        <span class="toast-icon">${icons[type] || icons.info}</span>
        <div class="toast-content">
          ${title ? `<div class="toast-title">${title}</div>` : ''}
          <div class="toast-message">${message}</div>
        </div>
        <button class="toast-close" aria-label="Close">&times;</button>
        ${duration ? `<div class="toast-progress" style="animation-duration: ${duration}ms"></div>` : ''}
      `;

      // Close button
      toast.querySelector('.toast-close').addEventListener('click', function() {
        Toast.hide(toast);
      });

      // Add to container
      this.container.appendChild(toast);

      // Auto dismiss
      if (duration && duration > 0) {
        setTimeout(function() {
          Toast.hide(toast);
        }, duration);
      }

      return toast;
    },

    // Hide toast
    hide: function(toastElement) {
      if (!toastElement) return;
      toastElement.classList.add('toast-slide-out', 'hiding');
      setTimeout(function() {
        if (toastElement.parentNode) {
          toastElement.parentNode.removeChild(toastElement);
        }
      }, 300);
    },

    // Convenience methods
    success: function(message, duration, title) {
      return this.show(message, 'success', duration !== undefined ? duration : 3000, title);
    },

    error: function(message, duration, title) {
      return this.show(message, 'error', duration !== undefined ? duration : 5000, title);
    },

    warning: function(message, duration, title) {
      return this.show(message, 'warning', duration !== undefined ? duration : 4000, title);
    },

    info: function(message, duration, title) {
      return this.show(message, 'info', duration !== undefined ? duration : 3000, title);
    }
  };

  // Make Toast global
  window.Toast = Toast;
  
  // Also provide showToast alias for backward compatibility
  window.showToast = function(message, type, duration) {
    return Toast.show(message, type, duration);
  };

  // ===== 4. LOADING BUTTON STATES =====
  const ButtonLoader = {
    // Show loading state
    show: function(button) {
      if (!button) return;

      // Store original content
      if (!button.dataset.originalText) {
        button.dataset.originalText = button.innerHTML;
      }

      button.classList.add('loading', 'btn-loading');
      button.disabled = true;
    },

    // Hide loading state
    hide: function(button, success) {
      if (!button) return;

      button.classList.remove('loading', 'btn-loading');

      if (success) {
        // Show success state
        button.classList.add('success');
        setTimeout(function() {
          button.classList.remove('success');
          button.disabled = false;
          if (button.dataset.originalText) {
            button.innerHTML = button.dataset.originalText;
            delete button.dataset.originalText;
          }
        }, 1500);
      } else {
        // Restore original state
        button.disabled = false;
        if (button.dataset.originalText) {
          button.innerHTML = button.dataset.originalText;
          delete button.dataset.originalText;
        }
      }
    }
  };

  // Make ButtonLoader global
  window.ButtonLoader = ButtonLoader;

  // Auto-apply loading state to form submits
  function initAutoFormLoading() {
    document.addEventListener('submit', function(e) {
      const form = e.target;
      const submitButton = form.querySelector('button[type="submit"], input[type="submit"]');
      
      if (submitButton && !submitButton.classList.contains('no-auto-loading')) {
        ButtonLoader.show(submitButton);
      }
    });
  }

  // ===== INITIALIZE =====
  function init() {
    initButtonRipple();
    initFormValidation();
    Toast.init();
    initAutoFormLoading();
  }

  // Run on DOM ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }

})();



