import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import api, { errorMessage } from '../../api/axios';
import { formatPrice } from '../../utils';
import Pager from '../../components/Pager';

const EMPTY = { name: '', description: '', price: '', stock: 0, imageUrl: '', categoryId: '' };

export default function AdminProducts() {
  const [params, setParams] = useSearchParams();
  const search = params.get('search') || '';
  const page = Number(params.get('page') || 0);

  const [text, setText] = useState(search);
  const [products, setProducts] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [categories, setCategories] = useState([]);
  const [reload, setReload] = useState(0);
  const [form, setForm] = useState(null);
  const [saving, setSaving] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [formError, setFormError] = useState('');

  useEffect(() => {
    api.get('/categories').then((r) => setCategories(r.data)).catch(() => {});
  }, []);

  useEffect(() => {
    let ignore = false;
    setLoading(true);
    api.get('/products', { params: { page, size: 10, search: search || undefined } })
      .then((r) => {
        if (ignore) return;
        setProducts(r.data.content);
        setTotalPages(r.data.page.totalPages);
      })
      .catch((e) => !ignore && setError(errorMessage(e)))
      .finally(() => !ignore && setLoading(false));
    return () => { ignore = true; };
  }, [search, page, reload]);

  const openForm = (p) => {
    setFormError('');
    setForm(p
      ? { ...p, description: p.description ?? '', imageUrl: p.imageUrl ?? '', categoryId: p.categoryId ?? '' }
      : { ...EMPTY });
  };
  const change = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const save = async (e) => {
    e.preventDefault();
    setFormError('');
    setSaving(true);
    const body = {
      name: form.name, description: form.description, imageUrl: form.imageUrl,
      price: Number(form.price), stock: Number(form.stock), categoryId: Number(form.categoryId),
    };
    try {
      if (form.id) await api.put(`/products/${form.id}`, body);
      else await api.post('/products', body);
      setForm(null);
      setReload((n) => n + 1);
    } catch (err) {
      setFormError(errorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  const remove = async (p) => {
    if (!window.confirm(`Delete "${p.name}"?`)) return;
    setError('');
    try {
      await api.delete(`/products/${p.id}`);
      setReload((n) => n + 1);
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  const goTo = (p) => { const n = new URLSearchParams(params); n.set('page', p); setParams(n); };

  return (
    <>
      <div className="admin-head">
        <h1>Products</h1>
        <button className="btn" onClick={() => openForm(null)}>Add product</button>
      </div>

      {form && (
        <form className="admin-form" onSubmit={save}>
          <h2 className="full">{form.id ? `Edit product #${form.id}` : 'New product'}</h2>
          {formError && <p className="error full">{formError}</p>}
          <label className="full">Name
            <input name="name" value={form.name} onChange={change} required maxLength={200} />
          </label>
          <label className="full">Description
            <textarea name="description" value={form.description} onChange={change} maxLength={2000} />
          </label>
          <label>Price (₹)
            <input name="price" type="number" step="0.01" min="0.01" value={form.price} onChange={change} required />
          </label>
          <label>Stock
            <input name="stock" type="number" min="0" value={form.stock} onChange={change} required />
          </label>
          <label>Category
            <select name="categoryId" value={form.categoryId} onChange={change} required>
              <option value="">Select a category</option>
              {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
          </label>
          <label>Image URL
            <input name="imageUrl" value={form.imageUrl} onChange={change} />
          </label>
          <div className="actions full">
            <button className="btn" disabled={saving}>{saving ? 'Saving...' : 'Save product'}</button>
            <button type="button" className="btn btn-outline" onClick={() => setForm(null)}>Cancel</button>
          </div>
        </form>
      )}

      <form className="filters" onSubmit={(e) => { e.preventDefault(); setParams(text.trim() ? { search: text.trim() } : {}); }}>
        <input placeholder="Search products..." value={text} onChange={(e) => setText(e.target.value)} />
        <button className="btn">Search</button>
      </form>

      {error && <p className="error">{error}</p>}
      {loading && <p>Loading...</p>}
      {!loading && products.length === 0 && <p>No products found.</p>}

      {products.length > 0 && (
        <div className="table-wrap">
          <table className="table">
            <thead><tr><th>ID</th><th>Name</th><th>Category</th><th>Price</th><th>Stock</th><th></th></tr></thead>
            <tbody>
              {products.map((p) => (
                <tr key={p.id}>
                  <td>{p.id}</td>
                  <td>{p.name}</td>
                  <td>{p.categoryName}</td>
                  <td>{formatPrice(p.price)}</td>
                  <td>{p.stock}</td>
                  <td className="actions">
                    <button className="btn btn-outline btn-sm" onClick={() => openForm(p)}>Edit</button>
                    <button className="btn btn-outline btn-sm" onClick={() => remove(p)}>Delete</button>
                  </td>
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
