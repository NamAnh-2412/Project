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

