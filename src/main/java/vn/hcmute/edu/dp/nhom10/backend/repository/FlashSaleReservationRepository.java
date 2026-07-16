package vn.hcmute.edu.dp.nhom10.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.hcmute.edu.dp.nhom10.backend.entity.FlashSaleReservation;
import vn.hcmute.edu.dp.nhom10.backend.enums.ReservationStatus;

import java.util.List;

@Repository
public interface FlashSaleReservationRepository extends JpaRepository<FlashSaleReservation, Long> {
    List<FlashSaleReservation> findAllByCheckoutSessionId(Long checkoutSessionId);
    List<FlashSaleReservation> findAllByCheckoutSessionIdAndStatus(
            Long checkoutSessionId,
            ReservationStatus status
    );
    boolean existsByCheckoutSessionId(Long checkoutSessionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from FlashSaleReservation r where r.checkoutSession.id = :checkoutSessionId order by r.flashSaleItem.id")
    List<FlashSaleReservation> findAllByCheckoutSessionIdForUpdate(@Param("checkoutSessionId") Long checkoutSessionId);
}
