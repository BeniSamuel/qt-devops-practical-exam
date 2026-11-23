import Link from 'next/link';
import { MOODS, formatDate } from '@/lib/api';

export default function EntryCard({ entry }) {
  const mood = MOODS[entry.mood];
  return (
    <Link href={`/entries/${entry.id}`} className="card entry-card" style={{ borderLeftColor: mood.color }}>
      <div className="row">
        <h3>{mood.emoji} {entry.title}</h3>
        <span className="muted small">{formatDate(entry.entryDate)}</span>
      </div>
      <p className="muted clamp">{entry.content}</p>
      {entry.tags.length > 0 && (
        <div className="tags">{entry.tags.map((t) => <span key={t} className="tag">#{t}</span>)}</div>
      )}
    </Link>
  );
}
