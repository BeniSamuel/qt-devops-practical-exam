'use client';

import { useState } from 'react';
import { MOODS, today } from '@/lib/api';

export default function EntryForm({ initial, onSubmit, submitLabel = 'Save entry' }) {
  const [form, setForm] = useState({
    title: initial?.title ?? '',
    content: initial?.content ?? '',
    mood: initial?.mood ?? 'GOOD',
    tags: (initial?.tags ?? []).join(', '),
    entryDate: initial?.entryDate ?? today(),
  });
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    setBusy(true);
    try {
      await onSubmit({
        ...form,
        tags: form.tags.split(',').map((t) => t.trim()).filter(Boolean),
      });
    } catch (err) {
      setError(err.message);
      setBusy(false);
    }
  };

  return (
    <form className="card form" onSubmit={submit}>
      {error && <div className="alert">{error}</div>}
      <label>Title<input value={form.title} onChange={set('title')} maxLength={200} required /></label>
      <div className="two">
        <label>Date<input type="date" value={form.entryDate} max={today()} onChange={set('entryDate')} required /></label>
        <label>Tags <span className="muted small">(comma separated)</span><input value={form.tags} onChange={set('tags')} placeholder="work, family" /></label>
      </div>
      <div className="field-label">How did you feel?</div>
      <div className="moods">
        {Object.entries(MOODS).map(([key, m]) => (
          <button type="button" key={key} className={`mood ${form.mood === key ? 'selected' : ''}`} onClick={() => setForm({ ...form, mood: key })}>
            <span className="emoji">{m.emoji}</span>{m.label}
          </button>
        ))}
      </div>
      <label>Dear diary…<textarea value={form.content} onChange={set('content')} rows={10} required /></label>
      <button disabled={busy}>{busy ? 'Saving…' : submitLabel}</button>
    </form>
  );
}
