import { createContext, useContext, useEffect, useState } from 'react';
import api from '../api/axios';
import { useAuth } from './AuthContext';

const CartContext = createContext(null);
const EMPTY = { items: [], totalItems: 0, total: 0 };

export function CartProvider({ children }) {
  const { user } = useAuth();
  const [cart, setCart] = useState(EMPTY);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!user) { setCart(EMPTY); return; }
    setLoading(true);
    api.get('/cart')
      .then((r) => setCart(r.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [user]);

  const refreshCart = () => api.get('/cart').then((r) => setCart(r.data)).catch(() => {});
  const addToCart = async (productId, quantity = 1) =>
    setCart((await api.post('/cart', { productId, quantity })).data);
  const updateQuantity = async (itemId, quantity) =>
    setCart((await api.put(`/cart/${itemId}`, { quantity })).data);
  const removeItem = async (itemId) =>
    setCart((await api.delete(`/cart/${itemId}`)).data);

  return (
    <CartContext.Provider value={{ cart, loading, addToCart, updateQuantity, removeItem, refreshCart }}>
      {children}
    </CartContext.Provider>
  );
}

export const useCart = () => useContext(CartContext);
