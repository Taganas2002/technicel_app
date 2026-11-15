export type Role = 'ADMIN' | 'TECHNICIAN';
export type ListType = 'OBD' | 'DPO' | 'OBD_DPO';
export type WorkListStatus = 'IN_PROGRESS' | 'FINISHED';

export interface AuthUser {
  userId: number;
  technicianCode: string;
  firstName: string;
  lastName: string;
  role: Role;
}

export interface LoginResponse extends AuthUser {
  accessToken: string;
}

export interface WorkList {
  id: number;
  technicianId: number;
  listType: ListType;
  initialDropCount: number;
  totalCompleted: number;
  remaining: number;
  progressPercentage: number;
  referenceDate: string;
  status: WorkListStatus;
  completionDate: string | null;
  problemDropCount: number;
  problemNote: string | null;
}

export interface DailyProgress {
  id: number;
  workListId: number;
  workDate: string;
  completedDrops: number;
  createdAt: string;
  updatedAt: string;
}

export interface DailyProgressSave {
  progress: DailyProgress;
  workList: WorkList;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface DashboardSummary {
  totalTechnicians: number;
  activeTechnicians: number;
  techniciansWorkingToday: number;
  activeLists: number;
  finishedLists: number;
  dropsCompletedToday: number;
  pendingDayOffRequests: number;
}

export interface TechnicianProgressRow {
  technicianId: number;
  technicianCode: string;
  fullName: string;
  nip: string;
  active: boolean;
  currentListId: number | null;
  listType: ListType | null;
  initialDrops: number | null;
  completedDrops: number;
  remainingDrops: number | null;
  progressPercentage: number | null;
  status: WorkListStatus | null;
  dropsCompletedToday: number;
  problemDropCount: number;
  referenceDate: string | null;
}

export interface AdminDashboard {
  summary: DashboardSummary;
  technicians: PageResponse<TechnicianProgressRow>;
}

export interface DashboardFilters {
  search: string;
  listType: '' | ListType;
  listStatus: '' | WorkListStatus;
}

export type RankingPeriod = 'DAY' | 'MONTH';

export interface DropRankingRow {
  rank: number;
  technicianId: number;
  technicianCode: string;
  fullName: string;
  completedDrops: number;
}

export interface DropRanking {
  period: RankingPeriod;
  fromDate: string;
  toDate: string;
  totalDrops: number;
  rows: DropRankingRow[];
}

export type NotificationType = 'LIST_CREATED';

export interface AdminNotification {
  id: number;
  type: NotificationType;
  title: string;
  message: string;
  technicianId: number | null;
  technicianCode: string | null;
  technicianName: string | null;
  workListId: number | null;
  listType: ListType | null;
  read: boolean;
  createdAt: string;
}

export interface AdminNotificationList {
  unreadCount: number;
  notifications: AdminNotification[];
}

export type DayOffStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface TechnicianAccount {
  id: number;
  technicianCode: string;
  firstName: string;
  lastName: string;
  nip: string;
  role: Role;
  active: boolean;
}

export interface DayOffRequest {
  id: number;
  technicianId: number;
  technicianCode: string;
  technicianFirstName: string;
  technicianLastName: string;
  startDate: string;
  endDate: string;
  numberOfDays: number;
  reason: string | null;
  status: DayOffStatus;
  adminComment: string | null;
  processedById: number | null;
  processedByName: string | null;
  createdAt: string;
  decisionDate: string | null;
}
