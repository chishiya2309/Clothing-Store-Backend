package vn.hcmute.edu.dp.nhom10.backend.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.dp.nhom10.backend.entity.VoucherReservation;
import vn.hcmute.edu.dp.nhom10.backend.enums.ReservationStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.VoucherSlot;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherReservationRepository extends JpaRepository<VoucherReservation, Long> {
    Optional<VoucherReservation> findByCheckoutSessionId(Long checkoutSessionId);
    Optional<VoucherReservation> findByCheckoutSessionIdAndStatus(Long checkoutSessionId, ReservationStatus status);
    List<VoucherReservation> findAllByCheckoutSessionId(Long checkoutSessionId);
    List<VoucherReservation> findAllByCheckoutSessionIdAndStatus(Long checkoutSessionId, ReservationStatus status);
    Optional<VoucherReservation> findByCheckoutSessionIdAndVoucherSlot(Long checkoutSessionId, VoucherSlot voucherSlot);

    boolean existsByCheckoutSession_Id(Long checkoutSessionId);
    boolean existsByCheckoutSession_IdAndVoucherSlot(Long checkoutSessionId, VoucherSlot voucherSlot);

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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select vr
            from VoucherReservation vr
            where vr.checkoutSession.id = :checkoutSessionId
            """)
    List<VoucherReservation> findAllByCheckoutSessionIdForUpdate(
            @Param("checkoutSessionId") Long checkoutSessionId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select vr
            from VoucherReservation vr
            where vr.checkoutSession.id = :checkoutSessionId
              and vr.voucherSlot = :voucherSlot
            """)
    Optional<VoucherReservation> findByCheckoutSessionIdAndVoucherSlotForUpdate(
            @Param("checkoutSessionId") Long checkoutSessionId,
            @Param("voucherSlot") VoucherSlot voucherSlot
    );
}
