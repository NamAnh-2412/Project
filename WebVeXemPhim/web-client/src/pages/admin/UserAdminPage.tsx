import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import api from "../../api/client";
import { errorText } from "../../lib/notify";

type AdminUser = {
  id: number;
  username: string;
  email?: string | null;
  role: string;
  enabled: boolean;
  createdAt?: string;
};

const ROLES = ["", "ADMIN", "CASHIER", "CUSTOMER"];

export default function UserAdminPage() {
  const qc = useQueryClient();
  const [q, setQ] = useState("");
  const [role, setRole] = useState("");
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [err, setErr] = useState("");

  const listQ = useQuery({
    queryKey: ["admin-users", q, role],
    queryFn: async () => {
      const { data } = await api.get<{ content: AdminUser[] }>("/api/users", {
        params: { q, role, size: 50, sort: "id,desc" },
      });
      return data.content;
    },
  });

  const createMut = useMutation({
    mutationFn: async () => {
      await api.post("/api/users/cashiers", { username: username.trim(), email: email.trim(), password });
    },
    onSuccess: () => {
      setUsername("");
      setEmail("");
      setPassword("");
      setErr("");
      qc.invalidateQueries({ queryKey: ["admin-users"] });
    },
    onError: (e: unknown) => {
      setErr(errorText(e, "Không tạo được thu ngân"));
    },
  });

  const statusMut = useMutation({
    mutationFn: (u: AdminUser) => api.patch(`/api/users/${u.id}/status`, { enabled: !u.enabled }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["admin-users"] }),
    onError: (e: unknown) => {
      setErr(errorText(e, "Không đổi trạng thái"));
    },
  });

  const reset = async (u: AdminUser) => {
    const next = window.prompt(`Mật khẩu mới cho ${u.username} (tối thiểu 6 ký tự)`);
    if (!next) return;
    try {
      await api.patch(`/api/users/${u.id}/password`, { password: next });
      setErr("");
    } catch (e: unknown) {
      setErr(errorText(e, "Không đổi mật khẩu"));
    }
  };

  return (
    <div>
      <h1 className="mb-1 text-2xl font-extrabold">Tài khoản</h1>
      <p className="mb-6 text-sm text-zinc-400">Tạo thu ngân, khóa tài khoản hoặc đặt lại mật khẩu. Khách tự đăng ký.</p>
      {err && <div className="mb-3 rounded-lg border border-red-500 bg-red-950 px-3 py-2 text-sm text-red-200">{err}</div>}
      <form
        className="mb-6 grid gap-3 rounded-xl border border-zinc-800 bg-zinc-900/70 p-4 sm:grid-cols-4"
        onSubmit={(e) => {
          e.preventDefault();
          createMut.mutate();
        }}
      >
        <label className="flex flex-col gap-1 text-xs font-extrabold uppercase tracking-wide text-zinc-400">
          Tên đăng nhập
          <input className="rounded-lg border border-zinc-600 bg-zinc-950 px-3 py-2 text-sm font-normal text-zinc-100" value={username} onChange={(e) => setUsername(e.target.value)} required minLength={3} />
        </label>
        <label className="flex flex-col gap-1 text-xs font-extrabold uppercase tracking-wide text-zinc-400">
          Email
          <input className="rounded-lg border border-zinc-600 bg-zinc-950 px-3 py-2 text-sm font-normal text-zinc-100" type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        </label>
        <label className="flex flex-col gap-1 text-xs font-extrabold uppercase tracking-wide text-zinc-400">
          Mật khẩu
          <input className="rounded-lg border border-zinc-600 bg-zinc-950 px-3 py-2 text-sm font-normal text-zinc-100" type="password" value={password} onChange={(e) => setPassword(e.target.value)} required minLength={6} />
        </label>
        <div className="flex items-end">
          <button type="submit" className="rounded-lg bg-yellow-400 px-4 py-2 font-bold text-zinc-900">Thêm thu ngân</button>
        </div>
      </form>
      <div className="mb-4 flex flex-wrap gap-2">
        <input className="rounded-lg border border-zinc-600 bg-zinc-950 px-3 py-2 text-sm" placeholder="Tìm tên hoặc email" value={q} onChange={(e) => setQ(e.target.value)} />
        <select className="rounded-lg border border-zinc-600 bg-zinc-950 px-3 py-2 text-sm" value={role} onChange={(e) => setRole(e.target.value)}>
          {ROLES.map((r) => <option key={r || "all"} value={r}>{r || "Mọi vai trò"}</option>)}
        </select>
      </div>
      <ul className="list-none space-y-2 p-0">
        {(listQ.data ?? []).map((u) => (
          <li key={u.id} className="flex flex-wrap items-center gap-3 rounded-xl border border-zinc-800 bg-zinc-900/70 px-4 py-3">
            <div className="min-w-0 flex-1">
              <div className="font-extrabold">{u.username}</div>
              <div className="text-xs text-zinc-500">{u.email || "Không email"} · {u.role} · {u.enabled ? "Đang mở" : "Đã khóa"}</div>
            </div>
            <button type="button" className="rounded-lg border border-zinc-600 px-3 py-1.5 text-xs font-extrabold" onClick={() => void reset(u)}>Đặt mật khẩu</button>
            <button type="button" className="rounded-lg border border-zinc-600 px-3 py-1.5 text-xs font-extrabold" onClick={() => statusMut.mutate(u)}>
              {u.enabled ? "Khóa" : "Mở"}
            </button>
          </li>
        ))}
      </ul>
    </div>
  );
}
