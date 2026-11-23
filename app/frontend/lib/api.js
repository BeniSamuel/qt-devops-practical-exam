// NEXT_PUBLIC_API_URL is inlined into the browser bundle at BUILD time (next build).
// Leave it empty when a reverse proxy serves the frontend and forwards /api/* to Spring Boot.
const BASE = (process.env.NEXT_PUBLIC_API_URL || '').replace(/\/$/, '');

export async function api(path, { method = 'GET', body } = {}) {
  const token = typeof window !== 'undefined' ? localStorage.getItem('token') : null;
  const headers = {};
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  if (token) headers.Authorization = `Bearer ${token}`;

  const res = await fetch(`${BASE}/api${path}`, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  if (res.status === 401 && token) {
    localStorage.removeItem('token');
    window.dispatchEvent(new Event('auth:logout'));
  }
  const data = res.status === 204 ? null : await res.json().catch(() => null);
  if (!res.ok) {
    const fields = data?.fields ? Object.entries(data.fields).map(([k, v]) => `${k}: ${v}`).join('\n') : '';
    throw new Error([data?.error || `Request failed (${res.status})`, fields].filter(Boolean).join('\n'));
  }
  return data;
}

export const MOODS = {
  GREAT: { emoji: '😄', label: 'Great', color: '#2f9e44' },
  GOOD: { emoji: '🙂', label: 'Good', color: '#74b816' },
  OKAY: { emoji: '😐', label: 'Okay', color: '#f59f00' },
  BAD: { emoji: '🙁', label: 'Bad', color: '#f76707' },
  TERRIBLE: { emoji: '😢', label: 'Terrible', color: '#e03131' },
};

export const today = () => {
  const d = new Date();
  return new Date(d.getTime() - d.getTimezoneOffset() * 60000).toISOString().slice(0, 10);
};

export const formatDate = (iso) =>
  new Date(`${iso}T00:00:00`).toLocaleDateString(undefined, { weekday: 'short', year: 'numeric', month: 'short', day: 'numeric' });
