import React, { useState, useEffect, useContext, useCallback } from 'react';
import { toast } from 'react-hot-toast';
import { QRCodeSVG } from 'qrcode.react';
import Navigation from '../components/Navigation';
import apiService from '../services/apiService';
import authService from '../services/authService';
import { AuthContext } from '../App';

export default function SettingsPage() {
  const { user, setUser } = useContext(AuthContext);
  const [profile, setProfile] = useState({ name: '', email: '' });
  const [twoFaSetup, setTwoFaSetup] = useState(null);
  const [totpCode, setTotpCode] = useState('');
  const [activity, setActivity] = useState([]);
  const [loading, setLoading] = useState(false);
  const [tab, setTab] = useState('profile');

  const loadProfile = useCallback(async () => {
    try {
      const res = await apiService.getProfile();
      const p = res.data.data;
      setProfile({ name: p.name, email: p.email });
    } catch {
      toast.error('Failed to load profile');
    }
  }, []);

  const loadActivity = useCallback(async () => {
    try {
      const res = await apiService.getActivity();
      setActivity(res.data.data || []);
    } catch {
      toast.error('Failed to load activity');
    }
  }, []);

  useEffect(() => {
    loadProfile();
    if (tab === 'activity') loadActivity();
  }, [loadProfile, loadActivity, tab]);

  const handleProfileUpdate = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await apiService.updateProfile({ name: profile.name });
      const updatedUser = { ...user, name: profile.name };
      authService.setUser(updatedUser, authService.getAccessToken(), authService.getRefreshToken());
      setUser(updatedUser);
      toast.success('Profile updated');
    } catch {
      toast.error('Update failed');
    } finally {
      setLoading(false);
    }
  };

  const handleSetup2FA = async () => {
    try {
      const res = await apiService.setup2fa();
      setTwoFaSetup(res.data.data);
    } catch {
      toast.error('Failed to setup 2FA');
    }
  };

  const handleVerify2FA = async (e) => {
    e.preventDefault();
    try {
      await apiService.verify2fa(totpCode);
      toast.success('2FA enabled successfully!');
      setTwoFaSetup(null);
      setTotpCode('');
    } catch {
      toast.error('Invalid code. Please try again.');
    }
  };

  const formatDate = (d) => d ? new Date(d).toLocaleString() : '-';

  return (
    <div style={{ minHeight: '100vh' }}>
      <Navigation />
      <main className="container" style={{ padding: '2rem 1rem', maxWidth: '800px' }}>
        <h2 style={{ fontSize: '1.5rem', fontWeight: 700, marginBottom: '1.5rem' }}>Settings</h2>

        <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1.5rem', borderBottom: '1px solid #e2e8f0' }}>
          {['profile', '2fa', 'activity'].map(t => (
            <button key={t} onClick={() => setTab(t)}
              style={{
                padding: '0.5rem 1rem', background: 'none', border: 'none', cursor: 'pointer',
                fontWeight: tab === t ? 700 : 400,
                color: tab === t ? '#1e40af' : '#64748b',
                borderBottom: tab === t ? '2px solid #1e40af' : '2px solid transparent',
              }}>
              {t === '2fa' ? 'Two-Factor Auth' : t.charAt(0).toUpperCase() + t.slice(1)}
            </button>
          ))}
        </div>

        {tab === 'profile' && (
          <div className="card">
            <h3 style={{ fontWeight: 600, marginBottom: '1rem' }}>Profile Information</h3>
            <form onSubmit={handleProfileUpdate}>
              <div className="form-group">
                <label className="form-label">Full Name</label>
                <input className="form-input" type="text" value={profile.name}
                  onChange={e => setProfile({ ...profile, name: e.target.value })} required />
              </div>
              <div className="form-group">
                <label className="form-label">Email (read-only)</label>
                <input className="form-input" type="email" value={profile.email} readOnly
                  style={{ backgroundColor: '#f1f5f9', cursor: 'not-allowed' }} />
              </div>
              <button className="btn btn-primary" type="submit" disabled={loading}>
                {loading ? 'Saving...' : 'Save Changes'}
              </button>
            </form>
          </div>
        )}

        {tab === '2fa' && (
          <div className="card">
            <h3 style={{ fontWeight: 600, marginBottom: '1rem' }}>Two-Factor Authentication</h3>
            {!twoFaSetup ? (
              <div>
                <p style={{ color: '#64748b', marginBottom: '1rem' }}>
                  Protect your account with time-based one-time passwords (TOTP).
                </p>
                <button className="btn btn-primary" onClick={handleSetup2FA}>
                  Setup 2FA
                </button>
              </div>
            ) : (
              <div>
                <p style={{ marginBottom: '1rem' }}>
                  Scan this QR code with your authenticator app (Google Authenticator, Authy):
                </p>
                <div style={{ display: 'flex', justifyContent: 'center', marginBottom: '1rem' }}>
                  <QRCodeSVG value={twoFaSetup.qrUrl} size={200} />
                </div>
                <p style={{ fontSize: '0.875rem', color: '#64748b', marginBottom: '1rem' }}>
                  Or enter this secret manually: <code style={{ background: '#f1f5f9', padding: '0.25rem 0.5rem', borderRadius: '0.25rem' }}>{twoFaSetup.secret}</code>
                </p>
                <form onSubmit={handleVerify2FA}>
                  <div className="form-group">
                    <label className="form-label">Verification Code</label>
                    <input className="form-input" type="text" value={totpCode} maxLength={6}
                      onChange={e => setTotpCode(e.target.value)} placeholder="Enter 6-digit code" required
                      style={{ maxWidth: '200px' }} />
                  </div>
                  <button className="btn btn-primary" type="submit">Verify & Enable</button>
                </form>
              </div>
            )}
          </div>
        )}

        {tab === 'activity' && (
          <div className="card">
            <h3 style={{ fontWeight: 600, marginBottom: '1rem' }}>Recent Activity</h3>
            {activity.length === 0 ? (
              <p style={{ color: '#64748b' }}>No activity yet</p>
            ) : (
              <table>
                <thead>
                  <tr>
                    <th>Action</th>
                    <th>Resource</th>
                    <th>IP Address</th>
                    <th>Time</th>
                  </tr>
                </thead>
                <tbody>
                  {activity.map(a => (
                    <tr key={a.id}>
                      <td><span className="badge badge-blue">{a.action}</span></td>
                      <td>{a.resource}</td>
                      <td>{a.ipAddress}</td>
                      <td>{formatDate(a.timestamp)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}
      </main>
    </div>
  );
}
