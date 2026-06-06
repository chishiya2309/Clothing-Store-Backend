# Walkthrough: Login Feature (Email + Google OAuth + Remember Me)

## Tổng quan

Triển khai hoàn chỉnh chức năng **Đăng nhập / Đăng xuất** cho Clothing Store Backend, bao gồm:

1. **Login bằng Email + Password** — JWT access token (15 phút) + refresh token (Redis)
2. **Google OAuth** — Frontend gửi Google ID token → Backend verify → issue JWT
3. **Remember Me** — Refresh token TTL 30 ngày (thay vì 7 ngày mặc định)
4. **Refresh Token** — Tự động renew access token
5. **Logout** — Xóa refresh token khỏi Redis
6. **Activity Logging** — Ghi log `login`, `login_failed`, `login_google` vào `activity_logs`

---

## API Endpoints

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| `POST` | `/api/auth/login` | Đăng nhập email + password |
| `POST` | `/api/auth/google` | Đăng nhập bằng Google OAuth |
| `POST` | `/api/auth/refresh` | Làm mới access token |
| `POST` | `/api/auth/logout` | Đăng xuất |

### Request/Response

**Login** (`POST /api/auth/login`):
```json
// Request
{ "email": "user@example.com", "password": "12345678", "rememberMe": true }

// Response
{ "status": 200, "message": "Login successful",
  "data": { "accessToken": "eyJ...", "refreshToken": "uuid-...", "expiresIn": 900 } }
```

**Google OAuth** (`POST /api/auth/google`):
```json
// Request
{ "idToken": "google-id-token-string" }

// Response (same as login)
```

---

## Files Changed

### New Files
| File | Mô tả |
|------|-------|
| [LoginRequest.java](file:///d:/Workspace/Nam3_Ky2_Dot2/DesignPattern/BaoCaoCuoiKy/ClothingStore/backend/src/main/java/vn/hcmute/edu/dp/nhom10/backend/dto/request/LoginRequest.java) | DTO: email, password, rememberMe |
| [GoogleAuthRequest.java](file:///d:/Workspace/Nam3_Ky2_Dot2/DesignPattern/BaoCaoCuoiKy/ClothingStore/backend/src/main/java/vn/hcmute/edu/dp/nhom10/backend/dto/request/GoogleAuthRequest.java) | DTO: Google idToken |
| [RefreshTokenRequest.java](file:///d:/Workspace/Nam3_Ky2_Dot2/DesignPattern/BaoCaoCuoiKy/ClothingStore/backend/src/main/java/vn/hcmute/edu/dp/nhom10/backend/dto/request/RefreshTokenRequest.java) | DTO: refreshToken |
| [TokenResponse.java](file:///d:/Workspace/Nam3_Ky2_Dot2/DesignPattern/BaoCaoCuoiKy/ClothingStore/backend/src/main/java/vn/hcmute/edu/dp/nhom10/backend/dto/response/TokenResponse.java) | DTO: accessToken, refreshToken, expiresIn |
| [ActivityLogRepository.java](file:///d:/Workspace/Nam3_Ky2_Dot2/DesignPattern/BaoCaoCuoiKy/ClothingStore/backend/src/main/java/vn/hcmute/edu/dp/nhom10/backend/repository/ActivityLogRepository.java) | JPA Repository cho activity_logs |

### Modified Files
| File | Thay đổi |
|------|----------|
| [AuthService.java](file:///d:/Workspace/Nam3_Ky2_Dot2/DesignPattern/BaoCaoCuoiKy/ClothingStore/backend/src/main/java/vn/hcmute/edu/dp/nhom10/backend/service/AuthService.java) | +3 methods: login, loginWithGoogle, refreshToken, logout |
| [AuthServiceImpl.java](file:///d:/Workspace/Nam3_Ky2_Dot2/DesignPattern/BaoCaoCuoiKy/ClothingStore/backend/src/main/java/vn/hcmute/edu/dp/nhom10/backend/service/impl/AuthServiceImpl.java) | Login logic, Google token verify, remember me TTL |
| [AuthController.java](file:///d:/Workspace/Nam3_Ky2_Dot2/DesignPattern/BaoCaoCuoiKy/ClothingStore/backend/src/main/java/vn/hcmute/edu/dp/nhom10/backend/controller/AuthController.java) | +4 endpoints: login, google, refresh, logout |
| [JwtTokenProvider.java](file:///d:/Workspace/Nam3_Ky2_Dot2/DesignPattern/BaoCaoCuoiKy/ClothingStore/backend/src/main/java/vn/hcmute/edu/dp/nhom10/backend/security/JwtTokenProvider.java) | +generateToken(email), +getJwtExpirationInMs() |
| [CustomUserDetailsService.java](file:///d:/Workspace/Nam3_Ky2_Dot2/DesignPattern/BaoCaoCuoiKy/ClothingStore/backend/src/main/java/vn/hcmute/edu/dp/nhom10/backend/security/CustomUserDetailsService.java) | disabled = !emailVerified |
| [ActivityLog.java](file:///d:/Workspace/Nam3_Ky2_Dot2/DesignPattern/BaoCaoCuoiKy/ClothingStore/backend/src/main/java/vn/hcmute/edu/dp/nhom10/backend/entity/ActivityLog.java) | +@ColumnTransformer for inet type |
| [GlobalExceptionHandling.java](file:///d:/Workspace/Nam3_Ky2_Dot2/DesignPattern/BaoCaoCuoiKy/ClothingStore/backend/src/main/java/vn/hcmute/edu/dp/nhom10/backend/exception/GlobalExceptionHandling.java) | +BadCredentialsException handler |
| [application-dev.yml](file:///d:/Workspace/Nam3_Ky2_Dot2/DesignPattern/BaoCaoCuoiKy/ClothingStore/backend/src/main/resources/application-dev.yml) | +refresh-token-ttl, +remember-me-token-ttl, +google.client-id |
| [pom.xml](file:///d:/Workspace/Nam3_Ky2_Dot2/DesignPattern/BaoCaoCuoiKy/ClothingStore/backend/pom.xml) | +google-api-client dependency |

---

## Test Results

```
Tests run: 23, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS
```

| Test Class | Tests | Status |
|------------|-------|--------|
| `AuthControllerTest` | 7 (register, verify, resend, login, google, refresh, logout) | ✅ PASS |
| `AuthServiceImplTest` | 16 (register ×2, verify ×3, resend ×2, login ×5, refresh ×2, logout ×1, rememberMe ×1) | ✅ PASS |

---

## Cần cấu hình

Thêm biến môi trường vào `.env`:
```properties
GOOGLE_CLIENT_ID=your-google-client-id.apps.googleusercontent.com
```

Lấy Google Client ID tại: [Google Cloud Console](https://console.cloud.google.com/apis/credentials)

---

## Security Notes

- Email không tồn tại → throw `BadCredentialsException` (không leak email enumeration)
- Account bị khóa / chưa verify → throw `AccessDeniedException` (403)
- Google OAuth tự động tạo user mới nếu chưa tồn tại, `emailVerified = true`, `authProvider = "google"`
- Refresh token lưu Redis với TTL, tự hết hạn
