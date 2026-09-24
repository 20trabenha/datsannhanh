import { useEffect, useState } from 'react';
import axios from 'axios';
import { getCourtAvailability, type CourtAvailability as Availability } from '../api/pickleballApi';

const localDate = () => {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
};
const time = (value: string) => value.slice(11, 16);

export default function CourtAvailability({ courtId, initialDate, onChoose }: {
  courtId: number; initialDate?: string; onChoose?: (startAt: string, endAt: string) => void;
}) {
  const [date, setDate] = useState(initialDate || localDate);
  const [data, setData] = useState<Availability | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [version, setVersion] = useState(0);

  useEffect(() => {
    let active = true;
    setLoading(true);
    getCourtAvailability(courtId, date)
      .then(response => { if (active) { setData(response.data); setError(''); } })
      .catch(err => { if (active) { setData(null); setError(axios.isAxiosError(err) && err.response?.data?.message || 'Không tải được lịch sân.'); } })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [courtId, date, version]);

  const booked = data?.booked || [];
  const closed = data?.closed || [];
  return <section className="availability-panel" aria-label="Lịch sân trống">
    <div className="availability-heading"><div><span className="section-kicker">LỊCH SÂN THEO NGÀY</span><h2>Khung giờ trống và đã đặt</h2></div>
      <div className="availability-controls"><label htmlFor={`schedule-date-${courtId}`}>Chọn ngày</label><input id={`schedule-date-${courtId}`} type="date" min={localDate()} value={date} onChange={event => setDate(event.target.value)} /><button type="button" onClick={() => setVersion(value => value + 1)}>Làm mới</button></div></div>
    {error && <p className="error" role="alert">{error}</p>}
    {loading ? <p>Đang tải lịch sân...</p> : data && <>
      {!data.hoursConfigured && <p className="schedule-hint">Giờ hoạt động chưa được cập nhật; lịch hiển thị cả ngày. Vui lòng xem thông tin sân trước khi đặt.</p>}
      <div className="schedule-legend"><span><i className="legend-available" /> Còn trống</span><span><i className="legend-booked" /> Đã đặt</span><span><i className="legend-closed" /> Tạm đóng</span><span><i className="legend-past" /> Đã qua</span></div>
      <div className="schedule-grid">{data.slots.map(slot => <div key={slot.startAt} className={`schedule-slot ${slot.status.toLowerCase()}`}>
        <strong>{time(slot.startAt)} – {time(slot.endAt)}</strong><span>{slot.status === 'BOOKED' ? 'Đã có người đặt' : slot.status === 'CLOSED' ? 'Sân tạm đóng' : slot.status === 'PAST' ? 'Đã qua' : 'Còn trống'}</span>
        {slot.status === 'AVAILABLE' && onChoose && <button type="button" onClick={() => onChoose(slot.startAt, slot.endAt)}>Chọn giờ</button>}
      </div>)}</div>
      <div className="booked-summary"><strong>Các lượt đã đặt trong ngày</strong>{booked.length === 0 ? <p>Chưa có lượt đặt sân trong ngày này.</p> :
        <div className="booked-list">{booked.map((item, index) => <span key={`${item.startAt}-${index}`}>{time(item.startAt)} – {time(item.endAt)}</span>)}</div>}</div>
      {closed.length > 0 && <div className="booked-summary"><strong>Khung giờ sân tạm đóng</strong><div className="closed-list">{closed.map((item, index) =>
        <span key={`${item.startAt}-${index}`}>{time(item.startAt)} – {time(item.endAt)}: {item.reason}</span>)}</div></div>}
    </>}
  </section>;
}
