'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';
import { useAuth } from '@/components/AuthProvider';

export default function LoginPage() {
  const { user, login } = useAuth();
  const router = useRouter();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const next = () => {
    const n = new URLSearchParams(window.location.search).get('next');
    return n && n.startsWith('/') ? n : '/';
  };

  useEffect(() => {
    if (user) router.replace(next());
  }, [user, router]);

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    setBusy(true);
    try {
      await login(email, password);
    } catch (err) {
      setError(err.message);
      setBusy(false);
    }
  };

  return (
    <div className="auth-page">
      <form className="card auth-card" onSubmit={submit}>
        <h1>📔 MyDiary</h1>
        <p className="muted">Sign in to write today&apos;s entry</p>
        {error && <div className="alert">{error}</div>}
        <label>Email<input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required /></label>
        <label>Password<input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required /></label>
        <button disabled={busy}>{busy ? 'Signing in…' : 'Sign in'}</button>
        <p className="muted small">New here? <Link href="/register">Create an account</Link></p>
      </form>
    </div>
  );
}
