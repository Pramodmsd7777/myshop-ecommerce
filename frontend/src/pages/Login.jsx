import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import api, { errorMessage } from '../api/axios';
import { useAuth } from '../context/AuthContext';

export default function Login() {
  const [form, setForm] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = location.state?.from?.pathname || '/';

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      const { data } = await api.post('/auth/login', form);
      login(data);
      navigate(from, { replace: true });
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="rope-stage">
      <div className="beam" />
      <div className="swing">
        <i className="rope l" /><i className="rope r" />
        <i className="knot l" /><i className="knot r" />
        <form className="auth-form" onSubmit={handleSubmit}>
          <h1>Welcome back</h1>
          <p className="sub">Sign in to continue shopping</p>
          {error && <p className="error">{error}</p>}
          <input name="email" type="email" placeholder="Email" value={form.email}
                 onChange={handleChange} required />
          <input name="password" type="password" placeholder="Password" value={form.password}
                 onChange={handleChange} required />
          <button className="btn" disabled={submitting}>
            {submitting ? 'Logging in...' : 'Login'}
          </button>
          <p>No account? <Link to="/register">Register</Link></p>
        </form>
      </div>
    </div>
  );
}
