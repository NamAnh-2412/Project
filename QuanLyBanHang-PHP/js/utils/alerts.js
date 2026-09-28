// ============================================
// ALERTS.JS - Auto-hide alert messages
// ============================================

document.addEventListener('DOMContentLoaded', function () {
  var alerts = document.querySelectorAll('.error, .success, .alert-success, .alert-danger');
  alerts.forEach(function(alert){
    setTimeout(function(){ 
      alert.style.transition = 'opacity 0.3s';
      alert.style.opacity = '0';
      setTimeout(function(){ alert.style.display='none'; }, 300);
    }, 3500);
  });
});

