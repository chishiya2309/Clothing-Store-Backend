package vn.hcmute.edu.dp.nhom10.backend.pattern.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import vn.hcmute.edu.dp.nhom10.backend.entity.Order;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;

import java.time.OffsetDateTime;

public final class OrderSpecification {

    private OrderSpecification() {
    }

    public static Specification<Order> hasStatus(OrderStatus status) {
        return (root, query, cb) -> status == null
                ? cb.conjunction()
                : cb.equal(root.get("status"), status);
    }

    public static Specification<Order> createdAtGreaterThanOrEqualTo(OffsetDateTime fromDateTime) {
        return (root, query, cb) -> fromDateTime == null
                ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("createdAt"), fromDateTime);
    }

    public static Specification<Order> createdAtLessThan(OffsetDateTime toDateTimeExclusive) {
        return (root, query, cb) -> toDateTimeExclusive == null
                ? cb.conjunction()
                : cb.lessThan(root.get("createdAt"), toDateTimeExclusive);
    }

    public static Specification<Order> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }

            String pattern = "%" + escapeLike(keyword.trim().toLowerCase()) + "%";
            Join<Order, User> userJoin = root.join("user", JoinType.LEFT);
            return cb.or(
                    cb.like(cb.lower(root.get("orderCode")), pattern, '\\'),
                    cb.like(cb.lower(root.get("shippingPhone")), pattern, '\\'),
                    cb.like(cb.lower(userJoin.get("fullName")), pattern, '\\'),
                    cb.like(cb.lower(userJoin.get("email")), pattern, '\\'),
                    cb.like(cb.lower(userJoin.get("phone")), pattern, '\\')
            );
        };
    }

    private static String escapeLike(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
