import axiosClient from './axiosClient';

export type Category = { id: number; name: string };
export type Court = { id: number; name: string; pricePerHour: number; description: string | null; imageUrl: string | null; categoryId: number; categoryName: string;
  address: string | null; amenities: string | null; openingTime: string | null; closingTime: string | null };
export type CourtInput = { name: string; pricePerHour: number; description: string; categoryId: number;
  address: string; amenities: string; openingTime: string | null; closingTime: string | null };
export type AvailabilitySlot = { startAt: string; endAt: string; status: 'AVAILABLE' | 'BOOKED' | 'CLOSED' | 'PAST' };
export type CourtAvailability = { courtId: number; date: string; hoursConfigured: boolean;
  booked: { startAt: string; endAt: string }[]; closed: { startAt: string; endAt: string; reason: string }[]; slots: AvailabilitySlot[] };
export type CourtBlock = { id: number; courtId: number; courtName: string; startAt: string; endAt: string; reason: string };
export type Page<T> = { content: T[]; totalPages: number; totalElements: number; number: number };
export type Booking = { id: number; courtId: number; courtName: string; customerName: string; customerPhone: string | null; customerNote: string | null;
  startAt: string; endAt: string; bookedAt: string; status: 'PENDING_PAYMENT' | 'CONFIRMED' | 'CANCELLED' | 'EXPIRED' | 'REJECTED';
  paymentStatus: 'PENDING' | 'DEPOSIT_PAID' | 'FULLY_PAID' | 'NOT_APPLICABLE'; totalAmount: number; depositAmount: number;
  remainingAmount: number; paymentDeadline: string | null; depositPaidAt: string | null; fullPaidAt: string | null;
  transferSubmittedAt: string | null; transferType: 'DEPOSIT' | 'FULL' | null; orderCode: string;
  decisionReason: string | null; decisionAt: string | null;
  transferReviewDeadline: string | null; transferReviewNote: string | null;
  depositBankReference: string | null; fullBankReference: string | null;
  depositConfirmedBy: string | null; fullConfirmedBy: string | null; decisionBy: string | null;
  refundStatus: 'NONE' | 'REVIEW_REQUIRED' | 'PENDING' | 'REFUNDED'; refundAmount: number | null;
  refundedAt: string | null; refundBankReference: string | null; refundConfirmedBy: string | null;
};
export type Quote = { courtId: number; durationMinutes: number; totalAmount: number; depositAmount: number; remainingAmount: number };

export const listCategories = () => axiosClient.get<Category[]>('/api/categories');
export const addCategory = (name: string) => axiosClient.post<Category>('/api/categories', { name });
export const editCategory = (id: number, name: string) => axiosClient.put<Category>(`/api/categories/${id}`, { name });
export const removeCategory = (id: number) => axiosClient.delete(`/api/categories/${id}`);

export const listCourts = (params: { keyword?: string; categoryId?: number; page: number; size?: number; sort?: string }) =>
  axiosClient.get<Page<Court>>('/api/courts', { params });
export const getCourt = (id: number) => axiosClient.get<Court>(`/api/courts/${id}`);
export const getCourtAvailability = (id: number, date: string) =>
  axiosClient.get<CourtAvailability>(`/api/courts/${id}/availability`, { params: { date } });
export const listCourtBlocks = (courtId?: number) => axiosClient.get<CourtBlock[]>('/api/admin/court-blocks', { params: { courtId } });
export const addCourtBlock = (data: { courtId: number; startAt: string; endAt: string; reason: string }) =>
  axiosClient.post<CourtBlock>('/api/admin/court-blocks', data);
export const removeCourtBlock = (id: number) => axiosClient.delete(`/api/admin/court-blocks/${id}`);
export const addCourt = (data: CourtInput) => axiosClient.post<Court>('/api/courts', data);
export const editCourt = (id: number, data: CourtInput) => axiosClient.put<Court>(`/api/courts/${id}`, data);
export const removeCourt = (id: number) => axiosClient.delete(`/api/courts/${id}`);
export const uploadCourtImage = (id: number, file: File) => {
  const data = new FormData(); data.append('file', file);
  return axiosClient.post<Court>(`/api/courts/${id}/image`, data, { headers: { 'Content-Type': undefined } });
};

export const addBooking = (data: { courtId: number; startAt: string; endAt: string; customerName: string; customerPhone: string; customerNote: string }) => axiosClient.post<Booking>('/api/bookings', data);
export const getQuote = (data: { courtId: number; startAt: string; endAt: string }) => axiosClient.get<Quote>('/api/bookings/quote', { params: data });
export const getBooking = (id: number) => axiosClient.get<Booking>(`/api/bookings/${id}`);
export const submitTransferNotice = (id: number, type: 'DEPOSIT' | 'FULL') =>
  axiosClient.post<Booking>(`/api/bookings/${id}/transfer-notice`, { type });
export const listMyBookings = () => axiosClient.get<Booking[]>('/api/bookings/my');
export const cancelBooking = (id: number) => axiosClient.delete(`/api/bookings/${id}`);
export type AdminBookingFilters = { page: number; size?: number; keyword?: string; status?: string; courtId?: number; date?: string };
export const listAdminBookings = (params: AdminBookingFilters) => axiosClient.get<Page<Booking>>('/api/admin/bookings', { params });
export const countReviewBookings = () => axiosClient.get<number>('/api/admin/bookings/review-count');
export const confirmBookingPayment = (id: number, action: 'CONFIRM_DEPOSIT' | 'CONFIRM_FULL', transactionReference: string) =>
  axiosClient.post<Booking>(`/api/admin/bookings/${id}/payment`, { action, transactionReference });
export const decideBooking = (id: number, action: 'REJECT' | 'CANCEL', reason: string) =>
  axiosClient.post<Booking>(`/api/admin/bookings/${id}/decision`, { action, reason });
export const refundBooking = (id: number, action: 'MARK_REQUIRED' | 'NO_PAYMENT_FOUND' | 'CONFIRM_REFUND', amount?: number, transactionReference?: string) =>
  axiosClient.post<Booking>(`/api/admin/bookings/${id}/refund`, { action, amount, transactionReference });
