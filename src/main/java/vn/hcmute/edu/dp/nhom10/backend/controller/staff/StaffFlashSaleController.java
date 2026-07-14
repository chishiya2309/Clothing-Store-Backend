package vn.hcmute.edu.dp.nhom10.backend.controller.staff;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.CreateFlashSaleCampaignRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.UpdateFlashSaleActivationRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.UpdateFlashSaleCampaignRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.UpsertFlashSaleItemRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ApiResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.FlashSaleCampaignResponse;
import vn.hcmute.edu.dp.nhom10.backend.service.FlashSaleCampaignService;

import java.time.OffsetDateTime;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/staff/flash-sales")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class StaffFlashSaleController {

    private final FlashSaleCampaignService flashSaleCampaignService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse createCampaign(@Valid @RequestBody CreateFlashSaleCampaignRequest request) {
        return response(HttpStatus.CREATED, "Flash sale campaign created successfully",
                flashSaleCampaignService.createCampaign(request));
    }

    @GetMapping
    public ApiResponse getCampaigns() {
        return response(HttpStatus.OK, "Flash sale campaigns retrieved successfully",
                new ArrayList<>(flashSaleCampaignService.getCampaigns()));
    }

    @GetMapping("/{campaignId}")
    public ApiResponse getCampaign(@PathVariable Long campaignId) {
        return response(HttpStatus.OK, "Flash sale campaign retrieved successfully",
                flashSaleCampaignService.getCampaign(campaignId));
    }

    @PutMapping("/{campaignId}")
    public ApiResponse updateCampaign(
            @PathVariable Long campaignId,
            @Valid @RequestBody UpdateFlashSaleCampaignRequest request
    ) {
        return response(HttpStatus.OK, "Flash sale campaign updated successfully",
                flashSaleCampaignService.updateCampaign(campaignId, request));
    }

    @PatchMapping("/{campaignId}/activation")
    public ApiResponse updateActivation(
            @PathVariable Long campaignId,
            @Valid @RequestBody UpdateFlashSaleActivationRequest request
    ) {
        return response(HttpStatus.OK, "Flash sale campaign activation updated successfully",
                flashSaleCampaignService.updateActivation(campaignId, request.isActive()));
    }

    @PostMapping("/{campaignId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse addItem(
            @PathVariable Long campaignId,
            @Valid @RequestBody UpsertFlashSaleItemRequest request
    ) {
        return response(HttpStatus.CREATED, "Flash sale item added successfully",
                flashSaleCampaignService.addItem(campaignId, request));
    }

    @PutMapping("/{campaignId}/items/{itemId}")
    public ApiResponse updateItem(
            @PathVariable Long campaignId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpsertFlashSaleItemRequest request
    ) {
        return response(HttpStatus.OK, "Flash sale item updated successfully",
                flashSaleCampaignService.updateItem(campaignId, itemId, request));
    }

    @DeleteMapping("/{campaignId}/items/{itemId}")
    public ApiResponse removeItem(@PathVariable Long campaignId, @PathVariable Long itemId) {
        return response(HttpStatus.OK, "Flash sale item removed successfully",
                flashSaleCampaignService.removeItem(campaignId, itemId));
    }

    private ApiResponse response(HttpStatus status, String message, Object data) {
        return ApiResponse.builder()
                .status(status.value())
                .message(message)
                .data(data)
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
