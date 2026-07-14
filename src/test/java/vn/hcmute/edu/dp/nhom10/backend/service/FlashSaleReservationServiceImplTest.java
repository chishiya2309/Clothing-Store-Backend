package vn.hcmute.edu.dp.nhom10.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.CheckoutItemSnapshot;
import vn.hcmute.edu.dp.nhom10.backend.entity.CheckoutSession;
import vn.hcmute.edu.dp.nhom10.backend.entity.FlashSaleItem;
import vn.hcmute.edu.dp.nhom10.backend.entity.FlashSaleReservation;
import vn.hcmute.edu.dp.nhom10.backend.enums.ReservationStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PriceSource;
import vn.hcmute.edu.dp.nhom10.backend.exception.InsufficientStockException;
import vn.hcmute.edu.dp.nhom10.backend.repository.CheckoutSessionRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.FlashSaleItemRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.FlashSaleReservationRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.FlashSaleReservationServiceImpl;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlashSaleReservationServiceImplTest {
    @Mock CheckoutSessionRepository checkoutRepository;
    @Mock FlashSaleItemRepository itemRepository;
    @Mock FlashSaleReservationRepository reservationRepository;

    @Test
    void reserveQuota_availableQuota_incrementsReservedAndCreatesReservation() {
        FlashSaleItem flashItem = FlashSaleItem.builder().id(5L).quota(10).reservedQuantity(3).soldQuantity(2).build();
        when(checkoutRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(CheckoutSession.builder().id(1L).build()));
        when(itemRepository.findAllByIdInForUpdate(List.of(5L))).thenReturn(List.of(flashItem));

        service().reserveQuota(1L, List.of(item(5L, 4)), OffsetDateTime.now().plusMinutes(10));

        assertEquals(7, flashItem.getReservedQuantity());
        verify(reservationRepository).saveAll(anyList());
    }

    @Test
    void reserveQuota_insufficientQuota_rollsBackBeforeSaving() {
        FlashSaleItem flashItem = FlashSaleItem.builder().id(5L).quota(10).reservedQuantity(7).soldQuantity(2).build();
        when(checkoutRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(CheckoutSession.builder().id(1L).build()));
        when(itemRepository.findAllByIdInForUpdate(List.of(5L))).thenReturn(List.of(flashItem));

        assertThrows(InsufficientStockException.class,
                () -> service().reserveQuota(1L, List.of(item(5L, 2)), OffsetDateTime.now().plusMinutes(10)));

        verify(reservationRepository, never()).saveAll(anyList());
    }

    @Test
    void reserveQuota_regularPrice_doesNothing() {
        CheckoutItemSnapshot regular = new CheckoutItemSnapshot(1L, 2L, "P", "V", 1,
                BigDecimal.TEN, BigDecimal.TEN, null, PriceSource.REGULAR);
        service().reserveQuota(1L, List.of(regular), OffsetDateTime.now().plusMinutes(10));
        verify(checkoutRepository, never()).findByIdForUpdate(1L);
    }

    @Test
    void consumeQuota_activeReservation_movesReservedToSold() {
        FlashSaleItem item = FlashSaleItem.builder().id(5L).quota(10).reservedQuantity(2).soldQuantity(3).build();
        FlashSaleReservation reservation = FlashSaleReservation.builder().flashSaleItem(item).quantity(2)
                .status(ReservationStatus.active).build();
        when(checkoutRepository.findByCheckoutCodeForUpdate("CHK-1"))
                .thenReturn(Optional.of(CheckoutSession.builder().id(1L).checkoutCode("CHK-1").build()));
        when(reservationRepository.findAllByCheckoutSessionIdForUpdate(1L)).thenReturn(List.of(reservation));
        when(itemRepository.findAllByIdInForUpdate(List.of(5L))).thenReturn(List.of(item));

        service().consumeQuota("CHK-1");

        assertEquals(0, item.getReservedQuantity());
        assertEquals(5, item.getSoldQuantity());
        assertEquals(ReservationStatus.consumed, reservation.getStatus());
    }

    @Test
    void releaseQuota_activeReservation_restoresAvailability() {
        FlashSaleItem item = FlashSaleItem.builder().id(5L).quota(10).reservedQuantity(2).soldQuantity(3).build();
        FlashSaleReservation reservation = FlashSaleReservation.builder().flashSaleItem(item).quantity(2)
                .status(ReservationStatus.active).build();
        when(checkoutRepository.findByCheckoutCodeForUpdate("CHK-1"))
                .thenReturn(Optional.of(CheckoutSession.builder().id(1L).checkoutCode("CHK-1").build()));
        when(reservationRepository.findAllByCheckoutSessionIdForUpdate(1L)).thenReturn(List.of(reservation));
        when(itemRepository.findAllByIdInForUpdate(List.of(5L))).thenReturn(List.of(item));

        service().releaseQuota("CHK-1");

        assertEquals(0, item.getReservedQuantity());
        assertEquals(3, item.getSoldQuantity());
        assertEquals(ReservationStatus.released, reservation.getStatus());
    }

    private FlashSaleReservationServiceImpl service() {
        return new FlashSaleReservationServiceImpl(checkoutRepository, itemRepository, reservationRepository);
    }

    private CheckoutItemSnapshot item(Long flashItemId, int quantity) {
        return new CheckoutItemSnapshot(1L, 2L, "P", "V", quantity, BigDecimal.TEN,
                BigDecimal.TEN.multiply(BigDecimal.valueOf(quantity)), flashItemId, PriceSource.FLASH_SALE);
    }
}
