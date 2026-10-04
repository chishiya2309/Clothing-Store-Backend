package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.ReservedCheckoutResult;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.ConfirmCheckoutRequestDTO;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.PreviewCheckoutRequestDTO;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.CheckoutPreviewResponse;

public interface CheckoutService {

    CheckoutPreviewResponse previewCheckout(
            PreviewCheckoutRequestDTO requestDTO,
            Long userId
    );

    ReservedCheckoutResult prepareCheckout(
            ConfirmCheckoutRequestDTO requestDTO,
            Long userId
    );
}
