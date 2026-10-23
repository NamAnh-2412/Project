import { useEffect, useState } from "react";
import QRCode from "qrcode";

export default function TicketQr({ value }: { value: string }) {
  const [src, setSrc] = useState("");
  useEffect(() => {
    let alive = true;
    QRCode.toDataURL(value, { margin: 1, width: 168 })
      .then((url) => {
        if (alive) setSrc(url);
      })
      .catch(() => {
        if (alive) setSrc("");
      });
    return () => {
      alive = false;
    };
  }, [value]);
  if (!src) return <div className="ticket-qr" style={{ width: 168, height: 168 }} />;
  return <img className="ticket-qr" src={src} width={168} height={168} alt="Mã QR vé" />;
}
