import { useEffect, useState, type FormEvent } from 'react';
import { emptyCourseForm, type Course, type CourseFormValues } from '../types/course';

interface Props {
  editingCourse: Course | null;
  onSubmit: (values: CourseFormValues) => Promise<void>;
  onCancel: () => void;
  submitting: boolean;
  serverError: string | null;
}

export default function CourseForm({ editingCourse, onSubmit, onCancel, submitting, serverError }: Props) {
  const [values, setValues] = useState<CourseFormValues>(emptyCourseForm);
  const [errors, setErrors] = useState<Partial<CourseFormValues>>({});

  useEffect(() => {
    setValues(editingCourse ? {
      tenMonHoc: editingCourse.tenMonHoc,
      soTinChi: String(editingCourse.soTinChi),
      soChoToiDa: String(editingCourse.soChoToiDa),
    } : emptyCourseForm);
    setErrors({});
  }, [editingCourse]);

  const validate = () => {
    const next: Partial<CourseFormValues> = {};
    if (!values.tenMonHoc.trim()) next.tenMonHoc = 'Ten mon hoc khong duoc de trong';
    const credits = Number(values.soTinChi);
    const capacity = Number(values.soChoToiDa);
    if (!Number.isInteger(credits) || credits < 1) next.soTinChi = 'So tin chi phai la so nguyen lon hon 0';
    if (!Number.isInteger(capacity) || capacity < 1) next.soChoToiDa = 'So cho toi da phai la so nguyen lon hon 0';
    setErrors(next);
    return Object.keys(next).length === 0;
  };

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    if (validate()) await onSubmit(values);
  };

  const field = (name: keyof CourseFormValues, label: string, type = 'text') => (
    <label style={{ display: 'block', marginBottom: 10 }}>
      {label}
      <input type={type} min={type === 'number' ? 1 : undefined} value={values[name]}
        onChange={(event) => setValues({ ...values, [name]: event.target.value })}
        style={{ display: 'block', width: '100%', boxSizing: 'border-box' }} />
      {errors[name] && <small style={{ color: '#b91c1c' }}>{errors[name]}</small>}
    </label>
  );

  return <form onSubmit={submit} style={{ border: '1px solid #ddd', padding: 16, borderRadius: 8, marginBottom: 16 }}>
    <h2>{editingCourse ? 'Sua mon hoc' : 'Them mon hoc moi'}</h2>
    {field('tenMonHoc', 'Ten mon hoc')}
    {field('soTinChi', 'So tin chi', 'number')}
    {field('soChoToiDa', 'So cho toi da', 'number')}
    {serverError && <p style={{ color: '#b91c1c' }}>{serverError}</p>}
    <button type="submit" disabled={submitting}>{submitting ? 'Dang luu...' : editingCourse ? 'Cap nhat' : 'Them moi'}</button>
    {editingCourse && <button type="button" onClick={onCancel} style={{ marginLeft: 8 }}>Huy</button>}
  </form>;
}
