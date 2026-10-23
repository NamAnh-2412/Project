import type { ReactNode } from "react";

export function Skeleton({ height = 180 }: { height?: number }) {
  return <div className="skeleton" style={{ height }} aria-hidden />;
}

export function EmptyState({ title, hint, action }: { title: string; hint?: string; action?: ReactNode }) {
  return (
    <div className="empty-state">
      <div style={{ fontSize: 36 }} aria-hidden>🎬</div>
      <h3 style={{ marginBottom: 6 }}>{title}</h3>
      {hint && <p className="muted">{hint}</p>}
      {action}
    </div>
  );
}
