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

