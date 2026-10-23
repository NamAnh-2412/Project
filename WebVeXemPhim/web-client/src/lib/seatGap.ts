import type { SeatCell } from "../api/types";

/** Ghế đã thanh toán (BOOKED) chặn ghế cách đúng 1 ô trống. HOLD không tính. */
export function singleGapFromPaid(seats: SeatCell[], selectedIds: number[]): string | null {
  const wanted = new Set(selectedIds);
  const paid = new Set(seats.filter((s) => s.status === "BOOKED").map((s) => s.seatId));
  if (paid.size === 0 || wanted.size === 0) return null;

  const rows = new Map<string, SeatCell[]>();
  for (const seat of seats) {
    const row = rows.get(seat.row) ?? [];
    row.push(seat);
    rows.set(seat.row, row);
  }

  for (const row of rows.values()) {
    row.sort((a, b) => a.number - b.number);
    for (let i = 0; i < row.length; i++) {
      const seat = row[i];
      if (!wanted.has(seat.seatId)) continue;
      if (gap(row, i, -1, wanted, paid) || gap(row, i, 1, wanted, paid)) {
        return `Không thể đặt ghế ${seat.label} vì cách ghế đã thanh toán đúng 1 ghế. Chọn ghế kề bên hoặc cách ít nhất 2 ghế trống.`;
      }
    }
  }
  return null;
}

function gap(row: SeatCell[], index: number, dir: number, wanted: Set<number>, paid: Set<number>) {
  const mid = index + dir;
  const far = index + dir * 2;
  if (mid < 0 || far < 0 || mid >= row.length || far >= row.length) return false;
  const seat = row[index];
  const middle = row[mid];
  const other = row[far];
  if (middle.number !== seat.number + dir || other.number !== seat.number + dir * 2) return false;
  if (paid.has(middle.seatId) || wanted.has(middle.seatId)) return false;
  return paid.has(other.seatId);
}
