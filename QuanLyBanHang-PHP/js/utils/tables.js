// ============================================
// TABLES.JS - Responsive Tables Helper
// ============================================

(function() {
  'use strict';

  // ===== AUTO ADD DATA-LABEL TO TABLE CELLS =====
  // Tự động thêm data-label vào các td dựa trên th tương ứng
  function initTableLabels() {
    const tables = document.querySelectorAll('table');
    
    tables.forEach(function(table) {
      // Skip nếu đã có data-labels
      if (table.classList.contains('labels-initialized')) {
        return;
      }

      const headers = table.querySelectorAll('thead th');
      const rows = table.querySelectorAll('tbody tr');
      
      if (headers.length === 0) return;

      // Lưu header texts
      const headerTexts = [];
      headers.forEach(function(th) {
        headerTexts.push(th.textContent.trim());
      });

      // Thêm data-label vào mỗi td
      rows.forEach(function(row) {
        const cells = row.querySelectorAll('td');
        cells.forEach(function(td, index) {
          if (index < headerTexts.length) {
            // Chỉ thêm nếu chưa có data-label
            if (!td.hasAttribute('data-label')) {
              td.setAttribute('data-label', headerTexts[index]);
            }
          }
        });
      });

      // Mark as initialized
      table.classList.add('labels-initialized');
    });
  }

  // ===== INITIALIZE =====
  function init() {
    initTableLabels();
  }

  // Run on DOM ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }

  // Re-run khi có dynamic content (AJAX, etc.)
  if (typeof MutationObserver !== 'undefined') {
    const observer = new MutationObserver(function(mutations) {
      let shouldInit = false;
      mutations.forEach(function(mutation) {
        if (mutation.addedNodes.length > 0) {
          mutation.addedNodes.forEach(function(node) {
            if (node.nodeType === 1 && (node.tagName === 'TABLE' || node.querySelector('table'))) {
              shouldInit = true;
            }
          });
        }
      });
      if (shouldInit) {
        initTableLabels();
      }
    });

    observer.observe(document.body, {
      childList: true,
      subtree: true
    });
  }

})();

