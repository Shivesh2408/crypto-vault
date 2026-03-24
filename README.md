# 🔐 Crypto-Vault

**Enterprise-Grade Secure File Encryption & Sharing Platform**

A comprehensive cybersecurity final year B.Tech project implementing industry-standard security practices.

## ✨ Features

- **AES-256 Encryption** – Files encrypted before storage using AES-GCM
- **RSA-2048 Key Management** – Secure key wrapping and exchange
- **JWT Authentication** – 1-hour access tokens with refresh token support
- **Two-Factor Authentication (TOTP)** – Compatible with Google Authenticator / Authy
- **Secure File Sharing** – Time-limited links, download count limits, password protection
- **Role-Based Access Control** – Admin, User, and Auditor roles
- **Audit Logging** – Immutable audit trail stored in Firestore
- **Admin Dashboard** – User management, analytics, security alerts
- **Responsive UI** – Dark/Light mode, mobile-friendly React frontend

## 🏗️ Architecture

```
crypto-vault/
├── src/                          # Java Spring Boot backend
│   ├── main/
│   │   ├── java/com/cryptovault/
│   │   │   ├── config/           # FirebaseConfig, SecurityConfig
│   │   │   ├── controller/       # AuthController, FileController, ShareController, AdminController, UserController
│   │   │   ├── dto/              # Request/Response DTOs
│   │   │   ├── model/            # User, FileMetadata, Share, AuditLog, SecurityAlert
│   │   │   ├── security/         # JwtTokenProvider, TotpUtil, EncryptionUtil, JwtAuthenticationFilter
│   │   │   └── service/          # AuthService, FileService, ShareService, AuditService, AdminService
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/cryptovault/
│           └── CryptoVaultApplicationTests.java
├── frontend/                     # React frontend
│   ├── public/
│   ├── src/
│   │   ├── components/           # Navigation, FileUpload, FileList, ShareModal
│   │   ├── pages/                # LoginPage, RegisterPage, DashboardPage, AdminPage, SettingsPage, SharePage
│   │   └── services/             # apiService, authService
│   └── package.json
└── pom.xml
```

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- Node.js 18+
- Firebase project (Firestore + Storage enabled)

### Backend Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Shivesh2408/crypto-vault.git
   cd crypto-vault
   ```

2. **Firebase Configuration:**
   - Create a Firebase project at https://console.firebase.google.com
   - Enable Firestore and Storage
   - Download your service account key JSON
   - Place it in the project root as `firebase-service-account-key.json`

3. **Update `src/main/resources/application.properties`:**
   ```properties
   jwt.secret=YourSecretKeyHereAtLeast256BitsLong
   firebase.storage.bucket=your-project-id.appspot.com
   ```

4. **Run the backend:**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
   Backend runs at: `http://localhost:8080`

### Frontend Setup

1. **Install dependencies:**
   ```bash
   cd frontend
   npm install
   ```

2. **Create `.env` file:**
   ```
   REACT_APP_API_URL=http://localhost:8080/api
   ```

3. **Start the frontend:**
   ```bash
   npm start
   ```
   Frontend runs at: `http://localhost:3000`

## 📡 API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login with email/password (+ optional TOTP) |
| POST | `/api/auth/setup-2fa` | Initialize 2FA setup |
| POST | `/api/auth/verify-2fa` | Verify and enable 2FA |
| POST | `/api/auth/refresh-token` | Refresh access token |

### Files
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/files/upload` | Upload and encrypt a file |
| GET | `/api/files` | List all user files |
| GET | `/api/files/{id}` | Get file metadata |
| GET | `/api/files/{id}/download` | Download and decrypt file |
| PUT | `/api/files/{id}` | Rename file |
| DELETE | `/api/files/{id}` | Delete file |

### Sharing
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/share/create` | Create share link |
| GET | `/api/share/{token}` | Access shared file (public) |
| DELETE | `/api/share/{id}` | Revoke share |
| GET | `/api/share/history` | Get share history |

### Admin (ADMIN role only)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/users` | List all users |
| PUT | `/api/admin/users/{id}` | Update user role |
| GET | `/api/admin/analytics` | System analytics |
| GET | `/api/admin/logs` | Audit logs |
| GET | `/api/admin/alerts` | Security alerts |

### User
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/user/profile` | Get user profile |
| PUT | `/api/user/profile` | Update profile |
| GET | `/api/user/activity` | Get activity log |
| GET | `/api/user/storage` | Get storage usage |

## 🔒 Security Architecture

- **AES-256-GCM** – Authenticated encryption for files (with random IV per file)
- **RSA-2048/OAEP** – Key wrapping for secure key exchange
- **SHA-256** – File integrity verification
- **bcrypt (cost 12)** – Password hashing
- **JJWT** – JWT implementation with HMAC-SHA256
- **TOTP (RFC 6238)** – Time-based one-time password for 2FA
- **Spring Security** – Stateless session management, role-based authorization
- **CORS** – Configurable allowed origins

## 🗄️ Database Schema (Firestore)

| Collection | Fields |
|-----------|--------|
| `users` | id, email, name, hashedPassword, role, twoFactorSecret, twoFactorEnabled, createdAt, updatedAt |
| `files` | id, userId, filename, size, mimeType, encryptedKey, fileHash, storagePath, version, createdAt |
| `shares` | id, fileId, userId, token, expiresAt, downloadLimit, downloadCount, hashedPassword, revoked |
| `audit_logs` | id, userId, action, resource, resourceId, ipAddress, timestamp |
| `security_alerts` | id, userId, type, description, severity, resolved, timestamp |

## 🧪 Running Tests

```bash
mvn test
```

Tests cover:
- AES-256 encryption/decryption round trip
- RSA-2048 key encryption/decryption
- SHA-256 hash consistency
- Base64 encode/decode
- TOTP secret generation and QR URL

## 📝 License

MIT License – For educational use.
