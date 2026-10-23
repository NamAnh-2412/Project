import { useEffect, useState } from "react";
import { fetchNotices, type StaffNotice } from "../api/bookingApi";

export default function NoticesPage({ heading }: { heading: string }) {
  const [list, setList] = useState<StaffNotice[]>([]);
  const [err, setErr] = useState("");

  useEffect(() => {
    fetchNotices()
      .then(setList)
      .catch(() => setErr("Không tải được thông báo"));
  }, []);

  return (
    <div className="page">
      <h1>{heading}</h1>
      <p className="muted">Thông báo khi khách hủy vé online. Hoàn đúng số tiền đã trả.</p>
      {err && <div className="toast err">{err}</div>}
      {list.length === 0 && !err && <p className="muted">Chưa có thông báo.</p>}
      {list.map((n) => (
        <div className="panel" key={n.id} style={{ marginBottom: 12 }}>
          <strong>{n.title}</strong>
          <p className="muted">{n.createdAt?.replace("T", " ").slice(0, 16)}</p>
          <p style={{ whiteSpace: "pre-wrap" }}>{n.body}</p>
        </div>
      ))}
    </div>
  );
}
