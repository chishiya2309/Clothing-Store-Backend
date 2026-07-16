package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PublicFlashSaleProductResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PublicFlashSaleResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.FlashSaleCampaign;
import vn.hcmute.edu.dp.nhom10.backend.entity.FlashSaleItem;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductImage;
import vn.hcmute.edu.dp.nhom10.backend.enums.FlashSaleStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.ImageType;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.repository.FlashSaleCampaignRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.FlashSaleItemRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.FlashSaleQueryService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FlashSaleQueryServiceImpl implements FlashSaleQueryService {

    private static final PageRequest FIRST_CAMPAIGN = PageRequest.of(0, 1);

    private final FlashSaleCampaignRepository campaignRepository;
    private final FlashSaleItemRepository itemRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<PublicFlashSaleResponse> getCurrentOrUpcomingCampaign() {
        OffsetDateTime serverTime = now();
        Optional<FlashSaleCampaign> campaign = first(campaignRepository.findActiveAt(serverTime, FIRST_CAMPAIGN));
        if (campaign.isEmpty()) {
            campaign = first(campaignRepository.findUpcomingAfter(serverTime, FIRST_CAMPAIGN));
        }
        return campaign.map(value -> toResponse(value, serverTime));
    }

    @Override
    @Transactional(readOnly = true)
    public PublicFlashSaleResponse getCampaign(Long campaignId) {
        FlashSaleCampaign campaign = campaignRepository.findById(campaignId)
                .filter(value -> Boolean.TRUE.equals(value.getIsActive()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active flash sale campaign not found with id: " + campaignId
                ));
        return toResponse(campaign, now());
    }

    private PublicFlashSaleResponse toResponse(FlashSaleCampaign campaign, OffsetDateTime serverTime) {
        List<PublicFlashSaleProductResponse> items = itemRepository
                .findPublicItemsByCampaignId(campaign.getId())
                .stream()
                .map(this::toProductResponse)
                .toList();
        return PublicFlashSaleResponse.builder()
                .id(campaign.getId())
                .name(campaign.getName())
                .description(campaign.getDescription())
                .startAt(campaign.getStartAt())
                .endAt(campaign.getEndAt())
                .status(resolveStatus(campaign, serverTime))
                .serverTime(serverTime)
                .items(items)
                .build();
    }

    private PublicFlashSaleProductResponse toProductResponse(FlashSaleItem item) {
        Product product = item.getProduct();
        int reserved = safeQuantity(item.getReservedQuantity());
        int sold = safeQuantity(item.getSoldQuantity());
        int available = Math.max(0, item.getQuota() - reserved - sold);
        BigDecimal originalPrice = product.getSalePrice() != null
                ? product.getSalePrice()
                : product.getBasePrice();
        return PublicFlashSaleProductResponse.builder()
                .flashSaleItemId(item.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productSlug(product.getSlug())
                .thumbnailUrl(resolveThumbnail(product))
                .originalPrice(originalPrice)
                .flashSalePrice(item.getFlashSalePrice())
                .quota(item.getQuota())
                .soldQuantity(sold)
                .availableQuantity(available)
                .soldOut(available == 0)
                .build();
    }

    private String resolveThumbnail(Product product) {
        return product.getImages().stream()
                .filter(image -> image.getImageType() == ImageType.thumbnail)
                .min(Comparator.comparingInt(this::safeDisplayOrder))
                .or(() -> product.getImages().stream()
                        .filter(image -> image.getImageType() == ImageType.main)
                        .min(Comparator.comparingInt(this::safeDisplayOrder)))
                .or(() -> product.getImages().stream()
                        .min(Comparator.comparingInt(this::safeDisplayOrder)))
                .map(ProductImage::getImageUrl)
                .orElse(null);
    }

    private FlashSaleStatus resolveStatus(FlashSaleCampaign campaign, OffsetDateTime serverTime) {
        if (!Boolean.TRUE.equals(campaign.getIsActive())) {
            return FlashSaleStatus.DISABLED;
        }
        if (serverTime.isBefore(campaign.getStartAt())) {
            return FlashSaleStatus.UPCOMING;
        }
        if (!serverTime.isBefore(campaign.getEndAt())) {
            return FlashSaleStatus.ENDED;
        }
        return FlashSaleStatus.ACTIVE;
    }

    private Optional<FlashSaleCampaign> first(List<FlashSaleCampaign> campaigns) {
        return campaigns.stream().findFirst();
    }

    private int safeQuantity(Integer quantity) {
        return quantity == null ? 0 : quantity;
    }

    private int safeDisplayOrder(ProductImage image) {
        return image.getDisplayOrder() == null ? Integer.MAX_VALUE : image.getDisplayOrder();
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now();
    }
}
