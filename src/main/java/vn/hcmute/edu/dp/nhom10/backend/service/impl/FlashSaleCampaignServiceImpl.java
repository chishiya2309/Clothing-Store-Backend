package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.CreateFlashSaleCampaignRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.UpdateFlashSaleCampaignRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.UpsertFlashSaleItemRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.FlashSaleCampaignResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.FlashSaleItemResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.FlashSaleCampaign;
import vn.hcmute.edu.dp.nhom10.backend.entity.FlashSaleItem;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;
import vn.hcmute.edu.dp.nhom10.backend.enums.FlashSaleStatus;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.repository.FlashSaleCampaignRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.FlashSaleItemRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.FlashSaleCampaignService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlashSaleCampaignServiceImpl implements FlashSaleCampaignService {

    private final FlashSaleCampaignRepository campaignRepository;
    private final FlashSaleItemRepository itemRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public FlashSaleCampaignResponse createCampaign(CreateFlashSaleCampaignRequest request) {
        validateTimeRange(request.startAt(), request.endAt());
        FlashSaleCampaign campaign = FlashSaleCampaign.builder()
                .name(request.name().trim())
                .description(normalizeDescription(request.description()))
                .startAt(request.startAt())
                .endAt(request.endAt())
                .isActive(request.isActive() == null || request.isActive())
                .build();
        return toResponse(campaignRepository.save(campaign), now());
    }

    @Override
    @Transactional
    public FlashSaleCampaignResponse updateCampaign(Long campaignId, UpdateFlashSaleCampaignRequest request) {
        validateTimeRange(request.startAt(), request.endAt());
        FlashSaleCampaign campaign = getCampaignEntity(campaignId);
        if (Boolean.TRUE.equals(request.isActive())) {
            validateCampaignItemsDoNotOverlap(campaign, request.startAt(), request.endAt());
        }
        campaign.setName(request.name().trim());
        campaign.setDescription(normalizeDescription(request.description()));
        campaign.setStartAt(request.startAt());
        campaign.setEndAt(request.endAt());
        campaign.setIsActive(request.isActive());
        return toResponse(campaignRepository.save(campaign), now());
    }

    @Override
    @Transactional
    public FlashSaleCampaignResponse updateActivation(Long campaignId, boolean isActive) {
        FlashSaleCampaign campaign = getCampaignEntity(campaignId);
        if (isActive) {
            validateCampaignItemsDoNotOverlap(campaign, campaign.getStartAt(), campaign.getEndAt());
        }
        campaign.setIsActive(isActive);
        return toResponse(campaignRepository.save(campaign), now());
    }

    @Override
    @Transactional(readOnly = true)
    public FlashSaleCampaignResponse getCampaign(Long campaignId) {
        return toResponse(getCampaignEntity(campaignId), now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FlashSaleCampaignResponse> getCampaigns() {
        OffsetDateTime now = now();
        return campaignRepository.findAllByOrderByStartAtDesc().stream()
                .map(campaign -> toResponse(campaign, now))
                .toList();
    }

    @Override
    @Transactional
    public FlashSaleCampaignResponse addItem(Long campaignId, UpsertFlashSaleItemRequest request) {
        FlashSaleCampaign campaign = getCampaignEntity(campaignId);
        Product product = getActiveProduct(request.productId());
        validateItemData(product, request.flashSalePrice(), request.quota(), 0, 0);
        if (itemRepository.existsByCampaignIdAndProductId(campaignId, product.getId())) {
            throw new InvalidDataException("Product already belongs to this flash sale campaign");
        }
        validateOverlapWhenActive(campaign, product.getId());

        FlashSaleItem item = FlashSaleItem.builder()
                .campaign(campaign)
                .product(product)
                .flashSalePrice(request.flashSalePrice())
                .quota(request.quota())
                .build();
        FlashSaleItem savedItem = itemRepository.save(item);
        campaign.getItems().add(savedItem);
        return toResponse(campaign, now());
    }

    @Override
    @Transactional
    public FlashSaleCampaignResponse updateItem(
            Long campaignId,
            Long itemId,
            UpsertFlashSaleItemRequest request
    ) {
        FlashSaleCampaign campaign = getCampaignEntity(campaignId);
        FlashSaleItem item = getItem(campaignId, itemId);
        Product product = getActiveProduct(request.productId());

        if (!item.getProduct().getId().equals(product.getId())
                && itemRepository.existsByCampaignIdAndProductId(campaignId, product.getId())) {
            throw new InvalidDataException("Product already belongs to this flash sale campaign");
        }
        validateItemData(
                product,
                request.flashSalePrice(),
                request.quota(),
                safeQuantity(item.getReservedQuantity()),
                safeQuantity(item.getSoldQuantity())
        );
        validateOverlapWhenActive(campaign, product.getId());

        item.setProduct(product);
        item.setFlashSalePrice(request.flashSalePrice());
        item.setQuota(request.quota());
        itemRepository.save(item);
        return toResponse(campaign, now());
    }

    @Override
    @Transactional
    public FlashSaleCampaignResponse removeItem(Long campaignId, Long itemId) {
        FlashSaleCampaign campaign = getCampaignEntity(campaignId);
        FlashSaleItem item = getItem(campaignId, itemId);
        if (safeQuantity(item.getReservedQuantity()) > 0 || safeQuantity(item.getSoldQuantity()) > 0) {
            throw new InvalidDataException("Flash sale item with reserved or sold quantity cannot be deleted");
        }
        itemRepository.delete(item);
        campaign.getItems().removeIf(existing -> existing.getId().equals(itemId));
        return toResponse(campaign, now());
    }

    private FlashSaleCampaign getCampaignEntity(Long campaignId) {
        return campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Flash sale campaign not found with id: " + campaignId
                ));
    }

    private FlashSaleItem getItem(Long campaignId, Long itemId) {
        return itemRepository.findByIdAndCampaignId(itemId, campaignId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Flash sale item not found with id: " + itemId
                ));
    }

    private Product getActiveProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        if (!Boolean.TRUE.equals(product.getIsActive())) {
            throw new InvalidDataException("Inactive product cannot join a flash sale campaign");
        }
        return product;
    }

    private void validateTimeRange(OffsetDateTime startAt, OffsetDateTime endAt) {
        if (!endAt.isAfter(startAt)) {
            throw new InvalidDataException("Campaign end time must be after start time");
        }
    }

    private void validateItemData(
            Product product,
            BigDecimal flashSalePrice,
            Integer quota,
            int reservedQuantity,
            int soldQuantity
    ) {
        BigDecimal regularPrice = product.getSalePrice() != null
                ? product.getSalePrice()
                : product.getBasePrice();
        if (regularPrice == null) {
            throw new InvalidDataException("Product price is missing");
        }
        if (flashSalePrice.compareTo(regularPrice) >= 0) {
            throw new InvalidDataException("Flash sale price must be lower than the current product price");
        }
        if (quota < reservedQuantity + soldQuantity) {
            throw new InvalidDataException("Quota cannot be lower than reserved and sold quantity");
        }
    }

    private void validateOverlapWhenActive(FlashSaleCampaign campaign, Long productId) {
        if (Boolean.TRUE.equals(campaign.getIsActive())
                && itemRepository.existsActiveOverlap(
                        productId,
                        campaign.getId(),
                        campaign.getStartAt(),
                        campaign.getEndAt()
                )) {
            throw new InvalidDataException("Product already belongs to an overlapping active flash sale campaign");
        }
    }

    private void validateCampaignItemsDoNotOverlap(
            FlashSaleCampaign campaign,
            OffsetDateTime startAt,
            OffsetDateTime endAt
    ) {
        for (FlashSaleItem item : campaign.getItems()) {
            if (itemRepository.existsActiveOverlap(
                    item.getProduct().getId(), campaign.getId(), startAt, endAt)) {
                throw new InvalidDataException(
                        "Campaign time overlaps another active flash sale for product: " + item.getProduct().getId()
                );
            }
        }
    }

    private FlashSaleCampaignResponse toResponse(FlashSaleCampaign campaign, OffsetDateTime now) {
        List<FlashSaleItemResponse> items = campaign.getItems().stream()
                .map(this::toItemResponse)
                .toList();
        return FlashSaleCampaignResponse.builder()
                .id(campaign.getId())
                .name(campaign.getName())
                .description(campaign.getDescription())
                .startAt(campaign.getStartAt())
                .endAt(campaign.getEndAt())
                .isActive(campaign.getIsActive())
                .status(resolveStatus(campaign, now))
                .items(items)
                .createdAt(campaign.getCreatedAt())
                .updatedAt(campaign.getUpdatedAt())
                .build();
    }

    private FlashSaleItemResponse toItemResponse(FlashSaleItem item) {
        int reserved = safeQuantity(item.getReservedQuantity());
        int sold = safeQuantity(item.getSoldQuantity());
        return FlashSaleItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .originalPrice(item.getProduct().getSalePrice() != null
                        ? item.getProduct().getSalePrice()
                        : item.getProduct().getBasePrice())
                .flashSalePrice(item.getFlashSalePrice())
                .quota(item.getQuota())
                .reservedQuantity(reserved)
                .soldQuantity(sold)
                .availableQuantity(Math.max(0, item.getQuota() - reserved - sold))
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    private FlashSaleStatus resolveStatus(FlashSaleCampaign campaign, OffsetDateTime now) {
        if (!Boolean.TRUE.equals(campaign.getIsActive())) {
            return FlashSaleStatus.DISABLED;
        }
        if (now.isBefore(campaign.getStartAt())) {
            return FlashSaleStatus.UPCOMING;
        }
        if (!now.isBefore(campaign.getEndAt())) {
            return FlashSaleStatus.ENDED;
        }
        return FlashSaleStatus.ACTIVE;
    }

    private String normalizeDescription(String description) {
        return description == null || description.trim().isEmpty() ? null : description.trim();
    }

    private int safeQuantity(Integer quantity) {
        return quantity == null ? 0 : quantity;
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now();
    }
}
