'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';
import { useAuth } from '@/components/AuthProvider';

export default function RegisterPage() {
  const { user, register } = useAuth();
  const router = useRouter();
  const [form, setForm] = useState({ fullName: '', email: '', password: '' });
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    if (user) router.replace('/');
  }, [user, router]);

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    setBusy(true);
    try {
      await register(form.fullName, form.email, form.password);
    } catch (err) {
      setError(err.message);
      setBusy(false);
    }
  };

  return (
    <div className="auth-page">
      <form className="card auth-card" onSubmit={submit}>
        <h1>Start your diary</h1>
        <p className="muted">Your entries are private — only you can read them.</p>
        {error && <div className="alert">{error}</div>}
        <label>Full name<input value={form.fullName} onChange={set('fullName')} minLength={2} required /></label>
        <label>Email<input type="email" value={form.email} onChange={set('email')} required /></label>
        <label>Password<input type="password" value={form.password} onChange={set('password')} minLength={8} required /></label>
        <button disabled={busy}>{busy ? 'Creating…' : 'Create account'}</button>
        <p className="muted small">Already have an account? <Link href="/login">Sign in</Link></p>
      </form>
    </div>
  );
}
