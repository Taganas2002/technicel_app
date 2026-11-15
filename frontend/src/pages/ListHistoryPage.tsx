import { useEffect, useState } from 'react';
import { EmptyState } from '../components/EmptyState';
import { ErrorState } from '../components/ErrorState';
import { LoadingState } from '../components/LoadingState';
import { WorkListCard } from '../components/WorkListCard';
import { apiErrorMessage } from '../api/errors';
import { useLanguage } from '../i18n/LanguageContext';
import { getFinishedLists, getListProgress } from '../services/listService';
import type { DailyProgress, WorkList } from '../types';

export function ListHistoryPage() {
  const { t } = useLanguage();
  const [lists, setLists] = useState<WorkList[]>([]);
  const [progressByList, setProgressByList] = useState<Record<number, DailyProgress[]>>({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    getFinishedLists()
      .then(async (finished) => {
        const entries = await Promise.all(finished.map(async (list) => [list.id, await getListProgress(list.id)] as const));
        if (!cancelled) {
          setLists(finished);
          setProgressByList(Object.fromEntries(entries));
        }
      })
      .catch((err) => {
        if (!cancelled) {
          setError(apiErrorMessage(err, t('unableLoadHistory')));
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false);
        }
      });
    return () => {
      cancelled = true;
    };
  }, [t]);

  if (loading) {
    return (
      <section className="page">
        <LoadingState label={t('loadingHistory')} />
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

  return (
    <section className="page">
      <article className="card">
        <h2>{t('finishedListsTitle')}</h2>
        <p className="muted">{t('finishedListsHint')}</p>
      </article>
      {lists.length === 0 ? <EmptyState label={t('noFinishedLists')} /> : null}
      <div className="card-list">
        {lists.map((list) => (
          <WorkListCard key={list.id} list={list} progress={progressByList[list.id]} />
        ))}
      </div>
    </section>
  );
}
