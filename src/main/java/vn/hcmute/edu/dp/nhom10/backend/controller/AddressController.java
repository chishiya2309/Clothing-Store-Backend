package vn.hcmute.edu.dp.nhom10.backend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.AddressRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.AddressResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ApiResponse;
import vn.hcmute.edu.dp.nhom10.backend.service.AddressService;

import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/customer/addresses")
@RequiredArgsConstructor
@Tag(name = "Address", description = "Quản lý địa chỉ giao hàng của khách hàng")
@Slf4j(topic = "ADDRESS-CONTROLLER")
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public ApiResponse getAddresses(Principal principal) {
        log.info("Fetching addresses for user: {}", principal.getName());
        List<AddressResponse> addresses = addressService.getAddresses(principal.getName());

        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách địa chỉ thành công")
                .data((java.io.Serializable) addresses)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse getAddress(Principal principal, @PathVariable Long id) {
        AddressResponse address = addressService.getAddress(principal.getName(), id);

        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Lấy địa chỉ thành công")
                .data(address)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse createAddress(
            Principal principal,
            @Valid @RequestBody AddressRequest request) {

        log.info("Creating address for user: {}", principal.getName());
        AddressResponse address = addressService.createAddress(principal.getName(), request);

        return ApiResponse.builder()
                .status(HttpStatus.CREATED.value())
                .message("Thêm địa chỉ thành công")
                .data(address)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse updateAddress(
            Principal principal,
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request) {

        log.info("Updating address id={} for user: {}", id, principal.getName());
        AddressResponse address = addressService.updateAddress(principal.getName(), id, request);

        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Cập nhật địa chỉ thành công")
                .data(address)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse deleteAddress(Principal principal, @PathVariable Long id) {
        log.info("Deleting address id={} for user: {}", id, principal.getName());
        addressService.deleteAddress(principal.getName(), id);

        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Xóa địa chỉ thành công")
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @PatchMapping("/{id}/default")
    public ApiResponse setDefaultAddress(Principal principal, @PathVariable Long id) {
        log.info("Setting default address id={} for user: {}", id, principal.getName());
        AddressResponse address = addressService.setDefaultAddress(principal.getName(), id);

        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Đặt địa chỉ mặc định thành công")
                .data(address)
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
