// ============================================
// ORDER CREATE - Xử lý tạo đơn hàng với nhóm lựa chọn
// ============================================

(function() {
  'use strict';
  
  let orders = []; // Mảng các đơn hàng (mỗi đơn là một tab)
  let activeOrderId = null; // ID của đơn đang active
  let currentProductId = null;
  let currentProductPrice = null;
  let productChoices = {}; // Lưu các nhóm lựa chọn của từng sản phẩm
  let productSizes = {}; // Lưu sizes của từng sản phẩm
  let matchedCustomer = null;
  let customerSearchTimer = null;

  function apiBase() {
    const base = window.APP_BASE_PATH || window.BASE_PATH || '/';
    return base.endsWith('/') ? base : base + '/';
  }

  function orderCreateUrl() {
    return apiBase() + 'don-hang/create';
  }

  function orderListUrl() {
    return apiBase() + 'don-hang';
  }
  
  // Helper: Lấy đơn đang active
  function getActiveOrder() {
    return orders.find(o => o.id === activeOrderId);
  }
  
  // Helper: Lấy giỏ hàng của đơn active
  function getActiveCart() {
    const order = getActiveOrder();
    return order ? order.cart : [];
  }
  
  // Helper: Set giỏ hàng của đơn active
  function setActiveCart(cart) {
    const order = getActiveOrder();
    if (order) {
      order.cart = cart;
      saveOrdersToStorage();
    }
  }
  
  // Lưu orders vào localStorage
  function saveOrdersToStorage() {
    try {
      localStorage.setItem('order_tabs', JSON.stringify({
        orders: orders,
        activeOrderId: activeOrderId
      }));
    } catch (e) {
      console.error('Error saving orders to localStorage:', e);
    }
  }
  
  // Load orders từ localStorage
  function loadOrdersFromStorage() {
    try {
      const saved = localStorage.getItem('order_tabs');
      if (saved) {
        const data = JSON.parse(saved);
        orders = data.orders || [];
        activeOrderId = data.activeOrderId || null;
        return true;
      }
    } catch (e) {
      console.error('Error loading orders from localStorage:', e);
    }
    return false;
  }
  
  // Xóa orders khỏi localStorage
  function clearOrdersStorage() {
    try {
      localStorage.removeItem('order_tabs');
    } catch (e) {
      console.error('Error clearing orders from localStorage:', e);
    }
  }
  
  // Tạo đơn mới
  function createNewOrder() {
    const orderId = Date.now();
    const order = {
      id: orderId,
      title: `Đơn hàng ${orders.length + 1}`,
      cart: [],
      ten_khachhang: '',
      sdt_khachhang: '',
      phuong_thuc_thanhtoan: window.DEFAULT_PAYMENT_METHOD || 'tien_mat',
      created_at: new Date().toISOString()
    };
    
    orders.push(order);
    activeOrderId = orderId;
    saveOrdersToStorage();
    updateTabs();
    updateCart();
    
    // Scroll đến tab mới
    setTimeout(() => {
      const tabsContainer = document.getElementById('orderTabs');
      if (tabsContainer) {
        const activeTab = tabsContainer.querySelector('.order-tab.active');
        if (activeTab) {
          activeTab.scrollIntoView({ behavior: 'smooth', block: 'nearest', inline: 'center' });
        }
      }
    }, 100);
  }
  
  // Load nhóm lựa chọn của sản phẩm từ server
  async function loadProductChoices(productId) {
    try {
      const url = `${apiBase()}api/product-choices?id=${productId}`;
      const response = await fetch(url);
      
      // Always get text first to check content
      const text = await response.text();
      
      // Check if response looks like HTML (PHP error)
      if (text.trim().startsWith('<') || text.includes('<br') || text.includes('<b>')) {
        console.error('API returned HTML instead of JSON. Response:', text.substring(0, 500));
        return [];
      }
      
      // Check Content-Type header
      const contentType = response.headers.get('content-type');
      if (contentType && !contentType.includes('application/json')) {
        console.warn('Unexpected Content-Type:', contentType);
      }
      
      // Try to parse JSON
      let data;
      try {
        data = JSON.parse(text);
      } catch (e) {
        console.error('Failed to parse JSON. Response was:', text.substring(0, 500));
        return [];
      }
      
      if (data.success) {
        productChoices[productId] = data.choices || [];
        return data.choices || [];
      }
      console.warn('API returned success=false for product', productId, data);
      return [];
    } catch (error) {
      console.error('Error loading product choices:', error);
      return [];
    }
  }
  
  // Load sizes của sản phẩm từ server
  async function loadProductSizes(productId) {
    try {
      const url = `${apiBase()}api/product-sizes?product_id=${productId}`;
      const response = await fetch(url);
      
      // Always get text first to check content
      const text = await response.text();
      
      // Check if response looks like HTML (PHP error)
      if (text.trim().startsWith('<') || text.includes('<br') || text.includes('<b>')) {
        console.error('API returned HTML instead of JSON. Response:', text.substring(0, 500));
        productSizes[productId] = [];
        return [];
      }
      
      // Check Content-Type header
      const contentType = response.headers.get('content-type');
      if (contentType && !contentType.includes('application/json')) {
        console.warn('Unexpected Content-Type:', contentType);
      }
      
      // Try to parse JSON
      let data;
      try {
        data = JSON.parse(text);
      } catch (e) {
        console.error('Failed to parse JSON. Response was:', text.substring(0, 500));
        productSizes[productId] = [];
        return [];
      }
      
      if (data.success && data.has_sizes) {
        productSizes[productId] = data.sizes || [];
        return data.sizes || [];
      }
      productSizes[productId] = [];
      return [];
    } catch (error) {
      console.error('Error loading product sizes:', error);
      productSizes[productId] = [];
      return [];
    }
  }
  
  const RANK_LABELS = {
    thuong: 'Thường',
    bac: 'Bạc',
    vang: 'Vàng',
    vip: 'VIP'
  };

  async function searchCustomers(query) {
    if (!query || query.trim().length < 2) {
      return [];
    }
    const url = `${apiBase()}api/search-customer?q=${encodeURIComponent(query.trim())}`;
    try {
      const response = await fetch(url);
      const text = await response.text();
      if (text.trim().startsWith('<')) return [];
      const data = JSON.parse(text);
      return data.success ? (data.customers || []) : [];
    } catch (e) {
      console.error('Customer search error:', e);
      return [];
    }
  }

  function getRankLabel(rank) {
    return RANK_LABELS[rank] || 'Thường';
  }

  function selectCustomer(customer) {
    matchedCustomer = customer;
    const order = getActiveOrder();
    const tenInput = document.getElementById('tenKhachhang');
    const sdtInput = document.getElementById('sdtKhachhang');

    if (tenInput) tenInput.value = customer.ten_khachhang || '';
    if (sdtInput) sdtInput.value = customer.so_dien_thoai || '';

    if (order) {
      order.ten_khachhang = customer.ten_khachhang || '';
      order.sdt_khachhang = customer.so_dien_thoai || '';
      saveOrdersToStorage();
      updateTabs();
    }

    updateMatchedBadge(customer);
    hideCustomerSuggestions();
    closeCustomerFindModal();
  }

  function clearMatchedCustomer() {
    matchedCustomer = null;
    const badge = document.getElementById('customerMatchedBadge');
    if (badge) badge.style.display = 'none';
  }

  function updateMatchedBadge(customer) {
    const badge = document.getElementById('customerMatchedBadge');
    const countEl = document.getElementById('customerOrderCount');
    if (!badge || !countEl) return;

    if (customer && (customer.tong_donhang > 0 || customer.tong_tien_da_chi > 0)) {
      const count = parseInt(customer.tong_donhang, 10) || 0;
      countEl.textContent = count + ' đơn trước';
      badge.style.display = 'flex';
    } else {
      badge.style.display = 'none';
    }
  }

  function renderSuggestionItem(customer) {
    const rank = getRankLabel(customer.loai_thanh_vien);
    return `
      <div class="autocomplete-suggestion" data-customer-id="${customer.id}">
        <div class="suggestion-left">
          <div class="suggestion-name">${escapeHtml(customer.ten_khachhang || '')}</div>
          <div class="suggestion-phone">${escapeHtml(customer.so_dien_thoai || 'Không có SĐT')}</div>
        </div>
        <div class="suggestion-right">
          <span class="suggestion-tier rank-${customer.loai_thanh_vien || 'thuong'}">${rank}</span>
        </div>
      </div>
    `;
  }

  function showCustomerSuggestions(customers) {
    const box = document.getElementById('customerSuggestions');
    if (!box) return;

    if (!customers.length) {
      box.style.display = 'none';
      box.innerHTML = '';
      return;
    }

    box.innerHTML = customers.map(renderSuggestionItem).join('');
    box.style.display = 'block';

    box.querySelectorAll('.autocomplete-suggestion').forEach((el, index) => {
      el.addEventListener('click', function() {
        selectCustomer(customers[index]);
      });
    });
  }

  function hideCustomerSuggestions() {
    const box = document.getElementById('customerSuggestions');
    if (box) {
      box.style.display = 'none';
      box.innerHTML = '';
    }
  }

  async function handleCustomerInputChange() {
    const tenInput = document.getElementById('tenKhachhang');
    const sdtInput = document.getElementById('sdtKhachhang');
    const query = ((sdtInput && sdtInput.value) || (tenInput && tenInput.value) || '').trim();

    if (query.length < 2) {
      hideCustomerSuggestions();
      if (!query) clearMatchedCustomer();
      return;
    }

    const customers = await searchCustomers(query);
    if (!customers.length) {
      hideCustomerSuggestions();
      clearMatchedCustomer();
      return;
    }

    const exactMatch = customers.find(c =>
      (sdtInput && sdtInput.value && c.so_dien_thoai === sdtInput.value.trim()) ||
      (tenInput && tenInput.value && c.ten_khachhang === tenInput.value.trim())
    );

    if (exactMatch) {
      matchedCustomer = exactMatch;
      updateMatchedBadge(exactMatch);
    } else {
      clearMatchedCustomer();
    }

    showCustomerSuggestions(customers);
  }

  function openCustomerFindModal() {
    const modal = document.getElementById('customerFindModal');
    const input = document.getElementById('customerFindInput');
    if (modal) modal.classList.add('active');
    if (input) {
      input.value = '';
      input.focus();
      renderCustomerFindResults([]);
    }
  }

  function closeCustomerFindModal() {
    const modal = document.getElementById('customerFindModal');
    if (modal) modal.classList.remove('active');
  }

  function renderCustomerFindResults(customers) {
    const box = document.getElementById('customerFindResults');
    if (!box) return;

    if (!customers.length) {
      box.innerHTML = '<div class="customer-find-empty">Nhập tên hoặc SĐT để tìm khách</div>';
      return;
    }

    box.innerHTML = customers.map((customer, index) => `
      <div class="customer-find-item" data-index="${index}">
        <div>
          <div class="customer-find-item-name">${escapeHtml(customer.ten_khachhang || '')}</div>
          <div class="customer-find-item-phone">${escapeHtml(customer.so_dien_thoai || 'Không có SĐT')} · ${parseInt(customer.tong_donhang, 10) || 0} đơn</div>
        </div>
        <span class="suggestion-tier rank-${customer.loai_thanh_vien || 'thuong'}">${getRankLabel(customer.loai_thanh_vien)}</span>
      </div>
    `).join('');

    box.querySelectorAll('.customer-find-item').forEach(el => {
      el.addEventListener('click', function() {
        const idx = parseInt(this.dataset.index, 10);
        if (!isNaN(idx) && customers[idx]) {
          selectCustomer(customers[idx]);
        }
      });
    });
  }

  function initCustomerSearch() {
    const tenInput = document.getElementById('tenKhachhang');
    const sdtInput = document.getElementById('sdtKhachhang');
    const btnFind = document.getElementById('btnFindCustomer');
    const btnClose = document.getElementById('btnCloseFindCustomer');
    const findInput = document.getElementById('customerFindInput');
    const findModal = document.getElementById('customerFindModal');
    const wrapper = document.querySelector('.customer-search-wrapper');

    function scheduleSearch() {
      clearTimeout(customerSearchTimer);
      customerSearchTimer = setTimeout(handleCustomerInputChange, 300);
    }

    if (tenInput) {
      tenInput.addEventListener('input', function() {
        const order = getActiveOrder();
        if (order) {
          order.ten_khachhang = this.value;
          saveOrdersToStorage();
          updateTabs();
        }
        scheduleSearch();
      });
    }

    if (sdtInput) {
      sdtInput.addEventListener('input', function() {
        const order = getActiveOrder();
        if (order) {
          order.sdt_khachhang = this.value;
          saveOrdersToStorage();
        }
        scheduleSearch();
      });
    }

    if (btnFind) {
      btnFind.addEventListener('click', openCustomerFindModal);
    }

    if (btnClose) {
      btnClose.addEventListener('click', closeCustomerFindModal);
    }

    if (findModal) {
      findModal.addEventListener('click', function(e) {
        if (e.target === findModal) closeCustomerFindModal();
      });
    }

    if (findInput) {
      let findTimer = null;
      findInput.addEventListener('input', function() {
        clearTimeout(findTimer);
        const query = this.value.trim();
        findTimer = setTimeout(async function() {
          if (query.length < 2) {
            renderCustomerFindResults([]);
            return;
          }
          const customers = await searchCustomers(query);
          renderCustomerFindResults(customers);
        }, 300);
      });
    }

    document.addEventListener('click', function(e) {
      if (wrapper && !wrapper.contains(e.target)) {
        hideCustomerSuggestions();
      }
    });
  }

  // Khởi tạo
  function init() {
    // Load orders từ localStorage
    if (!loadOrdersFromStorage() || orders.length === 0) {
      // Tạo đơn đầu tiên nếu chưa có
      createNewOrder();
    } else {
      updateTabs();
      updateCart();
    }
    
    // Không load trước - sẽ load khi click vào sản phẩm
    // document.querySelectorAll('.prod-card').forEach(card => {
    //   const productId = card.dataset.id;
    //   loadProductChoices(productId);
    // });
    
    // Click vào sản phẩm
    document.querySelectorAll('.prod-card').forEach(card => {
      card.addEventListener('click', async function() {
        const id = this.dataset.id;
        const name = this.dataset.name;
        const price = parseFloat(this.dataset.price);
        
        currentProductId = id;
        currentProductPrice = price;
        
        // Load nhóm lựa chọn và sizes song song
        const [choices, sizes] = await Promise.all([
          productChoices[id] ? Promise.resolve(productChoices[id]) : loadProductChoices(id),
          productSizes[id] ? Promise.resolve(productSizes[id]) : loadProductSizes(id)
        ]);
        
        showOptionsModal(id, name, price, choices, sizes);
      });
    });
    
    // Search sản phẩm
    const searchInput = document.getElementById('searchProd');
    if (searchInput) {
      searchInput.addEventListener('input', function() {
        const keyword = this.value.toLowerCase();
        document.querySelectorAll('.prod-card').forEach(card => {
          const name = card.dataset.name.toLowerCase();
          card.style.display = name.includes(keyword) ? '' : 'none';
        });
      });
    }
    
    // Nút thanh toán
    const btnThanhToan = document.getElementById('btnThanhToan');
    if (btnThanhToan) {
      btnThanhToan.addEventListener('click', function(e) {
        e.preventDefault();
        showPaymentModal();
      });
    }

    // Nút lưu đơn chờ xử lý
    const btnLuuDon = document.getElementById('btnLuuDon');
    if (btnLuuDon) {
      btnLuuDon.addEventListener('click', function(e) {
        e.preventDefault();
        savePendingOrder();
      });
    }
    
    // Nút thêm đơn hàng
    const btnAddNewOrder = document.getElementById('btnAddNewOrder');
    if (btnAddNewOrder) {
      btnAddNewOrder.addEventListener('click', function(e) {
        e.preventDefault();
        createNewOrder();
      });
    }
    
    // Đóng modal khi click ra ngoài
    const paymentModal = document.getElementById('paymentModal');
    if (paymentModal) {
      paymentModal.addEventListener('click', function(e) {
        if (e.target === paymentModal) {
          closePaymentModal();
        }
      });
    }
    
    initCustomerSearch();
  }
  
  // Helper: Escape HTML
  function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
  }
  
  // Hiển thị modal chọn lựa chọn
  function showOptionsModal(productId, productName, productPrice, choices, sizes = []) {
    const modal = document.getElementById('optionsModal');
    if (!modal) return;
    
    const optionsCard = modal.querySelector('.options-card');
    const safeProductName = productName.replace(/'/g, "\\'").replace(/"/g, '&quot;');
    optionsCard.innerHTML = `
      <h3>Tùy chọn: ${escapeHtml(productName)}</h3>
      <div id="choicesContainer"></div>
      <div style="text-align:right;margin-top:15px;flex-shrink:0;">
        <button class="btn btn-secondary" onclick="orderCreate.closeOptions()">Đóng</button>
        <button class="btn btn-primary" onclick="orderCreate.applyOptions(${productId}, '${safeProductName}', ${productPrice})">Áp dụng</button>
      </div>
    `;
    
    const container = document.getElementById('choicesContainer');
    
    if (!choices || choices.length === 0) {
      container.innerHTML = '<p style="color:#999;padding:20px;text-align:center">Sản phẩm này không có tùy chọn</p>';
    } else {
      choices.forEach(group => {
        const groupDiv = document.createElement('div');
        groupDiv.className = 'choice-group';
        groupDiv.style.marginBottom = '15px';
        
        const label = document.createElement('label');
        label.style.display = 'block';
        label.style.marginBottom = '5px';
        label.style.fontWeight = '600';
        label.textContent = group.ten_nhom + (group.bat_buoc ? ' *' : '');
        
        // Kiểm tra nếu là nhóm "Topping" hoặc chứa "topping" → dùng checkbox (cho phép chọn nhiều)
        const isTopping = group.ten_nhom.toLowerCase().includes('topping');
        
        if (isTopping) {
          // Hiển thị input số lượng cho topping (cho phép chọn nhiều lần)
          const toppingContainer = document.createElement('div');
          toppingContainer.className = 'choice-toppings';
          toppingContainer.dataset.nhomId = group.id;
          toppingContainer.dataset.nhomTen = group.ten_nhom;
          toppingContainer.style.display = 'flex';
          toppingContainer.style.flexDirection = 'column';
          toppingContainer.style.gap = '10px';
          
          group.lua_chon.forEach(choice => {
            const toppingItem = document.createElement('div');
            toppingItem.style.display = 'flex';
            toppingItem.style.alignItems = 'center';
            toppingItem.style.justifyContent = 'space-between';
            toppingItem.style.padding = '10px';
            toppingItem.style.border = '1px solid #e5e7eb';
            toppingItem.style.borderRadius = '8px';
            toppingItem.style.background = '#fff';
            toppingItem.style.transition = 'all 0.2s';
            
            toppingItem.addEventListener('mouseenter', function() {
              this.style.borderColor = '#2196f3';
              this.style.boxShadow = '0 2px 4px rgba(33, 150, 243, 0.1)';
            });
            toppingItem.addEventListener('mouseleave', function() {
              this.style.borderColor = '#e5e7eb';
              this.style.boxShadow = 'none';
            });
            
            // Tên topping và giá
            const toppingInfo = document.createElement('div');
            toppingInfo.style.flex = '1';
            toppingInfo.style.minWidth = '0';
            
            const toppingName = document.createElement('div');
            toppingName.style.fontWeight = '600';
            toppingName.style.color = '#22334b';
            toppingName.style.marginBottom = '2px';
            toppingName.textContent = choice.ten_lua_chon;
            
            const toppingPrice = document.createElement('div');
            toppingPrice.style.fontSize = '12px';
            toppingPrice.style.color = '#64748b';
            toppingPrice.textContent = choice.gia_tang_them > 0 ? `+${formatMoney(choice.gia_tang_them)}/phần` : 'Miễn phí';
            
            toppingInfo.appendChild(toppingName);
            toppingInfo.appendChild(toppingPrice);
            
            // Controls số lượng
            const qtyControls = document.createElement('div');
            qtyControls.style.display = 'flex';
            qtyControls.style.alignItems = 'center';
            qtyControls.style.gap = '8px';
            
            const btnMinus = document.createElement('button');
            btnMinus.type = 'button';
            btnMinus.className = 'qty-btn qty-minus';
            btnMinus.textContent = '−';
            btnMinus.style.width = '32px';
            btnMinus.style.height = '32px';
            btnMinus.style.border = '1px solid #d2d8ea';
            btnMinus.style.borderRadius = '6px';
            btnMinus.style.background = '#fff';
            btnMinus.style.cursor = 'pointer';
            btnMinus.style.fontSize = '18px';
            btnMinus.style.fontWeight = '600';
            btnMinus.style.color = '#64748b';
            btnMinus.style.display = 'flex';
            btnMinus.style.alignItems = 'center';
            btnMinus.style.justifyContent = 'center';
            btnMinus.style.transition = 'all 0.2s';
            
            btnMinus.addEventListener('mouseenter', function() {
              this.style.background = '#f44336';
              this.style.borderColor = '#f44336';
              this.style.color = '#fff';
            });
            btnMinus.addEventListener('mouseleave', function() {
              this.style.background = '#fff';
              this.style.borderColor = '#d2d8ea';
              this.style.color = '#64748b';
            });
            
            const qtyInput = document.createElement('input');
            qtyInput.type = 'number';
            qtyInput.className = 'topping-qty-input';
            qtyInput.value = '0';
            qtyInput.min = '0';
            qtyInput.max = '99';
            qtyInput.dataset.choiceId = choice.id;
            qtyInput.dataset.nhomId = group.id;
            qtyInput.dataset.nhomTen = group.ten_nhom;
            qtyInput.dataset.giaTang = choice.gia_tang_them;
            qtyInput.dataset.tenLuaChon = choice.ten_lua_chon;
            qtyInput.style.width = '50px';
            qtyInput.style.height = '32px';
            qtyInput.style.textAlign = 'center';
            qtyInput.style.border = '1px solid #d2d8ea';
            qtyInput.style.borderRadius = '6px';
            qtyInput.style.fontSize = '14px';
            qtyInput.style.fontWeight = '600';
            
            const btnPlus = document.createElement('button');
            btnPlus.type = 'button';
            btnPlus.className = 'qty-btn qty-plus';
            btnPlus.textContent = '+';
            btnPlus.style.width = '32px';
            btnPlus.style.height = '32px';
            btnPlus.style.border = '1px solid #d2d8ea';
            btnPlus.style.borderRadius = '6px';
            btnPlus.style.background = '#fff';
            btnPlus.style.cursor = 'pointer';
            btnPlus.style.fontSize = '18px';
            btnPlus.style.fontWeight = '600';
            btnPlus.style.color = '#64748b';
            btnPlus.style.display = 'flex';
            btnPlus.style.alignItems = 'center';
            btnPlus.style.justifyContent = 'center';
            btnPlus.style.transition = 'all 0.2s';
            
            btnPlus.addEventListener('mouseenter', function() {
              this.style.background = '#4caf50';
              this.style.borderColor = '#4caf50';
              this.style.color = '#fff';
            });
            btnPlus.addEventListener('mouseleave', function() {
              this.style.background = '#fff';
              this.style.borderColor = '#d2d8ea';
              this.style.color = '#64748b';
            });
            
            // Event handlers
            btnMinus.addEventListener('click', function() {
              const currentVal = parseInt(qtyInput.value) || 0;
              if (currentVal > 0) {
                qtyInput.value = currentVal - 1;
                qtyInput.dispatchEvent(new Event('change'));
              }
            });
            
            btnPlus.addEventListener('click', function() {
              const currentVal = parseInt(qtyInput.value) || 0;
              if (currentVal < 99) {
                qtyInput.value = currentVal + 1;
                qtyInput.dispatchEvent(new Event('change'));
              }
            });
            
            qtyInput.addEventListener('change', function() {
              const val = parseInt(this.value) || 0;
              if (val < 0) this.value = 0;
              if (val > 99) this.value = 99;
            });
            
            qtyControls.appendChild(btnMinus);
            qtyControls.appendChild(qtyInput);
            qtyControls.appendChild(btnPlus);
            
            toppingItem.appendChild(toppingInfo);
            toppingItem.appendChild(qtyControls);
            toppingContainer.appendChild(toppingItem);
          });
          
          groupDiv.appendChild(label);
          groupDiv.appendChild(toppingContainer);
        } else {
          // Hiển thị select cho các nhóm khác (chỉ chọn 1)
          const select = document.createElement('select');
          select.className = 'choice-select';
          select.dataset.nhomId = group.id;
          select.dataset.nhomTen = group.ten_nhom;
          select.style.width = '100%';
          select.style.padding = '8px';
          select.style.border = '1px solid #d2d8ea';
          select.style.borderRadius = '8px';
          
          // Option mặc định
          const defaultOption = document.createElement('option');
          defaultOption.value = '';
          defaultOption.textContent = `-- Chọn ${group.ten_nhom} --`;
          if (group.bat_buoc) {
            defaultOption.disabled = true;
            defaultOption.selected = true;
          }
          select.appendChild(defaultOption);
          
          // Các lựa chọn
          group.lua_chon.forEach(choice => {
            const option = document.createElement('option');
            option.value = choice.id;
            option.dataset.giaTang = choice.gia_tang_them;
            option.dataset.tenLuaChon = choice.ten_lua_chon;
            option.textContent = choice.ten_lua_chon + (choice.gia_tang_them > 0 ? ` (+${formatMoney(choice.gia_tang_them)})` : '');
            select.appendChild(option);
          });
          
          groupDiv.appendChild(label);
          groupDiv.appendChild(select);
        }
        
        container.appendChild(groupDiv);
      });
    }
    
    // Thêm size select nếu sản phẩm có size
    if (sizes && sizes.length > 0) {
      const sizeDiv = document.createElement('div');
      sizeDiv.className = 'choice-group';
      sizeDiv.style.marginBottom = '15px';
      sizeDiv.style.marginTop = '15px';
      sizeDiv.style.paddingTop = '15px';
      sizeDiv.style.borderTop = '1px solid #e0e0e0';
      
      const sizeLabel = document.createElement('label');
      sizeLabel.style.display = 'block';
      sizeLabel.style.marginBottom = '5px';
      sizeLabel.style.fontWeight = '600';
      sizeLabel.textContent = 'Kích thước';
      
      const sizeSelect = document.createElement('select');
      sizeSelect.id = 'productSizeSelect';
      sizeSelect.className = 'product-size-select';
      sizeSelect.style.width = '100%';
      sizeSelect.style.padding = '8px';
      sizeSelect.style.border = '1px solid #d2d8ea';
      sizeSelect.style.borderRadius = '8px';
      
      // Option mặc định
      const defaultSizeOption = document.createElement('option');
      defaultSizeOption.value = '';
      defaultSizeOption.textContent = '-- Chọn size --';
      sizeSelect.appendChild(defaultSizeOption);
      
      // Các size
      sizes.forEach(size => {
        const option = document.createElement('option');
        option.value = size.id;
        option.dataset.sizeName = size.ten_size;
        option.dataset.sizePrice = size.gia;
        option.textContent = size.ten_size + (size.gia > 0 ? ` (+${formatMoney(size.gia)})` : '');
        sizeSelect.appendChild(option);
      });
      
      sizeDiv.appendChild(sizeLabel);
      sizeDiv.appendChild(sizeSelect);
      container.appendChild(sizeDiv);
    }
    
    modal.style.display = 'flex';
    // Đảm bảo modal có thể scroll
    modal.style.overflowY = 'auto';
    modal.style.alignItems = 'flex-start';
    modal.style.paddingTop = '20px';
    modal.style.paddingBottom = '20px';
  }
  
  // Đóng modal
  function closeOptions() {
    const modal = document.getElementById('optionsModal');
    if (modal) {
      modal.style.display = 'none';
    }
  }
  
  // Áp dụng lựa chọn và thêm vào giỏ
  function applyOptions(productId, productName, productPrice) {
    const container = document.getElementById('choicesContainer');
    if (!container) return;
    
    const selectedChoices = [];
    let totalExtraPrice = 0;
    let selectedSizeId = null;
    let selectedSizeName = '';
    let sizePrice = 0;
    
    // Lấy size được chọn
    const sizeSelect = document.getElementById('productSizeSelect');
    if (sizeSelect && sizeSelect.value) {
      const selectedOption = sizeSelect.options[sizeSelect.selectedIndex];
      selectedSizeId = parseInt(sizeSelect.value);
      selectedSizeName = selectedOption.dataset.sizeName || '';
      sizePrice = parseFloat(selectedOption.dataset.sizePrice || 0);
      totalExtraPrice += sizePrice;
    }
    
    // Lấy các lựa chọn từ select (đường, đá, size, etc.)
    container.querySelectorAll('.choice-select').forEach(select => {
      const choiceId = select.value;
      if (choiceId) {
        const option = select.options[select.selectedIndex];
        const extraPrice = parseFloat(option.dataset.giaTang || 0);
        totalExtraPrice += extraPrice;
        
        selectedChoices.push({
          nhom_id: parseInt(select.dataset.nhomId),
          nhom_ten: select.dataset.nhomTen,
          lua_chon_id: parseInt(choiceId),
          lua_chon_ten: option.dataset.tenLuaChon,
          gia_tang_them: extraPrice
        });
      }
    });
    
    // Lấy các lựa chọn từ input số lượng (topping - cho phép chọn nhiều lần)
    container.querySelectorAll('.topping-qty-input').forEach(input => {
      const quantity = parseInt(input.value) || 0;
      if (quantity > 0) {
        const extraPrice = parseFloat(input.dataset.giaTang || 0);
        const totalPriceForThisTopping = extraPrice * quantity;
        totalExtraPrice += totalPriceForThisTopping;
        
        // Thêm vào selectedChoices với số lượng
        for (let i = 0; i < quantity; i++) {
      selectedChoices.push({
            nhom_id: parseInt(input.dataset.nhomId),
            nhom_ten: input.dataset.nhomTen,
            lua_chon_id: parseInt(input.dataset.choiceId),
            lua_chon_ten: input.dataset.tenLuaChon,
            gia_tang_them: extraPrice,
            so_luong: quantity  // Lưu số lượng để hiển thị
          });
        }
      }
    });
    
    // Kiểm tra bắt buộc (chỉ kiểm tra các nhóm không phải topping)
    const choices = productChoices[productId] || [];
    const requiredGroups = choices.filter(g => g.bat_buoc && !g.ten_nhom.toLowerCase().includes('topping'));
    const selectedGroupIds = selectedChoices.map(c => c.nhom_id);
    const missingRequired = requiredGroups.filter(g => !selectedGroupIds.includes(g.id));
    
    if (missingRequired.length > 0) {
      alert('Vui lòng chọn: ' + missingRequired.map(g => g.ten_nhom).join(', '));
      return;
    }
    
    // Tính giá cuối cùng
    const finalPrice = productPrice + totalExtraPrice;
    
    // Lấy giỏ hàng của đơn active
    const cart = getActiveCart();
    
    // Tạo key duy nhất cho item dựa trên productId và choices
    const choicesKey = JSON.stringify(selectedChoices.sort((a, b) => a.nhom_id - b.nhom_id));
    
    // Kiểm tra xem đã có item giống hệt trong giỏ chưa (cùng sản phẩm + cùng lựa chọn)
    const existingIndex = cart.findIndex(item => {
      if (item.id !== productId) return false;
      const itemChoicesKey = JSON.stringify((item.choices || []).sort((a, b) => a.nhom_id - b.nhom_id));
      return itemChoicesKey === choicesKey;
    });
    
    if (existingIndex >= 0) {
      // Nếu đã có, tăng số lượng
      cart[existingIndex].sl += 1;
    } else {
      // Nếu chưa có, thêm mới
      const cartItem = {
        id: parseInt(productId, 10),
        ten: productName,
        gia: finalPrice,
        sl: 1,
        size: selectedSizeName || '',
        size_id: selectedSizeId,
        choices: selectedChoices
      };
      cart.push(cartItem);
    }
    
    setActiveCart(cart);
    updateCart();
    updateTabs();
    closeOptions();
  }
  
  // Cập nhật tabs
  function updateTabs() {
    const tabsContainer = document.getElementById('orderTabs');
    if (!tabsContainer) return;
    
    if (orders.length === 0) {
      tabsContainer.innerHTML = '';
      return;
    }
    
    let html = '';
    orders.forEach((order, index) => {
      const isActive = order.id === activeOrderId;
      const itemCount = order.cart.reduce((sum, item) => sum + item.sl, 0);
      const total = order.cart.reduce((sum, item) => sum + (item.gia * item.sl), 0);
      
      // Hiển thị tên khách nếu có, nếu không thì hiển thị "Đơn hàng X"
      let title = order.ten_khachhang && order.ten_khachhang.trim() && order.ten_khachhang !== 'Khách lẻ' 
        ? order.ten_khachhang 
        : `Đơn hàng ${index + 1}`;
      
      // Giới hạn độ dài tên
      if (title.length > 18) {
        title = title.substring(0, 15) + '...';
      }
      
      html += `
        <div class="order-tab ${isActive ? 'active' : ''}" onclick="orderCreate.switchOrder(${order.id})">
          <div class="order-tab-header">
            <span class="order-tab-title">${title}</span>
            ${orders.length > 1 ? `<span class="order-tab-close" onclick="event.stopPropagation(); orderCreate.closeOrder(${order.id})" title="Đóng đơn hàng">×</span>` : ''}
          </div>
          <div class="order-tab-info">
            ${order.sdt_khachhang ? `<div class="order-tab-customer"><i class="fas fa-phone"></i> ${order.sdt_khachhang}</div>` : ''}
            ${itemCount > 0 ? `<span class="order-tab-badge">${itemCount} món</span>` : '<span style="font-size:11px;color:#94a3b8">Chưa có sản phẩm</span>'}
            ${total > 0 ? `<div class="order-tab-total">${formatMoney(total)}</div>` : ''}
          </div>
        </div>
      `;
    });
    
    tabsContainer.innerHTML = html;
  }
  
  // Chuyển đổi đơn hàng
  function switchOrder(orderId) {
    if (activeOrderId === orderId) return;
    
    activeOrderId = orderId;
    saveOrdersToStorage();
    updateTabs();
    updateCart();
  }
  
  // Đóng đơn hàng
  function closeOrder(orderId) {
    if (orders.length <= 1) {
      alert('Phải có ít nhất một đơn hàng');
      return;
    }
    
    if (confirm('Bạn có chắc chắn muốn đóng đơn hàng này?')) {
      const index = orders.findIndex(o => o.id === orderId);
      if (index >= 0) {
        orders.splice(index, 1);
        
        // Nếu đóng đơn đang active, chuyển sang đơn khác
        if (activeOrderId === orderId) {
          activeOrderId = orders.length > 0 ? orders[0].id : null;
        }
        
        saveOrdersToStorage();
        updateTabs();
        updateCart();
      }
    }
  }
  
  // Cập nhật giỏ hàng
  function updateCart() {
    const cartEl = document.getElementById('cart');
    const totalEl = document.getElementById('total');
    const itemsInput = document.getElementById('itemsInput');
    const tenKhachInput = document.getElementById('tenKhachhang');
    const sdtKhachInput = document.getElementById('sdtKhachhang');
    
    if (!cartEl) return;
    
    const cart = getActiveCart();
    const order = getActiveOrder();
    
    // Cập nhật input khách hàng
    if (tenKhachInput && order) {
      tenKhachInput.value = order.ten_khachhang || '';
    }
    if (sdtKhachInput && order) {
      sdtKhachInput.value = order.sdt_khachhang || '';
    }

    matchedCustomer = null;
    clearMatchedCustomer();
    hideCustomerSuggestions();
    
    if (!cart || cart.length === 0) {
      cartEl.innerHTML = '<div class="empty-state" style="padding:40px 20px;text-align:center;color:#94a3b8"><i class="fas fa-shopping-cart" style="font-size:32px;opacity:0.3;display:block;margin-bottom:12px"></i>Giỏ hàng trống</div>';
      if (totalEl) totalEl.textContent = '0₫';
      if (itemsInput) itemsInput.value = '[]';
      return;
    }
    
    let html = '';
    let total = 0;
    
    cart.forEach((item, index) => {
      const itemTotal = item.gia * item.sl;
      total += itemTotal;
      
      let choicesText = '';
      if (item.choices && item.choices.length > 0) {
        const groupedChoices = {};
        item.choices.forEach(c => {
          if (!groupedChoices[c.nhom_ten]) {
            groupedChoices[c.nhom_ten] = {};
          }
          // Đếm số lần xuất hiện của mỗi lựa chọn (cho topping)
          const choiceKey = c.lua_chon_ten;
          if (!groupedChoices[c.nhom_ten][choiceKey]) {
            groupedChoices[c.nhom_ten][choiceKey] = 0;
          }
          groupedChoices[c.nhom_ten][choiceKey]++;
        });
        
        const choiceParts = [];
        Object.keys(groupedChoices).forEach(nhomTen => {
          const choices = groupedChoices[nhomTen];
          const choiceList = [];
          Object.keys(choices).forEach(choiceName => {
            const count = choices[choiceName];
            if (count > 1) {
              choiceList.push(`${choiceName} (x${count})`);
          } else {
              choiceList.push(choiceName);
            }
          });
          if (choiceList.length > 0) {
            choiceParts.push(`${nhomTen}: ${choiceList.join(', ')}`);
          }
        });
        
        if (choiceParts.length > 0) {
        choicesText = '<div style="font-size:12px;color:#64748b;margin-top:6px;line-height:1.4">';
        choicesText += choiceParts.join(' | ');
        choicesText += '</div>';
        }
      }
      
      html += `
        <div class="cart-item">
          <div style="flex:1;min-width:0">
            <div style="font-weight:600;font-size:14px;color:#22334b;margin-bottom:4px">${item.ten}</div>
            ${choicesText}
            <div style="font-size:12px;color:#94a3b8;margin-top:6px">
              ${formatMoney(item.gia)} × ${item.sl}
            </div>
          </div>
          <div style="text-align:right;flex-shrink:0;display:flex;flex-direction:column;align-items:flex-end;gap:8px">
            <div style="font-weight:700;color:#059669;font-size:15px">${formatMoney(itemTotal)}</div>
            <div style="display:flex;align-items:center;gap:8px">
              <div class="qty">
                <button onclick="orderCreate.updateQty(${index}, -1)">-</button>
                <span style="padding:0 8px;font-weight:600;min-width:24px;text-align:center">${item.sl}</span>
                <button onclick="orderCreate.updateQty(${index}, 1)">+</button>
              </div>
              <button onclick="orderCreate.removeItem(${index})" style="width:28px;height:28px;border:1.5px solid #f44336;background:#fff;color:#f44336;border-radius:6px;cursor:pointer;font-size:16px;display:flex;align-items:center;justify-content:center;transition:all 0.2s" title="Xóa" onmouseover="this.style.background='#f44336';this.style.color='#fff'" onmouseout="this.style.background='#fff';this.style.color='#f44336'">×</button>
            </div>
          </div>
        </div>
      `;
    });
    
    cartEl.innerHTML = html;
    if (totalEl) totalEl.textContent = formatMoney(total);
    if (itemsInput) itemsInput.value = JSON.stringify(cart);
  }
  
  // Cập nhật số lượng
  function updateQty(index, delta) {
    const cart = getActiveCart();
    if (index < 0 || index >= cart.length) return;
    cart[index].sl += delta;
    if (cart[index].sl <= 0) {
      cart.splice(index, 1);
    }
    setActiveCart(cart);
    updateCart();
    updateTabs();
  }
  
  // Xóa item
  function removeItem(index) {
    const cart = getActiveCart();
    if (index < 0 || index >= cart.length) return;
    cart.splice(index, 1);
    setActiveCart(cart);
    updateCart();
    updateTabs();
  }
  
  // Hiển thị modal thanh toán
  function showPaymentModal() {
    const cart = getActiveCart();
    if (!cart || cart.length === 0) {
      alert('Vui lòng chọn ít nhất một sản phẩm');
      return;
    }
    
    // Tính tổng tiền
    let total = 0;
    cart.forEach(item => {
      total += item.gia * item.sl;
    });
    
    // Cập nhật tổng tiền trong modal
    const paymentTotal = document.getElementById('paymentTotal');
    if (paymentTotal) {
      paymentTotal.textContent = formatMoney(total);
    }
    
    // Reset phương thức thanh toán về tiền mặt
    selectPaymentMethod('tien_mat');
    
    // Hiển thị modal
    const modal = document.getElementById('paymentModal');
    if (modal) {
      modal.classList.add('active');
    }
  }
  
  // Đóng modal thanh toán
  function closePaymentModal() {
    const modal = document.getElementById('paymentModal');
    if (modal) {
      modal.classList.remove('active');
    }
    
    // Reset input khách trả
    const customerPaid = document.getElementById('customerPaid');
    if (customerPaid) {
      customerPaid.value = '';
    }
    updateChange();
  }
  
  // Chọn phương thức thanh toán
  function selectPaymentMethod(method) {
    // Cập nhật active state
    document.querySelectorAll('.payment-method').forEach(btn => {
      btn.classList.remove('active');
    });
    const btn = document.querySelector(`.payment-method[data-method="${method}"]`);
    if (btn) {
      btn.classList.add('active');
    }
    
    // Lưu phương thức đã chọn
    const hiddenInput = document.getElementById('selectedPaymentMethod');
    if (hiddenInput) {
      hiddenInput.value = method;
    }
    
    // Hiển thị/ẩn phần nhập tiền khách trả (chỉ hiện khi chọn tiền mặt)
    const customerPaidRow = document.getElementById('customerPaidRow');
    const changeRow = document.getElementById('changeRow');
    const numpad = document.getElementById('paymentNumpad');
    
    if (method === 'tien_mat') {
      if (customerPaidRow) customerPaidRow.style.display = 'flex';
      if (changeRow) changeRow.style.display = 'flex';
      if (numpad) numpad.style.display = 'block';
      
      // Tính tiền thừa
      updateChange();
    } else {
      if (customerPaidRow) customerPaidRow.style.display = 'none';
      if (changeRow) changeRow.style.display = 'none';
      if (numpad) numpad.style.display = 'none';
      
      // Reset input
      const customerPaid = document.getElementById('customerPaid');
      if (customerPaid) {
        customerPaid.value = '';
      }
    }
  }
  
  // Nhập số từ bàn phím số
  function numpadInput(value) {
    const input = document.getElementById('customerPaid');
    if (!input) return;
    
    const current = input.value.replace(/[^0-9]/g, '');
    const newValue = current + value;
    input.value = formatNumber(newValue);
    updateChange();
  }
  
  // Xóa số
  function numpadClear() {
    const input = document.getElementById('customerPaid');
    if (input) {
      input.value = '';
      updateChange();
    }
  }
  
  // Đặt số tiền nhanh
  function setQuickAmount(amount) {
    const input = document.getElementById('customerPaid');
    if (input) {
      input.value = formatNumber(amount.toString());
      updateChange();
    }
  }
  
  // Tính tiền thừa
  function updateChange() {
    const customerPaid = document.getElementById('customerPaid');
    const changeEl = document.getElementById('paymentChange');
    const paymentTotal = document.getElementById('paymentTotal');
    
    if (!customerPaid || !changeEl || !paymentTotal) return;
    
    // Lấy tổng tiền
    const cart = getActiveCart();
    let total = 0;
    cart.forEach(item => {
      total += item.gia * item.sl;
    });
    
    // Lấy số tiền khách trả
    const paid = parseFloat(customerPaid.value.replace(/[^0-9]/g, '')) || 0;
    
    // Tính tiền thừa
    const change = paid - total;
    
    if (change >= 0) {
      changeEl.textContent = formatMoney(change);
      changeEl.style.color = '#4caf50';
    } else {
      changeEl.textContent = formatMoney(Math.abs(change)) + ' (thiếu)';
      changeEl.style.color = '#f44336';
    }
  }
  
  // Format số với dấu phẩy
  function formatNumber(value) {
    const num = value.replace(/[^0-9]/g, '');
    if (!num) return '';
    return parseInt(num).toLocaleString('vi-VN');
  }
  
  // Xác nhận thanh toán
  function confirmPayment() {
    const method = document.getElementById('selectedPaymentMethod').value;
    const cart = getActiveCart();
    const order = getActiveOrder();
    
    if (!cart || cart.length === 0) {
      alert('Vui lòng chọn ít nhất một sản phẩm');
      return;
    }
    
    // Nếu là tiền mặt, kiểm tra số tiền khách trả
    if (method === 'tien_mat') {
      const customerPaid = document.getElementById('customerPaid');
      const paid = parseFloat(customerPaid.value.replace(/[^0-9]/g, '')) || 0;
      
      // Tính tổng tiền
      let total = 0;
      cart.forEach(item => {
        total += item.gia * item.sl;
      });
      
      if (paid < total) {
        alert('Số tiền khách trả không đủ!');
        return;
      }
    }
    
    // Lưu phương thức thanh toán vào order
    if (order) {
      order.phuong_thuc_thanhtoan = method;
      saveOrdersToStorage();
    }
    
    // Lưu phương thức thanh toán vào form
    const formInput = document.getElementById('phuongThucThanhToan');
    if (formInput) {
      formInput.value = method;
    }
    
    // Đóng modal
    closePaymentModal();
    
    // Submit form (tạo đơn ngay)
    submitOrder();
  }
  
  // In tạm tính (không lưu đơn)
  function printTempBill() {
    const cart = getActiveCart();
    const order = getActiveOrder();

    if (!cart || cart.length === 0) {
      alert('Vui lòng chọn ít nhất một sản phẩm');
      return;
    }

    const printArea = document.getElementById('tempBillPrint');
    if (!printArea) {
      alert('Không tìm thấy vùng in.');
      return;
    }

    const shopName = window.SHOP_NAME || 'CHILL GUYS';
    const customerName = (order && order.ten_khachhang) ? order.ten_khachhang : 'Khách lẻ';
    const customerPhone = (order && order.sdt_khachhang) ? order.sdt_khachhang : '';
    let total = 0;
    let rowsHtml = '';

    cart.forEach(function(item) {
      const lineTotal = Number(item.gia || 0) * (parseInt(item.sl, 10) || 1);
      total += lineTotal;
      rowsHtml += '<tr>'
        + '<td>' + escapeHtml(item.ten) + '</td>'
        + '<td style="text-align:center">' + (item.sl || 1) + '</td>'
        + '<td style="text-align:right">' + formatMoney(lineTotal) + '</td>'
        + '</tr>';
    });

    const now = new Date().toLocaleString('vi-VN');
    printArea.innerHTML = ''
      + '<div class="temp-bill">'
      + '<h2>' + escapeHtml(shopName) + '</h2>'
      + '<p class="temp-bill-meta">PHIẾU TẠM TÍNH</p>'
      + '<p class="temp-bill-meta">' + now + '</p>'
      + '<p>Khách: ' + escapeHtml(customerName)
      + (customerPhone ? ' · ' + escapeHtml(customerPhone) : '')
      + '</p>'
      + '<table><thead><tr><th>Món</th><th>SL</th><th>Thành tiền</th></tr></thead><tbody>'
      + rowsHtml
      + '</tbody></table>'
      + '<p class="temp-bill-total">Tổng: <strong>' + formatMoney(total) + '</strong></p>'
      + '<p class="temp-bill-note">* Chưa thanh toán — chỉ dùng tham khảo</p>'
      + '</div>';

    window.print();
  }
  
  // Lưu đơn chờ xử lý (chưa thanh toán)
  async function savePendingOrder() {
    const cart = getActiveCart();
    const order = getActiveOrder();

    if (!cart || cart.length === 0) {
      alert('Vui lòng chọn ít nhất một sản phẩm');
      return;
    }

    if (!order) {
      alert('Không tìm thấy đơn hàng');
      return;
    }

    const btnLuuDon = document.getElementById('btnLuuDon');
    if (btnLuuDon) {
      btnLuuDon.disabled = true;
      const btnText = btnLuuDon.querySelector('.btn-text');
      if (btnText) btnText.textContent = 'Đang lưu...';
    }

    try {
      const result = await submitSingleOrder({
        action: 'save_pending_order',
        items: cart,
        ten_khachhang: order.ten_khachhang || 'Khách lẻ',
        sdt_khachhang: order.sdt_khachhang || ''
      });

      const index = orders.findIndex(o => o.id === order.id);
      if (index >= 0) {
        orders.splice(index, 1);
        if (orders.length > 0) {
          activeOrderId = orders[0].id;
        } else {
          activeOrderId = null;
          createNewOrder();
        }
      }

      saveOrdersToStorage();
      updateTabs();
      updateCart();

      window.location.href = result.redirect || orderListUrl();
    } catch (error) {
      console.error('Error saving pending order:', error);
      alert(error.message || 'Có lỗi xảy ra khi lưu đơn. Vui lòng thử lại.');
    } finally {
      if (btnLuuDon) {
        btnLuuDon.disabled = false;
        const btnText = btnLuuDon.querySelector('.btn-text');
        if (btnText) btnText.innerHTML = '<i class="fas fa-save"></i> Lưu đơn';
      }
    }
  }

  // Submit đơn hàng
  async function submitOrder() {
    const cart = getActiveCart();
    const order = getActiveOrder();
    
    if (!cart || cart.length === 0) {
      alert('Vui lòng chọn ít nhất một sản phẩm');
      return;
    }
    
    if (!order) {
      alert('Không tìm thấy đơn hàng');
      return;
    }
    
    // Set loading state cho button
    const btnThanhToan = document.getElementById('btnThanhToan');
    if (btnThanhToan) {
      btnThanhToan.disabled = true;
      const btnText = btnThanhToan.querySelector('.btn-text');
      if (btnText) btnText.textContent = 'Đang xử lý...';
    }
    
    try {
      const result = await submitSingleOrder({
        action: 'create_order',
        items: cart,
        ten_khachhang: order.ten_khachhang || 'Khách lẻ',
        sdt_khachhang: order.sdt_khachhang || '',
        phuong_thuc_thanhtoan: order.phuong_thuc_thanhtoan || 'tien_mat'
      });
      
      // Xóa đơn đã submit
      const index = orders.findIndex(o => o.id === order.id);
      if (index >= 0) {
        orders.splice(index, 1);
        
        // Chuyển sang đơn khác nếu còn
        if (orders.length > 0) {
          activeOrderId = orders[0].id;
        } else {
          activeOrderId = null;
          createNewOrder();
        }
      }
      
      saveOrdersToStorage();
      updateTabs();
      updateCart();
      
      window.location.href = result.redirect || orderListUrl();
    } catch (error) {
      console.error('Error submitting order:', error);
      alert(error.message || 'Có lỗi xảy ra khi tạo đơn hàng. Vui lòng thử lại.');
    } finally {
      if (btnThanhToan) {
        btnThanhToan.disabled = false;
        const btnText = btnThanhToan.querySelector('.btn-text');
        if (btnText) btnText.textContent = 'Thanh toán';
      }
    }
  }
  
  // Format tiền
  function formatMoney(amount) {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount);
  }
  
  // Submit 1 đơn
  async function submitSingleOrder(order) {
    const normalizedItems = normalizeCartItems(order.items);
    if (!normalizedItems.length) {
      throw new Error('Vui lòng chọn ít nhất một sản phẩm.');
    }

    const params = new URLSearchParams();
    params.append('action', order.action || 'create_order');
    params.append('items', JSON.stringify(normalizedItems));
    params.append('ten_khachhang', order.ten_khachhang || 'Khách lẻ');
    params.append('sdt_khachhang', order.sdt_khachhang || '');
    if (order.phuong_thuc_thanhtoan) {
      params.append('phuong_thuc_thanhtoan', order.phuong_thuc_thanhtoan);
    }

    const response = await fetch(orderCreateUrl(), {
      method: 'POST',
      body: params,
      credentials: 'same-origin',
      headers: {
        'X-Requested-With': 'XMLHttpRequest',
        'Accept': 'application/json',
        'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
      }
    });

    let data;
    try {
      data = await response.json();
    } catch (error) {
      throw new Error('Không nhận được phản hồi từ server.');
    }

    if (!response.ok || !data.success) {
      throw new Error(data.error || data.message || 'Có lỗi khi lưu đơn hàng.');
    }

    return data;
  }

  function normalizeCartItems(items) {
    return (items || []).map(function(item) {
      return {
        id: parseInt(item.id, 10),
        ten: item.ten || '',
        gia: Number(item.gia || 0),
        sl: parseInt(item.sl, 10) || 1,
        size: item.size || '',
        size_id: item.size_id == null || item.size_id === '' ? null : parseInt(item.size_id, 10),
        choices: (item.choices || []).map(function(choice) {
          return {
            nhom_id: parseInt(choice.nhom_id, 10),
            nhom_ten: choice.nhom_ten || '',
            lua_chon_id: parseInt(choice.lua_chon_id, 10),
            lua_chon_ten: choice.lua_chon_ten || '',
            gia_tang_them: Number(choice.gia_tang_them || 0)
          };
        })
      };
    }).filter(function(item) {
      return Number.isFinite(item.id) && item.id > 0;
    });
  }
  
  // Export API
  window.orderCreate = {
    closeOptions,
    applyOptions,
    updateQty,
    removeItem,
    showPaymentModal,
    closePaymentModal,
    selectPaymentMethod,
    numpadInput,
    numpadClear,
    setQuickAmount,
    confirmPayment,
    printTempBill,
    switchOrder,
    closeOrder
  };
  
  // Khởi tạo khi DOM ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();

