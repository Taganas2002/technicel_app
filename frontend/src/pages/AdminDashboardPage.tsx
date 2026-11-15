import { FormEvent, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { EmptyState } from '../components/EmptyState';
import { ErrorState } from '../components/ErrorState';
import { LoadingState } from '../components/LoadingState';
import { ProgressBar, unfinishedShare } from '../components/ProgressBar';
import { StatusBadge } from '../components/StatusBadge';
import { TechnicianCard } from '../components/TechnicianCard';
import { apiErrorMessage } from '../api/errors';
import { DropRankingPanel } from '../components/DropRankingPanel';
import { listTypeLabel, useLanguage } from '../i18n/LanguageContext';
import { getAdminDashboard } from '../services/dashboardService';
import type { AdminDashboard, DashboardFilters, TechnicianProgressRow } from '../types';

const emptyFilters: DashboardFilters = {
  search: '',
  listType: '',
  listStatus: '',
};

function formatProgress(row: TechnicianProgressRow) {
  if (row.progressPercentage == null) {
    return '—';
  }
  return `${Math.round(row.progressPercentage)}%`;
}

export function AdminDashboardPage() {
  const { t } = useLanguage();
  const [filters, setFilters] = useState<DashboardFilters>(emptyFilters);
  const [applied, setApplied] = useState<DashboardFilters>(emptyFilters);
  const [data, setData] = useState<AdminDashboard | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError('');
    getAdminDashboard(applied)
      .then((response) => {
        if (!cancelled) {
          setData(response);
        }
      })
      .catch((err) => {
        if (!cancelled) {
          setError(apiErrorMessage(err, t('loadingDashboard')));
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
  }, [applied, t]);

  function onFilter(event: FormEvent) {
    event.preventDefault();
    setApplied(filters);
  }

  const summary = data?.summary;
  const rows = data?.technicians.content ?? [];

  return (
    <section className="page">
      {summary ? (
        <div className="summary-grid">
          <SummaryCard label={t('totalTechnicians')} value={summary.totalTechnicians} />
          <SummaryCard label={t('activeTechnicians')} value={summary.activeTechnicians} />
          <SummaryCard label={t('workingToday')} value={summary.techniciansWorkingToday} />
          <SummaryCard label={t('activeLists')} value={summary.activeLists} />
          <SummaryCard label={t('finishedLists')} value={summary.finishedLists} />
          <SummaryCard label={t('dropsToday')} value={summary.dropsCompletedToday} />
          <SummaryCard label={t('pendingDaysOff')} value={summary.pendingDayOffRequests} />
        </div>
      ) : null}

      <DropRankingPanel />

      <form className="filter-bar card" onSubmit={onFilter}>
        <div>
          <label htmlFor="search">{t('search')}</label>
          <input
            id="search"
            value={filters.search}
            onChange={(event) => setFilters((current) => ({ ...current, search: event.target.value }))}
            placeholder={t('searchPlaceholder')}
          />
        </div>
        <div>
          <label htmlFor="listType">{t('listType')}</label>
          <select
            id="listType"
            value={filters.listType}
            onChange={(event) => setFilters((current) => ({ ...current, listType: event.target.value as DashboardFilters['listType'] }))}
          >
            <option value="">{t('allTypes')}</option>
            <option value="OBD">OBD</option>
            <option value="DPO">DPO</option>
            <option value="OBD_DPO">OBD + DPO</option>
          </select>
        </div>
        <div>
          <label htmlFor="listStatus">{t('status')}</label>
          <select
            id="listStatus"
            value={filters.listStatus}
            onChange={(event) =>
              setFilters((current) => ({ ...current, listStatus: event.target.value as DashboardFilters['listStatus'] }))
            }
          >
            <option value="">{t('allStatuses')}</option>
            <option value="IN_PROGRESS">{t('statusInProgress')}</option>
            <option value="FINISHED">{t('statusFinished')}</option>
          </select>
        </div>
        <button type="submit" className="primary-button">
          {t('applyFilters')}
        </button>
      </form>

      {loading ? <LoadingState label={t('loadingTechnicians')} /> : null}
      {error ? <ErrorState message={error} /> : null}
      {!loading && !error && rows.length === 0 ? <EmptyState label={t('noTechniciansMatch')} /> : null}

      {rows.length > 0 ? (
        <>
          <div className="mobile-cards">
            {rows.map((row) => (
              <TechnicianCard key={row.technicianId} code="" name={row.fullName}>
                <p className="muted">{t('nip')} {row.nip}</p>
                <p>{row.listType ? listTypeLabel(row.listType) : t('noCurrentList')}</p>
                {row.initialDrops != null ? (
                  <>
                    <p>{t('dropsCount', { completed: row.completedDrops, initial: row.initialDrops })}</p>
                    <ProgressBar
                      value={row.progressPercentage ?? 0}
                      unfinished={unfinishedShare(row.problemDropCount, row.initialDrops ?? 0)}
                    />
                    <p>
                      <span className={row.problemDropCount > 0 ? 'done-line' : undefined}>{formatProgress(row)}</span>
                      {' · '}
                      {row.problemDropCount > 0
                        ? <span className="unfinished-line">{t('problemCount', { count: row.problemDropCount })}</span>
                        : t('remainingCount', { count: row.remainingDrops ?? 0 })}
                    </p>
                  </>
                ) : null}
                <p>{t('todayCount', { count: row.dropsCompletedToday })}</p>
                {row.status ? <StatusBadge status={row.status} problemDropCount={row.problemDropCount} /> : <p className="muted">{t('noActiveList')}</p>}
                <Link className="primary-button" to={`/admin/technicians/${row.technicianId}`}>
                  {t('viewTechnician')}
                </Link>
              </TechnicianCard>
            ))}
          </div>

          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>{t('technician')}</th>
                  <th>{t('nip')}</th>
                  <th>{t('currentList')}</th>
                  <th>{t('completed')}</th>
                  <th>{t('remaining')}</th>
                  <th>{t('progress')}</th>
                  <th>{t('today')}</th>
                  <th>{t('status')}</th>
                  <th>{t('actions')}</th>
                </tr>
              </thead>
              <tbody>
                {rows.map((row) => (
                  <tr key={row.technicianId}>
                    <td>
                      <strong>{row.fullName}</strong>
                    </td>
                    <td>{row.nip}</td>
                    <td>{row.listType ? listTypeLabel(row.listType) : '—'}</td>
                    <td>
                      {row.initialDrops == null ? '—' : `${row.completedDrops} / ${row.initialDrops}`}
                      {row.problemDropCount > 0 ? <div className="unfinished-line">{t('problemCount', { count: row.problemDropCount })}</div> : null}
                    </td>
                    <td>{row.remainingDrops ?? '—'}</td>
                    <td>{formatProgress(row)}</td>
                    <td>{row.dropsCompletedToday}</td>
                    <td>{row.status ? <StatusBadge status={row.status} problemDropCount={row.problemDropCount} /> : '—'}</td>
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
    </section>
  );
}

function SummaryCard({ label, value }: { label: string; value: number }) {
  return (
    <article className="card summary-card">
      <p className="eyebrow">{label}</p>
      <p className="metric">{value}</p>
    </article>
  );
}
