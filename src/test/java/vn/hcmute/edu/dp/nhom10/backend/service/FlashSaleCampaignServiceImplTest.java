package vn.hcmute.edu.dp.nhom10.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.CreateFlashSaleCampaignRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.UpdateFlashSaleCampaignRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.UpsertFlashSaleItemRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.FlashSaleCampaignResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.FlashSaleCampaign;
import vn.hcmute.edu.dp.nhom10.backend.entity.FlashSaleItem;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;
import vn.hcmute.edu.dp.nhom10.backend.enums.FlashSaleStatus;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.repository.FlashSaleCampaignRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.FlashSaleItemRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.FlashSaleCampaignServiceImpl;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlashSaleCampaignServiceImplTest {

    @Mock
    private FlashSaleCampaignRepository campaignRepository;
    @Mock
    private FlashSaleItemRepository itemRepository;
    @Mock
    private ProductRepository productRepository;

    private FlashSaleCampaignService service;

    @BeforeEach
    void setUp() {
        service = new FlashSaleCampaignServiceImpl(campaignRepository, itemRepository, productRepository);
    }

    @Test
    void createCampaign_validRequest_returnsUpcomingCampaign() {
        OffsetDateTime start = OffsetDateTime.now().plusHours(1);
        CreateFlashSaleCampaignRequest request = new CreateFlashSaleCampaignRequest(
                " Summer Sale ", " Campaign ", start, start.plusHours(2), true);
        when(campaignRepository.save(any(FlashSaleCampaign.class))).thenAnswer(invocation -> {
            FlashSaleCampaign campaign = invocation.getArgument(0);
            campaign.setId(1L);
            return campaign;
        });

        FlashSaleCampaignResponse response = service.createCampaign(request);

        assertEquals(1L, response.id());
        assertEquals("Summer Sale", response.name());
        assertEquals("Campaign", response.description());
        assertEquals(FlashSaleStatus.UPCOMING, response.status());
    }

    @Test
    void createCampaign_invalidTime_throwsConflict() {
        OffsetDateTime start = OffsetDateTime.now().plusHours(1);
        CreateFlashSaleCampaignRequest request = new CreateFlashSaleCampaignRequest(
                "Sale", null, start, start, true);

        assertThrows(InvalidDataException.class, () -> service.createCampaign(request));
        verify(campaignRepository, never()).save(any());
    }

    @Test
    void addItem_validRequest_addsItemToResponse() {
        FlashSaleCampaign campaign = campaign(true);
        Product product = product(10L, "Shirt", "500000", null, true);
        UpsertFlashSaleItemRequest request = new UpsertFlashSaleItemRequest(
                10L, new BigDecimal("299000"), 100);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(itemRepository.existsByCampaignIdAndProductId(1L, 10L)).thenReturn(false);
        when(itemRepository.existsActiveOverlap(anyLong(), anyLong(), any(), any())).thenReturn(false);
        when(itemRepository.save(any(FlashSaleItem.class))).thenAnswer(invocation -> {
            FlashSaleItem item = invocation.getArgument(0);
            item.setId(20L);
            return item;
        });

        FlashSaleCampaignResponse response = service.addItem(1L, request);

        assertEquals(1, response.items().size());
        assertEquals(20L, response.items().get(0).id());
        assertEquals(100, response.items().get(0).availableQuantity());
    }

    @Test
    void addItem_duplicateProduct_throwsConflict() {
        FlashSaleCampaign campaign = campaign(true);
        Product product = product(10L, "Shirt", "500000", null, true);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(itemRepository.existsByCampaignIdAndProductId(1L, 10L)).thenReturn(true);

        assertThrows(InvalidDataException.class, () -> service.addItem(
                1L, new UpsertFlashSaleItemRequest(10L, new BigDecimal("299000"), 100)));
        verify(itemRepository, never()).save(any());
    }

    @Test
    void addItem_priceNotLowerThanCurrentPrice_throwsConflict() {
        FlashSaleCampaign campaign = campaign(true);
        Product product = product(10L, "Shirt", "500000", "400000", true);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThrows(InvalidDataException.class, () -> service.addItem(
                1L, new UpsertFlashSaleItemRequest(10L, new BigDecimal("400000"), 100)));
    }

    @Test
    void updateItem_quotaBelowUsedAndReserved_throwsConflict() {
        FlashSaleCampaign campaign = campaign(true);
        Product product = product(10L, "Shirt", "500000", null, true);
        FlashSaleItem item = item(campaign, product, 30, 20, 100);
        campaign.getItems().add(item);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(itemRepository.findByIdAndCampaignId(20L, 1L)).thenReturn(Optional.of(item));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThrows(InvalidDataException.class, () -> service.updateItem(
                1L, 20L, new UpsertFlashSaleItemRequest(10L, new BigDecimal("299000"), 49)));
        verify(itemRepository, never()).save(any());
    }

    @Test
    void removeItem_withSoldQuantity_throwsConflict() {
        FlashSaleCampaign campaign = campaign(true);
        Product product = product(10L, "Shirt", "500000", null, true);
        FlashSaleItem item = item(campaign, product, 0, 1, 100);
        campaign.getItems().add(item);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(itemRepository.findByIdAndCampaignId(20L, 1L)).thenReturn(Optional.of(item));

        assertThrows(InvalidDataException.class, () -> service.removeItem(1L, 20L));
        verify(itemRepository, never()).delete(any());
    }

    @Test
    void activateCampaign_withOverlap_throwsConflict() {
        FlashSaleCampaign campaign = campaign(false);
        Product product = product(10L, "Shirt", "500000", null, true);
        campaign.getItems().add(item(campaign, product, 0, 0, 100));
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(itemRepository.existsActiveOverlap(anyLong(), anyLong(), any(), any())).thenReturn(true);

        assertThrows(InvalidDataException.class, () -> service.updateActivation(1L, true));
        verify(campaignRepository, never()).save(any());
    }

    @Test
    void getMissingCampaign_throwsNotFound() {
        when(campaignRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getCampaign(999L));
    }

    @Test
    void updateCampaign_endedTimeRange_returnsEndedStatus() {
        FlashSaleCampaign campaign = campaign(true);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(campaignRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        OffsetDateTime start = OffsetDateTime.now().minusDays(2);
        UpdateFlashSaleCampaignRequest request = new UpdateFlashSaleCampaignRequest(
                "Ended", null, start, start.plusDays(1), true);

        FlashSaleCampaignResponse response = service.updateCampaign(1L, request);

        assertEquals(FlashSaleStatus.ENDED, response.status());
    }

    private FlashSaleCampaign campaign(boolean active) {
        return FlashSaleCampaign.builder()
                .id(1L)
                .name("Sale")
                .startAt(OffsetDateTime.now().plusHours(1))
                .endAt(OffsetDateTime.now().plusHours(3))
                .isActive(active)
                .items(new ArrayList<>())
                .build();
    }

    private Product product(Long id, String name, String basePrice, String salePrice, boolean active) {
        return Product.builder()
                .id(id)
                .name(name)
                .slug("product-" + id)
                .basePrice(new BigDecimal(basePrice))
                .salePrice(salePrice == null ? null : new BigDecimal(salePrice))
                .isActive(active)
                .build();
    }

    private FlashSaleItem item(
            FlashSaleCampaign campaign,
            Product product,
            int reserved,
            int sold,
            int quota
    ) {
        return FlashSaleItem.builder()
                .id(20L)
                .campaign(campaign)
                .product(product)
                .flashSalePrice(new BigDecimal("299000"))
                .quota(quota)
                .reservedQuantity(reserved)
                .soldQuantity(sold)
                .build();
    }
}
