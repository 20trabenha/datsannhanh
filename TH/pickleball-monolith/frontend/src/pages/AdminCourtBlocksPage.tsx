import { useEffect, useState, type FormEvent } from 'react';
import axios from 'axios';
import { addCourtBlock, listCourtBlocks, listCourts, removeCourtBlock, type Court, type CourtBlock } from '../api/pickleballApi';

const dateTime = (value: string) => new Date(value).toLocaleString('vi-VN');
const message = (error: unknown) => axios.isAxiosError(error) && error.response?.data?.message || 'Không thể xử lý lịch khóa sân.';

export default function AdminCourtBlocksPage() {
  const [courts, setCourts] = useState<Court[]>([]);
  const [blocks, setBlocks] = useState<CourtBlock[]>([]);
  const [courtId, setCourtId] = useState('');
  const [startAt, setStartAt] = useState('');
  const [endAt, setEndAt] = useState('');
  const [reason, setReason] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const load = () => listCourtBlocks().then(r => setBlocks(r.data)).catch(e => setError(message(e)));
  useEffect(() => {
    const fetchCourts = async () => {
      const first = (await listCourts({ page: 0, size: 100 })).data;
      const rest = await Promise.all(Array.from({ length: Math.max(0, first.totalPages - 1) }, (_, i) =>
        listCourts({ page: i + 1, size: 100 })));
      setCourts([...first.content, ...rest.flatMap(r => r.data.content)]);
    };
    void fetchCourts().catch(e => setError(message(e)));
    void load();
  }, []);
  const submit = async (event: FormEvent) => {
    event.preventDefault(); setError(''); setBusy(true);
    try {
      await addCourtBlock({ courtId: Number(courtId), startAt, endAt, reason: reason.trim() });
      setStartAt(''); setEndAt(''); setReason(''); await load();
    } catch (e) { setError(message(e)); } finally { setBusy(false); }
  };
  const remove = async (block: CourtBlock) => {
    if (!window.confirm(`Mở lại sân ${block.courtName} từ ${dateTime(block.startAt)}?`)) return;
    setError(''); setBusy(true);
    try { await removeCourtBlock(block.id); await load(); }
    catch (e) { setError(message(e)); } finally { setBusy(false); }
  };
  return <main className="page"><h1>Lịch nghỉ và bảo trì sân</h1>
    <p>Khóa khung giờ chưa có đơn đặt. Nếu đã có khách đặt, hãy xử lý đơn trước.</p>
    <form className="panel form-grid" onSubmit={submit}>
      <h2 className="full">Thêm lịch khóa sân</h2>
      <label className="full">Sân<select value={courtId} onChange={e => setCourtId(e.target.value)} required>
        <option value="">Chọn sân</option>{courts.map(court => <option key={court.id} value={court.id}>{court.name}</option>)}
      </select></label>
      <label>Bắt đầu<input type="datetime-local" value={startAt} onChange={e => setStartAt(e.target.value)} required /></label>
      <label>Kết thúc<input type="datetime-local" value={endAt} onChange={e => setEndAt(e.target.value)} required /></label>
      <label className="full">Lý do<textarea value={reason} onChange={e => setReason(e.target.value)} maxLength={300} rows={2} required /></label>
      {error && <p className="error full" role="alert">{error}</p>}
      <div className="actions full"><button disabled={busy}>{busy ? 'Đang lưu...' : 'Khóa khung giờ'}</button></div>
    </form>
    <section><h2>Khung giờ đang khóa</h2>{blocks.length === 0 ? <p>Chưa có lịch nghỉ hoặc bảo trì.</p> :
      <div className="block-list">{blocks.map(block => <article className="panel block-card" key={block.id}>
        <div><strong>{block.courtName}</strong><p>{dateTime(block.startAt)} – {dateTime(block.endAt)}</p><p>Lý do: {block.reason}</p></div>
        <button type="button" disabled={busy} onClick={() => remove(block)}>Mở lại sân</button>
      </article>)}</div>}
    </section>
  </main>;
}
