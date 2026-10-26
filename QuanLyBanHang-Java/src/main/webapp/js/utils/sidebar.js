/* ===== js/utils/sidebar.js ===== */
// ============================================
// SIDEBAR.JS - Toggle sidebar menu (responsive)
// ============================================

document.addEventListener('DOMContentLoaded', function () {
  var sidebar = document.querySelector('.sidebar');
  var toggleBtn = document.querySelector('.menu-toggle');
  var backdrop = document.querySelector('.sidebar-backdrop');
  if (!sidebar || !toggleBtn) {
    return;
  }

  var desktopQuery = window.matchMedia('(min-width: 1101px)');

  function setOpen(open) {
    sidebar.classList.toggle('active', open);
    document.body.classList.toggle('sidebar-open', open);
    toggleBtn.setAttribute('aria-expanded', open ? 'true' : 'false');
    toggleBtn.setAttribute('aria-label', open ? 'Đóng menu' : 'Mở menu');
  }

  toggleBtn.addEventListener('click', function () {
    setOpen(!sidebar.classList.contains('active'));
  });

  if (backdrop) {
    backdrop.addEventListener('click', function () {
      setOpen(false);
    });
  }

  document.addEventListener('keydown', function (event) {
    if (event.key === 'Escape') {
      setOpen(false);
    }
  });

  sidebar.addEventListener('click', function (event) {
    if (desktopQuery.matches) {
      return;
    }
    var link = event.target.closest('a');
    if (!link || link.getAttribute('href') === '#') {
      return;
    }
    setOpen(false);
  });

  function closeIfDesktop() {
    if (desktopQuery.matches) {
      setOpen(false);
    }
  }

  if (desktopQuery.addEventListener) {
    desktopQuery.addEventListener('change', closeIfDesktop);
  } else if (desktopQuery.addListener) {
    desktopQuery.addListener(closeIfDesktop);
  }
});



