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
import vn.hcmute.edu.dp.nhom10.backend.dto.response.RevenueReportResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.BestsellerReportResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.LoyaltyCustomerReportResponse;
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

    @Query("SELECT new vn.hcmute.edu.dp.nhom10.backend.dto.response.RevenueReportResponse(" +
           "  CAST(o.createdAt AS date), " +
           "  COUNT(o.id), " +
           "  COUNT(CASE WHEN o.status = vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus.completed THEN 1L END), " +
           "  COUNT(CASE WHEN o.status = vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus.cancelled THEN 1L END), " +
           "  SUM(CASE WHEN o.status = vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus.completed THEN (o.subtotal + o.shippingFee) END), " +
           "  SUM(CASE WHEN o.status = vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus.completed THEN o.discountAmount END), " +
           "  SUM(CASE WHEN o.status = vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus.completed THEN o.totalAmount END)) " +
           "FROM Order o " +
           "WHERE o.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY CAST(o.createdAt AS date) " +
           "ORDER BY CAST(o.createdAt AS date) ASC")
    List<RevenueReportResponse> findRevenueReport(
            @Param("startDate") OffsetDateTime startDate, 
            @Param("endDate") OffsetDateTime endDate);

    @Query("SELECT new vn.hcmute.edu.dp.nhom10.backend.dto.response.BestsellerReportResponse(" +
           "  p.id, p.name, " +
           "  CASE WHEN cp.id IS NULL THEN c.name ELSE CONCAT(cp.name, ' > ', c.name) END, " +
           "  SUM(oi.quantity), SUM(oi.subtotal)) " +
           "FROM OrderItem oi " +
           "JOIN oi.productVariant pv " +
           "JOIN pv.product p " +
           "JOIN p.category c " +
           "LEFT JOIN c.parent cp " +
           "JOIN oi.order o " +
           "WHERE o.status = vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus.completed " +
           "  AND o.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY p.id, p.name, c.name, cp.id, cp.name " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<BestsellerReportResponse> findBestsellingProducts(
            @Param("startDate") OffsetDateTime startDate, 
            @Param("endDate") OffsetDateTime endDate);

    @Query("SELECT new vn.hcmute.edu.dp.nhom10.backend.dto.response.LoyaltyCustomerReportResponse(" +
           "  u.id, u.fullName, u.email, COALESCE(mt.name, 'Không có'), COUNT(o.id), SUM(o.totalAmount), u.loyaltyPoints) " +
           "FROM Order o " +
           "JOIN o.user u " +
           "LEFT JOIN u.membershipTier mt " +
           "WHERE o.status = vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus.completed " +
           "  AND o.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY u.id, u.fullName, u.email, mt.name, u.loyaltyPoints " +
           "ORDER BY SUM(o.totalAmount) DESC")
    List<LoyaltyCustomerReportResponse> findLoyaltyCustomers(
            @Param("startDate") OffsetDateTime startDate, 
            @Param("endDate") OffsetDateTime endDate);
}
