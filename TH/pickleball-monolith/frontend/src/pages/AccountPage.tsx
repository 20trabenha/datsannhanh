import { useState, type FormEvent } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { changePassword } from '../api/authApi';
import { useAuth } from '../context/AuthContext';

export default function AccountPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const submit = async (event: FormEvent) => {
    event.preventDefault(); setError('');
    if (newPassword.length < 8) { setError('Mật khẩu mới phải có ít nhất 8 ký tự.'); return; }
    if (newPassword !== confirmPassword) { setError('Mật khẩu nhập lại không khớp.'); return; }
    setBusy(true);
    try { await changePassword(currentPassword, newPassword); logout(); navigate('/login', { state: { passwordChanged: true } }); }
    catch (e) { setError(axios.isAxiosError(e) && e.response?.data?.message || 'Không đổi được mật khẩu.'); }
    finally { setBusy(false); }
  };
  return <main className="page narrow"><h1>Tài khoản của tôi</h1><div className="panel"><p>Tên đăng nhập: <strong>{user?.username}</strong></p>
    <h2>Đổi mật khẩu</h2><form className="account-form" onSubmit={submit}>
      <label>Mật khẩu hiện tại<input type="password" autoComplete="current-password" value={currentPassword} onChange={e => setCurrentPassword(e.target.value)} required /></label>
      <label>Mật khẩu mới<input type="password" autoComplete="new-password" value={newPassword} onChange={e => setNewPassword(e.target.value)} minLength={8} required /></label>
      <label>Nhập lại mật khẩu mới<input type="password" autoComplete="new-password" value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)} required /></label>
      {error && <p className="error" role="alert">{error}</p>}
      <button disabled={busy}>{busy ? 'Đang lưu...' : 'Đổi mật khẩu'}</button>
    </form></div></main>;
}
