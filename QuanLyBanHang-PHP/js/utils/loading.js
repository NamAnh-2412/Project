/**
 * Loading States Utilities
 * Cung cấp các hàm để dễ dàng thêm loading states vào các elements
 */

/**
 * Set loading state cho button
 * @param {string|HTMLElement} button - Button element hoặc ID
 * @param {boolean} isLoading - Trạng thái loading
 * @param {string} loadingText - Text hiển thị khi loading (optional, deprecated - use ButtonLoader instead)
 * @deprecated Use ButtonLoader.show() and ButtonLoader.hide() from micro-interactions.js instead
 */
function setButtonLoading(button, isLoading, loadingText = 'Đang xử lý...') {
  const btn = typeof button === 'string' ? document.getElementById(button) : button;
  if (!btn) return;
  
  // Use ButtonLoader if available (preferred method)
  if (typeof window.ButtonLoader !== 'undefined') {
    if (isLoading) {
      // Store loading text if provided
      if (loadingText && loadingText !== 'Đang xử lý...') {
        btn.dataset.loadingText = loadingText;
      }
      window.ButtonLoader.show(btn);
    } else {
      window.ButtonLoader.hide(btn, false);
    }
    return;
  }
  
  // Fallback: Legacy implementation
  let btnText = btn.querySelector('.btn-text');
  let btnLoading = btn.querySelector('.btn-loading');
  
  if (!btnText) {
    btnText = document.createElement('span');
    btnText.className = 'btn-text';
    btnText.innerHTML = btn.innerHTML;
    btn.innerHTML = '';
    btn.appendChild(btnText);
  }
  
  if (!btnLoading) {
    btnLoading = document.createElement('span');
    btnLoading.className = 'btn-loading';
    btn.appendChild(btnLoading);
  }
  
  if (isLoading) {
    btn.classList.add('loading');
    btn.disabled = true;
    btnLoading.innerHTML = `<i class="fas fa-spinner fa-spin"></i> ${loadingText}`;
    btnLoading.style.display = 'inline-flex';
  } else {
    btn.classList.remove('loading');
    btn.disabled = false;
    btnLoading.style.display = 'none';
  }
}

/**
 * Set loading state cho form
 * @param {string|HTMLElement} form - Form element hoặc ID
 * @param {boolean} isLoading - Trạng thái loading
 */
function setFormLoading(form, isLoading) {
  const formEl = typeof form === 'string' ? document.getElementById(form) : form;
  if (!formEl) return;
  
  if (isLoading) {
    formEl.classList.add('form-loading');
    // Disable tất cả inputs và buttons
    const inputs = formEl.querySelectorAll('input, select, textarea, button');
    inputs.forEach(input => {
      input.disabled = true;
    });
  } else {
    formEl.classList.remove('form-loading');
    // Enable lại tất cả inputs và buttons
    const inputs = formEl.querySelectorAll('input, select, textarea, button');
    inputs.forEach(input => {
      input.disabled = false;
    });
  }
}

/**
 * Hiển thị page loader (full page)
 * @param {string} text - Text hiển thị (optional)
 */
function showPageLoader(text = 'Đang tải...') {
  let loader = document.getElementById('page-loader');
  
  if (!loader) {
    loader = document.createElement('div');
    loader.id = 'page-loader';
    loader.className = 'page-loader';
    loader.innerHTML = `
      <div class="page-loader-spinner"></div>
      <div class="page-loader-text">${text}</div>
    `;
    document.body.appendChild(loader);
  } else {
    const textEl = loader.querySelector('.page-loader-text');
    if (textEl) textEl.textContent = text;
    loader.classList.remove('hidden');
  }
}

/**
 * Ẩn page loader
 */
function hidePageLoader() {
  const loader = document.getElementById('page-loader');
  if (loader) {
    loader.classList.add('hidden');
    // Xóa sau animation
    setTimeout(() => {
      if (loader.parentNode) {
        loader.parentNode.removeChild(loader);
      }
    }, 300);
  }
}

/**
 * Tạo skeleton loading cho table
 * @param {number} rows - Số dòng skeleton
 * @param {number} cols - Số cột skeleton
 * @returns {HTMLElement} Skeleton table element
 */
function createSkeletonTable(rows = 5, cols = 5) {
  const container = document.createElement('div');
  container.className = 'skeleton-table';
  
  for (let i = 0; i < rows; i++) {
    const row = document.createElement('div');
    row.className = 'skeleton-row';
    
    for (let j = 0; j < cols; j++) {
      const cell = document.createElement('div');
      cell.className = 'skeleton-cell';
      // Làm cho một số cell ngắn hơn để tự nhiên hơn
      if (j === 0) cell.classList.add('short');
      if (j === cols - 1) cell.classList.add('medium');
      row.appendChild(cell);
    }
    
    container.appendChild(row);
  }
  
  return container;
}

