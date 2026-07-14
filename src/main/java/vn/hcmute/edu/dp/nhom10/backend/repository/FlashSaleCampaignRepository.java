package vn.hcmute.edu.dp.nhom10.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.dp.nhom10.backend.entity.FlashSaleCampaign;

import java.util.List;

@Repository
public interface FlashSaleCampaignRepository extends JpaRepository<FlashSaleCampaign, Long> {
    List<FlashSaleCampaign> findAllByOrderByStartAtDesc();
}
