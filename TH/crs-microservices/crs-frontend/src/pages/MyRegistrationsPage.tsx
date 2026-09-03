import { useCallback, useEffect, useState } from 'react';
import axios from 'axios';
import { cancelRegistration, getMyRegistrations } from '../api/registrationApi';
import { getCourseById } from '../api/courseApi';
import Toast from '../components/Toast';
import { useToast } from '../hooks/useToast';
import type { ApiErrorResponse } from '../types/apiError';
import type { Registration } from '../types/registration';

type RegistrationRow = Registration & { courseName: string };
const getErrorMessage = (error: unknown, fallback: string) =>
  axios.isAxiosError<ApiErrorResponse>(error) && error.response?.data?.message
    ? error.response.data.message : fallback;

export default function MyRegistrationsPage() {
  const [rows, setRows] = useState<RegistrationRow[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [cancellingId, setCancellingId] = useState<number | null>(null);
  const { toast, showToast, clearToast } = useToast();

  const loadData = useCallback(async () => {
    setLoading(true); setLoadError(null);
    try {
      const registrations = (await getMyRegistrations()).data.filter((item) => item.trangThai === 'DA_DANG_KY');
      const enriched = await Promise.all(registrations.map(async (registration) => {
        try {
          const course = (await getCourseById(registration.courseId)).data;
          return { ...registration, courseName: course.tenMonHoc };
        } catch {
          return { ...registration, courseName: `Mon hoc #${registration.courseId} (khong tim thay thong tin)` };
        }
      }));
      setRows(enriched);
    } catch (error) {
      setLoadError(getErrorMessage(error, 'Khong tai duoc danh sach dang ky.'));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { loadData(); }, [loadData]);

  const handleCancel = async (row: RegistrationRow) => {
    if (!window.confirm(`Huy dang ky mon "${row.courseName}"?`)) return;
    setCancellingId(row.id);
    try {
      await cancelRegistration(row.id);
      setRows((current) => current.filter((item) => item.id !== row.id));
      showToast(`Da huy dang ky mon "${row.courseName}".`, 'success');
    } catch (error) {
      showToast(getErrorMessage(error, 'Huy dang ky khong thanh cong.'), 'error');
    } finally {
      setCancellingId(null);
    }
  };

  return <main style={{ padding: 24, maxWidth: 800, margin: '0 auto' }}>
    <h1>Mon hoc da dang ky</h1>
    {loading && <p>Dang tai...</p>}
    {!loading && loadError && <div style={{ color: '#b91c1c' }}><p>{loadError}</p><button onClick={loadData}>Thu lai</button></div>}
    {!loading && !loadError && rows.length === 0 && <p>Ban chua dang ky mon hoc nao.</p>}
    {!loading && !loadError && rows.length > 0 && <table style={{ width: '100%', borderCollapse: 'collapse' }}><thead><tr style={{ textAlign: 'left', borderBottom: '2px solid #333' }}><th>Ten mon hoc</th><th>Ngay dang ky</th><th>Thao tac</th></tr></thead><tbody>
      {rows.map((row) => <tr key={row.id} style={{ borderBottom: '1px solid #eee' }}><td>{row.courseName}</td><td>{new Date(row.ngayDangKy).toLocaleString('vi-VN')}</td><td><button onClick={() => handleCancel(row)} disabled={cancellingId === row.id}>{cancellingId === row.id ? 'Dang huy...' : 'Huy dang ky'}</button></td></tr>)}
    </tbody></table>}
    {toast && <Toast message={toast.message} type={toast.type} onClose={clearToast} />}
  </main>;
}
