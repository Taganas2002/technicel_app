import { useLanguage } from '../i18n/LanguageContext';
import type { Language } from '../i18n/translations';

const options: { code: Language; short: string; labelKey: 'english' | 'french' | 'arabic' }[] = [
  { code: 'en', short: 'EN', labelKey: 'english' },
  { code: 'fr', short: 'FR', labelKey: 'french' },
  { code: 'ar', short: 'ع', labelKey: 'arabic' },
];

export function LanguageSwitcher({
  compact = false,
  variant = 'compact',
}: {
  compact?: boolean;
  variant?: 'compact' | 'bar';
}) {
  const { language, setLanguage, t } = useLanguage();
  const bar = variant === 'bar';

  return (
    <div
      className={`language-switcher${compact || bar ? ' compact' : ''}${bar ? ' bar' : ''}`}
      role="group"
      aria-label={t('language')}
    >
      {options.map((option) => (
        <button
          key={option.code}
          type="button"
          aria-pressed={language === option.code}
          onClick={() => setLanguage(option.code)}
        >
          {bar ? t(option.labelKey) : option.short}
        </button>
      ))}
    </div>
  );
}
