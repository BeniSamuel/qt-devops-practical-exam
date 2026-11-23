'use client';

import Link from 'next/link';
import { useCallback, useEffect, useState } from 'react';
import AppShell from '@/components/AppShell';
import EntryCard from '@/components/EntryCard';
import { MOODS, api } from '@/lib/api';

const emptyFilters = { q: '', mood: '', tag: '', from: '', to: '' };

function Entries() {
  const [filters, setFilters] = useState(emptyFilters);
  const [applied, setApplied] = useState(emptyFilters);
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [error, setError] = useState('');

  // Allow links such as /entries?tag=work from the dashboard.
  useEffect(() => {
    const tag = new URLSearchParams(window.location.search).get('tag') || '';
    if (tag) {
      setFilters((f) => ({ ...f, tag }));
      setApplied((f) => ({ ...f, tag }));
    }
  }, []);

  const load = useCallback(() => {
    const params = new URLSearchParams({ page, size: 8 });
    Object.entries(applied).forEach(([k, v]) => v && params.set(k, v));
    api(`/entries?${params}`).then(setData).catch((e) => setError(e.message));
  }, [applied, page]);

  useEffect(() => { load(); }, [load]);

  const set = (k) => (e) => setFilters({ ...filters, [k]: e.target.value });
  const search = (e) => {
    e.preventDefault();
    setPage(0);
    setApplied(filters);
  };
  const reset = () => {
    setFilters(emptyFilters);
    setApplied(emptyFilters);
    setPage(0);
  };

  return (
    <>
      <div className="page-head">
        <h1>My entries</h1>
        <Link href="/entries/new" className="button">+ New entry</Link>
      </div>
      <form className="card filters" onSubmit={search}>
        <input placeholder="Search title or text" value={filters.q} onChange={set('q')} />
        <select value={filters.mood} onChange={set('mood')}>
          <option value="">Any mood</option>
          {Object.entries(MOODS).map(([k, m]) => <option key={k} value={k}>{m.emoji} {m.label}</option>)}
        </select>
        <input placeholder="Tag" value={filters.tag} onChange={set('tag')} />
        <input type="date" value={filters.from} onChange={set('from')} title="From" />
        <input type="date" value={filters.to} onChange={set('to')} title="To" />
        <button>Search</button>
        <button type="button" className="ghost" onClick={reset}>Reset</button>
      </form>
      {error && <div className="alert">{error}</div>}
      {!data ? <div className="muted">Loading…</div> : (
        <>
          <p className="muted small">{data.totalItems} entr{data.totalItems === 1 ? 'y' : 'ies'}</p>
          {data.items.map((e) => <EntryCard key={e.id} entry={e} />)}
          {data.totalPages > 1 && (
            <div className="pager">
              <button className="ghost" disabled={page === 0} onClick={() => setPage(page - 1)}>← Newer</button>
              <span className="muted">Page {page + 1} of {data.totalPages}</span>
              <button className="ghost" disabled={page + 1 >= data.totalPages} onClick={() => setPage(page + 1)}>Older →</button>
            </div>
          )}
        </>
      )}
    </>
  );
}

export default function Page() {
  return <AppShell><Entries /></AppShell>;
}
