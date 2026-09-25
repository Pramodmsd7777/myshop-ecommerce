import axios from 'axios';

const api = axios.create({ baseURL: (import.meta.env.VITE_API_URL || '') + '/api' });

// attach the JWT to every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// an expired token on a protected call means: log out and go to /login
api.interceptors.response.use(
  (res) => res,
  (err) => {
    const url = err.config?.url || '';
    if (err.response?.status === 401 && !url.startsWith('/auth')) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      if (window.location.pathname !== '/login') window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);

// turns the backend's error shapes into one readable string:
// {error: "..."} or {fieldName: "message", ...} (validation)
export function errorMessage(err) {
  const data = err.response?.data;
  if (!data) return 'Cannot reach the server. Is the backend running?';
  if (data.error) return data.error;
  if (typeof data === 'object') return Object.values(data).join(', ');
  return 'Something went wrong';
}

export default api;
