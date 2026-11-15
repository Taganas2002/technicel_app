import type { ReactNode } from 'react';
import { useLanguage } from '../i18n/LanguageContext';

export function ConfirmDialog({
  open,
  title,
  message,
  confirmLabel,
  disabled = false,
  onConfirm,
  onCancel,
  children,
}: {
  open: boolean;
  title: string;
  message: string;
  confirmLabel?: string;
  disabled?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
  children?: ReactNode;
}) {
  const { t } = useLanguage();
  if (!open) {
    return null;
  }

  return (
    <div className="dialog-backdrop">
      <div className="dialog" role="dialog" aria-modal="true">
        <h2>{title}</h2>
        <p>{message}</p>
        {children}
        <div className="dialog-actions">
          <button type="button" className="ghost-button" onClick={onCancel}>
            {t('cancel')}
          </button>
          <button type="button" className="primary-button" onClick={onConfirm} disabled={disabled}>
            {confirmLabel ?? t('approve')}
          </button>
        </div>
      </div>
    </div>
  );
}
