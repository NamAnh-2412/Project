// ============================================
// SIDEBAR.JS - Toggle sidebar menu (responsive)
// ============================================

document.addEventListener('DOMContentLoaded', function () {
  var sidebar = document.querySelector('.sidebar');
  var toggleBtn = document.querySelector('.menu-toggle');
  if (sidebar && toggleBtn) {
    toggleBtn.addEventListener('click', function () {
      sidebar.classList.toggle('active');
    });
  }
});

