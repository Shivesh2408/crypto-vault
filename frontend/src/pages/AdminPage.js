import React, { useState, useEffect, useCallback } from 'react';
import { toast } from 'react-hot-toast';
import Navigation from '../components/Navigation';
import apiService from '../services/apiService';

export default function AdminPage() {
  const [tab, setTab] = useState('users');
  const [users, setUsers] = useState([]);
  const [analytics, setAnalytics] = useState(null);
  const [logs, setLogs] = useState([]);
  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(true);

  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      if (tab === 'users') {
        const res = await apiService.getUsers();
        setUsers(res.data.data || []);
      } else if (tab === 'analytics') {
        const res = await apiService.getAnalytics();
        setAnalytics(res.data.data);
      } else if (tab === 'logs') {
        const res = await apiService.getLogs(100);
        setLogs(res.data.data || []);
      } else if (tab === 'alerts') {
        const res = await apiService.getAlerts();
        setAlerts(res.data.data || []);
      }
    } catch {
      toast.error('Failed to load data');
    } finally {
      setLoading(false);
    }
  }, [tab]);

  useEffect(() => { loadData(); }, [loadData]);

  const handleRoleChange = async (userId, newRole) => {
    try {
      await apiService.updateUserRole(userId, newRole);
      setUsers(users.map(u => u.id === userId ? { ...u, role: newRole } : u));
      toast.success('Role updated');
    } catch {
      toast.error('Failed to update role');
    }
  };

  const formatBytes = (b) => b ? `${(b / 1024 / 1024).toFixed(2)} MB` : '0 MB';
  const formatDate = (d) => d ? new Date(d).toLocaleString() : '-';

  const tabs = ['users', 'analytics', 'logs', 'alerts'];

  return (
    <div style={{ minHeight: '100vh' }}>
      <Navigation />
      <main className="container" style={{ padding: '2rem 1rem' }}>
        <h2 style={{ fontSize: '1.5rem', fontWeight: 700, marginBottom: '1.5rem' }}>Admin Dashboard</h2>

        <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1.5rem', borderBottom: '1px solid #e2e8f0' }}>
          {tabs.map(t => (
            <button key={t} onClick={() => setTab(t)}
              style={{
                padding: '0.5rem 1rem',
                background: 'none',
                border: 'none',
                cursor: 'pointer',
                fontWeight: tab === t ? 700 : 400,
                color: tab === t ? '#1e40af' : '#64748b',
                borderBottom: tab === t ? '2px solid #1e40af' : '2px solid transparent',
                textTransform: 'capitalize',
              }}>
              {t}
            </button>
          ))}
        </div>

        {loading ? (
          <div className="loading"><div className="spinner" /></div>
        ) : (
          <>
            {tab === 'users' && (
              <div className="card">
                <table>
                  <thead>
                    <tr>
                      <th>Name</th>
                      <th>Email</th>
                      <th>Role</th>
                      <th>Created</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {users.map(u => (
                      <tr key={u.id}>
                        <td>{u.name}</td>
                        <td>{u.email}</td>
                        <td>
                          <span className={`badge ${u.role === 'ADMIN' ? 'badge-red' : u.role === 'AUDITOR' ? 'badge-yellow' : 'badge-blue'}`}>
                            {u.role}
                          </span>
                        </td>
                        <td>{formatDate(u.createdAt)}</td>
                        <td>
                          <select value={u.role} onChange={e => handleRoleChange(u.id, e.target.value)}
                            style={{ fontSize: '0.8rem', padding: '0.25rem', borderRadius: '0.25rem', border: '1px solid #e2e8f0' }}>
                            <option value="USER">USER</option>
                            <option value="ADMIN">ADMIN</option>
                            <option value="AUDITOR">AUDITOR</option>
                          </select>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}

            {tab === 'analytics' && analytics && (
              <div className="grid-4">
                {[
                  { label: 'Total Users', value: analytics.totalUsers },
                  { label: 'Total Files', value: analytics.totalFiles },
                  { label: 'Total Shares', value: analytics.totalShares },
                  { label: 'Total Storage', value: formatBytes(analytics.totalStorageBytes) },
                ].map(stat => (
                  <div key={stat.label} className="card" style={{ textAlign: 'center' }}>
                    <div style={{ fontSize: '2rem', fontWeight: 700, color: '#1e40af' }}>{stat.value}</div>
                    <div style={{ color: '#64748b', fontSize: '0.875rem' }}>{stat.label}</div>
                  </div>
                ))}
              </div>
            )}

            {tab === 'logs' && (
              <div className="card">
                <table>
                  <thead>
                    <tr>
                      <th>Action</th>
                      <th>Resource</th>
                      <th>IP Address</th>
                      <th>Timestamp</th>
                    </tr>
                  </thead>
                  <tbody>
                    {logs.map(log => (
                      <tr key={log.id}>
                        <td><span className="badge badge-blue">{log.action}</span></td>
                        <td>{log.resource}</td>
                        <td>{log.ipAddress}</td>
                        <td>{formatDate(log.timestamp)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}

            {tab === 'alerts' && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                {alerts.length === 0 && <p style={{ color: '#64748b' }}>No alerts</p>}
                {alerts.map(alert => (
                  <div key={alert.id} className="card" style={{ borderLeft: `4px solid ${alert.severity === 'CRITICAL' ? '#ef4444' : alert.severity === 'HIGH' ? '#f59e0b' : '#3b82f6'}` }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                      <div>
                        <span className="badge badge-red" style={{ marginRight: '0.5rem' }}>{alert.severity}</span>
                        <strong>{alert.type}</strong>
                      </div>
                      <span style={{ color: '#64748b', fontSize: '0.75rem' }}>{formatDate(alert.timestamp)}</span>
                    </div>
                    <p style={{ marginTop: '0.5rem', fontSize: '0.875rem' }}>{alert.description}</p>
                  </div>
                ))}
              </div>
            )}
          </>
        )}
      </main>
    </div>
  );
}
