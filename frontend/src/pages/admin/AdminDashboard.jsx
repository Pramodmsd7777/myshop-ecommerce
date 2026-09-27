import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api, { errorMessage } from '../../api/axios';
import { formatPrice } from '../../utils';

export default function AdminDashboard() {
  const [stats, setStats] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/admin/stats')
      .then((r) => setStats(r.data))
      .catch((e) => setError(errorMessage(e)));
  }, []);

  if (error) return <p className="error">{error}</p>;
  if (!stats) return <p>Loading...</p>;

  const tiles = [
    ['Total users', stats.totalUsers],
    ['Total products', stats.totalProducts],
    ['Total orders', stats.totalOrders],
    ['Total revenue', formatPrice(stats.totalRevenue)],
  ];

  return (
    <>
      <h1>Dashboard</h1>
      <div className="stats">
        {tiles.map(([label, value]) => (
          <div className="stat" key={label}><small>{label}</small><strong>{value}</strong></div>
        ))}
      </div>

      <h2>Orders by status </h2>
      <div className="chips">
        {Object.entries(stats.ordersByStatus).map(([s, n]) => (
          <Link key={s} to={`/admin/orders?status=${s}`} className="chip">{s.toLowerCase()}: {n}</Link>
        ))}
      </div>
      <small>Revenue counts paid, shipped and delivered orders. Pending and cancelled orders are left out.</small>
    </>
  );
}
