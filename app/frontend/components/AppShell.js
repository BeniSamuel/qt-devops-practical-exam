'use client';

import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { useEffect } from 'react';
import { useAuth } from './AuthProvider';

/** Navigation + client-side route guard. The API enforces the same rules server-side. */
export default function AppShell({ children, roles }) {
  const { user, loading, logout } = useAuth();
  const router = useRouter();
  const pathname = usePathname();

  useEffect(() => {
    if (!loading && !user) router.replace(`/login?next=${encodeURIComponent(pathname)}`);
  }, [loading, user, router, pathname]);

  if (loading || !user) return <div className="center muted">Loading…</div>;

  const link = (href, label) => (
    <Link href={href} className={pathname === href || (href !== '/' && pathname.startsWith(href)) ? 'active' : ''}>{label}</Link>
  );

  return (
    <>
      <header className="topbar">
        <div className="brand">📔 MyDiary</div>
        <nav>
          {link('/', 'Dashboard')}
          {link('/entries', 'My entries')}
          {user.role === 'ADMIN' && link('/admin', 'Admin')}
        </nav>
        <div className="who">
          <span>{user.fullName}</span>
          <span className={`badge role-${user.role}`}>{user.role}</span>
          <button className="ghost" onClick={logout}>Log out</button>
        </div>
      </header>
      <main className="container">
        {roles && !roles.includes(user.role) ? (
          <div className="card"><h2>403 — Forbidden</h2><p>Your role ({user.role}) cannot open this page.</p></div>
        ) : (
          children
        )}
      </main>
    </>
  );
}
