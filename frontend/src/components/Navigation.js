import React, { useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import authService from '../services/authService';
import { AuthContext, ThemeContext } from '../App';

export default function Navigation() {
  const navigate = useNavigate();
  const { user, setUser } = useContext(AuthContext);
  const { darkMode, setDarkMode } = useContext(ThemeContext);

  const handleLogout = () => {
    authService.logout();
    setUser(null);
    toast.success('Logged out');
    navigate('/login');
  };

  return (
    <nav style={{
      background: darkMode ? '#1e293b' : '#1e40af',
      padding: '0 1rem',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      height: '56px',
      position: 'sticky',
      top: 0,
      zIndex: 100,
    }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '1.5rem' }}>
        <Link to="/dashboard" style={{ color: 'white', fontWeight: 700, fontSize: '1.1rem', textDecoration: 'none' }}>
          🔐 Crypto-Vault
        </Link>
        <Link to="/dashboard" style={navLinkStyle}>Files</Link>
        {user?.role === 'ADMIN' && (
          <Link to="/admin" style={navLinkStyle}>Admin</Link>
        )}
        <Link to="/settings" style={navLinkStyle}>Settings</Link>
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
        <button
          onClick={() => setDarkMode(!darkMode)}
          style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'white', fontSize: '1.25rem' }}
          title="Toggle theme"
        >
          {darkMode ? '☀️' : '🌙'}
        </button>
        <span style={{ color: 'rgba(255,255,255,0.8)', fontSize: '0.875rem' }}>{user?.name}</span>
        <button className="btn" onClick={handleLogout}
          style={{ background: 'rgba(255,255,255,0.15)', color: 'white', fontSize: '0.8rem' }}>
          Logout
        </button>
      </div>
    </nav>
  );
}

const navLinkStyle = {
  color: 'rgba(255,255,255,0.85)',
  textDecoration: 'none',
  fontSize: '0.875rem',
};
