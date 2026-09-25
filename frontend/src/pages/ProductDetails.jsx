import { useEffect, useState } from 'react';
import { useParams, Link, useNavigate, useLocation } from 'react-router-dom';
import api, { errorMessage } from '../api/axios';
import { formatPrice } from '../utils';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import WishlistButton from '../components/WishlistButton';

export default function ProductDetails() {
  const { id } = useParams();
  const { user } = useAuth();
  const { addToCart } = useCart();
  const navigate = useNavigate();
  const location = useLocation();

  const [product, setProduct] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [qty, setQty] = useState(1);
  const [adding, setAdding] = useState(false);
  const [message, setMessage] = useState('');
  const [cartError, setCartError] = useState('');

  useEffect(() => {
    setLoading(true);
    setQty(1);
    setMessage('');
    api
      .get(`/products/${id}`)
      .then((r) => setProduct(r.data))
      .catch((e) =>
        setError(e.response?.status === 404 ? 'Product not found' : errorMessage(e))
      )
      .finally(() => setLoading(false));
  }, [id]);

  const handleAdd = async () => {
    if (!user) {
      navigate('/login', { state: { from: location } });
      return;
    }
    setAdding(true);
    setMessage('');
    setCartError('');
    try {
      await addToCart(product.id, qty);
      setMessage(`Added ${qty} to your cart.`);
    } catch (e) {
      setCartError(errorMessage(e));
    } finally {
      setAdding(false);
    }
  };

  if (loading) return <p>Loading...</p>;
  if (error) return <p className="error">{error}. <Link to="/products">Back to products</Link></p>;

  const maxQty = Math.min(product.stock, 99);

  return (
    <div className="details">
      {product.imageUrl ? (
        <img src={product.imageUrl} alt={product.name} />
      ) : (
        <div className="img-placeholder big">No image</div>
      )}
      <div>
        <small>{product.categoryName}</small>
        <h1>{product.name}</h1>
        <p className="price">{formatPrice(product.price)}</p>
        <small className="free">Free delivery · Inclusive of all taxes</small>
        <p>{product.description}</p>
        <p>{product.stock > 0 ? `${product.stock} in stock` : 'Out of stock'}</p>

        {product.stock > 0 && (
          <div className="qty">
            <button className="btn btn-outline" disabled={qty <= 1}
                    onClick={() => setQty(qty - 1)}>−</button>
            <span>{qty}</span>
            <button className="btn btn-outline" disabled={qty >= maxQty}
                    onClick={() => setQty(qty + 1)}>+</button>
          </div>
        )}

        <p>
          <button className="btn" disabled={product.stock === 0 || adding} onClick={handleAdd}>
            {adding ? 'Adding...' : 'Add to cart'}
          </button>
        </p>

        {cartError && <p className="error">{cartError}</p>}
        {message && <p>{message} <Link to="/cart"><u>View cart</u></Link></p>}

        <p><WishlistButton productId={product.id} inline /></p>
        <p><Link to="/products">← Back to products</Link></p>
      </div>
    </div>
  );
}
