package vn.hcmute.edu.dp.nhom10.backend.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findByProductId(Long productId);
    Optional<ProductVariant> findByIdAndIsActiveTrue(Long id);
    Optional<ProductVariant> findByProductIdAndSizeIgnoreCaseAndColorIgnoreCaseAndIsActiveTrue(Long productId, String size, String color);

    @Query("""
            select pv
            from ProductVariant pv
            join pv.product p
            join p.category c
            where pv.isActive = true
              and p.isActive = true
              and p.deletedAt is null
              and (:categoryId is null or c.id = :categoryId)
              and (:minStock is null or pv.stockQuantity >= :minStock)
              and (:maxStock is null or pv.stockQuantity <= :maxStock)
              and (
                  :keyword is null
                  or lower(p.name) like lower(concat('%', :keyword, '%'))
                  or lower(p.slug) like lower(concat('%', :keyword, '%'))
                  or lower(pv.sku) like lower(concat('%', :keyword, '%'))
              )
            """)
    Page<ProductVariant> findInventoryReport(
            @Param("categoryId") Long categoryId,
            @Param("keyword") String keyword,
            @Param("minStock") Integer minStock,
            @Param("maxStock") Integer maxStock,
            Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select pv
            from ProductVariant pv
            where pv.id in :ids
            order by pv.id
            """)
    List<ProductVariant> findAllByIdInForUpdate(@Param("ids") Collection<Long> ids);
}
