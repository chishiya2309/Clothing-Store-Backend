package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.dto.pricing.CheckoutPricingRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.pricing.CheckoutPricingResult;

public interface CheckoutPricingService {
    CheckoutPricingResult calculate(CheckoutPricingRequest request);
}
