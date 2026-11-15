import { Outlet } from 'react-router-dom';
import { Header } from '../components/Header';
import { InstallAppBanner } from '../components/InstallAppBanner';
import { MobileNavigation } from '../components/MobileNavigation';
import { NotificationBell } from '../components/NotificationBell';
import { Sidebar } from '../components/Sidebar';
import { useAuth } from '../hooks/useAuth';
import { useLanguage } from '../i18n/LanguageContext';

export function AdminLayout() {
  const { user } = useAuth();
  const { t } = useLanguage();
  const name = user ? `${user.firstName} ${user.lastName}` : '';

  return (
    <div className="app-shell">
      <Sidebar />
      <div className="app-main">
        <Header title={t('adminTitle')} name={name}>
          <NotificationBell />
        </Header>
        <InstallAppBanner />
        <Outlet />
      </div>
      <MobileNavigation />
    </div>
  );
}
