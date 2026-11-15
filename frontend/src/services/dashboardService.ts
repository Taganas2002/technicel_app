import { api } from '../api/client';
import type { AdminDashboard, DashboardFilters, DropRanking, RankingPeriod } from '../types';

export async function getAdminDashboard(filters: DashboardFilters, page = 0) {
  const { data } = await api.get<AdminDashboard>('/api/admin/dashboard', {
    params: {
      search: filters.search.trim() || undefined,
      listType: filters.listType || undefined,
      listStatus: filters.listStatus || undefined,
      page,
      size: 20,
    },
  });
  return data;
}

export async function getDropRankings(period: RankingPeriod, date: string) {
  const { data } = await api.get<DropRanking>('/api/admin/dashboard/rankings', {
    params: { period, date },
  });
  return data;
}
