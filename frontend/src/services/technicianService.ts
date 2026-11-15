import { api } from '../api/client';
import type { DailyProgress, PageResponse, TechnicianAccount, WorkList } from '../types';

export async function listTechnicians(page = 0) {
  const { data } = await api.get<PageResponse<TechnicianAccount>>('/api/admin/technicians', {
    params: { page, size: 50, sort: 'technicianCode' },
  });
  return data;
}

export async function createTechnician(payload: {
  firstName: string;
  lastName: string;
  nip: string;
  password: string;
}) {
  const { data } = await api.post<TechnicianAccount>('/api/admin/technicians', payload);
  return data;
}

export async function updateTechnicianPassword(technician: TechnicianAccount, password: string) {
  const { data } = await api.put<TechnicianAccount>(`/api/admin/technicians/${technician.id}`, {
    technicianCode: technician.technicianCode,
    firstName: technician.firstName,
    lastName: technician.lastName,
    nip: technician.nip,
    password,
    active: technician.active,
  });
  return data;
}

export async function updateTechnicianStatus(id: number, active: boolean) {
  const { data } = await api.patch<TechnicianAccount>(`/api/admin/technicians/${id}/status`, { active });
  return data;
}

export async function getTechnician(id: number) {
  const { data } = await api.get<TechnicianAccount>(`/api/admin/technicians/${id}`);
  return data;
}

export async function getTechnicianLists(technicianId: number) {
  const { data } = await api.get<WorkList[]>(`/api/admin/technicians/${technicianId}/lists`);
  return data;
}

export async function getListProgressAsAdmin(listId: number) {
  const { data } = await api.get<DailyProgress[]>(`/api/lists/${listId}/progress`);
  return data;
}