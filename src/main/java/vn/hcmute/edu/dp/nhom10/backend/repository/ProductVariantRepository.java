package vn.hcmute.edu.dp.nhom10.backend.repository;

import jakarta.persistence.LockModeType;
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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select pv
            from ProductVariant pv
            where pv.id in :ids
            order by pv.id
            """)
    List<ProductVariant> findAllByIdInForUpdate(@Param("ids") Collection<Long> ids);
}
