import { useEffect, useRef, useState } from "react";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import { applyVoucher, confirmCash, confirmOnline, createPaymentLink, fetchAvailableVouchers, fetchMockQr, fetchPaymentOptions, getBooking, startMomo } from "../api/bookingApi";
import BookingStepper from "../components/BookingStepper";
import HoldClock from "../components/HoldClock";
import type { Reservation, VoucherOffer } from "../api/types";
import { useAbandonSale, useHoldSession } from "../context/HoldSessionContext";
import { isHoldExpired } from "../lib/holdClock";
import { errorText, notifyError, notifyOk } from "../lib/notify";
import Modal from "../components/Modal";
import ReceiptModal from "../components/ReceiptModal";

export default function CheckoutPage({ channel }: { channel: "ONLINE" | "POS" }) {
  const { reservationId } = useParams();
  const id = Number(reservationId);
  const nav = useNavigate();
  const { track, release } = useHoldSession();
  const abortSale = useAbandonSale(channel);
  const [res, setRes] = useState<Reservation | null>(null);
  const [err, setErr] = useState("");
  const [askCancel, setAskCancel] = useState(false);
  const [busy, setBusy] = useState(false);
  const [voucher, setVoucher] = useState("");
  const [wallet, setWallet] = useState<VoucherOffer[] | null>(null);
  const [payLink, setPayLink] = useState<{ checkoutUrl: string; qrCode: string } | null>(null);
  const [mockQr, setMockQr] = useState<string | null>(null);
  const [momoOn, setMomoOn] = useState(false);
  const [momoPolls, setMomoPolls] = useState(0);
  const [showReceipt, setShowReceipt] = useState(false);
  const receiptTold = useRef(false);
  const [params] = useSearchParams();

  useEffect(() => {
    getBooking(id)
      .then((booking) => {
        setRes(booking);
        if (booking.status === "HOLD" && booking.showtimeId) {
          track({
            reservationId: booking.id,
            showtimeId: booking.showtimeId,
            channel,
            expiresAt: booking.holdExpiresAt ?? null,
          });
        }
      })
      .catch((e) => {
        setErr("Không tải được giữ chỗ");
        notifyError(e, "Không tải được giữ chỗ");
      });
  }, [id, channel, track]);

  useEffect(() => {
    if (channel !== "ONLINE") return;
    fetchPaymentOptions().then((opt) => setMomoOn(opt.momo)).catch(() => setMomoOn(false));
  }, [channel]);

  useEffect(() => {
    const flag = params.get("momo");
    if (!flag || !res || channel !== "ONLINE") return;
    if (flag === "paid" || flag === "already_paid") {
      if (res.status === "CONFIRMED") {
        release();
        setShowReceipt(true);
        if (!receiptTold.current) {
          receiptTold.current = true;
          notifyOk("Thanh toán thành công. Vé và biên lai đã được xuất.");
        }
        return;
      }
      if (momoPolls >= 8) {
        setErr("MoMo chưa xác nhận vé. Nếu đã trừ tiền, tải lại trang sau ít phút.");
        return;
      }
      const timer = window.setTimeout(() => {
        getBooking(id).then(setRes).catch(() => undefined);
        setMomoPolls((n) => n + 1);
      }, 2000);
      return () => window.clearTimeout(timer);
    }
    if (flag === "failed") setErr("MoMo chưa trừ tiền. Bạn có thể thanh toán lại.");
    if (flag === "invalid") setErr("MoMo không xác nhận được giao dịch.");
    if (flag === "late") setErr("Tiền đến sau khi hết giữ ghế. Vé không được xuất. Liên hệ quầy nếu đã trừ tiền.");
  }, [params, res, id, channel, momoPolls, nav, release]);

  const pay = async () => {
    if (isHoldExpired(res?.holdExpiresAt)) {
      setErr("Hết thời gian giữ ghế");
      notifyError({ response: { data: { code: "HOLD_EXPIRED" } } }, "Hết thời gian giữ ghế");
      return;
    }
    setBusy(true);
    setErr("");
    try {
      const done = channel === "POS" ? await confirmCash(id) : await confirmOnline(id);
      release();
      notifyOk(channel === "POS" ? "Đã thu tiền" : "Thanh toán thành công. Vé và biên lai đã được xuất.");
      if (channel === "POS") nav(`/pos/print/${done.id}`);
      else {
        setRes(done);
        setShowReceipt(true);
      }
    } catch (e: unknown) {
      setErr(errorText(e, "Thanh toán thất bại"));
      notifyError(e, "Thanh toán thất bại");
    } finally {
      setBusy(false);
    }
  };

  const cancel = async () => {
    await abortSale();
    notifyOk("Đã hủy giữ chỗ");
  };

  const openWallet = async () => {
    if (!res) return;
    setBusy(true);
    setErr("");
    try {
      const gross = Number(res.seatsSubtotal || 0) + Number(res.concessionsSubtotal || 0);
      setWallet(await fetchAvailableVouchers(gross));
    } catch (e) {
      setErr(errorText(e, "Không tải được kho voucher"));
    } finally {
      setBusy(false);
    }
  };

  const useVoucher = async (code?: string) => {
    const raw = (code ?? voucher).trim();
    if (!raw) return;
    setBusy(true);
    setErr("");
    try {
      const next = await applyVoucher(id, raw);
      setVoucher(raw);
      setWallet(null);
      setRes(next);
      setMockQr((prev) => {
        if (prev) URL.revokeObjectURL(prev);
        return null;
      });
      notifyOk("Đã áp dụng mã giảm giá");
    } catch (e) {
      setErr(errorText(e, "Không áp dụng được mã"));
      notifyError(e, "Không áp dụng được mã");
    } finally {
      setBusy(false);
    }
  };

  const showMockQr = async () => {
    if (isHoldExpired(res?.holdExpiresAt)) {
      setErr("Hết thời gian giữ ghế");
      notifyError({ response: { data: { code: "HOLD_EXPIRED" } } }, "Hết thời gian giữ ghế");
      return;
    }
    setBusy(true);
    setErr("");
    try {
      const blob = await fetchMockQr(id);
      setMockQr((prev) => {
        if (prev) URL.revokeObjectURL(prev);
        return URL.createObjectURL(blob);
      });
    } catch (e) {
      setErr(errorText(e, "Không tạo được QR"));
      notifyError(e, "Không tạo được QR");
    } finally {
      setBusy(false);
    }
  };

  const momoPay = async () => {
    if (isHoldExpired(res?.holdExpiresAt)) {
      setErr("Hết thời gian giữ ghế");
      notifyError({ response: { data: { code: "HOLD_EXPIRED" } } }, "Hết thời gian giữ ghế");
      return;
    }
    setBusy(true);
    setErr("");
    try {
      const link = await startMomo(id);
      window.location.href = link.checkoutUrl;
    } catch (e) {
      setErr(errorText(e, "Không tạo được thanh toán MoMo"));
      notifyError(e, "Không tạo được thanh toán MoMo");
      setBusy(false);
    }
  };

  const bankPay = async () => {
    setBusy(true);
    setErr("");
    try {
      const link = await createPaymentLink(id);
      setPayLink(link);
      const fresh = await getBooking(id);
      setRes(fresh);
      notifyOk("Đã tạo mã chuyển khoản. Ghế được giữ thêm trong lúc chờ.");
    } catch (e) {
      setErr(errorText(e, "Không tạo được mã thanh toán"));
      notifyError(e, "Không tạo được mã thanh toán");
    } finally {
      setBusy(false);
    }
  };

  if (!res) return <div className="page">{err || "Đang tải..."}</div>;

  const expired = isHoldExpired(res.holdExpiresAt);
  const paidOnline = channel === "ONLINE" && res.status === "CONFIRMED";

  if (paidOnline) {
    return (
      <div className="page">
        <div className="panel receipt-success no-print">
          <h2>Thanh toán thành công</h2>
          <p className="muted">Vé và biên lai đã sẵn sàng. Bạn có thể in ngay hoặc xem lại ở Vé của tôi.</p>
          <div className="row" style={{ justifyContent: "center", marginTop: 16 }}>
            <button type="button" className="btn btn-yellow" onClick={() => setShowReceipt(true)}>
              Xem biên lai
            </button>
            <button type="button" className="btn btn-outline" onClick={() => nav("/tickets")}>
              Vé của tôi
            </button>
            <button type="button" className="btn btn-outline" onClick={() => nav("/")}>
              Về trang chủ
            </button>
          </div>
        </div>
        {showReceipt && (
          <ReceiptModal reservation={res} onClose={() => setShowReceipt(false)} />
        )}
      </div>
    );
  }

  return (
    <div className="page">
      <BookingStepper current={5} />
      <div className="panel">
        <h2>{res.movieTitle}</h2>
        <p className="muted">{res.roomName} · {res.startAt?.replace("T", " ").slice(0, 16)}</p>
        <p>Ghế: {res.seatLabels?.join(", ")}</p>
        {res.guestName && <p>Khách: {res.guestName}</p>}
        {(res.concessions ?? []).length > 0 && (
          <p>
            Đồ ăn vặt: {(res.concessions ?? []).map((c) => `${c.name} x${c.qty}`).join(", ")}
          </p>
        )}
        <HoldClock expiresAt={res.holdExpiresAt} channel={channel} showtimeId={res.showtimeId} />
        <p className="muted">
          Vé {Number(res.seatsSubtotal ?? res.totalAmount).toLocaleString()}đ
          {(res.concessionsSubtotal ?? 0) > 0 ? ` · Đồ ăn vặt ${Number(res.concessionsSubtotal).toLocaleString()}đ` : ""}
          {(res.discountAmount ?? 0) > 0 ? ` · Giảm ${Number(res.discountAmount).toLocaleString()}đ (${res.voucherCode})` : ""}
        </p>
        {channel === "ONLINE" && res.status === "HOLD" && (
          <div className="row" style={{ marginBottom: 12 }}>
            <input value={voucher} onChange={(e) => setVoucher(e.target.value)} placeholder="Mã giảm giá" />
            <button className="btn btn-outline" type="button" disabled={busy || !voucher.trim()} onClick={() => void useVoucher()}>
              Áp dụng
            </button>
            <button className="btn btn-outline" type="button" disabled={busy} onClick={() => void openWallet()}>
              Kho voucher
            </button>
          </div>
        )}
        {mockQr && (
          <div style={{ marginBottom: 12 }}>
            <img src={mockQr} alt="QR thanh toán giả lập" width={200} height={200} />
            <p className="muted">QR giả lập, không trừ tiền. Quét điện thoại sẽ thấy mã đơn. Bấm Xác nhận đã thanh toán để nhận vé.</p>
          </div>
        )}
        {payLink && (
          <div style={{ marginBottom: 12 }}>
            {payLink.qrCode.startsWith("http") ? (
              <img src={payLink.qrCode} alt="QR chuyển khoản" width={200} height={200} />
            ) : (
              <p className="muted">{payLink.qrCode}</p>
            )}
            <p><a href={payLink.checkoutUrl}>Mở trang chuyển khoản</a></p>
          </div>
        )}
        <p className={channel === "POS" ? "pos-price" : "price"}>{Number(res.totalAmount).toLocaleString()}đ</p>
        {err && <div className="toast err">{err}</div>}
        <div className="row">
          <button
            className="btn btn-outline"
            disabled={busy || expired}
            onClick={() => nav(channel === "POS" ? `/pos/concessions/${id}` : `/concessions/${id}`)}
          >
            Quay lại chọn đồ ăn
          </button>
          {channel === "ONLINE" && momoOn && (
            <button className="btn btn-yellow" disabled={busy || expired} onClick={() => void momoPay()}>
              {busy ? "Đang mở MoMo..." : "Thanh toán MoMo"}
            </button>
          )}
          {channel === "ONLINE" && !momoOn && (
            <button className="btn btn-outline" disabled={busy || expired} onClick={() => void bankPay()}>
              Chuyển khoản
            </button>
          )}
          {channel === "ONLINE" && !momoOn && !mockQr && (
            <button className="btn btn-yellow" disabled={busy || expired} onClick={() => void showMockQr()}>
              {busy ? "Đang tạo QR..." : "Thanh toán QR"}
            </button>
          )}
          {(channel === "POS" || (mockQr && !momoOn)) && (
            <button className="btn btn-yellow" disabled={busy || expired} onClick={() => void pay()}>
              {busy ? "Đang thanh toán..." : channel === "POS" ? "Đã thu tiền" : "Xác nhận đã thanh toán"}
            </button>
          )}
          <button className="btn btn-outline" disabled={busy} onClick={() => setAskCancel(true)}>
            Hủy giao dịch
          </button>
        </div>
      </div>
      {wallet && (
        <Modal title="Kho voucher" onClose={() => setWallet(null)}>
          {wallet.length === 0 && <p>Không có mã phù hợp với số tiền đơn này.</p>}
          <ul className="list-none space-y-2 p-0">
            {wallet.map((v) => (
              <li key={v.code} className="row" style={{ justifyContent: "space-between" }}>
                <span>
                  <strong>{v.code}</strong>
                  <span className="muted"> · giảm {Number(v.discountAmount).toLocaleString()}đ</span>
                </span>
                <button type="button" className="btn btn-yellow" disabled={busy} onClick={() => void useVoucher(v.code)}>Dùng</button>
              </li>
            ))}
          </ul>
        </Modal>
      )}
      {askCancel && (
        <Modal title="Hủy giữ chỗ" onClose={() => setAskCancel(false)}>
          <p>Ghế đang giữ sẽ được trả lại. Đơn chưa thu tiền nên không phát sinh hoàn vé.</p>
          <div className="row">
            <button type="button" className="btn btn-outline" onClick={() => setAskCancel(false)}>Ở lại</button>
            <button type="button" className="btn btn-yellow" onClick={() => void cancel()}>Hủy giao dịch</button>
          </div>
        </Modal>
      )}
    </div>
  );
}
