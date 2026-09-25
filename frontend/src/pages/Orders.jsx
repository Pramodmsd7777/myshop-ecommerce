import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api, { errorMessage } from '../api/axios';
import { formatPrice, formatDate } from '../utils';

export default function Orders() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/orders')
      .then((r) => setOrders(r.data))
      .catch((e) => setError(errorMessage(e)))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <p>Loading...</p>;

  return (
    <>
      <h1>Your orders</h1>
      {error && <p className="error">{error}</p>}
      {!error && orders.length === 0 && (
        <>
          <p>You haven't placed any orders yet.</p>
          <Link to="/products" className="btn">Browse products</Link>
        </>
      )}

      {orders.map((o) => (
        <Link to={`/orders/${o.id}`} className="order-card" key={o.id}>
          <div>
            <strong>Order #{o.id}</strong>
            <small>{formatDate(o.createdAt)}</small>
          </div>
          <span>{o.totalItems} {o.totalItems === 1 ? 'item' : 'items'}</span>
          <span className={`status status-${o.status.toLowerCase()}`}>{o.status.toLowerCase()}</span>
          <strong>{formatPrice(o.totalAmount)}</strong>
        </Link>
      ))}
    </>
  );
}