/**
 * Hiển thị skeleton loading trong container
 * @param {string|HTMLElement} container - Container element hoặc ID
 * @param {number} rows - Số dòng skeleton
 * @param {number} cols - Số cột skeleton
 */
function showSkeletonLoading(container, rows = 5, cols = 5) {
  const containerEl = typeof container === 'string' 
    ? document.getElementById(container) 
    : container;
  
  if (!containerEl) return;
  
  // Lưu nội dung gốc
  if (!containerEl.dataset.originalContent) {
    containerEl.dataset.originalContent = containerEl.innerHTML;
  }
  
  // Tạo và hiển thị skeleton
  const skeleton = createSkeletonTable(rows, cols);
  containerEl.innerHTML = '';
  containerEl.appendChild(skeleton);
}

/**
 * Ẩn skeleton và hiển thị lại nội dung gốc
 * @param {string|HTMLElement} container - Container element hoặc ID
 * @param {string} newContent - Nội dung mới (optional, nếu không có sẽ dùng original)
 */
function hideSkeletonLoading(container, newContent = null) {
  const containerEl = typeof container === 'string' 
    ? document.getElementById(container) 
    : container;
  
  if (!containerEl) return;
  
  if (newContent !== null) {
    containerEl.innerHTML = newContent;
  } else if (containerEl.dataset.originalContent) {
    containerEl.innerHTML = containerEl.dataset.originalContent;
    delete containerEl.dataset.originalContent;
  }
}

/**
 * Tạo inline loader (spinner nhỏ)
 * @param {string} size - Size: 'small', 'medium', 'large'
 * @returns {HTMLElement} Spinner element
 */
function createSpinner(size = 'small') {
  const spinner = document.createElement('div');
  spinner.className = `spinner-${size}`;
  return spinner;
}

/**
 * Tạo inline loader với text
 * @param {string} text - Text hiển thị
 * @param {string} size - Size: 'small', 'medium', 'large'
 * @returns {HTMLElement} Inline loader element
 */
function createInlineLoader(text = 'Đang tải...', size = 'small') {
  const loader = document.createElement('div');
  loader.className = 'inline-loader';
  loader.innerHTML = `
    <div class="spinner-${size}"></div>
    <span>${text}</span>
  `;
  return loader;
}

/**
 * Tạo progress bar
 * @param {number} percent - Phần trăm (0-100)
 * @param {string} containerId - ID của container (optional)
 * @returns {HTMLElement} Progress bar element
 */
function createProgressBar(percent = 0, containerId = null) {
  const container = document.createElement('div');
  container.className = 'progress-container';
  
  const bar = document.createElement('div');
  bar.className = 'progress-bar';
  bar.style.width = percent + '%';
  bar.textContent = percent + '%';
  
  container.appendChild(bar);
  
  if (containerId) {
    const targetContainer = document.getElementById(containerId);
    if (targetContainer) {
      targetContainer.appendChild(container);
    }
  }
  
  return container;
}

/**
 * Update progress bar
 * @param {HTMLElement} progressBar - Progress bar element
 * @param {number} percent - Phần trăm mới (0-100)
 */
function updateProgressBar(progressBar, percent) {
  if (!progressBar) return;
  
  const bar = progressBar.querySelector('.progress-bar');
  if (bar) {
    bar.style.width = percent + '%';
    bar.textContent = percent + '%';
  }
}

/**
 * Auto set button loading khi submit form
 * @param {string|HTMLElement} form - Form element hoặc ID
 * @param {string} buttonSelector - Selector của button submit (default: 'button[type="submit"]')
 */
function autoButtonLoadingOnSubmit(form, buttonSelector = 'button[type="submit"]') {
  const formEl = typeof form === 'string' ? document.getElementById(form) : form;
  if (!formEl) return;
  
  formEl.addEventListener('submit', function(e) {
    const button = formEl.querySelector(buttonSelector);
    if (button) {
      setButtonLoading(button, true, 'Đang xử lý...');
      
      // Reset sau 30 giây (fallback nếu không có response)
      setTimeout(() => {
        setButtonLoading(button, false);
      }, 30000);
    }
  });
}

// Export functions để sử dụng trong các file khác
if (typeof module !== 'undefined' && module.exports) {
  module.exports = {
    setButtonLoading,
    setFormLoading,
    showPageLoader,
    hidePageLoader,
    createSkeletonTable,
    showSkeletonLoading,
    hideSkeletonLoading,
    createSpinner,
    createInlineLoader,
    createProgressBar,
    updateProgressBar,
    autoButtonLoadingOnSubmit
  };
}

