/* ===== js/utils/toast.js ===== */
// ============================================
// TOAST.JS - Toast notifications (Alias to micro-interactions Toast)
// ============================================

// Alias for backward compatibility
// The actual Toast implementation is in micro-interactions.js
window.showToast = function(message, type, duration) {
  // Wait for Toast object to be available (loaded from micro-interactions.js)
  if (typeof window.Toast !== 'undefined') {
    window.Toast.show(message, type || 'info', duration || 3000);
  } else {
    // Fallback: Wait for Toast to load
    var checkToast = setInterval(function() {
      if (typeof window.Toast !== 'undefined') {
        window.Toast.show(message, type || 'info', duration || 3000);
        clearInterval(checkToast);
      }
    }, 50);
    
    // Timeout after 1 second
    setTimeout(function() {
      clearInterval(checkToast);
    }, 1000);
  }
};



/* ===== js/utils/sidebar.js ===== */
// ============================================
// SIDEBAR.JS - Toggle sidebar menu (responsive)
// ============================================

document.addEventListener('DOMContentLoaded', function () {
  var sidebar = document.querySelector('.sidebar');
  var toggleBtn = document.querySelector('.menu-toggle');
  if (sidebar && toggleBtn) {
    toggleBtn.addEventListener('click', function () {
      sidebar.classList.toggle('active');
    });
  }
});



/* ===== js/utils/alerts.js ===== */
// ============================================
// ALERTS.JS - Auto-hide alert messages
// ============================================

document.addEventListener('DOMContentLoaded', function () {
  var alerts = document.querySelectorAll('.error, .success, .alert-success, .alert-danger');
  alerts.forEach(function(alert){
    setTimeout(function(){ 
      alert.style.transition = 'opacity 0.3s';
      alert.style.opacity = '0';
      setTimeout(function(){ alert.style.display='none'; }, 300);
    }, 3500);
  });
});



/* ===== js/utils/animations.js ===== */
// ============================================
// ANIMATIONS.JS - Scroll reveal và animation utilities
// ============================================

(function() {
  'use strict';

  // ===== SCROLL REVEAL =====
  // Fade in elements khi scroll vào view
  function initScrollReveal() {
    const reveals = document.querySelectorAll('.scroll-reveal');
    
    if (reveals.length === 0) return;

    const revealOnScroll = function() {
      reveals.forEach(function(el) {
        // Skip nếu đã revealed
        if (el.classList.contains('revealed')) return;

        const windowHeight = window.innerHeight;
        const elementTop = el.getBoundingClientRect().top;
        const elementVisible = 150; // Pixels từ bottom của viewport

        if (elementTop < windowHeight - elementVisible) {
          el.classList.add('revealed');
        }
      });
    };

    // Throttle scroll event để tối ưu performance
    let ticking = false;
    window.addEventListener('scroll', function() {
      if (!ticking) {
        window.requestAnimationFrame(function() {
          revealOnScroll();
          ticking = false;
        });
        ticking = true;
      }
    });

    // Check on page load
    revealOnScroll();
  }

  // ===== INPUT VALIDATION ANIMATIONS =====
  // NOTE: Form validation functions (shakeInput, showInputSuccess, showInputError) 
  // are now centralized in forms.js. These aliases are removed to avoid conflicts.
  // Use forms.js functions directly instead.

  // ===== BUTTON SUCCESS ANIMATION =====
  window.animateButtonSuccess = function(buttonElement) {
    if (!buttonElement) return;
    
    buttonElement.classList.add('btn-success-animation');
    setTimeout(function() {
      buttonElement.classList.remove('btn-success-animation');
    }, 400);
  };

  // ===== MODAL CLOSING ANIMATION =====
  window.closeModalWithAnimation = function(modalElement, callback) {
    if (!modalElement) return;
    
    modalElement.classList.add('closing');
    setTimeout(function() {
      modalElement.classList.remove('active', 'closing');
      if (callback && typeof callback === 'function') {
        callback();
      }
    }, 200);
  };

  // ===== COUNTER ANIMATION =====
  // Trigger count up animation
  window.triggerCounterAnimation = function(element) {
    if (!element) return;
    
    // Remove và re-add class để trigger animation
    element.classList.remove('stat-value');
    // Force reflow
    void element.offsetWidth;
    element.classList.add('stat-value');
  };

  // ===== STAGGER ANIMATION HELPER =====
  // Thêm class animate-on-load cho elements
  window.addStaggerAnimation = function(container, selector) {
    const elements = container.querySelectorAll(selector || '> *');
    elements.forEach(function(el, index) {
      el.classList.add('animate-on-load');
      // Set delay dựa trên index nếu cần custom
    });
  };

  // Auto apply stagger cho containers có class .stagger-container
  function initStaggerAnimations() {
    const containers = document.querySelectorAll('.stagger-container');
    containers.forEach(function(container) {
      const children = container.children;
      Array.from(children).forEach(function(child, index) {
        if (!child.classList.contains('animate-on-load')) {
          child.classList.add('animate-on-load');
        }
      });
    });
  }

  // ===== SMOOTH SCROLL TO ELEMENT =====
  window.smoothScrollTo = function(element, offset) {
    if (!element) return;
    
    offset = offset || 0;
    const elementPosition = element.getBoundingClientRect().top + window.pageYOffset;
    const offsetPosition = elementPosition - offset;

    window.scrollTo({
      top: offsetPosition,
      behavior: 'smooth'
    });
  };

  // ===== INITIALIZE ON DOM READY =====
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', function() {
      initScrollReveal();
      initStaggerAnimations();
    });
  } else {
    // DOM already loaded
    initScrollReveal();
    initStaggerAnimations();
  }

})();



