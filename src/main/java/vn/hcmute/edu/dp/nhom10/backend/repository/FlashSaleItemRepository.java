package vn.hcmute.edu.dp.nhom10.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.dp.nhom10.backend.entity.FlashSaleItem;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlashSaleItemRepository extends JpaRepository<FlashSaleItem, Long> {
    List<FlashSaleItem> findAllByCampaignIdOrderByIdAsc(Long campaignId);
    Optional<FlashSaleItem> findByIdAndCampaignId(Long id, Long campaignId);
    Optional<FlashSaleItem> findByCampaignIdAndProductId(Long campaignId, Long productId);
    boolean existsByCampaignIdAndProductId(Long campaignId, Long productId);
}
