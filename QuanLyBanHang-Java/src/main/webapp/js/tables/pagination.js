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



