import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { createVoucher, fetchVouchers } from "../../api/bookingApi";
import { notifyError, notifyOk } from "../../lib/notify";

export default function VoucherAdminPage() {
  const qc = useQueryClient();
  const q = useQuery({ queryKey: ["admin-vouchers"], queryFn: fetchVouchers });
  const [code, setCode] = useState("");
  const [discountType, setDiscountType] = useState("AMOUNT");
  const [discountValue, setDiscountValue] = useState(20000);
  const [minOrderAmount, setMinOrderAmount] = useState(0);
  const [usageLimit, setUsageLimit] = useState(50);

  const save = useMutation({
    mutationFn: () => createVoucher({ code, discountType, discountValue, minOrderAmount, usageLimit }),
    onSuccess: () => {
      setCode("");
      notifyOk("Đã tạo mã giảm giá");
      void qc.invalidateQueries({ queryKey: ["admin-vouchers"] });
    },
    onError: (e) => notifyError(e, "Không tạo được mã"),
  });

  return (
    <div>
      <h1 className="mb-1 text-2xl font-extrabold">Mã giảm giá</h1>
      <p className="mb-6 text-sm text-zinc-400">Một mã cho một đơn online. Lượt chỉ trừ khi vé đã thanh toán.</p>
      <form
        className="mb-6 grid gap-3 rounded-xl border border-zinc-800 bg-zinc-900/70 p-4 md:grid-cols-6 md:items-end"
        onSubmit={(e) => {
          e.preventDefault();
          save.mutate();
        }}
      >
        <label className="grid gap-1 text-xs text-zinc-400">
          Mã khách nhập
          <input className="rounded-lg border border-zinc-600 bg-zinc-950 px-3 py-2 text-sm text-zinc-100" placeholder="CINE20" value={code} onChange={(e) => setCode(e.target.value)} />
        </label>
        <label className="grid gap-1 text-xs text-zinc-400">
          Cách giảm
          <select className="rounded-lg border border-zinc-600 bg-zinc-950 px-3 py-2 text-sm text-zinc-100" value={discountType} onChange={(e) => setDiscountType(e.target.value)}>
            <option value="AMOUNT">Số tiền (đ)</option>
            <option value="PERCENT">Phần trăm (%)</option>
          </select>
        </label>
        <label className="grid gap-1 text-xs text-zinc-400">
          {discountType === "PERCENT" ? "Mức giảm (%)" : "Mức giảm (đ)"}
          <input className="rounded-lg border border-zinc-600 bg-zinc-950 px-3 py-2 text-sm text-zinc-100" type="number" value={discountValue} onChange={(e) => setDiscountValue(Number(e.target.value))} />
        </label>
        <label className="grid gap-1 text-xs text-zinc-400">
          Đơn tối thiểu (đ)
          <input className="rounded-lg border border-zinc-600 bg-zinc-950 px-3 py-2 text-sm text-zinc-100" type="number" value={minOrderAmount} onChange={(e) => setMinOrderAmount(Number(e.target.value))} />
        </label>
        <label className="grid gap-1 text-xs text-zinc-400">
          Số lượt được dùng
          <input className="rounded-lg border border-zinc-600 bg-zinc-950 px-3 py-2 text-sm text-zinc-100" type="number" value={usageLimit} onChange={(e) => setUsageLimit(Number(e.target.value))} />
        </label>
        <button className="rounded-lg bg-yellow-400 px-3 py-2 font-extrabold text-zinc-950" type="submit" disabled={save.isPending || !code.trim()}>
          Tạo mã
        </button>
      </form>
      <ul className="space-y-2">
        {(q.data ?? []).map((v) => (
          <li key={v.id} className="rounded-lg border border-zinc-800 px-3 py-2 text-sm">
            <strong>{v.code}</strong>
            {" · "}Giảm {v.discountType === "PERCENT" ? `${v.discountValue}%` : `${Number(v.discountValue).toLocaleString("vi-VN")}đ`}
            {" · "}Đơn tối thiểu {Number(v.minOrderAmount ?? 0).toLocaleString("vi-VN")}đ
            {" · "}Đã dùng {v.usedCount}/{v.usageLimit} lượt
            {" · "}{v.status === "ACTIVE" ? "Đang mở" : v.status}
          </li>
        ))}
      </ul>
    </div>
  );
}
