import { Link } from 'react-router-dom';
import { useWishlist } from '../context/WishlistContext';
import ProductCard from '../components/ProductCard';

export default function Wishlist() {
  const { items, loading } = useWishlist();
  if (loading) return <p>Loading...</p>;

  return (
    <>
      <h1>Your wishlist</h1>
      {items.length === 0 ? (
        <>
          <p>Nothing saved yet. Tap the heart on any product to save it here.</p>
          <Link to="/products" className="btn">Browse products</Link>
        </>
      ) : (
        <div className="grid">
          {items.map((p) => <ProductCard key={p.id} product={p} />)}
        </div>
      )}
    </>
  );
}
