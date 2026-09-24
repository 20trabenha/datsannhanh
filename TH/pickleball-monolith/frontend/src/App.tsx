import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';
import { AuthProvider } from './context/AuthContext';
import LoginPage from './pages/LoginPage';
import CustomerRegisterPage from './pages/CustomerRegisterPage';
import ApiKeysPage from './pages/ApiKeysPage';
import AdminBookingsPage from './pages/AdminBookingsPage';
import PaymentPage from './pages/PaymentPage';
import AccountPage from './pages/AccountPage';
import ResetPasswordPage from './pages/ResetPasswordPage';
import AdminCustomersPage from './pages/AdminCustomersPage';
import AdminCourtBlocksPage from './pages/AdminCourtBlocksPage';
import { BookCourtPage, CategoriesPage, CourtDetailPage, CourtsPage, MyBookingsPage } from './pages/PickleballPages';

export default function App() {
  return <BrowserRouter><AuthProvider><Navbar /><Routes>
    <Route path="/" element={<Navigate to="/courts" replace />} />
    <Route path="/login" element={<LoginPage />} />
    <Route path="/register" element={<CustomerRegisterPage />} />
    <Route path="/reset-password" element={<ResetPasswordPage />} />
    <Route path="/account" element={<ProtectedRoute><AccountPage /></ProtectedRoute>} />
    <Route path="/courts" element={<CourtsPage />} />
    <Route path="/courts/:id" element={<CourtDetailPage />} />
    <Route path="/admin/courts" element={<ProtectedRoute requiredRole="ADMIN"><CourtsPage admin /></ProtectedRoute>} />
    <Route path="/admin/categories" element={<ProtectedRoute requiredRole="ADMIN"><CategoriesPage /></ProtectedRoute>} />
    <Route path="/admin/bookings" element={<ProtectedRoute requiredRole="ADMIN"><AdminBookingsPage /></ProtectedRoute>} />
    <Route path="/admin/customers" element={<ProtectedRoute requiredRole="ADMIN"><AdminCustomersPage /></ProtectedRoute>} />
    <Route path="/admin/court-blocks" element={<ProtectedRoute requiredRole="ADMIN"><AdminCourtBlocksPage /></ProtectedRoute>} />
    <Route path="/admin/api-keys" element={<ProtectedRoute requiredRole="ADMIN"><ApiKeysPage /></ProtectedRoute>} />
    <Route path="/book/:id" element={<ProtectedRoute requiredRole="CUSTOMER"><BookCourtPage /></ProtectedRoute>} />
    <Route path="/pay/:id" element={<ProtectedRoute requiredRole="CUSTOMER"><PaymentPage /></ProtectedRoute>} />
    <Route path="/my-bookings" element={<ProtectedRoute requiredRole="CUSTOMER"><MyBookingsPage /></ProtectedRoute>} />
    <Route path="*" element={<Navigate to="/courts" replace />} />
  </Routes></AuthProvider></BrowserRouter>;
}
