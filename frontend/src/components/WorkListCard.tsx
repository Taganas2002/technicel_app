import type { ReactNode } from 'react';
import { listTypeLabel, useLanguage } from '../i18n/LanguageContext';
import { ProgressBar, unfinishedShare } from './ProgressBar';
import { StatusBadge } from './StatusBadge';
import type { DailyProgress, WorkList } from '../types';

export function WorkListCard({
  list,
  progress,
  children,
}: {
  list: WorkList;
  progress?: DailyProgress[];
  children?: ReactNode;
}) {
  const { t } = useLanguage();
  return (
    <article className="card">
      <div className="card-row">
        <div>
          <p className="eyebrow">{listTypeLabel(list.listType)}</p>
          <h2>{list.status === 'FINISHED' ? t('finishedList') : t('workList')}</h2>
        </div>
        <StatusBadge status={list.status} problemDropCount={list.problemDropCount} />
      </div>
      <ProgressBar value={list.progressPercentage} unfinished={unfinishedShare(list.problemDropCount, list.initialDropCount)} />
      <div className="metric-grid">
        <div>
          <p className="eyebrow">{t('initial')}</p>
          <p className="metric">{list.initialDropCount}</p>
        </div>
        <div>
          <p className="eyebrow">{t('completed')}</p>
          <p className="metric">{list.totalCompleted}</p>
        </div>
        <div>
          <p className="eyebrow">{list.problemDropCount > 0 ? t('problemDrops') : t('remaining')}</p>
          <p className={list.problemDropCount > 0 ? 'metric metric-unfinished' : 'metric'}>
            {list.problemDropCount > 0 ? list.problemDropCount : list.remaining}
          </p>
        </div>
        <div>
          <p className="eyebrow">{t('progress')}</p>
          <p className="metric">{Math.round(list.progressPercentage)}%</p>
        </div>
      </div>
      <p className="muted">{t('referenceDateLabel', { date: list.referenceDate })}</p>
      {list.completionDate ? <p className="muted">{t('completedOn', { date: list.completionDate })}</p> : null}
      {list.problemNote ? <p className="muted">{list.problemNote}</p> : null}
      {progress && progress.length > 0 ? (
        <ul className="progress-list">
          {progress.map((entry) => (
            <li key={entry.id}>
              <span>{entry.workDate}</span>
              <strong>{t('dropsLabel', { count: entry.completedDrops })}</strong>
            </li>
          ))}
        </ul>
      ) : null}
      {children}
    </article>
  );
}
