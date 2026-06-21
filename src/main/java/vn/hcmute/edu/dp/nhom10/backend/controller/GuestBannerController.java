package vn.hcmute.edu.dp.nhom10.backend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ApiResponse;
import vn.hcmute.edu.dp.nhom10.backend.service.BannerService;

import java.time.OffsetDateTime;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/guest/banners")
@RequiredArgsConstructor
@Tag(name = "Public Banner", description = "Lấy banner active cho trang chủ")
public class GuestBannerController {

    private final BannerService bannerService;

    @GetMapping
    public ApiResponse getActiveBanners() {
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Get active banners successfully")
                .data(new ArrayList<>(bannerService.getActiveBanners()))
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
