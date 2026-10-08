package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.entity.Order;
import vn.hcmute.edu.dp.nhom10.backend.entity.OrderVoucher;
import vn.hcmute.edu.dp.nhom10.backend.entity.VoucherReservation;

import java.util.List;

public interface OrderVoucherSnapshotService {
    void saveSnapshots(Order order, List<VoucherReservation> voucherReservations);

    List<OrderVoucher> findSnapshots(Long orderId);

    boolean hasSnapshots(Long orderId);
}
