import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import api, { errorMessage } from '../api/axios';
import { useAuth } from '../context/AuthContext';

export default function Register() {
  const [form, setForm] = useState({ name: '', email: '', password: '' });
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
    if (form.password.length < 8) {
      setError('Password must be at least 8 characters');
      return;
    }
    setSubmitting(true);
    try {
      const { data } = await api.post('/auth/register', form);
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
          <h1>Create account</h1>
          <p className="sub">Join MyShop in a minute</p>
          {error && <p className="error">{error}</p>}
          <input name="name" placeholder="Full name" value={form.name}
                 onChange={handleChange} required />
          <input name="email" type="email" placeholder="Email" value={form.email}
                 onChange={handleChange} required />
          <input name="password" type="password" placeholder="Password (min 8 chars)"
                 value={form.password} onChange={handleChange} required />
          <button className="btn" disabled={submitting}>
            {submitting ? 'Creating...' : 'Register'}
          </button>
          <p>Already have an account? <Link to="/login">Login</Link></p>
        </form>
      </div>
    </div>
  );
}
