package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.CollectionResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.Collection;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.repository.CollectionRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.CollectionService;

@Service
@RequiredArgsConstructor
public class CollectionServiceImpl implements CollectionService {

    private final CollectionRepository collectionRepository;

    @Override
    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(value = "collections", key = "#slug")
    public CollectionResponse getCollectionBySlug(String slug) {
        Collection collection = collectionRepository.findBySlugAndIsActiveTrue(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found with slug: " + slug));

        return CollectionResponse.builder()
                .id(collection.getId())
                .name(collection.getName())
                .slug(collection.getSlug())
                .description(collection.getDescription())
                .bannerUrl(collection.getBannerUrl())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(value = "collections", key = "'all_active'")
    public java.util.List<CollectionResponse> getActiveCollections() {
        return collectionRepository.findAllByIsActiveTrue().stream()
                .map(collection -> CollectionResponse.builder()
                        .id(collection.getId())
                        .name(collection.getName())
                        .slug(collection.getSlug())
                        .description(collection.getDescription())
                        .bannerUrl(collection.getBannerUrl())
                        .build())
                .toList();
    }
}
