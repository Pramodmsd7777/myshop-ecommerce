import { useEffect, useState } from 'react';
import { Link, useLocation, useParams } from 'react-router-dom';
import api, { errorMessage } from '../api/axios';
import { formatPrice, formatDate, loadRazorpay } from '../utils';
import { useAuth } from '../context/AuthContext';

export default function OrderDetails() {
  const { id } = useParams();
  const { state } = useLocation();
  const { user } = useAuth();
  const [order, setOrder] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [paying, setPaying] = useState(false);
  const [payError, setPayError] = useState('');
  const [paidNow, setPaidNow] = useState(false);

  useEffect(() => {
    setLoading(true);
    api.get(`/orders/${id}`)
      .then((r) => setOrder(r.data))
      .catch((e) => setError(e.response?.status === 404 ? 'Order not found' : errorMessage(e)))
      .finally(() => setLoading(false));
  }, [id]);

  const pay = async () => {
    setPayError('');
    setPaying(true);
    try {
      if (!(await loadRazorpay())) throw new Error('Could not load the payment window. Check your connection.');
      const { data: init } = await api.post('/payments/create', { orderId: order.id });

      const rzp = new window.Razorpay({
        key: init.keyId,
        amount: init.amount,
        currency: init.currency,
        order_id: init.razorpayOrderId,
        name: 'MyShop',
        description: `Order #${order.id}`,
        prefill: { name: user.name, email: user.email },
        theme: { color: '#8b5cf6' },
        handler: async (resp) => {          // payment succeeded in the browser; the SERVER still verifies it
          try {
            const { data } = await api.post('/payments/verify', {
              razorpayOrderId: resp.razorpay_order_id,
              razorpayPaymentId: resp.razorpay_payment_id,
              razorpaySignature: resp.razorpay_signature,
            });
            setOrder(data);
            setPaidNow(true);
          } catch (e) {
            setPayError(`${errorMessage(e)} If money was deducted, refresh in a minute; it will show as paid.`);
          } finally {
            setPaying(false);
          }
        },
        modal: { ondismiss: () => setPaying(false) },
      });
      rzp.on('payment.failed', (r) => {
        setPayError(r.error?.description || 'Payment failed. You can try again.');
        setPaying(false);
      });
      rzp.open();
    } catch (e) {
      setPayError(e.response ? errorMessage(e) : e.message);
      setPaying(false);
    }
  };

  if (loading) return <p>Loading...</p>;
  if (error) return <p className="error">{error}. <Link to="/orders">Back to orders</Link></p>;

  return (
    <>
      {paidNow && <div className="success">Payment received. Thank you!</div>}
      {state?.placed && !paidNow && order.status === 'PENDING' && (
        <div className="success">Your order #{order.id} is placed. Complete the payment below to confirm it.</div>
      )}
      <h1>Order #{order.id}</h1>

      <div className="cart">
        <div>
          {order.items.map((i) => (
            <div className="cart-row" key={i.productId}>
              <Link to={`/products/${i.productId}`} className="cart-thumb">
                {i.imageUrl
                  ? <img src={i.imageUrl} alt={i.name} />
                  : <div className="img-placeholder">No image</div>}
              </Link>
              <div className="cart-info">
                <Link to={`/products/${i.productId}`}><strong>{i.name}</strong></Link>
                <small>{i.quantity} × {formatPrice(i.price)}</small>
              </div>
              <strong className="cart-sub">{formatPrice(i.subtotal)}</strong>
            </div>
          ))}
        </div>

        <aside className="summary">
          <h2>Summary</h2>
          <div className="row"><span>Status</span>
            <span className={`status status-${order.status.toLowerCase()}`}>{order.status.toLowerCase()}</span></div>
          <div className="row"><span>Placed</span><span>{formatDate(order.createdAt)}</span></div>
          <div className="row total"><span>Total</span><span>{formatPrice(order.totalAmount)}</span></div>
          <div><small>Ships to</small><p style={{ margin: '4px 0 0' }}>{order.shippingAddress}</p></div>

          {order.status === 'PENDING' && (
            <button className="btn" onClick={pay} disabled={paying}>
              {paying ? 'Processing...' : `Pay ${formatPrice(order.totalAmount)}`}
            </button>
          )}
          {payError && <p className="error">{payError}</p>}
          <Link to="/orders" className="btn btn-outline">All orders</Link>
        </aside>
      </div>
    </>
  );
}
