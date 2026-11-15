import { useAuth } from '../hooks/useAuth';
import { translateStatus, useLanguage } from '../i18n/LanguageContext';

export function ProfilePage() {
  const { user } = useAuth();
  const { t } = useLanguage();
  if (!user) {
    return null;
  }

  return (
    <section className="page">
      <article className="card">
        <h2>
          {user.firstName} {user.lastName}
        </h2>
        <p>{t('roleLabel', { role: translateStatus(user.role, t) })}</p>
        <p className="muted">{t('profileHint')}</p>
      </article>
    </section>
  );
}
