import React, { useState, useEffect, useContext, useCallback } from 'react';
import { toast } from 'react-hot-toast';
import Navigation from '../components/Navigation';
import FileUpload from '../components/FileUpload';
import FileList from '../components/FileList';
import ShareModal from '../components/ShareModal';
import apiService from '../services/apiService';
import { AuthContext } from '../App';

export default function DashboardPage() {
  const { user } = useContext(AuthContext);
  const [files, setFiles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [shareTarget, setShareTarget] = useState(null);
  const [storageUsage, setStorageUsage] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');

  const loadFiles = useCallback(async () => {
    try {
      const res = await apiService.listFiles();
      setFiles(res.data.data || []);
    } catch {
      toast.error('Failed to load files');
    } finally {
      setLoading(false);
    }
  }, []);

  const loadStorage = useCallback(async () => {
    try {
      const res = await apiService.getStorageUsage();
      setStorageUsage(res.data.data);
    } catch {
      // non-critical
    }
  }, []);

  useEffect(() => {
    loadFiles();
    loadStorage();
  }, [loadFiles, loadStorage]);

  const handleDelete = async (fileId) => {
    if (!window.confirm('Delete this file permanently?')) return;
    try {
      await apiService.deleteFile(fileId);
      setFiles(files.filter(f => f.id !== fileId));
      toast.success('File deleted');
      loadStorage();
    } catch {
      toast.error('Delete failed');
    }
  };

  const handleDownload = async (file) => {
    try {
      const res = await apiService.downloadFile(file.id);
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', file.filename);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch {
      toast.error('Download failed');
    }
  };

  const handleRename = async (fileId, newName) => {
    try {
      await apiService.renameFile(fileId, newName);
      setFiles(files.map(f => f.id === fileId ? { ...f, filename: newName } : f));
      toast.success('File renamed');
    } catch {
      toast.error('Rename failed');
    }
  };

  const filteredFiles = files.filter(f =>
    f.filename?.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const formatBytes = (bytes) => {
    if (!bytes) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`;
  };

  return (
    <div style={{ minHeight: '100vh' }}>
      <Navigation />
      <main className="container" style={{ padding: '2rem 1rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
          <div>
            <h2 style={{ fontSize: '1.5rem', fontWeight: 700 }}>My Files</h2>
            <p style={{ color: '#64748b', fontSize: '0.875rem' }}>
              Welcome back, {user?.name} •{' '}
              {storageUsage && <span>Storage used: {formatBytes(storageUsage.usageBytes)}</span>}
            </p>
          </div>
        </div>

        <FileUpload onUploadSuccess={() => { loadFiles(); loadStorage(); }} />

        <div style={{ margin: '1.5rem 0' }}>
          <input
            className="form-input"
            type="text"
            placeholder="Search files..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            style={{ maxWidth: '300px' }}
          />
        </div>

        {loading ? (
          <div className="loading"><div className="spinner" /></div>
        ) : (
          <FileList
            files={filteredFiles}
            onDelete={handleDelete}
            onDownload={handleDownload}
            onShare={(file) => setShareTarget(file)}
            onRename={handleRename}
          />
        )}

        {shareTarget && (
          <ShareModal
            file={shareTarget}
            onClose={() => setShareTarget(null)}
          />
        )}
      </main>
    </div>
  );
}
