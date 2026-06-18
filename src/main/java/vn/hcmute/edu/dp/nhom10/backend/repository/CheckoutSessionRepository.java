package vn.hcmute.edu.dp.nhom10.backend.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.dp.nhom10.backend.entity.CheckoutSession;

import java.util.Optional;

@Repository
public interface CheckoutSessionRepository extends JpaRepository<CheckoutSession, Long> {
    Optional<CheckoutSession> findByCheckoutCode(String checkoutCode);
    boolean existsByCheckoutCode(String checkoutCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select cs
            from CheckoutSession cs
            where cs.id = :id
            """)
    Optional<CheckoutSession> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select cs
            from CheckoutSession cs
            where cs.checkoutCode = :checkoutCode
            """)
    Optional<CheckoutSession> findByCheckoutCodeForUpdate(@Param("checkoutCode") String checkoutCode);
}
