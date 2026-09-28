import { useCallback, useEffect, useState } from 'react';
import type { CartItem, OrderTab } from '../types';

const STORAGE_KEY = 'order_tabs';

interface StoredState {
  orders: OrderTab[];
  activeOrderId: number | null;
}

function createEmptyOrder(index: number, defaultPaymentMethod: string): OrderTab {
  return {
    id: Date.now() + index,
    title: `Đơn hàng ${index + 1}`,
    cart: [],
    ten_khachhang: '',
    sdt_khachhang: '',
    phuong_thuc_thanhtoan: defaultPaymentMethod,
    created_at: new Date().toISOString(),
  };
}

function loadStored(): StoredState | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) {
      return null;
    }
    const data = JSON.parse(raw) as StoredState;
    return {
      orders: data.orders || [],
      activeOrderId: data.activeOrderId ?? null,
    };
  } catch {
    return null;
  }
}

function saveStored(orders: OrderTab[], activeOrderId: number | null) {
  localStorage.setItem(
    STORAGE_KEY,
    JSON.stringify({ orders, activeOrderId }),
  );
}

export function useOrderTabs(defaultPaymentMethod: string) {
  const [orders, setOrders] = useState<OrderTab[]>([]);
  const [activeOrderId, setActiveOrderId] = useState<number | null>(null);

  useEffect(() => {
    const stored = loadStored();
    if (stored && stored.orders.length > 0) {
      setOrders(stored.orders);
      setActiveOrderId(stored.activeOrderId ?? stored.orders[0]?.id ?? null);
      return;
    }
    const first = createEmptyOrder(0, defaultPaymentMethod);
    setOrders([first]);
    setActiveOrderId(first.id);
    saveStored([first], first.id);
  }, [defaultPaymentMethod]);

  const persist = useCallback((nextOrders: OrderTab[], nextActiveId: number | null) => {
    setOrders(nextOrders);
    setActiveOrderId(nextActiveId);
    saveStored(nextOrders, nextActiveId);
  }, []);

  const activeOrder = orders.find((order) => order.id === activeOrderId) ?? null;

  const updateActiveOrder = useCallback(
    (updater: (order: OrderTab) => OrderTab) => {
      if (activeOrderId == null) {
        return;
      }
      const nextOrders = orders.map((order) =>
        order.id === activeOrderId ? updater(order) : order,
      );
      persist(nextOrders, activeOrderId);
    },
    [activeOrderId, orders, persist],
  );

  const setActiveCart = useCallback(
    (cart: CartItem[]) => {
      updateActiveOrder((order) => ({ ...order, cart }));
    },
    [updateActiveOrder],
  );

  const createNewOrder = useCallback(() => {
    const order = createEmptyOrder(orders.length, defaultPaymentMethod);
    persist([...orders, order], order.id);
  }, [defaultPaymentMethod, orders, persist]);

  const switchOrder = useCallback(
    (orderId: number) => {
      if (orderId === activeOrderId) {
        return;
      }
      persist(orders, orderId);
    },
    [activeOrderId, orders, persist],
  );

  const closeOrder = useCallback(
    (orderId: number) => {
      if (orders.length <= 1) {
        alert('Phải có ít nhất một đơn hàng');
        return;
      }
      if (!confirm('Bạn có chắc chắn muốn đóng đơn hàng này?')) {
        return;
      }
      const nextOrders = orders.filter((order) => order.id !== orderId);
      const nextActive =
        activeOrderId === orderId ? nextOrders[0]?.id ?? null : activeOrderId;
      persist(nextOrders, nextActive);
    },
    [activeOrderId, orders, persist],
  );

  const removeSubmittedOrder = useCallback(
    (orderId: number) => {
      const nextOrders = orders.filter((order) => order.id !== orderId);
      if (nextOrders.length === 0) {
        const fresh = createEmptyOrder(0, defaultPaymentMethod);
        persist([fresh], fresh.id);
        return;
      }
      const nextActive =
        activeOrderId === orderId ? nextOrders[0].id : activeOrderId;
      persist(nextOrders, nextActive);
    },
    [activeOrderId, defaultPaymentMethod, orders, persist],
  );

  return {
    orders,
    activeOrder,
    activeOrderId,
    setActiveCart,
    updateActiveOrder,
    createNewOrder,
    switchOrder,
    closeOrder,
    removeSubmittedOrder,
  };
}
