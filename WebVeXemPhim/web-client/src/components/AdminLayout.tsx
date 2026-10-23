import { Link, NavLink, Outlet, useNavigate } from "react-router-dom";
import { useState } from "react";
import { useAuth } from "../context/AuthContext";
import { useBrand } from "../context/BrandContext";
import { assetUrl } from "../lib/assetUrl";
import ThemeToggle from "./ThemeToggle";
import "../admin.css";

const links = [
  { to: "/", label: "Trang chủ" },
  { to: "/admin/movies", label: "Phim" },
  { to: "/admin/genres", label: "Thể loại" },
  { to: "/admin/showtimes", label: "Suất chiếu" },
  { to: "/admin/concessions", label: "Đồ ăn vặt" },
  { to: "/admin/reports", label: "Báo cáo" },
  { to: "/admin/vouchers", label: "Mã giảm giá" },
  { to: "/admin/users", label: "Tài khoản" },
  { to: "/admin/notices", label: "Thông báo" },
  { to: "/admin/settings", label: "Cài đặt" },
];

export default function AdminLayout() {
  const { user, logout } = useAuth();
  const { name, logoUrl } = useBrand();
  const nav = useNavigate();
  const [collapsed, setCollapsed] = useState(false);

  return (
    <div className="admin-shell">
      <aside className={`admin-aside ${collapsed ? "is-collapsed" : ""}`}>
        <div className="admin-brand">
          <button type="button" className="admin-collapse" onClick={() => setCollapsed((v) => !v)} aria-label="Thu gọn menu">
            {collapsed ? "»" : "«"}
          </button>
          <Link to="/" className="admin-brand-row">
            {logoUrl ? (
              <img className="admin-brand-logo" src={assetUrl(logoUrl)} alt="" />
            ) : null}
            <div>
              <div className="admin-brand-title">{name}</div>
              <div className="admin-brand-sub">Về trang chủ</div>
            </div>
          </Link>
        </div>
        <nav className="admin-nav">
          {links.map((l) => (
            <NavLink
              key={l.to}
              to={l.to}
              end={l.to === "/"}
              className={({ isActive }) => (isActive ? "admin-nav-on" : "admin-nav-item")}
            >
              <span>{l.label}</span>
            </NavLink>
          ))}
        </nav>
        <div className="admin-aside-foot">
          <div className="admin-user">{user?.username} · ADMIN</div>
          <ThemeToggle compact />
          <button
            className="admin-logout"
            type="button"
            onClick={() => {
              logout();
              nav("/login");
            }}
          >
            Đăng xuất
          </button>
        </div>
      </aside>
      <main className="admin-main">
        <Outlet />
      </main>
    </div>
  );
}
