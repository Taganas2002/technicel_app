import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { useLanguage } from '../i18n/LanguageContext';
import { LanguageSwitcher } from './LanguageSwitcher';
import { ThemeSwitcher } from './ThemeSwitcher';
import type { ReactNode } from 'react';

export function Header({ title, name, children }: { title: string; name: string; children?: ReactNode }) {
  const { logout } = useAuth();
  const { t } = useLanguage();
  const navigate = useNavigate();

  function onLogout() {
    logout();
    navigate('/login', { replace: true });
  }

  return (
    <header className="app-header">
      <div className="header-title">
        <div>
          <p className="eyebrow">{t('appShort')}</p>
          <h1>{title}</h1>
        </div>
        <p className="header-user">{name}</p>
      </div>
      <div className="header-actions">
        <div className="header-tools">
          <LanguageSwitcher />
          <ThemeSwitcher />
        </div>
        {children}
        <button type="button" className="ghost-button header-logout" onClick={onLogout}>
          {t('logOut')}
        </button>
      </div>
    </header>
  );
}
