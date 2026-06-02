package vn.hcmute.edu.dp.nhom10.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.RegisterRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.ResendVerificationRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ApiResponse;
import vn.hcmute.edu.dp.nhom10.backend.service.AuthService;

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
        log.info("Verifying email with token: {}", token);
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
}
