import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { register } from "../api/authApi";
import { useAuth } from "../context/AuthContext";
import PasswordField from "../components/PasswordField";
import { notifyError, notifyOk } from "../lib/notify";

export default function RegisterPage() {
  const { login } = useAuth();
  const nav = useNavigate();
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [err, setErr] = useState("");

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErr("");
    try {
      await register(username, password, email);
      await login(username, password);
      notifyOk("Đã tạo tài khoản");
      nav("/");
    } catch (e) {
      const text = "Không đăng ký được (trùng tên, trùng email hoặc mật khẩu ngắn)";
      setErr(text);
      notifyError(e, text);
    }
  };

  return (
    <div className="page" style={{ maxWidth: 420 }}>
      <div className="panel">
        <h2>Đăng ký khách</h2>
        {err && <div className="toast err">{err}</div>}
        <form onSubmit={submit}>
          <div className="field">
            <label>Tên đăng nhập</label>
            <input value={username} onChange={(e) => setUsername(e.target.value)} />
          </div>
          <div className="field">
            <label>Email</label>
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
          </div>
          <div className="field">
            <label>Mật khẩu</label>
            <PasswordField value={password} onChange={setPassword} autoComplete="new-password" />
          </div>
          <button className="btn btn-yellow" type="submit">Tạo tài khoản</button>
        </form>
        <p className="muted" style={{ marginTop: 16 }}><Link to="/login">Đã có tài khoản</Link></p>
      </div>
    </div>
  );
}
