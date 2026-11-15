import { translateStatus, useLanguage } from '../i18n/LanguageContext';

export function StatusBadge({ status, problemDropCount = 0 }: { status: string; problemDropCount?: number }) {
  const { t } = useLanguage();
  const shown = status === 'FINISHED' && problemDropCount > 0 ? 'FINISHED_WITH_PROBLEMS' : status;
  const tone = shown.toLowerCase().replaceAll('_', '-');
  return <span className={`badge badge-${tone}`}>{translateStatus(shown, t)}</span>;
}
