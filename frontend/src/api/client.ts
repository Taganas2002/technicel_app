import axios from 'axios';

export const TOKEN_KEY = 'technicel.token';
export const USER_KEY = 'technicel.user';

function apiBaseUrl() {
  const configured = (import.meta.env.VITE_API_BASE_URL as string | undefined)?.trim();
  if (import.meta.env.DEV) {
    return configured || 'http://localhost:8080';
  }
  if (configured && !/localhost|127\.0\.0\.1/.test(configured)) {
    return configured;
  }
  return '';
}

export const api = axios.create({
  baseURL: apiBaseUrl(),
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = sessionStorage.getItem(TOKEN_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      sessionStorage.removeItem(TOKEN_KEY);
      sessionStorage.removeItem(USER_KEY);
      if (window.location.pathname !== '/login') {
        window.location.assign('/login');
      }
    }
    return Promise.reject(error);
  },
);
