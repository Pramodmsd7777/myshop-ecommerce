import { Fragment, useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import api, { errorMessage } from '../../api/axios';
import { formatPrice, formatDate } from '../../utils';
import Pager from '../../components/Pager';

const STATUSES = ['PENDING', 'PAID', 'SHIPPED', 'DELIVERED', 'CANCELLED'];

export default function AdminOrders() {
  const [params, setParams] = useSearchParams();
  const status = params.get('status') || '';
  const page = Number(params.get('page') || 0);

  const [orders, setOrders] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [openId, setOpenId] = useState(null);
  const [details, setDetails] = useState({});
  const [busyId, setBusyId] = useState(null);

  useEffect(() => {
    let ignore = false;
    setLoading(true);
    setError('');
    api.get('/admin/orders', { params: { status: status || undefined, page, size: 10 } })
      .then((r) => {
        if (ignore) return;
        setOrders(r.data.content);
        setTotalPages(r.data.page.totalPages);
      })
      .catch((e) => !ignore && setError(errorMessage(e)))
      .finally(() => !ignore && setLoading(false));
    return () => { ignore = true; };
  }, [status, page]);

  const toggle = async (id) => {
    if (openId === id) { setOpenId(null); return; }
    setOpenId(id);
    if (!details[id]) {
      try {
        const { data } = await api.get(`/admin/orders/${id}`);
        setDetails((d) => ({ ...d, [id]: data }));
      } catch (e) {
        setError(errorMessage(e));
      }
    }
  };

  const changeStatus = async (order, next) => {
    if (!next) return;
    if (next === 'CANCELLED' && !window.confirm(`Cancel order #${order.id}? The stock will be returned.`)) return;
    setBusyId(order.id);
    setError('');
    try {
      const { data } = await api.patch(`/admin/orders/${order.id}/status`, { status: next });
      setOrders((list) => list.map((o) =>
        o.id === order.id ? { ...o, status: data.status, nextStatuses: data.nextStatuses } : o));
      setDetails((d) => ({ ...d, [order.id]: data }));
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setBusyId(null);
    }
  };

  const goTo = (p) => { const n = new URLSearchParams(params); n.set('page', p); setParams(n); };

  return (
    <>
      <h1>Orders</h1>
      <div className="filters">
        <select value={status} onChange={(e) => setParams(e.target.value ? { status: e.target.value } : {})}>
          <option value="">All statuses</option>
          {STATUSES.map((s) => <option key={s} value={s}>{s.toLowerCase()}</option>)}
        </select>
      </div>

      {error && <p className="error">{error}</p>}
      {loading && <p>Loading...</p>}
      {!loading && !error && orders.length === 0 && <p>No orders found.</p>}

      {orders.length > 0 && (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr><th>Order</th><th>Customer</th><th>Date</th><th>Total</th><th>Status</th><th>Update</th></tr>
            </thead>
            <tbody>
              {orders.map((o) => (
                <Fragment key={o.id}>
                  <tr>
                    <td>
                      <button className="btn btn-outline btn-sm" onClick={() => toggle(o.id)}>
                        #{o.id} {openId === o.id ? '▲' : '▼'}
                      </button>
                    </td>
                    <td>{o.userName}<br /><small>{o.userEmail}</small></td>
                    <td>{formatDate(o.createdAt)}</td>
                    <td>{formatPrice(o.totalAmount)}</td>
                    <td><span className={`status status-${o.status.toLowerCase()}`}>{o.status.toLowerCase()}</span></td>
                    <td>
                      {o.nextStatuses.length === 0 ? <small>Final</small> : (
                        <select value="" disabled={busyId === o.id}
                                onChange={(e) => changeStatus(o, e.target.value)}>
                          <option value="">Change to...</option>
                          {o.nextStatuses.map((s) => <option key={s} value={s}>{s.toLowerCase()}</option>)}
                        </select>
                      )}
                    </td>
                  </tr>
                  {openId === o.id && (
                    <tr className="detail">
                      <td colSpan={6}>
                        {!details[o.id] ? 'Loading...' : (
                          <>
                            <p><strong>Ships to:</strong> {details[o.id].shippingAddress}</p>
                            {details[o.id].items.map((i) => (
                              <div className="row line" key={i.productId}>
                                <span>{i.name} × {i.quantity}</span>
                                <span>{formatPrice(i.subtotal)}</span>
                              </div>
                            ))}
                          </>
                        )}
                      </td>
                    </tr>
                  )}
                </Fragment>
              ))}
            </tbody>
          </table>
        </div>
      )}
      <Pager page={page} totalPages={totalPages} onChange={goTo} />
    </>
  );
}
