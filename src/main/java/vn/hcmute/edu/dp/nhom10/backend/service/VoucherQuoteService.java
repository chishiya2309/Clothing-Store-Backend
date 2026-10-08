package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.VoucherQuote;

import java.math.BigDecimal;

public interface VoucherQuoteService {
    VoucherQuote quote(String code, Long customerId, BigDecimal subtotal, BigDecimal shippingFee);
}
