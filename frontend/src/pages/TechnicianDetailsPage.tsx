import { FormEvent, useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { EmptyState } from '../components/EmptyState';
import { ErrorState } from '../components/ErrorState';
import { LoadingState } from '../components/LoadingState';
import { WorkListCard } from '../components/WorkListCard';
import { apiErrorMessage } from '../api/errors';
import { useLanguage } from '../i18n/LanguageContext';
import { deleteWorkList, updateInitialDrops } from '../services/listService';
import {
  getListProgressAsAdmin,
  getTechnician,
  getTechnicianLists,
  updateTechnicianPassword,
  updateTechnicianStatus,
} from '../services/technicianService';
import type { DailyProgress, TechnicianAccount, WorkList } from '../types';

export function TechnicianDetailsPage() {
  const { t } = useLanguage();
  const { id } = useParams();
  const technicianId = Number(id);
  const [technician, setTechnician] = useState<TechnicianAccount | null>(null);
  const [lists, setLists] = useState<WorkList[]>([]);
  const [progressByList, setProgressByList] = useState<Record<number, DailyProgress[]>>({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [dropCounts, setDropCounts] = useState<Record<number, string>>({});
  const [savingId, setSavingId] = useState<number | null>(null);
  const [message, setMessage] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [savingPassword, setSavingPassword] = useState(false);
  const [savingStatus, setSavingStatus] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);

  async function reload() {
    const [account, workLists] = await Promise.all([getTechnician(technicianId), getTechnicianLists(technicianId)]);
    const progressEntries = await Promise.all(
      workLists.map(async (list) => [list.id, await getListProgressAsAdmin(list.id)] as const),
    );
    setTechnician(account);
    setLists(workLists);
    setProgressByList(Object.fromEntries(progressEntries));
    setDropCounts(Object.fromEntries(workLists.map((list) => [list.id, String(list.initialDropCount)])));
  }

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    reload()
      .catch((err) => {
        if (!cancelled) {
          setError(apiErrorMessage(err, t('unableLoadTechnician')));
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
  }, [technicianId, t]);

  async function onResetPassword(event: FormEvent) {
    event.preventDefault();
    if (!technician || savingPassword) {
      return;
    }
    if (newPassword.trim().length < 6) {
      setError(t('passwordTooShort'));
      setMessage('');
      return;
    }
    setSavingPassword(true);
    setError('');
    setMessage('');
    try {
      const updated = await updateTechnicianPassword(technician, newPassword);
      setTechnician(updated);
      setNewPassword('');
      setMessage(t('passwordUpdated'));
    } catch (err) {
      setError(apiErrorMessage(err, t('unableResetPassword')));
    } finally {
      setSavingPassword(false);
    }
  }

  async function onToggleStatus() {
    if (!technician || savingStatus) {
      return;
    }
    setSavingStatus(true);
    setError('');
    setMessage('');
    try {
      const updated = await updateTechnicianStatus(technician.id, !technician.active);
      setTechnician(updated);
      setMessage(updated.active ? t('accountActivated') : t('accountDeactivatedDone'));
    } catch (err) {
      setError(apiErrorMessage(err, t('unableUpdateStatus')));
    } finally {
      setSavingStatus(false);
    }
  }

  async function onDeleteList() {
    if (deleteId == null) {
      return;
    }
    setMessage('');
    setError('');
    setSavingId(deleteId);
    try {
      await deleteWorkList(deleteId);
      setDeleteId(null);
      await reload();
      setMessage(t('listDeleted'));
    } catch (err) {
      setError(apiErrorMessage(err, t('unableUpdateDrops')));
    } finally {
      setSavingId(null);
    }
  }

  async function onSaveDrops(event: FormEvent, listId: number) {
    event.preventDefault();
    setMessage('');
    setSavingId(listId);
    try {
      await updateInitialDrops(listId, Number(dropCounts[listId]));
      await reload();
      setMessage(t('initialDropsUpdated'));
    } catch (err) {
      setError(apiErrorMessage(err, t('unableUpdateDrops')));
    } finally {
      setSavingId(null);
    }
  }

  if (loading) {
    return (
      <section className="page">
        <LoadingState label={t('loadingTechnician')} />
      </section>
    );
  }

  if (error && !technician) {
    return (
      <section className="page">
        <ErrorState message={error} />
      </section>
    );
  }

  if (!technician) {
    return (
      <section className="page">
        <EmptyState label={t('technicianNotFound')} />
      </section>
    );
  }

  return (
    <section className="page">
      <article className="card">
        <h2>
          {technician.firstName} {technician.lastName}
        </h2>
        <p>{t('nip')} {technician.nip}</p>
        <p>{technician.active ? t('statusActive') : t('statusInactive')}</p>
        <button
          type="button"
          className={technician.active ? 'ghost-button' : 'primary-button'}
          onClick={() => void onToggleStatus()}
          disabled={savingStatus}
        >
          {savingStatus ? t('saving') : technician.active ? t('deactivateAccount') : t('activateAccount')}
        </button>
        <form className="form-stack" onSubmit={onResetPassword}>
          <label htmlFor="newPassword">{t('newPassword')}</label>
          <input
            id="newPassword"
            type="password"
            minLength={6}
            autoComplete="new-password"
            value={newPassword}
            onChange={(event) => setNewPassword(event.target.value)}
            required
          />
          <button type="submit" className="primary-button" disabled={savingPassword}>
            {savingPassword ? t('saving') : t('resetPassword')}
          </button>
        </form>
        {message ? <p className="success-text">{message}</p> : null}
        {error ? <p className="error-text">{error}</p> : null}
      </article>

      {lists.length === 0 ? <EmptyState label={t('noListsYet')} /> : null}
      {lists.map((list) => (
        <WorkListCard key={list.id} list={list} progress={progressByList[list.id]}>
          <form className="form-stack" onSubmit={(event) => onSaveDrops(event, list.id)}>
            <label htmlFor={`drops-${list.id}`}>{t('initialDrops')}</label>
            <input
              id={`drops-${list.id}`}
              type="number"
              min={1}
              value={dropCounts[list.id] ?? ''}
              onChange={(event) => setDropCounts((current) => ({ ...current, [list.id]: event.target.value }))}
              required
            />
            <button type="submit" className="primary-button" disabled={savingId === list.id}>
              {savingId === list.id ? t('saving') : t('updateInitialDrops')}
            </button>
          </form>
          <button type="button" className="ghost-button" onClick={() => setDeleteId(list.id)}>
            {t('deleteList')}
          </button>
        </WorkListCard>
      ))}
      <ConfirmDialog
        open={deleteId != null}
        title={t('deleteListTitle')}
        message={t('deleteListMessage')}
        confirmLabel={t('deleteList')}
        disabled={savingId != null}
        onConfirm={() => void onDeleteList()}
        onCancel={() => {
          if (savingId == null) {
            setDeleteId(null);
          }
        }}
      />
    </section>
  );
}
