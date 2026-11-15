import { api } from '../api/client';
import type { AdminNotification, AdminNotificationList } from '../types';

export async function getAdminNotifications() {
  const { data } = await api.get<AdminNotificationList>('/api/admin/notifications');
  return data;
}

export async function markNotificationRead(id: number) {
  const { data } = await api.post<AdminNotification>(`/api/admin/notifications/${id}/read`);
  return data;
}

export async function markAllNotificationsRead() {
  const { data } = await api.post<AdminNotificationList>('/api/admin/notifications/read-all');
  return data;
}
