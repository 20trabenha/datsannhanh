import { useState } from 'react';
import axios from 'axios';
import { createCourse, deleteCourse, updateCourse } from '../api/courseApi';
import { useCourses } from '../api/useCourses';
import CourseForm from '../components/CourseForm';
import CourseList from '../components/CourseList';
import Pagination from '../components/Pagination';
import SearchBox from '../components/SearchBox';
import type { ApiErrorResponse } from '../types/apiError';
import type { Course, CourseFormValues } from '../types/course';

function errorMessage(error: unknown) {
  if (axios.isAxiosError<ApiErrorResponse>(error)) {
    const data = error.response?.data;
    if (data?.message) return data.message;
    const fieldError = data && Object.values(data).find((value) => typeof value === 'string');
    if (fieldError) return fieldError;
  }
  return 'Da xay ra loi, vui long thu lai.';
}

export default function AdminCoursesPage() {
  const [keyword, setKeyword] = useState('');
  const [page, setPage] = useState(0);
  const [editingCourse, setEditingCourse] = useState<Course | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const { courses, totalPages, state, errorMessage: loadError, refetch } = useCourses(keyword, page);
  const save = async (values: CourseFormValues) => { setSubmitting(true); setFormError(null); try { if (editingCourse) await updateCourse(editingCourse.id, values); else await createCourse(values); setEditingCourse(null); refetch(); } catch (error) { setFormError(errorMessage(error)); } finally { setSubmitting(false); } };
  const remove = async (course: Course) => { if (!window.confirm(`Xoa mon hoc "${course.tenMonHoc}"?`)) return; try { await deleteCourse(course.id); refetch(); } catch (error) { window.alert(errorMessage(error)); } };
  return <main style={{ padding: 24, maxWidth: 800, margin: '0 auto' }}><h1>Quan ly mon hoc</h1>
    <CourseForm editingCourse={editingCourse} onSubmit={save} onCancel={() => setEditingCourse(null)} submitting={submitting} serverError={formError} />
    <SearchBox onSearch={(next) => { setKeyword(next); setPage(0); }} />
    <div style={{ marginTop: 16 }}><CourseList courses={courses} state={state} errorMessage={loadError} onRetry={refetch} onEdit={setEditingCourse} onDelete={remove} /></div>
    <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
  </main>;
}
