import { Link, useNavigate } from 'react-router-dom';
import { todayIso } from '../api/errors';
import { CreateListForm } from '../components/CreateListForm';
import { EmptyState } from '../components/EmptyState';
import { ErrorState } from '../components/ErrorState';
import { LoadingState } from '../components/LoadingState';
import { FinishWithProblems } from '../components/FinishWithProblems';
import { WorkListCard } from '../components/WorkListCard';
import { useAuth } from '../hooks/useAuth';
import { useActiveList } from '../hooks/useActiveList';
import { useLanguage } from '../i18n/LanguageContext';

export function TechnicianDashboardPage() {
  const { user } = useAuth();
  const { t } = useLanguage();
  const navigate = useNavigate();
  const { list, progress, loading, error, reload } = useActiveList();
  const todayDrops = progress.find((entry) => entry.workDate === todayIso())?.completedDrops ?? 0;
  const recent = [...progress].reverse().slice(0, 5);

  if (loading) {
    return (
      <section className="page">
        <LoadingState label={t('loadingYourList')} />
      </section>
    );
  }

  if (error) {
    return (
      <section className="page">
        <ErrorState message={error} />
      </section>
    );
  }

  if (!list) {
    return (
      <section className="page">
        <article className="card">
          <h2>{t('hiName', { name: user?.firstName ?? '' })}</h2>
          <p className="muted">{t('noActiveListHint')}</p>
          <CreateListForm
            onCreated={() => {
              void reload();
              navigate('/lists/current');
            }}
          />
        </article>
      </section>
    );
  }

  return (
    <section className="page">
      <article className="card">
        <p className="eyebrow">{t('hello')}</p>
        <h2>{t('hiName', { name: user?.firstName ?? '' })}</h2>
      </article>

      <WorkListCard list={list}>
        <p className="today-line">{t('todayDropsLine', { count: todayDrops })}</p>
        <div className="cta-stack">
          <Link className="primary-button" to="/progress">
            {t('addTodaysDrops')}
          </Link>
          {todayDrops > 0 ? (
            <Link className="ghost-button" to="/progress">
              {t('fixTodayNumber')}
            </Link>
          ) : null}
          <FinishWithProblems list={list} onFinished={() => void reload()} />
          <Link className="ghost-button" to="/lists/current">
            {t('viewCurrentList')}
          </Link>
          <Link className="ghost-button" to="/day-off">
            {t('requestDayOff')}
          </Link>
        </div>
      </WorkListCard>

      <article className="card">
        <h2>{t('recentProgress')}</h2>
        {recent.length === 0 ? (
          <EmptyState label={t('noDropsYet')} />
        ) : (
          <ul className="progress-list">
            {recent.map((entry) => (
              <li key={entry.id}>
                <span>{entry.workDate}</span>
                <strong>{t('dropsLabel', { count: entry.completedDrops })}</strong>
              </li>
            ))}
          </ul>
        )}
      </article>
    </section>
  );
}
