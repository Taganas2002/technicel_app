import { useState } from 'react';
import { apiErrorMessage } from '../api/errors';
import { useLanguage } from '../i18n/LanguageContext';
import { finishWithProblems } from '../services/listService';
import type { WorkList } from '../types';
import { ConfirmDialog } from './ConfirmDialog';

export function FinishWithProblems({
  list,
  onFinished,
}: {
  list: WorkList;
  onFinished: (list: WorkList) => void;
}) {
  const { t } = useLanguage();
  const [open, setOpen] = useState(false);
  const [note, setNote] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  if (list.status !== 'IN_PROGRESS' || list.remaining <= 0) {
    return null;
  }

  async function confirm() {
    setSaving(true);
    setError('');
    try {
      const updated = await finishWithProblems(list.id, note);
      setOpen(false);
      setNote('');
      onFinished(updated);
    } catch (err) {
      setError(apiErrorMessage(err, t('unableSaveProgress')));
    } finally {
      setSaving(false);
    }
  }

  return (
    <>
      <button type="button" className="ghost-button" onClick={() => setOpen(true)}>
        {t('finishWithProblems')}
      </button>
      <ConfirmDialog
        open={open}
        title={t('finishWithProblemsTitle')}
        message={t('finishWithProblemsMessage', { count: list.remaining, done: list.totalCompleted })}
        confirmLabel={t('finishWithProblems')}
        disabled={saving}
        onConfirm={() => void confirm()}
        onCancel={() => {
          if (!saving) {
            setOpen(false);
          }
        }}
      >
        <label htmlFor="problemNote">{t('problemNote')}</label>
        <textarea
          id="problemNote"
          maxLength={500}
          value={note}
          onChange={(event) => setNote(event.target.value)}
        />
        {error ? <p className="error-text">{error}</p> : null}
      </ConfirmDialog>
    </>
  );
}
