package vn.hcmute.edu.dp.nhom10.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.dp.nhom10.backend.entity.PaymentAttempt;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, Long> {
    Optional<PaymentAttempt> findByPaymentReference(String paymentReference);
    boolean existsByPaymentReference(String paymentReference);
    List<PaymentAttempt> findAllByCheckoutSessionId(Long checkoutSessionId);
}
