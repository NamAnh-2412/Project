import { useMemo, useState } from 'react';
import { previewVoucher } from '../api';
import { calcCartTotal, formatMoney, formatNumber } from '../utils';
import type { CartItem, OrderCreateConfig } from '../types';

interface PaymentModalProps {
  config: OrderCreateConfig;
  cart: CartItem[];
  onClose: () => void;
  onConfirm: (method: string, voucherCode: string) => void;
  submitting: boolean;
}

const METHODS = [
  { id: 'tien_mat', label: 'Tiền mặt', icon: 'fa-money-bill-wave' },
  { id: 'chuyen_khoan', label: 'Chuyển khoản', icon: 'fa-university' },
  { id: 'the', label: 'Thẻ', icon: 'fa-credit-card' },
  { id: 'app_online', label: 'App online', icon: 'fa-mobile-alt' },
];

const QUICK_AMOUNTS = [10000, 20000, 50000, 100000, 200000, 500000];

export function PaymentModal({ config, cart, onClose, onConfirm, submitting }: PaymentModalProps) {
  const [method, setMethod] = useState('tien_mat');
  const [customerPaid, setCustomerPaid] = useState('');
  const [voucherInput, setVoucherInput] = useState('');
  const [appliedCode, setAppliedCode] = useState('');
  const [discount, setDiscount] = useState(0);
  const [voucherError, setVoucherError] = useState('');
  const [checkingVoucher, setCheckingVoucher] = useState(false);
  const total = useMemo(() => calcCartTotal(cart), [cart]);
  const payable = Math.max(0, total - discount);
  const paidAmount = parseFloat(customerPaid.replace(/[^0-9]/g, '')) || 0;
  const change = paidAmount - payable;

  async function applyCode() {
    const code = voucherInput.trim();
    if (!code) {
      setAppliedCode('');
      setDiscount(0);
      setVoucherError('');
      return;
    }
    setCheckingVoucher(true);
    try {
      const quote = await previewVoucher(config, code, total);
      setAppliedCode(quote.ma);
      setVoucherInput(quote.ma);
      setDiscount(quote.giam);
      setVoucherError('');
    } catch (error) {
      setAppliedCode('');
      setDiscount(0);
      setVoucherError(error instanceof Error ? error.message : 'Mã giảm giá không dùng được.');
    } finally {
      setCheckingVoucher(false);
    }
  }

  function handleConfirm() {
    if (method === 'tien_mat' && paidAmount < payable) {
      alert('Số tiền khách trả không đủ!');
      return;
    }
    onConfirm(method, appliedCode);
  }

  return (
    <div className="payment-modal active">
      <div className="payment-dialog">
        <div className="payment-header">
          <h2>Thanh toán</h2>
          <button className="payment-close" type="button" onClick={onClose}>
            ×
          </button>
        </div>
        <div className="payment-body">
          <div className="payment-left">
            <div className="payment-methods">
              {METHODS.map((item) => (
                <button
                  key={item.id}
                  type="button"
                  className={`payment-method ${method === item.id ? 'active' : ''}`}
                  data-method={item.id}
                  onClick={() => setMethod(item.id)}
                >
                  <i className={`fas ${item.icon}`} /> {item.label}
                </button>
              ))}
            </div>
          </div>
          <div className="payment-right">
            <div className="payment-info">
              <div className="payment-row">
                <span>Tạm tính</span>
                <strong>{formatMoney(total)}</strong>
              </div>
              <div className="payment-row" style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                <input
                  type="text"
                  value={voucherInput}
                  placeholder="Mã giảm giá"
                  onChange={(event) => setVoucherInput(event.target.value.toUpperCase())}
                  style={{ flex: 1, padding: 8, border: '1px solid #d2d8ea', borderRadius: 6 }}
                />
                <button type="button" className="btn btn-secondary" disabled={checkingVoucher} onClick={applyCode}>
                  {checkingVoucher ? '...' : 'Áp dụng'}
                </button>
              </div>
              {voucherError && (
                <div className="payment-row" style={{ color: '#f44336' }}>{voucherError}</div>
              )}
              {discount > 0 && (
                <div className="payment-row">
                  <span>Giảm {appliedCode}</span>
                  <strong>-{formatMoney(discount)}</strong>
                </div>
              )}
              <div className="payment-row">
                <span>Tổng tiền (1)</span>
                <strong id="paymentTotal" style={{ color: '#f44336' }}>
                  {formatMoney(payable)}
                </strong>
              </div>
              {method === 'tien_mat' && (
                <>
                  <div className="payment-row" id="customerPaidRow" style={{ display: 'flex' }}>
                    <label htmlFor="customerPaid">Khách trả *</label>
                    <input
                      type="text"
                      id="customerPaid"
                      value={customerPaid}
                      readOnly
                      placeholder="0"
                      style={{
                        textAlign: 'right',
                        fontSize: 18,
                        fontWeight: 600,
                        padding: 8,
                        border: '1px solid #d2d8ea',
                        borderRadius: 6,
                        width: 150,
                      }}
                    />
                  </div>
                  <div className="payment-row" id="changeRow" style={{ display: 'flex' }}>
                    <span>Tiền thừa</span>
                    <strong
                      id="paymentChange"
                      style={{ color: change >= 0 ? '#4caf50' : '#f44336' }}
                    >
                      {change >= 0
                        ? formatMoney(change)
                        : `${formatMoney(Math.abs(change))} (thiếu)`}
                    </strong>
                  </div>
                </>
              )}
            </div>
            {method === 'tien_mat' && (
              <div className="payment-numpad" id="paymentNumpad" style={{ display: 'block' }}>
                <div className="numpad-grid">
                  {['7', '8', '9', '4', '5', '6', '1', '2', '3', '0', '00', '000'].map((key) => (
                    <button
                      key={key}
                      type="button"
                      className="numpad-btn"
                      onClick={() =>
                        setCustomerPaid(formatNumber(customerPaid.replace(/[^0-9]/g, '') + key))
                      }
                    >
                      {key}
                    </button>
                  ))}
                  <button
                    type="button"
                    className="numpad-btn numpad-clear"
                    onClick={() => setCustomerPaid('')}
                  >
                    C
                  </button>
                </div>
                <div className="quick-amounts">
                  {QUICK_AMOUNTS.map((amount) => (
                    <button
                      key={amount}
                      type="button"
                      className="quick-btn"
                      onClick={() => setCustomerPaid(formatNumber(String(amount)))}
                    >
                      {amount.toLocaleString('vi-VN')}
                    </button>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>
        <div className="payment-footer">
          <button type="button" className="btn btn-secondary" onClick={onClose}>
            ĐÓNG
          </button>
          <button
            type="button"
            className="btn btn-primary"
            id="btnConfirmPayment"
            disabled={submitting}
            onClick={handleConfirm}
          >
            <span className="btn-text">{submitting ? 'Đang xử lý...' : 'XÁC NHẬN'}</span>
          </button>
        </div>
        <input type="hidden" id="selectedPaymentMethod" value={method} />
      </div>
    </div>
  );
}
