package vn.hcmute.edu.dp.nhom10.backend.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.dp.nhom10.backend.entity.VoucherReservation;
import vn.hcmute.edu.dp.nhom10.backend.enums.ReservationStatus;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface VoucherReservationRepository extends JpaRepository<VoucherReservation, Long> {
    Optional<VoucherReservation> findByCheckoutSessionId(Long checkoutSessionId);
    Optional<VoucherReservation> findByCheckoutSessionIdAndStatus(Long checkoutSessionId, ReservationStatus status);

    boolean existsByCheckoutSession_Id(Long checkoutSessionId);

    @Query("""
            select count(vr)
            from VoucherReservation vr
            where vr.voucher.id = :voucherId
              and vr.status = :status
              and vr.expiresAt > :now
            """)
    long countActiveReservations(
            @Param("voucherId") Long voucherId,
            @Param("status") ReservationStatus status,
            @Param("now") OffsetDateTime now
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select vr
            from VoucherReservation vr
            where vr.checkoutSession.id = :checkoutSessionId
            """)
    Optional<VoucherReservation> findByCheckoutSessionIdForUpdate(
            @Param("checkoutSessionId") Long checkoutSessionId
    );
}
