import { useState } from "react";

type Props = {
  value: string;
  onChange: (value: string) => void;
  autoComplete?: string;
};

function Eye({ off }: { off: boolean }) {
  return (
    <svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true">
      <path
        fill="none"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
        d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12z"
      />
      <circle cx="12" cy="12" r="3" fill="none" stroke="currentColor" strokeWidth="2" />
      {off && <path fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" d="M4 20L20 4" />}
    </svg>
  );
}

export default function PasswordField({ value, onChange, autoComplete }: Props) {
  const [visible, setVisible] = useState(false);
  return (
    <div className="password-field">
      <input
        type={visible ? "text" : "password"}
        value={value}
        autoComplete={autoComplete}
        onChange={(e) => onChange(e.target.value)}
      />
      <button
        type="button"
        className="password-toggle"
        aria-label={visible ? "Ẩn mật khẩu" : "Hiện mật khẩu"}
        aria-pressed={visible}
        onClick={() => setVisible((v) => !v)}
      >
        <Eye off={visible} />
      </button>
    </div>
  );
}
