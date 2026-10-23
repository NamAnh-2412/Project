import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { cancelHold, fetchSeatMap, getBooking, holdSeats } from "../api/bookingApi";
import type { SeatCell, SeatMap } from "../api/types";
import SeatGrid from "../components/SeatGrid";
import BookingStepper from "../components/BookingStepper";
import HoldClock from "../components/HoldClock";
import { useAuth } from "../context/AuthContext";
import { useAbandonSale, useHoldSession } from "../context/HoldSessionContext";
import { singleGapFromPaid } from "../lib/seatGap";
import { errorText, notifyError, notifyOk } from "../lib/notify";
import { Client } from "@stomp/stompjs";

const MAX_SEATS = 8;

function mergeSelected(current: number[], map: SeatMap): { next: number[]; lost: string[] } {
  const byId = new Map(map.seats.map((s) => [s.seatId, s]));
  const lost: string[] = [];
  const kept = current.filter((id) => {
    const s = byId.get(id);
    if (!s) return false;
    if (s.status === "HOLD" || s.status === "BOOKED") {
      lost.push(s.label);
      return false;
    }
    return s.status === "AVAILABLE" || s.status === "HOLD_MINE";
  });
  const mine = map.seats.filter((s) => s.status === "HOLD_MINE").map((s) => s.seatId);
  return { next: [...new Set([...kept, ...mine])], lost };
}

