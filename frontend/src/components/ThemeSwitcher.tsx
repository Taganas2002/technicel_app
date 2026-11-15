import { useLanguage } from '../i18n/LanguageContext';
import { useTheme } from '../theme/ThemeContext';

export function ThemeSwitcher() {
  const { theme, toggleTheme } = useTheme();
  const { t } = useLanguage();
  const dark = theme === 'dark';

  return (
    <button
      type="button"
      className="ghost-button theme-switcher"
      aria-pressed={dark}
      onClick={toggleTheme}
    >
      {dark ? t('lightMode') : t('darkMode')}
    </button>
  );
}
