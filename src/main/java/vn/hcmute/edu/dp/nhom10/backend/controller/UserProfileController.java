package vn.hcmute.edu.dp.nhom10.backend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.UpdateProfileRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.ChangePasswordRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ApiResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.UserProfileResponse;
import vn.hcmute.edu.dp.nhom10.backend.service.UserProfileService;

import java.security.Principal;
import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/customer/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "Quản lý thông tin cá nhân của khách hàng")
@Slf4j(topic = "PROFILE-CONTROLLER")
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping
    public ApiResponse getProfile(Principal principal) {
        log.info("Fetching profile for user: {}", principal.getName());
        UserProfileResponse profileResponse = userProfileService.getUserProfile(principal.getName());

        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Get user profile successfully!")
                .data(profileResponse)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @PutMapping
    public ApiResponse updateProfile(
            Principal principal,
            @RequestBody UpdateProfileRequest request) {

        log.info("Updating profile for user: {}", principal.getName());
        UserProfileResponse updatedProfile = userProfileService.updateUserProfile(principal.getName(), request);

        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Update profile successfully!")
                .data(updatedProfile)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @PutMapping("/password")
    public ApiResponse changePassword(
            Principal principal,
            @jakarta.validation.Valid @RequestBody ChangePasswordRequest request) {

        log.info("Changing password for user: {}", principal.getName());
        userProfileService.changePassword(principal.getName(), request);

        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Đổi mật khẩu thành công")
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
