import { useEffect, useRef, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import {
  Area,
  AreaChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { fetchRevenueReport } from "../../api/bookingApi";
import { formatVnd } from "../../lib/money";
import type { RevenueMoney } from "../../api/types";

/* ── helpers ── */

/** Animates a number from 0 → target over `duration` ms. */
function useCountUp(target: number, duration = 800) {
  const [value, setValue] = useState(0);
  const raf = useRef(0);
  useEffect(() => {
    if (!target) { setValue(0); return; }
    const start = performance.now();
    const animate = (now: number) => {
      const t = Math.min((now - start) / duration, 1);
      const eased = 1 - Math.pow(1 - t, 3); // ease-out cubic
      setValue(Math.round(eased * target));
      if (t < 1) raf.current = requestAnimationFrame(animate);
    };
    raf.current = requestAnimationFrame(animate);
    return () => cancelAnimationFrame(raf.current);
  }, [target, duration]);
  return value;
}

/* ── Donut chart (pure SVG) ── */

function Donut({ ratio, color }: { ratio: number; color: string }) {
  const r = 28;
  const circ = 2 * Math.PI * r;
  const filled = circ * Math.max(ratio, 0.01);
  return (
    <div className="rev-donut-wrap">
      <svg viewBox="0 0 72 72">
        <circle className="rev-donut-track" cx="36" cy="36" r={r} />
        <circle
          className="rev-donut-fill"
          cx="36"
          cy="36"
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

type StatCardProps = {
  icon: string;
  iconClass: string;
  label: string;
  hint: string;
  m?: RevenueMoney;
};

function StatCard({ icon, iconClass, label, hint, m }: StatCardProps) {
  const total = useCountUp(Number(m?.total ?? 0));
  return (
    <article className="rev-card" id={`rev-card-${label.toLowerCase().replace(/\s+/g, "-")}`}>
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
        <span>{m?.ticketsSold ?? 0} ghế · {hint}</span>
      </div>
    </article>
  );
}

/* ── Channel Card (with donut) ── */

type ChannelCardProps = {
  icon: string;
  iconClass: string;
  label: string;
  hint: string;
  accent: "online" | "pos";
  accentColor: string;
  m?: RevenueMoney;
  grandTotal?: number;
};

function ChannelCard({ icon, iconClass, label, hint, accent, accentColor, m, grandTotal }: ChannelCardProps) {
  const total = useCountUp(Number(m?.total ?? 0));
  const ticketRatio = m && m.total > 0 ? m.ticket / m.total : 0;
  const shareRatio = grandTotal && grandTotal > 0 ? Number(m?.total ?? 0) / grandTotal : 0;

  return (
    <article className={`rev-card rev-card--${accent}`} id={`rev-channel-${accent}`}>
      <div className="rev-card-header">
        <span className={`rev-card-icon ${iconClass}`}>{icon}</span>
        <span className="rev-card-label">{label}</span>
        {shareRatio > 0 && (
          <span className="rev-chart-badge">{Math.round(shareRatio * 100)}% tổng</span>
        )}
      </div>
      <div className="rev-channel-body">
        <Donut ratio={ticketRatio} color={accentColor} />
        <div>
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
            <span>{m?.ticketsSold ?? 0} ghế · {hint}</span>
          </div>
        </div>
      </div>
    </article>
  );
}

/* ── Horizontal bar rank ── */

function BarRank({ rank }: { rank: number }) {
  const cls =
    rank === 1 ? "rev-bar-rank--1" :
      rank === 2 ? "rev-bar-rank--2" :
        rank === 3 ? "rev-bar-rank--3" : "rev-bar-rank--default";
  return <span className={`rev-bar-rank ${cls}`}>{rank}</span>;
}

/* ── Main Page ── */

export default function PosRevenuePage() {
  const q = useQuery({
    queryKey: ["pos-revenue"],
    queryFn: () => fetchRevenueReport(),
  });
  const data = q.data;

  // Prepare chart data from series
  const chartData = (data?.series ?? []).map((s) => ({
    label: s.label,
    ticket: Number(s.ticket),
    concession: Number(s.concession),
    total: Number(s.total),
  }));

  // Top movies max revenue (for bar width)
  const maxMovieRevenue = Math.max(...(data?.topMovies ?? []).map((m) => m.revenue), 1);
  // Top concessions max revenue
  const maxConcessionRevenue = Math.max(...(data?.topConcessions ?? []).map((p) => p.revenue), 1);
  // Grand total for channel share
  const monthGrand = Number(data?.month?.total ?? 0);

  return (
    <div className="page">
      {/* ── Header ── */}
      <header className="rev-header">
        <h1>Doanh thu</h1>
        <p className="rev-subtitle">
          Tiền đã thu lúc khách thanh toán. Gồm vé đặt online và vé bán tại quầy.
        </p>
      </header>

      {/* ── Error ── */}
      {q.isError && (
        <div className="toast err">Không tải được doanh thu. Thử đăng nhập lại.</div>
      )}

      {/* ── Loading skeleton ── */}
      {q.isPending && (
        <div className="rev-skeleton">
          <div className="rev-skeleton-card" />
          <div className="rev-skeleton-card" />
          <div className="rev-skeleton-card" />
        </div>
      )}

      {data && (
        <>
          {/* ── Top 3 stat cards ── */}
          <div className="rev-stats-row">
            <StatCard icon="☀️" iconClass="rev-card-icon--today" label="Hôm nay" hint="trong ngày" m={data.today} />
            <StatCard icon="📅" iconClass="rev-card-icon--week" label="Tuần này" hint="từ thứ Hai" m={data.week} />
            <StatCard icon="📊" iconClass="rev-card-icon--month" label="Tháng này" hint="từ đầu tháng" m={data.month} />
          </div>

          {/* ── Channel cards with donut ── */}
          <div className="rev-stats-row-2">
            <ChannelCard
              icon="🌐"
              iconClass="rev-card-icon--online"
              label="Online · tháng này"
              hint="khách tự đặt"
              accent="online"
              accentColor="#22c55e"
              m={data.byChannel.online}
              grandTotal={monthGrand}
            />
            <ChannelCard
              icon="🏪"
              iconClass="rev-card-icon--pos"
              label="Quầy · tháng này"
              hint="nhân viên bán"
              accent="pos"
              accentColor="#3b82f6"
              m={data.byChannel.pos}
              grandTotal={monthGrand}
            />
          </div>

          {/* ── Area chart ── */}
          {chartData.length > 0 && (
            <section className="rev-chart-section" id="rev-chart-trend">
              <div className="rev-chart-header">
                <h2 className="rev-chart-title">
                  📈 Xu hướng doanh thu
                  <span className="rev-chart-badge">{chartData.length} ngày</span>
                </h2>
              </div>
              <div style={{ width: "100%", height: 240 }}>
                <ResponsiveContainer width="100%" height="100%">
                  <AreaChart data={chartData}>
                    <defs>
                      <linearGradient id="revGradTicket" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="0%" stopColor="#f5c518" stopOpacity={0.35} />
                        <stop offset="100%" stopColor="#f5c518" stopOpacity={0.02} />
                      </linearGradient>
                      <linearGradient id="revGradConcession" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="0%" stopColor="#22c55e" stopOpacity={0.3} />
                        <stop offset="100%" stopColor="#22c55e" stopOpacity={0.02} />
                      </linearGradient>
                    </defs>
                    <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.06)" />
                    <XAxis dataKey="label" tick={{ fill: "#a1a1aa", fontSize: 11 }} />
                    <YAxis tick={{ fill: "#a1a1aa", fontSize: 11 }} />
                    <Tooltip
                      contentStyle={{
                        background: "rgba(17, 8, 20, 0.92)",
                        border: "1px solid rgba(245, 197, 24, 0.32)",
                        borderRadius: 12,
                        color: "#fff",
                        backdropFilter: "blur(12px)",
                      }}
                      formatter={(value, name) => [formatVnd(Number(value ?? 0)), String(name)]}
                    />
                    <Area
                      type="monotone"
                      dataKey="ticket"
                      name="Vé"
                      stroke="#f5c518"
                      strokeWidth={2}
                      fill="url(#revGradTicket)"
                    />
                    <Area
                      type="monotone"
                      dataKey="concession"
                      name="Bắp nước"
                      stroke="#22c55e"
                      strokeWidth={2}
                      fill="url(#revGradConcession)"
                    />
                  </AreaChart>
                </ResponsiveContainer>
              </div>
              <div className="rev-chart-summary">
                <span>
                  Tổng khoảng: <strong>{formatVnd(data.range.totals.total)}</strong>
                </span>
                <span>Vé {formatVnd(data.range.totals.ticket)}</span>
                <span>Đồ ăn vặt {formatVnd(data.range.totals.concession)}</span>
              </div>
            </section>
          )}

          {/* ── Bottom grid: top movies + top concessions ── */}
          <div className="rev-bottom-grid">
            {/* Top movies */}
            <section className="rev-ranking" id="rev-top-movies">
              <h2 className="rev-ranking-title">🏆 Phim bán chạy trong tháng</h2>
              {(data.topMovies ?? []).length === 0 ? (
                <p className="rev-empty">Chưa có vé trong tháng này.</p>
              ) : (
                <ul className="rev-bar-list">
                  {data.topMovies.map((m, i) => (
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
            <section className="rev-ranking" id="rev-top-concessions">
              <h2 className="rev-ranking-title">🍿 Đồ ăn vặt bán chạy</h2>
              {(data.topConcessions ?? []).length === 0 ? (
                <p className="rev-empty">Chưa bán đồ ăn vặt trong tháng này.</p>
              ) : (
                <ul className="rev-bar-list">
                  {data.topConcessions.map((p, i) => (
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
        </>
      )}
    </div>
  );
}
