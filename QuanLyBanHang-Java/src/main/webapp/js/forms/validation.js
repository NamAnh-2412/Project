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



