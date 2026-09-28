import type {
  CartItem,
  ChoiceGroup,
  CreateContextResponse,
  Customer,
  OrderCreateConfig,
  ProductSize,
} from './types';

function apiBase(config: OrderCreateConfig): string {
  const base = config.basePath || '/';
  return base.endsWith('/') ? base : `${base}/`;
}

async function parseJson<T>(response: Response): Promise<T> {
  const text = await response.text();
  if (text.trim().startsWith('<')) {
    throw new Error('Server trả về HTML thay vì JSON.');
  }
  return JSON.parse(text) as T;
}

export function resolveConfig(): OrderCreateConfig {
  const fromWindow = window.ORDER_CREATE_CONFIG ?? {};
  const basePath = fromWindow.basePath || window.APP_BASE_PATH || '/';
  return {
    basePath,
    defaultPaymentMethod: fromWindow.defaultPaymentMethod || 'tien_mat',
    shopName: fromWindow.shopName || 'CHILL GUYS',
    initialCat: fromWindow.initialCat ?? null,
    orderListUrl: fromWindow.orderListUrl || `${basePath.replace(/\/?$/, '')}/don-hang`,
  };
}

export async function fetchCreateContext(
  config: OrderCreateConfig,
  cat: number | null,
): Promise<CreateContextResponse> {
  const params = new URLSearchParams();
  if (cat != null) {
    params.set('cat', String(cat));
  }
  const qs = params.toString();
  const url = `${apiBase(config)}api/don-hang/create-data${qs ? `?${qs}` : ''}`;
  const response = await fetch(url, { credentials: 'same-origin' });
  const data = await parseJson<CreateContextResponse>(response);
  if (!data.success) {
    throw new Error(data.error || 'Không tải được dữ liệu tạo đơn.');
  }
  return data;
}

export async function fetchProductChoices(
  config: OrderCreateConfig,
  productId: number,
): Promise<ChoiceGroup[]> {
  const url = `${apiBase(config)}api/product-choices?id=${productId}`;
  const response = await fetch(url, { credentials: 'same-origin' });
  const data = await parseJson<{ success: boolean; choices?: ChoiceGroup[] }>(response);
  return data.success ? data.choices ?? [] : [];
}

export async function fetchProductSizes(
  config: OrderCreateConfig,
  productId: number,
): Promise<ProductSize[]> {
  const url = `${apiBase(config)}api/product-sizes?product_id=${productId}`;
  const response = await fetch(url, { credentials: 'same-origin' });
  const data = await parseJson<{ success: boolean; has_sizes?: boolean; sizes?: ProductSize[] }>(
    response,
  );
  return data.success && data.has_sizes ? data.sizes ?? [] : [];
}

export async function searchCustomers(
  config: OrderCreateConfig,
  query: string,
): Promise<Customer[]> {
  if (!query || query.trim().length < 2) {
    return [];
  }
  const url = `${apiBase(config)}api/search-customer?q=${encodeURIComponent(query.trim())}`;
  const response = await fetch(url, { credentials: 'same-origin' });
  const data = await parseJson<{ success: boolean; customers?: Customer[] }>(response);
  return data.success ? data.customers ?? [] : [];
}

export interface SubmitOrderPayload {
  action: 'create_order' | 'save_pending_order';
  items: CartItem[];
  ten_khachhang: string;
  sdt_khachhang: string;
  phuong_thuc_thanhtoan?: string;
}

export async function submitOrder(
  config: OrderCreateConfig,
  payload: SubmitOrderPayload,
  normalizedItems: CartItem[],
): Promise<{ redirect: string; message: string }> {
  const params = new URLSearchParams();
  params.append('action', payload.action);
  params.append('items', JSON.stringify(normalizedItems));
  params.append('ten_khachhang', payload.ten_khachhang || 'Khách lẻ');
  params.append('sdt_khachhang', payload.sdt_khachhang || '');
  if (payload.phuong_thuc_thanhtoan) {
    params.append('phuong_thuc_thanhtoan', payload.phuong_thuc_thanhtoan);
  }

  const response = await fetch(`${apiBase(config)}api/don-hang/create`, {
    method: 'POST',
    body: params,
    credentials: 'same-origin',
    headers: {
      'X-Requested-With': 'XMLHttpRequest',
      Accept: 'application/json',
      'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
    },
  });

  const data = await parseJson<{
    success: boolean;
    error?: string;
    message?: string;
    redirect?: string;
  }>(response);

  if (!response.ok || !data.success) {
    throw new Error(data.error || data.message || 'Có lỗi khi lưu đơn hàng.');
  }

  return {
    redirect: data.redirect || config.orderListUrl,
    message: data.message || 'Thành công.',
  };
}
