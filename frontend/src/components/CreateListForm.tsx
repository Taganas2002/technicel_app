import { FormEvent, useState } from 'react';
import { apiErrorMessage, todayIso } from '../api/errors';
import { useLanguage } from '../i18n/LanguageContext';
import { createWorkList } from '../services/listService';
import type { ListType, WorkList } from '../types';

export function CreateListForm({
  onCreated,
}: {
  onCreated: (list: WorkList) => void;
}) {
  const { t } = useLanguage();
  const [listType, setListType] = useState<ListType>('OBD');
  const [initialDrops, setInitialDrops] = useState('100');
  const [referenceDate, setReferenceDate] = useState(todayIso());
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setError('');
    setSaving(true);
    try {
      const list = await createWorkList(listType, Number(initialDrops), referenceDate);
      onCreated(list);
    } catch (err) {
      setError(apiErrorMessage(err, t('unableCreateList')));
    } finally {
      setSaving(false);
    }
  }

  return (
    <form className="form-stack" onSubmit={onSubmit}>
      <label htmlFor="listType">{t('listType')}</label>
      <select id="listType" value={listType} onChange={(event) => setListType(event.target.value as ListType)}>
        <option value="OBD">OBD</option>
        <option value="DPO">DPO</option>
        <option value="OBD_DPO">OBD + DPO</option>
      </select>
      <label htmlFor="initialDrops">{t('initialDrops')}</label>
      <input
        id="initialDrops"
        type="number"
        min={1}
        inputMode="numeric"
        value={initialDrops}
        onChange={(event) => setInitialDrops(event.target.value)}
        required
      />
      <label htmlFor="referenceDate">{t('referenceDate')}</label>
      <input
        id="referenceDate"
        type="date"
        value={referenceDate}
        onChange={(event) => setReferenceDate(event.target.value)}
        required
      />
      {error ? <p className="error-text">{error}</p> : null}
      <button type="submit" className="primary-button" disabled={saving}>
        {saving ? t('creating') : t('createList')}
      </button>
    </form>
  );
}
