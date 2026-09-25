import { NavLink, Outlet } from 'react-router-dom';

export default function AdminLayout() {
  return (
    <>
      <nav className="admin-tabs">
        <NavLink to="/admin" end>Dashboard</NavLink>
        <NavLink to="/admin/products">Products</NavLink>
        <NavLink to="/admin/orders">Orders</NavLink>
        <NavLink to="/admin/users">Users</NavLink>
      </nav>
      <Outlet />
    </>
  );
}
