package vn.hcmute.edu.dp.nhom10.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.dp.nhom10.backend.entity.CheckoutSessionItem;

import java.util.List;

@Repository
public interface CheckoutSessionItemRepository extends JpaRepository<CheckoutSessionItem, Long> {
    List<CheckoutSessionItem> findAllByCheckoutSessionId(Long checkoutSessionId);

    @Query("""
            select csi
            from CheckoutSessionItem csi
            join fetch csi.productVariant pv
            where csi.checkoutSession.id = :checkoutSessionId
            order by csi.id
            """)
    List<CheckoutSessionItem> findAllByCheckoutSessionIdWithVariant(
            @Param("checkoutSessionId") Long checkoutSessionId
    );

    @Query("SELECT COUNT(csi) > 0 FROM CheckoutSessionItem csi WHERE csi.productVariant.product.id = :productId")
    boolean existsByProductVariantProductId(@Param("productId") Long productId);
}
