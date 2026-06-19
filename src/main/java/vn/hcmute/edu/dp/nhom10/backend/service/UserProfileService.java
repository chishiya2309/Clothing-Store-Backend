package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.dto.request.UpdateProfileRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.ChangePasswordRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.UserProfileResponse;

public interface UserProfileService {
    UserProfileResponse getUserProfile(String email);
    UserProfileResponse updateUserProfile(String email, UpdateProfileRequest request);
    void changePassword(String email, ChangePasswordRequest request);
}
