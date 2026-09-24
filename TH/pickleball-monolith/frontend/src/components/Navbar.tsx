import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  return <header className="site-header"><nav className="navbar" aria-label="Điều hướng chính">
    <Link className="brand" to="/courts" aria-label="Trang chủ Pickleball">
      <span className="brand-mark" aria-hidden="true">P</span><span className="brand-copy"><strong>PICKLEBALL</strong><small>ĐẶT SÂN NHANH</small></span>
    </Link>
    <div className="nav-links">
      <NavLink to="/courts" className={({ isActive }) => isActive ? 'active' : ''}>Khám phá sân</NavLink>
      {user?.role === 'ADMIN' && <><NavLink to="/admin/courts">Quản lý sân</NavLink><NavLink to="/admin/court-blocks">Lịch nghỉ sân</NavLink><NavLink to="/admin/categories">Danh mục</NavLink><NavLink to="/admin/bookings">Lượt đặt sân</NavLink><NavLink to="/admin/customers">Khách hàng</NavLink><NavLink to="/admin/api-keys">API Key</NavLink></>}
      {user?.role === 'CUSTOMER' && <NavLink to="/my-bookings">Sân đã đặt</NavLink>}
      {user && <NavLink to="/account">Tài khoản</NavLink>}
    </div>
    <div className="nav-account">
      {isAuthenticated ? <><span className="nav-greeting">Xin chào, <strong>{user?.username}</strong></span><button className="nav-logout" onClick={() => { logout(); navigate('/login'); }}>Đăng xuất</button></> : <><Link className="nav-login" to="/login">Đăng nhập</Link><Link className="nav-register" to="/register">Đăng ký</Link></>}
    </div>
  </nav></header>;
}
