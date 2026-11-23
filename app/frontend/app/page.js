'use client';

import Link from 'next/link';
import { useEffect, useState } from 'react';
import AppShell from '@/components/AppShell';
import { useAuth } from '@/components/AuthProvider';
import EntryCard from '@/components/EntryCard';
import { MOODS, api, today } from '@/lib/api';

function Dashboard() {
  const { user } = useAuth();
  const [stats, setStats] = useState(null);
  const [recent, setRecent] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    Promise.all([api('/entries/stats'), api('/entries?size=4')])
      .then(([s, r]) => {
        setStats(s);
        setRecent(r.items);
      })
      .catch((e) => setError(e.message));
  }, []);

  if (error) return <div className="alert">{error}</div>;
  if (!stats) return <div className="muted">Loading…</div>;

  const totalMoods = Object.values(stats.moodBreakdown).reduce((a, b) => a + b, 0) || 1;
  const wroteToday = stats.lastEntryDate === today();

  return (
    <>
      <div className="page-head">
        <div>
          <h1>Hello, {user.fullName.split(' ')[0]} 👋</h1>
          <p className="muted">{wroteToday ? 'You already wrote today. Nice!' : "You haven't written today yet."}</p>
        </div>
        <Link href="/entries/new" className="button">+ New entry</Link>
      </div>

      <div className="stats">
        <Stat label="Total entries" value={stats.totalEntries} />
        <Stat label="This month" value={stats.entriesThisMonth} />
        <Stat label="Current streak" value={`${stats.currentStreak} 🔥`} />
        <Stat label="Longest streak" value={stats.longestStreak} />
      </div>

      <div className="two">
        <div className="card">
          <h2>Mood breakdown</h2>
          {Object.entries(MOODS).map(([key, m]) => (
            <div key={key} className="mood-bar">
              <span>{m.emoji} {m.label}</span>
              <div className="bar"><div style={{ width: `${(stats.moodBreakdown[key] / totalMoods) * 100}%`, background: m.color }} /></div>
              <span className="muted small">{stats.moodBreakdown[key]}</span>
            </div>
          ))}
        </div>
        <div className="card">
          <h2>Top tags</h2>
          {stats.topTags.length === 0 ? <p className="muted">No tags yet.</p> : (
            <div className="tags">
              {stats.topTags.map((t) => (
                <Link key={t.tag} href={`/entries?tag=${encodeURIComponent(t.tag)}`} className="tag">#{t.tag} · {t.count}</Link>
              ))}
            </div>
          )}
        </div>
      </div>

      <h2>Recent entries</h2>
      {recent.length === 0 ? <p className="muted">No entries yet — <Link href="/entries/new">write your first one</Link>.</p> : (
        recent.map((e) => <EntryCard key={e.id} entry={e} />)
      )}
    </>
  );
}

function Stat({ label, value }) {
  return (
    <div className="card stat">
      <div className="stat-value">{value}</div>
      <div className="muted">{label}</div>
    </div>
  );
}

export default function Page() {
  return <AppShell><Dashboard /></AppShell>;
}