/* ===== js/utils/forms.js ===== */
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



/* ===== js/utils/tables.js ===== */
// ============================================
// TABLES.JS - Responsive Tables Helper
// ============================================

(function() {
  'use strict';

  // ===== AUTO ADD DATA-LABEL TO TABLE CELLS =====
  // Tự động thêm data-label vào các td dựa trên th tương ứng
  function initTableLabels() {
    const tables = document.querySelectorAll('table');
    
    tables.forEach(function(table) {
      // Skip nếu đã có data-labels
      if (table.classList.contains('labels-initialized')) {
        return;
      }

      const headers = table.querySelectorAll('thead th');
      const rows = table.querySelectorAll('tbody tr');
      
      if (headers.length === 0) return;

      // Lưu header texts
      const headerTexts = [];
      headers.forEach(function(th) {
        headerTexts.push(th.textContent.trim());
      });

      // Thêm data-label vào mỗi td
      rows.forEach(function(row) {
        const cells = row.querySelectorAll('td');
        cells.forEach(function(td, index) {
          if (index < headerTexts.length) {
            // Chỉ thêm nếu chưa có data-label
            if (!td.hasAttribute('data-label')) {
              td.setAttribute('data-label', headerTexts[index]);
            }
          }
        });
      });

      // Mark as initialized
      table.classList.add('labels-initialized');
    });
  }

  // ===== INITIALIZE =====
  function init() {
    initTableLabels();
  }

  // Run on DOM ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }

  // Re-run khi có dynamic content (AJAX, etc.)
  if (typeof MutationObserver !== 'undefined') {
    const observer = new MutationObserver(function(mutations) {
      let shouldInit = false;
      mutations.forEach(function(mutation) {
        if (mutation.addedNodes.length > 0) {
          mutation.addedNodes.forEach(function(node) {
            if (node.nodeType === 1 && (node.tagName === 'TABLE' || node.querySelector('table'))) {
              shouldInit = true;
            }
          });
        }
      });
      if (shouldInit) {
        initTableLabels();
      }
    });

    observer.observe(document.body, {
      childList: true,
      subtree: true
    });
  }

})();



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



/* ===== js/utils/dark-mode.js ===== */
// ============================================
// DARK-MODE.JS - Dark Mode Toggle & Management
// ============================================

