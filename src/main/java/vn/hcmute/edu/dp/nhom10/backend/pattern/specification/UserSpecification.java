package vn.hcmute.edu.dp.nhom10.backend.pattern.specification;

import org.springframework.data.jpa.domain.Specification;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.enums.UserRole;

public final class UserSpecification {

    private UserSpecification() {
    }

    /**
     * Tìm kiếm người dùng (keyword)
     */
    public static Specification<User> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }

            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("fullName")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern),
                    cb.like(root.get("phone"), pattern)
            );
        };
    }

    /**
     * Lọc người dùng dựa trên vai trò (UserRole).
     */
    public static Specification<User> hasRole(UserRole role) {
        return (root, query, cb) -> {
            if (role == null) {
                return cb.conjunction();
            }

            return cb.equal(root.get("role"), role);
        };
    }

    /**
     * Lọc người dùng dựa trên trạng thái kích hoạt (isActive).
     */
    public static Specification<User> hasActiveStatus(Boolean isActive) {
        return (root, query, cb) -> {
            if (isActive == null) {
                return cb.conjunction();
            }

            return cb.equal(root.get("isActive"), isActive);
        };
    }
}
