import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api, { errorMessage } from '../api/axios';
import ProductCard from '../components/ProductCard';

// Always returns an array, even if the server sends an object, HTML or nothing
const toArray = (value) => (Array.isArray(value) ? value : []);

export default function Home() {
  const [latest, setLatest] = useState([]);
  const [deals, setDeals] = useState([]);
  const [categories, setCategories] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      api.get('/products', { params: { size: 4, sort: 'id,desc' } }),
      api.get('/products', { params: { size: 4, sort: 'price,asc' } }),
      api.get('/categories'),
    ])
      .then(([latestRes, dealsRes, catRes]) => {
        setLatest(toArray(latestRes.data?.content));
        setDeals(toArray(dealsRes.data?.content));
        setCategories(toArray(catRes.data));
      })
      .catch((e) => setError(errorMessage(e)))
      .finally(() => setLoading(false));
  }, []);

  return (
    <>
      <section className="hero">
        <div>
          <h1>Welcome to MyShop</h1>
          <p>Everything you need, from ₹99.</p>
          <Link to="/products" className="btn">Shop now</Link>
        </div>
      </section>

      {error && <p className="error">{error}</p>}
      {loading && <p>Loading...</p>}

      {categories.length > 0 && (
        <section>
          <h2>Shop by category</h2>
          <div className="cats">
            {categories.map((c) => (
              <Link key={c.id} to={`/products?categoryId=${c.id}`} className="cat">
                <i>{c.name?.charAt(0)}</i>
                {c.name}
              </Link>
            ))}
          </div>
        </section>
      )}

      {deals.length > 0 && (
        <section>
          <h2>Great value picks</h2>
          <div className="grid">
            {deals.map((p) => <ProductCard key={p.id} product={p} />)}
          </div>
        </section>
      )}

      {latest.length > 0 && (
        <section>
          <h2>New arrivals</h2>
          <div className="grid">
            {latest.map((p) => <ProductCard key={p.id} product={p} />)}
          </div>
        </section>
      )}
    </>
  );
}

