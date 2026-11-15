import { useLanguage } from '../i18n/LanguageContext';

export function LoadingState({ label }: { label?: string }) {
  const { t } = useLanguage();
  return <p className="state">{label ?? t('loading')}</p>;
}
