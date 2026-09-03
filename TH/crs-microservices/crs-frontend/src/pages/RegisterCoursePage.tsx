import { useState } from 'react';
import axios from 'axios';
import { useCourses } from '../api/useCourses';
import { registerCourse } from '../api/registrationApi';
import SearchBox from '../components/SearchBox';
import CourseList from '../components/CourseList';
import Pagination from '../components/Pagination';
import Toast from '../components/Toast';
import { useToast } from '../hooks/useToast';
import type { Course } from '../types/course';
import type { ApiErrorResponse } from '../types/apiError';

const getErrorMessage = (error: unknown, fallback: string) =>
  axios.isAxiosError<ApiErrorResponse>(error) && error.response?.data?.message
    ? error.response.data.message : fallback;

export default function RegisterCoursePage() {
  const [keyword, setKeyword] = useState('');
  const [page, setPage] = useState(0);
  const [registeringId, setRegisteringId] = useState<number | null>(null);
  const { courses, totalPages, state, errorMessage, refetch } = useCourses(keyword, page);
  const { toast, showToast, clearToast } = useToast();

  const handleRegister = async (course: Course) => {
    if (registeringId !== null || course.soChoConLai === 0) return;
    setRegisteringId(course.id);
    try {
      await registerCourse({ courseId: course.id });
      showToast(`Dang ky thanh cong mon "${course.tenMonHoc}".`, 'success');
      refetch();
    } catch (error) {
      showToast(getErrorMessage(error, 'Dang ky khong thanh cong, vui long thu lai.'), 'error');
    } finally {
      setRegisteringId(null);
    }
  };

  return <main style={{ padding: 24, maxWidth: 800, margin: '0 auto' }}>
    <h1>Dang ky hoc phan</h1>
    <SearchBox onSearch={(next) => { setKeyword(next); setPage(0); }} />
    <div style={{ marginTop: 16 }}><CourseList courses={courses} state={state} errorMessage={errorMessage} onRetry={refetch} onRegister={handleRegister} registeringId={registeringId} /></div>
    <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
    {toast && <Toast message={toast.message} type={toast.type} onClose={clearToast} />}
  </main>;
}
