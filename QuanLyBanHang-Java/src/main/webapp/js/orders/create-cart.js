(function () {
  'use strict';
  const ctx = window.qlbhPos;
  ctx.apiBase = function apiBase() {
    const base = window.APP_BASE_PATH || window.BASE_PATH || '/';
    return base.endsWith('/') ? base : base + '/';
  }

  ctx.orderCreateUrl = function orderCreateUrl() {
    return ctx.apiBase() + 'don-hang/create';
  }

  ctx.orderListUrl = function orderListUrl() {
    return ctx.apiBase() + 'don-hang';
  }
  
  // Helper: Lấy đơn đang active
  ctx.getActiveOrder = function getActiveOrder() {
    return ctx.orders.find(o => o.id === ctx.activeOrderId);
  }
  
  // Helper: Lấy giỏ hàng của đơn active
  ctx.getActiveCart = function getActiveCart() {
    const order = ctx.getActiveOrder();
    return order ? order.cart : [];
  }
  
  // Helper: Set giỏ hàng của đơn active
  ctx.setActiveCart = function setActiveCart(cart) {
    const order = ctx.getActiveOrder();
    if (order) {
      order.cart = cart;
      ctx.saveOrdersToStorage();
    }
  }
  
  // Lưu ctx.orders vào localStorage
  ctx.saveOrdersToStorage = function saveOrdersToStorage() {
    try {
      localStorage.setItem('order_tabs', JSON.stringify({
        orders: ctx.orders,
        activeOrderId: ctx.activeOrderId
      }));
    } catch (e) {
      console.error('Error saving ctx.orders to localStorage:', e);
    }
  }
  
  // Load ctx.orders từ localStorage
  ctx.loadOrdersFromStorage = function loadOrdersFromStorage() {
    try {
      const saved = localStorage.getItem('order_tabs');
      if (saved) {
        const data = JSON.parse(saved);
        ctx.orders = data.orders || [];
        ctx.activeOrderId = data.activeOrderId || null;
        return true;
      }
    } catch (e) {
      console.error('Error loading ctx.orders from localStorage:', e);
    }
    return false;
  }
  
  // Xóa ctx.orders khỏi localStorage
  ctx.clearOrdersStorage = function clearOrdersStorage() {
    try {
      localStorage.removeItem('order_tabs');
    } catch (e) {
      console.error('Error clearing ctx.orders from localStorage:', e);
    }
  }
  
  // Tạo đơn mới
  ctx.createNewOrder = function createNewOrder() {
    const orderId = Date.now();
    const order = {
      id: orderId,
      title: `Đơn hàng ${ctx.orders.length + 1}`,
      cart: [],
      ten_khachhang: '',
      sdt_khachhang: '',
      phuong_thuc_thanhtoan: window.DEFAULT_PAYMENT_METHOD || 'tien_mat',
      created_at: new Date().toISOString()
    };
    
    ctx.orders.push(order);
    ctx.activeOrderId = orderId;
    ctx.saveOrdersToStorage();
    ctx.updateTabs();
    ctx.updateCart();
    
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
  ctx.loadProductChoices = async function loadProductChoices(productId) {
    try {
      const url = `${ctx.apiBase()}api/product-choices?id=${productId}`;
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
        ctx.productChoices[productId] = data.choices || [];
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
  ctx.loadProductSizes = async function loadProductSizes(productId) {
    try {
      const url = `${ctx.apiBase()}api/product-sizes?product_id=${productId}`;
      const response = await fetch(url);
      
      // Always get text first to check content
      const text = await response.text();
      
      // Check if response looks like HTML (PHP error)
      if (text.trim().startsWith('<') || text.includes('<br') || text.includes('<b>')) {
        console.error('API returned HTML instead of JSON. Response:', text.substring(0, 500));
        ctx.productSizes[productId] = [];
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
        ctx.productSizes[productId] = [];
        return [];
      }
      
      if (data.success && data.has_sizes) {
        ctx.productSizes[productId] = data.sizes || [];
        return data.sizes || [];
      }
      ctx.productSizes[productId] = [];
      return [];
    } catch (error) {
      console.error('Error loading product sizes:', error);
      ctx.productSizes[productId] = [];
      return [];
    }
  }
  
  const RANK_LABELS = {
    thuong: 'Thường',
    bac: 'Bạc',
    vang: 'Vàng',
    vip: 'VIP'
  };

  ctx.searchCustomers = async function searchCustomers(query) {
    if (!query || query.trim().length < 2) {
      return [];
    }
    const url = `${ctx.apiBase()}api/search-customer?q=${encodeURIComponent(query.trim())}`;
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

  ctx.getRankLabel = function getRankLabel(rank) {
    return RANK_LABELS[rank] || 'Thường';
  }

  ctx.selectCustomer = function selectCustomer(customer) {
    ctx.matchedCustomer = customer;
    const order = ctx.getActiveOrder();
    const tenInput = document.getElementById('tenKhachhang');
    const sdtInput = document.getElementById('sdtKhachhang');

    if (tenInput) tenInput.value = customer.ten_khachhang || '';
    if (sdtInput) sdtInput.value = customer.so_dien_thoai || '';

    if (order) {
      order.ten_khachhang = customer.ten_khachhang || '';
      order.sdt_khachhang = customer.so_dien_thoai || '';
      ctx.saveOrdersToStorage();
      ctx.updateTabs();
    }

    ctx.updateMatchedBadge(customer);
    ctx.hideCustomerSuggestions();
    ctx.closeCustomerFindModal();
  }

  ctx.clearMatchedCustomer = function clearMatchedCustomer() {
    ctx.matchedCustomer = null;
    const badge = document.getElementById('customerMatchedBadge');
    if (badge) badge.style.display = 'none';
  }

  ctx.updateMatchedBadge = function updateMatchedBadge(customer) {
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

  ctx.renderSuggestionItem = function renderSuggestionItem(customer) {
    const rank = ctx.getRankLabel(customer.loai_thanh_vien);
    return `
      <div class="autocomplete-suggestion" data-customer-id="${customer.id}">
        <div class="suggestion-left">
          <div class="suggestion-name">${ctx.escapeHtml(customer.ten_khachhang || '')}</div>
          <div class="suggestion-phone">${ctx.escapeHtml(customer.so_dien_thoai || 'Không có SĐT')}</div>
        </div>
        <div class="suggestion-right">
          <span class="suggestion-tier rank-${customer.loai_thanh_vien || 'thuong'}">${rank}</span>
        </div>
      </div>
    `;
  }

  ctx.showCustomerSuggestions = function showCustomerSuggestions(customers) {
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
        ctx.selectCustomer(customers[index]);
      });
    });
  }

  ctx.hideCustomerSuggestions = function hideCustomerSuggestions() {
    const box = document.getElementById('customerSuggestions');
    if (box) {
      box.style.display = 'none';
      box.innerHTML = '';
    }
  }

  ctx.handleCustomerInputChange = async function handleCustomerInputChange() {
    const tenInput = document.getElementById('tenKhachhang');
    const sdtInput = document.getElementById('sdtKhachhang');
    const query = ((sdtInput && sdtInput.value) || (tenInput && tenInput.value) || '').trim();

    if (query.length < 2) {
      ctx.hideCustomerSuggestions();
      if (!query) ctx.clearMatchedCustomer();
      return;
    }

    const customers = await ctx.searchCustomers(query);
    if (!customers.length) {
      ctx.hideCustomerSuggestions();
      ctx.clearMatchedCustomer();
      return;
    }

    const exactMatch = customers.find(c =>
      (sdtInput && sdtInput.value && c.so_dien_thoai === sdtInput.value.trim()) ||
      (tenInput && tenInput.value && c.ten_khachhang === tenInput.value.trim())
    );

    if (exactMatch) {
      ctx.matchedCustomer = exactMatch;
      ctx.updateMatchedBadge(exactMatch);
    } else {
      ctx.clearMatchedCustomer();
    }

    ctx.showCustomerSuggestions(customers);
  }

  ctx.openCustomerFindModal = function openCustomerFindModal() {
    const modal = document.getElementById('customerFindModal');
    const input = document.getElementById('customerFindInput');
    if (modal) modal.classList.add('active');
    if (input) {
      input.value = '';
      input.focus();
      ctx.renderCustomerFindResults([]);
    }
  }

  ctx.closeCustomerFindModal = function closeCustomerFindModal() {
    const modal = document.getElementById('customerFindModal');
    if (modal) modal.classList.remove('active');
  }

  ctx.renderCustomerFindResults = function renderCustomerFindResults(customers) {
    const box = document.getElementById('customerFindResults');
    if (!box) return;

    if (!customers.length) {
      box.innerHTML = '<div class="customer-find-empty">Nhập tên hoặc SĐT để tìm khách</div>';
      return;
    }

    box.innerHTML = customers.map((customer, index) => `
      <div class="customer-find-item" data-index="${index}">
        <div>
          <div class="customer-find-item-name">${ctx.escapeHtml(customer.ten_khachhang || '')}</div>
          <div class="customer-find-item-phone">${ctx.escapeHtml(customer.so_dien_thoai || 'Không có SĐT')} · ${parseInt(customer.tong_donhang, 10) || 0} đơn</div>
        </div>
        <span class="suggestion-tier rank-${customer.loai_thanh_vien || 'thuong'}">${ctx.getRankLabel(customer.loai_thanh_vien)}</span>
      </div>
    `).join('');

    box.querySelectorAll('.customer-find-item').forEach(el => {
      el.addEventListener('click', function() {
        const idx = parseInt(this.dataset.index, 10);
        if (!isNaN(idx) && customers[idx]) {
          ctx.selectCustomer(customers[idx]);
        }
      });
    });
  }

  ctx.initCustomerSearch = function initCustomerSearch() {
    const tenInput = document.getElementById('tenKhachhang');
    const sdtInput = document.getElementById('sdtKhachhang');
    const btnFind = document.getElementById('btnFindCustomer');
    const btnClose = document.getElementById('btnCloseFindCustomer');
    const findInput = document.getElementById('customerFindInput');
    const findModal = document.getElementById('customerFindModal');
    const wrapper = document.querySelector('.customer-search-wrapper');

    function scheduleSearch() {
      clearTimeout(ctx.customerSearchTimer);
      ctx.customerSearchTimer = setTimeout(handleCustomerInputChange, 300);
    }

    if (tenInput) {
      tenInput.addEventListener('input', function() {
        const order = ctx.getActiveOrder();
        if (order) {
          order.ten_khachhang = this.value;
          ctx.saveOrdersToStorage();
          ctx.updateTabs();
        }
        scheduleSearch();
      });
    }

    if (sdtInput) {
      sdtInput.addEventListener('input', function() {
        const order = ctx.getActiveOrder();
        if (order) {
          order.sdt_khachhang = this.value;
          ctx.saveOrdersToStorage();
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
        if (e.target === findModal) ctx.closeCustomerFindModal();
      });
    }

    if (findInput) {
      let findTimer = null;
      findInput.addEventListener('input', function() {
        clearTimeout(findTimer);
        const query = this.value.trim();
        findTimer = setTimeout(async function() {
          if (query.length < 2) {
            ctx.renderCustomerFindResults([]);
            return;
          }
          const customers = await ctx.searchCustomers(query);
          ctx.renderCustomerFindResults(customers);
        }, 300);
      });
    }

    document.addEventListener('click', function(e) {
      if (wrapper && !wrapper.contains(e.target)) {
        ctx.hideCustomerSuggestions();
      }
    });
  }

  // Khởi tạo
  ctx.init = function init() {
    // Load ctx.orders từ localStorage
    if (!ctx.loadOrdersFromStorage() || ctx.orders.length === 0) {
      // Tạo đơn đầu tiên nếu chưa có
      ctx.createNewOrder();
    } else {
      ctx.updateTabs();
      ctx.updateCart();
    }
    
    // Không load trước - sẽ load khi click vào sản phẩm
    // document.querySelectorAll('.prod-card').forEach(card => {
    //   const productId = card.dataset.id;
    //   ctx.loadProductChoices(productId);
    // });
    
    // Click vào sản phẩm
    document.querySelectorAll('.prod-card').forEach(card => {
      card.addEventListener('click', async function() {
        const id = this.dataset.id;
        const name = this.dataset.name;
        const price = parseFloat(this.dataset.price);
        
        ctx.currentProductId = id;
        ctx.currentProductPrice = price;
        
        // Load nhóm lựa chọn và sizes song song
        const [choices, sizes] = await Promise.all([
          ctx.productChoices[id] ? Promise.resolve(ctx.productChoices[id]) : ctx.loadProductChoices(id),
          ctx.productSizes[id] ? Promise.resolve(ctx.productSizes[id]) : ctx.loadProductSizes(id)
        ]);
        
        ctx.showOptionsModal(id, name, price, choices, sizes);
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
        ctx.showPaymentModal();
      });
    }

    // Nút lưu đơn chờ xử lý
    const btnLuuDon = document.getElementById('btnLuuDon');
    if (btnLuuDon) {
      btnLuuDon.addEventListener('click', function(e) {
        e.preventDefault();
        ctx.savePendingOrder();
      });
    }
    
    // Nút thêm đơn hàng
    const btnAddNewOrder = document.getElementById('btnAddNewOrder');
    if (btnAddNewOrder) {
      btnAddNewOrder.addEventListener('click', function(e) {
        e.preventDefault();
        ctx.createNewOrder();
      });
    }
    
    // Đóng modal khi click ra ngoài
    const paymentModal = document.getElementById('paymentModal');
    if (paymentModal) {
      paymentModal.addEventListener('click', function(e) {
        if (e.target === paymentModal) {
          ctx.closePaymentModal();
        }
      });
    }
    
    ctx.initCustomerSearch();
  }
  
  // Helper: Escape HTML
  ctx.escapeHtml = function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
  }
  
  // Hiển thị modal chọn lựa chọn
  ctx.showOptionsModal = function showOptionsModal(productId, productName, productPrice, choices, sizes = []) {
    const modal = document.getElementById('optionsModal');
    if (!modal) return;
    
    const optionsCard = modal.querySelector('.options-card');
    const safeProductName = productName.replace(/'/g, "\\'").replace(/"/g, '&quot;');
    optionsCard.innerHTML = `
      <h3>Tùy chọn: ${ctx.escapeHtml(productName)}</h3>
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
            toppingPrice.textContent = choice.gia_tang_them > 0 ? `+${ctx.formatMoney(choice.gia_tang_them)}/phần` : 'Miễn phí';
            
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
            option.textContent = choice.ten_lua_chon + (choice.gia_tang_them > 0 ? ` (+${ctx.formatMoney(choice.gia_tang_them)})` : '');
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
        option.textContent = size.ten_size + (size.gia > 0 ? ` (+${ctx.formatMoney(size.gia)})` : '');
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
  ctx.closeOptions = function closeOptions() {
    const modal = document.getElementById('optionsModal');
    if (modal) {
      modal.style.display = 'none';
    }
  }
  
  // Áp dụng lựa chọn và thêm vào giỏ
  ctx.applyOptions = function applyOptions(productId, productName, productPrice) {
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
    const choices = ctx.productChoices[productId] || [];
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
    const cart = ctx.getActiveCart();
    
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
    
    ctx.setActiveCart(cart);
    ctx.updateCart();
    ctx.updateTabs();
    ctx.closeOptions();
  }
  
  // Cập nhật tabs
  ctx.updateTabs = function updateTabs() {
    const tabsContainer = document.getElementById('orderTabs');
    if (!tabsContainer) return;
    
    if (ctx.orders.length === 0) {
      tabsContainer.innerHTML = '';
      return;
    }
    
    let html = '';
    ctx.orders.forEach((order, index) => {
      const isActive = order.id === ctx.activeOrderId;
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
            ${ctx.orders.length > 1 ? `<span class="order-tab-close" onclick="event.stopPropagation(); orderCreate.closeOrder(${order.id})" title="Đóng đơn hàng">×</span>` : ''}
          </div>
          <div class="order-tab-info">
            ${order.sdt_khachhang ? `<div class="order-tab-customer"><i class="fas fa-phone"></i> ${order.sdt_khachhang}</div>` : ''}
            ${itemCount > 0 ? `<span class="order-tab-badge">${itemCount} món</span>` : '<span style="font-size:11px;color:#94a3b8">Chưa có sản phẩm</span>'}
            ${total > 0 ? `<div class="order-tab-total">${ctx.formatMoney(total)}</div>` : ''}
          </div>
        </div>
      `;
    });
    
    tabsContainer.innerHTML = html;
  }
  
  // Chuyển đổi đơn hàng
  ctx.switchOrder = function switchOrder(orderId) {
    if (ctx.activeOrderId === orderId) return;
    
    ctx.activeOrderId = orderId;
    ctx.saveOrdersToStorage();
    ctx.updateTabs();
    ctx.updateCart();
  }
  
  // Đóng đơn hàng
  ctx.closeOrder = function closeOrder(orderId) {
    if (ctx.orders.length <= 1) {
      alert('Phải có ít nhất một đơn hàng');
      return;
    }
    
    if (confirm('Bạn có chắc chắn muốn đóng đơn hàng này?')) {
      const index = ctx.orders.findIndex(o => o.id === orderId);
      if (index >= 0) {
        ctx.orders.splice(index, 1);
        
        // Nếu đóng đơn đang active, chuyển sang đơn khác
        if (ctx.activeOrderId === orderId) {
          ctx.activeOrderId = ctx.orders.length > 0 ? ctx.orders[0].id : null;
        }
        
        ctx.saveOrdersToStorage();
        ctx.updateTabs();
        ctx.updateCart();
      }
    }
  }
  
  // Cập nhật giỏ hàng
  ctx.updateCart = function updateCart() {
    const cartEl = document.getElementById('cart');
    const totalEl = document.getElementById('total');
    const itemsInput = document.getElementById('itemsInput');
    const tenKhachInput = document.getElementById('tenKhachhang');
    const sdtKhachInput = document.getElementById('sdtKhachhang');
    
    if (!cartEl) return;
    
    const cart = ctx.getActiveCart();
    const order = ctx.getActiveOrder();
    
    // Cập nhật input khách hàng
    if (tenKhachInput && order) {
      tenKhachInput.value = order.ten_khachhang || '';
    }
    if (sdtKhachInput && order) {
      sdtKhachInput.value = order.sdt_khachhang || '';
    }

    ctx.matchedCustomer = null;
    ctx.clearMatchedCustomer();
    ctx.hideCustomerSuggestions();
    
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
              ${ctx.formatMoney(item.gia)} × ${item.sl}
            </div>
          </div>
          <div style="text-align:right;flex-shrink:0;display:flex;flex-direction:column;align-items:flex-end;gap:8px">
            <div style="font-weight:700;color:#059669;font-size:15px">${ctx.formatMoney(itemTotal)}</div>
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
    if (totalEl) totalEl.textContent = ctx.formatMoney(total);
    if (itemsInput) itemsInput.value = JSON.stringify(cart);
    ctx.scheduleDisplay(cart, total);
  }
  ctx.scheduleDisplay = function scheduleDisplay(cart, total) {
    clearTimeout(ctx.displayTimer);
    ctx.displayTimer = setTimeout(function () {
      const lines = (cart || []).map(function (item) {
        return { ten: item.ten + (item.size ? ' (' + item.size + ')' : ''), sl: item.sl, tien: item.gia * item.sl };
      });
      const body = new URLSearchParams();
      body.set('payload', JSON.stringify({ lines: lines, tong: total }));
      fetch(ctx.apiBase() + 'api/cfd', {
        method: 'POST',
        body: body,
        credentials: 'same-origin',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' }
      }).catch(function () {});
    }, 250);
  }
  
  // Cập nhật số lượng
  ctx.updateQty = function updateQty(index, delta) {
    const cart = ctx.getActiveCart();
    if (index < 0 || index >= cart.length) return;
    cart[index].sl += delta;
    if (cart[index].sl <= 0) {
      cart.splice(index, 1);
    }
    ctx.setActiveCart(cart);
    ctx.updateCart();
    ctx.updateTabs();
  }
  
  // Xóa item
  ctx.removeItem = function removeItem(index) {
    const cart = ctx.getActiveCart();
    if (index < 0 || index >= cart.length) return;
    cart.splice(index, 1);
    ctx.setActiveCart(cart);
    ctx.updateCart();
    ctx.updateTabs();
  }

})();