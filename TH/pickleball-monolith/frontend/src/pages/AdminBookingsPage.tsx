import { useCallback, useEffect, useState } from 'react';
import axios from 'axios';
import { confirmBookingPayment, countReviewBookings, decideBooking, listAdminBookings, listCourts,
  refundBooking, type Booking, type Court, type Page } from '../api/pickleballApi';

const money = (value: number) => new Intl.NumberFormat('vi-VN').format(value) + ' đ';
const dateTime = (value: string) => new Date(value).toLocaleString('vi-VN');
const errorMessage = (e: unknown) => axios.isAxiosError(e) && e.response?.data?.message || 'Không thể cập nhật đơn.';
type Decision = { id: number; action: 'REJECT' | 'CANCEL'; reason: string };
type Payment = { id: number; action: 'CONFIRM_DEPOSIT' | 'CONFIRM_FULL'; reference: string };
type Refund = { id: number; action: 'MARK_REQUIRED' | 'CONFIRM_REFUND'; amount: string; reference: string };

function statusLabel(row: Booking) {
  if (row.status === 'REJECTED') return 'Đã từ chối';
  if (row.status === 'CANCELLED') return 'Đã hủy';
  if (row.status === 'EXPIRED') return 'Hết hạn';
  if (row.paymentStatus === 'FULLY_PAID') return 'Đã thanh toán đủ';
  if (row.transferSubmittedAt) return 'Chờ kiểm tra chuyển khoản';
  if (row.paymentStatus === 'DEPOSIT_PAID') return 'Đã cọc';
  return row.paymentStatus === 'NOT_APPLICABLE' ? 'Đã xác nhận' : 'Chờ cọc';
}

