import { Link } from 'react-router-dom';
import { todayIso } from '../api/errors';
import { ErrorState } from '../components/ErrorState';
import { LoadingState } from '../components/LoadingState';
import { FinishWithProblems } from '../components/FinishWithProblems';
import { WorkListCard } from '../components/WorkListCard';
import { useActiveList } from '../hooks/useActiveList';
import { useLanguage } from '../i18n/LanguageContext';

export function CurrentListPage() {
  const { list, progress, loading, error, reload } = useActiveList();
  const { t } = useLanguage();
  const todayDrops = progress.find((entry) => entry.workDate === todayIso())?.completedDrops ?? 0;

  if (loading) {
    return (
      <section className="page">
        <LoadingState label={t('loadingCurrentList')} />
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
          <h2>{t('noActiveList')}</h2>
          <p className="muted">{t('createListToStart')}</p>
          <Link className="primary-button" to="/lists/new">
            {t('createList')}
          </Link>
        </article>
      </section>
    );
  }

  return (
    <section className="page">
      <WorkListCard list={list} progress={progress}>
        {list.status === 'IN_PROGRESS' ? (
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
          </div>
        ) : todayDrops > 0 ? (
          <Link className="ghost-button" to="/progress">
            {t('fixTodayNumber')}
          </Link>
        ) : null}
      </WorkListCard>
    </section>
  );
}
