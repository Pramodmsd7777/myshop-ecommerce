import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useWishlist } from '../context/WishlistContext';

export default function WishlistButton({ productId, inline = false }) {
  const { user } = useAuth();
  const { has, add, remove } = useWishlist();
  const navigate = useNavigate();
  const location = useLocation();
  const [busy, setBusy] = useState(false);
  const active = has(productId);

  const toggle = async () => {
    if (!user) { navigate('/login', { state: { from: location } }); return; }
    setBusy(true);
    try { active ? await remove(productId) : await add(productId); }
    catch { /* leave the heart as it was */ }
    finally { setBusy(false); }
  };

  return (
    <button type="button" disabled={busy} onClick={toggle}
            className={`heart ${active ? 'on' : ''} ${inline ? 'inline' : ''}`}
            aria-pressed={active}
            aria-label={active ? 'Remove from wishlist' : 'Add to wishlist'}>
      {active ? '♥' : '♡'}{inline && (active ? ' Saved to wishlist' : ' Save to wishlist')}
    </button>
  );
}
