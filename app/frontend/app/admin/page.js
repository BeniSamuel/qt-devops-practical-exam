'use client';

import { useEffect, useState } from 'react';
import AppShell from '@/components/AppShell';
import { useAuth } from '@/components/AuthProvider';
import { api } from '@/lib/api';

function Admin() {
  const { user } = useAuth();
  const [users, setUsers] = useState([]);
  const [error, setError] = useState('');

  const load = () => api('/admin/users').then(setUsers).catch((e) => setError(e.message));
  useEffect(() => { load(); }, []);

  const run = async (fn) => {
    setError('');
    try {
      await fn();
      load();
    } catch (e) {
      setError(e.message);
    }
  };

  return (
    <>
      <h1>User administration</h1>
      <p className="muted">Admins manage accounts but <b>cannot read anyone&apos;s diary</b> — only the number of entries is shown.</p>
      {error && <div className="alert">{error}</div>}
      <div className="card">
        <table className="list">
          <thead><tr><th>Name</th><th>Email</th><th>Joined</th><th>Entries</th><th>Role</th><th></th></tr></thead>
          <tbody>
            {users.map((u) => (
              <tr key={u.id}>
                <td>{u.fullName}</td>
                <td>{u.email}</td>
                <td>{new Date(u.createdAt).toLocaleDateString()}</td>
                <td>{u.entryCount}</td>
                <td>
                  <select value={u.role} disabled={u.id === user.id}
                    onChange={(e) => run(() => api(`/admin/users/${u.id}/role`, { method: 'PATCH', body: { role: e.target.value } }))}>
                    <option value="USER">USER</option>
                    <option value="ADMIN">ADMIN</option>
                  </select>
                </td>
                <td>
                  {u.id !== user.id && (
                    <button className="link danger"
                      onClick={() => confirm(`Delete ${u.email} and all their entries?`) && run(() => api(`/admin/users/${u.id}`, { method: 'DELETE' }))}>
                      Delete
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  );
}

export default function Page() {
  return <AppShell roles={['ADMIN']}><Admin /></AppShell>;
}
