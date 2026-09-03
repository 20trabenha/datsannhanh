import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  return <nav style={{ display: 'flex', gap: 16, padding: 12, borderBottom: '1px solid #ddd', alignItems: 'center' }}>
    <Link to="/courses">Danh sach mon hoc</Link>
    {user?.role === 'ADMIN' && <Link to="/admin/courses">Quan tri mon hoc</Link>}
    {user?.role === 'STUDENT' && <Link to="/register-course">Dang ky hoc phan</Link>}
    <div style={{ marginLeft: 'auto' }}>
      {isAuthenticated ? <><span style={{ marginRight: 12 }}>Xin chao, {user?.username} ({user?.role})</span><button onClick={() => { logout(); navigate('/login'); }}>Dang xuat</button></> : <Link to="/login">Dang nhap</Link>}
    </div>
  </nav>;
}
