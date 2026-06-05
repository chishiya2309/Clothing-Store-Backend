package vn.hcmute.edu.dp.nhom10.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.RegisterRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.LoginRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.RefreshTokenRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.ResendVerificationRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ApiResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.TokenResponse;
import vn.hcmute.edu.dp.nhom10.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;

import java.time.OffsetDateTime;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Xử lý xác thực")
@Slf4j(topic = "AUTH-CONTROLLER")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registering new user with email: {}", request.email());
        authService.register(request);
        return ApiResponse.builder()
                .status(HttpStatus.CREATED.value())
                .message("Registration successful. Please check your email to verify your account.")
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @GetMapping("/verify-email")
    public ApiResponse verifyEmail(@RequestParam("token") String token) {
        log.info("Verifying email");
        authService.verifyEmail(token);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Email verified successfully")
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @PostMapping("/resend-verification")
    public ApiResponse resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        log.info("Resending verification email to: {}", request.email());
        authService.resendVerificationEmail(request.email());
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Verification email resent successfully")
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @PostMapping("/login")
    public ApiResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        log.info("User login attempt with email: {}", request.email());
        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");

        TokenResponse tokenResponse = authService.login(request, ipAddress, userAgent);

        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Login successful")
                .data(tokenResponse)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @PostMapping("/refresh")
    public ApiResponse refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Refreshing token");
        TokenResponse tokenResponse = authService.refreshToken(request.refreshToken());

        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Token refreshed successfully")
                .data(tokenResponse)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse logout(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Logging out user");
        authService.logout(request.refreshToken());

        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Logged out successfully")
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