export default function SeatMapPage({ channel }: { channel: "ONLINE" | "POS" }) {
  const { showtimeId } = useParams();
  const id = Number(showtimeId);
  const { user } = useAuth();
  const nav = useNavigate();
  const { track, release, session } = useHoldSession();
  const abortSale = useAbandonSale(channel);
  const [map, setMap] = useState<SeatMap | null>(null);
  const [selected, setSelected] = useState<number[]>([]);
  const [guest, setGuest] = useState("");
  const [err, setErr] = useState("");
  const [zoom, setZoom] = useState(1);
  const [holdId, setHoldId] = useState<number | null>(null);
  const [expiresAt, setExpiresAt] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const selectedRef = useRef<number[]>([]);
  const holdIdRef = useRef<number | null>(null);
  const chainRef = useRef(Promise.resolve());
  const firstLoad = useRef(true);

  selectedRef.current = selected;
  holdIdRef.current = holdId;

  const applyMap = (d: SeatMap, replace: boolean) => {
    setMap(d);
    if (replace) {
      const mine = d.seats.filter((s) => s.status === "HOLD_MINE").map((s) => s.seatId);
      setSelected(mine);
      selectedRef.current = mine;
      return;
    }
    const { next, lost } = mergeSelected(selectedRef.current, d);
    setSelected(next);
    selectedRef.current = next;
    if (lost.length) {
      setErr(`Ghế ${lost.join(", ")} đã được người khác giữ trước`);
    }
  };

  const load = (replace = false) =>
    fetchSeatMap(id)
      .then((d) => applyMap(d, replace))
      .catch(() => setErr("Không tải sơ đồ ghế"));

  useEffect(() => {
    if (!session || session.showtimeId !== id || session.channel !== channel) return;
    holdIdRef.current = session.reservationId;
    setHoldId(session.reservationId);
    setExpiresAt(session.expiresAt);
    getBooking(session.reservationId)
      .then((booking) => {
        if (booking.guestName) setGuest(booking.guestName);
        if (booking.holdExpiresAt) setExpiresAt(booking.holdExpiresAt);
      })
      .catch(() => undefined);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id, channel]);

  useEffect(() => {
    firstLoad.current = true;
    load(true).finally(() => {
      firstLoad.current = false;
    });
    const t = setInterval(() => load(false), 5000);
    const wsBase = (import.meta.env.VITE_API_BASE_URL || "http://localhost:8080").replace(/^http/, "ws");
    const client = new Client({
      brokerURL: `${wsBase}/ws-cineve`,
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe(`/topic/showtimes/${id}`, (frame) => {
          const body = JSON.parse(frame.body) as {
            updatedSeats?: { seatId: number; status: string }[];
          };
          const updates = body.updatedSeats ?? [];
          if (!updates.length) return;
          setMap((prev) => {
            if (!prev) return prev;
            const mine = new Set(selectedRef.current);
            const seats = prev.seats.map((seat) => {
              const next = updates.find((u) => u.seatId === seat.seatId);
              if (!next) return seat;
              if (seat.status === "HOLD_MINE" && next.status === "HOLD") return seat;
              if (next.status === "HOLD" && mine.has(seat.seatId) && holdIdRef.current) {
                return { ...seat, status: "HOLD_MINE" as const };
              }
              return { ...seat, status: next.status as SeatCell["status"] };
            });
            return { ...prev, seats };
          });
        });
      },
    });
    client.activate();
    return () => {
      clearInterval(t);
      void client.deactivate();
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const syncHold = (seatIds: number[], guestName?: string) => {
    const job = chainRef.current.then(async () => {
      if (seatIds.length === 0) {
        const existing = holdIdRef.current;
        if (existing) {
          await cancelHold(existing);
          holdIdRef.current = null;
          setHoldId(null);
          setExpiresAt(null);
          release();
        }
        return null;
      }
      const res = await holdSeats({
        showtimeId: id,
        seatIds,
        channel,
        guestName: guestName || undefined,
      });
      holdIdRef.current = res.reservationId;
      setHoldId(res.reservationId);
      setExpiresAt(res.holdExpiresAt);
      track({
        reservationId: res.reservationId,
        showtimeId: id,
        channel,
        expiresAt: res.holdExpiresAt,
      });
      const ids = res.seats.map((s) => s.seatId);
      setSelected(ids);
      selectedRef.current = ids;
      return res;
    });
    chainRef.current = job.then(
      () => undefined,
      () => undefined,
    );
    return job;
  };

  const selectedSeats = useMemo(
    () => map?.seats.filter((s) => selected.includes(s.seatId)) ?? [],
    [map, selected],
  );
  const total = selectedSeats.reduce((s, x) => s + Number(x.price), 0);

  const toggle = (seat: SeatCell) => {
    if (seat.status === "BOOKED" || seat.status === "HOLD") return;
    const pair = seat.pairSeatId ? map?.seats.find((s) => s.seatId === seat.pairSeatId) : undefined;
    if (pair && (pair.status === "BOOKED" || pair.status === "HOLD")) {
      setErr("Ghế đôi còn lại đang được giữ");
      return;
    }
    const group = pair ? [seat.seatId, pair.seatId] : [seat.seatId];
    const cur = selectedRef.current;
    let next: number[];
    const adding = !cur.includes(seat.seatId);
    if (!adding) {
      next = cur.filter((x) => !group.includes(x));
    } else {
      next = [...cur];
      for (const seatId of group) {
        if (!next.includes(seatId)) next.push(seatId);
      }
      if (next.length > MAX_SEATS) {
        setErr(`Tối đa ${MAX_SEATS} ghế mỗi giao dịch`);
        return;
      }
    }
    if (adding && map) {
      const gap = singleGapFromPaid(map.seats, next);
      if (gap) {
        setErr(gap);
        notifyError({ response: { data: { message: gap } } }, gap);
        return;
      }
    }
    selectedRef.current = next;
    setSelected(next);
    setErr("");
    syncHold(next, guest).catch((e: unknown) => {
      setErr(errorText(e, "Không giữ được ghế"));
      notifyError(e, "Không giữ được ghế");
      load(true);
    });
  };

  const holdAndGo = async () => {
    if (map) {
      const gap = singleGapFromPaid(map.seats, selectedRef.current);
      if (gap) {
        setErr(gap);
        notifyError({ response: { data: { message: gap } } }, gap);
        return;
      }
    }
    setErr("");
    setBusy(true);
    try {
      const res = await syncHold(selectedRef.current, guest);
      const rid = res?.reservationId ?? holdIdRef.current;
      if (!rid) throw new Error("Chưa giữ được ghế");
      nav(channel === "POS" ? `/pos/concessions/${rid}` : `/concessions/${rid}`);
    } catch (e: unknown) {
      setErr(errorText(e, "Không giữ được ghế"));
      notifyError(e, "Không giữ được ghế");
      load(true);
    } finally {
      setBusy(false);
    }
  };

  if (!map) return <div className="page">Đang tải sơ đồ...</div>;

  return (
    <div className="page">
      <BookingStepper current={3} />
      <h2>{map.movieTitle}</h2>
      <p className="muted">{map.roomName} · {map.startAt.replace("T", " ").slice(0, 16)}</p>
      <p className="muted">
        Ghế được khóa ngay khi chọn (giữ {map.holdMinutes} phút, không gia hạn khi đổi ghế). Ai giữ trước trên server mới được bán.
        Ghế đã thanh toán không cho đặt cách đúng 1 ghế trống; ghế đang giữ chưa thanh toán thì vẫn đặt được.
      </p>
      <HoldClock
        expiresAt={expiresAt}
        channel={channel}
        showtimeId={id}
        redirectOnExpire={false}
        onExpired={() => {
          holdIdRef.current = null;
          setHoldId(null);
          setExpiresAt(null);
          setSelected([]);
          selectedRef.current = [];
          setErr("Hết thời gian giữ ghế. Chọn lại.");
          notifyError({ response: { data: { code: "HOLD_EXPIRED" } } }, "Hết thời gian giữ ghế");
          load(true);
        }}
      />
      {err && <div className="toast err">{err}</div>}
      {channel === "POS" && (
        <div className="field" style={{ maxWidth: 320 }}>
          <label>Tên khách (tuỳ chọn)</label>
          <input value={guest} onChange={(e) => setGuest(e.target.value)} />
        </div>
      )}
      <div className="seat-zoom">
        <button type="button" className="btn btn-outline" onClick={() => setZoom((z) => Math.max(0.7, Number((z - 0.1).toFixed(2))))}>−</button>
        <button type="button" className="btn btn-outline" onClick={() => setZoom(1)}>100%</button>
        <button type="button" className="btn btn-outline" onClick={() => setZoom((z) => Math.min(1.8, Number((z + 0.1).toFixed(2))))}>+</button>
      </div>
      <div
        className="seat-scroll-container"
        onWheel={(e) => {
          if (!e.ctrlKey) return;
          e.preventDefault();
          setZoom((z) => Math.min(1.8, Math.max(0.7, Number((z + (e.deltaY < 0 ? 0.1 : -0.1)).toFixed(2)))));
        }}
      >
        <div className="seat-stage" style={{ transform: `scale(${zoom})` }}>
          <SeatGrid seats={map.seats} selected={selected} onToggle={toggle} />
        </div>
        <div className="seat-scroll-hint">‹ Vuốt ngang để xem toàn bộ sơ đồ ghế ›</div>
      </div>
      <div className="booking-dock">
        <div>
          <div className="muted">
            {selectedSeats.length > 0 ? (
              <strong>{selectedSeats.map((s) => s.label).join(", ")} ({selectedSeats.length} ghế)</strong>
            ) : (
              `Chưa chọn ghế (tối đa ${MAX_SEATS})`
            )}{" "}
            · {user?.username}
          </div>
          <div className="price">{total.toLocaleString()}đ</div>
        </div>
        <div className="row">
          <button
            type="button"
            className="btn btn-outline"
            onClick={() => void abortSale().then(() => notifyOk("Đã hủy giữ chỗ"))}
          >
            Hủy giao dịch
          </button>
          <button className="btn btn-yellow" disabled={selected.length === 0 || busy} onClick={holdAndGo}>
            {busy ? "Đang giữ ghế..." : "Tiếp tục"}
          </button>
        </div>
      </div>
    </div>
  );
}
