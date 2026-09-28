import { useMemo, useState } from 'react';
import type { CartChoice, ChoiceGroup, ProductModalState, ProductSize } from '../types';
import { formatMoney } from '../utils';

interface OptionsModalProps {
  state: ProductModalState;
  onClose: () => void;
  onApply: (item: {
    choices: CartChoice[];
    sizeId: number | null;
    sizeName: string;
    finalPrice: number;
  }) => void;
}

export function OptionsModal({ state, onClose, onApply }: OptionsModalProps) {
  const [selectValues, setSelectValues] = useState<Record<number, string>>({});
  const [toppingQty, setToppingQty] = useState<Record<number, number>>({});
  const [sizeId, setSizeId] = useState('');

  const toppingGroups = useMemo(
    () =>
      state.choices.filter((group) => group.ten_nhom.toLowerCase().includes('topping')),
    [state.choices],
  );
  const selectGroups = useMemo(
    () =>
      state.choices.filter((group) => !group.ten_nhom.toLowerCase().includes('topping')),
    [state.choices],
  );

  function handleApply() {
    const selectedChoices: CartChoice[] = [];
    let extraPrice = 0;

    if (sizeId) {
      const size = state.sizes.find((item) => String(item.id) === sizeId);
      if (size) {
        extraPrice += Number(size.gia || 0);
      }
    }

    selectGroups.forEach((group) => {
      const value = selectValues[group.id];
      if (!value) {
        return;
      }
      const choice = group.lua_chon.find((item) => String(item.id) === value);
      if (!choice) {
        return;
      }
      const price = Number(choice.gia_tang_them || 0);
      extraPrice += price;
      selectedChoices.push({
        nhom_id: group.id,
        nhom_ten: group.ten_nhom,
        lua_chon_id: choice.id,
        lua_chon_ten: choice.ten_lua_chon,
        gia_tang_them: price,
      });
    });

    toppingGroups.forEach((group) => {
      group.lua_chon.forEach((choice) => {
        const qty = toppingQty[choice.id] || 0;
        if (qty <= 0) {
          return;
        }
        const price = Number(choice.gia_tang_them || 0);
        extraPrice += price * qty;
        for (let i = 0; i < qty; i += 1) {
          selectedChoices.push({
            nhom_id: group.id,
            nhom_ten: group.ten_nhom,
            lua_chon_id: choice.id,
            lua_chon_ten: choice.ten_lua_chon,
            gia_tang_them: price,
            so_luong: qty,
          });
        }
      });
    });

    const requiredGroups = selectGroups.filter((group) => group.bat_buoc);
    const selectedGroupIds = new Set(selectedChoices.map((choice) => choice.nhom_id));
    const missing = requiredGroups.filter((group) => !selectedGroupIds.has(group.id));
    if (missing.length > 0) {
      alert(`Vui lòng chọn: ${missing.map((group) => group.ten_nhom).join(', ')}`);
      return;
    }

    const selectedSize = state.sizes.find((item) => String(item.id) === sizeId);
    onApply({
      choices: selectedChoices,
      sizeId: selectedSize ? selectedSize.id : null,
      sizeName: selectedSize?.ten_size || '',
      finalPrice: state.productPrice + extraPrice,
    });
  }

  return (
    <div className="options-modal" style={{ display: 'flex' }}>
      <div className="options-card">
        <h3>Tùy chọn: {state.productName}</h3>
        <div id="choicesContainer">
          {state.choices.length === 0 && (
            <p style={{ color: '#999', padding: 20, textAlign: 'center' }}>
              Sản phẩm này không có tùy chọn
            </p>
          )}

          {selectGroups.map((group) => (
            <ChoiceSelectGroup
              key={group.id}
              group={group}
              value={selectValues[group.id] || ''}
              onChange={(value) =>
                setSelectValues((prev) => ({ ...prev, [group.id]: value }))
              }
            />
          ))}

          {toppingGroups.map((group) => (
            <ToppingGroup
              key={group.id}
              group={group}
              quantities={toppingQty}
              onChange={(choiceId, qty) =>
                setToppingQty((prev) => ({ ...prev, [choiceId]: qty }))
              }
            />
          ))}

          {state.sizes.length > 0 && (
            <SizeSelect sizes={state.sizes} value={sizeId} onChange={setSizeId} />
          )}
        </div>
        <div style={{ textAlign: 'right', marginTop: 15, flexShrink: 0 }}>
          <button type="button" className="btn btn-secondary" onClick={onClose}>
            Đóng
          </button>{' '}
          <button type="button" className="btn btn-primary" onClick={handleApply}>
            Áp dụng
          </button>
        </div>
      </div>
    </div>
  );
}

