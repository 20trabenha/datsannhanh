import { useState } from 'react';
import { useCourses } from '../api/useCourses';
import SearchBox from '../components/SearchBox';
import CourseList from '../components/CourseList';
import Pagination from '../components/Pagination';

export default function CoursesPage() {
  const [keyword, setKeyword] = useState('');
  const [page, setPage] = useState(0);
  const { courses, totalPages, state, errorMessage, refetch } = useCourses(keyword, page);
  return <main style={{ padding: 24, maxWidth: 800, margin: '0 auto' }}>
    <h1>Danh sach mon hoc</h1>
    <SearchBox onSearch={(next) => { setKeyword(next); setPage(0); }} />
    <div style={{ marginTop: 16 }}><CourseList courses={courses} state={state} errorMessage={errorMessage} onRetry={refetch} /></div>
    <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
  </main>;
}
