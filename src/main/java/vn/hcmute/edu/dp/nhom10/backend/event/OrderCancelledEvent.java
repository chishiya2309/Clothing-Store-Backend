package vn.hcmute.edu.dp.nhom10.backend.event;

import vn.hcmute.edu.dp.nhom10.backend.entity.Order;

public record OrderCancelledEvent(Order order) {}
