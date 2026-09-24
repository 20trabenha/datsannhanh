import axiosClient from './axiosClient';
import type { LoginRequest, LoginResponse, RegisterRequest } from '../types/auth';

export const login = (payload: LoginRequest) =>
  axiosClient.post<LoginResponse>('/api/auth/login', payload);

export const register = (payload: RegisterRequest) =>
  axiosClient.post<LoginResponse>('/api/auth/register', payload);
export const changePassword = (currentPassword: string, newPassword: string) =>
  axiosClient.post('/api/auth/change-password', { currentPassword, newPassword });
export const resetPassword = (username: string, resetCode: string, newPassword: string) =>
  axiosClient.post('/api/auth/reset-password', { username, resetCode, newPassword });
export type CustomerAccount = { id: number; username: string; phoneNumber: string | null };
export type ResetCode = { resetCode: string; expiresAt: string };
export const listCustomerAccounts = () => axiosClient.get<CustomerAccount[]>('/api/admin/customers');
export const issueResetCode = (id: number) => axiosClient.post<ResetCode>(`/api/admin/customers/${id}/reset-code`);