(function() {
  'use strict';

  const DarkMode = {
    // Get current theme
    getTheme: function() {
      return document.documentElement.getAttribute('data-theme') || 'light';
    },

    // Set theme
    setTheme: function(theme, skipTransition) {
      if (theme === 'dark' || theme === 'light') {
        // Skip transition on initial load
        if (skipTransition) {
          document.documentElement.setAttribute('data-theme', theme);
          localStorage.setItem('theme', theme);
          this.updateToggleButton(theme);
          return true;
        }
        
        // Add transitioning attribute for smooth animation
        document.documentElement.setAttribute('data-theme-transitioning', 'true');
        
        // Use requestAnimationFrame to ensure smooth transition
        requestAnimationFrame(function() {
          requestAnimationFrame(function() {
            document.documentElement.setAttribute('data-theme', theme);
            
            // Remove transitioning attribute after transition completes
            setTimeout(function() {
              document.documentElement.removeAttribute('data-theme-transitioning');
            }, 500); // Match CSS transition duration (0.5s)
          });
        });
        
        localStorage.setItem('theme', theme);
        this.updateToggleButton(theme);
        return true;
      }
      return false;
    },

    // Toggle theme
    toggle: function() {
      const currentTheme = this.getTheme();
      const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
      this.setTheme(newTheme);
      return newTheme;
    },

    // Load saved theme or system preference
    init: function() {
      // Check localStorage first
      const savedTheme = localStorage.getItem('theme');
      
      if (savedTheme === 'dark' || savedTheme === 'light') {
        // Skip transition on initial load (avoid flash)
        this.setTheme(savedTheme, true);
        return;
      }

      // If no saved theme, check system preference
      if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
        this.setTheme('dark', true);
      } else {
        this.setTheme('light', true);
      }
    },

    // Update toggle button icon
    updateToggleButton: function(theme) {
      const toggleButtons = document.querySelectorAll('.theme-toggle');
      toggleButtons.forEach(function(button) {
        // Button will update via CSS classes
        // This function can be used for custom logic if needed
      });
    },

    // Listen to system theme changes (optional)
    watchSystemTheme: function() {
      if (window.matchMedia) {
        const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
        
        // Listen for changes
        mediaQuery.addEventListener('change', function(e) {
          // Only apply if user hasn't set a preference
          if (!localStorage.getItem('theme')) {
            DarkMode.setTheme(e.matches ? 'dark' : 'light');
          }
        });
      }
    }
  };

  // Initialize toggle buttons
  function initToggleButtons() {
    const toggleButtons = document.querySelectorAll('.theme-toggle');
    
    toggleButtons.forEach(function(button) {
      button.addEventListener('click', function(e) {
        e.preventDefault();
        const newTheme = DarkMode.toggle();
        
        // Optional: Show toast notification
        if (typeof Toast !== 'undefined') {
          Toast.info(
            newTheme === 'dark' ? 'Đã chuyển sang chế độ tối' : 'Đã chuyển sang chế độ sáng',
            2000
          );
        }
      });
    });
  }

  // Initialize on DOM ready
  function init() {
    // Initialize theme (load from localStorage or system)
    DarkMode.init();
    
    // Initialize toggle buttons
    initToggleButtons();
    
    // Watch system theme changes (optional - only if no user preference)
    // DarkMode.watchSystemTheme(); // Uncomment if you want auto-switch based on system
    
    // Make DarkMode global
    window.DarkMode = DarkMode;
  }

  // Run on DOM ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }

})();



/* ===== js/utils/confirm.js ===== */
// ============================================
// CONFIRM.JS - Custom confirm for delete links
// ============================================

document.addEventListener('DOMContentLoaded', function () {
  document.querySelectorAll('a[data-confirm]').forEach(function(link){
    link.addEventListener('click', function(e){
      var msg = link.getAttribute('data-confirm') || 'Bạn chắc chắn muốn xóa?';
      if (!window.confirm(msg)) e.preventDefault();
    });
  });
});



/* ===== js/utils/clipboard.js ===== */
// ============================================
// CLIPBOARD.JS - Copy to clipboard functionality
// ============================================

