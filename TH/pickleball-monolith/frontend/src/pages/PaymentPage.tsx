import { useEffect, useState } from 'react';
import axios from 'axios';
import { Link, useParams } from 'react-router-dom';
import { getBooking, submitTransferNotice, type Booking } from '../api/pickleballApi';

const money = (value: number) => new Intl.NumberFormat('vi-VN').format(value) + ' đ';
const errorMessage = (error: unknown) => axios.isAxiosError(error) && error.response?.data?.message || 'Không thể kết nối đến hệ thống.';

export default function PaymentPage() {
  const { id } = useParams();
  const [booking, setBooking] = useState<Booking | null>(null);
  const [type, setType] = useState<'DEPOSIT' | 'FULL'>('DEPOSIT');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  useEffect(() => {
    let active = true;
    const load = () => getBooking(Number(id)).then(response => {
      if (!active) return;
      setBooking(response.data);
      if (response.data.paymentStatus === 'DEPOSIT_PAID') setType('FULL');
    }).catch(e => { if (active) setError(errorMessage(e)); });
    void load();
    const timer = window.setInterval(() => { void load(); }, 10000);
    return () => { active = false; window.clearInterval(timer); };
  }, [id]);

  const submit = async () => {
    if (!booking) return;
    setBusy(true); setError('');
    try { setBooking((await submitTransferNotice(booking.id, type)).data); }
    catch (e) { setError(errorMessage(e)); }
    finally { setBusy(false); }
  };

  const due = booking ? type === 'DEPOSIT' ? booking.depositAmount :
    booking.paymentStatus === 'DEPOSIT_PAID' ? booking.remainingAmount : booking.totalAmount : 0;
  const canTransfer = booking && !booking.transferSubmittedAt &&
    (booking.status === 'PENDING_PAYMENT' || booking.status === 'CONFIRMED') &&
    booking.paymentStatus !== 'FULLY_PAID' && booking.paymentStatus !== 'NOT_APPLICABLE';
  const content = booking ? `${type === 'DEPOSIT' ? 'COC' : 'TT'} ${booking.orderCode}` : '';

  return <main className="page narrow"><h1>Thông tin đặt cọc và thanh toán</h1>
    {error && <p className="error" role="alert">{error}</p>}
    {booking ? <div className="panel">
      <h2>{booking.courtName}</h2>
      <p>Mã đơn: <strong>{booking.orderCode}</strong></p>
      <p>Giờ bắt đầu: {new Date(booking.startAt).toLocaleString('vi-VN')}</p>
      <p>Giờ kết thúc: {new Date(booking.endAt).toLocaleString('vi-VN')}</p>
      <div className="price-summary">
        <p>Tổng tiền sân: <strong>{money(booking.totalAmount)}</strong></p>
        <p>Tiền cọc 10%: <strong>{money(booking.depositAmount)}</strong></p>
        <p>Còn lại sau khi cọc: <strong>{money(booking.remainingAmount)}</strong></p>
      </div>
      {booking.status === 'REJECTED' || booking.status === 'CANCELLED' ?
        <p className="decision-notice">{booking.status === 'REJECTED' ? 'Admin đã từ chối đơn.' : 'Đơn đã bị hủy.'}
          {booking.decisionReason && <> Lý do: <strong>{booking.decisionReason}</strong></>}</p> :
        booking.status === 'EXPIRED' ? <p className="decision-notice">Đã hết thời hạn báo chuyển khoản. Vui lòng đặt lại sân.</p> :
        booking.paymentStatus === 'FULLY_PAID' ? <p className="payment-success">Admin đã xác nhận thanh toán đủ.</p> : <>
          {booking.paymentStatus === 'DEPOSIT_PAID' && <p className="payment-success">Admin đã xác nhận tiền cọc. Còn lại {money(booking.remainingAmount)}.</p>}
          {booking.transferSubmittedAt ? <p className="transfer-pending">Đã báo chuyển khoản {booking.transferType === 'DEPOSIT' ? 'tiền cọc' : 'toàn bộ tiền sân'}. Đang chờ admin kiểm tra và xác nhận. Mã đối chiếu: <strong>{booking.orderCode}</strong>.
            {booking.transferReviewDeadline && <> Hạn kiểm tra: <strong>{new Date(booking.transferReviewDeadline).toLocaleString('vi-VN')}</strong>.</>}</p> : <>
            {booking.paymentStatus === 'PENDING' && <div className="payment-options">
              <label><input type="radio" name="payment-type" checked={type === 'DEPOSIT'} onChange={() => setType('DEPOSIT')} /> Cọc trước 10% ({money(booking.depositAmount)})</label>
              <label><input type="radio" name="payment-type" checked={type === 'FULL'} onChange={() => setType('FULL')} /> Thanh toán toàn bộ ({money(booking.totalAmount)})</label>
            </div>}
            <div className="transfer-info">
              <h3>Chuyển khoản ngân hàng</h3>
              <p>Ngân hàng: <strong>Techcombank</strong></p>
              <p>Chủ tài khoản: <strong>VUONG PHU HOANG</strong></p>
              <p>Số tài khoản: <strong>1907 3502 9220 15</strong></p>
              <p>Số tiền chuyển: <strong>{money(due)}</strong></p>
              <p>Nội dung chuyển khoản: <strong>{content}</strong></p>
            </div>
            {booking.status === 'PENDING_PAYMENT' && booking.paymentDeadline &&
              <p className="payment-note">Vui lòng báo chuyển khoản trước {new Date(booking.paymentDeadline).toLocaleString('vi-VN')} để giữ sân.</p>}
            {canTransfer && <div className="actions"><button onClick={submit} disabled={busy}>{busy ? 'Đang gửi...' : 'Tôi đã chuyển khoản'}</button></div>}
            <p className="payment-note">Admin kiểm tra giao dịch và cập nhật trạng thái thủ công.</p>
          </>}
        </>}
      {booking.transferReviewNote && <p className="decision-notice">{booking.transferReviewNote}</p>}
      {booking.refundStatus === 'REVIEW_REQUIRED' && <p className="transfer-pending">Admin đang đối chiếu khoản chuyển để xác định có cần hoàn tiền.</p>}
      {booking.refundStatus === 'PENDING' && <p className="transfer-pending">Cần hoàn tiền: <strong>{money(booking.refundAmount || 0)}</strong>. Đang chờ admin thực hiện.</p>}
      {booking.refundStatus === 'REFUNDED' && <p className="payment-success">Đã hoàn <strong>{money(booking.refundAmount || 0)}</strong>
        {booking.refundedAt && <> vào {new Date(booking.refundedAt).toLocaleString('vi-VN')}</>}
        {booking.refundBankReference && <>. Mã giao dịch: {booking.refundBankReference}</>}.</p>}
      <p className="payment-back"><Link to="/my-bookings">Xem các sân đã đặt</Link></p>
    </div> : !error && <p>Đang tải thông tin thanh toán...</p>}
  </main>;
}
