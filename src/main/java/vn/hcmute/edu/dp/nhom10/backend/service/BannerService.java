package vn.hcmute.edu.dp.nhom10.backend.service;

import org.springframework.web.multipart.MultipartFile;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.BannerResponse;

import java.time.OffsetDateTime;
import java.util.List;

public interface BannerService {
    List<BannerResponse> getAllBanners();
    List<BannerResponse> getActiveBanners();
    BannerResponse createBanner(String title, String linkUrl, Integer displayOrder,
                                Boolean isActive, OffsetDateTime startDate, OffsetDateTime endDate,
                                MultipartFile image);
    BannerResponse updateBanner(Long id, String title, String linkUrl, Integer displayOrder,
                                Boolean isActive, OffsetDateTime startDate, OffsetDateTime endDate,
                                MultipartFile image);
    void deleteBanner(Long id);
}