document.addEventListener('DOMContentLoaded', function () {
  document.querySelectorAll('.copy-to-clipboard').forEach(function(element){
    element.style.cursor = 'pointer';
    element.title = 'Click để copy';
    
    element.addEventListener('click', function(){
      var text = element.getAttribute('data-copy') || element.textContent.trim();
      navigator.clipboard.writeText(text).then(function(){
        if(window.showToast) {
          showToast('Đã copy: ' + text, 'success');
        }
      }).catch(function(){
        // Fallback cho trình duyệt cũ
        var textarea = document.createElement('textarea');
        textarea.value = text;
        document.body.appendChild(textarea);
        textarea.select();
        document.execCommand('copy');
        document.body.removeChild(textarea);
        if(window.showToast) {
          showToast('Đã copy: ' + text, 'success');
        }
      });
    });
  });
});



/* ===== js/utils/keyboard.js ===== */
// ============================================
// KEYBOARD.JS - Keyboard shortcuts
// ============================================

document.addEventListener('DOMContentLoaded', function () {
  document.addEventListener('keydown', function(e){
    // Ctrl+K hoặc Cmd+K: Focus vào ô tìm kiếm
    if((e.ctrlKey || e.metaKey) && e.key === 'k'){
      e.preventDefault();
      var searchInput = document.querySelector('input[type="search"], input[name="kw"], input.search-realtime, input[placeholder*="tìm"], input[placeholder*="Tìm"]');
      if(searchInput){
        searchInput.focus();
        searchInput.select();
      }
    }
    
    // ESC: Đóng sidebar trên mobile
    if(e.key === 'Escape' && window.innerWidth < 768){
      var sidebar = document.querySelector('.sidebar');
      if(sidebar && sidebar.classList.contains('active')){
        sidebar.classList.remove('active');
      }
    }
  });
});



/* ===== js/forms/money-format.js ===== */
// ============================================
// MONEY-FORMAT.JS - Format số tiền tự động
// ============================================

document.addEventListener('DOMContentLoaded', function () {
  // Tự động format các input có class .format-money
  document.querySelectorAll('input.format-money').forEach(function(input){
    input.addEventListener('input', function(e){
      var value = e.target.value.replace(/[^\d]/g, '');
      if(value) {
        e.target.value = parseInt(value).toLocaleString('vi-VN');
      }
    });
    
    // Khi blur, thêm đơn vị tiền tệ
    input.addEventListener('blur', function(e){
      var value = e.target.value.replace(/[^\d]/g, '');
      if(value) {
        e.target.value = parseInt(value).toLocaleString('vi-VN') + '₫';
      }
    });
    
    // Khi focus, xóa đơn vị để dễ nhập
    input.addEventListener('focus', function(e){
      e.target.value = e.target.value.replace(/[^\d]/g, '');
    });
  });
});



/* ===== js/forms/loading.js ===== */
// ============================================
// LOADING.JS - Loading spinner cho form
// ============================================

document.addEventListener('DOMContentLoaded', function () {
  // Tự động hiển thị loading khi submit form có class .form-with-loading
  document.querySelectorAll('form.form-with-loading').forEach(function(form){
    form.addEventListener('submit', function(e){
      var submitBtn = form.querySelector('button[type="submit"], input[type="submit"]');
      if(submitBtn){
        submitBtn.disabled = true;
        var originalText = submitBtn.textContent || submitBtn.value;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang xử lý...';
        
        // Nếu form không submit (validation fail), restore button
        setTimeout(function(){
          if(!form.checkValidity()){
            submitBtn.disabled = false;
            submitBtn.textContent = originalText;
            submitBtn.value = originalText;
          }
        }, 100);
      }
    });
  });
});



/* ===== js/forms/validation.js ===== */
// ============================================
// VALIDATION.JS - Validation form nâng cao
// ============================================

document.addEventListener('DOMContentLoaded', function () {
  // Validate số điện thoại
  document.querySelectorAll('input[type="tel"], input[name*="dien_thoai"], input[name*="sdt"]').forEach(function(input){
    input.addEventListener('blur', function(e){
      var value = e.target.value.replace(/\D/g, '');
      if(value && (value.length < 10 || value.length > 11)){
        e.target.setCustomValidity('Số điện thoại phải có 10-11 chữ số');
        e.target.style.borderColor = '#ef4444';
      } else {
        e.target.setCustomValidity('');
        e.target.style.borderColor = '';
      }
    });
  });

  // Validate email
  document.querySelectorAll('input[type="email"]').forEach(function(input){
    input.addEventListener('blur', function(e){
      var email = e.target.value;
      var emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if(email && !emailRegex.test(email)){
        e.target.setCustomValidity('Email không hợp lệ');
        e.target.style.borderColor = '#ef4444';
      } else {
        e.target.setCustomValidity('');
        e.target.style.borderColor = '';
      }
    });
  });
});



