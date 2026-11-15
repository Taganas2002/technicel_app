import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import { TOKEN_KEY, USER_KEY } from '../api/client';
import { login as loginRequest } from '../services/authService';
import type { AuthUser, Role } from '../types';

interface AuthContextValue {
  user: AuthUser | null;
  token: string | null;
  login: (nip: string, password: string) => Promise<AuthUser>;
  logout: () => void;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

function readUser(): AuthUser | null {
  const raw = sessionStorage.getItem(USER_KEY);
  if (!raw) {
    return null;
  }
  try {
    return JSON.parse(raw) as AuthUser;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => sessionStorage.getItem(TOKEN_KEY));
  const [user, setUser] = useState<AuthUser | null>(() => readUser());

  const login = useCallback(async (nip: string, password: string) => {
    const response = await loginRequest(nip, password);
    const nextUser: AuthUser = {
      userId: response.userId,
      technicianCode: response.technicianCode,
      firstName: response.firstName,
      lastName: response.lastName,
      role: response.role,
    };
    sessionStorage.setItem(TOKEN_KEY, response.accessToken);
    sessionStorage.setItem(USER_KEY, JSON.stringify(nextUser));
    setToken(response.accessToken);
    setUser(nextUser);
    return nextUser;
  }, []);

  const logout = useCallback(() => {
    sessionStorage.removeItem(TOKEN_KEY);
    sessionStorage.removeItem(USER_KEY);
    setToken(null);
    setUser(null);
  }, []);

  const value = useMemo(
    () => ({
      user,
      token,
      login,
      logout,
      isAuthenticated: Boolean(token && user),
    }),
    [user, token, login, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
}

export function homePathForRole(role: Role) {
  return role === 'ADMIN' ? '/admin/dashboard' : '/dashboard';
}
