import { useTheme } from "../context/ThemeContext";

function SunIcon({ className = "w-4 h-4" }: { className?: string }) {
  return (
    <svg
      className={className}
      width="16"
      height="16"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2.2"
      strokeLinecap="round"
      strokeLinejoin="round"
      style={{ display: "inline-block", verticalAlign: "middle" }}
    >
      <circle cx="12" cy="12" r="5" />
      <line x1="12" y1="1" x2="12" y2="3" />
      <line x1="12" y1="21" x2="12" y2="23" />
      <line x1="4.22" y1="4.22" x2="5.64" y2="5.64" />
      <line x1="18.36" y1="18.36" x2="19.78" y2="19.78" />
      <line x1="1" y1="12" x2="3" y2="12" />
      <line x1="21" y1="12" x2="23" y2="12" />
      <line x1="4.22" y1="19.78" x2="5.64" y2="18.36" />
      <line x1="18.36" y1="5.64" x2="19.78" y2="4.22" />
    </svg>
  );
}

function MoonIcon({ className = "w-4 h-4" }: { className?: string }) {
  return (
    <svg
      className={className}
      width="16"
      height="16"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2.2"
      strokeLinecap="round"
      strokeLinejoin="round"
      style={{ display: "inline-block", verticalAlign: "middle" }}
    >
      <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />
    </svg>
  );
}

export default function ThemeToggle({ compact = false }: { compact?: boolean }) {
  const { theme, setTheme, toggle } = useTheme();

  if (compact) {
    const isDark = theme === "dark";
    const nextLabel = isDark ? "Chuyển sang nền Sáng" : "Chuyển sang nền Tối";

    return (
      <button
        type="button"
        className="theme-toggle-btn"
        onClick={toggle}
        aria-label={nextLabel}
        title={nextLabel}
      >
        <span className="theme-toggle-icon" aria-hidden="true">
          {isDark ? <SunIcon /> : <MoonIcon />}
        </span>
        <span>{isDark ? "Sáng" : "Tối"}</span>
      </button>
    );
  }

  return (
    <div className="theme-toggle-group" role="radiogroup" aria-label="Lựa chọn giao diện">
      <button
        type="button"
        role="radio"
        aria-checked={theme === "dark"}
        className={`theme-segment-btn ${theme === "dark" ? "is-active" : ""}`}
        onClick={() => setTheme("dark")}
      >
        <MoonIcon />
        <span>Giao diện Tối</span>
      </button>
      <button
        type="button"
        role="radio"
        aria-checked={theme === "light"}
        className={`theme-segment-btn ${theme === "light" ? "is-active" : ""}`}
        onClick={() => setTheme("light")}
      >
        <SunIcon />
        <span>Giao diện Sáng</span>
      </button>
    </div>
  );
}
