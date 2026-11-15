import { api } from '../api/client';
import type { LoginResponse } from '../types';

export async function login(nip: string, password: string): Promise<LoginResponse> {
  const { data } = await api.post<LoginResponse>('/api/auth/login', { nip, password });
  return data;
}
