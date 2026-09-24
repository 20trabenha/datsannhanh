import { useEffect, useState } from 'react';
import axios from 'axios';
import { issueResetCode, listCustomerAccounts, type CustomerAccount, type ResetCode } from '../api/authApi';

export default function AdminCustomersPage() {
  const [customers, setCustomers] = useState<CustomerAccount[]>([]);
  const [keyword, setKeyword] = useState('');
  const [issued, setIssued] = useState<{ customer: CustomerAccount; code: ResetCode } | null>(null);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  useEffect(() => { listCustomerAccounts().then(r => setCustomers(r.data))
    .catch(e => setError(axios.isAxiosError(e) && e.response?.data?.message || 'Không tải được khách hàng.')); }, []);
  const create = async (customer: CustomerAccount) => {
    if (!window.confirm(`Bạn đã xác minh tài khoản ${customer.username} trước khi cấp mã đặt lại mật khẩu?`)) return;
    setBusy(true); setError('');
    try { setIssued({ customer, code: (await issueResetCode(customer.id)).data }); }
    catch (e) { setError(axios.isAxiosError(e) && e.response?.data?.message || 'Không cấp được mã.'); }
    finally { setBusy(false); }
  };
  const visible = customers.filter(c => `${c.username} ${c.phoneNumber || ''}`.toLowerCase().includes(keyword.trim().toLowerCase()));
  return <main className="page"><h1>Tài khoản khách hàng</h1><p>Chỉ cấp mã đặt lại mật khẩu sau khi đã xác minh danh tính khách hàng.</p>
    <div className="filters"><input aria-label="Tìm khách hàng" placeholder="Tìm tên đăng nhập hoặc số điện thoại" value={keyword} onChange={e => setKeyword(e.target.value)} /></div>
    {error && <p className="error" role="alert">{error}</p>}
    <div className="customer-account-list">{visible.map(customer => <article className="panel customer-account-card" key={customer.id}>
      <div><strong>{customer.username}</strong><p>{customer.phoneNumber || 'Chưa có số điện thoại'}</p></div>
      <button disabled={busy} onClick={() => create(customer)}>Cấp mã đặt lại</button>
    </article>)}</div>
    {issued && <div className="decision-overlay" role="presentation"><div className="panel decision-dialog" role="dialog" aria-modal="true" aria-label="Mã đặt lại mật khẩu">
      <h2>Mã cho {issued.customer.username}</h2><p>Chỉ hiển thị lần này. Gửi riêng cho khách sau khi xác minh danh tính.</p>
      <p className="reset-code">{issued.code.resetCode}</p><p>Hết hạn: {new Date(issued.code.expiresAt).toLocaleString('vi-VN')}</p>
      <button type="button" onClick={() => setIssued(null)}>Đã lưu mã</button>
    </div></div>}
  </main>;
}
