import { useEffect, useState } from 'react';
import { useLanguage } from '../i18n/LanguageContext';

const DISMISS_KEY = 'technicel.installDismissed';

interface BeforeInstallPromptEvent extends Event {
  prompt: () => Promise<void>;
  userChoice: Promise<{ outcome: 'accepted' | 'dismissed' }>;
}

function isStandalone() {
  const iosNavigator = navigator as Navigator & { standalone?: boolean };
  return window.matchMedia('(display-mode: standalone)').matches || Boolean(iosNavigator.standalone);
}

function isIos() {
  return /iPad|iPhone|iPod/.test(navigator.userAgent);
}

function isAndroid() {
  return /Android/i.test(navigator.userAgent);
}

export function InstallAppBanner() {
  const { t } = useLanguage();
  const [promptEvent, setPromptEvent] = useState<BeforeInstallPromptEvent | null>(null);
  const [dismissed, setDismissed] = useState(() => localStorage.getItem(DISMISS_KEY) === '1');
  const [installed, setInstalled] = useState(isStandalone);

  useEffect(() => {
    function onPrompt(event: Event) {
      event.preventDefault();
      setPromptEvent(event as BeforeInstallPromptEvent);
    }
    function onInstalled() {
      setInstalled(true);
      setPromptEvent(null);
    }
    window.addEventListener('beforeinstallprompt', onPrompt);
    window.addEventListener('appinstalled', onInstalled);
    return () => {
      window.removeEventListener('beforeinstallprompt', onPrompt);
      window.removeEventListener('appinstalled', onInstalled);
    };
  }, []);

  if (installed || dismissed) {
    return null;
  }

  const hint = isIos() ? t('installIosHint') : isAndroid() ? t('installAndroidHint') : t('installGenericHint');

  async function install() {
    if (!promptEvent) {
      return;
    }
    await promptEvent.prompt();
    const choice = await promptEvent.userChoice;
    setPromptEvent(null);
    if (choice.outcome === 'accepted') {
      setInstalled(true);
    }
  }

  function dismiss() {
    localStorage.setItem(DISMISS_KEY, '1');
    setDismissed(true);
  }

  return (
    <aside className="install-banner">
      <strong>{t('installApp')}</strong>
      <p className="muted">{promptEvent ? t('installAppHint') : hint}</p>
      <div className="install-actions">
        {promptEvent ? (
          <button type="button" className="primary-button" onClick={() => void install()}>
            {t('installApp')}
          </button>
        ) : null}
        <button type="button" className="ghost-button" onClick={dismiss}>
          {t('installNotNow')}
        </button>
      </div>
    </aside>
  );
}
