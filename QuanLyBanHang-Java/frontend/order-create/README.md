# React pilot — Tạo đơn hàng

Ứng dụng React thay thế `js/orders/create.js` tại `/don-hang/create`.

## Cấu trúc

- `src/` — mã nguồn TypeScript + React
- Build ra: `src/main/webapp/assets/react/order-create/order-create.js`

## Build

```powershell
cd frontend/order-create
.\build.ps1
# hoặc: npm run build
```

Sau khi build thành công, reload `/don-hang/create` — JSP tự dùng React khi file bundle tồn tại.

## Chế độ

| URL | Hành vi |
|-----|---------|
| `/don-hang/create` | React nếu đã build bundle, ngược lại legacy JSP + `create.js` |
| `/don-hang/create?legacy=1` | Luôn dùng legacy |

## API sử dụng

- `GET /api/don-hang/create-data?cat=` — danh mục + sản phẩm (đổi tab không reload trang)
- `GET /api/product-choices`, `/api/product-sizes`
- `GET /api/search-customer`
- `POST /api/don-hang/create` — lưu / thanh toán đơn

## Dev local (tùy chọn)

```powershell
npm run dev
```

Mở Vite dev server; cần proxy session Tomcat nếu test API thật.
