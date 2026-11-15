import type { ReactNode } from 'react';
import { useLanguage } from '../i18n/LanguageContext';
import { StatusBadge } from './StatusBadge';
import type { DayOffRequest } from '../types';

export function DayOffCard({
  request,
  children,
}: {
  request: DayOffRequest;
  children?: ReactNode;
}) {
  const { t } = useLanguage();
  const name = `${request.technicianFirstName} ${request.technicianLastName}`.trim();
  return (
    <article className="card">
      <div className="card-row">
        <div>
          <h2>{name || t('dayOff')}</h2>
        </div>
        <StatusBadge status={request.status} />
      </div>
      <p>
        {request.startDate} → {request.endDate} · {request.numberOfDays}{' '}
        {request.numberOfDays === 1 ? t('dayOne') : t('dayMany')}
      </p>
      {request.reason ? <p className="muted">{request.reason}</p> : null}
      {request.adminComment ? <p className="muted">{t('adminPrefix', { comment: request.adminComment })}</p> : null}
      {children}
    </article>
  );
}
