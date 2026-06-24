package vn.hcmute.edu.dp.nhom10.backend.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.dp.nhom10.backend.entity.InventoryReservation;
import vn.hcmute.edu.dp.nhom10.backend.enums.ReservationStatus;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Long> {
    List<InventoryReservation> findAllByCheckoutSessionId(Long checkoutSessionId);
    List<InventoryReservation> findAllByCheckoutSessionIdAndStatus(Long checkoutSessionId, ReservationStatus status);

    boolean existsByCheckoutSession_Id(Long checkoutSessionId);

    @Query("""
            select coalesce(sum(ir.quantity), 0)
            from InventoryReservation ir
            where ir.productVariant.id = :productVariantId
              and ir.status = :status
              and ir.expiresAt > :now
            """)
    Long sumReservedQuantity(
            @Param("productVariantId") Long productVariantId,
            @Param("status") ReservationStatus status,
            @Param("now") OffsetDateTime now
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select ir
            from InventoryReservation ir
            where ir.checkoutSession.id = :checkoutSessionId
            order by ir.productVariant.id
            """)
    List<InventoryReservation> findAllByCheckoutSessionIdForUpdate(
            @Param("checkoutSessionId") Long checkoutSessionId
    );

    @Query("SELECT COUNT(ir) > 0 FROM InventoryReservation ir WHERE ir.productVariant.product.id = :productId")
    boolean existsByProductVariantProductId(@Param("productId") Long productId);
}
