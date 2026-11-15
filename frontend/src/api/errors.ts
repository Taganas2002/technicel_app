import { isAxiosError } from 'axios';

export function isNotFound(error: unknown) {
  return isAxiosError(error) && error.response?.status === 404;
}

export function apiErrorMessage(error: unknown, fallback = 'Something went wrong. Please try again.') {
  if (isAxiosError(error)) {
    const message = error.response?.data?.message;
    if (typeof message === 'string' && message.trim()) {
      return message;
    }
    if (!error.response) {
      return 'Unable to connect. Please try again.';
    }
  }
  return fallback;
}

export function todayIso() {
  const now = new Date();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  return `${now.getFullYear()}-${month}-${day}`;
}