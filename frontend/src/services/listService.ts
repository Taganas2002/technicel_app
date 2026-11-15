import { api } from '../api/client';
import type { DailyProgress, DailyProgressSave, ListType, WorkList } from '../types';

export async function getActiveList() {
  const { data } = await api.get<WorkList>('/api/lists/active');
  return data;
}

export async function getListProgress(listId: number) {
  const { data } = await api.get<DailyProgress[]>(`/api/lists/${listId}/progress`);
  return data;
}

export async function saveDailyProgress(listId: number, completedDrops: number, workDate: string) {
  const { data } = await api.post<DailyProgressSave>(`/api/lists/${listId}/progress`, {
    workDate,
    completedDrops,
  });
  return data;
}

export async function finishWithProblems(listId: number, note: string) {
  const { data } = await api.post<WorkList>(`/api/lists/${listId}/finish-with-problems`, {
    note: note.trim() || null,
  });
  return data;
}

export async function createWorkList(listType: ListType, initialDropCount: number, referenceDate: string) {
  const { data } = await api.post<WorkList>('/api/lists', {
    listType,
    initialDropCount,
    referenceDate,
  });
  return data;
}

export async function getFinishedLists() {
  const { data } = await api.get<WorkList[]>('/api/lists/history');
  return data;
}

export async function deleteWorkList(listId: number) {
  await api.delete(`/api/admin/lists/${listId}`);
}

export async function updateInitialDrops(listId: number, initialDropCount: number) {
  const { data } = await api.patch<WorkList>(`/api/admin/lists/${listId}/initial-drops`, {
    initialDropCount,
  });
  return data;
}