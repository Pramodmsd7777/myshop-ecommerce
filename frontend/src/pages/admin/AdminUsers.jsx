import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import api, { errorMessage } from '../../api/axios';
import { formatDate } from '../../utils';
import Pager from '../../components/Pager';

export default function AdminUsers() {
  const [params, setParams] = useSearchParams();
  const search = params.get('search') || '';
  const page = Number(params.get('page') || 0);

  const [text, setText] = useState(search);
  const [users, setUsers] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let ignore = false;
    setLoading(true);
    setError('');
    api.get('/admin/users', { params: { search: search || undefined, page, size: 10 } })
      .then((r) => {
        if (ignore) return;
        setUsers(r.data.content);
        setTotalPages(r.data.page.totalPages);
      })
      .catch((e) => !ignore && setError(errorMessage(e)))
      .finally(() => !ignore && setLoading(false));
    return () => { ignore = true; };
  }, [search, page]);

  const goTo = (p) => { const n = new URLSearchParams(params); n.set('page', p); setParams(n); };

  return (
    <>
      <h1>Users</h1>
      <form className="filters" onSubmit={(e) => { e.preventDefault(); setParams(text.trim() ? { search: text.trim() } : {}); }}>
        <input placeholder="Search by name or email..." value={text} onChange={(e) => setText(e.target.value)} />
        <button className="btn">Search</button>
      </form>

      {error && <p className="error">{error}</p>}
      {loading && <p>Loading...</p>}
      {!loading && !error && users.length === 0 && <p>No users found.</p>}

      {users.length > 0 && (
        <div className="table-wrap">
          <table className="table">
            <thead><tr><th>ID</th><th>Name</th><th>Email</th><th>Role</th><th>Joined</th></tr></thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.id}>
                  <td>{u.id}</td><td>{u.name}</td><td>{u.email}</td>
                  <td>{u.role.toLowerCase()}</td><td>{formatDate(u.createdAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      <Pager page={page} totalPages={totalPages} onChange={goTo} />
    </>
  );
}
