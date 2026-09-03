import { useState, type FormEvent } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { login as loginApi } from '../api/authApi';
import { useAuth } from '../context/AuthContext';
import type { ApiErrorResponse } from '../types/apiError';

export default function LoginPage() {
  const [username, setUsername] = useState(''); const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null); const [submitting, setSubmitting] = useState(false);
  const { login } = useAuth(); const navigate = useNavigate();
  const submit = async (event: FormEvent) => { event.preventDefault(); setError(null); if (!username.trim() || !password) { setError('Vui long nhap ten dang nhap va mat khau.'); return; } setSubmitting(true); try { const response = await loginApi({ username: username.trim(), password }); login(response.data); navigate('/courses'); } catch (err) { setError(axios.isAxiosError<ApiErrorResponse>(err) && err.response?.data?.message ? err.response.data.message : 'Dang nhap that bai, vui long thu lai.'); } finally { setSubmitting(false); } };
  return <main style={{ maxWidth: 360, margin: '80px auto', padding: 24, border: '1px solid #ddd', borderRadius: 8 }}><h1>Dang nhap he thong CRS</h1><form onSubmit={submit}>
    <label>Ten dang nhap<input value={username} onChange={(e) => setUsername(e.target.value)} style={{ display: 'block', width: '100%', boxSizing: 'border-box', margin: '4px 0 12px' }} /></label>
    <label>Mat khau<input type="password" value={password} onChange={(e) => setPassword(e.target.value)} style={{ display: 'block', width: '100%', boxSizing: 'border-box', marginTop: 4 }} /></label>
    {error && <p style={{ color: '#b91c1c' }}>{error}</p>}<button type="submit" disabled={submitting} style={{ width: '100%', marginTop: 12 }}>{submitting ? 'Dang xu ly...' : 'Dang nhap'}</button>
  </form></main>;
}
