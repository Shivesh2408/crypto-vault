import React, { useState } from 'react';

const FILE_ICONS = {
  'image/': '🖼️',
  'video/': '🎬',
  'audio/': '🎵',
  'application/pdf': '📄',
  'text/': '📝',
  'application/zip': '🗜️',
};

function getFileIcon(mimeType) {
  if (!mimeType) return '📁';
  for (const [key, icon] of Object.entries(FILE_ICONS)) {
    if (mimeType.startsWith(key)) return icon;
  }
  return '📁';
}

function formatBytes(bytes) {
  if (!bytes) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`;
}

function formatDate(dateStr) {
  if (!dateStr) return '-';
  return new Date(dateStr).toLocaleDateString();
}

export default function FileList({ files, onDelete, onDownload, onShare, onRename }) {
  const [renamingId, setRenamingId] = useState(null);
  const [renameValue, setRenameValue] = useState('');

  const startRename = (file) => {
    setRenamingId(file.id);
    setRenameValue(file.filename);
  };

  const submitRename = (fileId) => {
    if (renameValue.trim()) {
      onRename(fileId, renameValue.trim());
    }
    setRenamingId(null);
  };

  if (files.length === 0) {
    return (
      <div style={{ textAlign: 'center', padding: '3rem', color: '#64748b' }}>
        <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>📂</div>
        <p>No files yet. Upload your first file to get started!</p>
      </div>
    );
  }

  return (
    <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
      <table>
        <thead>
          <tr>
            <th>Name</th>
            <th>Size</th>
            <th>Type</th>
            <th>Uploaded</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {files.map(file => (
            <tr key={file.id}>
              <td>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <span style={{ fontSize: '1.25rem' }}>{getFileIcon(file.mimeType)}</span>
                  {renamingId === file.id ? (
                    <input
                      value={renameValue}
                      onChange={e => setRenameValue(e.target.value)}
                      onBlur={() => submitRename(file.id)}
                      onKeyDown={e => e.key === 'Enter' && submitRename(file.id)}
                      style={{ fontSize: '0.875rem', padding: '0.25rem', border: '1px solid #cbd5e1', borderRadius: '0.25rem' }}
                      autoFocus
                    />
                  ) : (
                    <span style={{ fontWeight: 500 }}>{file.filename}</span>
                  )}
                </div>
              </td>
              <td>{formatBytes(file.size)}</td>
              <td><span style={{ fontSize: '0.75rem', color: '#64748b' }}>{file.mimeType || 'Unknown'}</span></td>
              <td>{formatDate(file.createdAt)}</td>
              <td>
                <div style={{ display: 'flex', gap: '0.25rem' }}>
                  <button className="btn" onClick={() => onDownload(file)}
                    style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem', background: '#eff6ff', color: '#1d4ed8' }}>
                    ⬇ Download
                  </button>
                  <button className="btn" onClick={() => onShare(file)}
                    style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem', background: '#f0fdf4', color: '#16a34a' }}>
                    🔗 Share
                  </button>
                  <button className="btn" onClick={() => startRename(file)}
                    style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem', background: '#fefce8', color: '#854d0e' }}>
                    ✏️
                  </button>
                  <button className="btn" onClick={() => onDelete(file.id)}
                    style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem', background: '#fef2f2', color: '#dc2626' }}>
                    🗑️
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
