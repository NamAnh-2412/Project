// ============================================
// MAIN.JS - JavaScript chung cho toàn bộ dự án
// Load tất cả các module theo thứ tự
// ============================================

// Load các module theo thứ tự
(function() {
  // Lấy base path từ global variable (được set từ PHP layout)
  // Hoặc tính toán từ vị trí của main.js (fallback)
  var basePath = '';
  
  // Ưu tiên dùng APP_BASE_PATH từ PHP
  if (typeof window.APP_BASE_PATH !== 'undefined' && window.APP_BASE_PATH) {
    basePath = window.APP_BASE_PATH;
  } else {
    // Fallback: Tính toán từ script tag
    var mainScriptTags = document.querySelectorAll('script[src*="main.js"]');
    if (mainScriptTags.length > 0) {
      var mainJsPath = mainScriptTags[0].src || mainScriptTags[0].getAttribute('src');
      if (mainJsPath) {
        // Xử lý cả absolute URL và relative path
        try {
          var url = new URL(mainJsPath, window.location.href);
          var pathname = url.pathname;
          var match = pathname.match(/^(.*\/)js\/main\.js$/);
          if (match) {
            basePath = url.origin + match[1];
          }
        } catch(e) {
          // Nếu URL() không hỗ trợ, dùng regex
          var match = mainJsPath.match(/^(.*\/)js\/main\.js(\?.*)?$/);
          if (match) {
            basePath = match[1];
          }
        }
      }
    }
    
    // Fallback cuối cùng: dùng window.location
    if (!basePath) {
      var path = window.location.pathname;
      var pathParts = path.split('/').filter(function(p) { return p && p !== 'app' && !p.endsWith('.php'); });
      basePath = window.location.origin + '/' + (pathParts.length > 0 ? pathParts.join('/') + '/' : '');
    }
  }
  
  // Đảm bảo basePath kết thúc bằng /
  if (basePath && !basePath.endsWith('/')) {
    basePath += '/';
  }
  
  var scripts = [
    basePath + 'js/utils/toast.js',           // Phải load trước vì các module khác dùng showToast
    basePath + 'js/utils/sidebar.js',
    basePath + 'js/utils/alerts.js',
    basePath + 'js/utils/animations.js',      // Scroll reveal và animation utilities
    basePath + 'js/utils/forms.js',           // Form inputs utilities (floating labels, validation)
    basePath + 'js/utils/tables.js',          // Responsive tables (auto data-label)
    basePath + 'js/utils/micro-interactions.js', // Micro-interactions (ripple, validation, toast, loading)
    basePath + 'js/utils/dark-mode.js',       // Dark mode toggle & management
    basePath + 'js/utils/confirm.js',
    basePath + 'js/utils/clipboard.js',
    basePath + 'js/utils/keyboard.js',
    basePath + 'js/forms/money-format.js',
    basePath + 'js/forms/loading.js',
    basePath + 'js/forms/validation.js',
    basePath + 'js/tables/search.js',
    basePath + 'js/tables/sort.js',
    basePath + 'js/tables/pagination.js',
    basePath + 'js/tables/export.js',
    basePath + 'js/print/print.js'
  ];
  
  function loadScript(src, callback) {
    var script = document.createElement('script');
    script.src = src;
    script.onload = callback;
    script.onerror = function() {
      console.error('Lỗi khi load script: ' + src);
      if(callback) callback();
    };
    document.head.appendChild(script);
  }
  
  function loadScripts(index) {
    if (index >= scripts.length) return;
    loadScript(scripts[index], function() {
      loadScripts(index + 1);
    });
  }
  
  // Bắt đầu load khi DOM ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', function() {
      loadScripts(0);
    });
  } else {
    loadScripts(0);
  }
})();

