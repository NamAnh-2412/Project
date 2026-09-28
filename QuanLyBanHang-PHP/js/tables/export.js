// ============================================
// EXPORT.JS - Export table to CSV
// ============================================

document.addEventListener('DOMContentLoaded', function () {
  // Export bảng ra CSV khi click vào nút có class .export-csv
  document.querySelectorAll('.export-csv').forEach(function(btn){
    btn.addEventListener('click', function(e){
      e.preventDefault();
      var table = btn.closest('.content-card, .tbl-wrap')?.querySelector('table');
      if(!table) return;
      
      var rows = table.querySelectorAll('tr');
      var csv = [];
      
      rows.forEach(function(row){
        var cols = row.querySelectorAll('th, td');
        var rowData = [];
        cols.forEach(function(col){
          var text = col.textContent.trim().replace(/"/g, '""');
          rowData.push('"' + text + '"');
        });
        csv.push(rowData.join(','));
      });
      
      var csvContent = csv.join('\n');
      var blob = new Blob(['\ufeff' + csvContent], {type: 'text/csv;charset=utf-8;'});
      var link = document.createElement('a');
      link.href = URL.createObjectURL(blob);
      link.download = 'export_' + new Date().getTime() + '.csv';
      link.click();
      
      if(window.showToast) {
        showToast('Đã xuất file CSV thành công!', 'success');
      }
    });
  });
});

