package vn.hcmute.edu.dp.nhom10.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ApiResponse;
import vn.hcmute.edu.dp.nhom10.backend.service.CollectionService;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/guest/collections")
@RequiredArgsConstructor
@Tag(name = "Public Collection", description = "Các API lấy thông tin bộ sưu tập")
public class GuestCollectionController {

    private final CollectionService collectionService;

    @GetMapping("/{slug}")
    @Operation(summary = "Lấy thông tin bộ sưu tập bằng slug")
    public ApiResponse getCollectionBySlug(@PathVariable String slug) {
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(collectionService.getCollectionBySlug(slug))
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
