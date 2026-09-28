// ============================================
// DARK-MODE.JS - Dark Mode Toggle & Management
// ============================================

(function() {
  'use strict';

  const DarkMode = {
    // Get current theme
    getTheme: function() {
      return document.documentElement.getAttribute('data-theme') || 'light';
    },

    // Set theme
    setTheme: function(theme, skipTransition) {
      if (theme === 'dark' || theme === 'light') {
        // Skip transition on initial load
        if (skipTransition) {
          document.documentElement.setAttribute('data-theme', theme);
          localStorage.setItem('theme', theme);
          this.updateToggleButton(theme);
          return true;
        }
        
        // Add transitioning attribute for smooth animation
        document.documentElement.setAttribute('data-theme-transitioning', 'true');
        
        // Use requestAnimationFrame to ensure smooth transition
        requestAnimationFrame(function() {
          requestAnimationFrame(function() {
            document.documentElement.setAttribute('data-theme', theme);
            
            // Remove transitioning attribute after transition completes
            setTimeout(function() {
              document.documentElement.removeAttribute('data-theme-transitioning');
            }, 500); // Match CSS transition duration (0.5s)
          });
        });
        
        localStorage.setItem('theme', theme);
        this.updateToggleButton(theme);
        return true;
      }
      return false;
    },

    // Toggle theme
    toggle: function() {
      const currentTheme = this.getTheme();
      const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
      this.setTheme(newTheme);
      return newTheme;
    },

    // Load saved theme or system preference
    init: function() {
      // Check localStorage first
      const savedTheme = localStorage.getItem('theme');
      
      if (savedTheme === 'dark' || savedTheme === 'light') {
        // Skip transition on initial load (avoid flash)
        this.setTheme(savedTheme, true);
        return;
      }

      // If no saved theme, check system preference
      if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
        this.setTheme('dark', true);
      } else {
        this.setTheme('light', true);
      }
    },

    // Update toggle button icon
    updateToggleButton: function(theme) {
      const toggleButtons = document.querySelectorAll('.theme-toggle');
      toggleButtons.forEach(function(button) {
        // Button will update via CSS classes
        // This function can be used for custom logic if needed
      });
    },

    // Listen to system theme changes (optional)
    watchSystemTheme: function() {
      if (window.matchMedia) {
        const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
        
        // Listen for changes
        mediaQuery.addEventListener('change', function(e) {
          // Only apply if user hasn't set a preference
          if (!localStorage.getItem('theme')) {
            DarkMode.setTheme(e.matches ? 'dark' : 'light');
          }
        });
      }
    }
  };

  // Initialize toggle buttons
  function initToggleButtons() {
    const toggleButtons = document.querySelectorAll('.theme-toggle');
    
    toggleButtons.forEach(function(button) {
      button.addEventListener('click', function(e) {
        e.preventDefault();
        const newTheme = DarkMode.toggle();
        
        // Optional: Show toast notification
        if (typeof Toast !== 'undefined') {
          Toast.info(
            newTheme === 'dark' ? 'Đã chuyển sang chế độ tối' : 'Đã chuyển sang chế độ sáng',
            2000
          );
        }
      });
    });
  }

  // Initialize on DOM ready
  function init() {
    // Initialize theme (load from localStorage or system)
    DarkMode.init();
    
    // Initialize toggle buttons
    initToggleButtons();
    
    // Watch system theme changes (optional - only if no user preference)
    // DarkMode.watchSystemTheme(); // Uncomment if you want auto-switch based on system
    
    // Make DarkMode global
    window.DarkMode = DarkMode;
  }

  // Run on DOM ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }

})();

