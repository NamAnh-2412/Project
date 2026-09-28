export interface OrderCreateConfig {
  basePath: string;
  defaultPaymentMethod: string;
  shopName: string;
  initialCat: number | null;
  orderListUrl: string;
}

export interface Category {
  id: number;
  ten_danh_muc: string;
}

export interface Product {
  id: number;
  ten_san_pham: string;
  gia_ban: number;
  anh_url?: string | null;
  danh_muc_id?: number;
}

export interface ChoiceOption {
  id: number;
  ten_lua_chon: string;
  gia_tang_them: number;
  thu_tu?: number;
}

export interface ChoiceGroup {
  id: number;
  ten_nhom: string;
  mota?: string;
  bat_buoc: boolean;
  lua_chon: ChoiceOption[];
}

export interface ProductSize {
  id: number;
  ten_size: string;
  gia: number;
  dang_hoat_dong?: number;
}

export interface CartChoice {
  nhom_id: number;
  nhom_ten: string;
  lua_chon_id: number;
  lua_chon_ten: string;
  gia_tang_them: number;
  so_luong?: number;
}

export interface CartItem {
  id: number;
  ten: string;
  gia: number;
  sl: number;
  size: string;
  size_id: number | null;
  choices: CartChoice[];
}

export interface OrderTab {
  id: number;
  title: string;
  cart: CartItem[];
  ten_khachhang: string;
  sdt_khachhang: string;
  phuong_thuc_thanhtoan: string;
  created_at: string;
}

export interface Customer {
  id: number;
  ten_khachhang: string;
  so_dien_thoai: string;
  loai_thanh_vien?: string;
  tong_donhang?: number;
  tong_tien_da_chi?: number;
}

export interface CreateContextResponse {
  success: boolean;
  categories: Category[];
  active_cat: number | null;
  products: Product[];
  default_payment_method: string;
  payment_options: Record<string, string>;
  error?: string;
}

export interface ProductModalState {
  productId: number;
  productName: string;
  productPrice: number;
  choices: ChoiceGroup[];
  sizes: ProductSize[];
}

declare global {
  interface Window {
    ORDER_CREATE_CONFIG?: Partial<OrderCreateConfig>;
    ORDER_CREATE_FLASH?: { type: string; message: string };
    APP_BASE_PATH?: string;
  }
}

export {};
