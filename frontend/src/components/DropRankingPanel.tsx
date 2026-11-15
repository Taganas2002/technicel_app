import { ChangeEvent, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { EmptyState } from './EmptyState';
import { ErrorState } from './ErrorState';
import { LoadingState } from './LoadingState';
import { apiErrorMessage, todayIso } from '../api/errors';
import { useLanguage } from '../i18n/LanguageContext';
import { getDropRankings } from '../services/dashboardService';
import type { DropRanking, RankingPeriod } from '../types';

function monthValue(isoDate: string) {
  return isoDate.slice(0, 7);
}

export function DropRankingPanel() {
  const { t } = useLanguage();
  const [period, setPeriod] = useState<RankingPeriod>('DAY');
  const [date, setDate] = useState(todayIso());
  const [data, setData] = useState<DropRanking | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError('');
    getDropRankings(period, date)
      .then((response) => {
        if (!cancelled) {
          setData(response);
        }
      })
      .catch((err) => {
        if (!cancelled) {
          setError(apiErrorMessage(err, t('unableLoadRankings')));
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
  }, [period, date, t]);

  function onPeriodChange(next: RankingPeriod) {
    setPeriod(next);
  }

  function onDateChange(event: ChangeEvent<HTMLInputElement>) {
    const value = event.currentTarget.value;
    if (!value) {
      return;
    }
    setDate(period === 'MONTH' ? `${value}-01` : value);
  }

  const rows = data?.rows ?? [];

  return (
    <article className="card">
      <div className="card-row">
        <div>
          <p className="eyebrow">{t('whoDroppedMore')}</p>
          <h2>{t('dropsByTechnician')}</h2>
        </div>
        {data ? <p className="metric">{data.totalDrops}</p> : null}
      </div>
      <div className="mode-switch" role="group" aria-label={t('rankingPeriod')}>
        <button type="button" aria-pressed={period === 'DAY'} onClick={() => onPeriodChange('DAY')}>
          {t('day')}
        </button>
        <button type="button" aria-pressed={period === 'MONTH'} onClick={() => onPeriodChange('MONTH')}>
          {t('month')}
        </button>
      </div>
      <form className="form-stack ranking-date" onSubmit={(event) => event.preventDefault()}>
        <label htmlFor="rankingDate">{period === 'MONTH' ? t('month') : t('day')}</label>
        <input
          id="rankingDate"
          type={period === 'MONTH' ? 'month' : 'date'}
          value={period === 'MONTH' ? monthValue(date) : date}
          onChange={onDateChange}
        />
      </form>
      {data ? (
        <p className="muted">
          {data.fromDate === data.toDate
            ? t('dateSame', { date: data.fromDate })
            : t('dateRange', { from: data.fromDate, to: data.toDate })}
        </p>
      ) : null}
      {loading ? <LoadingState label={t('loadingRankings')} /> : null}
      {error ? <ErrorState message={error} /> : null}
      {!loading && !error && rows.length === 0 ? <EmptyState label={t('noRankings')} /> : null}
      {rows.length > 0 ? (
        <>
          <ul className="ranking-list">
            {rows.map((row) => (
              <li key={row.technicianId}>
                <span className="rank-number">{row.rank}</span>
                <div>
                  <strong>{row.fullName}</strong>
                </div>
                <strong>{row.completedDrops}</strong>
              </li>
            ))}
          </ul>
          <div className="table-wrap ranking-table">
            <table className="data-table">
              <thead>
                <tr>
                  <th>{t('rank')}</th>
                  <th>{t('technician')}</th>
                  <th>{t('drops')}</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {rows.map((row) => (
                  <tr key={row.technicianId}>
                    <td>{row.rank}</td>
                    <td>
                      <strong>{row.fullName}</strong>
                    </td>
                    <td>{row.completedDrops}</td>
                    <td>
                      <Link className="ghost-button" to={`/admin/technicians/${row.technicianId}`}>
                        {t('view')}
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      ) : null}
    </article>
  );
}
