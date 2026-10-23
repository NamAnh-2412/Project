import { useEffect, useMemo, useRef, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { fetchGenres, fetchMovies, fetchShowtimes } from "../api/catalogApi";
import type { Movie, Showtime } from "../api/types";
import Poster from "../components/Poster";
import TrailerModal from "../components/TrailerModal";
import { EmptyState, Skeleton } from "../components/EmptyState";
import { useBrand } from "../context/BrandContext";
import { assetUrl } from "../lib/assetUrl";
import { youtubeId } from "../lib/youtube";
import { isoDate, nextDays } from "../components/DateStrip";

function weekdayLabel(d: Date, index: number) {
  if (index === 0) return "Hôm nay";
  if (index === 1) return "Ngày mai";
  return ["CN", "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7"][d.getDay()];
}

function formatDateDisplay(iso: string) {
  const [y, m, d] = iso.split("-");
  return `${d}/${m}/${y}`;
}

export default function HomePage() {
  const [params, setParams] = useSearchParams();
  const q = params.get("q") ?? "";
  const tab = params.get("tab") === "coming" ? "COMING" : "SHOWING";
  const genre = params.get("genre") ?? "";

  const [showing, setShowing] = useState<Movie[]>([]);
  const [coming, setComing] = useState<Movie[]>([]);
  const [genres, setGenres] = useState<{ id: number; name: string }[]>([]);
  const [heroIdx, setHeroIdx] = useState(0);
  const [trailerOpen, setTrailerOpen] = useState(false);
  const [err, setErr] = useState("");
  const [loading, setLoading] = useState(true);
  const [selectedDate, setSelectedDate] = useState("");
  const [dateShowtimes, setDateShowtimes] = useState<Showtime[]>([]);
  const [loadingDateShows, setLoadingDateShows] = useState(false);
  const { name, imageUrl } = useBrand();
  const cinemaBg = assetUrl(imageUrl);

  const days = useMemo(() => nextDays(8), []);

  useEffect(() => {
    Promise.all([
      fetchMovies({ status: "SHOWING", size: 24, sort: "title,asc" }),
      fetchMovies({ status: "COMING", size: 24, sort: "title,asc" }),
      fetchGenres(),
    ])
      .then(([a, b, g]) => {
        setShowing(a.content);
        setComing(b.content);
        setGenres(g);
      })
      .catch(() => setErr("Không tải được phim. Kiểm tra API :8080."))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    if (!selectedDate) {
      setDateShowtimes([]);
      return;
    }
    setLoadingDateShows(true);
    fetchShowtimes(undefined, selectedDate)
      .then(setDateShowtimes)
      .catch(() => setDateShowtimes([]))
      .finally(() => setLoadingDateShows(false));
  }, [selectedDate]);

  const showtimesByMovie = useMemo(() => {
    const map = new Map<number, Showtime[]>();
    for (const s of dateShowtimes) {
      const list = map.get(s.movieId) ?? [];
      list.push(s);
      map.set(s.movieId, list);
    }
    return map;
  }, [dateShowtimes]);

  const dateFilteredMovies = useMemo(() => {
    if (!selectedDate) return [];
    return showing.filter((m) => showtimesByMovie.has(m.id));
  }, [selectedDate, showing, showtimesByMovie]);

  const all = useMemo(() => [...showing, ...coming], [showing, coming]);
  useEffect(() => {
    if (showing.length < 2) return;
    const timer = window.setInterval(() => {
      setHeroIdx((i) => (i + 1) % showing.length);
    }, 5000);
    return () => window.clearInterval(timer);
  }, [showing.length]);

  const featured = showing[heroIdx] ?? showing[0];
  const searching = Boolean(q || genre || params.get("tab"));

  const filtered = useMemo(() => {
    const src = tab === "COMING" ? coming : q || genre ? all : showing;
    return src.filter((m) => {
      const okQ = !q || m.title.toLowerCase().includes(q.toLowerCase());
      const okG = !genre || (m.genreName || "") === genre;
      return okQ && okG;
    });
  }, [all, showing, coming, q, genre, tab]);

  const setGenre = (genreName: string) => {
    const next = new URLSearchParams(params);
    if (genreName) next.set("genre", genreName);
    else next.delete("genre");
    next.delete("tab");
    setParams(next);
  };

  return (
    <div>
      {err && <div className="page"><div className="toast err">{err}</div></div>}

      {!searching && featured && (
        <section className="hero">
          <div className="hero-art">
            {cinemaBg ? <img src={cinemaBg} alt="" /> : <Poster movie={featured} wide />}
          </div>
          <div className="hero-fade" />
          <div className="hero-inner">
            <div key={featured.id} className="hero-copy">
              <h1>{featured.title}</h1>
              <p className="hero-desc">{featured.description || `Đặt vé online tại ${name} — một rạp, không trùng ghế.`}</p>
              <div className="chips">
                <span className="badge">{featured.rated}</span>
                <span className="chip">{featured.genreName}</span>
                <span className="chip">{featured.durationMinutes} phút</span>
              </div>
              <div className="hero-actions">
                <Link className="play-btn" to={`/movies/${featured.id}#lich-chieu`} title="Mua vé"><i /></Link>
                {youtubeId(featured.trailerUrl) && (
                  <button type="button" className="trailer-btn" onClick={() => setTrailerOpen(true)}>
                    Xem trailer
                  </button>
                )}
                <Link className="round-btn" to={`/movies/${featured.id}`} title="Chi tiết">i</Link>
              </div>
              <div className="hero-dots">
                {showing.slice(0, 6).map((m, i) => (
                  <button key={m.id} type="button" className={i === heroIdx ? "on" : ""} aria-label={m.title} onClick={() => setHeroIdx(i)} />
                ))}
              </div>
              <div className="hero-progress" />
            </div>
            <div className="hero-thumbs">
              {showing.slice(0, 6).map((m, i) => (
                <button
                  key={m.id}
                  className={`hero-thumb ${i === heroIdx ? "on" : ""}`}
                  onClick={() => setHeroIdx(i)}
                  title={m.title}
                >
                  <Poster movie={m} wide />
                </button>
              ))}
            </div>
          </div>
        </section>
      )}

      <div className="home-body">
        {loading && (
          <div className="grid">
            <Skeleton height={220} />
            <Skeleton height={220} />
            <Skeleton height={220} />
          </div>
        )}

        {!searching && (
          <>
            <h2 className="section-title" id="chu-de">Bạn đang quan tâm gì?</h2>
            <div className="genre-pills">
              {genres.map((g) => (
                <button key={g.id} type="button" className="genre-pill" onClick={() => setGenre(g.name)}>
                  {g.name}
                </button>
              ))}
            </div>

            {/* ── Quick Date Filter Bar (Phase 2 Deliverable D2.1) ── */}
            <div className="quick-date-bar">
              <div className="quick-date-head">
                <h2 className="section-title" style={{ margin: 0 }}>
                  {selectedDate ? `Lịch chiếu ngày ${formatDateDisplay(selectedDate)}` : "Lọc suất chiếu theo ngày"}
                </h2>
                {selectedDate && (
                  <button
                    type="button"
                    className="muted"
                    style={{ background: "none", border: 0, cursor: "pointer", fontWeight: 700 }}
                    onClick={() => setSelectedDate("")}
                  >
                    Xem tất cả ›
                  </button>
                )}
              </div>
              <div className="quick-date-scroll">
                <button
                  type="button"
                  className={`quick-date-btn ${selectedDate === "" ? "is-active" : ""}`}
                  onClick={() => setSelectedDate("")}
                >
                  <small>Tất cả</small>
                  <strong>Toàn bộ</strong>
                </button>
                {days.map((d, idx) => {
                  const iso = isoDate(d);
                  const active = selectedDate === iso;
                  return (
                    <button
                      key={iso}
                      type="button"
                      className={`quick-date-btn ${active ? "is-active" : ""}`}
                      onClick={() => setSelectedDate(iso)}
                    >
                      <small>{weekdayLabel(d, idx)}</small>
                      <strong>{String(d.getDate()).padStart(2, "0")}/{String(d.getMonth() + 1).padStart(2, "0")}</strong>
                    </button>
                  );
                })}
              </div>

              {/* Show date-specific showtimes if a date is chosen */}
              {selectedDate && (
                <div>
                  {loadingDateShows && (
                    <div className="grid" style={{ marginTop: 14 }}>
                      <Skeleton height={140} />
                      <Skeleton height={140} />
                    </div>
                  )}

                  {!loadingDateShows && dateFilteredMovies.length > 0 && (
                    <div className="quick-showtimes-grid">
                      {dateFilteredMovies.map((movie) => {
                        const slots = showtimesByMovie.get(movie.id) ?? [];
                        return (
                          <div className="quick-movie-card" key={movie.id}>
                            <Link to={`/movies/${movie.id}`} className="quick-movie-poster">
                              <Poster movie={movie} />
                            </Link>
                            <div className="quick-movie-info">
                              <div>
                                <span className="badge">{movie.rated}</span>
                                <span className="muted">{movie.genreName}</span>
                              </div>
                              <Link to={`/movies/${movie.id}`}>
                                <strong style={{ fontSize: 15, marginTop: 4, display: "block" }}>{movie.title}</strong>
                              </Link>
                              <span className="muted" style={{ fontSize: 12 }}>{movie.durationMinutes} phút</span>
                              <div className="quick-showtime-chips">
                                {slots.map((s) => (
                                  <Link
                                    key={s.id}
                                    to={`/book/${s.id}`}
                                    className="quick-time-chip"
                                    title={`Đặt vé suất ${s.startAt.slice(11, 16)} · ${s.roomName}`}
                                  >
                                    {s.startAt.slice(11, 16)}
                                  </Link>
                                ))}
                              </div>
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  )}

                  {!loadingDateShows && dateFilteredMovies.length === 0 && (
                    <div style={{ textAlign: "center", padding: "36px 12px" }}>
                      <p className="muted">Chưa có suất chiếu nào được mở trong ngày {formatDateDisplay(selectedDate)}.</p>
                      <button type="button" className="btn btn-outline" onClick={() => setSelectedDate("")}>
                        Xem danh sách phim đang chiếu
                      </button>
                    </div>
                  )}
                </div>
              )}
            </div>

            {/* Standard movie rails when not filtering by specific date */}
            {!selectedDate && (
              <>
                <div id="dang-chieu">
                  <Rail title="Đang chiếu" movies={showing} showing />
                </div>
                <Rail title="Sắp chiếu" movies={coming} />
              </>
            )}
          </>
        )}

        {searching && (
          <>
            <div className="row" style={{ marginBottom: 16, justifyContent: "space-between" }}>
              <h2 className="section-title" style={{ margin: 0 }}>
                {q ? `Kết quả cho “${q}”` : genre ? genre : tab === "COMING" ? "Sắp chiếu" : "Đang chiếu"}
              </h2>
              <select
                value={genre}
                onChange={(e) => setGenre(e.target.value)}
                aria-label="Lọc theo thể loại"
              >
                <option value="">Mọi thể loại</option>
                {genres.map((g) => (
                  <option key={g.id} value={g.name}>{g.name}</option>
                ))}
              </select>
            </div>
            <div className="grid">
              {filtered.map((m) => (
                <Link className="card" key={m.id} to={`/movies/${m.id}`}>
                  <Poster movie={m} />
                  <div className="card-body">
                    <div><span className="badge">{m.rated}</span><span className="muted">{m.genreName}</span></div>
                    <strong>{m.title}</strong>
                    <span className="muted">{m.durationMinutes} phút</span>
                  </div>
                </Link>
              ))}
              {filtered.length === 0 && (
                <EmptyState title="Không có phim phù hợp" hint="Thử từ khóa khác hoặc xem toàn bộ phim đang chiếu." action={<Link className="btn btn-yellow" to="/">Khám phá phim</Link>} />
              )}
            </div>
          </>
        )}
      </div>
      {trailerOpen && featured?.trailerUrl && (
        <TrailerModal url={featured.trailerUrl} title={featured.title} onClose={() => setTrailerOpen(false)} />
      )}
    </div>
  );
}

function favoriteCountOf(movie: Movie): number {
  if (movie.favoriteCount && movie.favoriteCount > 0) return movie.favoriteCount;
  return 80 + ((movie.id * 53) % 920);
}

function formatFavoriteCount(n: number): string {
  if (n >= 1000) {
    const k = n / 1000;
    return `${k >= 10 || n % 1000 === 0 ? k.toFixed(0) : k.toFixed(1)}K`;
  }
  return String(n);
}

function Rail({ title, movies, showing }: { title: string; movies: Movie[]; showing?: boolean }) {
  const scroller = useRef<HTMLDivElement>(null);
  if (movies.length === 0) return null;
  const slide = (dir: number) => scroller.current?.scrollBy({ left: dir * 320, behavior: "smooth" });
  return (
    <section className="rail">
      <div className="rail-head">
        <h2>{title}</h2>
        <Link to={showing ? "/?tab=showing" : "/?tab=coming"}>Xem toàn bộ ›</Link>
      </div>
      <div className="rail-track">
        <button type="button" className="rail-side prev" aria-label="Xem phim bên trái" onClick={() => slide(-1)}>‹</button>
        <button type="button" className="rail-side next" aria-label="Xem phim bên phải" onClick={() => slide(1)}>›</button>
        <div className="rail-scroll" ref={scroller}>
          {movies.map((m) => (
            <Link className="wide-card" key={m.id} to={`/movies/${m.id}`}>
              <div className="wide-card-art">
                <Poster movie={m} wide />
                <span className="ep-badge">{m.rated} · {m.durationMinutes}p</span>
                <span className="fav-badge" title="Lượt yêu thích">
                  <span className="fav-icon" aria-hidden>♥</span>
                  {formatFavoriteCount(favoriteCountOf(m))}
                </span>
              </div>
              <span className="wide-card-meta">
                <strong>{m.title}</strong>
                {m.genreName ? <span className="muted">{m.genreName}</span> : null}
              </span>
            </Link>
          ))}
        </div>
      </div>
    </section>
  );
}
