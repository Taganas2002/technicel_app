import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { useLanguage } from '../i18n/LanguageContext';
import { LanguageSwitcher } from './LanguageSwitcher';
import { ThemeSwitcher } from './ThemeSwitcher';

export function Sidebar() {
  const { user, logout } = useAuth();
  const { t } = useLanguage();
  const navigate = useNavigate();
  const links =
    user?.role === 'ADMIN'
      ? [
          { to: '/admin/dashboard', label: t('navDashboard') },
          { to: '/admin/technicians', label: t('navTechnicians') },
          { to: '/admin/day-off', label: t('navDaysOff') },
        ]
      : [
          { to: '/dashboard', label: t('navHome') },
          { to: '/progress', label: t('navAddDrops') },
          { to: '/lists/current', label: t('navCurrentList') },
          { to: '/lists/history', label: t('navHistory') },
          { to: '/day-off', label: t('navDaysOffTech') },
          { to: '/profile', label: t('navProfile') },
        ];

  function onLogout() {
    logout();
    navigate('/login', { replace: true });
  }

  return (
    <aside className="sidebar">
      <div className="brand">{t('brand')}</div>
      <nav>
        {links.map((link) => (
          <NavLink key={link.to} to={link.to} className={({ isActive }) => (isActive ? 'active' : '')}>
            {link.label}
          </NavLink>
        ))}
      </nav>
      <LanguageSwitcher />
      <ThemeSwitcher />
      <button type="button" className="ghost-button" onClick={onLogout}>
        {t('logOut')}
      </button>
    </aside>
  );
}
