import type { SeatCell } from "../api/types";

type Props = {
  seats: SeatCell[];
  selected: number[];
  onToggle: (seat: SeatCell) => void;
};

export default function SeatGrid({ seats, selected, onToggle }: Props) {
  const rows = [...new Set(seats.map((s) => s.row))];
  return (
    <>
      <div className="screen-wrap">
        <svg className="screen-curve" viewBox="0 0 300 48" aria-hidden>
          <path d="M 10 36 Q 150 6 290 36" fill="none" stroke="#f5c518" strokeWidth="6" strokeLinecap="round" />
          <path d="M 24 40 Q 150 16 276 40" fill="none" stroke="rgba(255,255,255,0.35)" strokeWidth="2" />
        </svg>
        <div className="screen-label">MÀN HÌNH</div>
      </div>
      <div className="screen-glow" />
      <div className="seat-grid">
        {rows.map((row) => (
          <div className="seat-row" key={row}>
            <span className="seat-lab">{row}</span>
            {seats
              .filter((s) => s.row === row)
              .map((s) => {
                const mine = selected.includes(s.seatId) || s.status === "HOLD_MINE";
                const state =
                  s.status === "BOOKED"
                    ? "booked"
                    : s.status === "HOLD" && !mine
                      ? "hold"
                      : mine
                        ? "mine"
                        : "";
                const kind = s.type === "VIP" ? "vip" : s.type === "COUPLE" ? "couple" : "";
                const cls = `${kind} ${state}`.trim();
                const disabled = s.status === "BOOKED" || (s.status === "HOLD" && !mine);
                return (
                  <button
                    key={s.seatId}
                    className={`seat ${cls}`}
                    disabled={disabled}
                    title={`${s.label} ${s.price.toLocaleString()}đ`}
                    onClick={() => onToggle(s)}
                  >
                    {s.type === "VIP" ? "★" : s.type === "COUPLE" ? "♥" : s.number}
                  </button>
                );
              })}
          </div>
        ))}
      </div>
      <div className="legend">
        <span><i style={{ background: "#e2e8f0" }} /> Trống</span>
        <span><i style={{ background: "var(--brand-yellow)" }} /> Đang chọn</span>
        <span><i style={{ background: "var(--sold)" }} /> Đang giữ</span>
        <span><i style={{ background: "#334155" }} /> Đã bán</span>
        <span><i style={{ borderColor: "#ffd700", background: "transparent" }} /> VIP</span>
        <span><i style={{ borderColor: "#f472b6", background: "transparent" }} /> Ghế đôi</span>
      </div>
    </>
  );
}
