import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { translations, type Language, type TranslationKey } from './translations';

const STORAGE_KEY = 'technicel.language';

type Vars = Record<string, string | number>;

function interpolate(template: string, vars?: Vars) {
  if (!vars) {
    return template;
  }
  return template.replace(/\{\{(\w+)\}\}/g, (_, name: string) => String(vars[name] ?? ''));
}

function readStoredLanguage(): Language {
  const stored = localStorage.getItem(STORAGE_KEY);
  return stored === 'ar' || stored === 'fr' ? stored : 'en';
}

function applyDocumentLanguage(language: Language) {
  document.documentElement.lang = language;
  document.documentElement.dir = language === 'ar' ? 'rtl' : 'ltr';
  document.title = translations[language].appName;
}

interface LanguageContextValue {
  language: Language;
  setLanguage: (language: Language) => void;
  t: (key: TranslationKey, vars?: Vars) => string;
}

const LanguageContext = createContext<LanguageContextValue | null>(null);

export function LanguageProvider({ children }: { children: ReactNode }) {
  const [language, setLanguageState] = useState<Language>(() => {
    const initial = readStoredLanguage();
    applyDocumentLanguage(initial);
    return initial;
  });

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, language);
    applyDocumentLanguage(language);
  }, [language]);

  const value = useMemo<LanguageContextValue>(() => {
    return {
      language,
      setLanguage(next) {
        localStorage.setItem(STORAGE_KEY, next);
        setLanguageState(next);
      },
      t(key, vars) {
        return interpolate(translations[language][key], vars);
      },
    };
  }, [language]);

  return <LanguageContext.Provider value={value}>{children}</LanguageContext.Provider>;
}

export function useLanguage() {
  const context = useContext(LanguageContext);
  if (!context) {
    throw new Error('useLanguage must be used inside LanguageProvider');
  }
  return context;
}

export function listTypeLabel(type: string | null | undefined) {
  if (type === 'OBD_DPO') {
    return 'OBD + DPO';
  }
  return type ?? '';
}

export function translateStatus(status: string, t: LanguageContextValue['t']) {
  switch (status) {
    case 'IN_PROGRESS':
      return t('statusInProgress');
    case 'FINISHED':
      return t('statusFinished');
    case 'FINISHED_WITH_PROBLEMS':
      return t('statusFinishedWithProblems');
    case 'PENDING':
      return t('statusPending');
    case 'APPROVED':
      return t('statusApproved');
    case 'REJECTED':
      return t('statusRejected');
    case 'ADMIN':
      return t('roleAdmin');
    case 'TECHNICIAN':
      return t('roleTechnician');
    default:
      return status.replaceAll('_', ' ');
  }
}
