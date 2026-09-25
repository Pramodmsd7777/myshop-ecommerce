import { createContext, useContext, useEffect, useState } from 'react';
import api from '../api/axios';
import { useAuth } from './AuthContext';

const WishlistContext = createContext(null);

export function WishlistProvider({ children }) {
  const { user } = useAuth();
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!user) { setItems([]); return; }
    setLoading(true);
    api.get('/wishlist')
      .then((r) => setItems(r.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [user]);

  const has = (productId) => items.some((p) => p.id === productId);
  const add = async (productId) => setItems((await api.post(`/wishlist/${productId}`)).data);
  const remove = async (productId) => setItems((await api.delete(`/wishlist/${productId}`)).data);

  return (
    <WishlistContext.Provider value={{ items, loading, has, add, remove }}>
      {children}
    </WishlistContext.Provider>
  );
}

export const useWishlist = () => useContext(WishlistContext);
