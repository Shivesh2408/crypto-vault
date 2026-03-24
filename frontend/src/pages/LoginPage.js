import React, { useState, useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import apiService from '../services/apiService';
import authService from '../services/authService';
import { AuthContext } from '../App';

export default function LoginPage() {
  const navigate = useNavigate();
  const { setUser } = useContext(AuthContext);
  const [form, setForm] = useState({ email: '', password: '', totpCode: '' });
  const [needsTwoFactor, setNeedsTwoFactor] = useState(false);
  const [pendingUserId, setPendingUserId] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const res = await apiService.login(form);
      const { data } = res.data;
      if (data.twoFactorRequired) {
        setNeedsTwoFactor(true);
        setPendingUserId(data.userId);
        setLoading(false);
        return;
      }
      authService.setUser(
        { userId: data.userId, email: data.email, name: data.name, role: data.role },
        data.accessToken,
        data.refreshToken
      );
      setUser({ userId: data.userId, email: data.email, name: data.name, role: data.role });
      toast.success('Welcome back!');
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '1rem' }}>
      <div className="card" style={{ width: '100%', maxWidth: '420px' }}>
        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 700, color: '#1e40af' }}>🔐 Crypto-Vault</h1>
          <p style={{ color: '#64748b', marginTop: '0.5rem' }}>Sign in to your account</p>
        </div>

        {error && <div className="alert alert-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          {!needsTwoFactor ? (
            <>
              <div className="form-group">
                <label className="form-label">Email</label>
                <input
                  className="form-input"
                  type="email"
                  name="email"
                  value={form.email}
                  onChange={handleChange}
                  placeholder="you@example.com"
                  required
                />
              </div>
              <div className="form-group">
                <label className="form-label">Password</label>
                <input
                  className="form-input"
                  type="password"
                  name="password"
                  value={form.password}
                  onChange={handleChange}
                  placeholder="••••••••"
                  required
                />
              </div>
            </>
          ) : (
            <div className="form-group">
              <label className="form-label">Two-Factor Authentication Code</label>
              <input
                className="form-input"
                type="text"
                name="totpCode"
                value={form.totpCode}
                onChange={handleChange}
                placeholder="Enter 6-digit code"
                maxLength={6}
                required
              />
              <p style={{ fontSize: '0.75rem', color: '#64748b', marginTop: '0.25rem' }}>
                Enter the code from your authenticator app
              </p>
            </div>
          )}

          <button className="btn btn-primary" type="submit" disabled={loading}
            style={{ width: '100%', padding: '0.75rem', marginTop: '0.5rem' }}>
            {loading ? 'Signing in...' : needsTwoFactor ? 'Verify Code' : 'Sign In'}
          </button>
        </form>

        <p style={{ textAlign: 'center', marginTop: '1rem', fontSize: '0.875rem', color: '#64748b' }}>
          Don't have an account? <Link to="/register" style={{ color: '#1e40af' }}>Sign up</Link>
        </p>
      </div>
    </div>
  );
}
