import { FormEvent, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { isAxiosError } from 'axios';
import { InstallAppBanner } from '../components/InstallAppBanner';
import { LanguageSwitcher } from '../components/LanguageSwitcher';
import { ThemeSwitcher } from '../components/ThemeSwitcher';
import { homePathForRole, useAuth } from '../hooks/useAuth';
import { useLanguage } from '../i18n/LanguageContext';

export function LoginPage() {
  const { login } = useAuth();
  const { t } = useLanguage();
  const navigate = useNavigate();
  const [nip, setNip] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  function loginErrorMessage(err: unknown) {
    if (isAxiosError(err)) {
      if (err.response?.status === 401) {
        const message = err.response.data?.message;
        if (message === 'Account is inactive') {
          return t('accountDeactivated');
        }
        return t('loginBadCredentials');
      }
      if (!err.response) {
        return t('loginOffline');
      }
    }
    return t('loginFailed');
  }

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setError('');
    setLoading(true);
    try {
      const user = await login(nip.trim(), password);
      navigate(homePathForRole(user.role), { replace: true });
    } catch (err) {
      setError(loginErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="login-page">
      <section className="login-card">
        <div className="login-toolbar">
          <LanguageSwitcher variant="bar" />
          <ThemeSwitcher />
        </div>
        <p className="eyebrow">{t('appName')}</p>
        <h1>{t('signIn')}</h1>
        <p className="muted">{t('loginHint')}</p>
        <form onSubmit={onSubmit} className="login-form">
          <label htmlFor="nip">{t('nip')}</label>
          <input
            id="nip"
            name="nip"
            autoComplete="username"
            inputMode="text"
            value={nip}
            onChange={(event) => setNip(event.target.value)}
            required
          />
          <label htmlFor="password">{t('password')}</label>
          <input
            id="password"
            name="password"
            type="password"
            autoComplete="current-password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            required
          />
          {error ? <p className="error-text">{error}</p> : null}
          <button type="submit" className="primary-button" disabled={loading}>
            {loading ? t('signingIn') : t('signIn')}
          </button>
        </form>
        <InstallAppBanner />
      </section>
    </main>
  );
}
