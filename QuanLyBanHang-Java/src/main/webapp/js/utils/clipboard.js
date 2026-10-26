/* ===== js/utils/clipboard.js ===== */
// ============================================
// CLIPBOARD.JS - Copy to clipboard functionality
// ============================================

document.addEventListener('DOMContentLoaded', function () {
  document.querySelectorAll('.copy-to-clipboard').forEach(function(element){
    element.style.cursor = 'pointer';
    element.title = 'Click để copy';
    
    element.addEventListener('click', function(){
      var text = element.getAttribute('data-copy') || element.textContent.trim();
      navigator.clipboard.writeText(text).then(function(){
        if(window.showToast) {
          showToast('Đã copy: ' + text, 'success');
        }
      }).catch(function(){
        // Fallback cho trình duyệt cũ
        var textarea = document.createElement('textarea');
        textarea.value = text;
        document.body.appendChild(textarea);
        textarea.select();
        document.execCommand('copy');
        document.body.removeChild(textarea);
        if(window.showToast) {
          showToast('Đã copy: ' + text, 'success');
        }
      });
    });
  });
});



