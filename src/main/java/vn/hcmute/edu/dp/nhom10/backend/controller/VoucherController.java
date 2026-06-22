package vn.hcmute.edu.dp.nhom10.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.ApplyVoucherRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ApiResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.AppliedVoucherResponse;
import vn.hcmute.edu.dp.nhom10.backend.service.VoucherService;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    @PostMapping("/apply")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse apply(@Valid @RequestBody ApplyVoucherRequest request, Authentication authentication) {
        AppliedVoucherResponse response = voucherService.apply(request, authentication.getName());
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Voucher applied successfully")
                .data(response)
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
