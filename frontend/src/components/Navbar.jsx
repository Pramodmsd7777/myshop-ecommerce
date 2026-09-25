import { NavLink, Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import { useWishlist } from '../context/WishlistContext';

export default function Navbar() {
  const { user, logout, isAdmin } = useAuth();
  const { cart } = useCart();
  const { items: wished } = useWishlist();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <header className="navbar">
      <div className="container navbar-inner">
        <Link to="/" className="brand">MyShop</Link>
        <nav className="nav-links">
          <NavLink to="/" end>Home</NavLink>
          <NavLink to="/products">Products</NavLink>
        </nav>
        <div className="nav-auth">
          {user ? (
            <>
              <NavLink to="/wishlist">
                Wishlist{wished.length > 0 && <span className="cart-badge">{wished.length}</span>}
              </NavLink>
              <NavLink to="/orders">Orders</NavLink>
              <NavLink to="/cart">
                Cart{cart.totalItems > 0 && <span className="cart-badge">{cart.totalItems}</span>}
              </NavLink>
              {isAdmin && <NavLink to="/admin">Admin</NavLink>}
              <span>Hi, {user.name}</span>
              <button className="btn btn-outline" onClick={handleLogout}>Logout</button>
            </>
          ) : (
            <>
              <NavLink to="/login">Login</NavLink>
              <NavLink to="/register" className="btn">Register</NavLink>
            </>
          )}
        </div>
      </div>
    </header>
  );
}
