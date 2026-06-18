package vn.hcmute.edu.dp.nhom10.backend.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.dp.nhom10.backend.entity.PaymentAttempt;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, Long> {
    Optional<PaymentAttempt> findByPaymentReference(String paymentReference);
    boolean existsByPaymentReference(String paymentReference);
    List<PaymentAttempt> findAllByCheckoutSessionId(Long checkoutSessionId);
    Optional<PaymentAttempt> findTopByCheckoutSession_IdOrderByCreatedAtDesc(Long checkoutSessionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select pa
            from PaymentAttempt pa
            where pa.paymentReference = :paymentReference
            """)
    Optional<PaymentAttempt> findByPaymentReferenceForUpdate(
            @Param("paymentReference") String paymentReference
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select pa
            from PaymentAttempt pa
            where pa.checkoutSession.id = :checkoutSessionId
            order by pa.id
            """)
    List<PaymentAttempt> findAllByCheckoutSessionIdForUpdate(
            @Param("checkoutSessionId") Long checkoutSessionId
    );
}
