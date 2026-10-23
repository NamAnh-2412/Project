import { FormEvent, useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { checkInTicket, lookupTicket, type TicketLookup } from "../../api/bookingApi";
import { errorText, notifyError, notifyOk } from "../../lib/notify";

function statusLabel(status: string) {
  if (status === "CHECKED_IN") return "Đã vào cửa";
  if (status === "ISSUED") return "Chưa soát";
  if (status === "CANCELLED") return "Đã hủy";
  return status;
}

function beep() {
  const ctx = new AudioContext();
  const tone = ctx.createOscillator();
  const gain = ctx.createGain();
  tone.frequency.value = 880;
  tone.connect(gain);
  gain.connect(ctx.destination);
  tone.start();
  tone.stop(ctx.currentTime + 0.08);
}

export default function PosScanPage() {
  const [code, setCode] = useState("");
  const [ticket, setTicket] = useState<TicketLookup | null>(null);
  const [err, setErr] = useState("");
  const [ok, setOk] = useState("");
  const [busy, setBusy] = useState(false);
  const [cameraOn, setCameraOn] = useState(false);
  const videoRef = useRef<HTMLVideoElement>(null);
  const lockRef = useRef(false);

  const acceptCode = async (raw: string, autoCheckIn: boolean) => {
    const trimmed = raw.split("|")[0].trim();
    if (!trimmed) return;
    setCode(trimmed);
    setErr("");
    setOk("");
    setBusy(true);
    try {
      const found = await lookupTicket(trimmed);
      if (autoCheckIn && found.status === "ISSUED") {
        const done = await checkInTicket(trimmed);
        setTicket(done);
        setOk("Soát vé thành công — mời khách vào phòng");
        notifyOk("Soát vé thành công — mời khách vào phòng");
        beep();
      } else {
        setTicket(found);
      }
    } catch (e: unknown) {
      setErr(errorText(e, "Không có mã vé"));
    } finally {
      setBusy(false);
    }
  };

  useEffect(() => {
    if (!cameraOn) return;
    const Detector = (window as unknown as { BarcodeDetector?: new (opts: { formats: string[] }) => { detect: (src: CanvasImageSource) => Promise<{ rawValue: string }[]> } }).BarcodeDetector;
    if (!Detector || !navigator.mediaDevices?.getUserMedia) {
      setErr("Trình duyệt này chưa có máy quét QR. Hãy nhập mã vé.");
      setCameraOn(false);
      return;
    }
    let stop = false;
    let stream: MediaStream | null = null;
    const detector = new Detector({ formats: ["qr_code"] });
    navigator.mediaDevices.getUserMedia({ video: { facingMode: "environment" } })
      .then((media) => {
        stream = media;
        if (videoRef.current) {
          videoRef.current.srcObject = media;
          void videoRef.current.play();
        }
        const loop = async () => {
          if (stop || !videoRef.current) return;
          try {
            const codes = await detector.detect(videoRef.current);
            const raw = codes[0]?.rawValue;
            if (raw && !lockRef.current) {
              lockRef.current = true;
              await acceptCode(raw, true);
              setTimeout(() => { lockRef.current = false; }, 1500);
            }
          } catch {
            /* khung hình chưa sẵn sàng */
          }
          if (!stop) requestAnimationFrame(() => { void loop(); });
        };
        void loop();
      })
      .catch(() => {
        setErr("Không mở được camera");
        setCameraOn(false);
      });
    return () => {
      stop = true;
      stream?.getTracks().forEach((t) => t.stop());
    };
  }, [cameraOn]);

  const lookup = async (e?: FormEvent) => {
    e?.preventDefault();
    setErr("");
    setOk("");
    setTicket(null);
    const trimmed = code.trim();
    if (!trimmed) {
      setErr("Nhập mã vé");
      return;
    }
    setBusy(true);
    try {
      setTicket(await lookupTicket(trimmed));
    } catch {
      setErr("Không có mã vé");
    } finally {
      setBusy(false);
    }
  };

  const checkIn = async () => {
    if (!ticket) return;
    setErr("");
    setOk("");
    setBusy(true);
    try {
      const done = await checkInTicket(ticket.ticketCode);
      setTicket(done);
      setOk("Soát vé thành công");
      notifyOk("Soát vé thành công");
    } catch (e: unknown) {
      const text = errorText(e, "Không soát được vé");
      setErr(text);
      notifyError(e, text);
    } finally {
      setBusy(false);
    }
  };

  const checked = ticket?.status === "CHECKED_IN";
  const cancelled = ticket?.status === "CANCELLED";

  return (
    <div className="page">
      <h1>Soát vé</h1>
      <p className="muted">Tra mã, xác nhận vào cửa một lần, hoặc in lại vé đã bán.</p>
      <form onSubmit={(e) => void lookup(e)}>
        <div className="field" style={{ maxWidth: 360 }}>
          <label>Mã vé</label>
          <input
            value={code}
            onChange={(e) => setCode(e.target.value)}
            placeholder="VX-2026-..."
            autoFocus
          />
        </div>
        <button className="btn btn-yellow" type="submit" disabled={busy}>
          {busy ? "Đang kiểm tra..." : "Kiểm tra"}
        </button>
        <button className="btn btn-outline" type="button" onClick={() => setCameraOn((v) => !v)}>
          {cameraOn ? "Tắt camera" : "Bật camera quét"}
        </button>
      </form>
      {cameraOn && (
        <div className="scan-frame">
          <video ref={videoRef} muted playsInline />
        </div>
      )}
      {err && <div className="toast err" style={{ marginTop: 16 }}>{err}</div>}
      {ok && <div className="toast" style={{ marginTop: 16 }}>{ok}</div>}
      {ticket && (
        <div className="panel" style={{ marginTop: 16, maxWidth: 480 }}>
          <p><strong>{ticket.ticketCode}</strong></p>
          <p>{ticket.movieTitle}</p>
          <p className="muted">
            {ticket.roomName} · {ticket.startAt?.replace("T", " ").slice(0, 16)} · ghế {ticket.seatLabel}
          </p>
          <p>
            Trạng thái: <strong>{statusLabel(ticket.status)}</strong>
            {ticket.checkedInAt ? ` · ${ticket.checkedInAt.replace("T", " ").slice(0, 16)}` : ""}
          </p>
          <div className="row" style={{ marginTop: 12 }}>
            <button
              type="button"
              className="btn btn-yellow"
              disabled={busy || checked || cancelled}
              onClick={() => void checkIn()}
            >
              {cancelled ? "Vé đã hủy" : checked ? "Đã vào cửa" : "Xác nhận vào cửa"}
            </button>
            {ticket.reservationId ? (
              <Link className="btn btn-outline" to={`/pos/print/${ticket.reservationId}`}>
                In lại
              </Link>
            ) : null}
          </div>
        </div>
      )}
    </div>
  );
}
