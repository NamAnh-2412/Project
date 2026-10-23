import { useEffect, useState } from "react";
import { toPng } from "html-to-image";
import { Link } from "react-router-dom";
import { cancelOnlineTickets, myTickets } from "../api/bookingApi";
import type { Reservation } from "../api/types";
import { EmptyState } from "../components/EmptyState";
import Modal from "../components/Modal";
import ReceiptModal from "../components/ReceiptModal";
import TicketQr from "../components/TicketQr";
import { errorText, notifyError, notifyOk } from "../lib/notify";

const CANCEL_BEFORE_MINUTES = 30;

function minutesUntil(startAt?: string) {
  if (!startAt) return null;
  const start = new Date(startAt).getTime();
  if (Number.isNaN(start)) return null;
  return (start - Date.now()) / 60000;
}

function calendarUrl(r: Reservation) {
  if (!r.startAt) return "";
  const start = r.startAt.replace(/[-:]/g, "").slice(0, 15);
  const d = new Date(r.startAt);
  if (Number.isNaN(d.getTime())) return "";
  d.setMinutes(d.getMinutes() + 120);
  const pad = (n: number) => String(n).padStart(2, "0");
  const end = `${d.getFullYear()}${pad(d.getMonth() + 1)}${pad(d.getDate())}T${pad(d.getHours())}${pad(d.getMinutes())}00`;
  const text = encodeURIComponent(r.movieTitle || "Vé xem phim");
  const loc = encodeURIComponent(r.roomName || "CineVe");
  return `https://calendar.google.com/calendar/render?action=TEMPLATE&text=${text}&dates=${start}/${end}&location=${loc}`;
}

async function saveTicketPng(id: number) {
  const node = document.getElementById(`ticket-${id}`);
  if (!node) return;
  const url = await toPng(node, { cacheBust: true, backgroundColor: "#111111" });
  const a = document.createElement("a");
  a.href = url;
  a.download = `ve-${id}.png`;
  a.click();
}

function canCancel(r: Reservation) {
  if (r.status !== "CONFIRMED" || r.channel !== "ONLINE") return false;
  if ((r.tickets ?? []).some((t) => t.status === "CHECKED_IN")) return false;
  const minutes = minutesUntil(r.startAt);
  return minutes != null && minutes > CANCEL_BEFORE_MINUTES;
}

