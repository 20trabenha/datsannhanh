import { useState, type FormEvent } from 'react';
import axios from 'axios';
import { Link, useNavigate } from 'react-router-dom';
import { register } from '../api/authApi';
import { useAuth } from '../context/AuthContext';
import type { ApiErrorResponse } from '../types/apiError';

type Field = 'username' | 'password' | 'phoneNumber';
type Errors = Partial<Record<Field, string>>;

export default function CustomerRegisterPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [errors, setErrors] = useState<Errors>({});
  const [serverError, setServerError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setServerError(null);
    const next: Errors = {};
    if (!username.trim()) next.username = 'Tên đăng nhập không được để trống.';
    else if (!/^[A-Za-z0-9]+$/.test(username)) next.username = 'Tên đăng nhập không được chứa ký tự đặc biệt.';
    if (!password.trim()) next.password = 'Mật khẩu không được để trống.';
    if (!/^[0-9]{10}$/.test(phoneNumber)) next.phoneNumber = 'Số điện thoại phải gồm đúng 10 chữ số, vui lòng nhập lại.';
    setErrors(next);
    if (Object.keys(next).length > 0) return;

    setSubmitting(true);
    try {
      const response = await register({ username, password, phoneNumber });
      login(response.data);
      navigate('/courts');
    } catch (error) {
      setServerError(axios.isAxiosError<ApiErrorResponse>(error) && error.response?.data?.message
        ? error.response.data.message : 'Đăng ký không thành công, vui lòng thử lại.');
    } finally {
      setSubmitting(false);
    }
  };

  return <main className="auth-page"><div className="auth-shell">
    <section className="auth-promo" aria-label="Giới thiệu đặt sân pickleball"><span className="auth-promo-kicker">PICKLEBALL · ĐẶT SÂN NHANH</span><h2>Bắt đầu chơi<br />theo cách của bạn.</h2><p>Tạo tài khoản để tìm sân và sắp xếp trận đấu tiếp theo.</p><div className="auth-promo-court" aria-hidden="true"><span className="auth-court-net" /><span className="auth-court-ball" /></div></section>
    <section className="auth-card"><span className="auth-eyebrow">THAM GIA CÙNG CHÚNG TÔI</span><h1>Đăng ký</h1><p className="auth-intro">Tạo tài khoản khách hàng để đặt sân pickleball.</p>
      <form onSubmit={submit} noValidate className="auth-form">
        <label htmlFor="register-username">Tên đăng nhập</label><div className="auth-input"><span aria-hidden="true">◎</span><input id="register-username" value={username} onChange={(event) => setUsername(event.target.value)} autoComplete="username" aria-invalid={!!errors.username} placeholder="Chỉ gồm chữ và số" /></div>
        {errors.username && <small className="field-error">{errors.username}</small>}
        <label htmlFor="register-password">Mật khẩu</label><div className="auth-input"><span aria-hidden="true">◇</span><input id="register-password" type="password" value={password} onChange={(event) => setPassword(event.target.value)} autoComplete="new-password" aria-invalid={!!errors.password} placeholder="Nhập mật khẩu" /></div>
        {errors.password && <small className="field-error">{errors.password}</small>}
        <label htmlFor="register-phone">Số điện thoại</label><div className="auth-input"><span aria-hidden="true">✆</span><input id="register-phone" type="tel" inputMode="numeric" value={phoneNumber} onChange={(event) => setPhoneNumber(event.target.value)} autoComplete="tel" aria-invalid={!!errors.phoneNumber} placeholder="Nhập 10 chữ số" /></div>
        {errors.phoneNumber && <small className="field-error">{errors.phoneNumber}</small>}
        {serverError && <p role="alert" className="error auth-error">{serverError}</p>}
        <button className="auth-submit" type="submit" disabled={submitting}>{submitting ? 'Đang đăng ký...' : 'Tạo tài khoản →'}</button>
      </form><p className="auth-switch">Đã có tài khoản? <Link to="/login">Đăng nhập</Link></p>
    </section>
  </div></main>;
}
