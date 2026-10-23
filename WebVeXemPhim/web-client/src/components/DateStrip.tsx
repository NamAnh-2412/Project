export function isoDate(d = new Date()) {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${y}-${m}-${day}`;
}

export function nextDays(count = 14) {
  const start = new Date();
  start.setHours(12, 0, 0, 0);
  return Array.from({ length: count }, (_, i) => {
    const d = new Date(start);
    d.setDate(start.getDate() + i);
    return d;
  });
}

function weekday(d: Date, index: number) {
  if (index === 0) return "Hôm nay";
  if (index === 1) return "Ngày mai";
  return ["Chủ nhật", "Thứ hai", "Thứ ba", "Thứ tư", "Thứ năm", "Thứ sáu", "Thứ bảy"][d.getDay()];
}

export default function DateStrip({ value, onChange }: { value: string; onChange: (iso: string) => void }) {
  const days = nextDays(14);
  return (
    <div className="date-strip" role="listbox" aria-label="Chọn ngày chiếu">
      {days.map((d, i) => {
        const iso = isoDate(d);
        return (
          <button
            key={iso}
            type="button"
            className={`date-card ${iso === value ? "on" : ""}`}
            onClick={() => onChange(iso)}
          >
            <small>{weekday(d, i)}</small>
            <strong>{String(d.getDate()).padStart(2, "0")}/{String(d.getMonth() + 1).padStart(2, "0")}</strong>
          </button>
        );
      })}
    </div>
  );
}

export function showBand(startAt: string) {
  const hour = Number(startAt.slice(11, 13));
  if (Number.isNaN(hour) || hour < 13) return "Sáng & trưa";
  if (hour < 18) return "Chiều";
  return "Tối & đêm";
}
