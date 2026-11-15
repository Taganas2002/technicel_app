import { Navigate, Route, Routes } from 'react-router-dom';
import { AdminLayout } from '../layouts/AdminLayout';
import { TechnicianLayout } from '../layouts/TechnicianLayout';
import { LoginPage } from '../pages/LoginPage';
import { AdminDashboardPage } from '../pages/AdminDashboardPage';
import { AdminDayOffPage } from '../pages/AdminDayOffPage';
import { CreateListPage } from '../pages/CreateListPage';
import { CurrentListPage } from '../pages/CurrentListPage';
import { DailyProgressPage } from '../pages/DailyProgressPage';
import { DayOffPage } from '../pages/DayOffPage';
import { ListHistoryPage } from '../pages/ListHistoryPage';
import { ProfilePage } from '../pages/ProfilePage';
import { TechnicianDashboardPage } from '../pages/TechnicianDashboardPage';
import { TechnicianDetailsPage } from '../pages/TechnicianDetailsPage';
import { TechniciansPage } from '../pages/TechniciansPage';
import { GuestRoute, ProtectedRoute, RoleRoute } from './guards';

export function AppRoutes() {
  return (
    <Routes>
      <Route element={<GuestRoute />}>
        <Route path="/login" element={<LoginPage />} />
      </Route>

      <Route element={<ProtectedRoute />}>
        <Route element={<RoleRoute role="ADMIN" />}>
          <Route element={<AdminLayout />}>
            <Route path="/admin/dashboard" element={<AdminDashboardPage />} />
            <Route path="/admin/technicians" element={<TechniciansPage />} />
            <Route path="/admin/technicians/:id" element={<TechnicianDetailsPage />} />
            <Route path="/admin/day-off" element={<AdminDayOffPage />} />
          </Route>
        </Route>

        <Route element={<RoleRoute role="TECHNICIAN" />}>
          <Route element={<TechnicianLayout />}>
            <Route path="/dashboard" element={<TechnicianDashboardPage />} />
            <Route path="/lists/current" element={<CurrentListPage />} />
            <Route path="/lists/new" element={<CreateListPage />} />
            <Route path="/lists/history" element={<ListHistoryPage />} />
            <Route path="/progress" element={<DailyProgressPage />} />
            <Route path="/day-off" element={<DayOffPage />} />
            <Route path="/profile" element={<ProfilePage />} />
          </Route>
        </Route>
      </Route>

      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}