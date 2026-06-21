package vn.hcmute.edu.dp.nhom10.backend.service;

import org.springframework.data.domain.Page;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.AdminUserResponse;
import vn.hcmute.edu.dp.nhom10.backend.enums.UserRole;

public interface AdminUserService {
    //Lấy danh sách user phân trang (kết hợp tìm kiếm động)
    Page<AdminUserResponse> getUsers(int page, int size, String keyword, UserRole role, Boolean isActive);

    //Khóa/mở khóa
    AdminUserResponse updateUserStatus(Long id, Boolean isActive);

    //Cập nhật role
    AdminUserResponse updateUserRole(Long id, UserRole role);

}
