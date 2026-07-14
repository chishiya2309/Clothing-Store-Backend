package vn.hcmute.edu.dp.nhom10.backend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ApiResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PublicFlashSaleResponse;
import vn.hcmute.edu.dp.nhom10.backend.service.FlashSaleQueryService;

import java.time.OffsetDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/guest/flash-sales")
@RequiredArgsConstructor
@Tag(name = "Public Flash Sale", description = "Public flash sale campaigns and products")
public class GuestFlashSaleController {

    private final FlashSaleQueryService flashSaleQueryService;

    @GetMapping("/current")
    public ApiResponse getCurrentCampaign() {
        Optional<PublicFlashSaleResponse> campaign = flashSaleQueryService.getCurrentOrUpcomingCampaign();
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message(campaign.isPresent()
                        ? "Current flash sale campaign retrieved successfully"
                        : "No active or upcoming flash sale campaign")
                .data(campaign.orElse(null))
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @GetMapping("/{campaignId}")
    public ApiResponse getCampaign(@PathVariable Long campaignId) {
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Flash sale campaign retrieved successfully")
                .data(flashSaleQueryService.getCampaign(campaignId))
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
