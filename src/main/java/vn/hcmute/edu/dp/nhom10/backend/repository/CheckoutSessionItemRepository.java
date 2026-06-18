package vn.hcmute.edu.dp.nhom10.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.dp.nhom10.backend.entity.CheckoutSessionItem;

import java.util.List;

@Repository
public interface CheckoutSessionItemRepository extends JpaRepository<CheckoutSessionItem, Long> {
    List<CheckoutSessionItem> findAllByCheckoutSessionId(Long checkoutSessionId);
}
