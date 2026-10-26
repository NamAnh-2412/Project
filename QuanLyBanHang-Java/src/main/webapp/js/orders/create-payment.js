(function () {
  'use strict';
  const ctx = window.qlbhPos;
  ctx.cartGross = function cartGross() {
    const cart = ctx.getActiveCart() || [];
    let total = 0;
    cart.forEach(item => {
      total += item.gia * item.sl;
    });
    return total;
  }

  ctx.payableTotal = function payableTotal() {
    return Math.max(0, ctx.cartGross() - (ctx.appliedVoucher.amount || 0));
  }

  ctx.applyVoucher = async function applyVoucher() {
    const input = document.getElementById('voucherCode');
    const code = input ? input.value.trim() : '';
    if (!code) {
      ctx.appliedVoucher = { code: '', amount: 0 };
      ctx.refreshVoucherRow();
      return;
    }
    const params = new URLSearchParams();
    params.set('ma', code);
    params.set('tong', String(ctx.cartGross()));
    const response = await fetch(ctx.apiBase() + 'api/ma-giam-gia/kiem-tra?' + params.toString(), {
      credentials: 'same-origin',
      headers: { 'Accept': 'application/json' }
    });
    const data = await response.json();
    if (!data.success) {
      ctx.appliedVoucher = { code: '', amount: 0 };
      ctx.refreshVoucherRow();
      alert(data.error || 'Mã giảm giá không dùng được.');
      return;
    }
    ctx.appliedVoucher = { code: data.ma, amount: Number(data.giam || 0) };
    ctx.refreshVoucherRow();
  }

  ctx.refreshVoucherRow = function refreshVoucherRow() {
    const row = document.getElementById('voucherDiscountRow');
    const label = document.getElementById('voucherDiscount');
    const paymentTotal = document.getElementById('paymentTotal');
    if (row && label) {
      if (ctx.appliedVoucher.amount > 0) {
        row.style.display = 'flex';
        label.textContent = '-' + ctx.formatMoney(ctx.appliedVoucher.amount);
      } else {
        row.style.display = 'none';
      }
    }
    if (paymentTotal) {
      paymentTotal.textContent = ctx.formatMoney(ctx.payableTotal());
    }
    ctx.updateChange();
  }

  // Hiển thị modal thanh toán
  ctx.showPaymentModal = function showPaymentModal() {
    const cart = ctx.getActiveCart();
    if (!cart || cart.length === 0) {
      alert('Vui lòng chọn ít nhất một sản phẩm');
      return;
    }
    
    ctx.appliedVoucher = { code: '', amount: 0 };
    const voucherInput = document.getElementById('voucherCode');
    if (voucherInput) voucherInput.value = '';
    ctx.refreshVoucherRow();
    
    // Reset phương thức thanh toán về tiền mặt
    ctx.selectPaymentMethod('tien_mat');
    
    // Hiển thị modal
    const modal = document.getElementById('paymentModal');
    if (modal) {
      modal.classList.add('active');
    }
    const first = cart[0];
    const suggest = document.getElementById('suggestBox');
    if (suggest) suggest.textContent = '';
    if (first && first.id) {
      fetch(ctx.apiBase() + 'api/goi-y?sanpham_id=' + encodeURIComponent(first.id), { credentials: 'same-origin' })
        .then(function (response) { return response.json(); })
        .then(function (data) {
          if (suggest && data.goiY && data.goiY.length) {
            suggest.textContent = 'Gợi ý kèm: ' + data.goiY.map(function (item) { return item.ten; }).join(', ');
          }
        })
        .catch(function () {});
    }
  }
  
  // Đóng modal thanh toán
  ctx.closePaymentModal = function closePaymentModal() {
    const modal = document.getElementById('paymentModal');
    if (modal) {
      modal.classList.remove('active');
    }
    
    // Reset input khách trả
    const customerPaid = document.getElementById('customerPaid');
    if (customerPaid) {
      customerPaid.value = '';
    }
    ctx.updateChange();
  }
  
  // Chọn phương thức thanh toán
  ctx.selectPaymentMethod = function selectPaymentMethod(method) {
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
      ctx.updateChange();
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
  ctx.numpadInput = function numpadInput(value) {
    const input = document.getElementById('customerPaid');
    if (!input) return;
    
    const current = input.value.replace(/[^0-9]/g, '');
    const newValue = current + value;
    input.value = ctx.formatNumber(newValue);
    ctx.updateChange();
  }
  
  // Xóa số
  ctx.numpadClear = function numpadClear() {
    const input = document.getElementById('customerPaid');
    if (input) {
      input.value = '';
      ctx.updateChange();
    }
  }
  
  // Đặt số tiền nhanh
  ctx.setQuickAmount = function setQuickAmount(amount) {
    const input = document.getElementById('customerPaid');
    if (input) {
      input.value = ctx.formatNumber(amount.toString());
      ctx.updateChange();
    }
  }
  
  // Tính tiền thừa
  ctx.updateChange = function updateChange() {
    const customerPaid = document.getElementById('customerPaid');
    const changeEl = document.getElementById('paymentChange');
    const paymentTotal = document.getElementById('paymentTotal');
    
    if (!customerPaid || !changeEl || !paymentTotal) return;
    
    // Lấy tổng tiền
    const total = ctx.payableTotal();
    
    // Lấy số tiền khách trả
    const paid = parseFloat(customerPaid.value.replace(/[^0-9]/g, '')) || 0;
    
    // Tính tiền thừa
    const change = paid - total;
    
    if (change >= 0) {
      changeEl.textContent = ctx.formatMoney(change);
      changeEl.style.color = '#4caf50';
    } else {
      changeEl.textContent = ctx.formatMoney(Math.abs(change)) + ' (thiếu)';
      changeEl.style.color = '#f44336';
    }
  }
  
  // Format số với dấu phẩy
  ctx.formatNumber = function formatNumber(value) {
    const num = value.replace(/[^0-9]/g, '');
    if (!num) return '';
    return parseInt(num).toLocaleString('vi-VN');
  }
  
  // Xác nhận thanh toán
  ctx.confirmPayment = function confirmPayment() {
    const method = document.getElementById('selectedPaymentMethod').value;
    const cart = ctx.getActiveCart();
    const order = ctx.getActiveOrder();
    
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
      ctx.saveOrdersToStorage();
    }
    
    // Lưu phương thức thanh toán vào form
    const formInput = document.getElementById('phuongThucThanhToan');
    if (formInput) {
      formInput.value = method;
    }
    
    // Đóng modal
    ctx.closePaymentModal();
    
    // Submit form (tạo đơn ngay)
    ctx.submitOrder();
  }
  
  // In tạm tính (không lưu đơn)
  ctx.printTempBill = function printTempBill() {
    const cart = ctx.getActiveCart();
    const order = ctx.getActiveOrder();

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
        + '<td>' + ctx.escapeHtml(item.ten) + '</td>'
        + '<td style="text-align:center">' + (item.sl || 1) + '</td>'
        + '<td style="text-align:right">' + ctx.formatMoney(lineTotal) + '</td>'
        + '</tr>';
    });

    const now = new Date().toLocaleString('vi-VN');
    printArea.innerHTML = ''
      + '<div class="temp-bill">'
      + '<h2>' + ctx.escapeHtml(shopName) + '</h2>'
      + '<p class="temp-bill-meta">PHIẾU TẠM TÍNH</p>'
      + '<p class="temp-bill-meta">' + now + '</p>'
      + '<p>Khách: ' + ctx.escapeHtml(customerName)
      + (customerPhone ? ' · ' + ctx.escapeHtml(customerPhone) : '')
      + '</p>'
      + '<table><thead><tr><th>Món</th><th>SL</th><th>Thành tiền</th></tr></thead><tbody>'
      + rowsHtml
      + '</tbody></table>'
      + '<p class="temp-bill-total">Tổng: <strong>' + ctx.formatMoney(total) + '</strong></p>'
      + '<p class="temp-bill-note">* Chưa thanh toán — chỉ dùng tham khảo</p>'
      + '</div>';

    window.print();
  }

})();