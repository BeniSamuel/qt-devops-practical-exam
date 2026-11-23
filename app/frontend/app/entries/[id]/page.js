'use client';

import Link from 'next/link';
import { useParams, useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';
import AppShell from '@/components/AppShell';
import EntryForm from '@/components/EntryForm';
import { MOODS, api, formatDate } from '@/lib/api';

function Entry() {
  const { id } = useParams();
  const router = useRouter();
  const [entry, setEntry] = useState(null);
  const [editing, setEditing] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    api(`/entries/${id}`).then(setEntry).catch((e) => setError(e.message));
  }, [id]);

  if (error) return <div className="alert">{error} — <Link href="/entries">back to my entries</Link></div>;
  if (!entry) return <div className="muted">Loading…</div>;

  const save = async (body) => {
    setEntry(await api(`/entries/${id}`, { method: 'PUT', body }));
    setEditing(false);
  };

  const remove = async () => {
    if (!confirm('Delete this entry permanently?')) return;
    try {
      await api(`/entries/${id}`, { method: 'DELETE' });
      router.push('/entries');
    } catch (e) {
      setError(e.message);
    }
  };

  const mood = MOODS[entry.mood];
  return (
    <>
      <Link href="/entries" className="small">← My entries</Link>
      {editing ? (
        <>
          <div className="page-head"><h1>Edit entry</h1><button className="ghost" onClick={() => setEditing(false)}>Cancel</button></div>
          <EntryForm initial={entry} onSubmit={save} submitLabel="Save changes" />
        </>
      ) : (
        <article className="card entry-view" style={{ borderTopColor: mood.color }}>
          <div className="page-head">
            <div>
              <p className="muted">{formatDate(entry.entryDate)} · {mood.emoji} {mood.label}</p>
              <h1>{entry.title}</h1>
            </div>
            <div className="actions">
              <button className="ghost" onClick={() => setEditing(true)}>Edit</button>
              <button className="danger" onClick={remove}>Delete</button>
            </div>
          </div>
          <div className="content">{entry.content}</div>
          {entry.tags.length > 0 && <div className="tags">{entry.tags.map((t) => <span key={t} className="tag">#{t}</span>)}</div>}
          <p className="muted small">Last updated {new Date(entry.updatedAt).toLocaleString()}</p>
        </article>
      )}
    </>
  );
}

export default function Page() {
  return <AppShell><Entry /></AppShell>;
}
