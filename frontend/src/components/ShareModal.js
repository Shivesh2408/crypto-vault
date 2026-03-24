import React, { useState } from 'react';
import { toast } from 'react-hot-toast';
import apiService from '../services/apiService';

const EXPIRY_OPTIONS = [
  { label: '1 hour', hours: 1 },
  { label: '6 hours', hours: 6 },
  { label: '24 hours', hours: 24 },
  { label: '3 days', hours: 72 },
  { label: '7 days', hours: 168 },
  { label: '30 days', hours: 720 },
];

export default function ShareModal({ file, onClose }) {
  const [form, setForm] = useState({
    expiresInHours: 24,
    downloadLimit: 0,
    password: '',
    sharedWithEmail: '',
  });
  const [shareLink, setShareLink] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleCreate = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const res = await apiService.createShare({
        fileId: file.id,
        expiresInHours: parseInt(form.expiresInHours),
        downloadLimit: parseInt(form.downloadLimit) || 0,
        password: form.password || null,
        sharedWithEmail: form.sharedWithEmail || null,
      });
      const share = res.data.data;
      const link = `${window.location.origin}/share/${share.token}`;
      setShareLink(link);
      toast.success('Share link created!');
    } catch {
      toast.error('Failed to create share link');
    } finally {
      setLoading(false);
    }
  };

  const copyToClipboard = () => {
    navigator.clipboard.writeText(shareLink);
    toast.success('Link copied to clipboard!');
  };

  return (
    <div style={{
      position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.5)',
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      zIndex: 1000, padding: '1rem',
    }}>
      <div className="card" style={{ width: '100%', maxWidth: '480px', position: 'relative' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
          <h3 style={{ fontWeight: 600 }}>Share: {file.filename}</h3>
          <button onClick={onClose} style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: '1.25rem' }}>×</button>
        </div>

        {!shareLink ? (
          <form onSubmit={handleCreate}>
            <div className="form-group">
              <label className="form-label">Expires in</label>
              <select className="form-input" name="expiresInHours" value={form.expiresInHours} onChange={handleChange}>
                {EXPIRY_OPTIONS.map(o => (
                  <option key={o.hours} value={o.hours}>{o.label}</option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">Download limit (0 = unlimited)</label>
              <input className="form-input" type="number" name="downloadLimit" min="0"
                value={form.downloadLimit} onChange={handleChange} />
            </div>

            <div className="form-group">
              <label className="form-label">Password (optional)</label>
              <input className="form-input" type="password" name="password"
                value={form.password} onChange={handleChange} placeholder="Leave blank for no password" />
            </div>

            <div className="form-group">
              <label className="form-label">Share with email (optional)</label>
              <input className="form-input" type="email" name="sharedWithEmail"
                value={form.sharedWithEmail} onChange={handleChange} placeholder="recipient@example.com" />
            </div>

            <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end' }}>
              <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
              <button type="submit" className="btn btn-primary" disabled={loading}>
                {loading ? 'Creating...' : 'Create Share Link'}
              </button>
            </div>
          </form>
        ) : (
          <div>
            <div className="alert alert-success">Share link created successfully!</div>
            <div style={{
              background: '#f1f5f9',
              border: '1px solid #e2e8f0',
              borderRadius: '0.375rem',
              padding: '0.75rem',
              wordBreak: 'break-all',
              fontSize: '0.875rem',
              marginBottom: '1rem',
            }}>
              {shareLink}
            </div>
            <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end' }}>
              <button className="btn btn-secondary" onClick={onClose}>Close</button>
              <button className="btn btn-primary" onClick={copyToClipboard}>📋 Copy Link</button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
