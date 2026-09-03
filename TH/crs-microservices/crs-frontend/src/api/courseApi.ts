// path: crs-frontend/src/api/courseApi.ts
import axiosClient from './axiosClient';
import type { Course, CourseFormValues, PagedResponse } from '../types/course';

export const getCourses = (keyword?: string, page?: number, size = 10) => {
  return axiosClient.get<PagedResponse<Course>>('/api/courses', {
    params: { keyword, page, size },
  });
};

const toPayload = (values: CourseFormValues) => ({
  tenMonHoc: values.tenMonHoc.trim(),
  soTinChi: Number(values.soTinChi),
  soChoToiDa: Number(values.soChoToiDa),
});

export const createCourse = (values: CourseFormValues) =>
  axiosClient.post<Course>('/api/courses', toPayload(values));

export const updateCourse = (id: number, values: CourseFormValues) =>
  axiosClient.put<Course>(`/api/courses/${id}`, toPayload(values));

export const deleteCourse = (id: number) => axiosClient.delete(`/api/courses/${id}`);
