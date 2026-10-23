export function formatVnd(n?: number | string | null) {
  return `${Number(n || 0).toLocaleString("vi-VN")}đ`;
}
