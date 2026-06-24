package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.entity.Order;

public interface LoyaltyPointService {
    LoyaltyPointAwardResult awardForCompletedOrder(Order order);
}
