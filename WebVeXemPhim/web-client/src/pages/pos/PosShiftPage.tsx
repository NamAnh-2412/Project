import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { fetchConcessions } from "../../api/catalogApi";
import { closeShift, fetchCurrentShift, openShift, sellSnacks } from "../../api/bookingApi";
import type { PosShift } from "../../api/types";
import { formatVnd } from "../../lib/money";
import { errorText } from "../../lib/notify";

function when(iso?: string | null) {
  return iso ? iso.replace("T", " ").slice(0, 16) : "—";
}

export default function PosShiftPage() {
  const qc = useQueryClient();
  const [opening, setOpening] = useState(500000);
  const [actual, setActual] = useState(0);
  const [closed, setClosed] = useState<PosShift | null>(null);
  const [qty, setQty] = useState<Record<number, number>>({});
  const [sale, setSale] = useState("");
  const [err, setErr] = useState("");

  const shiftQ = useQuery({ queryKey: ["pos-shift"], queryFn: fetchCurrentShift });
  const foodQ = useQuery({ queryKey: ["pos-snacks"], queryFn: fetchConcessions });
  const shift = shiftQ.data;

  const openMut = useMutation({
    mutationFn: () => openShift(opening),
    onSuccess: () => {
      setErr("");
      setClosed(null);
      qc.invalidateQueries({ queryKey: ["pos-shift"] });
    },
    onError: (e) => setErr(errorText(e, "Không mở được ca")),
  });

  const closeMut = useMutation({
    mutationFn: () => closeShift(actual),
    onSuccess: (data) => {
      setClosed(data);
      setErr("");
      qc.invalidateQueries({ queryKey: ["pos-shift"] });
    },
    onError: (e) => setErr(errorText(e, "Không kết được ca")),
  });

  const snackMut = useMutation({
    mutationFn: () => {
      const items = Object.entries(qty)
        .map(([productId, n]) => ({ productId: Number(productId), qty: n }))
        .filter((x) => x.qty > 0);
      if (items.length === 0) throw new Error("Chọn ít nhất một món");
      return sellSnacks(items);
    },
    onSuccess: (data) => {
      setQty({});
      setSale(`${data.detail} · ${formatVnd(data.totalAmount)}`);
      setErr("");
      qc.invalidateQueries({ queryKey: ["pos-shift"] });
    },
    onError: (e) => setErr(errorText(e, "Không bán được bắp nước")),
  });

  return (
    <div className="page">
      <h2>Ca quầy</h2>
      {err && <div className="toast err">{err}</div>}
      {!shift && (
        <form className="panel" onSubmit={(e) => { e.preventDefault(); openMut.mutate(); }}>
          <label>
            Tiền đầu ca (đ)
            <input type="number" min={0} step={1000} value={opening} onChange={(e) => setOpening(Number(e.target.value))} />
          </label>
          <button className="btn btn-yellow" type="submit">Mở ca</button>
        </form>
      )}
      {shift && (
        <div className="panel">
          <p>Ca #{shift.id} đang mở từ {when(shift.openedAt)}</p>
          <p>Tiền đầu ca {formatVnd(shift.openingCash)}</p>
          <p className="muted">Vé tiền mặt và bắp lẻ trong ca sẽ cộng vào khi kết ca. Bán vé vẫn được khi chưa mở ca, nhưng không vào Z-report.</p>
        </div>
      )}

      <h3>Bắp nước không kèm vé</h3>
      <div className="grid" style={{ gridTemplateColumns: "repeat(auto-fill, minmax(180px, 1fr))" }}>
        {(foodQ.data ?? []).filter((p) => p.status === "ACTIVE").map((p) => (
          <div className="panel" key={p.id}>
            <strong>{p.name}</strong>
            <div className="price" style={{ fontSize: 16 }}>{formatVnd(p.price)}</div>
            <div className="row">
              <button type="button" className="btn btn-outline" onClick={() => setQty((q) => ({ ...q, [p.id]: Math.max(0, (q[p.id] ?? 0) - 1) }))}>−</button>
              <strong>{qty[p.id] ?? 0}</strong>
              <button type="button" className="btn btn-outline" onClick={() => setQty((q) => ({ ...q, [p.id]: Math.min(8, (q[p.id] ?? 0) + 1) }))}>+</button>
            </div>
          </div>
        ))}
      </div>
      <button className="btn btn-yellow" type="button" disabled={snackMut.isPending} onClick={() => snackMut.mutate()}>Thu tiền bắp nước</button>
      {sale && <p>Đã bán: {sale}</p>}

      {shift && (
        <form className="panel" style={{ marginTop: 16 }} onSubmit={(e) => { e.preventDefault(); closeMut.mutate(); }}>
          <label>
            Tiền mặt đếm được khi kết ca
            <input type="number" min={0} step={1000} value={actual} onChange={(e) => setActual(Number(e.target.value))} />
          </label>
          <button className="btn btn-yellow" type="submit">Kết ca</button>
        </form>
      )}

      {closed && (
        <article className="panel k80" id="print-area">
          <h3>Z-report ca #{closed.id}</h3>
          <p>Mở {when(closed.openedAt)} · Đóng {when(closed.closedAt)}</p>
          <p>Đầu ca {formatVnd(closed.openingCash)}</p>
          <p>Vé tiền mặt {formatVnd(closed.cashSales)} · {closed.ticketCount || 0} vé</p>
          <p>Vé chuyển khoản / QR {formatVnd(closed.cardSales)}</p>
          <p>Bắp lẻ {formatVnd(closed.snackSales)}</p>
          <p>Kỳ vọng {formatVnd(closed.expectedCash)}</p>
          <p>Thực đếm {formatVnd(closed.actualCash)}</p>
          <p>Chênh lệch {formatVnd(closed.difference)}</p>
          <p style={{ marginTop: 24 }}>Chữ ký bàn giao: ________________</p>
          <button className="btn btn-outline no-print" type="button" onClick={() => window.print()}>In Z-report</button>
        </article>
      )}
    </div>
  );
}
