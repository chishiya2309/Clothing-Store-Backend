package vn.hcmute.edu.dp.nhom10.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.dp.nhom10.backend.entity.FlashSaleCampaign;

import java.util.List;
import java.time.OffsetDateTime;

@Repository
public interface FlashSaleCampaignRepository extends JpaRepository<FlashSaleCampaign, Long> {
    List<FlashSaleCampaign> findAllByOrderByStartAtDesc();

    @Query("""
            select campaign
            from FlashSaleCampaign campaign
            where campaign.isActive = true
              and campaign.startAt <= :now
              and campaign.endAt > :now
            order by campaign.startAt desc, campaign.id desc
            """)
    List<FlashSaleCampaign> findActiveAt(@Param("now") OffsetDateTime now, Pageable pageable);

    @Query("""
            select campaign
            from FlashSaleCampaign campaign
            where campaign.isActive = true
              and campaign.startAt > :now
            order by campaign.startAt asc, campaign.id asc
            """)
    List<FlashSaleCampaign> findUpcomingAfter(@Param("now") OffsetDateTime now, Pageable pageable);
}
