import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from "react";

export type Theme = "dark" | "light";
export const THEME_STORAGE_KEY = "vx_theme";
const BROADCAST_EVENT = "vx_theme_change";

type Ctx = {
  theme: Theme;
  setTheme: (t: Theme) => void;
  toggle: () => void;
};

const ThemeContext = createContext<Ctx | null>(null);

export function readTheme(): Theme {
  try {
    const saved = localStorage.getItem(THEME_STORAGE_KEY);
    if (saved === "light" || saved === "dark") {
      return saved;
    }
  } catch {
    // LocalStorage might be disabled or unavailable
  }
  return "dark";
}

export function applyTheme(theme: Theme) {
  try {
    const root = document.documentElement;
    root.setAttribute("data-theme", theme);
    root.classList.remove("light", "dark");
    root.classList.add(theme);
    root.style.colorScheme = theme;

    const themeColorMeta = document.querySelector('meta[name="theme-color"]');
    if (themeColorMeta) {
      themeColorMeta.setAttribute("content", theme === "light" ? "#f6f8fc" : "#07040a");
    }

    localStorage.setItem(THEME_STORAGE_KEY, theme);
  } catch {
    // Ignore storage errors in restricted contexts
  }
}

export function ThemeProvider({ children }: { children: ReactNode }) {
  const [theme, setThemeState] = useState<Theme>(readTheme);

  const setTheme = (next: Theme) => {
    setThemeState(next);
    applyTheme(next);
    try {
      window.dispatchEvent(new CustomEvent(BROADCAST_EVENT, { detail: next }));
    } catch {
      // Ignore
    }
  };

  const toggle = () => {
    setTheme(theme === "dark" ? "light" : "dark");
  };

  useEffect(() => {
    applyTheme(theme);
  }, [theme]);

  // Synchronize across multiple browser tabs / windows & decoupled components
  useEffect(() => {
    const handleStorage = (e: StorageEvent) => {
      if (e.key === THEME_STORAGE_KEY && (e.newValue === "light" || e.newValue === "dark")) {
        setThemeState(e.newValue);
        applyTheme(e.newValue);
      }
    };

    const handleCustom = (e: Event) => {
      const custom = e as CustomEvent<Theme>;
      if (custom.detail && (custom.detail === "light" || custom.detail === "dark")) {
        setThemeState(custom.detail);
      }
    };

    window.addEventListener("storage", handleStorage);
    window.addEventListener(BROADCAST_EVENT, handleCustom);

    return () => {
      window.removeEventListener("storage", handleStorage);
      window.removeEventListener(BROADCAST_EVENT, handleCustom);
    };
  }, []);

  const value = useMemo<Ctx>(
    () => ({
      theme,
      setTheme,
      toggle,
    }),
    [theme],
  );

  return <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>;
}

export function useTheme() {
  const ctx = useContext(ThemeContext);
  if (!ctx) throw new Error("ThemeProvider missing");
  return ctx;
}
