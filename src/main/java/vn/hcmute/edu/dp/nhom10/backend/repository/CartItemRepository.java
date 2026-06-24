package vn.hcmute.edu.dp.nhom10.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.dp.nhom10.backend.entity.CartItem;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByUserIdAndProductVariantId(Long userId, Long productVariantId);
    List<CartItem> findAllByUserId(Long userId);

    @Query("SELECT COUNT(ci) > 0 FROM CartItem ci WHERE ci.productVariant.product.id = :productId")
    boolean existsByProductVariantProductId(@Param("productId") Long productId);

    @Query("""
            select ci
            from CartItem ci
            join fetch ci.productVariant pv
            join fetch pv.product p
            where ci.user.id = :userId
            order by ci.id
            """)
    List<CartItem> findCheckoutItemsByUserId(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from CartItem ci
            where ci.user.id = :userId
              and ci.productVariant.id in :productVariantIds
            """)
    int deletePurchasedItems(
            @Param("userId") Long userId,
            @Param("productVariantIds") Collection<Long> productVariantIds
    );

    void deleteAllByUserId(Long userId);
}
