(function () {
  'use strict';
  const ctx = window.qlbhPos;
  ctx.savePendingOrder = async function savePendingOrder() {
    const cart = ctx.getActiveCart();
    const order = ctx.getActiveOrder();

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
      const result = await ctx.submitSingleOrder({
        action: 'save_pending_order',
        items: cart,
        ten_khachhang: order.ten_khachhang || 'Khách lẻ',
        sdt_khachhang: order.sdt_khachhang || '',
        ma_giam: ctx.appliedVoucher.code || ''
      });

      const index = ctx.orders.findIndex(o => o.id === order.id);
      if (index >= 0) {
        ctx.orders.splice(index, 1);
        if (ctx.orders.length > 0) {
          ctx.activeOrderId = ctx.orders[0].id;
        } else {
          ctx.activeOrderId = null;
          ctx.createNewOrder();
        }
      }

      ctx.saveOrdersToStorage();
      ctx.updateTabs();
      ctx.updateCart();

      window.location.href = result.redirect || ctx.orderListUrl();
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
  ctx.submitOrder = async function submitOrder() {
    const cart = ctx.getActiveCart();
    const order = ctx.getActiveOrder();
    
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
      const result = await ctx.submitSingleOrder({
        action: 'create_order',
        items: cart,
        ten_khachhang: order.ten_khachhang || 'Khách lẻ',
        sdt_khachhang: order.sdt_khachhang || '',
        phuong_thuc_thanhtoan: order.phuong_thuc_thanhtoan || 'tien_mat',
        ma_giam: ctx.appliedVoucher.code || ''
      });
      
      // Xóa đơn đã submit
      const index = ctx.orders.findIndex(o => o.id === order.id);
      if (index >= 0) {
        ctx.orders.splice(index, 1);
        
        // Chuyển sang đơn khác nếu còn
        if (ctx.orders.length > 0) {
          ctx.activeOrderId = ctx.orders[0].id;
        } else {
          ctx.activeOrderId = null;
          ctx.createNewOrder();
        }
      }
      
      ctx.saveOrdersToStorage();
      ctx.updateTabs();
      ctx.updateCart();
      if (result.offline) {
        alert(result.message);
        return;
      }
      
      window.location.href = result.redirect || ctx.orderListUrl();
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
  ctx.formatMoney = function formatMoney(amount) {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount);
  }
  
  // Submit 1 đơn
  ctx.submitSingleOrder = async function submitSingleOrder(order) {
    const normalizedItems = ctx.normalizeCartItems(order.items);
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
    if (order.ma_giam) {
      params.append('ma_giam', order.ma_giam);
    }

    const response = await fetch(ctx.orderCreateUrl(), {
      method: 'POST',
      body: params,
      credentials: 'same-origin',
      headers: {
        'X-Requested-With': 'XMLHttpRequest',
        'Accept': 'application/json',
        'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
      }
    }).catch(function () {
      if (window.qlbhOffline) {
        window.qlbhOffline.save(params.toString());
      }
      return null;
    });

    if (!response) {
      return { success: true, offline: true, message: 'Mất mạng. Đơn đã lưu trên máy và sẽ gửi khi có mạng.' };
    }

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

  ctx.normalizeCartItems = function normalizeCartItems(items) {
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
  window.orderCreate = {
    closeOptions: ctx.closeOptions,
    applyOptions: ctx.applyOptions,
    updateQty: ctx.updateQty,
    removeItem: ctx.removeItem,
    showPaymentModal: ctx.showPaymentModal,
    closePaymentModal: ctx.closePaymentModal,
    selectPaymentMethod: ctx.selectPaymentMethod,
    applyVoucher: ctx.applyVoucher,
    numpadInput: ctx.numpadInput,
    numpadClear: ctx.numpadClear,
    setQuickAmount: ctx.setQuickAmount,
    confirmPayment: ctx.confirmPayment,
    printTempBill: ctx.printTempBill,
    switchOrder: ctx.switchOrder,
    closeOrder: ctx.closeOrder
  };
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', ctx.init);
  } else {
    ctx.init();
  }
})();