export default function TicketsPage() {
  const [list, setList] = useState<Reservation[]>([]);
  const [busyId, setBusyId] = useState<number | null>(null);
  const [err, setErr] = useState("");
  const [pending, setPending] = useState<Reservation | null>(null);
  const [receipt, setReceipt] = useState<Reservation | null>(null);

  const load = () => {
    myTickets().then(setList).catch(() => setList([]));
  };

  useEffect(() => {
    load();
  }, []);

  const cancel = async (r: Reservation) => {
    const amount = Number(r.totalAmount || 0).toLocaleString();
    setErr("");
    setBusyId(r.id);
    try {
      await cancelOnlineTickets(r.id);
      notifyOk(`Đã hủy vé và ghi hoàn ${amount}đ`);
      setPending(null);
      load();
    } catch (e: unknown) {
      const text = errorText(e, "Không hủy được vé");
      setErr(text);
      notifyError(e, text);
    } finally {
      setBusyId(null);
    }
  };

  return (
    <div className="page">
      <h1>Vé của tôi</h1>
      <p className="muted">Vé online hủy được trước giờ chiếu {CANCEL_BEFORE_MINUTES} phút. Vé đã soát thì không hủy. Hoàn đúng số tiền đã trả, không trả lại lượt mã giảm giá.</p>
      {err && <div className="toast err">{err}</div>}
      {list.length === 0 && (
        <EmptyState title="Chưa có vé" hint="Khi thanh toán xong, vé điện tử hiện ở đây." action={<Link className="btn btn-yellow" to="/">Khám phá phim</Link>} />
      )}
      {list.map((r) => {
        const refunded = r.status === "REFUNDED";
        const checkedIn = (r.tickets ?? []).some((t) => t.status === "CHECKED_IN");
        const minutes = minutesUntil(r.startAt);
        const open = canCancel(r);
        const statusClass = refunded || (r.tickets ?? []).every((t) => t.status === "CANCELLED")
          ? "dead"
          : checkedIn
            ? "in"
            : "ok";
        const statusText = refunded ? "ĐÃ HỦY" : checkedIn ? "ĐÃ VÀO CỬA" : "CHƯA SOÁT";
        const code = r.tickets?.[0]?.ticketCode || `Đơn ${r.id}`;
        return (
          <article className="ticket-stub" id={`ticket-${r.id}`} key={r.id}>
            <div className="ticket-top">
              <h3 style={{ margin: "0 0 6px" }}>{r.movieTitle}</h3>
              <p className="muted" style={{ margin: 0 }}>
                {r.roomName} · {r.startAt?.replace("T", " ").slice(0, 16)}
              </p>
              <p style={{ margin: "8px 0 0" }}>
                Ghế: {(r.tickets ?? []).map((t) => t.seatLabel).filter(Boolean).join(", ") || "—"}
              </p>
              {(r.concessions ?? []).length > 0 && (
                <p className="muted">Đồ ăn vặt: {(r.concessions ?? []).map((c) => `${c.name} x${c.qty}`).join(", ")}</p>
              )}
            </div>
            <div className="ticket-perf" />
            <div className="ticket-bot">
              <TicketQr value={r.tickets?.[0]?.qrPayload || code} />
              <div>
                <div className="muted">Mã vé</div>
                <strong>{code}</strong>
                <div className={`ticket-status ${statusClass}`}>{statusText}</div>
                <p className="price" style={{ fontSize: 18 }}>{Number(r.totalAmount || 0).toLocaleString()}đ</p>
                {refunded && <p className="muted">Đã hoàn {Number(r.totalAmount || 0).toLocaleString()}đ. Ghế đã mở lại.</p>}
                <div className="row" style={{ marginTop: 8 }}>
                  {!refunded && r.status === "CONFIRMED" && (
                    <>
                      <button className="btn btn-outline" type="button" onClick={() => setReceipt(r)}>
                        In biên lai
                      </button>
                      <button className="btn btn-outline" type="button" onClick={() => void saveTicketPng(r.id)}>
                        Lưu ảnh vé
                      </button>
                      {r.startAt && (
                        <a className="btn btn-outline" href={calendarUrl(r)} target="_blank" rel="noreferrer">
                          Thêm lịch
                        </a>
                      )}
                    </>
                  )}
                  {open && (
                    <button className="btn btn-outline" type="button" disabled={busyId === r.id} onClick={() => setPending(r)}>
                      Hủy vé
                    </button>
                  )}
                </div>
                {!refunded && !open && r.channel === "ONLINE" && r.status === "CONFIRMED" && checkedIn && (
                  <p className="muted">Vé đã soát, không hủy được.</p>
                )}
                {!refunded && !open && r.channel === "ONLINE" && r.status === "CONFIRMED" && !checkedIn && minutes != null && minutes <= CANCEL_BEFORE_MINUTES && (
                  <p className="muted">Đã trong {CANCEL_BEFORE_MINUTES} phút trước giờ chiếu, không hủy được.</p>
                )}
              </div>
            </div>
          </article>
        );
      })}
      {pending && (
        <Modal title="Hủy vé online" onClose={() => setPending(null)}>
          <p>
            Hoàn {Number(pending.totalAmount || 0).toLocaleString()}đ — đúng số đã trả sau giảm giá.
            Mã giảm giá không được hoàn lượt. Ghế sẽ mở lại cho người khác đặt.
          </p>
          <div className="row">
            <button type="button" className="btn btn-outline" onClick={() => setPending(null)}>Giữ vé</button>
            <button type="button" className="btn btn-yellow" disabled={busyId === pending.id} onClick={() => void cancel(pending)}>
              {busyId === pending.id ? "Đang hủy..." : "Xác nhận hủy"}
            </button>
          </div>
        </Modal>
      )}
      {receipt && (
        <ReceiptModal reservation={receipt} onClose={() => setReceipt(null)} />
      )}
    </div>
  );
}
