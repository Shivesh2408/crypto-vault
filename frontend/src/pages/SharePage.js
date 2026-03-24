import React, { useState, useEffect, useCallback } from 'react';
import { useParams } from 'react-router-dom';
import apiService from '../services/apiService';

export default function SharePage() {
  const { token } = useParams();
  const [status, setStatus] = useState('idle'); // idle | loading | password | success | error
  const [password, setPassword] = useState('');
  const [message, setMessage] = useState('');

  const downloadShare = useCallback(async (pwd) => {
    setStatus('loading');
    try {
      const res = await apiService.accessShare(token, pwd || undefined);
      const disposition = res.headers['content-disposition'] || '';
      const filename = disposition.includes('filename=')
        ? disposition.split('filename=')[1].replace(/"/g, '')
        : 'download';
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', filename);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
      setStatus('success');
    } catch (err) {
      const errMsg = err.response?.data?.message || 'Access denied or link expired';
      if (err.response?.status === 403 && !pwd) {
        setStatus('password');
      } else {
        setStatus('error');
        setMessage(errMsg);
      }
    }
  }, [token]);

  useEffect(() => {
    downloadShare(null);
  }, [downloadShare]);

  const handlePasswordSubmit = (e) => {
    e.preventDefault();
    downloadShare(password);
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '1rem' }}>
      <div className="card" style={{ width: '100%', maxWidth: '420px', textAlign: 'center' }}>
        <h2 style={{ fontSize: '1.5rem', fontWeight: 700, color: '#1e40af', marginBottom: '1rem' }}>
          🔐 Crypto-Vault
        </h2>
        <h3 style={{ marginBottom: '1rem' }}>Secure File Download</h3>

        {status === 'loading' && (
          <div className="loading"><div className="spinner" /></div>
        )}

        {status === 'password' && (
          <form onSubmit={handlePasswordSubmit}>
            <p style={{ color: '#64748b', marginBottom: '1rem' }}>
              This file is password protected. Enter the password to download.
            </p>
            <div className="form-group" style={{ textAlign: 'left' }}>
              <label className="form-label">Password</label>
              <input className="form-input" type="password" value={password}
                onChange={e => setPassword(e.target.value)} placeholder="Enter password" required />
            </div>
            <button className="btn btn-primary" type="submit" style={{ width: '100%' }}>
              Download File
            </button>
          </form>
        )}

        {status === 'success' && (
          <div className="alert alert-success">
            ✅ File downloaded successfully!
          </div>
        )}

        {status === 'error' && (
          <div className="alert alert-error">
            ❌ {message || 'This share link is invalid, expired, or has reached its download limit.'}
          </div>
        )}
      </div>
    </div>
  );
}