/* ===== js/tables/search.js ===== */
// ============================================
// SEARCH.JS - Real-time search cho bảng
// ============================================

document.addEventListener('DOMContentLoaded', function () {
  // Tìm kiếm real-time cho các input có class .search-realtime
  document.querySelectorAll('input.search-realtime').forEach(function(input){
    var searchTimeout;
    input.addEventListener('input', function(e){
      clearTimeout(searchTimeout);
      var searchTerm = e.target.value.toLowerCase();
      var wrap = e.target.closest('.content-card, .tbl-wrap, .content-wrap');
      var table = wrap ? wrap.querySelector('table') : null;
      
      if(!table) return;
      
      searchTimeout = setTimeout(function(){
        var rows = table.querySelectorAll('tbody tr');
        rows.forEach(function(row){
          var text = row.textContent.toLowerCase();
          row.style.display = text.includes(searchTerm) ? '' : 'none';
        });
      }, 300);
    });
  });
});



/* ===== js/tables/sort.js ===== */
// ============================================
// SORT.JS - Sắp xếp bảng (Sort Table)
// ============================================

document.addEventListener('DOMContentLoaded', function () {
  // Click vào header để sắp xếp
  document.querySelectorAll('table th[data-sort]').forEach(function(header){
    header.style.cursor = 'pointer';
    header.style.userSelect = 'none';
    header.innerHTML += ' <i class="fas fa-sort" style="opacity:0.5;font-size:0.8em;"></i>';
    
    header.addEventListener('click', function(){
      var table = header.closest('table');
      var tbody = table.querySelector('tbody');
      var rows = Array.from(tbody.querySelectorAll('tr'));
      var columnIndex = Array.from(header.parentElement.children).indexOf(header);
      var sortType = header.getAttribute('data-sort'); // 'text', 'number', 'date'
      var isAsc = header.classList.contains('sort-asc');
      
      // Reset tất cả headers
      table.querySelectorAll('th').forEach(function(th){
        th.classList.remove('sort-asc', 'sort-desc');
      });
      
      // Sắp xếp
      rows.sort(function(a, b){
        var aCell = a.children[columnIndex];
        var bCell = b.children[columnIndex];
        var aVal = (aCell && aCell.textContent) ? aCell.textContent.trim() : '';
        var bVal = (bCell && bCell.textContent) ? bCell.textContent.trim() : '';
        
        if(sortType === 'number'){
          aVal = parseFloat(aVal.replace(/[^\d.-]/g, '')) || 0;
          bVal = parseFloat(bVal.replace(/[^\d.-]/g, '')) || 0;
          return isAsc ? bVal - aVal : aVal - bVal;
        } else if(sortType === 'date'){
          aVal = new Date(aVal);
          bVal = new Date(bVal);
          return isAsc ? bVal - aVal : aVal - bVal;
        } else {
          return isAsc ? bVal.localeCompare(aVal) : aVal.localeCompare(bVal);
        }
      });
      
      // Cập nhật DOM
      rows.forEach(function(row){ tbody.appendChild(row); });
      
      // Cập nhật icon
      header.classList.toggle('sort-asc', !isAsc);
      header.classList.toggle('sort-desc', isAsc);
      var icon = header.querySelector('i');
      if(icon){
        icon.className = isAsc ? 'fas fa-sort-up' : 'fas fa-sort-down';
        icon.style.opacity = '1';
      }
    });
  });
});



/* ===== js/tables/pagination.js ===== */
// ============================================
// PAGINATION.JS - Phân trang tự động cho bảng
// ============================================

