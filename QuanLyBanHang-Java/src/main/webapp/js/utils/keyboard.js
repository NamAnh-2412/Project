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



