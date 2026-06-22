package vn.hcmute.edu.dp.nhom10.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
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
}