document.addEventListener('DOMContentLoaded', function () {
  // Thêm phân trang cho bảng có class .table-paginate
  document.querySelectorAll('table.table-paginate').forEach(function(table){
    var tbody = table.querySelector('tbody');
    if(!tbody) return;
    
    var rows = Array.from(tbody.querySelectorAll('tr'));
    var rowsPerPage = 20;
    var currentPage = 1;
    var totalPages = Math.ceil(rows.length / rowsPerPage);
    
    if(rows.length <= rowsPerPage) return; // Không cần phân trang nếu ít dữ liệu
    
    function showPage(page){
      var start = (page - 1) * rowsPerPage;
      var end = start + rowsPerPage;
      
      rows.forEach(function(row, index){
        row.style.display = (index >= start && index < end) ? '' : 'none';
      });
      
      // Cập nhật pagination controls
      updatePaginationControls();
    }
    
    function updatePaginationControls(){
      var existing = table.parentElement.querySelector('.pagination');
      if(existing) existing.remove();
      
      var pagination = document.createElement('div');
      pagination.className = 'pagination';
      pagination.style.cssText = 'display:flex;justify-content:center;align-items:center;gap:8px;margin-top:20px;';
      
      // Nút Previous
      var prevBtn = document.createElement('button');
      prevBtn.textContent = '← Trước';
      prevBtn.disabled = currentPage === 1;
      prevBtn.onclick = function(){ if(currentPage > 1) showPage(--currentPage); };
      prevBtn.style.cssText = 'padding:8px 16px;border:1px solid #ddd;background:#fff;border-radius:6px;cursor:pointer;';
      pagination.appendChild(prevBtn);
      
      // Số trang
      var pageInfo = document.createElement('span');
      pageInfo.textContent = 'Trang ' + currentPage + ' / ' + totalPages;
      pageInfo.style.cssText = 'padding:0 16px;';
      pagination.appendChild(pageInfo);
      
      // Nút Next
      var nextBtn = document.createElement('button');
      nextBtn.textContent = 'Sau →';
      nextBtn.disabled = currentPage === totalPages;
      nextBtn.onclick = function(){ if(currentPage < totalPages) showPage(++currentPage); };
      nextBtn.style.cssText = 'padding:8px 16px;border:1px solid #ddd;background:#fff;border-radius:6px;cursor:pointer;';
      pagination.appendChild(nextBtn);
      
      table.parentElement.appendChild(pagination);
    }
    
    showPage(1);
  });
});



/* ===== js/tables/export.js ===== */
// ============================================
// EXPORT.JS - Export table to CSV
// ============================================

document.addEventListener('DOMContentLoaded', function () {
  // Export bảng ra CSV khi click vào nút có class .export-csv
  document.querySelectorAll('.export-csv').forEach(function(btn){
    btn.addEventListener('click', function(e){
      e.preventDefault();
      var wrap = btn.closest('.content-card, .tbl-wrap');
      var table = wrap ? wrap.querySelector('table') : null;
      if(!table) return;
      
      var rows = table.querySelectorAll('tr');
      var csv = [];
      
      rows.forEach(function(row){
        var cols = row.querySelectorAll('th, td');
        var rowData = [];
        cols.forEach(function(col){
          var text = col.textContent.trim().replace(/"/g, '""');
          rowData.push('"' + text + '"');
        });
        csv.push(rowData.join(','));
      });
      
      var csvContent = csv.join('\n');
      var blob = new Blob(['\ufeff' + csvContent], {type: 'text/csv;charset=utf-8;'});
      var link = document.createElement('a');
      link.href = URL.createObjectURL(blob);
      link.download = 'export_' + new Date().getTime() + '.csv';
      link.click();
      
      if(window.showToast) {
        showToast('Đã xuất file CSV thành công!', 'success');
      }
    });
  });
});



/* ===== js/print/print.js ===== */
// ============================================
// PRINT.JS - Print functionality
// ============================================

document.addEventListener('DOMContentLoaded', function () {
  // In trang khi click vào nút có class .print-page
  document.querySelectorAll('.print-page').forEach(function(btn){
    btn.addEventListener('click', function(e){
      e.preventDefault();
      window.print();
    });
  });
});



