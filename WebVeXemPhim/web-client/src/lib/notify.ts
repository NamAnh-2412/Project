import { toast } from "sonner";

const TEXT: Record<string, string> = {
  SEAT_TAKEN: "Ghế này vừa có người chọn. Vui lòng chọn ghế khác!",
  HOLD_EXPIRED: "Thời gian giữ ghế đã hết. Vui lòng chọn lại ghế!",
  SHOWTIME_CLOSED: "Suất chiếu này đã đóng bán vé!",
  SHOWTIME_ENDED: "Suất chiếu này đã đóng bán vé!",
  CONCESSION_HIDDEN: "Sản phẩm bắp nước hiện không còn bán!",
  VOUCHER_INVALID: "Mã giảm giá không hợp lệ hoặc đã hết hạn!",
  VOUCHER_LIMIT_REACHED: "Mã giảm giá đã hết lượt sử dụng!",
  ALREADY_CHECKED_IN: "CẢNH BÁO: Vé này đã soát vào rạp trước đó!",
  CHECKED_IN_BLOCK: "Vé đã soát, không hủy được",
  TICKET_CANCELLED: "Vé đã hủy",
  NOT_ONLINE: "Chỉ hủy vé đặt online",
  TOO_LATE: "Chỉ hủy trước giờ chiếu 30 phút",
  NOT_PAID: "Vé chưa thanh toán",
  SHOWTIME_OVERLAP: "Suất chồng giờ trong cùng phòng",
  HAS_TICKETS: "Suất đã bán, không sửa hoặc hủy được",
  HAS_HOLDS: "Suất đang có ghế giữ, không sửa hoặc hủy được",
  USERNAME_TAKEN: "Tên đăng nhập đã có người dùng",
  EMAIL_TAKEN: "Email đã được dùng",
  AUTH_FAILED: "Sai tài khoản hoặc mật khẩu",
  PAYMENT_NOT_CONFIGURED: "Cổng thanh toán chưa được cấu hình. Dùng thanh toán giả lập.",
  USE_MOMO: "Vé online thanh toán bằng MoMo",
};

export function errorText(e: unknown, fallback: string) {
  const body = (e as { response?: { data?: { message?: string; code?: string } } })?.response?.data;
  if (body?.code && TEXT[body.code]) return TEXT[body.code];
  return body?.message || fallback;
}

export function notifyError(e: unknown, fallback: string) {
  toast.error(errorText(e, fallback));
}

export function notifyOk(message: string) {
  toast.success(message);
}
