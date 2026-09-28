import { useEffect, useState } from 'react';
import type { Customer, OrderCreateConfig } from '../types';
import { searchCustomers } from '../api';
import { getRankLabel } from '../utils';

interface CustomerFindModalProps {
  config: OrderCreateConfig;
  open: boolean;
  onClose: () => void;
  onSelect: (customer: Customer) => void;
}

export function CustomerFindModal({ config, open, onClose, onSelect }: CustomerFindModalProps) {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<Customer[]>([]);

  useEffect(() => {
    if (!open) {
      setQuery('');
      setResults([]);
    }
  }, [open]);

  useEffect(() => {
    if (!open) {
      return;
    }
    const timer = window.setTimeout(async () => {
      if (query.trim().length < 2) {
        setResults([]);
        return;
      }
      setResults(await searchCustomers(config, query));
    }, 300);
    return () => window.clearTimeout(timer);
  }, [config, open, query]);

  if (!open) {
    return null;
  }

  return (
    <div
      className="customer-find-modal active"
      onClick={(event) => {
        if (event.target === event.currentTarget) {
          onClose();
        }
      }}
    >
      <div className="customer-find-dialog">
        <div className="customer-find-header">
          <h3>
            <i className="fas fa-users" /> Tìm khách hàng
          </h3>
          <button type="button" className="customer-find-close" onClick={onClose}>
            &times;
          </button>
        </div>
        <div className="customer-find-body">
          <input
            type="text"
            id="customerFindInput"
            className="input-field"
            placeholder="Nhập tên hoặc SĐT..."
            autoComplete="off"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
          />
          <div id="customerFindResults" className="customer-find-results">
            {results.length === 0 ? (
              <div className="customer-find-empty">Nhập tên hoặc SĐT để tìm khách</div>
            ) : (
              results.map((customer) => (
                <button
                  key={customer.id}
                  type="button"
                  className="customer-find-item"
                  style={{
                    width: '100%',
                    border: 'none',
                    background: 'transparent',
                    textAlign: 'left',
                    cursor: 'pointer',
                  }}
                  onClick={() => onSelect(customer)}
                >
                  <div>
                    <div className="customer-find-item-name">{customer.ten_khachhang}</div>
                    <div className="customer-find-item-phone">
                      {customer.so_dien_thoai || 'Không có SĐT'} ·{' '}
                      {parseInt(String(customer.tong_donhang), 10) || 0} đơn
                    </div>
                  </div>
                  <span className={`suggestion-tier rank-${customer.loai_thanh_vien || 'thuong'}`}>
                    {getRankLabel(customer.loai_thanh_vien)}
                  </span>
                </button>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
