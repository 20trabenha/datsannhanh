import { useCallback, useEffect, useState, type FormEvent } from 'react';
import axios from 'axios';
import { createApiKey, getApiKeys, revokeApiKey } from '../api/apiKeyApi';
import type { ApiKey } from '../types/apiKey';
import type { ApiErrorResponse } from '../types/apiError';
import Toast from '../components/Toast';
import { useToast } from '../hooks/useToast';

function messageFrom(error: unknown) {
  if (axios.isAxiosError<ApiErrorResponse>(error) && error.response?.data?.message) return error.response.data.message;
  return 'Không thể thực hiện thao tác API Key.';
}

export default function ApiKeysPage() {
  const [keys, setKeys] = useState<ApiKey[]>([]);
  const [loading, setLoading] = useState(true);
  const [ownerName, setOwnerName] = useState('');
  const [validDays, setValidDays] = useState('30');
  const [newKeyValue, setNewKeyValue] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const { toast, showToast, clearToast } = useToast();

  const loadKeys = useCallback(async () => {
    setLoading(true);
    try { setKeys((await getApiKeys()).data); }
    catch (error) { showToast(messageFrom(error), 'error'); }
    finally { setLoading(false); }
  }, [showToast]);

  useEffect(() => { void loadKeys(); }, [loadKeys]);

  const create = async (event: FormEvent) => {
    event.preventDefault();
    if (!ownerName.trim()) { showToast('Hãy nhập tên đối tác.', 'error'); return; }
    const days = validDays.trim() ? Number(validDays) : undefined;
    if (days !== undefined && (!Number.isInteger(days) || days < 1)) { showToast('Số ngày hiệu lực phải là số nguyên dương.', 'error'); return; }
    setSubmitting(true);
    setNewKeyValue(null);
    try {
      const response = await createApiKey({ ownerName: ownerName.trim(), validDays: days });
      setNewKeyValue(response.data.keyValue);
      setOwnerName('');
      showToast('Đã cấp API Key mới.', 'success');
      await loadKeys();
    } catch (error) { showToast(messageFrom(error), 'error'); }
    finally { setSubmitting(false); }
  };

  const revoke = async (key: ApiKey) => {
    if (!window.confirm(`Thu hồi API Key của "${key.ownerName}"?`)) return;
    try {
      await revokeApiKey(key.id);
      setKeys((current) => current.map((item) => item.id === key.id ? { ...item, status: 'REVOKED' } : item));
      showToast('Đã thu hồi API Key.', 'success');
    } catch (error) { showToast(messageFrom(error), 'error'); }
  };

  return <main style={{ padding: 24, maxWidth: 900, margin: '0 auto' }}>
    <h1>Quản lý API Key</h1>
    <p>Cấp key cho đối tác để gọi API danh sách sân pickleball. Key chỉ hiển thị một lần sau khi tạo.</p>
    <form onSubmit={create} style={{ display: 'grid', gap: 10, maxWidth: 480, border: '1px solid #ddd', borderRadius: 8, padding: 16 }}>
      <h2 style={{ margin: 0 }}>Cấp API Key mới</h2>
      <label>Tên đối tác<input value={ownerName} onChange={(event) => setOwnerName(event.target.value)} disabled={submitting} style={{ display: 'block', width: '100%', boxSizing: 'border-box', marginTop: 4 }} /></label>
      <label>Scope<input value="courts:read" readOnly style={{ display: 'block', width: '100%', boxSizing: 'border-box', marginTop: 4 }} /></label>
      <label>Số ngày hiệu lực (để trống = vĩnh viễn)<input type="number" min="1" value={validDays} onChange={(event) => setValidDays(event.target.value)} disabled={submitting} style={{ display: 'block', width: '100%', boxSizing: 'border-box', marginTop: 4 }} /></label>
      <button type="submit" disabled={submitting}>{submitting ? 'Đang cấp key...' : 'Cấp API Key'}</button>
    </form>
    {newKeyValue && <section style={{ marginTop: 16, padding: 16, border: '1px solid #15803d', borderRadius: 8, background: '#f0fdf4' }}>
      <strong>Sao chép API Key ngay bây giờ — key này sẽ không hiện lại:</strong>
      <code style={{ display: 'block', overflowWrap: 'anywhere', marginTop: 8 }}>{newKeyValue}</code>
    </section>}
    <h2 style={{ marginTop: 28 }}>Danh sách API Key</h2>
    {loading ? <p>Đang tải...</p> : keys.length === 0 ? <p>Chưa có API Key nào.</p> : <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
      <thead><tr><th>Đối tác</th><th>Scope</th><th>Trạng thái</th><th>Hết hạn</th><th>Thao tác</th></tr></thead>
      <tbody>{keys.map((key) => <tr key={key.id} style={{ borderTop: '1px solid #ddd' }}>
        <td>{key.ownerName}</td><td>{key.scopes}</td><td style={{ color: key.status === 'ACTIVE' ? '#15803d' : '#b91c1c' }}>{key.status}</td>
        <td>{key.expiresAt ? new Date(key.expiresAt).toLocaleDateString('vi-VN') : 'Vĩnh viễn'}</td>
        <td>{key.status === 'ACTIVE' && <button onClick={() => void revoke(key)}>Thu hồi</button>}</td>
      </tr>)}</tbody>
    </table>}
    {toast && <Toast message={toast.message} type={toast.type} onClose={clearToast} />}
  </main>;
}
