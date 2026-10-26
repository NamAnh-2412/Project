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



