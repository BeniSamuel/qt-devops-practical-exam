'use client';

import { useRouter } from 'next/navigation';
import AppShell from '@/components/AppShell';
import EntryForm from '@/components/EntryForm';
import { api } from '@/lib/api';

export default function NewEntryPage() {
  const router = useRouter();
  const create = async (body) => {
    const entry = await api('/entries', { method: 'POST', body });
    router.push(`/entries/${entry.id}`);
  };
  return (
    <AppShell>
      <h1>New entry</h1>
      <EntryForm onSubmit={create} submitLabel="Save entry" />
    </AppShell>
  );
}
