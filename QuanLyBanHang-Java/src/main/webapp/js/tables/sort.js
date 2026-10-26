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



