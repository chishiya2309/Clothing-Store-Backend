package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.VoucherQuote;
import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.VoucherQuoteRequest;
import vn.hcmute.edu.dp.nhom10.backend.enums.VoucherSlot;

import java.math.BigDecimal;

public interface VoucherQuoteService {
    VoucherQuote quote(VoucherQuoteRequest request);

    VoucherQuote quote(String code, Long customerId, BigDecimal subtotal, BigDecimal shippingFee);

    default VoucherQuote quoteProductVoucher(String code, Long customerId,
                                             BigDecimal subtotal, BigDecimal shippingFee) {
        return quote(new VoucherQuoteRequest(code, VoucherSlot.product, customerId, subtotal, shippingFee));
    }
}
