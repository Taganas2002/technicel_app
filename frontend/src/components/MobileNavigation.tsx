import { NavLink } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { useLanguage } from '../i18n/LanguageContext';

export function MobileNavigation() {
  const { user } = useAuth();
  const { t } = useLanguage();
  const links =
    user?.role === 'ADMIN'
      ? [
          { to: '/admin/dashboard', label: t('navHome') },
          { to: '/admin/technicians', label: t('navTechsShort') },
          { to: '/admin/day-off', label: t('navOffShort') },
        ]
      : [
          { to: '/dashboard', label: t('navHome') },
          { to: '/progress', label: t('navDropsShort') },
          { to: '/lists/history', label: t('navHistShort') },
          { to: '/day-off', label: t('navOffShort') },
        ];

  return (
    <nav className="mobile-nav">
      {links.map((link) => (
        <NavLink key={link.to} to={link.to} className={({ isActive }) => (isActive ? 'active' : '')}>
          {link.label}
        </NavLink>
      ))}
    </nav>
  );
}
