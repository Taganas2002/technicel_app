import { useCallback, useEffect, useState } from 'react';
import { apiErrorMessage, isNotFound } from '../api/errors';
import { useLanguage } from '../i18n/LanguageContext';
import { getActiveList, getListProgress } from '../services/listService';
import type { DailyProgress, WorkList } from '../types';

export function useActiveList() {
  const { t } = useLanguage();
  const [list, setList] = useState<WorkList | null>(null);
  const [progress, setProgress] = useState<DailyProgress[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const reload = useCallback(async () => {
    setError('');
    try {
      const active = await getActiveList();
      const entries = await getListProgress(active.id);
      setList(active);
      setProgress(entries);
    } catch (err) {
      setList(null);
      setProgress([]);
      if (!isNotFound(err)) {
        setError(apiErrorMessage(err, t('unableLoadCurrentList')));
      }
    } finally {
      setLoading(false);
    }
  }, [t]);

  useEffect(() => {
    void reload();
  }, [reload]);

  return { list, progress, loading, error, reload, setList, setProgress };
}
