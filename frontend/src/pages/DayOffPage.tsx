import { FormEvent, useEffect, useState } from 'react';
import { DayOffCard } from '../components/DayOffCard';
import { EmptyState } from '../components/EmptyState';
import { LoadingState } from '../components/LoadingState';
import { apiErrorMessage, todayIso } from '../api/errors';
import { useLanguage } from '../i18n/LanguageContext';
import { createDayOffRequest, getMyDayOffRequests } from '../services/dayOffService';
import type { DayOffRequest } from '../types';

export function DayOffPage() {
  const { t } = useLanguage();
  const [requests, setRequests] = useState<DayOffRequest[]>([]);
  const [startDate, setStartDate] = useState(todayIso());
  const [endDate, setEndDate] = useState(todayIso());
  const [reason, setReason] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  async function reload() {
    setRequests(await getMyDayOffRequests());
  }

  useEffect(() => {
    let cancelled = false;
    getMyDayOffRequests()
      .then((items) => {
        if (!cancelled) {
          setRequests(items);
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
  }, [t]);

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setError('');
    setSuccess('');
    setSaving(true);
    try {
      await createDayOffRequest(startDate, endDate, reason);
      setReason('');
      setSuccess(t('requestSent'));
      await reload();
    } catch (err) {
      setError(apiErrorMessage(err, t('unableSendRequest')));
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="page">
      <article className="card">
        <h2>{t('requestDaysOff')}</h2>
        <form className="form-stack" onSubmit={onSubmit}>
          <label htmlFor="startDate">{t('startDate')}</label>
          <input id="startDate" type="date" value={startDate} onChange={(event) => setStartDate(event.target.value)} required />
          <label htmlFor="endDate">{t('endDate')}</label>
          <input id="endDate" type="date" value={endDate} onChange={(event) => setEndDate(event.target.value)} required />
          <label htmlFor="reason">{t('reason')}</label>
          <textarea id="reason" rows={3} value={reason} onChange={(event) => setReason(event.target.value)} />
          {error ? <p className="error-text">{error}</p> : null}
          {success ? <p className="success-text">{success}</p> : null}
          <button type="submit" className="primary-button" disabled={saving}>
            {saving ? t('sending') : t('submitRequest')}
          </button>
        </form>
      </article>

      {loading ? <LoadingState label={t('loadingRequests')} /> : null}
      {!loading && requests.length === 0 ? <EmptyState label={t('noDayOffYet')} /> : null}
      <div className="card-list">
        {requests.map((request) => (
          <DayOffCard key={request.id} request={request} />
        ))}
      </div>
    </section>
  );
}
