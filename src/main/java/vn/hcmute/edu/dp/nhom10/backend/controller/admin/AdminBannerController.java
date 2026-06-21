package vn.hcmute.edu.dp.nhom10.backend.controller.admin;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ApiResponse;
import vn.hcmute.edu.dp.nhom10.backend.service.BannerService;

import java.time.OffsetDateTime;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/admin/banners")
@RequiredArgsConstructor
@Tag(name = "Admin Banner", description = "Quản lý banner trang chủ")
@Slf4j(topic = "ADMIN-BANNER-CONTROLLER")
public class AdminBannerController {

    private final BannerService bannerService;

    @GetMapping
    public ApiResponse getAllBanners() {
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Get all banners successfully")
                .data(new ArrayList<>(bannerService.getAllBanners()))
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse createBanner(
            @RequestParam("title") String title,
            @RequestParam(value = "linkUrl", required = false) String linkUrl,
            @RequestParam(value = "displayOrder", defaultValue = "0") Integer displayOrder,
            @RequestParam(value = "isActive", defaultValue = "true") Boolean isActive,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            @RequestParam("image") MultipartFile image) {

        log.info("Creating banner: {}", title);
        return ApiResponse.builder()
                .status(HttpStatus.CREATED.value())
                .message("Banner created successfully")
                .data(bannerService.createBanner(title, linkUrl, displayOrder, isActive, startDate, endDate, image))
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse updateBanner(
            @PathVariable Long id,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "linkUrl", required = false) String linkUrl,
            @RequestParam(value = "displayOrder", required = false) Integer displayOrder,
            @RequestParam(value = "isActive", required = false) Boolean isActive,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        log.info("Updating banner id: {}", id);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Banner updated successfully")
                .data(bannerService.updateBanner(id, title, linkUrl, displayOrder, isActive, startDate, endDate, image))
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse deleteBanner(@PathVariable Long id) {
        log.info("Deleting banner id: {}", id);
        bannerService.deleteBanner(id);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Banner deleted successfully")
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
