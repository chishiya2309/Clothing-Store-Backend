package vn.hcmute.edu.dp.nhom10.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.RegisterRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.LoginRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.GoogleAuthRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.RefreshTokenRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.ResendVerificationRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.ForgotPasswordRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.ResetPasswordRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.TokenResponse;
import vn.hcmute.edu.dp.nhom10.backend.exception.GlobalExceptionHandling;
import vn.hcmute.edu.dp.nhom10.backend.service.AuthService;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

        private MockMvc mockMvc;

        private final ObjectMapper objectMapper = new ObjectMapper();

        @Mock
        private AuthService authService;

        @InjectMocks
        private AuthController authController;

        @BeforeEach
        void setUp() {
                LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
                validator.afterPropertiesSet();

                mockMvc = MockMvcBuilders.standaloneSetup(authController)
                                .setControllerAdvice(new GlobalExceptionHandling())
                                .setValidator(validator)
                                .build();
        }

        @Test
        void register_success() throws Exception {
                RegisterRequest request = new RegisterRequest("test@test.com", "Password123", "Test User");

                doNothing().when(authService).register(request);

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.status").value(201))
                                .andExpect(jsonPath("$.message").value(
                                                "Registration successful. Please check your email to verify your account."));
        }

        @Test
        void verifyEmail_success() throws Exception {
                String token = "valid_token";

                doNothing().when(authService).verifyEmail(token);

                mockMvc.perform(get("/api/auth/verify-email")
                                .param("token", token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(200))
                                .andExpect(jsonPath("$.message").value("Email verified successfully"));
        }

        @Test
        void resendVerification_success() throws Exception {
                ResendVerificationRequest request = new ResendVerificationRequest("test@test.com");

                doNothing().when(authService).resendVerificationEmail(request.email());

                mockMvc.perform(post("/api/auth/resend-verification")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(200))
                                .andExpect(jsonPath("$.message").value("Verification email resent successfully"));
        }

        @Test
        void login_success() throws Exception {
                LoginRequest request = new LoginRequest("test@test.com", "Password123", false);
                TokenResponse tokenResponse = TokenResponse.builder()
                                .accessToken("access_token")
                                .refreshToken("refresh_token")
                                .expiresIn(900L)
                                .build();

                when(authService.login(eq(request), any(), any())).thenReturn(tokenResponse);

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(200))
                                .andExpect(jsonPath("$.message").value("Login successful"))
                                .andExpect(jsonPath("$.data.accessToken").value("access_token"))
                                .andExpect(jsonPath("$.data.refreshToken").value("refresh_token"))
                                .andExpect(jsonPath("$.data.expiresIn").value(900));
        }

        @Test
        void refreshToken_success() throws Exception {
                RefreshTokenRequest request = new RefreshTokenRequest("refresh_token_string");
                TokenResponse tokenResponse = TokenResponse.builder()
                                .accessToken("new_access_token")
                                .refreshToken("refresh_token_string")
                                .expiresIn(900L)
                                .build();

                when(authService.refreshToken(request.refreshToken())).thenReturn(tokenResponse);

                mockMvc.perform(post("/api/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(200))
                                .andExpect(jsonPath("$.message").value("Token refreshed successfully"))
                                .andExpect(jsonPath("$.data.accessToken").value("new_access_token"));
        }

        @Test
        void logout_success() throws Exception {
                RefreshTokenRequest request = new RefreshTokenRequest("refresh_token_string");

                doNothing().when(authService).logout(request.refreshToken());

                mockMvc.perform(post("/api/auth/logout")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(200))
                                .andExpect(jsonPath("$.message").value("Logged out successfully"));
        }

        @Test
        void loginWithGoogle_success() throws Exception {
                GoogleAuthRequest request = new GoogleAuthRequest("valid_google_id_token");
                TokenResponse tokenResponse = TokenResponse.builder()
                                .accessToken("google_access_token")
                                .refreshToken("google_refresh_token")
                                .expiresIn(900L)
                                .build();

                when(authService.loginWithGoogle(eq(request), any(), any())).thenReturn(tokenResponse);

                mockMvc.perform(post("/api/auth/google")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(200))
                                .andExpect(jsonPath("$.message").value("Login successful"))
                                .andExpect(jsonPath("$.data.accessToken").value("google_access_token"));
        }

        @Test
        void forgotPassword_success() throws Exception {
                ForgotPasswordRequest request = new ForgotPasswordRequest("test@test.com");

                doNothing().when(authService).forgotPassword(any(ForgotPasswordRequest.class));

                mockMvc.perform(post("/api/auth/forgot-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(200))
                                .andExpect(jsonPath("$.message").value(
                                                "If your email is registered, a password reset link has been sent."));
        }

        @Test
        void resetPassword_success() throws Exception {
                ResetPasswordRequest request = new ResetPasswordRequest("valid_token", "NewPass123", "NewPass123");

                doNothing().when(authService).resetPassword(any(ResetPasswordRequest.class));

                mockMvc.perform(post("/api/auth/reset-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(200))
                                .andExpect(jsonPath("$.message").value(
                                                "Password reset successfully. All previous sessions have been revoked."));
        }
}
