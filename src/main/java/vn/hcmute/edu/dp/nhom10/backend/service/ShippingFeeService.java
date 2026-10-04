package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.entity.Address;

import java.math.BigDecimal;

public interface ShippingFeeService {
    BigDecimal calculate(Address address);
}
