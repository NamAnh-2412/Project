(function() {
  // Get basePath from window (set by PHP) or calculate from current location
  var basePath = '';
  
  // First, try to use the basePath set in the view
  if (typeof window.APP_BASE_PATH !== 'undefined' && window.APP_BASE_PATH) {
    basePath = window.APP_BASE_PATH;
  } else {
    // Calculate from current location
    var path = window.location.pathname;
    // Remove /app/kho/create.php or similar
    var pathParts = path.split('/').filter(function(p) { 
      return p && p !== 'app' && !p.endsWith('.php'); 
    });
    basePath = window.location.origin + '/' + (pathParts.length > 0 ? pathParts.join('/') + '/' : '');
  }
  
  // Ensure basePath ends with /
  if (basePath && !basePath.endsWith('/')) {
    basePath += '/';
  }
  
  // Get elements
  var searchInput = document.getElementById('searchMaterial');
  var materialRows = document.querySelectorAll('.material-row');
  var addMaterialBtns = document.querySelectorAll('.btn-add-material');
  var cartContainer = document.getElementById('cart-items');
  var totalAmountEl = document.getElementById('total-amount');
  var btnCommit = document.getElementById('btn-commit');
  var modal = document.getElementById('addMaterialModal');
  var addMaterialForm = document.getElementById('addMaterialForm');
  var btnCancelModal = document.getElementById('btn-cancel-modal');

  // Search functionality
  if (searchInput) {
    searchInput.addEventListener('input', function() {
      var searchTerm = this.value.toLowerCase();
      materialRows.forEach(function(row) {
        var name = row.getAttribute('data-name') || '';
        if (name.includes(searchTerm)) {
          row.style.display = '';
        } else {
          row.style.display = 'none';
        }
      });
    });
  }

  // Open modal when click "Thêm"
  addMaterialBtns.forEach(function(btn) {
    btn.addEventListener('click', function() {
      var id = this.getAttribute('data-id');
      var name = this.getAttribute('data-name');
      var donvi = this.getAttribute('data-donvi');
      var gianhap = this.getAttribute('data-gianhap') || '0';

      document.getElementById('modal-material-id').value = id;
      document.getElementById('modal-material-name').value = name;
      document.getElementById('modal-material-qty').value = '';
      document.getElementById('modal-material-price').value = gianhap;
      document.getElementById('modal-material-expiry').value = '';

      modal.style.display = 'flex';
    });
  });

  // Close modal
  function closeModal() {
    modal.style.display = 'none';
    addMaterialForm.reset();
  }

  if (btnCancelModal) {
    btnCancelModal.addEventListener('click', closeModal);
  }

  // Add material to cart
  if (addMaterialForm) {
    addMaterialForm.addEventListener('submit', function(e) {
      e.preventDefault();

      var id = document.getElementById('modal-material-id').value;
      var qty = parseFloat(document.getElementById('modal-material-qty').value);
      var donGia = parseFloat(document.getElementById('modal-material-price').value) || 0;
      var hanSuDung = document.getElementById('modal-material-expiry').value || null;

      if (!id || qty <= 0) {
        if (typeof showToast === 'function') {
          showToast('Vui lòng nhập số lượng hợp lệ', 'error');
        } else {
          alert('Vui lòng nhập số lượng hợp lệ');
        }
        return;
      }

      // Disable submit button to prevent double submission
      var submitBtn = addMaterialForm.querySelector('button[type="submit"]');
      var originalBtnText = submitBtn ? submitBtn.textContent : '';
      if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Đang thêm...';
      }

      // Calculate URL from current page location
      // Current page is app/kho/create.php, so add_cart.php is in the same directory
      var currentPath = window.location.pathname;
      var addCartUrl = currentPath.replace(/create\.php$/, 'add_cart.php');
      
      // Call API to add to cart
      fetch(addCartUrl, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          id: parseInt(id),
          qty: qty,
          don_gia: donGia,
          han_su_dung: hanSuDung
        })
      })
      .then(function(response) {
        // Get response text first to check if it's valid JSON
        return response.text().then(function(text) {
          // Check if response is ok
          if (!response.ok) {
            // Try to parse error message from JSON
            try {
              var errorData = JSON.parse(text);
              throw new Error(errorData.error || 'HTTP error! status: ' + response.status);
            } catch (e) {
              if (e.message && e.message.includes('HTTP error')) {
                throw e;
              }
              throw new Error('HTTP error! status: ' + response.status + '. Response: ' + text.substring(0, 100));
            }
          }
          
          // Try to parse JSON
          try {
            return JSON.parse(text);
          } catch (e) {
            console.error('Invalid JSON response:', text);
            throw new Error('Phản hồi từ server không hợp lệ. Vui lòng kiểm tra console để xem chi tiết.');
          }
        });
      })
      .then(function(data) {
        // Re-enable button
        if (submitBtn) {
          submitBtn.disabled = false;
          submitBtn.textContent = originalBtnText;
        }

        if (data && data.success) {
          closeModal();
          // Show success message
          if (typeof showToast === 'function') {
            showToast('Đã thêm nguyên liệu vào giỏ hàng', 'success');
          }
          // Update cart without full page reload
          updateCartFromServer();
        } else {
          var errorMsg = (data && data.error) ? data.error : 'Không thể thêm vào giỏ hàng';
          if (typeof showToast === 'function') {
            showToast('Lỗi: ' + errorMsg, 'error');
          } else {
            alert('Lỗi: ' + errorMsg);
          }
        }
      })
      .catch(function(error) {
        console.error('Error:', error);
        console.error('Error message:', error.message);
        console.error('Error stack:', error.stack);
        
        // Re-enable button
        if (submitBtn) {
          submitBtn.disabled = false;
          submitBtn.textContent = originalBtnText;
        }
        
        // Show error message
        var errorMsg = error.message || 'Có lỗi xảy ra khi thêm vào giỏ hàng';
        if (typeof showToast === 'function') {
          showToast(errorMsg, 'error');
        } else {
          alert(errorMsg);
        }
      });
    });
  }

  // Function to update cart from server
  function updateCartFromServer() {
    // Fetch current cart data from server by reloading the page data
    // For now, we'll do a simple reload but could be improved with an API endpoint
    // that returns just the cart HTML
    location.reload();
  }

  // Function to render cart items
  function renderCartItems(cartItems) {
    if (!cartContainer) return;
    
    if (!cartItems || cartItems.length === 0) {
      cartContainer.innerHTML = '<div style="text-align:center;padding:40px;color:#999;">Giỏ hàng trống</div>';
      if (btnCommit) {
        btnCommit.disabled = true;
      }
      if (totalAmountEl) {
        totalAmountEl.textContent = '0₫';
      }
      return;
    }

    var html = '';
    var total = 0;
    cartItems.forEach(function(item) {
      var itemTotal = (item.don_gia || 0) * (item.qty || 0);
      total += itemTotal;
      
      html += '<div class="cart-item" data-id="' + item.id + '" style="display:flex;justify-content:space-between;align-items:center;padding:12px;border-bottom:1px solid #e5e7eb;margin-bottom:8px;">';
      html += '<div style="flex:1;">';
      html += '<div style="font-weight:600;color:#22334b;margin-bottom:4px;">' + (item.name || '') + '</div>';
      html += '<div style="font-size:13px;color:#64748b;">';
      html += 'SL: ' + (item.qty ? parseFloat(item.qty).toLocaleString('vi-VN', {minimumFractionDigits: 2, maximumFractionDigits: 2}) : '0') + ' ' + (item.don_vi || '');
      if (item.don_gia && item.don_gia > 0) {
        html += ' | Giá: ' + parseFloat(item.don_gia).toLocaleString('vi-VN') + '₫';
      }
      html += '</div>';
      html += '</div>';
      html += '<button type="button" class="btn-remove-item" data-id="' + item.id + '" style="padding:6px 10px;background:#dc3545;color:#fff;border:none;border-radius:4px;cursor:pointer;">';
      html += '<i class="fas fa-times"></i>';
      html += '</button>';
      html += '</div>';
    });

    cartContainer.innerHTML = html;
    
    // Re-attach remove event listeners
    attachRemoveListeners();
    
    // Update total and commit button
    if (totalAmountEl) {
      totalAmountEl.textContent = parseFloat(total).toLocaleString('vi-VN') + '₫';
    }
    if (btnCommit) {
      btnCommit.disabled = false;
    }
  }

  // Function to attach remove listeners
  function attachRemoveListeners() {
    var removeBtns = document.querySelectorAll('.btn-remove-item');
    removeBtns.forEach(function(btn) {
      // Remove existing listeners by cloning
      var newBtn = btn.cloneNode(true);
      btn.parentNode.replaceChild(newBtn, btn);
      
      newBtn.addEventListener('click', function() {
        var id = this.getAttribute('data-id');

        if (!confirm('Bạn có chắc chắn muốn xóa nguyên liệu này khỏi giỏ hàng?')) {
          return;
        }

        // Calculate URL from current page location
        var currentPath = window.location.pathname;
        var removeCartUrl = currentPath.replace(/create\.php$/, 'remove_cart.php');
        
        fetch(removeCartUrl, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            id: parseInt(id)
          })
        })
        .then(function(response) {
          if (!response.ok) {
            throw new Error('HTTP error! status: ' + response.status);
          }
          // Get response text first to check if it's valid JSON
          return response.text().then(function(text) {
            try {
              return JSON.parse(text);
            } catch (e) {
              console.error('Invalid JSON response:', text);
              throw new Error('Phản hồi từ server không hợp lệ');
            }
          });
        })
        .then(function(data) {
          if (data && data.success) {
            if (typeof showToast === 'function') {
              showToast('Đã xóa khỏi giỏ hàng', 'success');
            }
            updateCartFromServer();
          } else {
            var errorMsg = (data && data.error) ? data.error : 'Không thể xóa khỏi giỏ hàng';
            if (typeof showToast === 'function') {
              showToast('Lỗi: ' + errorMsg, 'error');
            } else {
              alert('Lỗi: ' + errorMsg);
            }
          }
        })
        .catch(function(error) {
          console.error('Error:', error);
          if (typeof showToast === 'function') {
            showToast('Có lỗi xảy ra khi xóa khỏi giỏ hàng', 'error');
          } else {
            alert('Có lỗi xảy ra khi xóa khỏi giỏ hàng');
          }
        });
      });
    });
  }

  // Remove item from cart (initial setup)
  attachRemoveListeners();

  // Calculate total
  function calculateTotal() {
    var items = document.querySelectorAll('.cart-item');
    var total = 0;
    items.forEach(function(item) {
      // Extract price and quantity from item (if available in data attributes)
      // For now, we'll calculate from the displayed text or reload
    });
    // Reload to get accurate total from server
  }

  // Commit cart
  if (btnCommit) {
    // Prevent duplicate event listeners
    if (btnCommit.dataset.listenerAttached === 'true') {
      return;
    }
    btnCommit.dataset.listenerAttached = 'true';
    
    btnCommit.addEventListener('click', function() {
      // Prevent double submission
      if (btnCommit.disabled) {
        return;
      }
      
      if (cartContainer.children.length === 0 || cartContainer.querySelector('.cart-item') === null) {
        if (typeof showToast === 'function') {
          showToast('Giỏ hàng trống. Vui lòng thêm nguyên liệu vào giỏ hàng.', 'warning');
        } else {
          alert('Giỏ hàng trống. Vui lòng thêm nguyên liệu vào giỏ hàng.');
        }
        return;
      }

      if (!confirm('Bạn có chắc chắn muốn tạo phiếu nhập kho? Phiếu sẽ ở trạng thái chờ duyệt.')) {
        return;
      }

      var nhaCungCapId = document.getElementById('nha_cung_cap_id').value || null;
      var ghiChu = document.getElementById('ghi_chu').value || null;

      btnCommit.disabled = true;
      btnCommit.textContent = 'Đang xử lý...';

      // Calculate URL from current page location
      // Current page is app/kho/create.php, so commit.php is in the same directory
      var currentPath = window.location.pathname;
      var commitUrl = currentPath.replace(/create\.php$/, 'commit.php');
      
      fetch(commitUrl, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          nha_cung_cap_id: nhaCungCapId ? parseInt(nhaCungCapId) : null,
          ghi_chu: ghiChu
        })
      })
      .then(function(response) {
        // Get response text first to check if it's valid JSON
        return response.text().then(function(text) {
          // Check if response is ok
          if (!response.ok) {
            // Try to parse error message from JSON
            try {
              var errorData = JSON.parse(text);
              throw new Error(errorData.error || 'HTTP error! status: ' + response.status);
            } catch (e) {
              if (e.message && e.message.includes('HTTP error')) {
                throw e;
              }
              throw new Error('HTTP error! status: ' + response.status + '. Response: ' + text.substring(0, 100));
            }
          }
          
          // Try to parse JSON
          try {
            return JSON.parse(text);
          } catch (e) {
            console.error('Invalid JSON response:', text);
            throw new Error('Phản hồi từ server không hợp lệ. Vui lòng kiểm tra console để xem chi tiết.');
          }
        });
      })
      .then(function(data) {
        if (data && data.success) {
          // Calculate redirect URL from current location
          // Current page is app/kho/create.php, so index.php is in the same directory
          var currentPath = window.location.pathname;
          var redirectUrl = currentPath.replace(/create\.php$/, 'index.php');
          
          // Show success message
          if (typeof showToast === 'function') {
            showToast('Đã tạo phiếu nhập kho thành công! Mã phiếu: ' + data.ma_phieu_nhap, 'success');
            // Wait a bit before redirect to show toast
            setTimeout(function() {
              window.location.href = redirectUrl;
            }, 1000);
          } else {
            alert('Đã tạo phiếu nhập kho thành công!\nMã phiếu: ' + data.ma_phieu_nhap);
            window.location.href = redirectUrl;
          }
        } else {
          var errorMsg = (data && data.error) ? data.error : 'Không thể tạo phiếu nhập kho';
          if (typeof showToast === 'function') {
            showToast('Lỗi: ' + errorMsg, 'error');
          } else {
            alert('Lỗi: ' + errorMsg);
          }
          btnCommit.disabled = false;
          btnCommit.innerHTML = '<i class="fas fa-check"></i> Tạo phiếu nhập kho';
        }
      })
      .catch(function(error) {
        console.error('Error:', error);
        console.error('Error message:', error.message);
        console.error('Error stack:', error.stack);
        
        // Show error message
        var errorMsg = error.message || 'Có lỗi xảy ra khi tạo phiếu nhập kho';
        if (typeof showToast === 'function') {
          showToast(errorMsg, 'error');
        } else {
          alert(errorMsg);
        }
        btnCommit.disabled = false;
        btnCommit.innerHTML = '<i class="fas fa-check"></i> Tạo phiếu nhập kho';
      });
    });
  }

  // Calculate total on page load
  function updateTotal() {
    if (!totalAmountEl) return;
    
    var items = document.querySelectorAll('.cart-item');
    var total = 0;
    
    items.forEach(function(item) {
      // Try to extract price and quantity from the displayed text
      var text = item.textContent || '';
      // Look for pattern like "Giá: 100,000₫" and "SL: 10.00"
      var priceMatch = text.match(/Giá:\s*([\d.,]+)/);
      var qtyMatch = text.match(/SL:\s*([\d.,]+)/);
      
      if (priceMatch && qtyMatch) {
        var price = parseFloat(priceMatch[1].replace(/\./g, '').replace(',', '.'));
        var qty = parseFloat(qtyMatch[1].replace(/\./g, '').replace(',', '.'));
        if (!isNaN(price) && !isNaN(qty)) {
          total += price * qty;
        }
      }
    });
    
    if (total > 0) {
      totalAmountEl.textContent = parseFloat(total).toLocaleString('vi-VN') + '₫';
    } else if (items.length > 0) {
      totalAmountEl.textContent = items.length + ' mặt hàng';
    } else {
      totalAmountEl.textContent = '0₫';
    }
  }

  updateTotal();
})();

