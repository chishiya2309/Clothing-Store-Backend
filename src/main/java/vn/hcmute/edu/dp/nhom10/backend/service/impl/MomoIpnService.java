package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.MomoIpnRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.MomoIpnTransactionResult;
import vn.hcmute.edu.dp.nhom10.backend.entity.PaymentAttempt;
import vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment.MomoCallbackVerifier;
import vn.hcmute.edu.dp.nhom10.backend.repository.PaymentAttemptRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.internal.MomoIpnTransactionService;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "MOMO-IPN")
public class MomoIpnService {

    private final MomoCallbackVerifier callbackVerifier;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final MomoIpnTransactionService ipnTransactionService;

    public boolean handleIpn(MomoIpnRequest request) {
        if (request == null
                || !callbackVerifier.hasValidSignature(request)
                || !callbackVerifier.matchesConfiguredPartner(request)) {
            log.info("MoMo IPN rejected: orderId={}, transId={}, reason=invalid_signature_or_partner",
                    request == null ? null : request.orderId(),
                    request == null ? null : request.transId());
            return false;
        }

        PaymentAttempt paymentAttempt = paymentAttemptRepository
                .findByPaymentReference(request.orderId())
                .orElse(null);
        if (paymentAttempt == null) {
            log.info("MoMo IPN rejected: orderId={}, transId={}, reason=attempt_not_found",
                    request.orderId(), request.transId());
            return false;
        }
        if (!callbackVerifier.matchesAttempt(request, paymentAttempt)) {
            log.warn("MoMo IPN rejected: orderId={}, transId={}, reason=attempt_mismatch",
                    request.orderId(), request.transId());
            return false;
        }

        try {
            MomoIpnTransactionResult result = ipnTransactionService.process(request);
            boolean accepted = result.code() == MomoIpnTransactionResult.Code.ACCEPTED;
            log.info("MoMo IPN processed: orderId={}, transId={}, result={}",
                    request.orderId(), request.transId(), result.code());
            return accepted;
        } catch (RuntimeException e) {
            log.error("MoMo IPN failed: orderId={}, transId={}", request.orderId(), request.transId(), e);
            return false;
        }
    }
}
