package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.dto.response.PublicFlashSaleResponse;

import java.util.Optional;

public interface FlashSaleQueryService {
    Optional<PublicFlashSaleResponse> getCurrentOrUpcomingCampaign();
    PublicFlashSaleResponse getCampaign(Long campaignId);
}
