import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { listTypeLabel, useLanguage } from '../i18n/LanguageContext';
import type { TranslationKey } from '../i18n/translations';
import {
  getAdminNotifications,
  markAllNotificationsRead,
  markNotificationRead,
} from '../services/notificationService';
import type { AdminNotification } from '../types';

const POLL_MS = 15000;

function notificationCopy(
  item: AdminNotification,
  t: (key: TranslationKey, vars?: Record<string, string | number>) => string,
) {
  if (item.type === 'LIST_CREATED') {
    return {
      title: t('notifyListCreatedTitle'),
      message: t('notifyListCreatedMessage', {
        name: item.technicianName ?? item.technicianCode ?? '',
        type: listTypeLabel(item.listType),
      }),
    };
  }
  return { title: item.title, message: item.message };
}

function notifyBrowser(title: string, body: string) {
  if (typeof Notification === 'undefined' || Notification.permission !== 'granted') {
    return;
  }
  try {
    new Notification(title, { body });
  } catch {
    // Browser may block Notification construction outside a user gesture.
  }
}

export function NotificationBell() {
  const { t } = useLanguage();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);
  const [items, setItems] = useState<AdminNotification[]>([]);
  const knownIds = useRef<Set<number> | null>(null);
  const rootRef = useRef<HTMLDivElement>(null);

  async function refresh(announceNew: boolean) {
    const data = await getAdminNotifications();
    const nextIds = new Set(data.notifications.map((item) => item.id));
    if (announceNew && knownIds.current) {
      data.notifications
        .filter((item) => !item.read && !knownIds.current?.has(item.id))
        .forEach((item) => {
          const copy = notificationCopy(item, t);
          notifyBrowser(copy.title, copy.message);
        });
    }
    knownIds.current = nextIds;
    setUnreadCount(data.unreadCount);
    setItems(data.notifications);
  }

  useEffect(() => {
    let cancelled = false;
    refresh(false).catch(() => {
      if (!cancelled) {
        setItems([]);
      }
    });
    const timer = window.setInterval(() => {
      refresh(true).catch(() => undefined);
    }, POLL_MS);
    return () => {
      cancelled = true;
      window.clearInterval(timer);
    };
  }, [t]);

  useEffect(() => {
    function onPointerDown(event: MouseEvent) {
      if (rootRef.current && !rootRef.current.contains(event.target as Node)) {
        setOpen(false);
      }
    }
    document.addEventListener('mousedown', onPointerDown);
    return () => document.removeEventListener('mousedown', onPointerDown);
  }, []);

  async function onEnableAlerts() {
    if (typeof Notification === 'undefined') {
      return;
    }
    await Notification.requestPermission();
  }

  async function onOpenItem(item: AdminNotification) {
    if (!item.read) {
      try {
        await markNotificationRead(item.id);
        setItems((current) => current.map((entry) => (entry.id === item.id ? { ...entry, read: true } : entry)));
        setUnreadCount((count) => Math.max(0, count - 1));
      } catch {
        // Keep the panel usable even if mark-read fails.
      }
    }
    setOpen(false);
    if (item.technicianId) {
      navigate(`/admin/technicians/${item.technicianId}`);
    }
  }

  async function onMarkAll() {
    const data = await markAllNotificationsRead();
    knownIds.current = new Set(data.notifications.map((item) => item.id));
    setUnreadCount(data.unreadCount);
    setItems(data.notifications);
  }

  const canAskPermission = typeof Notification !== 'undefined' && Notification.permission === 'default';

  return (
    <div className="notification-bell" ref={rootRef}>
      <button
        type="button"
        className="ghost-button notification-trigger"
        aria-expanded={open}
        aria-label={unreadCount > 0 ? t('notificationsUnread', { count: unreadCount }) : t('notifications')}
        onClick={() => {
          setOpen((current) => {
            if (!current) {
              void refresh(true);
            }
            return !current;
          });
        }}
      >
        {t('alerts')}
        {unreadCount > 0 ? <span className="notification-badge">{unreadCount > 9 ? '9+' : unreadCount}</span> : null}
      </button>
      {open ? (
        <div className="notification-panel" role="dialog" aria-label={t('adminNotifications')}>
          <div className="notification-panel-header">
            <h2>{t('notifications')}</h2>
            {unreadCount > 0 ? (
              <button type="button" className="ghost-button" onClick={() => void onMarkAll()}>
                {t('markAllRead')}
              </button>
            ) : null}
          </div>
          {canAskPermission ? (
            <button type="button" className="ghost-button" onClick={() => void onEnableAlerts()}>
              {t('enableDesktopAlerts')}
            </button>
          ) : null}
          {items.length === 0 ? <p className="muted">{t('noNotifications')}</p> : null}
          <ul className="notification-list">
            {items.map((item) => {
              const copy = notificationCopy(item, t);
              return (
                <li key={item.id}>
                  <button type="button" className={item.read ? 'notification-item' : 'notification-item unread'} onClick={() => void onOpenItem(item)}>
                    <strong>{copy.title}</strong>
                    <span>{copy.message}</span>
                  </button>
                </li>
              );
            })}
          </ul>
        </div>
      ) : null}
    </div>
  );
}
