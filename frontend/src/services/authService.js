const AUTH_KEY = 'crypto_vault_auth';
const ACCESS_TOKEN_KEY = 'crypto_vault_access_token';
const REFRESH_TOKEN_KEY = 'crypto_vault_refresh_token';

const authService = {
  setUser(user, accessToken, refreshToken) {
    localStorage.setItem(AUTH_KEY, JSON.stringify(user));
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
    if (refreshToken) localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
  },

  getCurrentUser() {
    try {
      const data = localStorage.getItem(AUTH_KEY);
      return data ? JSON.parse(data) : null;
    } catch {
      return null;
    }
  },

  getAccessToken() {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
  },

  setAccessToken(token) {
    localStorage.setItem(ACCESS_TOKEN_KEY, token);
  },

  getRefreshToken() {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  },

  logout() {
    localStorage.removeItem(AUTH_KEY);
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
  },

  isAuthenticated() {
    return !!this.getAccessToken() && !!this.getCurrentUser();
  },
};

export default authService;
