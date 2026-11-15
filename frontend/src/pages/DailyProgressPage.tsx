import { FormEvent, useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { ErrorState } from '../components/ErrorState';
import { LoadingState } from '../components/LoadingState';
import { FinishWithProblems } from '../components/FinishWithProblems';
import { ProgressBar, unfinishedShare } from '../components/ProgressBar';
import { StatusBadge } from '../components/StatusBadge';
import { useActiveList } from '../hooks/useActiveList';
import { listTypeLabel, useLanguage } from '../i18n/LanguageContext';
import { apiErrorMessage, todayIso } from '../api/errors';
import { getListProgress, saveDailyProgress } from '../services/listService';

type ProgressMode = 'add' | 'correct';

export function DailyProgressPage() {
  const { t } = useLanguage();
  const { list, progress, loading, error, setList, setProgress } = useActiveList();
  const today = todayIso();
  const todayEntry = useMemo(() => progress.find((entry) => entry.workDate === today), [progress, today]);
  const alreadyToday = todayEntry?.completedDrops ?? 0;
  const [mode, setMode] = useState<ProgressMode>('add');
  const [drops, setDrops] = useState('');
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState('');
  const [success, setSuccess] = useState('');

  const canAdd = Boolean(list && list.status !== 'FINISHED' && list.remaining > 0);
  const canCorrect = alreadyToday > 0;
  const effectiveMode: ProgressMode = canAdd && mode === 'add' ? 'add' : canCorrect ? 'correct' : 'add';
  const correcting = effectiveMode === 'correct';
  const parsed = Number(drops);
  const hasValue = Number.isFinite(parsed) && parsed > 0;
  const nextToday = correcting ? (hasValue ? parsed : alreadyToday) : alreadyToday + (hasValue ? parsed : 0);

  useEffect(() => {
    if (correcting && alreadyToday > 0) {
      setDrops((current) => (current === '' ? String(alreadyToday) : current));
    }
  }, [correcting, alreadyToday]);

  function switchMode(next: ProgressMode) {
    setMode(next);
    setFormError('');
    setSuccess('');
    setDrops(next === 'correct' && alreadyToday > 0 ? String(alreadyToday) : '');
  }

  async function onSave(event: FormEvent) {
    event.preventDefault();
    if (!list || saving) {
      return;
    }
    const value = Number(drops);
    const maxToday = alreadyToday + list.remaining;
    if (!Number.isFinite(value) || value < 1) {
      setFormError(correcting ? t('enterCorrectTotal') : t('enterDropsToAdd'));
      return;
    }
    if (correcting) {
      if (value === alreadyToday) {
        setFormError(t('todayAlready', { count: alreadyToday }));
        return;
      }
      if (value > maxToday) {
        setFormError(t('todayCannotExceed', { max: maxToday }));
        return;
      }
    } else if (value > list.remaining) {
      setFormError(t('onlyRemaining', { count: list.remaining }));
      return;
    }
    setFormError('');
    setSuccess('');
    setSaving(true);
    try {
      const nextTotal = correcting ? value : alreadyToday + value;
      const saved = await saveDailyProgress(list.id, nextTotal, today);
      setList(saved.workList);
      setProgress(await getListProgress(saved.workList.id));
      setSuccess(
        saved.workList.status === 'FINISHED'
          ? t('listCompletedShort')
          : correcting
            ? t('updatedToday', { from: alreadyToday, to: saved.progress.completedDrops })
            : t('addedToday', { added: value, total: saved.progress.completedDrops }),
      );
      setMode('add');
      setDrops('');
    } catch (err) {
      setFormError(apiErrorMessage(err, t('unableSaveProgress')));
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return (
      <section className="page">
        <LoadingState label={t('loadingTodayForm')} />
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
          <h2>{t('addTodaysDropsTitle')}</h2>
          <p className="muted">{t('needActiveList')}</p>
          <Link className="primary-button" to="/dashboard">
            {t('goToDashboard')}
          </Link>
        </article>
      </section>
    );
  }

  const completed = list.status === 'FINISHED';
  const showForm = canAdd || canCorrect;
  const inputMax = correcting ? alreadyToday + list.remaining : list.remaining;

  return (
    <section className="page">
      <article className="card">
        <div className="card-row">
          <div>
            <p className="eyebrow">{t('currentList')}</p>
            <h2>{listTypeLabel(list.listType)}</h2>
          </div>
          <StatusBadge status={list.status} problemDropCount={list.problemDropCount} />
        </div>
        <ProgressBar value={list.progressPercentage} unfinished={unfinishedShare(list.problemDropCount, list.initialDropCount)} />
        <div className="metric-grid">
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
            <p className="eyebrow">{t('today')}</p>
            <p className="metric">{alreadyToday}</p>
          </div>
          <div>
            <p className="eyebrow">{t('progress')}</p>
            <p className="metric">{Math.round(list.progressPercentage)}%</p>
          </div>
        </div>
      </article>

      {completed && !canCorrect ? (
        <article className="card completed-banner">
          <h2>{list.problemDropCount > 0 ? t('statusFinishedWithProblems') : t('listCompleted')}</h2>
          <p>
            {list.problemDropCount > 0
              ? t('finishedWithProblemsDone', { done: list.totalCompleted, problem: list.problemDropCount })
              : list.completionDate
                ? t('listReachedOn', { count: list.initialDropCount, date: list.completionDate })
                : t('listReached', { count: list.initialDropCount })}
          </p>
          <Link className="primary-button" to="/dashboard">
            {t('backToDashboard')}
          </Link>
        </article>
      ) : null}

      {showForm ? (
        <article className="card">
          {completed ? <h2>{t('fixTodayNumber')}</h2> : <h2>{effectiveMode === 'correct' ? t('fixTodayNumber') : t('addDrops')}</h2>}
          {canAdd && canCorrect ? (
            <div className="mode-switch" role="group" aria-label={t('progressMode')}>
              <button type="button" aria-pressed={effectiveMode === 'add'} onClick={() => switchMode('add')}>
                {t('addDrops')}
              </button>
              <button type="button" aria-pressed={effectiveMode === 'correct'} onClick={() => switchMode('correct')}>
                {t('fixToday')}
              </button>
            </div>
          ) : null}
          <p className="muted">
            {effectiveMode === 'correct'
              ? t('alreadySavedToday', { count: alreadyToday })
              : t('alreadyCompletedToday', { count: alreadyToday })}
          </p>
          <form className="form-stack" onSubmit={onSave}>
            <label htmlFor="completedToday">
              {effectiveMode === 'correct' ? t('todayCorrectTotal') : t('dropsToAddNow')}
            </label>
            <input
              id="completedToday"
              type="number"
              min={1}
              max={inputMax}
              inputMode="numeric"
              value={drops}
              onChange={(event) => setDrops(event.target.value)}
              required
            />
            {hasValue ? (
              <p className="muted">
                {effectiveMode === 'correct'
                  ? t('todayWillChange', { from: alreadyToday, to: nextToday })
                  : t('todayWillBecome', { count: nextToday })}
              </p>
            ) : null}
            {formError ? <p className="error-text">{formError}</p> : null}
            {success ? <p className="success-text">{success}</p> : null}
            <button type="submit" className="primary-button" disabled={saving || inputMax < 1}>
              {saving ? t('saving') : effectiveMode === 'correct' ? t('updateToday') : t('saveProgress')}
            </button>
            {list.status === 'IN_PROGRESS' ? (
              <FinishWithProblems
                list={list}
                onFinished={(updated) => {
                  setList(updated);
                  setSuccess(t('finishedWithProblemsDone', { done: updated.totalCompleted, problem: updated.problemDropCount }));
                }}
              />
            ) : null}
          </form>
        </article>
      ) : null}
    </section>
  );
}
