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



