import { FormEvent, useState } from "react";
import { NavLink, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useHoldSession } from "../context/HoldSessionContext";
import { useBrand } from "../context/BrandContext";
import { assetUrl } from "../lib/assetUrl";
import ThemeToggle from "./ThemeToggle";

export default function Navbar() {
  const { user, logout } = useAuth();
  const { abandon } = useHoldSession();
  const nav = useNavigate();
  const loc = useLocation();
  const { name, logoUrl } = useBrand();
  const [q, setQ] = useState("");
  const [menuOpen, setMenuOpen] = useState(false);
  const [searchOpen, setSearchOpen] = useState(false);
  const [accountOpen, setAccountOpen] = useState(false);
  const home = user?.role === "CASHIER" ? "/pos" : user?.role === "ADMIN" ? "/admin/movies" : "/";

  const search = (e: FormEvent) => {
    e.preventDefault();
    nav(q.trim() ? `/?q=${encodeURIComponent(q.trim())}` : "/");
  };

  return (
    <header className="nav">
      <NavLink to={home} className="logo">
        {logoUrl ? (
          <img className="logo-img" src={assetUrl(logoUrl)} alt="" />
        ) : (
          <span className="logo-mark" aria-hidden><i /></span>
        )}
        <span className="logo-text">
          <strong>{name}</strong>
          <span>Đặt vé xem phim</span>
        </span>
      </NavLink>

      {user?.role !== "CASHIER" && user?.role !== "ADMIN" && (
        <>
          <button type="button" className="nav-burger" aria-label="Mở tìm kiếm" onClick={() => setSearchOpen((v) => !v)}>⌕</button>
          <form className={`nav-search ${searchOpen ? "open" : ""}`} onSubmit={search}>
          <span aria-hidden>⌕</span>
          <input
            value={q}
            onChange={(e) => setQ(e.target.value)}
            placeholder="Tìm kiếm phim..."
          />
        </form>
        </>
      )}

      <button type="button" className="nav-burger" aria-label="Mở menu" onClick={() => setMenuOpen((v) => !v)}>☰</button>
      <nav className={`nav-links ${menuOpen ? "open" : ""}`} onClick={() => setMenuOpen(false)}>
        {user?.role !== "CASHIER" && user?.role !== "ADMIN" && (
          <>
            <NavLink to="/#chu-de" end>Chủ đề</NavLink>
            <NavLink to="/?tab=showing" className={() => (loc.search.includes("tab=showing") ? "active" : "")}>Đang chiếu</NavLink>
            <NavLink to="/?tab=coming" className={() => (loc.search.includes("tab=coming") ? "active" : "")}>Sắp chiếu<span className="nav-badge">NEW</span></NavLink>
            <NavLink to="/#dang-chieu" end>Lịch chiếu</NavLink>
            {user?.role === "CUSTOMER" && <NavLink to="/tickets">Vé của tôi</NavLink>}
          </>
        )}
        {user?.role === "CASHIER" && (
          <>
            <NavLink to="/pos" end>Bán vé</NavLink>
            <NavLink to="/pos/shift">Ca quầy</NavLink>
            <NavLink to="/pos/revenue">Doanh thu</NavLink>
            <NavLink to="/pos/tickets">Soát vé</NavLink>
            <NavLink to="/pos/notices">Thông báo</NavLink>
          </>
        )}
        {user?.role === "ADMIN" && (
          <>
            <NavLink to="/admin/genres">Thể loại</NavLink>
            <NavLink to="/admin/movies">Phim</NavLink>
            <NavLink to="/admin/showtimes">Suất chiếu</NavLink>
            <NavLink to="/admin/concessions">Đồ ăn vặt</NavLink>
            <NavLink to="/admin/reports">Báo cáo</NavLink>
            <NavLink to="/admin/notices">Thông báo</NavLink>
            <NavLink to="/admin/settings">Cài đặt</NavLink>
          </>
        )}
      </nav>

      <div className="nav-account">
        <ThemeToggle compact />
        {!user && (
          <>
            <NavLink to="/register" className="muted" style={{ fontSize: 13, fontWeight: 600 }}>Đăng ký</NavLink>
            <NavLink to="/login" className="btn-pill">Thành viên</NavLink>
          </>
        )}
        {user && (
          <div className="nav-menu">
            <button type="button" className="avatar-pill" aria-label="Tài khoản" onClick={() => setAccountOpen((v) => !v)}>
              {user.username.slice(0, 1).toUpperCase()}
            </button>
            {accountOpen && (
              <div className="nav-drop">
                <div className="muted" style={{ padding: "6px 10px" }}>{user.username}</div>
                <button
                  type="button"
                  onClick={async () => {
                    await abandon();
                    logout();
                    nav(user.role === "CASHIER" ? "/pos/login" : "/");
                  }}
                >
                  Thoát
                </button>
              </div>
            )}
          </div>
        )}
      </div>
    </header>
  );
}
