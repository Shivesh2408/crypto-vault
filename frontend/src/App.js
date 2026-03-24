import React, { useState, useEffect, createContext, useContext } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import AdminPage from './pages/AdminPage';
import SettingsPage from './pages/SettingsPage';
import SharePage from './pages/SharePage';
import authService from './services/authService';

export const AuthContext = createContext(null);
export const ThemeContext = createContext(null);

function PrivateRoute({ children, roles }) {
  const { user } = useContext(AuthContext);
  if (!user) return <Navigate to="/login" replace />;
  if (roles && !roles.includes(user.role)) return <Navigate to="/dashboard" replace />;
  return children;
}

function App() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [darkMode, setDarkMode] = useState(
    () => localStorage.getItem('darkMode') === 'true'
  );

  useEffect(() => {
    const storedUser = authService.getCurrentUser();
    if (storedUser) setUser(storedUser);
    setLoading(false);
  }, []);

  useEffect(() => {
    document.body.classList.toggle('dark', darkMode);
    localStorage.setItem('darkMode', darkMode);
  }, [darkMode]);

  if (loading) {
    return (
      <div className="loading" style={{ height: '100vh' }}>
        <div className="spinner" />
      </div>
    );
  }

  return (
    <ThemeContext.Provider value={{ darkMode, setDarkMode }}>
      <AuthContext.Provider value={{ user, setUser }}>
        <Router>
          <Toaster position="top-right" />
          <Routes>
            <Route path="/login" element={user ? <Navigate to="/dashboard" /> : <LoginPage />} />
            <Route path="/register" element={user ? <Navigate to="/dashboard" /> : <RegisterPage />} />
            <Route path="/share/:token" element={<SharePage />} />
            <Route
              path="/dashboard"
              element={<PrivateRoute><DashboardPage /></PrivateRoute>}
            />
            <Route
              path="/admin"
              element={<PrivateRoute roles={['ADMIN']}><AdminPage /></PrivateRoute>}
            />
            <Route
              path="/settings"
              element={<PrivateRoute><SettingsPage /></PrivateRoute>}
            />
            <Route path="/" element={<Navigate to={user ? '/dashboard' : '/login'} />} />
          </Routes>
        </Router>
      </AuthContext.Provider>
    </ThemeContext.Provider>
  );
}

export default App;