export default function AdminBookingsPage() {
  const [result, setResult] = useState<Page<Booking> | null>(null);
  const [courts, setCourts] = useState<Court[]>([]);
  const [reviewCount, setReviewCount] = useState(0);
  const [keywordInput, setKeywordInput] = useState('');
  const [keyword, setKeyword] = useState('');
  const [status, setStatus] = useState('');
  const [courtId, setCourtId] = useState('');
  const [date, setDate] = useState('');
  const [page, setPage] = useState(0);
  const [expandedId, setExpandedId] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [decision, setDecision] = useState<Decision | null>(null);
  const [payment, setPayment] = useState<Payment | null>(null);
  const [refund, setRefund] = useState<Refund | null>(null);

  useEffect(() => { const timer = window.setTimeout(() => { setKeyword(keywordInput.trim()); setPage(0); }, 300);
    return () => window.clearTimeout(timer); }, [keywordInput]);
  useEffect(() => {
    const fetchCourts = async () => {
      const first = (await listCourts({ page: 0, size: 100 })).data;
      const rest = await Promise.all(Array.from({ length: Math.max(0, first.totalPages - 1) }, (_, i) =>
        listCourts({ page: i + 1, size: 100 })));
      setCourts([...first.content, ...rest.flatMap(r => r.data.content)]);
    };
    void fetchCourts().catch(e => setError(errorMessage(e)));
  }, []);
  const load = useCallback(async () => {
    try {
      const [bookings, count] = await Promise.all([
        listAdminBookings({ page, size: 10, keyword: keyword || undefined, status: status || undefined,
          courtId: courtId ? Number(courtId) : undefined, date: date || undefined }),
        countReviewBookings()]);
      setResult(bookings.data); setReviewCount(count.data); setError('');
    } catch (e) { setError(errorMessage(e)); }
    finally { setLoading(false); }
  }, [page, keyword, status, courtId, date]);
  useEffect(() => { void load(); const timer = window.setInterval(() => { void load(); }, 10000);
    return () => window.clearInterval(timer); }, [load]);
  const afterUpdate = async () => { await load(); };
  const submitPayment = async () => {
    if (!payment || payment.reference.trim().length < 4) { setError('Vui lòng nhập mã giao dịch ngân hàng ít nhất 4 ký tự.'); return; }
    setBusy(true); setError('');
    try { await confirmBookingPayment(payment.id, payment.action, payment.reference.trim()); setPayment(null); await afterUpdate(); }
    catch (e) { setError(errorMessage(e)); }
    finally { setBusy(false); }
  };
  const submitDecision = async () => {
    if (!decision?.reason.trim()) { setError('Vui lòng nhập lý do để khách hàng biết kết quả.'); return; }
    setBusy(true); setError('');
    try { await decideBooking(decision.id, decision.action, decision.reason.trim()); setDecision(null); await afterUpdate(); }
    catch (e) { setError(errorMessage(e)); }
    finally { setBusy(false); }
  };
  const submitRefund = async () => {
    if (!refund) return;
    if (refund.action === 'MARK_REQUIRED' && (!Number.isInteger(Number(refund.amount)) || Number(refund.amount) < 1)) {
      setError('Vui lòng nhập số tiền hoàn hợp lệ.'); return;
    }
    if (refund.action === 'CONFIRM_REFUND' && refund.reference.trim().length < 4) {
      setError('Vui lòng nhập mã giao dịch hoàn tiền ít nhất 4 ký tự.'); return;
    }
    setBusy(true); setError('');
    try { await refundBooking(refund.id, refund.action, refund.action === 'MARK_REQUIRED' ? Number(refund.amount) : undefined,
      refund.action === 'CONFIRM_REFUND' ? refund.reference.trim() : undefined); setRefund(null); await afterUpdate(); }
    catch (e) { setError(errorMessage(e)); }
    finally { setBusy(false); }
  };
  const noPaymentFound = async (row: Booking) => {
    if (!window.confirm(`Đã đối chiếu ngân hàng và không nhận tiền cho đơn ${row.orderCode}?`)) return;
    setBusy(true); setError('');
    try { await refundBooking(row.id, 'NO_PAYMENT_FOUND'); await afterUpdate(); }
    catch (e) { setError(errorMessage(e)); }
    finally { setBusy(false); }
  };

  return <main className="page admin-bookings-page"><div className="admin-bookings-heading"><div><h1>Khách hàng và sân đã đặt</h1><p>Theo dõi từng đơn và xử lý thanh toán tại phần chi tiết.</p></div>
    <div className="admin-booking-counts"><span><strong>{result?.totalElements ?? 0}</strong> Đơn phù hợp</span><span><strong>{reviewCount}</strong> Chờ kiểm tra chuyển khoản</span></div></div>
    <div className="filters admin-booking-filters">
      <input aria-label="Tìm lượt đặt" placeholder="Mã đơn, tên khách, số điện thoại hoặc sân" value={keywordInput} onChange={e => setKeywordInput(e.target.value)} />
      <select aria-label="Lọc sân" value={courtId} onChange={e => { setCourtId(e.target.value); setPage(0); }}><option value="">Tất cả sân</option>{courts.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}</select>
      <input aria-label="Lọc ngày đặt sân" type="date" value={date} onChange={e => { setDate(e.target.value); setPage(0); }} />
      <select aria-label="Trạng thái đặt sân" value={status} onChange={e => { setStatus(e.target.value); setPage(0); }}>
        <option value="">Tất cả trạng thái</option><option value="PENDING_PAYMENT">Chờ cọc</option><option value="CONFIRMED">Đã xác nhận</option>
        <option value="REJECTED">Đã từ chối</option><option value="CANCELLED">Đã hủy</option><option value="EXPIRED">Hết hạn</option>
      </select></div>
    {error && !decision && !payment && !refund && <p className="error" role="alert">{error}</p>}
    {loading ? <p>Đang tải...</p> : !result?.content.length ? <div className="panel">Chưa có lượt đặt sân phù hợp.</div> :
      <div className="admin-booking-list">{result.content.map(row => <article className="admin-booking-card" key={row.id}>
        <div className="admin-booking-top"><div><span className="admin-booking-code">{row.orderCode}</span><h2>{row.courtName}</h2></div>
          <span className={`admin-booking-status status-${row.status.toLowerCase()}`}>{statusLabel(row)}</span></div>
        <div className="admin-booking-summary">
          <div><small>Khách hàng</small><strong>{row.customerName}</strong><span>{row.customerPhone || 'Chưa có số điện thoại'}</span></div>
          <div><small>Thời gian đặt sân</small><strong>{dateTime(row.startAt)}</strong><span>Đến {dateTime(row.endAt)}</span></div>
          <div><small>Tiền sân</small><strong>{row.paymentStatus === 'NOT_APPLICABLE' ? '—' : money(row.totalAmount)}</strong><span>Cọc 10%: {row.paymentStatus === 'NOT_APPLICABLE' ? '—' : money(row.depositAmount)}</span></div>
        </div>
        {(row.refundStatus === 'PENDING' || row.refundStatus === 'REVIEW_REQUIRED') && <p className="transfer-pending">{row.refundStatus === 'PENDING' ? `Cần hoàn ${money(row.refundAmount || 0)}` : 'Cần đối chiếu khoản chuyển'}</p>}
        <button type="button" className="admin-booking-toggle" aria-expanded={expandedId === row.id} onClick={() => setExpandedId(expandedId === row.id ? null : row.id)}>
          {expandedId === row.id ? 'Thu gọn' : 'Xem chi tiết và xử lý'} <span aria-hidden="true">{expandedId === row.id ? '↑' : '↓'}</span></button>
        {expandedId === row.id && <div className="admin-booking-detail">
          <div className="admin-booking-detail-grid">
            <section><h3>Thông tin khách hàng</h3><p>Họ và tên: <strong>{row.customerName}</strong></p><p>Số điện thoại: <strong>{row.customerPhone || '—'}</strong></p><p>Ghi chú: <strong>{row.customerNote || 'Không có'}</strong></p></section>
            <section><h3>Thanh toán</h3><p>Tổng tiền: <strong>{row.paymentStatus === 'NOT_APPLICABLE' ? '—' : money(row.totalAmount)}</strong></p><p>Tiền cọc: <strong>{row.paymentStatus === 'NOT_APPLICABLE' ? '—' : money(row.depositAmount)}</strong></p><p>Còn lại: <strong>{row.paymentStatus === 'NOT_APPLICABLE' ? '—' : money(row.remainingAmount)}</strong></p>
              <p>Trạng thái: <strong>{row.paymentStatus === 'NOT_APPLICABLE' ? 'Chưa có dữ liệu' : row.paymentStatus === 'FULLY_PAID' ? 'Đã thanh toán đủ' : row.paymentStatus === 'DEPOSIT_PAID' ? 'Đã cọc, chưa thanh toán đủ' : 'Chưa xác nhận cọc'}</strong></p>
              <p>Khách báo chuyển khoản: <strong>{row.transferSubmittedAt ? `${row.transferType === 'DEPOSIT' ? 'Tiền cọc' : 'Thanh toán đủ'} · ${dateTime(row.transferSubmittedAt)}` : 'Chưa báo'}</strong></p>
              {row.transferReviewDeadline && row.transferSubmittedAt && <p>Hạn kiểm tra: <strong>{dateTime(row.transferReviewDeadline)}</strong></p>}
              {row.depositBankReference && <p>Đã xác nhận cọc: <strong>{row.depositBankReference} · {row.depositConfirmedBy || 'Admin'} · {row.depositPaidAt && dateTime(row.depositPaidAt)}</strong></p>}
              {row.fullBankReference && <p>Đã xác nhận đủ: <strong>{row.fullBankReference} · {row.fullConfirmedBy || 'Admin'} · {row.fullPaidAt && dateTime(row.fullPaidAt)}</strong></p>}
            </section>
          </div>
          {row.transferReviewNote && <p className="decision-notice">{row.transferReviewNote}</p>}
          {row.decisionReason && <p className="decision-notice">Lý do {row.status === 'REJECTED' ? 'từ chối' : 'hủy đơn'}: <strong>{row.decisionReason}</strong>{row.decisionBy && <> · Bởi {row.decisionBy}</>}</p>}
          {row.refundStatus === 'REVIEW_REQUIRED' && <div className="refund-panel"><h3>Đối chiếu tiền cần hoàn</h3><p>Kiểm tra ngân hàng trước khi chọn kết quả.</p><div className="actions">
            <button disabled={busy} onClick={() => { setError(''); setRefund({ id: row.id, action: 'MARK_REQUIRED', amount: '', reference: '' }); }}>Có tiền cần hoàn</button>
            <button type="button" disabled={busy} onClick={() => noPaymentFound(row)}>Không nhận được tiền</button></div></div>}
          {row.refundStatus === 'PENDING' && <div className="refund-panel"><h3>Chờ hoàn {money(row.refundAmount || 0)}</h3>
            <button disabled={busy} onClick={() => { setError(''); setRefund({ id: row.id, action: 'CONFIRM_REFUND', amount: '', reference: '' }); }}>Xác nhận đã hoàn</button></div>}
          {row.refundStatus === 'REFUNDED' && <p className="payment-success">Đã hoàn {money(row.refundAmount || 0)} · {row.refundBankReference} · {row.refundConfirmedBy} · {row.refundedAt && dateTime(row.refundedAt)}</p>}
          {(row.status === 'PENDING_PAYMENT' || row.status === 'CONFIRMED') && row.paymentStatus !== 'NOT_APPLICABLE' &&
            <div className="admin-booking-actions">
              {row.paymentStatus === 'PENDING' && <button disabled={busy} onClick={() => { setError(''); setPayment({ id: row.id, action: 'CONFIRM_DEPOSIT', reference: '' }); }}>Xác nhận đã cọc</button>}
              {row.paymentStatus !== 'FULLY_PAID' && <button disabled={busy} onClick={() => { setError(''); setPayment({ id: row.id, action: 'CONFIRM_FULL', reference: '' }); }}>Xác nhận đã thanh toán đủ</button>}
              {row.status === 'PENDING_PAYMENT' && <button type="button" disabled={busy} onClick={() => { setError(''); setDecision({ id: row.id, action: 'REJECT', reason: '' }); }}>Từ chối đơn</button>}
              <button type="button" disabled={busy} onClick={() => { setError(''); setDecision({ id: row.id, action: 'CANCEL', reason: '' }); }}>Hủy đơn</button>
            </div>}
        </div>}
      </article>)}</div>}
    {result && result.totalPages > 1 && <div className="pagination"><button type="button" disabled={page === 0} onClick={() => setPage(p => p - 1)}>Trang trước</button>
      <span>Trang {page + 1} / {result.totalPages}</span><button type="button" disabled={page + 1 >= result.totalPages} onClick={() => setPage(p => p + 1)}>Trang sau</button></div>}
    {payment && <div className="decision-overlay" role="presentation"><div className="panel decision-dialog" role="dialog" aria-modal="true" aria-label="Xác nhận thanh toán">
      <h2>{payment.action === 'CONFIRM_DEPOSIT' ? 'Xác nhận tiền cọc' : 'Xác nhận thanh toán đủ'}</h2><p>Chỉ xác nhận sau khi đã đối chiếu giao dịch ngân hàng.</p>
      {error && <p className="error" role="alert">{error}</p>}
      <label htmlFor="payment-reference">Mã giao dịch ngân hàng</label><input id="payment-reference" maxLength={100} value={payment.reference} onChange={e => setPayment({ ...payment, reference: e.target.value })} />
      <div className="actions"><button type="button" onClick={() => setPayment(null)} disabled={busy}>Đóng</button><button onClick={submitPayment} disabled={busy || payment.reference.trim().length < 4}>{busy ? 'Đang lưu...' : 'Xác nhận'}</button></div>
    </div></div>}
    {decision && <div className="decision-overlay" role="presentation"><div className="panel decision-dialog" role="dialog" aria-modal="true" aria-label={decision.action === 'REJECT' ? 'Từ chối đơn' : 'Hủy đơn'}>
      <h2>{decision.action === 'REJECT' ? 'Từ chối đơn' : 'Hủy đơn'}</h2><p>Khách hàng sẽ thấy lý do này trong đơn đặt sân.</p>
      {error && <p className="error" role="alert">{error}</p>}
      <label htmlFor="decision-reason">Lý do</label><textarea id="decision-reason" maxLength={500} rows={4} value={decision.reason} onChange={e => setDecision({ ...decision, reason: e.target.value })} />
      <div className="actions"><button type="button" onClick={() => setDecision(null)} disabled={busy}>Đóng</button><button onClick={submitDecision} disabled={busy || !decision.reason.trim()}>{busy ? 'Đang lưu...' : 'Xác nhận'}</button></div>
    </div></div>}
    {refund && <div className="decision-overlay" role="presentation"><div className="panel decision-dialog" role="dialog" aria-modal="true" aria-label="Xử lý hoàn tiền">
      <h2>{refund.action === 'MARK_REQUIRED' ? 'Ghi nhận tiền cần hoàn' : 'Xác nhận đã hoàn tiền'}</h2>
      {error && <p className="error" role="alert">{error}</p>}
      {refund.action === 'MARK_REQUIRED' ? <label>Số tiền đã nhận cần hoàn<input type="number" min="1" step="1" value={refund.amount} onChange={e => setRefund({ ...refund, amount: e.target.value })} /></label> :
        <label>Mã giao dịch hoàn tiền<input maxLength={100} value={refund.reference} onChange={e => setRefund({ ...refund, reference: e.target.value })} /></label>}
      <div className="actions"><button type="button" onClick={() => setRefund(null)} disabled={busy}>Đóng</button><button onClick={submitRefund} disabled={busy}>{busy ? 'Đang lưu...' : 'Xác nhận'}</button></div>
    </div></div>}
  </main>;
}
