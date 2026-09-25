import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api, { errorMessage } from '../api/axios';
import { useCart } from '../context/CartContext';
import { formatPrice } from '../utils';

export default function Checkout() {
  const { cart, loading, refreshCart } = useCart();
  const navigate = useNavigate();
  const [address, setAddress] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  if (loading) return <p>Loading...</p>;

  if (cart.items.length === 0) {
    return (
      <>
        <h1>Checkout</h1>
        <p>Your cart is empty.</p>
        <Link to="/products" className="btn">Browse products</Link>
      </>
    );
  }

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      const { data } = await api.post('/orders', { shippingAddress: address });
      navigate(`/orders/${data.id}`, { replace: true, state: { placed: true } });
      refreshCart();
    } catch (err) {
      setError(errorMessage(err));
      refreshCart();
      setSubmitting(false);
    }
  };

  return (
    <>
      <h1>Checkout</h1>
      {error && <p className="error">{error} <Link to="/cart"><u>Review cart</u></Link></p>}

      <form className="cart" onSubmit={handleSubmit}>
        <div>
          <h2>Shipping address</h2>
          <textarea
            required
            maxLength={255}
            placeholder="House no., street, city, PIN code"
            value={address}
            onChange={(e) => setAddress(e.target.value)}
          />
          <h2>Items</h2>
          {cart.items.map((i) => (
            <div className="row line" key={i.id}>
              <span>{i.name} × {i.quantity}</span>
              <span>{formatPrice(i.subtotal)}</span>
            </div>
          ))}
        </div>

        <aside className="summary">
          <h2>Order summary</h2>
          <div className="row"><span>Items</span><span>{cart.totalItems}</span></div>
          <div className="row total"><span>Total</span><span>{formatPrice(cart.total)}</span></div>
          <button className="btn" disabled={submitting}>
            {submitting ? 'Placing order...' : 'Place order'}
          </button>
          <small>You can pay right after placing the order.</small>
        </aside>
      </form>
    </>
  );
}
