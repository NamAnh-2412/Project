import { useState, useEffect, useRef } from "react";
import { useQuery } from "@tanstack/react-query";
import {
  Area,
  AreaChart,
  Bar,
  BarChart,
  CartesianGrid,
  Legend,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { fetchRevenueReport } from "../../api/bookingApi";
import { formatVnd } from "../../lib/money";
import type { RevenueMoney } from "../../api/types";

/* ── helpers ── */

function pad(n: number) {
  return String(n).padStart(2, "0");
}

function isoDay(d: Date) {
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}

function monthStart() {
  const d = new Date();
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-01`;
}

/** Animate a number from 0 → target. */
function useCountUp(target: number, duration = 800) {
  const [value, setValue] = useState(0);
  const raf = useRef(0);
  useEffect(() => {
    if (!target) { setValue(0); return; }
    const start = performance.now();
    const animate = (now: number) => {
      const t = Math.min((now - start) / duration, 1);
      const eased = 1 - Math.pow(1 - t, 3);
      setValue(Math.round(eased * target));
      if (t < 1) raf.current = requestAnimationFrame(animate);
    };
    raf.current = requestAnimationFrame(animate);
    return () => cancelAnimationFrame(raf.current);
  }, [target, duration]);
  return value;
}

/* ── Donut chart (SVG) ── */

function Donut({ ratio, color, size = 72 }: { ratio: number; color: string; size?: number }) {
  const r = (size - 16) / 2;
  const circ = 2 * Math.PI * r;
  const filled = circ * Math.max(ratio, 0.01);
  return (
    <div className="rev-donut-wrap" style={{ width: size, height: size }}>
      <svg viewBox={`0 0 ${size} ${size}`} style={{ width: size, height: size }}>
        <circle className="rev-donut-track" cx={size / 2} cy={size / 2} r={r} />
        <circle
          className="rev-donut-fill"
          cx={size / 2}
          cy={size / 2}
          r={r}
          stroke={color}
          strokeDasharray={`${filled} ${circ - filled}`}
        />
      </svg>
      <span className="rev-donut-label">{Math.round(ratio * 100)}%</span>
    </div>
  );
}

/* ── Stat Card ── */

function StatCard({ icon, iconClass, label, m }: { icon: string; iconClass: string; label: string; m?: RevenueMoney }) {
  const total = useCountUp(Number(m?.total ?? 0));
  return (
    <article className="rev-card">
      <div className="rev-card-header">
        <span className={`rev-card-icon ${iconClass}`}>{icon}</span>
        <span className="rev-card-label">{label}</span>
      </div>
      <strong className="rev-card-total">{total.toLocaleString("vi-VN")}đ</strong>
      <div className="rev-card-breakdown">
        <span className="rev-tag rev-tag--ticket">
          <span className="rev-tag-icon">🎫</span> Vé {formatVnd(m?.ticket)}
        </span>
        <span className="rev-tag rev-tag--food">
          <span className="rev-tag-icon">🍿</span> Đồ ăn {formatVnd(m?.concession)}
        </span>
      </div>
      <div className="rev-card-footer">
        <span>{m?.ticketsSold ?? 0} ghế đã bán</span>
      </div>
    </article>
  );
}

/* ── Bar rank badge ── */

function BarRank({ rank }: { rank: number }) {
  const cls =
    rank === 1 ? "rev-bar-rank--1" :
      rank === 2 ? "rev-bar-rank--2" :
        rank === 3 ? "rev-bar-rank--3" : "rev-bar-rank--default";
  return <span className={`rev-bar-rank ${cls}`}>{rank}</span>;
}

/* ── Chart type toggle ── */

type ChartType = "area" | "bar";

/* ── Main ── */

export default function ReportAdminPage() {
  const [from, setFrom] = useState(monthStart);
  const [to, setTo] = useState(isoDay(new Date()));
  const [grain, setGrain] = useState("DAY");
  const [chartType, setChartType] = useState<ChartType>("area");

  const q = useQuery({
    queryKey: ["admin-revenue", from, to, grain],
    queryFn: () => fetchRevenueReport({ from, to, grain }),
  });
  const data = q.data;
  const chart = (data?.series ?? []).map((s) => ({
    label: s.label,
    ticket: Number(s.ticket),
    concession: Number(s.concession),
    total: Number(s.total),
  }));

  const maxMovieRevenue = Math.max(...(data?.topMovies ?? []).map((m) => m.revenue), 1);
  const maxConcessionRevenue = Math.max(...(data?.topConcessions ?? []).map((p) => p.revenue), 1);
  const monthGrand = Number(data?.month?.total ?? 0);
  const onlineRatio = monthGrand > 0 ? Number(data?.byChannel.online.total ?? 0) / monthGrand : 0;
  const posRatio = monthGrand > 0 ? Number(data?.byChannel.pos.total ?? 0) / monthGrand : 0;

  const tooltipStyle = {
    background: "rgba(17, 8, 20, 0.92)",
    border: "1px solid rgba(245, 197, 24, 0.32)",
    borderRadius: 12,
    color: "#fff",
    backdropFilter: "blur(12px)",
  };

  return (
    <div>
      {/* ── Header ── */}
      <header className="rev-header">
        <h1>Báo cáo doanh thu</h1>
        <p className="rev-subtitle">
          Doanh thu theo <strong style={{ color: "var(--ink)" }}>lúc thanh toán</strong>. Tách vé và đồ ăn vặt.
        </p>
      </header>

      {/* ── Error ── */}
      {q.isError && (
        <div className="toast err" style={{ marginBottom: 16 }}>
          Không tải được báo cáo. Đăng nhập admin rồi thử lại.
        </div>
      )}

      {/* ── Loading skeleton ── */}
      {q.isPending && (
        <div className="rev-skeleton">
          <div className="rev-skeleton-card" />
          <div className="rev-skeleton-card" />
          <div className="rev-skeleton-card" />
        </div>
      )}

      {/* ── Top 3 stat cards ── */}
      <div className="rev-stats-row" style={{ marginBottom: 20 }}>
        <StatCard icon="☀️" iconClass="rev-card-icon--today" label="Hôm nay" m={data?.today} />
        <StatCard icon="📅" iconClass="rev-card-icon--week" label="Tuần này" m={data?.week} />
        <StatCard icon="📊" iconClass="rev-card-icon--month" label="Tháng này" m={data?.month} />
      </div>

      {/* ── Channel summary row ── */}
      {data && (
        <div className="rev-stats-row-2" style={{ marginBottom: 20 }}>
          {/* Online */}
          <article className="rev-card rev-card--online">
            <div className="rev-card-header">
              <span className="rev-card-icon rev-card-icon--online">🌐</span>
              <span className="rev-card-label">Online · tháng này</span>
              {onlineRatio > 0 && (
                <span className="rev-chart-badge">{Math.round(onlineRatio * 100)}% tổng</span>
              )}
            </div>
            <div className="rev-channel-body">
              <Donut
                ratio={
                  Number(data.byChannel.online.total ?? 0) > 0
                    ? data.byChannel.online.ticket / data.byChannel.online.total
                    : 0
                }
                color="#22c55e"
              />
              <div>
                <strong className="rev-card-total" style={{ fontSize: 26 }}>
                  {formatVnd(data.byChannel.online.total)}
                </strong>
                <div className="rev-card-breakdown">
                  <span className="rev-tag rev-tag--ticket">
                    <span className="rev-tag-icon">🎫</span> {formatVnd(data.byChannel.online.ticket)}
                  </span>
                  <span className="rev-tag rev-tag--food">
                    <span className="rev-tag-icon">🍿</span> {formatVnd(data.byChannel.online.concession)}
                  </span>
                </div>
                <div className="rev-card-footer">{data.byChannel.online.ticketsSold} ghế · khách tự đặt</div>
              </div>
            </div>
          </article>

          {/* POS */}
          <article className="rev-card rev-card--pos">
            <div className="rev-card-header">
              <span className="rev-card-icon rev-card-icon--pos">🏪</span>
              <span className="rev-card-label">Quầy · tháng này</span>
              {posRatio > 0 && (
                <span className="rev-chart-badge">{Math.round(posRatio * 100)}% tổng</span>
              )}
            </div>
            <div className="rev-channel-body">
              <Donut
                ratio={
                  Number(data.byChannel.pos.total ?? 0) > 0
                    ? data.byChannel.pos.ticket / data.byChannel.pos.total
                    : 0
                }
                color="#3b82f6"
              />
              <div>
                <strong className="rev-card-total" style={{ fontSize: 26 }}>
                  {formatVnd(data.byChannel.pos.total)}
                </strong>
                <div className="rev-card-breakdown">
                  <span className="rev-tag rev-tag--ticket">
                    <span className="rev-tag-icon">🎫</span> {formatVnd(data.byChannel.pos.ticket)}
                  </span>
                  <span className="rev-tag rev-tag--food">
                    <span className="rev-tag-icon">🍿</span> {formatVnd(data.byChannel.pos.concession)}
                  </span>
                </div>
                <div className="rev-card-footer">{data.byChannel.pos.ticketsSold} ghế · nhân viên bán</div>
              </div>
            </div>
          </article>
        </div>
      )}

      {/* ── Date range filters ── */}
      <section className="rev-chart-section">
        <div className="rev-chart-header">
          <h2 className="rev-chart-title">
            📈 Biểu đồ khoảng chọn
            {chart.length > 0 && <span className="rev-chart-badge">{chart.length} mốc</span>}
          </h2>
          <div style={{ display: "flex", gap: 6 }}>
            <button
              type="button"
              className={`rev-chart-type-btn ${chartType === "area" ? "rev-chart-type-btn--on" : ""}`}
              onClick={() => setChartType("area")}
              title="Area chart"
            >
              📈
            </button>
            <button
              type="button"
              className={`rev-chart-type-btn ${chartType === "bar" ? "rev-chart-type-btn--on" : ""}`}
              onClick={() => setChartType("bar")}
              title="Bar chart"
            >
              📊
            </button>
          </div>
        </div>

        {/* Filters row */}
        <div style={{ display: "flex", flexWrap: "wrap", gap: 12, marginBottom: 16, alignItems: "flex-end" }}>
          <label className="rev-filter-label">
            <span>Từ ngày</span>
            <input type="date" value={from} onChange={(e) => setFrom(e.target.value)} />
          </label>
          <label className="rev-filter-label">
            <span>Đến ngày</span>
            <input type="date" value={to} onChange={(e) => setTo(e.target.value)} />
          </label>
          <label className="rev-filter-label">
            <span>Nhóm theo</span>
            <select value={grain} onChange={(e) => setGrain(e.target.value)}>
              <option value="DAY">Ngày</option>
              <option value="WEEK">Tuần</option>
              <option value="MONTH">Tháng</option>
            </select>
          </label>
        </div>

        {/* Chart */}
        {chart.length === 0 ? (
          <p className="rev-empty">Chưa có doanh thu trong khoảng này.</p>
        ) : (
          <div style={{ width: "100%", height: 280 }}>
            <ResponsiveContainer width="100%" height="100%">
              {chartType === "area" ? (
                <AreaChart data={chart}>
                  <defs>
                    <linearGradient id="adminGradTicket" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0%" stopColor="#f5c518" stopOpacity={0.35} />
                      <stop offset="100%" stopColor="#f5c518" stopOpacity={0.02} />
                    </linearGradient>
                    <linearGradient id="adminGradConcession" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0%" stopColor="#22c55e" stopOpacity={0.3} />
                      <stop offset="100%" stopColor="#22c55e" stopOpacity={0.02} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.06)" />
                  <XAxis dataKey="label" tick={{ fill: "#a1a1aa", fontSize: 11 }} />
                  <YAxis tick={{ fill: "#a1a1aa", fontSize: 11 }} />
                  <Tooltip contentStyle={tooltipStyle} formatter={(value, name) => [formatVnd(Number(value ?? 0)), String(name)]} />
                  <Legend />
                  <Area type="monotone" dataKey="ticket" name="Vé" stroke="#f5c518" strokeWidth={2} fill="url(#adminGradTicket)" />
                  <Area type="monotone" dataKey="concession" name="Bắp nước" stroke="#22c55e" strokeWidth={2} fill="url(#adminGradConcession)" />
                </AreaChart>
              ) : (
                <BarChart data={chart}>
                  <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.06)" />
                  <XAxis dataKey="label" tick={{ fill: "#a1a1aa", fontSize: 11 }} />
                  <YAxis tick={{ fill: "#a1a1aa", fontSize: 11 }} />
                  <Tooltip contentStyle={tooltipStyle} formatter={(value, name) => [formatVnd(Number(value ?? 0)), String(name)]} />
                  <Legend />
                  <Bar dataKey="ticket" name="Vé" stackId="money" fill="#f5c518" radius={[4, 4, 0, 0]} />
                  <Bar dataKey="concession" name="Bắp nước" stackId="money" fill="#22c55e" radius={[4, 4, 0, 0]} />
                </BarChart>
              )}
            </ResponsiveContainer>
          </div>
        )}

        {/* Summary */}
        <div className="rev-chart-summary">
          <span>Tổng khoảng: <strong>{formatVnd(data?.range.totals.total)}</strong></span>
          <span>Vé {formatVnd(data?.range.totals.ticket)}</span>
          <span>Đồ ăn vặt {formatVnd(data?.range.totals.concession)}</span>
          <span>ONLINE {formatVnd(data?.byChannel.online.total)}</span>
          <span>POS {formatVnd(data?.byChannel.pos.total)}</span>
        </div>
      </section>

      {/* ── Bottom grid: top movies + top concessions ── */}
      <div className="rev-bottom-grid">
        {/* Top movies */}
        <section className="rev-ranking" id="admin-top-movies">
          <h2 className="rev-ranking-title">🏆 Top phim xem nhiều</h2>
          {(data?.topMovies ?? []).length === 0 ? (
            <p className="rev-empty">Chưa có vé trong khoảng này.</p>
          ) : (
            <ul className="rev-bar-list">
              {(data?.topMovies ?? []).map((m, i) => (
                <li className="rev-bar-item" key={`${m.movieId}-${m.movieTitle}`}>
                  <div className="rev-bar-label">
                    <span className="rev-bar-name">
                      <BarRank rank={i + 1} />
                      <span>{m.movieTitle}</span>
                    </span>
                    <span className="rev-bar-value">
                      <span className="rev-bar-tickets">{m.tickets} vé</span>
                      {" "}
                      {formatVnd(m.revenue)}
                    </span>
                  </div>
                  <div className="rev-bar-track">
                    <div
                      className="rev-bar-fill"
                      style={{ width: `${(m.revenue / maxMovieRevenue) * 100}%` }}
                    />
                  </div>
                </li>
              ))}
            </ul>
          )}
        </section>

        {/* Top concessions */}
        <section className="rev-ranking" id="admin-top-concessions">
          <h2 className="rev-ranking-title">🍿 Top đồ ăn vặt</h2>
          {(data?.topConcessions ?? []).length === 0 ? (
            <p className="rev-empty">Chưa bán đồ ăn vặt trong khoảng này.</p>
          ) : (
            <ul className="rev-bar-list">
              {(data?.topConcessions ?? []).map((p, i) => (
                <li className="rev-bar-item" key={`${p.productId}-${p.name}`}>
                  <div className="rev-bar-label">
                    <span className="rev-bar-name">
                      <BarRank rank={i + 1} />
                      <span>{p.name}</span>
                    </span>
                    <span className="rev-bar-value">
                      <span className="rev-bar-qty">{p.qty} phần</span>
                      {" "}
                      {formatVnd(p.revenue)}
                    </span>
                  </div>
                  <div className="rev-bar-track">
                    <div
                      className="rev-bar-fill rev-bar-fill--concession"
                      style={{ width: `${(p.revenue / maxConcessionRevenue) * 100}%` }}
                    />
                  </div>
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>
    </div>
  );
}
