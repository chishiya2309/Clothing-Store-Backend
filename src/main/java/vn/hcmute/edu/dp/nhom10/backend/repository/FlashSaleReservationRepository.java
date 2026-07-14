package vn.hcmute.edu.dp.nhom10.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
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
}
