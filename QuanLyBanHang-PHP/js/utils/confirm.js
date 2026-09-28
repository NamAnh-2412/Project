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

