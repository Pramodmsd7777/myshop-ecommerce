import { Link } from 'react-router-dom';
import { formatPrice } from '../utils';
import WishlistButton from './WishlistButton';

export default function ProductCard({ product }) {
  const discount = product.mrp && product.mrp > product.price
    ? Math.round((1 - product.price / product.mrp) * 100)
    : 0;

  return (
    <div className="card">
      <WishlistButton productId={product.id} />
      <Link to={`/products/${product.id}`} className="card-link">
        {product.imageUrl ? (
          <img src={product.imageUrl} alt={product.name} />
        ) : (
          <div className="img-placeholder">No image</div>
        )}
        <div className="card-body">
          <small>{product.categoryName}</small>
          <h3>{product.name}</h3>
          <div className="pline">
            <strong>{formatPrice(product.price)}</strong>
            {discount > 0 && <em className="off2">{discount}% off</em>}
          </div>
          {product.stock === 0
            ? <span className="badge">Out of stock</span>
            : <small className="free">Free delivery</small>}
        </div>
      </Link>
    </div>
  );
}
