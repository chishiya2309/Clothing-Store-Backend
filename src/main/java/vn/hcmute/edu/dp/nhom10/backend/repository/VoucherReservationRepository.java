package vn.hcmute.edu.dp.nhom10.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.dp.nhom10.backend.entity.VoucherReservation;
import vn.hcmute.edu.dp.nhom10.backend.enums.ReservationStatus;

import java.util.Optional;

@Repository
public interface VoucherReservationRepository extends JpaRepository<VoucherReservation, Long> {
    Optional<VoucherReservation> findByCheckoutSessionId(Long checkoutSessionId);
    Optional<VoucherReservation> findByCheckoutSessionIdAndStatus(Long checkoutSessionId, ReservationStatus status);
}
