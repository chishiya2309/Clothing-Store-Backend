package vn.hcmute.edu.dp.nhom10.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.CheckoutItemSnapshot;
import vn.hcmute.edu.dp.nhom10.backend.entity.CheckoutSession;
import vn.hcmute.edu.dp.nhom10.backend.entity.InventoryReservation;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;
import vn.hcmute.edu.dp.nhom10.backend.enums.ReservationStatus;
import vn.hcmute.edu.dp.nhom10.backend.exception.InsufficientStockException;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.pattern.reservation.inventory.InventoryReservationServiceImpl;
import vn.hcmute.edu.dp.nhom10.backend.repository.CheckoutSessionRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.InventoryReservationRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductVariantRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryReservationServiceImplTest {

    @Mock
    private CheckoutSessionRepository checkoutSessionRepository;

    @Mock
    private InventoryReservationRepository inventoryReservationRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @InjectMocks
    private InventoryReservationServiceImpl inventoryReservationService;

    @Test
    void reserveStock_oneVariant_success() {
        OffsetDateTime expiresAt = future();
        CheckoutSession session = checkoutSession(1L, "CHK-1");
        ProductVariant variant = variant(10L, 5, true);
        mockReserveSession(session);
        when(productVariantRepository.findAllByIdInForUpdate(List.of(10L))).thenReturn(List.of(variant));
        when(inventoryReservationRepository.sumReservedQuantity(eq(10L), eq(ReservationStatus.active), any(OffsetDateTime.class)))
                .thenReturn(0L);

        inventoryReservationService.reserveStock(1L, List.of(item(10L, 2)), expiresAt);

        List<InventoryReservation> saved = captureSavedReservations();
        assertEquals(1, saved.size());
        assertEquals(session, saved.get(0).getCheckoutSession());
        assertEquals(variant, saved.get(0).getProductVariant());
        assertEquals(2, saved.get(0).getQuantity());
        assertEquals(ReservationStatus.active, saved.get(0).getStatus());
        assertEquals(expiresAt, saved.get(0).getExpiresAt());
        assertEquals(5, variant.getStockQuantity());
    }

    @Test
    void reserveStock_multipleVariants_success() {
        CheckoutSession session = checkoutSession(1L, "CHK-1");
        ProductVariant firstVariant = variant(10L, 5, true);
        ProductVariant secondVariant = variant(20L, 7, true);
        mockReserveSession(session);
        when(productVariantRepository.findAllByIdInForUpdate(List.of(10L, 20L)))
                .thenReturn(List.of(firstVariant, secondVariant));
        when(inventoryReservationRepository.sumReservedQuantity(eq(10L), eq(ReservationStatus.active), any(OffsetDateTime.class)))
                .thenReturn(0L);
        when(inventoryReservationRepository.sumReservedQuantity(eq(20L), eq(ReservationStatus.active), any(OffsetDateTime.class)))
                .thenReturn(1L);

        inventoryReservationService.reserveStock(1L, List.of(item(20L, 3), item(10L, 2)), future());

        List<InventoryReservation> saved = captureSavedReservations();
        assertEquals(2, saved.size());
        assertEquals(10L, saved.get(0).getProductVariant().getId());
        assertEquals(20L, saved.get(1).getProductVariant().getId());
    }

    @Test
    void reserveStock_productVariantNotFound_throwsException() {
        mockReserveSession(checkoutSession(1L, "CHK-1"));
        when(productVariantRepository.findAllByIdInForUpdate(List.of(10L))).thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class,
                () -> inventoryReservationService.reserveStock(1L, List.of(item(10L, 1)), future()));

        verify(inventoryReservationRepository, never()).saveAll(any());
    }

    @Test
    void reserveStock_productVariantInactive_throwsException() {
        mockReserveSession(checkoutSession(1L, "CHK-1"));
        when(productVariantRepository.findAllByIdInForUpdate(List.of(10L)))
                .thenReturn(List.of(variant(10L, 5, false)));

        assertThrows(IllegalArgumentException.class,
                () -> inventoryReservationService.reserveStock(1L, List.of(item(10L, 1)), future()));

        verify(inventoryReservationRepository, never()).saveAll(any());
    }

    @Test
    void reserveStock_invalidRequestedQuantity_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> inventoryReservationService.reserveStock(1L, List.of(item(10L, 0)), future()));

        verifyNoInteractions(checkoutSessionRepository, inventoryReservationRepository, productVariantRepository);
    }

    @Test
    void reserveStock_invalidExpiresAt_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> inventoryReservationService.reserveStock(1L, List.of(item(10L, 1)), OffsetDateTime.now().minusMinutes(1)));

        verifyNoInteractions(checkoutSessionRepository, inventoryReservationRepository, productVariantRepository);
    }

    @Test
    void reserveStock_emptyItems_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> inventoryReservationService.reserveStock(1L, List.of(), future()));

        verifyNoInteractions(checkoutSessionRepository, inventoryReservationRepository, productVariantRepository);
    }

    @Test
    void reserveStock_duplicateProductVariantId_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> inventoryReservationService.reserveStock(1L, List.of(item(10L, 1), item(10L, 2)), future()));

        verifyNoInteractions(checkoutSessionRepository, inventoryReservationRepository, productVariantRepository);
    }

    @Test
    void reserveStock_availableWhenNoActiveReservation_success() {
        mockReserveSession(checkoutSession(1L, "CHK-1"));
        when(productVariantRepository.findAllByIdInForUpdate(List.of(10L))).thenReturn(List.of(variant(10L, 5, true)));
        when(inventoryReservationRepository.sumReservedQuantity(eq(10L), eq(ReservationStatus.active), any(OffsetDateTime.class)))
                .thenReturn(0L);

        inventoryReservationService.reserveStock(1L, List.of(item(10L, 5)), future());

        verify(inventoryReservationRepository).saveAll(any());
    }

    @Test
    void reserveStock_availableAfterSubtractingActiveReservation_success() {
        mockReserveSession(checkoutSession(1L, "CHK-1"));
        when(productVariantRepository.findAllByIdInForUpdate(List.of(10L))).thenReturn(List.of(variant(10L, 10, true)));
        when(inventoryReservationRepository.sumReservedQuantity(eq(10L), eq(ReservationStatus.active), any(OffsetDateTime.class)))
                .thenReturn(4L);

        inventoryReservationService.reserveStock(1L, List.of(item(10L, 6)), future());

        verify(inventoryReservationRepository).saveAll(any());
    }

    @Test
    void reserveStock_notEnoughAfterSubtractingActiveReservation_throwsException() {
        mockReserveSession(checkoutSession(1L, "CHK-1"));
        when(productVariantRepository.findAllByIdInForUpdate(List.of(10L))).thenReturn(List.of(variant(10L, 10, true)));
        when(inventoryReservationRepository.sumReservedQuantity(eq(10L), eq(ReservationStatus.active), any(OffsetDateTime.class)))
                .thenReturn(5L);

        assertThrows(InsufficientStockException.class,
                () -> inventoryReservationService.reserveStock(1L, List.of(item(10L, 6)), future()));

        verify(inventoryReservationRepository, never()).saveAll(any());
    }

    @Test
    void reserveStock_expiredActiveReservationIsNotCounted_success() {
        mockReserveSession(checkoutSession(1L, "CHK-1"));
        when(productVariantRepository.findAllByIdInForUpdate(List.of(10L))).thenReturn(List.of(variant(10L, 5, true)));
        when(inventoryReservationRepository.sumReservedQuantity(eq(10L), eq(ReservationStatus.active), any(OffsetDateTime.class)))
                .thenReturn(0L);

        inventoryReservationService.reserveStock(1L, List.of(item(10L, 5)), future());

        verify(inventoryReservationRepository).sumReservedQuantity(eq(10L), eq(ReservationStatus.active), any(OffsetDateTime.class));
        verify(inventoryReservationRepository).saveAll(any());
    }

    @Test
    void reserveStock_existingReservation_throwsException() {
        when(checkoutSessionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(checkoutSession(1L, "CHK-1")));
        when(inventoryReservationRepository.existsByCheckoutSession_Id(1L)).thenReturn(true);

        assertThrows(InvalidDataException.class,
                () -> inventoryReservationService.reserveStock(1L, List.of(item(10L, 1)), future()));

        verify(productVariantRepository, never()).findAllByIdInForUpdate(any());
        verify(inventoryReservationRepository, never()).saveAll(any());
    }

    @Test
    void reserveStock_oneVariantInsufficient_doesNotSaveAnyReservation() {
        ProductVariant firstVariant = variant(10L, 10, true);
        ProductVariant secondVariant = variant(20L, 2, true);
        mockReserveSession(checkoutSession(1L, "CHK-1"));
        when(productVariantRepository.findAllByIdInForUpdate(List.of(10L, 20L)))
                .thenReturn(List.of(firstVariant, secondVariant));
        when(inventoryReservationRepository.sumReservedQuantity(eq(10L), eq(ReservationStatus.active), any(OffsetDateTime.class)))
                .thenReturn(0L);
        when(inventoryReservationRepository.sumReservedQuantity(eq(20L), eq(ReservationStatus.active), any(OffsetDateTime.class)))
                .thenReturn(0L);

        assertThrows(InsufficientStockException.class,
                () -> inventoryReservationService.reserveStock(1L, List.of(item(10L, 1), item(20L, 3)), future()));

        verify(inventoryReservationRepository, never()).saveAll(any());
    }

    @Test
    void consumeStockReservation_activeReservation_success() {
        ProductVariant variant = variant(10L, 5, true);
        InventoryReservation reservation = reservation(1L, variant, 2, ReservationStatus.active, future());
        mockConsumeSession(List.of(reservation));
        when(productVariantRepository.findAllByIdInForUpdate(List.of(10L))).thenReturn(List.of(variant));

        inventoryReservationService.consumeStockReservation("CHK-1");

        assertEquals(3, variant.getStockQuantity());
        assertEquals(ReservationStatus.consumed, reservation.getStatus());
        verify(productVariantRepository).saveAll(List.of(variant));
        verify(inventoryReservationRepository).saveAll(List.of(reservation));
    }

    @Test
    void consumeStockReservation_multipleVariants_success() {
        ProductVariant firstVariant = variant(10L, 5, true);
        ProductVariant secondVariant = variant(20L, 8, true);
        InventoryReservation firstReservation = reservation(1L, firstVariant, 2, ReservationStatus.active, future());
        InventoryReservation secondReservation = reservation(2L, secondVariant, 3, ReservationStatus.active, future());
        mockConsumeSession(List.of(firstReservation, secondReservation));
        when(productVariantRepository.findAllByIdInForUpdate(List.of(10L, 20L)))
                .thenReturn(List.of(firstVariant, secondVariant));

        inventoryReservationService.consumeStockReservation("CHK-1");

        assertEquals(3, firstVariant.getStockQuantity());
        assertEquals(5, secondVariant.getStockQuantity());
        assertEquals(ReservationStatus.consumed, firstReservation.getStatus());
        assertEquals(ReservationStatus.consumed, secondReservation.getStatus());
    }

    @Test
    void consumeStockReservation_calledAgain_doesNotSubtractStockAgain() {
        ProductVariant variant = variant(10L, 5, true);
        InventoryReservation reservation = reservation(1L, variant, 2, ReservationStatus.consumed, future());
        mockConsumeSession(List.of(reservation));

        inventoryReservationService.consumeStockReservation("CHK-1");

        assertEquals(5, variant.getStockQuantity());
        verify(productVariantRepository, never()).findAllByIdInForUpdate(any());
        verify(productVariantRepository, never()).saveAll(any());
        verify(inventoryReservationRepository, never()).saveAll(any());
    }

    @Test
    void consumeStockReservation_releasedReservation_throwsException() {
        InventoryReservation reservation = reservation(1L, variant(10L, 5, true), 2, ReservationStatus.released, future());
        mockConsumeSession(List.of(reservation));

        assertThrows(InvalidDataException.class, () -> inventoryReservationService.consumeStockReservation("CHK-1"));

        verify(productVariantRepository, never()).findAllByIdInForUpdate(any());
    }

    @Test
    void consumeStockReservation_expiredReservation_throwsException() {
        InventoryReservation reservation = reservation(1L, variant(10L, 5, true), 2, ReservationStatus.expired, future());
        mockConsumeSession(List.of(reservation));

        assertThrows(InvalidDataException.class, () -> inventoryReservationService.consumeStockReservation("CHK-1"));

        verify(productVariantRepository, never()).findAllByIdInForUpdate(any());
    }

    @Test
    void consumeStockReservation_activeButExpired_throwsException() {
        InventoryReservation reservation = reservation(1L, variant(10L, 5, true), 2, ReservationStatus.active, past());
        mockConsumeSession(List.of(reservation));

        assertThrows(InvalidDataException.class, () -> inventoryReservationService.consumeStockReservation("CHK-1"));

        verify(productVariantRepository, never()).findAllByIdInForUpdate(any());
    }

    @Test
    void consumeStockReservation_oneVariantInsufficient_throwsException() {
        ProductVariant variant = variant(10L, 1, true);
        InventoryReservation reservation = reservation(1L, variant, 2, ReservationStatus.active, future());
        mockConsumeSession(List.of(reservation));
        when(productVariantRepository.findAllByIdInForUpdate(List.of(10L))).thenReturn(List.of(variant));

        assertThrows(InsufficientStockException.class, () -> inventoryReservationService.consumeStockReservation("CHK-1"));

        verify(productVariantRepository, never()).saveAll(any());
        verify(inventoryReservationRepository, never()).saveAll(any());
    }

    @Test
    void releaseStockReservation_activeReservation_setsReleased() {
        InventoryReservation reservation = reservation(1L, variant(10L, 5, true), 2, ReservationStatus.active, future());
        mockReleaseSession(List.of(reservation));

        inventoryReservationService.releaseStockReservation("CHK-1");

        assertEquals(ReservationStatus.released, reservation.getStatus());
        verify(inventoryReservationRepository).saveAll(List.of(reservation));
    }

    @Test
    void releaseStockReservation_releasedReservation_keepsState() {
        InventoryReservation reservation = reservation(1L, variant(10L, 5, true), 2, ReservationStatus.released, future());
        mockReleaseSession(List.of(reservation));

        inventoryReservationService.releaseStockReservation("CHK-1");

        assertEquals(ReservationStatus.released, reservation.getStatus());
        verify(inventoryReservationRepository, never()).saveAll(any());
    }

    @Test
    void releaseStockReservation_expiredReservation_keepsState() {
        InventoryReservation reservation = reservation(1L, variant(10L, 5, true), 2, ReservationStatus.expired, past());
        mockReleaseSession(List.of(reservation));

        inventoryReservationService.releaseStockReservation("CHK-1");

        assertEquals(ReservationStatus.expired, reservation.getStatus());
        verify(inventoryReservationRepository, never()).saveAll(any());
    }

    @Test
    void releaseStockReservation_consumedReservation_keepsState() {
        InventoryReservation reservation = reservation(1L, variant(10L, 5, true), 2, ReservationStatus.consumed, future());
        mockReleaseSession(List.of(reservation));

        inventoryReservationService.releaseStockReservation("CHK-1");

        assertEquals(ReservationStatus.consumed, reservation.getStatus());
        verify(inventoryReservationRepository, never()).saveAll(any());
    }

    @Test
    void releaseStockReservation_doesNotChangeStockQuantity() {
        ProductVariant variant = variant(10L, 5, true);
        InventoryReservation reservation = reservation(1L, variant, 2, ReservationStatus.active, future());
        mockReleaseSession(List.of(reservation));

        inventoryReservationService.releaseStockReservation("CHK-1");

        assertEquals(5, variant.getStockQuantity());
        verifyNoInteractions(productVariantRepository);
    }

    private void mockReserveSession(CheckoutSession checkoutSession) {
        when(checkoutSessionRepository.findByIdForUpdate(checkoutSession.getId())).thenReturn(Optional.of(checkoutSession));
        when(inventoryReservationRepository.existsByCheckoutSession_Id(checkoutSession.getId())).thenReturn(false);
    }

    private void mockConsumeSession(List<InventoryReservation> reservations) {
        CheckoutSession checkoutSession = checkoutSession(1L, "CHK-1");
        when(checkoutSessionRepository.findByCheckoutCodeForUpdate("CHK-1")).thenReturn(Optional.of(checkoutSession));
        when(inventoryReservationRepository.findAllByCheckoutSessionIdForUpdate(1L)).thenReturn(reservations);
    }

    private void mockReleaseSession(List<InventoryReservation> reservations) {
        mockConsumeSession(reservations);
    }

    @SuppressWarnings("unchecked")
    private List<InventoryReservation> captureSavedReservations() {
        ArgumentCaptor<Iterable<InventoryReservation>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(inventoryReservationRepository).saveAll(captor.capture());
        return StreamSupport.stream(captor.getValue().spliterator(), false).toList();
    }

    private CheckoutItemSnapshot item(Long productVariantId, Integer quantity) {
        return new CheckoutItemSnapshot(
                1L,
                productVariantId,
                "Product",
                "Size: M, Color: Black",
                quantity,
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(100000)
        );
    }

    private CheckoutSession checkoutSession(Long id, String checkoutCode) {
        return CheckoutSession.builder()
                .id(id)
                .checkoutCode(checkoutCode)
                .build();
    }

    private InventoryReservation reservation(
            Long id,
            ProductVariant variant,
            Integer quantity,
            ReservationStatus status,
            OffsetDateTime expiresAt
    ) {
        return InventoryReservation.builder()
                .id(id)
                .checkoutSession(checkoutSession(1L, "CHK-1"))
                .productVariant(variant)
                .quantity(quantity)
                .status(status)
                .expiresAt(expiresAt)
                .build();
    }

    private ProductVariant variant(Long id, Integer stockQuantity, boolean active) {
        return ProductVariant.builder()
                .id(id)
                .stockQuantity(stockQuantity)
                .isActive(active)
                .build();
    }

    private OffsetDateTime future() {
        return OffsetDateTime.now().plusHours(1);
    }

    private OffsetDateTime past() {
        return OffsetDateTime.now().minusHours(1);
    }
}
