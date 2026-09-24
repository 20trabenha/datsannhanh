import { useState, type FormEvent } from 'react';
import axios from 'axios';
import { Link, useNavigate } from 'react-router-dom';
import { resetPassword } from '../api/authApi';

export default function ResetPasswordPage() {
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [resetCode, setResetCode] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const submit = async (event: FormEvent) => {
    event.preventDefault(); setError('');
    if (newPassword.length < 8) { setError('Mật khẩu mới phải có ít nhất 8 ký tự.'); return; }
    if (newPassword !== confirmPassword) { setError('Mật khẩu nhập lại không khớp.'); return; }
    setBusy(true);
    try { await resetPassword(username.trim(), resetCode.trim(), newPassword); navigate('/login', { state: { passwordChanged: true } }); }
    catch (e) { setError(axios.isAxiosError(e) && e.response?.data?.message || 'Không đặt lại được mật khẩu.'); }
    finally { setBusy(false); }
  };
  return <main className="page narrow"><h1>Đặt lại mật khẩu</h1><div className="panel">
    <p>Liên hệ quản trị viên để xác minh tài khoản và nhận mã đặt lại. Mã có hiệu lực 15 phút và dùng một lần.</p>
    <form className="account-form" onSubmit={submit}>
      <label>Tên đăng nhập<input value={username} onChange={e => setUsername(e.target.value)} autoComplete="username" required /></label>
      <label>Mã đặt lại<input value={resetCode} onChange={e => setResetCode(e.target.value)} required /></label>
      <label>Mật khẩu mới<input type="password" value={newPassword} onChange={e => setNewPassword(e.target.value)} minLength={8} autoComplete="new-password" required /></label>
      <label>Nhập lại mật khẩu mới<input type="password" value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)} autoComplete="new-password" required /></label>
      {error && <p className="error" role="alert">{error}</p>}
      <div className="actions"><button disabled={busy}>{busy ? 'Đang lưu...' : 'Đặt lại mật khẩu'}</button><Link className="button secondary" to="/login">Quay lại đăng nhập</Link></div>
    </form></div></main>;
}
