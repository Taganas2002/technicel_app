import { useEffect, useState } from 'react';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { DayOffCard } from '../components/DayOffCard';
import { EmptyState } from '../components/EmptyState';
import { ErrorState } from '../components/ErrorState';
import { LoadingState } from '../components/LoadingState';
import { apiErrorMessage } from '../api/errors';
import { useLanguage } from '../i18n/LanguageContext';
import { decideDayOff, getAdminDayOffRequests } from '../services/dayOffService';
import type { DayOffRequest, DayOffStatus } from '../types';

export function AdminDayOffPage() {
  const { t } = useLanguage();
  const [status, setStatus] = useState<DayOffStatus | ''>('PENDING');
  const [requests, setRequests] = useState<DayOffRequest[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [comment, setComment] = useState('');
  const [pending, setPending] = useState<{ id: number; decision: 'approve' | 'reject' } | null>(null);
  const [saving, setSaving] = useState(false);

  async function reload(nextStatus = status) {
    const page = await getAdminDayOffRequests(nextStatus);
    setRequests(page.content);
  }

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    getAdminDayOffRequests(status)
      .then((page) => {
        if (!cancelled) {
          setRequests(page.content);
        }
      })
      .catch((err) => {
        if (!cancelled) {
          setError(apiErrorMessage(err, t('unableLoadDayOff')));
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
  }, [status, t]);

  async function onConfirm() {
    if (!pending || saving) {
      return;
    }
    setSaving(true);
    setError('');
    try {
      await decideDayOff(pending.id, pending.decision, comment);
      setPending(null);
      setComment('');
      await reload();
    } catch (err) {
      setError(apiErrorMessage(err, t('unableUpdateRequest')));
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="page">
      <form
        className="filter-bar card"
        onSubmit={(event) => {
          event.preventDefault();
        }}
      >
        <div>
          <label htmlFor="status">{t('status')}</label>
          <select
            id="status"
            value={status}
            onChange={(event) => setStatus(event.target.value as DayOffStatus | '')}
          >
            <option value="PENDING">{t('statusPending')}</option>
            <option value="APPROVED">{t('statusApproved')}</option>
            <option value="REJECTED">{t('statusRejected')}</option>
            <option value="">{t('all')}</option>
          </select>
        </div>
      </form>

      {error ? <ErrorState message={error} /> : null}
      {loading ? <LoadingState label={t('loadingRequests')} /> : null}
      {!loading && requests.length === 0 ? <EmptyState label={t('noRequestsFilter')} /> : null}

      <div className="card-list">
        {requests.map((request) => (
          <DayOffCard key={request.id} request={request}>
            {request.status === 'PENDING' ? (
              <div className="cta-stack">
                <button type="button" className="primary-button" onClick={() => setPending({ id: request.id, decision: 'approve' })}>
                  {t('approve')}
                </button>
                <button type="button" className="ghost-button" onClick={() => setPending({ id: request.id, decision: 'reject' })}>
                  {t('reject')}
                </button>
              </div>
            ) : null}
          </DayOffCard>
        ))}
      </div>

      <ConfirmDialog
        open={Boolean(pending)}
        title={pending?.decision === 'reject' ? t('rejectRequest') : t('approveRequest')}
        message={t('cannotUndo')}
        confirmLabel={saving ? t('saving') : pending?.decision === 'reject' ? t('reject') : t('approve')}
        disabled={saving}
        onCancel={() => setPending(null)}
        onConfirm={() => {
          void onConfirm();
        }}
      >
        <label htmlFor="adminComment">{t('adminComment')}</label>
        <textarea id="adminComment" rows={3} value={comment} onChange={(event) => setComment(event.target.value)} />
      </ConfirmDialog>
    </section>
  );
}
