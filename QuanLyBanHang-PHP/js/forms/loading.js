// ============================================
// LOADING.JS - Loading spinner cho form
// ============================================

document.addEventListener('DOMContentLoaded', function () {
  // Tự động hiển thị loading khi submit form có class .form-with-loading
  document.querySelectorAll('form.form-with-loading').forEach(function(form){
    form.addEventListener('submit', function(e){
      var submitBtn = form.querySelector('button[type="submit"], input[type="submit"]');
      if(submitBtn){
        submitBtn.disabled = true;
        var originalText = submitBtn.textContent || submitBtn.value;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang xử lý...';
        
        // Nếu form không submit (validation fail), restore button
        setTimeout(function(){
          if(!form.checkValidity()){
            submitBtn.disabled = false;
            submitBtn.textContent = originalText;
            submitBtn.value = originalText;
          }
        }, 100);
      }
    });
  });
});

