import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import {
  fetchCreateContext,
  fetchProductChoices,
  fetchProductSizes,
  resolveConfig,
  searchCustomers,
  submitOrder,
} from '../api';
import { useOrderTabs } from '../hooks/useOrderTabs';
import type {
  CartChoice,
  CartItem,
  Category,
  Customer,
  Product,
  ProductModalState,
} from '../types';
import {
  calcCartTotal,
  cartItemKey,
  formatMoney,
  getRankLabel,
  normalizeCartItems,
  summarizeChoices,
} from '../utils';
import { CustomerFindModal } from './CustomerFindModal';
import { OptionsModal } from './OptionsModal';
import { PaymentModal } from './PaymentModal';

export function OrderCreateApp() {
  const config = useMemo(() => resolveConfig(), []);
  const {
    orders,
    activeOrder,
    setActiveCart,
    updateActiveOrder,
    createNewOrder,
    switchOrder: baseSwitchOrder,
    closeOrder,
    removeSubmittedOrder,
  } = useOrderTabs(config.defaultPaymentMethod);

  const [categories, setCategories] = useState<Category[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [activeCat, setActiveCat] = useState<number | null>(config.initialCat);
  const [searchProd, setSearchProd] = useState('');
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [flash] = useState<string | null>(window.ORDER_CREATE_FLASH?.message ?? null);

  const [optionsModal, setOptionsModal] = useState<ProductModalState | null>(null);
  const [paymentOpen, setPaymentOpen] = useState(false);
  const [customerFindOpen, setCustomerFindOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [savingPending, setSavingPending] = useState(false);

  const [matchedCustomer, setMatchedCustomer] = useState<Customer | null>(null);
  const [suggestions, setSuggestions] = useState<Customer[]>([]);
  const customerTimer = useRef<number | null>(null);
  const choicesCache = useRef<Record<number, ProductModalState['choices']>>({});
  const sizesCache = useRef<Record<number, ProductModalState['sizes']>>({});

  const loadContext = useCallback(
    async (cat: number | null) => {
      setLoading(true);
      setLoadError(null);
      try {
        const data = await fetchCreateContext(config, cat);
        setCategories(data.categories);
        setProducts(data.products);
        setActiveCat(data.active_cat);
      } catch (error) {
        setLoadError(error instanceof Error ? error.message : 'Không tải được dữ liệu.');
      } finally {
        setLoading(false);
      }
    },
    [config],
  );

  useEffect(() => {
    loadContext(config.initialCat);
  }, [config.initialCat, loadContext]);

  const filteredProducts = useMemo(() => {
    const keyword = searchProd.trim().toLowerCase();
    if (!keyword) {
      return products;
    }
    return products.filter((product) =>
      product.ten_san_pham.toLowerCase().includes(keyword),
    );
  }, [products, searchProd]);

  const cart = activeOrder?.cart ?? [];
  const cartTotal = calcCartTotal(cart);

  const switchOrder = useCallback(
    (orderId: number) => {
      baseSwitchOrder(orderId);
      setMatchedCustomer(null);
      setSuggestions([]);
    },
    [baseSwitchOrder],
  );

  const handleCategoryClick = (catId: number) => {
    if (catId === activeCat) {
      return;
    }
    const url = new URL(window.location.href);
    url.searchParams.set('cat', String(catId));
    window.history.replaceState({}, '', url.toString());
    loadContext(catId);
  };

  const handleProductClick = async (product: Product) => {
    const [choices, sizes] = await Promise.all([
      choicesCache.current[product.id]
        ? Promise.resolve(choicesCache.current[product.id])
        : fetchProductChoices(config, product.id).then((result) => {
            choicesCache.current[product.id] = result;
            return result;
          }),
      sizesCache.current[product.id]
        ? Promise.resolve(sizesCache.current[product.id])
        : fetchProductSizes(config, product.id).then((result) => {
            sizesCache.current[product.id] = result;
            return result;
          }),
    ]);

    setOptionsModal({
      productId: product.id,
      productName: product.ten_san_pham,
      productPrice: Number(product.gia_ban || 0),
      choices,
      sizes,
    });
  };

  const addToCart = (payload: {
    choices: CartChoice[];
    sizeId: number | null;
    sizeName: string;
    finalPrice: number;
  }) => {
    if (!optionsModal || !activeOrder) {
      return;
    }

    const newItem: CartItem = {
      id: optionsModal.productId,
      ten: optionsModal.productName,
      gia: payload.finalPrice,
      sl: 1,
      size: payload.sizeName,
      size_id: payload.sizeId,
      choices: payload.choices,
    };

    const key = cartItemKey(newItem);
    const nextCart = [...cart];
    const existingIndex = nextCart.findIndex((item) => cartItemKey(item) === key);
    if (existingIndex >= 0) {
      nextCart[existingIndex] = { ...nextCart[existingIndex], sl: nextCart[existingIndex].sl + 1 };
    } else {
      nextCart.push(newItem);
    }

    setActiveCart(nextCart);
    setOptionsModal(null);
  };

  const updateQty = (index: number, delta: number) => {
    const nextCart = [...cart];
    if (index < 0 || index >= nextCart.length) {
      return;
    }
    nextCart[index] = { ...nextCart[index], sl: nextCart[index].sl + delta };
    if (nextCart[index].sl <= 0) {
      nextCart.splice(index, 1);
    }
    setActiveCart(nextCart);
  };

  const removeItem = (index: number) => {
    const nextCart = [...cart];
    nextCart.splice(index, 1);
    setActiveCart(nextCart);
  };

  const selectCustomer = (customer: Customer) => {
    setMatchedCustomer(customer);
    updateActiveOrder((order) => ({
      ...order,
      ten_khachhang: customer.ten_khachhang || '',
      sdt_khachhang: customer.so_dien_thoai || '',
    }));
    setSuggestions([]);
    setCustomerFindOpen(false);
  };

  const scheduleCustomerSearch = (name: string, phone: string) => {
    if (customerTimer.current) {
      window.clearTimeout(customerTimer.current);
    }
    customerTimer.current = window.setTimeout(async () => {
      const query = (phone || name).trim();
      if (query.length < 2) {
        setSuggestions([]);
        if (!query) {
          setMatchedCustomer(null);
        }
        return;
      }
      const customers = await searchCustomers(config, query);
      if (!customers.length) {
        setSuggestions([]);
        setMatchedCustomer(null);
        return;
      }
      const exact = customers.find(
        (customer) =>
          (phone && customer.so_dien_thoai === phone.trim()) ||
          (name && customer.ten_khachhang === name.trim()),
      );
      setMatchedCustomer(exact ?? null);
      setSuggestions(customers);
    }, 300);
  };

  const handleSubmit = async (
    action: 'create_order' | 'save_pending_order',
    paymentMethod?: string,
  ) => {
    if (!activeOrder || cart.length === 0) {
      alert('Vui lòng chọn ít nhất một sản phẩm');
      return;
    }

    const isPending = action === 'save_pending_order';
    if (isPending) {
      setSavingPending(true);
    } else {
      setSubmitting(true);
    }

    try {
      const result = await submitOrder(
        config,
        {
          action,
          items: cart,
          ten_khachhang: activeOrder.ten_khachhang || 'Khách lẻ',
          sdt_khachhang: activeOrder.sdt_khachhang || '',
          phuong_thuc_thanhtoan: paymentMethod || activeOrder.phuong_thuc_thanhtoan,
        },
        normalizeCartItems(cart),
      );
      removeSubmittedOrder(activeOrder.id);
      window.location.href = result.redirect;
    } catch (error) {
      alert(error instanceof Error ? error.message : 'Có lỗi khi lưu đơn hàng.');
    } finally {
      setSubmitting(false);
      setSavingPending(false);
      setPaymentOpen(false);
    }
  };

  const printTempBill = () => {
    if (!activeOrder || cart.length === 0) {
      alert('Vui lòng chọn ít nhất một sản phẩm');
      return;
    }
    const printArea = document.getElementById('tempBillPrint');
    if (!printArea) {
      alert('Không tìm thấy vùng in.');
      return;
    }

    const rows = cart
      .map(
        (item: CartItem) =>
          `<tr><td>${item.ten}</td><td style="text-align:center">${item.sl}</td>` +
          `<td style="text-align:right">${formatMoney(item.gia * item.sl)}</td></tr>`,
      )
      .join('');

    printArea.innerHTML =
      `<div class="temp-bill"><h2>${config.shopName}</h2>` +
      `<p class="temp-bill-meta">PHIẾU TẠM TÍNH</p>` +
      `<p class="temp-bill-meta">${new Date().toLocaleString('vi-VN')}</p>` +
      `<p>Khách: ${activeOrder.ten_khachhang || 'Khách lẻ'}` +
      `${activeOrder.sdt_khachhang ? ` · ${activeOrder.sdt_khachhang}` : ''}</p>` +
      `<table><thead><tr><th>Món</th><th>SL</th><th>Thành tiền</th></tr></thead><tbody>${rows}</tbody></table>` +
      `<p class="temp-bill-total">Tổng: <strong>${formatMoney(cartTotal)}</strong></p>` +
      `<p class="temp-bill-note">* Chưa thanh toán — chỉ dùng tham khảo</p></div>`;

    window.print();
  };

  return (
    <>
      <div className="header-bar-taodon">
        <div className="left">
          <a href={`${config.basePath.replace(/\/?$/, '')}/don-hang`} className="btn-back">
            <i className="fa fa-arrow-left" /> Về danh sách đơn
          </a>
          <h1>
            <span className="ic">
              <i className="fas fa-plus-circle" />
            </span>{' '}
            TẠO ĐƠN HÀNG
          </h1>
        </div>
        <button
          className="btn btn-primary"
          type="button"
          id="btnAddNewOrder"
          style={{ padding: '10px 20px', display: 'flex', alignItems: 'center', gap: 8 }}
          onClick={createNewOrder}
        >
          <i className="fas fa-plus" /> Thêm đơn hàng
        </button>
      </div>

      {flash && (
        <div className="alert alert-danger" style={{ margin: '10px 20px 0 20px', flexShrink: 0 }}>
          {flash}
        </div>
      )}

      {loadError && (
        <div className="alert alert-danger" style={{ margin: '10px 20px 0 20px' }}>
          {loadError}
        </div>
      )}

      <div className="order-cards-container">
        <div className="order-cards" id="orderTabs">
          {orders.map((order, index) => {
            const itemCount = order.cart.reduce((sum: number, item: CartItem) => sum + item.sl, 0);
            const total = calcCartTotal(order.cart);
            let title =
              order.ten_khachhang &&
              order.ten_khachhang.trim() &&
              order.ten_khachhang !== 'Khách lẻ'
                ? order.ten_khachhang
                : `Đơn hàng ${index + 1}`;
            if (title.length > 18) {
              title = `${title.substring(0, 15)}...`;
            }
            const isActive = order.id === activeOrder?.id;
            return (
              <div
                key={order.id}
                className={`order-tab ${isActive ? 'active' : ''}`}
                onClick={() => switchOrder(order.id)}
              >
                <div className="order-tab-header">
                  <span className="order-tab-title">{title}</span>
                  {orders.length > 1 && (
                    <span
                      className="order-tab-close"
                      title="Đóng đơn hàng"
                      onClick={(event) => {
                        event.stopPropagation();
                        closeOrder(order.id);
                      }}
                    >
                      ×
                    </span>
                  )}
                </div>
                <div className="order-tab-info">
                  {order.sdt_khachhang && (
                    <div className="order-tab-customer">
                      <i className="fas fa-phone" /> {order.sdt_khachhang}
                    </div>
                  )}
                  {itemCount > 0 ? (
                    <span className="order-tab-badge">{itemCount} món</span>
                  ) : (
                    <span style={{ fontSize: 11, color: '#94a3b8' }}>Chưa có sản phẩm</span>
                  )}
                  {total > 0 && <div className="order-tab-total">{formatMoney(total)}</div>}
                </div>
              </div>
            );
          })}
        </div>
      </div>

      <div className="order-layout">
        <div className="panel panel-categories">
          <div className="panel-header">
            <i className="fas fa-list" />
            <strong>Nhóm sản phẩm</strong>
          </div>
          <div className="cat-list">
            {categories.map((cat) => (
              <div
                key={cat.id}
                className={`cat-item ${activeCat === cat.id ? 'active' : ''}`}
                onClick={() => handleCategoryClick(cat.id)}
              >
                {cat.ten_danh_muc}
              </div>
            ))}
            {!loading && categories.length === 0 && (
              <div className="empty-state">Chưa có nhóm</div>
            )}
          </div>
        </div>

        <div className="panel panel-products">
          <div className="panel-header">
            <i className="fas fa-box" />
            <strong>Sản phẩm</strong>
            <input
              type="text"
              id="searchProd"
              className="search-input"
              placeholder="Tìm sản phẩm..."
              value={searchProd}
              onChange={(event) => setSearchProd(event.target.value)}
            />
          </div>
          <div className="prod-grid" id="prodGrid">
            {loading ? (
              <div className="empty-state">Đang tải sản phẩm...</div>
            ) : filteredProducts.length === 0 ? (
              <div className="empty-state">Không có sản phẩm.</div>
            ) : (
              filteredProducts.map((product) => (
                <div
                  key={product.id}
                  className="prod-card"
                  data-id={product.id}
                  onClick={() => handleProductClick(product)}
                >
                  {product.anh_url && (
                    <div className="prod-image">
                      <img
                        src={`${config.basePath.replace(/\/?$/, '')}/${product.anh_url}`}
                        alt={product.ten_san_pham}
                      />
                    </div>
                  )}
                  <div className="prod-name">{product.ten_san_pham}</div>
                  <div className="prod-price">{formatMoney(Number(product.gia_ban || 0))}</div>
                </div>
              ))
            )}
          </div>
        </div>

        <div className="panel panel-cart">
          <div className="customer-info">
            <div className="panel-header customer-panel-header">
              <div className="customer-panel-title">
                <i className="fas fa-user" />
                <strong>Thông tin khách</strong>
              </div>
              <button
                type="button"
                className="btn-find-customer"
                id="btnFindCustomer"
                onClick={() => setCustomerFindOpen(true)}
              >
                <i className="fas fa-search" /> Tìm khách
              </button>
            </div>
            {matchedCustomer &&
              ((matchedCustomer.tong_donhang ?? 0) > 0 ||
                (matchedCustomer.tong_tien_da_chi ?? 0) > 0) && (
                <div
                  id="customerMatchedBadge"
                  className="customer-matched-badge"
                  style={{ display: 'flex' }}
                >
                  <i className="fas fa-star" />
                  <span>Khách lâu dài</span>
                  <span className="customer-order-count" id="customerOrderCount">
                    {parseInt(String(matchedCustomer.tong_donhang), 10) || 0} đơn trước
                  </span>
                </div>
              )}
            <div className="customer-inputs customer-search-wrapper">
              <input
                id="tenKhachhang"
                placeholder="Tên khách (mặc định Khách lẻ)"
                className="input-field"
                autoComplete="off"
                value={activeOrder?.ten_khachhang ?? ''}
                onChange={(event) => {
                  const value = event.target.value;
                  updateActiveOrder((order) => ({ ...order, ten_khachhang: value }));
                  scheduleCustomerSearch(value, activeOrder?.sdt_khachhang ?? '');
                }}
              />
              <input
                id="sdtKhachhang"
                placeholder="SĐT"
                className="input-field input-phone"
                autoComplete="off"
                value={activeOrder?.sdt_khachhang ?? ''}
                onChange={(event) => {
                  const value = event.target.value;
                  updateActiveOrder((order) => ({ ...order, sdt_khachhang: value }));
                  scheduleCustomerSearch(activeOrder?.ten_khachhang ?? '', value);
                }}
              />
              {suggestions.length > 0 && (
                <div id="customerSuggestions" className="autocomplete-suggestions" style={{ display: 'block' }}>
                  {suggestions.map((customer) => (
                    <button
                      key={customer.id}
                      type="button"
                      className="autocomplete-suggestion"
                      style={{ width: '100%', border: 'none', background: 'transparent', textAlign: 'left' }}
                      onClick={() => selectCustomer(customer)}
                    >
                      <div className="suggestion-left">
                        <div className="suggestion-name">{customer.ten_khachhang}</div>
                        <div className="suggestion-phone">
                          {customer.so_dien_thoai || 'Không có SĐT'}
                        </div>
                      </div>
                      <div className="suggestion-right">
                        <span className={`suggestion-tier rank-${customer.loai_thanh_vien || 'thuong'}`}>
                          {getRankLabel(customer.loai_thanh_vien)}
                        </span>
                      </div>
                    </button>
                  ))}
                </div>
              )}
            </div>
          </div>

          <div className="cart" id="cart">
            {cart.length === 0 ? (
              <div
                className="empty-state"
                style={{ padding: '40px 20px', textAlign: 'center', color: '#94a3b8' }}
              >
                <i
                  className="fas fa-shopping-cart"
                  style={{
                    fontSize: 32,
                    opacity: 0.3,
                    display: 'block',
                    marginBottom: 12,
                  }}
                />
                Giỏ hàng trống
              </div>
            ) : (
              cart.map((item: CartItem, index: number) => {
                const choiceParts = summarizeChoices(item.choices);
                return (
                  <div key={`${cartItemKey(item)}-${index}`} className="cart-item">
                    <div style={{ flex: 1, minWidth: 0 }}>
                      <div
                        style={{
                          fontWeight: 600,
                          fontSize: 14,
                          color: '#22334b',
                          marginBottom: 4,
                        }}
                      >
                        {item.ten}
                      </div>
                      {choiceParts.length > 0 && (
                        <div style={{ fontSize: 12, color: '#64748b', marginTop: 6, lineHeight: 1.4 }}>
                          {choiceParts.join(' | ')}
                        </div>
                      )}
                      <div style={{ fontSize: 12, color: '#94a3b8', marginTop: 6 }}>
                        {formatMoney(item.gia)} × {item.sl}
                      </div>
                    </div>
                    <div
                      style={{
                        textAlign: 'right',
                        flexShrink: 0,
                        display: 'flex',
                        flexDirection: 'column',
                        alignItems: 'flex-end',
                        gap: 8,
                      }}
                    >
                      <div style={{ fontWeight: 700, color: '#059669', fontSize: 15 }}>
                        {formatMoney(item.gia * item.sl)}
                      </div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                        <div className="qty">
                          <button type="button" onClick={() => updateQty(index, -1)}>
                            -
                          </button>
                          <span
                            style={{
                              padding: '0 8px',
                              fontWeight: 600,
                              minWidth: 24,
                              textAlign: 'center',
                            }}
                          >
                            {item.sl}
                          </span>
                          <button type="button" onClick={() => updateQty(index, 1)}>
                            +
                          </button>
                        </div>
                        <button
                          type="button"
                          onClick={() => removeItem(index)}
                          style={{
                            width: 28,
                            height: 28,
                            border: '1.5px solid #f44336',
                            background: '#fff',
                            color: '#f44336',
                            borderRadius: 6,
                            cursor: 'pointer',
                          }}
                          title="Xóa"
                        >
                          ×
                        </button>
                      </div>
                    </div>
                  </div>
                );
              })
            )}
          </div>

          <div className="sum">
            <div className="total-row">
              <span>Tổng tiền:</span>
              <b id="total">{formatMoney(cartTotal)}</b>
            </div>
            <div className="cart-actions">
              <button
                className="btn btn-secondary btn-save-order"
                type="button"
                id="btnLuuDon"
                disabled={savingPending}
                onClick={() => handleSubmit('save_pending_order')}
              >
                <span className="btn-text">
                  <i className="fas fa-save" /> {savingPending ? 'Đang lưu...' : 'Lưu đơn'}
                </span>
              </button>
              <button
                className="btn btn-secondary"
                type="button"
                onClick={printTempBill}
              >
                <i className="fas fa-print" /> In tạm tính
              </button>
              <button
                className="btn btn-primary btn-payment"
                type="button"
                id="btnThanhToan"
                disabled={submitting}
                onClick={() => {
                  if (cart.length === 0) {
                    alert('Vui lòng chọn ít nhất một sản phẩm');
                    return;
                  }
                  setPaymentOpen(true);
                }}
              >
                <span className="btn-text">{submitting ? 'Đang xử lý...' : 'Thanh toán'}</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      {optionsModal && (
        <OptionsModal
          key={optionsModal.productId}
          state={optionsModal}
          onClose={() => setOptionsModal(null)}
          onApply={addToCart}
        />
      )}

      {paymentOpen && (
        <PaymentModal
          cart={cart}
          submitting={submitting}
          onClose={() => setPaymentOpen(false)}
          onConfirm={(method) => {
            updateActiveOrder((order) => ({ ...order, phuong_thuc_thanhtoan: method }));
            handleSubmit('create_order', method);
          }}
        />
      )}

      <CustomerFindModal
        config={config}
        open={customerFindOpen}
        onClose={() => setCustomerFindOpen(false)}
        onSelect={selectCustomer}
      />

      <div id="tempBillPrint" className="temp-bill-print" aria-hidden="true" />
    </>
  );
}
