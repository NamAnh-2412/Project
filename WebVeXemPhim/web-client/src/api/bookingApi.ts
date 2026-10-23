import api from "./client";
import type { HoldResponse, PosShift, Reservation, RevenueReport, Review, SeatMap, VoucherOffer } from "./types";

export async function fetchSeatMap(showtimeId: number) {
  const { data } = await api.get<SeatMap>(`/api/bookings/showtimes/${showtimeId}/seat-map`);
  return data;
}

export async function holdSeats(body: {
  showtimeId: number;
  seatIds: number[];
  channel: "ONLINE" | "POS";
  guestName?: string;
}) {
  const { data } = await api.post<HoldResponse>("/api/bookings/hold", body);
  return data;
}

export async function fetchAvailableVouchers(orderAmount: number) {
  const { data } = await api.get<VoucherOffer[]>("/api/vouchers/available", {
    params: { orderAmount },
  });
  return data;
}

export async function fetchReviews(movieId: number) {
  const { data } = await api.get<Review[]>(`/api/movies/${movieId}/reviews`);
  return data;
}

export async function saveReview(movieId: number, rating: number, comment: string) {
  const { data } = await api.post<Review>(`/api/movies/${movieId}/reviews`, { rating, comment });
  return data;
}

export async function fetchCurrentShift() {
  const { data } = await api.get<PosShift | null>("/api/pos/shifts/current");
  return data;
}

export async function openShift(openingCash: number) {
  const { data } = await api.post<PosShift>("/api/pos/shifts/open", { openingCash });
  return data;
}

export async function closeShift(actualCash: number) {
  const { data } = await api.post<PosShift>("/api/pos/shifts/close", { actualCash });
  return data;
}

export async function sellSnacks(items: { productId: number; qty: number }[]) {
  const { data } = await api.post<{ id: number; totalAmount: number; detail: string }>("/api/pos/snacks", { items });
  return data;
}

export async function applyVoucher(id: number, code: string) {
  const { data } = await api.post<Reservation>(`/api/bookings/${id}/apply-voucher`, { code });
  return data;
}

export async function fetchPaymentOptions() {
  const { data } = await api.get<{ momo: boolean }>("/api/payments/options");
  return data;
}

export async function startMomo(id: number) {
  const { data } = await api.post<{ checkoutUrl: string }>(`/api/bookings/${id}/momo`);
  return data;
}

export async function createPaymentLink(id: number) {
  const { data } = await api.post<{
    paymentId: number;
    orderCode: number;
    amount: number;
    checkoutUrl: string;
    qrCode: string;
  }>(`/api/bookings/${id}/create-payment-link`);
  return data;
}

export async function fetchMockQr(id: number) {
  const { data } = await api.get<Blob>(`/api/bookings/${id}/mock-qr`, { responseType: "blob" });
  return data;
}

export async function confirmOnline(id: number) {
  const { data } = await api.post<Reservation>(`/api/bookings/${id}/confirm`, {
    paymentMethod: "ONLINE_MOCK",
  });
  return data;
}

export async function confirmCash(id: number) {
  const { data } = await api.post<Reservation>(`/api/pos/bookings/${id}/confirm-cash`, {});
  return data;
}

export async function cancelHold(id: number) {
  await api.delete(`/api/bookings/${id}`);
}

export async function cancelOnlineTickets(id: number) {
  const { data } = await api.post<Reservation>(`/api/bookings/${id}/cancel-tickets`);
  return data;
}

export type StaffNotice = {
  id: number;
  title: string;
  body: string;
  reservationId?: number | null;
  createdAt: string;
};

export async function fetchNotices() {
  const { data } = await api.get<StaffNotice[]>("/api/notices");
  return data;
}

export async function myTickets() {
  const { data } = await api.get<Reservation[]>("/api/bookings/my");
  return data;
}

export async function getBooking(id: number) {
  const { data } = await api.get<Reservation>(`/api/bookings/${id}`);
  return data;
}

export async function setBookingConcessions(id: number, items: { productId: number; qty: number }[]) {
  const { data } = await api.put<Reservation>(`/api/bookings/${id}/concessions`, { items });
  return data;
}

export async function fetchRevenueReport(params?: { from?: string; to?: string; grain?: string }) {
  const { data } = await api.get<RevenueReport>("/api/reports/revenue", { params });
  return data;
}

export type TicketLookup = {
  reservationId: number;
  ticketCode: string;
  movieTitle: string;
  roomName: string;
  startAt: string;
  seatLabel: string;
  status: string;
  checkedInAt?: string | null;
};

export async function lookupTicket(code: string) {
  const { data } = await api.get<TicketLookup>(`/api/tickets/${encodeURIComponent(code)}`);
  return data;
}

export type Voucher = {
  id: number;
  code: string;
  discountType: string;
  discountValue: number;
  minOrderAmount?: number;
  usageLimit: number;
  usedCount: number;
  status: string;
};

export async function fetchVouchers() {
  const { data } = await api.get<Voucher[]>("/api/vouchers");
  return data;
}

export async function createVoucher(body: {
  code: string;
  discountType: string;
  discountValue: number;
  minOrderAmount?: number;
  maxDiscountAmount?: number;
  usageLimit: number;
}) {
  const { data } = await api.post<Voucher>("/api/vouchers", body);
  return data;
}

export async function checkInTicket(code: string) {
  const { data } = await api.post<TicketLookup>(`/api/tickets/${encodeURIComponent(code)}/check-in`);
  return data;
}
