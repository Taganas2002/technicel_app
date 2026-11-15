import { api } from '../api/client';
import type { DayOffRequest, DayOffStatus, PageResponse } from '../types';

export async function createDayOffRequest(startDate: string, endDate: string, reason: string) {
  const { data } = await api.post<DayOffRequest>('/api/day-off-requests', {
    startDate,
    endDate,
    reason: reason.trim() || undefined,
  });
  return data;
}

export async function getMyDayOffRequests() {
  const { data } = await api.get<DayOffRequest[]>('/api/day-off-requests/my');
  return data;
}

export async function getAdminDayOffRequests(status?: DayOffStatus | '') {
  const { data } = await api.get<PageResponse<DayOffRequest>>('/api/admin/day-off-requests', {
    params: {
      status: status || undefined,
      size: 50,
    },
  });
  return data;
}

export async function decideDayOff(id: number, decision: 'approve' | 'reject', adminComment: string) {
  const { data } = await api.patch<DayOffRequest>(`/api/admin/day-off-requests/${id}/${decision}`, {
    adminComment: adminComment.trim() || undefined,
  });
  return data;
}