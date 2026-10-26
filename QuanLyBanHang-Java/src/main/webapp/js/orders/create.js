(function () {
  'use strict';
  window.qlbhPos = {
    orders: [],
    activeOrderId: null,
    currentProductId: null,
    currentProductPrice: null,
    productChoices: {},
    productSizes: {},
    matchedCustomer: null,
    customerSearchTimer: null,
    displayTimer: null,
    appliedVoucher: { code: '', amount: 0 }
  };
})();