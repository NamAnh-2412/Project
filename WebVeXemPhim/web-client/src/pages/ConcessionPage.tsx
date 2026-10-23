import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { fetchConcessions } from "../api/catalogApi";
import { assetUrl } from "../lib/assetUrl";
import { getBooking, setBookingConcessions } from "../api/bookingApi";
import type { ConcessionProduct, Reservation } from "../api/types";
import BookingStepper from "../components/BookingStepper";
import HoldClock from "../components/HoldClock";
import { useAbandonSale, useHoldSession } from "../context/HoldSessionContext";
import { isHoldExpired, seatMapPath } from "../lib/holdClock";

const TYPE_LABEL: Record<string, string> = {
  FOOD: "Đồ ăn",
  DRINK: "Nước",
  COMBO: "Combo",
};

export default function ConcessionPage({ channel }: { channel: "ONLINE" | "POS" }) {
  const { reservationId } = useParams();
  const id = Number(reservationId);
  const nav = useNavigate();
  const { track } = useHoldSession();
  const abortSale = useAbandonSale(channel);
  const [res, setRes] = useState<Reservation | null>(null);
  const [products, setProducts] = useState<ConcessionProduct[]>([]);
  const [qty, setQty] = useState<Record<number, number>>({});
  const [err, setErr] = useState("");
  const [saving, setSaving] = useState(false);
  const [tab, setTab] = useState("ALL");

  useEffect(() => {
    Promise.all([getBooking(id), fetchConcessions()])
      .then(([booking, list]) => {
        setRes(booking);
        if (booking.status === "HOLD" && booking.showtimeId) {
          track({
            reservationId: booking.id,
            showtimeId: booking.showtimeId,
            channel,
            expiresAt: booking.holdExpiresAt ?? null,
          });
        }
        setProducts(list.filter((p) => p.status === "ACTIVE"));
        const next: Record<number, number> = {};
        for (const line of booking.concessions ?? []) {
          if (line.productId) next[line.productId] = line.qty;
        }
        setQty(next);
      })
      .catch(() => setErr("Không tải được đồ ăn vặt"));
  }, [id]);

  const seats = Number(res?.seatsSubtotal ?? 0);
  const fnb = useMemo(() => {
    return products.reduce((sum, p) => sum + Number(p.price) * (qty[p.id] ?? 0), 0);
  }, [products, qty]);

  const checkout = channel === "POS" ? `/pos/checkout/${id}` : `/checkout/${id}`;

  const save = async (items: { productId: number; qty: number }[]) => {
    setErr("");
    setSaving(true);
    try {
      await setBookingConcessions(id, items);
      nav(checkout);
    } catch (e: unknown) {
      const msg = (e as { response?: { data?: { message?: string } } })?.response?.data?.message;
      setErr(msg || "Không lưu được đồ ăn vặt");
    } finally {
      setSaving(false);
    }
  };

  const continuePay = () => {
    const items = Object.entries(qty)
      .map(([productId, n]) => ({ productId: Number(productId), qty: n }))
      .filter((x) => x.qty > 0);
    return save(items);
  };

  if (!res) return <div className="page">{err || "Đang tải..."}</div>;

  const expired = isHoldExpired(res.holdExpiresAt);
  const visible = products.filter((p) => tab === "ALL" || p.type === tab);
  const art = (type: string) => (type === "DRINK" ? "🥤" : type === "COMBO" ? "🍿" : "🎬");

  return (
    <div className="page">
      <BookingStepper current={4} />
      <h2>Đồ ăn vặt</h2>
      <p className="muted">
        {res.movieTitle} · ghế {res.seatLabels?.join(", ")} · bước này có thể bỏ qua
      </p>
      <HoldClock expiresAt={res.holdExpiresAt} channel={channel} showtimeId={res.showtimeId} />
      {err && <div className="toast err">{err}</div>}
      <div className="fnb-tabs">
        {[
          ["ALL", "Tất cả"],
          ["COMBO", "Combo"],
          ["FOOD", "Bắp rang"],
          ["DRINK", "Nước"],
        ].map(([id, label]) => (
          <button key={id} type="button" className={`genre-pill ${tab === id ? "on" : ""}`} onClick={() => setTab(id)}>
            {label}
          </button>
        ))}
      </div>
      <div className="grid" style={{ gridTemplateColumns: "repeat(auto-fill, minmax(220px, 1fr))", marginTop: 16 }}>
        {visible.map((p) => {
          const n = qty[p.id] ?? 0;
          return (
            <div className="panel fnb-card" key={p.id}>
              {p.imageUrl ? (
                <img className="fnb-photo" src={assetUrl(p.imageUrl)} alt="" />
              ) : (
                <div className="fnb-art" aria-hidden>{art(p.type)}</div>
              )}
              <div className="fnb-body">
                <div className="muted">{TYPE_LABEL[p.type] || p.type}{p.badge ? ` · ${p.badge}` : ""}</div>
                <strong>{p.name}</strong>
                {p.description && <p className="muted">{p.description}</p>}
                <div className="price" style={{ fontSize: 18 }}>{Number(p.price).toLocaleString()}đ</div>
                <div className="row" style={{ marginTop: 12, alignItems: "center" }}>
                  <button
                    type="button"
                    className="btn btn-outline"
                    disabled={n <= 0}
                    onClick={() => setQty((q) => ({ ...q, [p.id]: Math.max(0, (q[p.id] ?? 0) - 1) }))}
                  >
                    −
                  </button>
                  <strong className="qty-num" key={n}>{n}</strong>
                  <button
                    type="button"
                    className="btn btn-outline"
                    disabled={n >= 8}
                    onClick={() => setQty((q) => ({ ...q, [p.id]: Math.min(8, (q[p.id] ?? 0) + 1) }))}
                  >
                    +
                  </button>
                </div>
              </div>
            </div>
          );
        })}
        {visible.length === 0 && <p className="muted">Chưa có món trong mục này.</p>}
      </div>
      <div className="row" style={{ justifyContent: "space-between", marginTop: 20 }}>
        <div>
          <div className="muted">Ghế {seats.toLocaleString()}đ · Đồ ăn vặt {fnb.toLocaleString()}đ</div>
          <div className={channel === "POS" ? "pos-price" : "price"}>{(seats + fnb).toLocaleString()}đ</div>
        </div>
        <div className="row">
          <button
            type="button"
            className="btn btn-outline"
            disabled={saving || !res.showtimeId}
            onClick={() => res.showtimeId && nav(seatMapPath(channel, res.showtimeId))}
          >
            Quay lại chọn ghế
          </button>
          <button type="button" className="btn btn-outline" disabled={saving} onClick={() => void abortSale()}>
            Hủy giao dịch
          </button>
          <button type="button" className="btn btn-outline" disabled={saving || expired} onClick={() => save([])}>
            Bỏ qua
          </button>
          <button type="button" className="btn btn-yellow" disabled={saving || expired} onClick={continuePay}>
            Tiếp tục
          </button>
        </div>
      </div>
    </div>
  );
}
