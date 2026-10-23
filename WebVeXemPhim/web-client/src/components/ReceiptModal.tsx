import { useBrand } from "../context/BrandContext";
import type { Reservation } from "../api/types";
import TicketQr from "./TicketQr";

export default function ReceiptModal({
  reservation,
  onClose,
  title = "Biên lai thanh toán",
}: {
  reservation: Reservation;
  onClose?: () => void;
  title?: string;
}) {
  const { name } = useBrand();
  const primary = reservation.tickets?.[0];
  const qrValue = primary?.qrPayload || primary?.ticketCode || `CINE-${reservation.id}`;
  const seats = reservation.seatLabels?.length
    ? reservation.seatLabels
    : (reservation.tickets ?? []).map((t) => t.seatLabel).filter(Boolean);

  return (
    <div className="cv-modal-back" onClick={onClose}>
      <div className="cv-modal receipt-modal" role="dialog" aria-modal="true" aria-label={title} onClick={(e) => e.stopPropagation()}>
        <div className="no-print" style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", gap: 12, marginBottom: 12 }}>
          <div>
            <h3 style={{ margin: 0 }}>{title}</h3>
            <p className="muted" style={{ margin: "4px 0 0" }}>
              Đơn #{reservation.id} · Đã thanh toán
            </p>
          </div>
          {onClose && (
            <button type="button" className="btn btn-outline" onClick={onClose} aria-label="Đóng">
              Đóng
            </button>
          )}
        </div>

        <div className="no-print row" style={{ marginBottom: 16, justifyContent: "space-between" }}>
          <p className="muted" style={{ margin: 0 }}>In qua máy in nhiệt hoặc lưu PDF.</p>
          <button type="button" className="btn btn-yellow" onClick={() => window.print()}>
            In biên lai
          </button>
        </div>

        <div className="panel print-area k80 receipt-paper" id="print-area">
          <h2>{name || "CineVe"}</h2>
          <p style={{ textAlign: "center", marginTop: 0 }}>BIÊN LAI ĐẶT VÉ &amp; THANH TOÁN</p>
          <p className="muted" style={{ textAlign: "center" }}>
            Mã đơn #{reservation.id} · {new Date().toLocaleString("vi-VN")}
          </p>
          <hr />
          <p><strong>{reservation.movieTitle}</strong></p>
          <p>Phòng: {reservation.roomName}</p>
          <p>Suất: {reservation.startAt?.replace("T", " ").slice(0, 16)}</p>
          {reservation.guestName && <p>Khách: {reservation.guestName}</p>}
          <hr />
          <p>
            Ghế ({seats.length}): {seats.join(", ") || "—"}
          </p>
          <p style={{ textAlign: "right" }}>
            Vé {Number(reservation.seatsSubtotal ?? reservation.totalAmount).toLocaleString()}đ
          </p>
          {(reservation.concessions ?? []).length > 0 && (
            <>
              <hr />
              <p><strong>Đồ ăn vặt</strong></p>
              {(reservation.concessions ?? []).map((c) => (
                <p key={`${c.productId}-${c.name}`} style={{ display: "flex", justifyContent: "space-between", gap: 8 }}>
                  <span>{c.name} x{c.qty}</span>
                  <span>{Number(c.lineTotal).toLocaleString()}đ</span>
                </p>
              ))}
            </>
          )}
          {(reservation.discountAmount ?? 0) > 0 && (
            <p style={{ display: "flex", justifyContent: "space-between", gap: 8 }}>
              <span>Giảm giá ({reservation.voucherCode})</span>
              <span>-{Number(reservation.discountAmount).toLocaleString()}đ</span>
            </p>
          )}
          <hr />
          <p className="price" style={{ textAlign: "right", fontSize: 20 }}>
            {Number(reservation.totalAmount).toLocaleString()}đ
          </p>
          <p style={{ textAlign: "right", color: "#15803d", fontWeight: 700 }}>ĐÃ THANH TOÁN</p>
          <div style={{ textAlign: "center", marginTop: 12 }}>
            <TicketQr value={qrValue} />
            <p><strong>{primary?.ticketCode || `Đơn ${reservation.id}`}</strong></p>
            {(reservation.tickets?.length ?? 0) > 1 && (
              <p className="muted">Có {reservation.tickets!.length} mã vé — xem đủ ở Vé của tôi</p>
            )}
            <p className="muted">Quét mã tại cửa soát vé</p>
          </div>
          <hr />
          <p style={{ textAlign: "center" }}>Cảm ơn quý khách. Biên lai điện tử có giá trị như vé vào rạp.</p>
        </div>

        <div className="no-print row" style={{ marginTop: 16, justifyContent: "flex-end" }}>
          {onClose && (
            <button type="button" className="btn btn-outline" onClick={onClose}>
              Đóng
            </button>
          )}
          <button type="button" className="btn btn-yellow" onClick={() => window.print()}>
            In biên lai
          </button>
        </div>
      </div>
    </div>
  );
}
