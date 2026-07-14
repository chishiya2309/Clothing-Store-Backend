package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.dto.request.CreateFlashSaleCampaignRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.UpdateFlashSaleCampaignRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.UpsertFlashSaleItemRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.FlashSaleCampaignResponse;

import java.util.List;

public interface FlashSaleCampaignService {
    FlashSaleCampaignResponse createCampaign(CreateFlashSaleCampaignRequest request);
    FlashSaleCampaignResponse updateCampaign(Long campaignId, UpdateFlashSaleCampaignRequest request);
    FlashSaleCampaignResponse updateActivation(Long campaignId, boolean isActive);
    FlashSaleCampaignResponse getCampaign(Long campaignId);
    List<FlashSaleCampaignResponse> getCampaigns();
    FlashSaleCampaignResponse addItem(Long campaignId, UpsertFlashSaleItemRequest request);
    FlashSaleCampaignResponse updateItem(Long campaignId, Long itemId, UpsertFlashSaleItemRequest request);
    FlashSaleCampaignResponse removeItem(Long campaignId, Long itemId);
}
