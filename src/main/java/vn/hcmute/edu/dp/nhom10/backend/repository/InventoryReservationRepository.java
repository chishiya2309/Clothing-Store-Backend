package vn.hcmute.edu.dp.nhom10.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.dp.nhom10.backend.entity.InventoryReservation;
import vn.hcmute.edu.dp.nhom10.backend.enums.ReservationStatus;

import java.util.List;

@Repository
public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Long> {
    List<InventoryReservation> findAllByCheckoutSessionId(Long checkoutSessionId);
    List<InventoryReservation> findAllByCheckoutSessionIdAndStatus(Long checkoutSessionId, ReservationStatus status);
}
