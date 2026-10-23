import { useEffect, useState } from "react";
import { Link, useLocation, useNavigate, useParams } from "react-router-dom";
import { fetchMovie, fetchShowtimes } from "../api/catalogApi";
import { fetchReviews, saveReview } from "../api/bookingApi";
import type { Movie, Review, Showtime } from "../api/types";
import Poster from "../components/Poster";
import TrailerModal from "../components/TrailerModal";
import DateStrip, { isoDate, showBand } from "../components/DateStrip";
import { useAuth } from "../context/AuthContext";
import { youtubeEmbedSrc, youtubeId } from "../lib/youtube";

function today() {
  return isoDate();
}

function formatDate(iso?: string) {
  if (!iso) return "";
  const [y, m, d] = iso.split("-");
  if (!y || !m || !d) return iso;
  return `${d}/${m}/${y}`;
}

const STATUS_LABEL: Record<string, string> = {
  SHOWING: "Đang chiếu",
  COMING: "Sắp chiếu",
  HIDDEN: "Ngừng chiếu",
};

export default function MovieDetailPage() {
  const { movieId } = useParams();
  const id = Number(movieId);
  const { user } = useAuth();
  const nav = useNavigate();
  const loc = useLocation();
  const [movie, setMovie] = useState<Movie | null>(null);
  const [date, setDate] = useState(today());
  const [shows, setShows] = useState<Showtime[]>([]);
  const [err, setErr] = useState("");
  const [trailerOpen, setTrailerOpen] = useState(false);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [stars, setStars] = useState(5);
  const [comment, setComment] = useState("");
  const [reviewMsg, setReviewMsg] = useState("");

  useEffect(() => {
    if (!Number.isFinite(id) || id < 1) {
      setErr("Không tìm thấy phim");
      setMovie(null);
      return;
    }
    setErr("");
    fetchMovie(id)
      .then(setMovie)
      .catch(() => {
        setMovie(null);
        setErr("Không tìm thấy phim");
      });
  }, [id]);

  useEffect(() => {
    if (!Number.isFinite(id) || id < 1) return;
    fetchReviews(id).then(setReviews).catch(() => setReviews([]));
  }, [id]);

  useEffect(() => {
    if (!movie || movie.status !== "SHOWING") {
      setShows([]);
      return;
    }
    fetchShowtimes(id, date).then(setShows).catch(() => setShows([]));
  }, [id, date, movie]);

  useEffect(() => {
    if (!movie || loc.hash !== "#lich-chieu") return;
    document.getElementById("lich-chieu")?.scrollIntoView({ behavior: "smooth" });
  }, [movie, loc.hash]);

  const trailer = youtubeId(movie?.trailerUrl);
  const canBook = movie?.status === "SHOWING" && user?.role !== "ADMIN";

  const pick = (sid: number) => {
    if (user?.role === "ADMIN") return;
    if (user?.role === "CASHIER") {
      nav(`/pos/book/${sid}`);
      return;
    }
    if (!user) {
      nav("/login", { state: { from: `/book/${sid}` } });
      return;
    }
    nav(`/book/${sid}`);
  };

  return (
    <div>
      {movie && (
        <section className="hero hero-detail">
          <div className="hero-art"><Poster movie={movie} wide /></div>
          <div className="hero-fade" />
          <div className="hero-inner">
            <div>
              <div className="chips" style={{ marginBottom: 10 }}>
                <span className="badge">{movie.rated}</span>
                <span className="chip">{STATUS_LABEL[movie.status] || movie.status}</span>
              </div>
              <h1>{movie.title}</h1>
              <div className="chips">
                {movie.genreName && <span className="chip">{movie.genreName}</span>}
                <span className="chip">{movie.durationMinutes} phút</span>
                {movie.releaseDate && <span className="chip">KC {formatDate(movie.releaseDate)}</span>}
                {(movie.ratingCount ?? 0) > 0 && (
                  <span className="chip">{Number(movie.ratingAverage).toFixed(1)} ★ · {movie.ratingCount} đánh giá</span>
                )}
              </div>
              <div className="hero-actions">
                {movie.status === "SHOWING" && (
                  <a className="play-btn" href="#lich-chieu" title="Chọn suất"><i /></a>
                )}
                {trailer && (
                  <button type="button" className="trailer-btn" onClick={() => setTrailerOpen(true)}>
                    Xem trailer
                  </button>
                )}
              </div>
            </div>
          </div>
        </section>
      )}

      <div className="page">
        {err && <div className="toast err">{err}</div>}
        {movie?.status === "HIDDEN" && (
          <div className="toast">Phim đang ngừng chiếu — chỉ admin xem được trang này.</div>
        )}
        {movie?.status === "COMING" && (
          <div className="toast">Phim sắp chiếu — chưa mở bán vé.</div>
        )}

        {movie && (
          <div className="movie-detail" style={{ gridTemplateColumns: "minmax(0, 1fr)" }}>
            <div>
              <h2 style={{ marginTop: 0 }}>Thông tin phim</h2>
              <dl className="movie-dl">
                <dt>Thể loại</dt>
                <dd>{movie.genreName || "—"}</dd>
                <dt>Thời lượng</dt>
                <dd>{movie.durationMinutes} phút</dd>
                <dt>Giới hạn tuổi</dt>
                <dd>{movie.rated || "—"}</dd>
                <dt>Đạo diễn</dt>
                <dd>{movie.director || "—"}</dd>
                <dt>Diễn viên</dt>
                <dd>{movie.castNames || "—"}</dd>
                <dt>Khởi chiếu</dt>
                <dd>{formatDate(movie.releaseDate) || "—"}</dd>
              </dl>
              <p className="movie-desc">
                {movie.description || "Chưa có mô tả."}
              </p>
              {trailer && movie.trailerUrl && (
                <div className="trailer-frame" style={{ marginTop: 20 }}>
                  <iframe
                    title={`${movie.title} trailer`}
                    src={youtubeEmbedSrc(trailer)}
                    allow="accelerometer; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                    allowFullScreen
                  />
                </div>
              )}
            </div>
          </div>
        )}

        {movie?.status === "SHOWING" && (
          <div className="panel" id="lich-chieu">
            <h2 style={{ marginTop: 0 }}>Lịch chiếu</h2>
            {user?.role === "ADMIN" && (
              <p className="muted">Bạn đang xem với tài khoản admin — không đặt vé tại đây.</p>
            )}
            <DateStrip value={date} onChange={setDate} />
            {["Sáng & trưa", "Chiều", "Tối & đêm"].map((band) => {
              const rows = shows.filter((s) => showBand(s.startAt) === band);
              if (rows.length === 0) return null;
              return (
                <div className="show-band" key={band}>
                  <h3 style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 10 }}>
                    <span>{band}</span>
                    <span className="muted" style={{ fontSize: 12, fontWeight: 500 }}>({rows.length} suất)</span>
                  </h3>
                  <div className="chips" style={{ gap: 10 }}>
                    {rows.map((s) => {
                      const left = s.capacity != null ? Number(s.capacity) - Number(s.booked ?? 0) - Number(s.hold ?? 0) : null;
                      const roomLower = s.roomName?.toLowerCase() || "";
                      const isVip = roomLower.includes("vip");
                      const is3d = roomLower.includes("3d") || roomLower.includes("imax");
                      const formatLabel = isVip ? "VIP Lounge" : is3d ? "3D Phụ đề" : "2D Phụ đề";
                      const formatClass = isVip ? "badge-vip" : is3d ? "badge-3d" : "badge-2d";
                      const isSoldOut = left != null && left <= 0;

                      return (
                        <button
                          key={s.id}
                          className="showtime-card-chip"
                          type="button"
                          disabled={!canBook || isSoldOut}
                          onClick={() => pick(s.id)}
                          title={`${s.startAt.slice(11, 16)} · ${s.roomName} (${formatLabel})`}
                        >
                          <div className="showtime-time-row">
                            <span className="showtime-time">{s.startAt.slice(11, 16)}</span>
                            <span className={`format-badge ${formatClass}`}>{formatLabel}</span>
                          </div>
                          <div className="showtime-meta-row">
                            <span>{s.roomName}</span>
                            <span>·</span>
                            <span style={{ fontWeight: 700 }}>{Number(s.basePrice).toLocaleString()}đ</span>
                            {left != null && (
                              <>
                                <span>·</span>
                                <span className={`seat-badge ${isSoldOut ? "full" : left <= 10 ? "warn" : "ok"}`}>
                                  {isSoldOut ? "Hết ghế" : `Còn ${left} ghế`}
                                </span>
                              </>
                            )}
                          </div>
                        </button>
                      );
                    })}
                  </div>
                </div>
              );
            })}
            {shows.length === 0 && <p className="muted">Không có suất ngày này.</p>}
          </div>
        )}

        <section style={{ marginTop: 28 }}>
          <h2>Đánh giá</h2>
          <p className="muted">Chỉ gửi được sau khi vé của bạn đã được soát tại cửa.</p>
          {user?.role === "CUSTOMER" && (
            <form
              className="panel"
              onSubmit={(e) => {
                e.preventDefault();
                saveReview(id, stars, comment)
                  .then((saved) => {
                    setReviews((list) => [saved, ...list.filter((r) => r.id !== saved.id)]);
                    setReviewMsg("Đã lưu đánh giá");
                    fetchMovie(id).then(setMovie).catch(() => undefined);
                  })
                  .catch(() => setReviewMsg("Chỉ đánh giá khi bạn đã xem phim và vé đã soát"));
              }}
            >
              <label>
                Số sao
                <select value={stars} onChange={(e) => setStars(Number(e.target.value))}>
                  {[5, 4, 3, 2, 1].map((n) => <option key={n} value={n}>{n}</option>)}
                </select>
              </label>
              <label>
                Nhận xét
                <textarea maxLength={500} value={comment} onChange={(e) => setComment(e.target.value)} rows={3} />
              </label>
              <button className="btn btn-yellow" type="submit">Gửi đánh giá</button>
              {reviewMsg && <p className="muted">{reviewMsg}</p>}
            </form>
          )}
          {reviews.length === 0 && <p className="muted">Chưa có đánh giá.</p>}
          {reviews.map((r) => (
            <article key={r.id} className="panel">
              <strong>{r.username}</strong> · {"★".repeat(r.rating)}
              {r.comment && <p>{r.comment}</p>}
            </article>
          ))}
        </section>

        <p style={{ marginTop: 24 }}>
          <Link to="/">← Về trang phim</Link>
          {user?.role === "ADMIN" && (
            <>
              {" · "}
              <Link to="/admin/movies">Quản lý phim</Link>
            </>
          )}
        </p>
      </div>

      {trailerOpen && movie?.trailerUrl && (
        <TrailerModal url={movie.trailerUrl} title={movie.title} onClose={() => setTrailerOpen(false)} />
      )}
    </div>
  );
}
