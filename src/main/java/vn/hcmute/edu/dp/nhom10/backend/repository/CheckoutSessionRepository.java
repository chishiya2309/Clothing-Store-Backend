package vn.hcmute.edu.dp.nhom10.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.dp.nhom10.backend.entity.CheckoutSession;

import java.util.Optional;

@Repository
public interface CheckoutSessionRepository extends JpaRepository<CheckoutSession, Long> {
    Optional<CheckoutSession> findByCheckoutCode(String checkoutCode);
    boolean existsByCheckoutCode(String checkoutCode);
}
