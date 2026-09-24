import { useCallback, useEffect, useState, type FormEvent } from 'react';
import axios from 'axios';
import { Link, useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import CourtAvailability from '../components/CourtAvailability';
import * as api from '../api/pickleballApi';
import type { Booking, Category, Court, CourtInput, Quote } from '../api/pickleballApi';

const money = (value: number) => new Intl.NumberFormat('vi-VN').format(value) + ' đ';
const dateTime = (value: string) => new Date(value).toLocaleString('vi-VN');
function message(error: unknown) {
  if (axios.isAxiosError(error) && typeof error.response?.data?.message === 'string') return error.response.data.message;
  return 'Không thể kết nối đến hệ thống. Vui lòng thử lại.';
}

export function CourtsPage({ admin = false }: { admin?: boolean }) {
  const { user } = useAuth();
  const [categories, setCategories] = useState<Category[]>([]);
  const [courts, setCourts] = useState<Court[]>([]);
  const [keyword, setKeyword] = useState('');
  const [categoryId, setCategoryId] = useState('');
  const [sort, setSort] = useState('id,asc');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState<Court | null>(null);
  const [version, setVersion] = useState(0);
  const refresh = () => setVersion(value => value + 1);

  useEffect(() => { api.listCategories().then(r => setCategories(r.data)).catch(e => setError(message(e))); }, []);
  useEffect(() => {
    let live = true;
    setLoading(true);
    api.listCourts({ keyword, categoryId: categoryId ? Number(categoryId) : undefined, page, size: 6, sort })
      .then(r => { if (live) { setCourts(r.data.content); setTotalPages(r.data.totalPages); setError(''); } })
      .catch(e => { if (live) setError(message(e)); })
      .finally(() => { if (live) setLoading(false); });
    return () => { live = false; };
  }, [keyword, categoryId, page, sort, version]);

  const remove = async (court: Court) => {
    if (!window.confirm(`Xóa sân "${court.name}"?`)) return;
    try { await api.removeCourt(court.id); refresh(); }
    catch (e) { setError(message(e)); }
  };
  return <main className={admin ? 'page' : 'courts-page'}>
    {!admin && <section className="home-hero">
      <div className="hero-inner">
        <div className="hero-copy">
          <span className="hero-kicker"><span className="kicker-dot" /> SÂN CHƠI PICKLEBALL CỦA BẠN</span>
          <h1>Chọn sân đẹp.<br /><span>Vào trận thật nhanh.</span></h1>
          <p>Tìm sân pickleball phù hợp, chọn giờ chơi và đặt sân ngay trên một trang.</p>
          <div className="hero-search"><span aria-hidden="true">⌕</span><input aria-label="Tìm sân pickleball" placeholder="Bạn muốn chơi ở sân nào?" value={keyword} onChange={e => { setKeyword(e.target.value); setPage(0); }} /><button onClick={() => document.getElementById('court-results')?.scrollIntoView({ behavior: 'smooth' })}>Tìm sân <span aria-hidden="true">→</span></button></div>
          <div className="hero-steps"><span><b>01</b> Tìm sân</span><i /><span><b>02</b> Chọn giờ</span><i /><span><b>03</b> Đặt sân</span></div>
        </div>
        <div className="hero-visual" aria-hidden="true"><div className="hero-glow" /><div className="court-art"><span className="court-middle" /><span className="court-net" /><span className="court-left" /><span className="court-right" /></div><div className="hero-ball" /><div className="hero-visual-tag">PICKLEBALL<br /><strong>LET'S PLAY</strong></div></div>
      </div>
    </section>}
    <div className={admin ? '' : 'page home-results'} id="court-results">
    {admin ? <h1>Quản lý sân pickleball</h1> : <div className="section-heading"><div><span className="section-kicker">KHÁM PHÁ SÂN</span><h2>Sân pickleball dành cho bạn</h2><p>Chọn sân phù hợp để bắt đầu trận đấu tiếp theo.</p></div><span className="result-count">{loading ? 'Đang tìm sân...' : `${courts.length} sân trên trang này`}</span></div>}
    {admin && <CourtForm categories={categories} editing={editing} onSaved={() => { setEditing(null); refresh(); }} onCancel={() => setEditing(null)} />}
    <div className="filters court-filters">
      <input aria-label="Tìm tên sân" placeholder="Tìm theo tên sân" value={keyword} onChange={e => { setKeyword(e.target.value); setPage(0); }} />
      <select aria-label="Danh mục sân" value={categoryId} onChange={e => { setCategoryId(e.target.value); setPage(0); }}>
        <option value="">Tất cả danh mục</option>{categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
      </select>
      <select aria-label="Sắp xếp sân" value={sort} onChange={e => { setSort(e.target.value); setPage(0); }}>
        <option value="id,asc">Mới thêm sau</option><option value="name,asc">Tên A–Z</option>
        <option value="pricePerHour,asc">Giá thấp đến cao</option><option value="pricePerHour,desc">Giá cao đến thấp</option>
      </select>
    </div>
    {error && <p className="error" role="alert">{error}</p>}
    {loading ? <p>Đang tải danh sách sân...</p> : courts.length === 0 ? <p>Không tìm thấy sân phù hợp.</p> :
      <div className="court-grid">{courts.map(court => <article className="court-card" key={court.id}>
        <div className="court-image">{court.imageUrl ? <img src={court.imageUrl} alt={court.name} /> : <div className="court-image-placeholder" aria-hidden="true"><span /></div>}<span className="court-image-chip">{court.categoryName}</span></div>
        <div className="court-content"><p className="eyebrow">{court.categoryName}</p><h2>{court.name}</h2>
          <p className="court-description">{court.address ? `⌖ ${court.address}` : court.description || 'Địa chỉ sân chưa được cập nhật.'}</p>
          {court.openingTime && court.closingTime && <p className="court-hours">Mở cửa: {court.openingTime.slice(0, 5)} – {court.closingTime.slice(0, 5)}</p>}
          <div className="court-bottom"><div><small>GIÁ THUÊ MỖI GIỜ</small><strong>{money(court.pricePerHour)}</strong></div>
          {(admin || user?.role !== 'ADMIN') && <div className="actions">{admin ? <><Link className="court-detail-link" to={`/courts/${court.id}`}>Xem lịch</Link><button onClick={() => { setEditing(court); window.scrollTo({ top: 0, behavior: 'smooth' }); }}>Sửa</button><button onClick={() => remove(court)}>Xóa</button></> :
            <><Link className="court-detail-link" to={`/courts/${court.id}`}>Xem lịch</Link><Link className="button" to={user?.role === 'CUSTOMER' ? `/book/${court.id}` : '/login'}>Đặt sân →</Link></>}</div>}</div>
        </div></article>)}</div>}
    {totalPages > 1 && <div className="pagination"><button disabled={page === 0} onClick={() => setPage(page - 1)}>Trang trước</button><span>Trang {page + 1}/{totalPages}</span><button disabled={page + 1 >= totalPages} onClick={() => setPage(page + 1)}>Trang sau</button></div>}
    </div>
  </main>;
}

function CourtForm({ categories, editing, onSaved, onCancel }: { categories: Category[]; editing: Court | null; onSaved: () => void; onCancel: () => void }) {
  const [name, setName] = useState('');
  const [price, setPrice] = useState('');
  const [description, setDescription] = useState('');
  const [address, setAddress] = useState('');
  const [amenities, setAmenities] = useState('');
  const [openingTime, setOpeningTime] = useState('');
  const [closingTime, setClosingTime] = useState('');
  const [categoryId, setCategoryId] = useState('');
  const [image, setImage] = useState<File | null>(null);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  useEffect(() => {
    setName(editing?.name || ''); setPrice(editing ? String(editing.pricePerHour) : '');
    setDescription(editing?.description || ''); setCategoryId(editing ? String(editing.categoryId) : '');
    setAddress(editing?.address || ''); setAmenities(editing?.amenities || '');
    setOpeningTime(editing?.openingTime?.slice(0, 5) || ''); setClosingTime(editing?.closingTime?.slice(0, 5) || '');
    setImage(null); setError('');
  }, [editing]);
  const submit = async (event: FormEvent) => {
    event.preventDefault();
    if (!name.trim() || !Number.isInteger(Number(price)) || Number(price) < 1 || !categoryId) {
      setError('Vui lòng nhập tên sân, giá thuê hợp lệ và chọn danh mục.'); return;
    }
    if ((openingTime && !closingTime) || (!openingTime && closingTime) || (openingTime && closingTime && openingTime >= closingTime)) {
      setError('Vui lòng nhập giờ mở cửa và đóng cửa hợp lệ.'); return;
    }
    const data: CourtInput = { name: name.trim(), pricePerHour: Number(price), description: description.trim(), categoryId: Number(categoryId),
      address: address.trim(), amenities: amenities.trim(), openingTime: openingTime || null, closingTime: closingTime || null };
    setBusy(true); setError('');
    try {
      const court = editing ? (await api.editCourt(editing.id, data)).data : (await api.addCourt(data)).data;
      if (image) await api.uploadCourtImage(court.id, image);
      if (!editing) { setName(''); setPrice(''); setDescription(''); setCategoryId(''); setAddress(''); setAmenities(''); setOpeningTime(''); setClosingTime(''); setImage(null); }
      onSaved();
    } catch (e) { setError(message(e)); }
    finally { setBusy(false); }
  };
  return <form className="panel form-grid" onSubmit={submit}>
    <h2>{editing ? 'Sửa thông tin sân' : 'Thêm sân mới'}</h2>
    <label>Tên sân<input value={name} onChange={e => setName(e.target.value)} required /></label>
    <label>Giá thuê mỗi giờ (đồng)<input type="number" min="1" value={price} onChange={e => setPrice(e.target.value)} required /></label>
    <label>Danh mục<select value={categoryId} onChange={e => setCategoryId(e.target.value)} required><option value="">Chọn danh mục</option>{categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}</select></label>
    <label>Địa chỉ sân<input value={address} onChange={e => setAddress(e.target.value)} maxLength={200} placeholder="Nhập địa chỉ sân" /></label>
    <label>Giờ mở cửa<input type="time" value={openingTime} onChange={e => setOpeningTime(e.target.value)} /></label>
    <label>Giờ đóng cửa<input type="time" value={closingTime} onChange={e => setClosingTime(e.target.value)} /></label>
    <label>Ảnh sân<input type="file" accept="image/png,image/jpeg,image/webp" onChange={e => setImage(e.target.files?.[0] || null)} /></label>
    <label className="full">Mô tả<textarea value={description} onChange={e => setDescription(e.target.value)} maxLength={500} /></label>
    <label className="full">Tiện ích<textarea value={amenities} onChange={e => setAmenities(e.target.value)} maxLength={500} placeholder="Ví dụ: đèn chiếu sáng, chỗ để xe, nước uống" /></label>
    {error && <p className="error full" role="alert">{error}</p>}
    <div className="actions full"><button disabled={busy}>{busy ? 'Đang lưu...' : editing ? 'Cập nhật' : 'Thêm sân'}</button>{editing && <button type="button" onClick={onCancel}>Hủy sửa</button>}</div>
  </form>;
}

export function CategoriesPage() {
  const [rows, setRows] = useState<Category[]>([]);
  const [name, setName] = useState('');
  const [editingId, setEditingId] = useState<number | null>(null);
  const [error, setError] = useState('');
  const load = useCallback(() => api.listCategories().then(r => setRows(r.data)).catch(e => setError(message(e))), []);
  useEffect(() => { void load(); }, [load]);
  const save = async (event: FormEvent) => {
    event.preventDefault(); if (!name.trim()) { setError('Tên danh mục không được để trống.'); return; }
    try { if (editingId) await api.editCategory(editingId, name.trim()); else await api.addCategory(name.trim()); setName(''); setEditingId(null); setError(''); await load(); }
    catch (e) { setError(message(e)); }
  };
  const remove = async (row: Category) => {
    if (!window.confirm(`Xóa danh mục "${row.name}"?`)) return;
    try { await api.removeCategory(row.id); await load(); } catch (e) { setError(message(e)); }
  };
  return <main className="page"><h1>Quản lý danh mục sân</h1><form className="panel inline-form" onSubmit={save}>
    <label>Tên danh mục<input value={name} onChange={e => setName(e.target.value)} /></label>
    <button>{editingId ? 'Cập nhật' : 'Thêm danh mục'}</button>{editingId && <button type="button" onClick={() => { setEditingId(null); setName(''); }}>Hủy</button>}
  </form>{error && <p className="error" role="alert">{error}</p>}
    <div className="panel"><table><thead><tr><th>Danh mục</th><th>Thao tác</th></tr></thead><tbody>{rows.map(row => <tr key={row.id}><td>{row.name}</td><td><button onClick={() => { setEditingId(row.id); setName(row.name); }}>Sửa</button> <button onClick={() => remove(row)}>Xóa</button></td></tr>)}</tbody></table></div>
  </main>;
}

export function CourtDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [court, setCourt] = useState<Court | null>(null);
  const [error, setError] = useState('');
  useEffect(() => { api.getCourt(Number(id)).then(r => setCourt(r.data)).catch(e => setError(message(e))); }, [id]);
  const choose = (startAt: string, endAt: string) => {
    const query = new URLSearchParams({ start: startAt.slice(0, 16), end: endAt.slice(0, 16) });
    navigate(user?.role === 'CUSTOMER' ? `/book/${id}?${query}` : '/login');
  };
  return <main className="page court-detail-page">
    <p><Link to="/courts">← Danh sách sân</Link></p>
    {error && <p className="error" role="alert">{error}</p>}
    {court ? <>
      <section className="court-detail-head panel">
        {court.imageUrl && <img src={court.imageUrl} alt={court.name} />}
        <div><span className="section-kicker">{court.categoryName}</span><h1>{court.name}</h1>
          <p>{court.description || 'Chưa có mô tả sân.'}</p>
          <dl><div><dt>Địa chỉ</dt><dd>{court.address || 'Chưa cập nhật'}</dd></div>
            <div><dt>Giờ hoạt động</dt><dd>{court.openingTime && court.closingTime ? `${court.openingTime.slice(0, 5)} – ${court.closingTime.slice(0, 5)}` : 'Chưa cập nhật'}</dd></div>
            <div><dt>Tiện ích</dt><dd>{court.amenities || 'Chưa cập nhật'}</dd></div>
            <div><dt>Giá thuê</dt><dd><strong>{money(court.pricePerHour)} / giờ</strong></dd></div></dl>
          {user?.role !== 'ADMIN' && <Link className="button" to={user?.role === 'CUSTOMER' ? `/book/${court.id}` : '/login'}>Đặt sân</Link>}
        </div>
      </section>
      <CourtAvailability courtId={court.id} onChoose={user?.role === 'CUSTOMER' ? choose : undefined} />
    </> : !error && <p>Đang tải thông tin sân...</p>}
  </main>;
}

export function BookCourtPage() {
  const { id } = useParams(); const navigate = useNavigate(); const [searchParams] = useSearchParams();
  const [court, setCourt] = useState<Court | null>(null);
  const [startAt, setStartAt] = useState(searchParams.get('start') || ''); const [endAt, setEndAt] = useState(searchParams.get('end') || '');
  const [customerName, setCustomerName] = useState(''); const [customerPhone, setCustomerPhone] = useState('');
  const [customerNote, setCustomerNote] = useState('');
  const [quote, setQuote] = useState<Quote | null>(null);
  const [error, setError] = useState(''); const [busy, setBusy] = useState(false);
  useEffect(() => { api.getCourt(Number(id)).then(r => setCourt(r.data)).catch(e => setError(message(e))); }, [id]);
  useEffect(() => {
    if (!court || !startAt || !endAt || new Date(startAt) <= new Date() || new Date(endAt) <= new Date(startAt)) {
      setQuote(null); return;
    }
    let live = true;
    const timer = window.setTimeout(() => api.getQuote({ courtId: court.id, startAt, endAt })
      .then(r => { if (live) { setQuote(r.data); setError(''); } })
      .catch(e => { if (live) { setQuote(null); setError(message(e)); } }), 300);
    return () => { live = false; window.clearTimeout(timer); };
  }, [court, startAt, endAt]);
  const submit = async (event: FormEvent) => {
    event.preventDefault();
    if (!quote || !startAt || !endAt || new Date(startAt) <= new Date() || new Date(endAt) <= new Date(startAt)) {
      setError('Vui lòng chọn giờ bắt đầu trong tương lai và giờ kết thúc sau giờ bắt đầu.'); return;
    }
    if (!customerName.trim()) { setError('Vui lòng nhập họ và tên người đặt sân.'); return; }
    if (!/^\d{10}$/.test(customerPhone.trim())) { setError('Số điện thoại phải gồm đúng 10 chữ số.'); return; }
    setBusy(true); setError('');
    try { const booking = (await api.addBooking({ courtId: Number(id), startAt, endAt,
      customerName: customerName.trim(), customerPhone: customerPhone.trim(), customerNote: customerNote.trim() })).data; navigate(`/pay/${booking.id}`); }
    catch (e) { setError(message(e)); } finally { setBusy(false); }
  };
  return <main className="page narrow"><h1>Đặt sân pickleball</h1>{court ? <>
    <p><Link to={`/courts/${court.id}`}>← Thông tin và lịch sân</Link></p>
    <CourtAvailability courtId={court.id} initialDate={startAt.slice(0, 10) || undefined}
      onChoose={(start, end) => { setStartAt(start.slice(0, 16)); setEndAt(end.slice(0, 16)); setQuote(null); }} />
    <form className="panel form-grid" onSubmit={submit}>
    <h2 className="full">{court.name}</h2><p className="full">{court.categoryName} · {money(court.pricePerHour)} / giờ</p>
    {court.address && <p className="full">Địa chỉ: {court.address}</p>}
    {court.openingTime && court.closingTime && <p className="full">Giờ hoạt động: {court.openingTime.slice(0, 5)} – {court.closingTime.slice(0, 5)}</p>}
    <label>Giờ bắt đầu<input type="datetime-local" value={startAt} onChange={e => { setStartAt(e.target.value); setQuote(null); }} required /></label>
    <label>Giờ kết thúc<input type="datetime-local" value={endAt} onChange={e => { setEndAt(e.target.value); setQuote(null); }} required /></label>
    <h2 className="full booking-contact-title">Thông tin người đặt sân</h2>
    <label>Họ và tên<input type="text" value={customerName} onChange={e => setCustomerName(e.target.value)} maxLength={100} autoComplete="name" required /></label>
    <label>Số điện thoại<input type="tel" value={customerPhone} onChange={e => setCustomerPhone(e.target.value)} inputMode="numeric" pattern="[0-9]{10}" maxLength={10} autoComplete="tel" required /></label>
    <label className="full">Ghi chú khi đặt sân (không bắt buộc)<textarea value={customerNote} onChange={e => setCustomerNote(e.target.value)} maxLength={500} rows={3} placeholder="Thông tin cần lưu ý cho sân" /></label>
    {quote && <div className="full price-summary"><p>Thời lượng: {quote.durationMinutes} phút</p><p>Tổng tiền sân: <strong>{money(quote.totalAmount)}</strong></p><p>Cọc trước 10%: <strong>{money(quote.depositAmount)}</strong></p><p>Còn lại: {money(quote.remainingAmount)}</p></div>}
    {error && <p className="error full" role="alert">{error}</p>}<div className="actions full"><button disabled={busy || !quote}>{busy ? 'Đang xử lý...' : 'Tiếp tục thanh toán cọc'}</button><Link className="button secondary" to="/courts">Quay lại</Link></div>
  </form></> : <p>{error || 'Đang tải thông tin sân...'}</p>}</main>;
}

export function MyBookingsPage() {
  const [rows, setRows] = useState<Booking[]>([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [status, setStatus] = useState('');
  const [expandedId, setExpandedId] = useState<number | null>(null);
  const load = useCallback((silent = false) => {
    if (!silent) setLoading(true);
    api.listMyBookings().then(r => { setRows(r.data); setError(''); })
      .catch(e => setError(message(e))).finally(() => { if (!silent) setLoading(false); });
  }, []);
  useEffect(() => { load(); const timer = window.setInterval(() => load(true), 10000); return () => window.clearInterval(timer); }, [load]);
  const cancel = async (row: Booking) => {
    if (!window.confirm(`Hủy đơn ${row.orderCode} tại ${row.courtName}?`)) return;
    try { await api.cancelBooking(row.id); load(); } catch (e) { setError(message(e)); }
  };
  const visible = rows.filter(row => !status || row.status === status);
  const statusText = (row: Booking) => row.status === 'REJECTED' ? 'Đã từ chối' :
    row.status === 'CANCELLED' ? 'Đã hủy' : row.status === 'EXPIRED' ? 'Hết hạn' :
    row.paymentStatus === 'NOT_APPLICABLE' ? 'Đã xác nhận' :
    row.paymentStatus === 'FULLY_PAID' ? 'Đã thanh toán đủ' : row.transferSubmittedAt ? 'Chờ admin kiểm tra' :
    row.paymentStatus === 'DEPOSIT_PAID' ? 'Đã cọc' : 'Chờ cọc';
  return <main className="page my-bookings-page">
    <div className="my-bookings-heading"><div><h1>Lịch sử đơn đặt sân</h1><p>Theo dõi thông tin, thanh toán và kết quả xử lý từng đơn.</p></div>
      <span>{rows.length} đơn đặt sân</span></div>
    <div className="filters my-booking-filters"><select aria-label="Lọc trạng thái đơn" value={status} onChange={e => setStatus(e.target.value)}>
      <option value="">Tất cả đơn</option><option value="PENDING_PAYMENT">Chờ cọc</option><option value="CONFIRMED">Đã xác nhận</option>
      <option value="REJECTED">Đã từ chối</option><option value="CANCELLED">Đã hủy</option><option value="EXPIRED">Hết hạn</option>
    </select></div>
    {error && <p className="error" role="alert">{error}</p>}
    {loading ? <p>Đang tải...</p> : visible.length === 0 ? <div className="panel">{rows.length ? 'Không có đơn ở trạng thái này.' : 'Bạn chưa đặt sân nào.'}</div> :
      <div className="my-booking-list">{visible.map(row => <article className="my-booking-card" key={row.id}>
        <div className="my-booking-top"><div><span className="admin-booking-code">{row.orderCode}</span><h2>{row.courtName}</h2></div>
          <span className={`admin-booking-status status-${row.status.toLowerCase()}`}>{statusText(row)}</span></div>
        <div className="my-booking-summary">
          <div><small>Thời gian</small><strong>{dateTime(row.startAt)}</strong><span>Đến {dateTime(row.endAt)}</span></div>
          <div><small>Người đặt sân</small><strong>{row.customerName}</strong><span>{row.customerPhone || 'Chưa có số điện thoại'}</span></div>
          <div><small>Tổng tiền sân</small><strong>{row.paymentStatus === 'NOT_APPLICABLE' ? '—' : money(row.totalAmount)}</strong><span>Cọc 10%: {row.paymentStatus === 'NOT_APPLICABLE' ? '—' : money(row.depositAmount)}</span></div>
        </div>
        {row.decisionReason && <p className="my-booking-decision">Lý do {row.status === 'REJECTED' ? 'từ chối' : 'hủy đơn'}: <strong>{row.decisionReason}</strong></p>}
        {row.transferReviewNote && <p className="my-booking-decision">{row.transferReviewNote}</p>}
        {row.refundStatus === 'REVIEW_REQUIRED' && <p className="transfer-pending">Đang đối chiếu khoản chuyển để xác định tiền cần hoàn.</p>}
        {row.refundStatus === 'PENDING' && <p className="transfer-pending">Đang chờ hoàn {money(row.refundAmount || 0)}.</p>}
        {row.refundStatus === 'REFUNDED' && <p className="payment-success">Đã hoàn {money(row.refundAmount || 0)}{row.refundBankReference && ` · Mã giao dịch ${row.refundBankReference}`}</p>}
        <div className="my-booking-footer">
          <button type="button" className="admin-booking-toggle" aria-expanded={expandedId === row.id} onClick={() => setExpandedId(expandedId === row.id ? null : row.id)}>
            {expandedId === row.id ? 'Thu gọn' : 'Xem chi tiết'} <span aria-hidden="true">{expandedId === row.id ? '↑' : '↓'}</span></button>
          {(row.status === 'PENDING_PAYMENT' || row.status === 'CONFIRMED') && row.paymentStatus !== 'FULLY_PAID' && row.paymentStatus !== 'NOT_APPLICABLE' &&
            <Link className="button" to={`/pay/${row.id}`}>{row.transferSubmittedAt ? 'Xem chuyển khoản' : row.paymentStatus === 'PENDING' ? 'Thông tin đặt cọc' : 'Thanh toán còn lại'}</Link>}
        </div>
        {expandedId === row.id && <div className="my-booking-detail">
          <div className="admin-booking-detail-grid"><section><h3>Thông tin đặt sân</h3>
            <p>Người đặt: <strong>{row.customerName}</strong></p><p>Số điện thoại: <strong>{row.customerPhone || '—'}</strong></p>
            <p>Ghi chú: <strong>{row.customerNote || 'Không có'}</strong></p><p>Ngày tạo đơn: <strong>{dateTime(row.bookedAt)}</strong></p>
          </section><section><h3>Thanh toán</h3>
            <p>Tổng tiền: <strong>{row.paymentStatus === 'NOT_APPLICABLE' ? '—' : money(row.totalAmount)}</strong></p>
            <p>Tiền cọc: <strong>{row.paymentStatus === 'NOT_APPLICABLE' ? '—' : money(row.depositAmount)}</strong></p>
            <p>Còn lại: <strong>{row.paymentStatus === 'NOT_APPLICABLE' ? '—' : money(row.remainingAmount)}</strong></p>
            <p>Trạng thái: <strong>{row.paymentStatus === 'NOT_APPLICABLE' ? 'Chưa có dữ liệu' : row.paymentStatus === 'FULLY_PAID' ? 'Đã thanh toán đủ' : row.paymentStatus === 'DEPOSIT_PAID' ? 'Đã cọc, chưa thanh toán đủ' : row.transferSubmittedAt ? 'Đã báo chuyển khoản, chờ xác nhận' : 'Chưa xác nhận cọc'}</strong></p>
            {row.transferReviewDeadline && row.transferSubmittedAt && <p>Hạn kiểm tra: <strong>{dateTime(row.transferReviewDeadline)}</strong></p>}
            {row.depositBankReference && <p>Mã giao dịch cọc: <strong>{row.depositBankReference}</strong></p>}
            {row.fullBankReference && <p>Mã giao dịch thanh toán đủ: <strong>{row.fullBankReference}</strong></p>}
          </section></div>
          {(row.status === 'CONFIRMED' || row.status === 'PENDING_PAYMENT') && !row.transferSubmittedAt && !row.depositPaidAt &&
            <button type="button" className="my-booking-cancel" onClick={() => cancel(row)}>Hủy đặt sân</button>}
        </div>}
      </article>)}</div>}
  </main>;
}
