import { createContext, useContext, useEffect, useState, type ReactNode } from 'react';
import type { LoginResponse } from '../types/auth';

type AuthUser = { id: number; username: string; role: 'ADMIN' | 'CUSTOMER' };
type AuthContextValue = {
  user: AuthUser | null;
  isAuthenticated: boolean;
  login: (data: LoginResponse) => void;
  logout: () => void;
};

const TOKEN_KEY = 'pickleball_token';
const USER_KEY = 'pickleball_user';
const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const readStoredUser = (): AuthUser | null => {
    const token = localStorage.getItem(TOKEN_KEY);
    const savedUser = localStorage.getItem(USER_KEY);
    if (!token || !savedUser) return null;
    try {
      const parsed = JSON.parse(savedUser) as Partial<AuthUser>;
      if (!Number.isInteger(parsed.id) || (parsed.id as number) < 1 || typeof parsed.username !== 'string' || (parsed.role !== 'ADMIN' && parsed.role !== 'CUSTOMER')) throw new Error('Invalid saved user');
      return { id: parsed.id as number, username: parsed.username, role: parsed.role };
    } catch {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
      return null;
    }
  };

  const [user, setUser] = useState<AuthUser | null>(() => {
    return readStoredUser();
  });

  useEffect(() => {
    const onStorage = () => setUser(readStoredUser());
    window.addEventListener('storage', onStorage);
    window.addEventListener('pickleball-auth-expired', onStorage);
    return () => {
      window.removeEventListener('storage', onStorage);
      window.removeEventListener('pickleball-auth-expired', onStorage);
    };
  }, []);

  const login = (data: LoginResponse) => {
    const authUser: AuthUser = { id: data.userId, username: data.username, role: data.role };
    localStorage.setItem(TOKEN_KEY, data.token);
    localStorage.setItem(USER_KEY, JSON.stringify(authUser));
    setUser(authUser);
  };

  const logout = () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    setUser(null);
  };

  return <AuthContext.Provider value={{ user, isAuthenticated: !!user, login, logout }}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth phải được dùng bên trong AuthProvider');
  return context;
}
