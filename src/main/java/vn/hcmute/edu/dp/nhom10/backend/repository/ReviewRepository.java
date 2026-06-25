package vn.hcmute.edu.dp.nhom10.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.dp.nhom10.backend.entity.Review;
import vn.hcmute.edu.dp.nhom10.backend.entity.Order;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT DISTINCT o FROM Order o " +
           "JOIN o.orderItems oi " +
           "JOIN oi.productVariant pv " +
           "WHERE o.user.id = :userId AND pv.product.id = :productId " +
           "AND o.status = :status " +
           "AND NOT EXISTS (SELECT 1 FROM Review r WHERE r.user.id = :userId AND r.product.id = :productId AND r.order.id = o.id)")
    List<Order> findEligibleOrdersForReview(
            @Param("userId") Long userId, 
            @Param("productId") Long productId,
            @Param("status") OrderStatus status);

    // Kiem tra nguoi dung da danh gia san pham nay cho don hang nay chua
    boolean existsByUserIdAndProductIdAndOrderId(Long userId, Long productId, Long orderId);

    // Kiem tra khach hang da tung mua san pham va don hang o trang thai hoan thanh
    @Query("SELECT COUNT(oi) > 0 FROM OrderItem oi " +
           "JOIN oi.order o JOIN oi.productVariant pv " +
           "WHERE o.user.id = :userId AND pv.product.id = :productId " +
           "AND o.status = :status")
    boolean hasPurchasedProduct(
            @Param("userId") Long userId, 
            @Param("productId") Long productId,
            @Param("status") OrderStatus status);

    // Lay danh sach review da duyet cua mot san pham (public)
    @Query("SELECT r FROM Review r " +
           "WHERE r.product.id = :productId AND r.isApproved = true AND r.isActive = true " +
           "ORDER BY r.createdAt DESC")
    Page<Review> findApprovedByProductId(@Param("productId") Long productId, Pageable pageable);

    // Loc danh sach review da duyet theo so sao
    @Query("SELECT r FROM Review r " +
           "WHERE r.product.id = :productId AND r.isApproved = true AND r.rating = :rating AND r.isActive = true " +
           "ORDER BY r.createdAt DESC")
    Page<Review> findApprovedByProductIdAndRating(
            @Param("productId") Long productId, 
            @Param("rating") Short rating, 
            Pageable pageable);

    // Loc danh sach review da duyet va co anh
    @Query("SELECT DISTINCT r FROM Review r " +
           "JOIN r.images img " +
           "WHERE r.product.id = :productId AND r.isApproved = true AND r.isActive = true " +
           "ORDER BY r.createdAt DESC")
    Page<Review> findApprovedWithImagesByProductId(@Param("productId") Long productId, Pageable pageable);

    // Thong ke phan bo so sao
    @Query("SELECT r.rating, COUNT(r) FROM Review r " +
           "WHERE r.product.id = :productId AND r.isApproved = true AND r.isActive = true " +
           "GROUP BY r.rating")
    List<Object[]> countRatingDistribution(@Param("productId") Long productId);

    // Tinh trung binh rating cua mot san pham tu cac review da duyet
    @Query("SELECT COALESCE(AVG(CAST(r.rating as double)), 0.0) FROM Review r " +
           "WHERE r.product.id = :productId AND r.isApproved = true AND r.isActive = true")
    Double calculateAverageRating(@Param("productId") Long productId);

    // Dem tong so review da duyet cua mot san pham
    long countByProductIdAndIsApprovedTrueAndIsActiveTrue(Long productId);

    boolean existsByProductId(Long productId);

    // Lay danh sach review chua duyet (Staff)
    Page<Review> findByIsApprovedFalseAndIsActiveTrue(Pageable pageable);

    // Lay danh sach review da duyet (Staff)
    Page<Review> findByIsApprovedTrueAndIsActiveTrue(Pageable pageable);

    // Lay danh sach review da xoa (Staff)
    Page<Review> findByIsActiveFalse(Pageable pageable);
}
