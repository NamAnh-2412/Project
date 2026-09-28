import type { CartChoice, CartItem } from './types';

export function formatMoney(amount: number): string {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount);
}

export function formatNumber(value: string): string {
  const num = value.replace(/[^0-9]/g, '');
  if (!num) {
    return '';
  }
  return parseInt(num, 10).toLocaleString('vi-VN');
}

const RANK_LABELS: Record<string, string> = {
  thuong: 'Thường',
  bac: 'Bạc',
  vang: 'Vàng',
  vip: 'VIP',
};

export function getRankLabel(rank?: string): string {
  return RANK_LABELS[rank || 'thuong'] || 'Thường';
}

export function normalizeCartItems(items: CartItem[]): CartItem[] {
  return items
    .map((item) => ({
      id: parseInt(String(item.id), 10),
      ten: item.ten || '',
      gia: Number(item.gia || 0),
      sl: parseInt(String(item.sl), 10) || 1,
      size: item.size || '',
      size_id: item.size_id == null || item.size_id === ('' as unknown as number) ? null : parseInt(String(item.size_id), 10),
      choices: (item.choices || []).map((choice) => ({
        nhom_id: parseInt(String(choice.nhom_id), 10),
        nhom_ten: choice.nhom_ten || '',
        lua_chon_id: parseInt(String(choice.lua_chon_id), 10),
        lua_chon_ten: choice.lua_chon_ten || '',
        gia_tang_them: Number(choice.gia_tang_them || 0),
      })),
    }))
    .filter((item) => Number.isFinite(item.id) && item.id > 0);
}

export function cartItemKey(item: CartItem): string {
  const choicesKey = JSON.stringify(
    [...(item.choices || [])].sort((a, b) => a.nhom_id - b.nhom_id),
  );
  return `${item.id}|${item.size_id ?? ''}|${choicesKey}`;
}

export function summarizeChoices(choices: CartChoice[]): string[] {
  const grouped: Record<string, Record<string, number>> = {};
  choices.forEach((choice) => {
    if (!grouped[choice.nhom_ten]) {
      grouped[choice.nhom_ten] = {};
    }
    const key = choice.lua_chon_ten;
    grouped[choice.nhom_ten][key] = (grouped[choice.nhom_ten][key] || 0) + 1;
  });

  return Object.entries(grouped).map(([nhomTen, choiceMap]) => {
    const list = Object.entries(choiceMap).map(([name, count]) =>
      count > 1 ? `${name} (x${count})` : name,
    );
    return `${nhomTen}: ${list.join(', ')}`;
  });
}

export function calcCartTotal(cart: CartItem[]): number {
  return cart.reduce((sum, item) => sum + item.gia * item.sl, 0);
}
