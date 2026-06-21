package vn.hcmute.edu.dp.nhom10.backend.controller.staff;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.CreateVoucherRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.UpdateVoucherRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ApiResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.VoucherResponse;
import vn.hcmute.edu.dp.nhom10.backend.service.VoucherService;

import java.util.ArrayList;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/staff/vouchers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STAFF')")
public class StaffVoucherController {

    private final VoucherService voucherService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse create(@Valid @RequestBody CreateVoucherRequest request) {
        VoucherResponse response = voucherService.create(request);
        return ApiResponse.builder()
                .status(HttpStatus.CREATED.value())
                .message("Voucher created successfully")
                .data(response)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @GetMapping
    public ApiResponse getAll() {
        List<VoucherResponse> response = voucherService.getAll();
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Vouchers retrieved successfully")
                .data(new ArrayList<>(response))
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse getById(@PathVariable Long id) {
        VoucherResponse response = voucherService.getById(id);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Voucher retrieved successfully")
                .data(response)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse update(@PathVariable Long id, @Valid @RequestBody UpdateVoucherRequest request) {
        VoucherResponse response = voucherService.update(id, request);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Voucher updated successfully")
                .data(response)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse deleteOrDeactivate(@PathVariable Long id) {
        voucherService.deleteOrDeactivate(id);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Voucher deleted or deactivated successfully")
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
