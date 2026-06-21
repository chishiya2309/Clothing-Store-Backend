package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.BannerResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.Banner;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.repository.BannerRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.BannerService;
import vn.hcmute.edu.dp.nhom10.backend.service.S3Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "BANNER-SERVICE")
public class BannerServiceImpl implements BannerService {

    private final BannerRepository bannerRepository;
    private final S3Service s3Service;

    @Override
    @Transactional(readOnly = true)
    public List<BannerResponse> getAllBanners() {
        return bannerRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BannerResponse> getActiveBanners() {
        return bannerRepository.findActiveBanners(OffsetDateTime.now())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BannerResponse createBanner(String title, String linkUrl, Integer displayOrder,
                                       Boolean isActive, OffsetDateTime startDate, OffsetDateTime endDate,
                                       MultipartFile image) {
        String imageUrl = s3Service.uploadFile("banners", image);

        Banner banner = Banner.builder()
                .title(title)
                .imageUrl(imageUrl)
                .linkUrl(linkUrl)
                .displayOrder(displayOrder != null ? displayOrder : 0)
                .isActive(isActive != null ? isActive : true)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        Banner saved = bannerRepository.save(banner);
        log.info("Created banner: {} (id={})", saved.getTitle(), saved.getId());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public BannerResponse updateBanner(Long id, String title, String linkUrl, Integer displayOrder,
                                       Boolean isActive, OffsetDateTime startDate, OffsetDateTime endDate,
                                       MultipartFile image) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy banner với id: " + id));

        if (title != null) banner.setTitle(title);
        if (linkUrl != null) banner.setLinkUrl(linkUrl);
        if (displayOrder != null) banner.setDisplayOrder(displayOrder);
        if (isActive != null) banner.setIsActive(isActive);
        if (startDate != null) banner.setStartDate(startDate);
        if (endDate != null) banner.setEndDate(endDate);

        if (image != null && !image.isEmpty()) {
            // Delete old image from S3
            s3Service.deleteFile(banner.getImageUrl());
            // Upload new image
            String newImageUrl = s3Service.uploadFile("banners", image);
            banner.setImageUrl(newImageUrl);
        }

        Banner saved = bannerRepository.save(banner);
        log.info("Updated banner: {} (id={})", saved.getTitle(), saved.getId());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void deleteBanner(Long id) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy banner với id: " + id));

        s3Service.deleteFile(banner.getImageUrl());
        bannerRepository.delete(banner);
        log.info("Deleted banner: {} (id={})", banner.getTitle(), id);
    }

    private BannerResponse mapToResponse(Banner banner) {
        return BannerResponse.builder()
                .id(banner.getId())
                .title(banner.getTitle())
                .imageUrl(banner.getImageUrl())
                .linkUrl(banner.getLinkUrl())
                .displayOrder(banner.getDisplayOrder())
                .isActive(banner.getIsActive())
                .startDate(banner.getStartDate())
                .endDate(banner.getEndDate())
                .createdAt(banner.getCreatedAt())
                .build();
    }
}
