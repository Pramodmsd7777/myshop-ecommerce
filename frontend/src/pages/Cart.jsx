import { useState } from 'react';
import { Link } from 'react-router-dom';
import { errorMessage } from '../api/axios';
import { useCart } from '../context/CartContext';
import { formatPrice } from '../utils';

export default function Cart() {
  const { cart, loading, updateQuantity, removeItem } = useCart();
  const [error, setError] = useState('');
  const [busyId, setBusyId] = useState(null);

  const run = async (itemId, action) => {
    setError('');
    setBusyId(itemId);
    try { await action(); }
    catch (e) { setError(errorMessage(e)); }
    finally { setBusyId(null); }
  };

  if (loading) return <p>Loading...</p>;

  if (cart.items.length === 0) {
    return (
      <>
        <h1>Your cart</h1>
        <p>Your cart is empty.</p>
        <Link to="/products" className="btn">Browse products</Link>
      </>
    );
  }

  return (
    <>
      <h1>Your cart</h1>
      {error && <p className="error">{error}</p>}

      <div className="cart">
        <div className="cart-items">
          {cart.items.map((item) => {
            const busy = busyId === item.id;
            return (
              <div className="cart-row" key={item.id}>
                <Link to={`/products/${item.productId}`} className="cart-thumb">
                  {item.imageUrl
                    ? <img src={item.imageUrl} alt={item.name} />
                    : <div className="img-placeholder">No image</div>}
                </Link>

                <div className="cart-info">
                  <Link to={`/products/${item.productId}`}><strong>{item.name}</strong></Link>
                  <small>{formatPrice(item.price)} each</small>
                </div>

                <div className="qty">
                  <button className="btn btn-outline" disabled={busy || item.quantity <= 1}
                          onClick={() => run(item.id, () => updateQuantity(item.id, item.quantity - 1))}>−</button>
                  <span>{item.quantity}</span>
                  <button className="btn btn-outline" disabled={busy || item.quantity >= item.stock}
                          onClick={() => run(item.id, () => updateQuantity(item.id, item.quantity + 1))}>+</button>
                </div>

                <strong className="cart-sub">{formatPrice(item.subtotal)}</strong>
                <button className="link-btn" disabled={busy}
                        onClick={() => run(item.id, () => removeItem(item.id))}>Remove</button>
              </div>
            );
          })}
        </div>

        <aside className="summary">
          <h2>Order summary</h2>
          <div className="row"><span>Items</span><span>{cart.totalItems}</span></div>
          <div className="row total"><span>Total</span><span>{formatPrice(cart.total)}</span></div>
          <Link to="/checkout" className="btn">Checkout</Link>
        </aside>
      </div>
    </>
  );
}
