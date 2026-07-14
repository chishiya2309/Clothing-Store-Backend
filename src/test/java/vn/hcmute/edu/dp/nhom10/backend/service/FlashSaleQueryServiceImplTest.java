package vn.hcmute.edu.dp.nhom10.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
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
import vn.hcmute.edu.dp.nhom10.backend.service.impl.FlashSaleQueryServiceImpl;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlashSaleQueryServiceImplTest {

    @Mock
    private FlashSaleCampaignRepository campaignRepository;
    @Mock
    private FlashSaleItemRepository itemRepository;

    private FlashSaleQueryService service;

    @BeforeEach
    void setUp() {
        service = new FlashSaleQueryServiceImpl(campaignRepository, itemRepository);
    }

    @Test
    void getCurrent_activeCampaign_returnsProductProgress() {
        FlashSaleCampaign campaign = campaign(
                OffsetDateTime.now().minusHours(1),
                OffsetDateTime.now().plusHours(1),
                true
        );
        FlashSaleItem item = item(campaign, 25, 10, 100);
        when(campaignRepository.findActiveAt(any(), any(Pageable.class))).thenReturn(List.of(campaign));
        when(itemRepository.findPublicItemsByCampaignId(1L)).thenReturn(List.of(item));

        PublicFlashSaleResponse response = service.getCurrentOrUpcomingCampaign().orElseThrow();

        assertEquals(FlashSaleStatus.ACTIVE, response.status());
        assertEquals(1, response.items().size());
        assertEquals(25, response.items().get(0).soldQuantity());
        assertEquals(65, response.items().get(0).availableQuantity());
        assertFalse(response.items().get(0).soldOut());
        assertEquals("thumbnail.jpg", response.items().get(0).thumbnailUrl());
        verify(campaignRepository, never()).findUpcomingAfter(any(), any(Pageable.class));
    }

    @Test
    void getCurrent_withoutActive_returnsNearestUpcoming() {
        FlashSaleCampaign campaign = campaign(
                OffsetDateTime.now().plusHours(1),
                OffsetDateTime.now().plusHours(2),
                true
        );
        when(campaignRepository.findActiveAt(any(), any(Pageable.class))).thenReturn(List.of());
        when(campaignRepository.findUpcomingAfter(any(), any(Pageable.class))).thenReturn(List.of(campaign));
        when(itemRepository.findPublicItemsByCampaignId(1L)).thenReturn(List.of());

        PublicFlashSaleResponse response = service.getCurrentOrUpcomingCampaign().orElseThrow();

        assertEquals(FlashSaleStatus.UPCOMING, response.status());
    }

    @Test
    void getCurrent_withoutCampaign_returnsEmpty() {
        when(campaignRepository.findActiveAt(any(), any(Pageable.class))).thenReturn(List.of());
        when(campaignRepository.findUpcomingAfter(any(), any(Pageable.class))).thenReturn(List.of());

        assertTrue(service.getCurrentOrUpcomingCampaign().isEmpty());
        verifyNoInteractions(itemRepository);
    }

    @Test
    void getCampaign_disabledCampaign_throwsNotFound() {
        FlashSaleCampaign campaign = campaign(
                OffsetDateTime.now().plusHours(1),
                OffsetDateTime.now().plusHours(2),
                false
        );
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));

        assertThrows(ResourceNotFoundException.class, () -> service.getCampaign(1L));
    }

    @Test
    void getCampaign_soldOutItem_returnsZeroAvailable() {
        FlashSaleCampaign campaign = campaign(
                OffsetDateTime.now().minusHours(1),
                OffsetDateTime.now().plusHours(1),
                true
        );
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(itemRepository.findPublicItemsByCampaignId(1L))
                .thenReturn(List.of(item(campaign, 90, 10, 100)));

        PublicFlashSaleResponse response = service.getCampaign(1L);

        assertEquals(0, response.items().get(0).availableQuantity());
        assertTrue(response.items().get(0).soldOut());
    }

    @Test
    void getCampaign_endedCampaign_returnsEndedStatusForHistory() {
        FlashSaleCampaign campaign = campaign(
                OffsetDateTime.now().minusHours(2),
                OffsetDateTime.now().minusHours(1),
                true
        );
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(itemRepository.findPublicItemsByCampaignId(1L)).thenReturn(List.of());

        PublicFlashSaleResponse response = service.getCampaign(1L);

        assertEquals(FlashSaleStatus.ENDED, response.status());
    }

    private FlashSaleCampaign campaign(OffsetDateTime startAt, OffsetDateTime endAt, boolean active) {
        return FlashSaleCampaign.builder()
                .id(1L)
                .name("Flash Sale")
                .startAt(startAt)
                .endAt(endAt)
                .isActive(active)
                .build();
    }

    private FlashSaleItem item(FlashSaleCampaign campaign, int sold, int reserved, int quota) {
        Product product = Product.builder()
                .id(10L)
                .name("Shirt")
                .slug("shirt")
                .basePrice(new BigDecimal("500000"))
                .salePrice(new BigDecimal("400000"))
                .isActive(true)
                .images(new ArrayList<>())
                .build();
        product.getImages().add(ProductImage.builder()
                .imageUrl("main.jpg")
                .imageType(ImageType.main)
                .displayOrder(0)
                .build());
        product.getImages().add(ProductImage.builder()
                .imageUrl("thumbnail.jpg")
                .imageType(ImageType.thumbnail)
                .displayOrder(1)
                .build());
        return FlashSaleItem.builder()
                .id(20L)
                .campaign(campaign)
                .product(product)
                .flashSalePrice(new BigDecimal("299000"))
                .quota(quota)
                .soldQuantity(sold)
                .reservedQuantity(reserved)
                .build();
    }
}