function ChoiceSelectGroup({
  group,
  value,
  onChange,
}: {
  group: ChoiceGroup;
  value: string;
  onChange: (value: string) => void;
}) {
  return (
    <div className="choice-group" style={{ marginBottom: 15 }}>
      <label style={{ display: 'block', marginBottom: 5, fontWeight: 600 }}>
        {group.ten_nhom}
        {group.bat_buoc ? ' *' : ''}
      </label>
      <select
        className="choice-select"
        value={value}
        onChange={(event) => onChange(event.target.value)}
        style={{ width: '100%', padding: 8, border: '1px solid #d2d8ea', borderRadius: 8 }}
      >
        <option value="" disabled={group.bat_buoc}>
          -- Chọn {group.ten_nhom} --
        </option>
        {group.lua_chon.map((choice) => (
          <option key={choice.id} value={choice.id}>
            {choice.ten_lua_chon}
            {choice.gia_tang_them > 0 ? ` (+${formatMoney(choice.gia_tang_them)})` : ''}
          </option>
        ))}
      </select>
    </div>
  );
}

function ToppingGroup({
  group,
  quantities,
  onChange,
}: {
  group: ChoiceGroup;
  quantities: Record<number, number>;
  onChange: (choiceId: number, qty: number) => void;
}) {
  return (
    <div className="choice-group" style={{ marginBottom: 15 }}>
      <label style={{ display: 'block', marginBottom: 5, fontWeight: 600 }}>
        {group.ten_nhom}
      </label>
      <div className="choice-toppings" style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
        {group.lua_chon.map((choice) => {
          const qty = quantities[choice.id] || 0;
          return (
            <div
              key={choice.id}
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                padding: 10,
                border: '1px solid #e5e7eb',
                borderRadius: 8,
                background: '#fff',
              }}
            >
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontWeight: 600, color: '#22334b', marginBottom: 2 }}>
                  {choice.ten_lua_chon}
                </div>
                <div style={{ fontSize: 12, color: '#64748b' }}>
                  {choice.gia_tang_them > 0
                    ? `+${formatMoney(choice.gia_tang_them)}/phần`
                    : 'Miễn phí'}
                </div>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                <button
                  type="button"
                  className="qty-btn qty-minus"
                  onClick={() => onChange(choice.id, Math.max(0, qty - 1))}
                >
                  −
                </button>
                <input
                  type="number"
                  className="topping-qty-input"
                  value={qty}
                  min={0}
                  max={99}
                  onChange={(event) => {
                    const next = Math.max(0, Math.min(99, parseInt(event.target.value, 10) || 0));
                    onChange(choice.id, next);
                  }}
                  style={{
                    width: 50,
                    height: 32,
                    textAlign: 'center',
                    border: '1px solid #d2d8ea',
                    borderRadius: 6,
                  }}
                />
                <button
                  type="button"
                  className="qty-btn qty-plus"
                  onClick={() => onChange(choice.id, Math.min(99, qty + 1))}
                >
                  +
                </button>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

function SizeSelect({
  sizes,
  value,
  onChange,
}: {
  sizes: ProductSize[];
  value: string;
  onChange: (value: string) => void;
}) {
  return (
    <div
      className="choice-group"
      style={{ marginBottom: 15, marginTop: 15, paddingTop: 15, borderTop: '1px solid #e0e0e0' }}
    >
      <label style={{ display: 'block', marginBottom: 5, fontWeight: 600 }}>
        Kích thước
      </label>
      <select
        id="productSizeSelect"
        className="product-size-select"
        value={value}
        onChange={(event) => onChange(event.target.value)}
        style={{ width: '100%', padding: 8, border: '1px solid #d2d8ea', borderRadius: 8 }}
      >
        <option value="">-- Chọn size --</option>
        {sizes.map((size) => (
          <option key={size.id} value={size.id}>
            {size.ten_size}
            {size.gia > 0 ? ` (+${formatMoney(size.gia)})` : ''}
          </option>
        ))}
      </select>
    </div>
  );
}
