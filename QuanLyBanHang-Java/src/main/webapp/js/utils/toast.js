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



