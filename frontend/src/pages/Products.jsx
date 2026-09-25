import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import api, { errorMessage } from '../api/axios';
import ProductCard from '../components/ProductCard';

const SORTS = [
  { value: 'id,desc', label: 'Newest' },
  { value: 'price,asc', label: 'Price: low to high' },
  { value: 'price,desc', label: 'Price: high to low' },
  { value: 'name,asc', label: 'Name: A to Z' },
];

export default function Products() {
  const [params, setParams] = useSearchParams();
  const search = params.get('search') || '';
  const categoryId = params.get('categoryId') || '';
  const minPrice = params.get('minPrice') || '';
  const maxPrice = params.get('maxPrice') || '';
  const sort = params.get('sort') || 'id,desc';
  const page = Number(params.get('page') || 0);

  const [form, setForm] = useState({ search, minPrice, maxPrice });
  const [products, setProducts] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [total, setTotal] = useState(0);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => setForm({ search, minPrice, maxPrice }), [search, minPrice, maxPrice]);

  useEffect(() => {
    api.get('/categories').then((r) => setCategories(r.data)).catch(() => {});
  }, []);

  useEffect(() => {
    let ignore = false;
    setLoading(true);
    setError('');
    api
      .get('/products', {
        params: {
          page, size: 12, sort,
          search: search || undefined,
          categoryId: categoryId || undefined,
          minPrice: minPrice || undefined,
          maxPrice: maxPrice || undefined,
        },
      })
      .then((r) => {
        if (ignore) return;
        setProducts(r.data.content);
        setTotalPages(r.data.page.totalPages);
        setTotal(r.data.page.totalElements);
      })
      .catch((e) => !ignore && setError(errorMessage(e)))
      .finally(() => !ignore && setLoading(false));
    return () => { ignore = true; };
  }, [search, categoryId, minPrice, maxPrice, sort, page]);

  const update = (changes) => {
    const next = new URLSearchParams(params);
    Object.entries(changes).forEach(([k, v]) =>
      v === '' || v == null ? next.delete(k) : next.set(k, v)
    );
    if (!('page' in changes)) next.delete('page');
    setParams(next);
  };

  const applyFilters = (e) => {
    e.preventDefault();
    if (form.minPrice && form.maxPrice && Number(form.minPrice) > Number(form.maxPrice)) {
      setError('Min price cannot be greater than max price');
      return;
    }
    update({ search: form.search.trim(), minPrice: form.minPrice, maxPrice: form.maxPrice });
  };

  const hasFilters = search || categoryId || minPrice || maxPrice;
  const field = (name) => (e) => setForm({ ...form, [name]: e.target.value });

  return (
    <>
      <h1>Products</h1>

      <div className="filters">
        <form onSubmit={applyFilters}>
          <input placeholder="Search products..." value={form.search} onChange={field('search')} />
          <input type="number" min="0" placeholder="Min ₹" value={form.minPrice}
                 onChange={field('minPrice')} style={{ width: 90 }} />
          <input type="number" min="0" placeholder="Max ₹" value={form.maxPrice}
                 onChange={field('maxPrice')} style={{ width: 90 }} />
          <button className="btn">Apply</button>
        </form>

        <select value={categoryId} onChange={(e) => update({ categoryId: e.target.value })}>
          <option value="">All categories</option>
          {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>

        <select value={sort} onChange={(e) => update({ sort: e.target.value })}>
          {SORTS.map((s) => <option key={s.value} value={s.value}>{s.label}</option>)}
        </select>

        {hasFilters && (
          <button className="btn btn-outline" onClick={() => setParams({})}>Clear filters</button>
        )}
      </div>

      {error && <p className="error">{error}</p>}
      {loading && <p>Loading...</p>}
      {!loading && !error && (
        <p><small>{total} {total === 1 ? 'product' : 'products'} found</small></p>
      )}
      {!loading && !error && products.length === 0 && (
        <p>No products match your filters. Try a different search or clear the filters.</p>
      )}

      <div className="grid">
        {products.map((p) => <ProductCard key={p.id} product={p} />)}
      </div>

      {totalPages > 1 && (
        <div className="pagination">
          <button className="btn btn-outline" disabled={page === 0}
                  onClick={() => update({ page: page - 1 })}>Prev</button>
          <span>Page {page + 1} of {totalPages}</span>
          <button className="btn btn-outline" disabled={page + 1 >= totalPages}
                  onClick={() => update({ page: page + 1 })}>Next</button>
        </div>
      )}
    </>
  );
}
