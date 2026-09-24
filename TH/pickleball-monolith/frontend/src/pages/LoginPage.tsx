import { useState, type FormEvent } from 'react';
import axios from 'axios';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { login as loginApi } from '../api/authApi';
import { useAuth } from '../context/AuthContext';
import type { ApiErrorResponse } from '../types/apiError';

export default function LoginPage() {
  const [username, setUsername] = useState(''); const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState<string | null>(null); const [submitting, setSubmitting] = useState(false);
  const { login } = useAuth(); const navigate = useNavigate();
  const location = useLocation();
  const submit = async (event: FormEvent) => { event.preventDefault(); setError(null); if (!username.trim() || !password) { setError('Vui lòng nhập tên đăng nhập và mật khẩu.'); return; } setSubmitting(true); try { const response = await loginApi({ username: username.trim(), password }); login(response.data); navigate('/courts'); } catch (err) { setError(axios.isAxiosError<ApiErrorResponse>(err) && err.response?.data?.message ? err.response.data.message : 'Đăng nhập thất bại, vui lòng thử lại.'); } finally { setSubmitting(false); } };
  return <main className="auth-page"><div className="auth-shell">
    <section className="auth-promo" aria-label="Giới thiệu đặt sân pickleball"><span className="auth-promo-kicker">PICKLEBALL · ĐẶT SÂN NHANH</span><h2>Trận đấu tiếp theo<br />đang chờ bạn.</h2><p>Đăng nhập để chọn sân, đặt giờ và theo dõi các lượt đặt của mình.</p><div className="auth-promo-court" aria-hidden="true"><span className="auth-court-net" /><span className="auth-court-ball" /></div></section>
    <section className="auth-card"><span className="auth-eyebrow">CHÀO MỪNG TRỞ LẠI</span><h1>Đăng nhập</h1><p className="auth-intro">Tiếp tục hành trình pickleball của bạn.</p>
      {(location.state as { passwordChanged?: boolean } | null)?.passwordChanged && <p className="payment-success">Đã đổi mật khẩu. Vui lòng đăng nhập lại.</p>}
      <form onSubmit={submit} className="auth-form">
        <label htmlFor="login-username">Tên đăng nhập</label><div className="auth-input"><span aria-hidden="true">◎</span><input id="login-username" value={username} onChange={(e) => setUsername(e.target.value)} autoComplete="username" placeholder="Nhập tên đăng nhập" required /></div>
        <label htmlFor="login-password">Mật khẩu</label><div className="auth-input"><span aria-hidden="true">◇</span><input id="login-password" type={showPassword ? 'text' : 'password'} value={password} onChange={(e) => setPassword(e.target.value)} autoComplete="current-password" placeholder="Nhập mật khẩu" required /><button type="button" className="show-password" onClick={() => setShowPassword(value => !value)} aria-label={showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}>{showPassword ? 'Ẩn' : 'Hiện'}</button></div>
        {error && <p className="error auth-error" role="alert">{error}</p>}<button className="auth-submit" type="submit" disabled={submitting}>{submitting ? 'Đang xử lý...' : 'Đăng nhập →'}</button>
      </form><p className="auth-switch"><Link to="/reset-password">Quên mật khẩu?</Link></p><p className="auth-switch">Chưa có tài khoản? <Link to="/register">Đăng ký ngay</Link></p>
    </section>
  </div></main>;
}
