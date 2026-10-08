package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.entity.Order;
import vn.hcmute.edu.dp.nhom10.backend.entity.OrderVoucher;
import vn.hcmute.edu.dp.nhom10.backend.entity.Voucher;
import vn.hcmute.edu.dp.nhom10.backend.entity.VoucherReservation;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderVoucherRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.OrderVoucherSnapshotService;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderVoucherSnapshotServiceImpl implements OrderVoucherSnapshotService {

    private final OrderVoucherRepository orderVoucherRepository;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void saveSnapshots(Order order, List<VoucherReservation> voucherReservations) {
        if (order == null || order.getId() == null || voucherReservations == null || voucherReservations.isEmpty()) {
            return;
        }

        List<OrderVoucher> snapshots = voucherReservations.stream()
                .filter(reservation -> reservation.getVoucher() != null)
                .map(reservation -> toSnapshot(order, reservation))
                .toList();
        if (!snapshots.isEmpty()) {
            orderVoucherRepository.saveAll(snapshots);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderVoucher> findSnapshots(Long orderId) {
        if (orderId == null) {
            return List.of();
        }
        return orderVoucherRepository.findAllByOrder_IdOrderByVoucherSlotAscIdAsc(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasSnapshots(Long orderId) {
        return orderId != null && orderVoucherRepository.existsByOrder_Id(orderId);
    }

    private OrderVoucher toSnapshot(Order order, VoucherReservation reservation) {
        Voucher voucher = reservation.getVoucher();
        return OrderVoucher.builder()
                .order(order)
                .voucher(voucher)
                .voucherCode(voucher.getCode())
                .discountType(voucher.getDiscountType())
                .voucherSlot(reservation.getVoucherSlot())
                .discountAmount(defaultZero(reservation.getDiscountAmount()))
                .build();
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
