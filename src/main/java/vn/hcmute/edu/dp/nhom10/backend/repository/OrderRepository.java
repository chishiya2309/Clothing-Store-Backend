package vn.hcmute.edu.dp.nhom10.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.dp.nhom10.backend.entity.Order;
import vn.hcmute.edu.dp.nhom10.backend.dto.projection.RevenueReportProjection;
import vn.hcmute.edu.dp.nhom10.backend.dto.projection.BestsellerReportProjection;
import vn.hcmute.edu.dp.nhom10.backend.dto.projection.LoyaltyCustomerReportProjection;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository cung cấp các phương thức truy xuất dữ liệu gộp nhóm từ bảng orders.
 * Định nghĩa các câu truy vấn JPQL phức tạp gộp nhóm theo thời gian và liên kết thực thể
 *          để kết xuất trực tiếp các biểu mẫu thống kê doanh số, bán chạy và khách hàng thân thiết
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    boolean existsByOrderCode(String orderCode);

    // Customer order history: paginated, ordered newest first
    @Query("SELECT o FROM Order o JOIN FETCH o.orderItems oi " +
           "WHERE o.user.email = :email " +
           "ORDER BY o.createdAt DESC")
    Page<Order> findByUserEmail(
            @Param("email") String email,
            Pageable pageable);

    // Customer order history with status filter: paginated, ordered newest first
    @Query("SELECT o FROM Order o JOIN FETCH o.orderItems oi " +
           "WHERE o.user.email = :email " +
           "AND o.status = :status " +
           "ORDER BY o.createdAt DESC")
    Page<Order> findByUserEmailAndStatus(
            @Param("email") String email,
            @Param("status") OrderStatus status,
            Pageable pageable);

    Optional<Order> findByOrderCodeAndUserEmail(String orderCode, String email);
    Optional<Order> findByOrderCode(String orderCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT o
            FROM Order o
            WHERE o.orderCode = :orderCode
            """)
    Optional<Order> findByOrderCodeForUpdate(@Param("orderCode") String orderCode);

    @Query(value = """
            SELECT
                o.created_at::date                                                          AS date,
                COUNT(o.id)                                                                 AS totalOrders,
                COUNT(CASE WHEN o.status = 'completed'::order_status THEN 1 END)            AS completedOrders,
                COUNT(CASE WHEN o.status = 'cancelled'::order_status THEN 1 END)            AS cancelledOrders,
                SUM(CASE WHEN o.status = 'completed'::order_status THEN (o.subtotal + o.shipping_fee) END) AS totalRevenue,
                SUM(CASE WHEN o.status = 'completed'::order_status THEN o.discount_amount END)             AS totalDiscounts,
                SUM(CASE WHEN o.status = 'completed'::order_status THEN o.total_amount END)                AS netRevenue
            FROM orders o
            WHERE o.created_at BETWEEN :startDate AND :endDate
            GROUP BY o.created_at::date
            ORDER BY o.created_at::date ASC
            """, nativeQuery = true)
    List<RevenueReportProjection> findRevenueReport(
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate);

    @Query(value = """
            SELECT
                p.id                                                                        AS productId,
                p.name                                                                      AS productName,
                CASE WHEN cp.id IS NULL THEN c.name ELSE (cp.name || ' > ' || c.name) END  AS categoryName,
                SUM(oi.quantity)                                                            AS totalQuantitySold,
                SUM(oi.subtotal)                                                            AS totalRevenue
            FROM order_items oi
            JOIN product_variants pv ON pv.id = oi.product_variant_id
            JOIN products p          ON p.id  = pv.product_id AND p.deleted_at IS NULL
            JOIN categories c        ON c.id  = p.category_id
            LEFT JOIN categories cp  ON cp.id = c.parent_id
            JOIN orders o            ON o.id  = oi.order_id
            WHERE o.status = 'completed'::order_status
              AND o.created_at BETWEEN :startDate AND :endDate
            GROUP BY p.id, p.name, c.name, cp.id, cp.name
            ORDER BY SUM(oi.quantity) DESC
            """, nativeQuery = true)
    List<BestsellerReportProjection> findBestsellingProducts(
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate);

    @Query(value = """
            SELECT
                u.id                                 AS userId,
                u.full_name                          AS fullName,
                u.email                              AS email,
                COALESCE(mt.name, 'Kh\u00f4ng c\u00f3')        AS membershipTier,
                COUNT(o.id)                          AS totalOrders,
                SUM(o.total_amount)                  AS totalSpent,
                u.loyalty_points                     AS loyaltyPoints
            FROM orders o
            JOIN users u         ON u.id  = o.user_id
            LEFT JOIN membership_tiers mt ON mt.id = u.membership_tier_id
            WHERE o.status = 'completed'::order_status
              AND o.created_at BETWEEN :startDate AND :endDate
            GROUP BY u.id, u.full_name, u.email, mt.name, u.loyalty_points
            ORDER BY SUM(o.total_amount) DESC
            """, nativeQuery = true)
    List<LoyaltyCustomerReportProjection> findLoyaltyCustomers(
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate);
}
