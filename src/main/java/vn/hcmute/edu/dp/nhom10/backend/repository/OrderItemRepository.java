package vn.hcmute.edu.dp.nhom10.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.hcmute.edu.dp.nhom10.backend.entity.OrderItem;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @Query("""
            select oi
            from OrderItem oi
            join fetch oi.productVariant pv
            where oi.order.id = :orderId
            order by oi.id
            """)
    List<OrderItem> findAllByOrderIdWithVariantOrderById(
            @Param("orderId") Long orderId
    );

    @Query(value = """
            SELECT oi2.productVariant.product.id
            FROM OrderItem oi1
            JOIN OrderItem oi2 ON oi1.order.id = oi2.order.id
            WHERE oi1.productVariant.product.id = :productId
              AND oi2.productVariant.product.id != :productId
            GROUP BY oi2.productVariant.product.id
            ORDER BY COUNT(oi2.productVariant.product.id) DESC
            LIMIT :limit
            """)
    List<Long> findCoPurchasedProductIds(@Param("productId") Long productId, @Param("limit") int limit);

    List<OrderItem> findByOrderIdAndProductVariantProductId(Long orderId, Long productId);
}
