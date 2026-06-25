package vn.hcmute.edu.dp.nhom10.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.VnPayIpnResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.VnPayReturnResponseDTO;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.VnPayIpnService;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.VnPayReturnService;

import java.net.URI;

@RestController
@RequestMapping("/api/payments/vnpay")
@RequiredArgsConstructor
public class VnPayCallbackController {

    private final VnPayReturnService returnService;
    private final VnPayIpnService ipnService;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @GetMapping("/return")
    public ResponseEntity<Void> handleReturn(
            @RequestParam MultiValueMap<String, String> parameters
    ) {
        VnPayReturnResponseDTO response = returnService.handleReturn(parameters);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(buildFrontendReturnUri(response));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("/ipn")
    public ResponseEntity<VnPayIpnResponse> handleIpn(
            @RequestParam MultiValueMap<String, String> parameters
    ) {
        return ResponseEntity.ok(ipnService.handleIpn(parameters));
    }

    private URI buildFrontendReturnUri(VnPayReturnResponseDTO response) {
        String status = toFrontendStatus(response.paymentStatus());
        String message = toFrontendMessage(status);

        return UriComponentsBuilder.fromUriString(normalizeFrontendUrl())
                .path("/checkout/result")
                .queryParam("status", status)
                .queryParam("paymentMethod", "vnpay")
                .queryParamIfPresent("checkoutCode", java.util.Optional.ofNullable(response.checkoutCode()))
                .queryParamIfPresent("paymentReference", java.util.Optional.ofNullable(response.paymentReference()))
                .queryParamIfPresent("gatewayTransactionId", java.util.Optional.ofNullable(response.gatewayTransactionId()))
                .queryParam("message", message)
                .build()
                .encode()
                .toUri();
    }

    private String normalizeFrontendUrl() {
        if (frontendUrl == null || frontendUrl.isBlank()) {
            return "http://localhost:5173";
        }
        return frontendUrl.endsWith("/") ? frontendUrl.substring(0, frontendUrl.length() - 1) : frontendUrl;
    }

    private String toFrontendStatus(String paymentStatus) {
        if ("success".equals(paymentStatus)) {
            return "success";
        }
        if ("processing".equals(paymentStatus)) {
            return "pending";
        }
        return "failed";
    }

    private String toFrontendMessage(String status) {
        if ("success".equals(status)) {
            return "Thanh to\u00e1n VNPay th\u00e0nh c\u00f4ng. \u0110\u01a1n h\u00e0ng c\u1ee7a b\u1ea1n \u0111\u00e3 \u0111\u01b0\u1ee3c ghi nh\u1eadn.";
        }
        if ("pending".equals(status)) {
            return "Thanh to\u00e1n VNPay \u0111ang \u0111\u01b0\u1ee3c x\u1eed l\u00fd. Vui l\u00f2ng ki\u1ec3m tra l\u1ea1i \u0111\u01a1n h\u00e0ng sau \u00edt ph\u00fat.";
        }
        return "Thanh to\u00e1n VNPay kh\u00f4ng th\u00e0nh c\u00f4ng. Vui l\u00f2ng th\u1eed l\u1ea1i ho\u1eb7c ch\u1ecdn ph\u01b0\u01a1ng th\u1ee9c thanh to\u00e1n kh\u00e1c.";
    }
}
