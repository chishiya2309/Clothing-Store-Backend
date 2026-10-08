package vn.hcmute.edu.dp.nhom10.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.hcmute.edu.dp.nhom10.backend.entity.Order;
import vn.hcmute.edu.dp.nhom10.backend.entity.OrderVoucher;
import vn.hcmute.edu.dp.nhom10.backend.entity.Voucher;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderVoucherRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.VoucherRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.OrderVoucherAdjustmentServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderVoucherAdjustmentServiceImplTest {

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private OrderVoucherRepository orderVoucherRepository;

    @InjectMocks
    private OrderVoucherAdjustmentServiceImpl service;

    @Test
    void restoreVoucherUsageForCancelledOrder_withVoucherLocksAndDecrementsOnce() {
        Voucher voucher = Voucher.builder().id(3L).timesUsed(2).build();
        Order order = Order.builder().id(1L).voucher(Voucher.builder().id(3L).build()).build();
        when(orderVoucherRepository.findAllByOrder_IdOrderByVoucherSlotAscIdAsc(1L)).thenReturn(List.of());
        when(voucherRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(voucher));

        service.restoreVoucherUsageForCancelledOrder(order);

        assertEquals(1, voucher.getTimesUsed());
        verify(voucherRepository).findByIdForUpdate(3L);
    }

    @Test
    void restoreVoucherUsageForCancelledOrder_withoutVoucherDoesNothing() {
        when(orderVoucherRepository.findAllByOrder_IdOrderByVoucherSlotAscIdAsc(1L)).thenReturn(List.of());

        service.restoreVoucherUsageForCancelledOrder(Order.builder().id(1L).build());

        verify(voucherRepository, never()).findByIdForUpdate(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void restoreVoucherUsageForCancelledOrder_missingVoucherThrows() {
        Order order = Order.builder().id(1L).voucher(Voucher.builder().id(3L).build()).build();
        when(orderVoucherRepository.findAllByOrder_IdOrderByVoucherSlotAscIdAsc(1L)).thenReturn(List.of());
        when(voucherRepository.findByIdForUpdate(3L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.restoreVoucherUsageForCancelledOrder(order));
    }

    @Test
    void restoreVoucherUsageForCancelledOrder_zeroTimesUsedThrows() {
        Voucher voucher = Voucher.builder().id(3L).timesUsed(0).build();
        Order order = Order.builder().id(1L).voucher(Voucher.builder().id(3L).build()).build();
        when(orderVoucherRepository.findAllByOrder_IdOrderByVoucherSlotAscIdAsc(1L)).thenReturn(List.of());
        when(voucherRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(voucher));

        assertThrows(IllegalStateException.class, () -> service.restoreVoucherUsageForCancelledOrder(order));

        assertEquals(0, voucher.getTimesUsed());
    }

    @Test
    void restoreVoucherUsageForCancelledOrder_unpersistedVoucherThrowsBeforeLocking() {
        Order order = Order.builder().id(1L).voucher(Voucher.builder().code("SAVE10").build()).build();
        when(orderVoucherRepository.findAllByOrder_IdOrderByVoucherSlotAscIdAsc(1L)).thenReturn(List.of());

        assertThrows(IllegalStateException.class, () -> service.restoreVoucherUsageForCancelledOrder(order));

        verify(voucherRepository, never()).findByIdForUpdate(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void restoreVoucherUsageForCancelledOrder_withSnapshotsRestoresEachVoucher() {
        Order order = Order.builder().id(1L).voucher(Voucher.builder().id(99L).build()).build();
        Voucher productVoucherRef = Voucher.builder().id(3L).build();
        Voucher shippingVoucherRef = Voucher.builder().id(4L).build();
        Voucher productVoucher = Voucher.builder().id(3L).timesUsed(2).build();
        Voucher shippingVoucher = Voucher.builder().id(4L).timesUsed(1).build();
        when(orderVoucherRepository.findAllByOrder_IdOrderByVoucherSlotAscIdAsc(1L)).thenReturn(List.of(
                OrderVoucher.builder().voucher(productVoucherRef).build(),
                OrderVoucher.builder().voucher(shippingVoucherRef).build()
        ));
        when(voucherRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(productVoucher));
        when(voucherRepository.findByIdForUpdate(4L)).thenReturn(Optional.of(shippingVoucher));

        service.restoreVoucherUsageForCancelledOrder(order);

        assertEquals(1, productVoucher.getTimesUsed());
        assertEquals(0, shippingVoucher.getTimesUsed());
        verify(voucherRepository, never()).findByIdForUpdate(99L);
    }
}
