package vn.hcmute.edu.dp.nhom10.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.dp.nhom10.backend.entity.FlashSaleItem;

import java.util.List;
import java.util.Optional;
import java.time.OffsetDateTime;

@Repository
public interface FlashSaleItemRepository extends JpaRepository<FlashSaleItem, Long> {
    List<FlashSaleItem> findAllByCampaignIdOrderByIdAsc(Long campaignId);
    Optional<FlashSaleItem> findByIdAndCampaignId(Long id, Long campaignId);
    Optional<FlashSaleItem> findByCampaignIdAndProductId(Long campaignId, Long productId);
    boolean existsByCampaignIdAndProductId(Long campaignId, Long productId);

    @Query("""
            select item
            from FlashSaleItem item
            join fetch item.product product
            where item.campaign.id = :campaignId
              and product.isActive = true
              and product.deletedAt is null
            order by item.id asc
            """)
    List<FlashSaleItem> findPublicItemsByCampaignId(@Param("campaignId") Long campaignId);

    @Query("""
            select count(item) > 0
            from FlashSaleItem item
            join item.campaign campaign
            where item.product.id = :productId
              and campaign.id <> :campaignId
              and campaign.isActive = true
              and campaign.startAt < :endAt
              and campaign.endAt > :startAt
            """)
    boolean existsActiveOverlap(
            @Param("productId") Long productId,
            @Param("campaignId") Long campaignId,
            @Param("startAt") OffsetDateTime startAt,
            @Param("endAt") OffsetDateTime endAt
    );
}
