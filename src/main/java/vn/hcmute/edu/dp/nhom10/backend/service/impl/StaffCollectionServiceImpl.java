package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffCollectionProductsRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffCollectionRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.*;
import vn.hcmute.edu.dp.nhom10.backend.entity.Collection;
import vn.hcmute.edu.dp.nhom10.backend.entity.CollectionProduct;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductImage;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.pattern.command.catalog.CatalogCommand;
import vn.hcmute.edu.dp.nhom10.backend.pattern.command.catalog.CatalogCommandExecutor;
import vn.hcmute.edu.dp.nhom10.backend.pattern.state.collection.CollectionStateResolver;
import vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.slug.SlugGenerationStrategy;
import vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.slug.VietnameseSlugGenerationStrategy;
import vn.hcmute.edu.dp.nhom10.backend.repository.CollectionRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.StaffCollectionService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffCollectionServiceImpl implements StaffCollectionService {

    private final CollectionRepository collectionRepository;
    private final ProductRepository productRepository;
    private final CollectionStateResolver stateResolver;
    private final CatalogCommandExecutor commandExecutor;
    private final SlugGenerationStrategy slugStrategy = new VietnameseSlugGenerationStrategy();

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StaffCollectionResponse> getCollections(int page, int size, String keyword) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        // For simplicity: JPA default page findAll, we can add specifications if searching is needed
        Page<Collection> collectionPage = collectionRepository.findAll(pageRequest);

        List<StaffCollectionResponse> content = collectionPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<StaffCollectionResponse>builder()
                .content(content)
                .pageNumber(collectionPage.getNumber())
                .pageSize(collectionPage.getSize())
                .totalElements(collectionPage.getTotalElements())
                .totalPages(collectionPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public StaffCollectionDetailResponse getCollectionDetail(Long id) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bộ sưu tập không tồn tại"));

        List<ProductGridResponse> products = collection.getCollectionProducts().stream()
                .map(cp -> mapProductToGrid(cp.getProduct()))
                .collect(Collectors.toList());

        return StaffCollectionDetailResponse.builder()
                .collection(mapToResponse(collection))
                .products(products)
                .build();
    }

    @Override
    @CacheEvict(value = "collections", allEntries = true)
    public StaffCollectionResponse createCollection(StaffCollectionRequest request, String username) {
        return commandExecutor.execute(new CatalogCommand<StaffCollectionResponse>() {
            @Override
            public StaffCollectionResponse execute() {
                String slug = request.getSlug();
                if (slug == null || slug.isBlank()) {
                    slug = slugStrategy.generate(request.getName());
                }

                Collection collection = Collection.builder()
                        .name(request.getName())
                        .slug(slug)
                        .description(request.getDescription())
                        .bannerUrl(request.getBannerUrl())
                        .startDate(request.getStartDate())
                        .endDate(request.getEndDate())
                        .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                        .build();

                Collection saved = collectionRepository.save(collection);
                return mapToResponse(saved);
            }

            @Override
            public String getDescription() {
                return "Tạo bộ sưu tập mới: " + request.getName();
            }
        }, username);
    }

    @Override
    @CacheEvict(value = "collections", allEntries = true)
    public StaffCollectionResponse updateCollection(Long id, StaffCollectionRequest request, String username) {
        return commandExecutor.execute(new CatalogCommand<StaffCollectionResponse>() {
            @Override
            public StaffCollectionResponse execute() {
                Collection collection = collectionRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Bộ sưu tập không tồn tại"));

                String slug = request.getSlug();
                if (slug == null || slug.isBlank()) {
                    slug = slugStrategy.generate(request.getName());
                }

                collection.setName(request.getName());
                collection.setSlug(slug);
                collection.setDescription(request.getDescription());
                collection.setBannerUrl(request.getBannerUrl());
                collection.setStartDate(request.getStartDate());
                collection.setEndDate(request.getEndDate());
                collection.setIsActive(request.getIsActive() != null ? request.getIsActive() : collection.getIsActive());

                Collection saved = collectionRepository.save(collection);
                return mapToResponse(saved);
            }

            @Override
            public String getDescription() {
                return "Cập nhật bộ sưu tập ID: " + id + ", Tên mới: " + request.getName();
            }
        }, username);
    }

    @Override
    @CacheEvict(value = "collections", allEntries = true)
    public void deleteCollection(Long id, String username) {
        commandExecutor.execute(new CatalogCommand<Void>() {
            @Override
            public Void execute() {
                Collection collection = collectionRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Bộ sưu tập không tồn tại"));
                collectionRepository.delete(collection);
                return null;
            }

            @Override
            public String getDescription() {
                return "Xóa bộ sưu tập ID: " + id;
            }
        }, username);
    }

    @Override
    @CacheEvict(value = "collections", allEntries = true)
    public StaffCollectionDetailResponse addProductsToCollection(Long id, StaffCollectionProductsRequest request, String username) {
        return commandExecutor.execute(new CatalogCommand<StaffCollectionDetailResponse>() {
            @Override
            public StaffCollectionDetailResponse execute() {
                Collection collection = collectionRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Bộ sưu tập không tồn tại"));

                for (Long prodId : request.getProductIds()) {
                    boolean alreadyExists = collection.getCollectionProducts().stream()
                            .anyMatch(cp -> cp.getProduct().getId().equals(prodId));
                    if (!alreadyExists) {
                        Product product = productRepository.findById(prodId)
                                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại ID: " + prodId));

                        CollectionProduct cp = CollectionProduct.builder()
                                .collection(collection)
                                .product(product)
                                .displayOrder(0)
                                .build();
                        collection.getCollectionProducts().add(cp);
                    }
                }

                Collection saved = collectionRepository.save(collection);
                return getCollectionDetail(saved.getId());
            }

            @Override
            public String getDescription() {
                return "Thêm sản phẩm vào bộ sưu tập ID: " + id + ", Danh sách sản phẩm: " + request.getProductIds();
            }
        }, username);
    }

    @Override
    @CacheEvict(value = "collections", allEntries = true)
    public StaffCollectionDetailResponse removeProductsFromCollection(Long id, StaffCollectionProductsRequest request, String username) {
        return commandExecutor.execute(new CatalogCommand<StaffCollectionDetailResponse>() {
            @Override
            public StaffCollectionDetailResponse execute() {
                Collection collection = collectionRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Bộ sưu tập không tồn tại"));

                collection.getCollectionProducts().removeIf(cp -> request.getProductIds().contains(cp.getProduct().getId()));

                Collection saved = collectionRepository.save(collection);
                return getCollectionDetail(saved.getId());
            }

            @Override
            public String getDescription() {
                return "Xóa sản phẩm khỏi bộ sưu tập ID: " + id + ", Danh sách sản phẩm: " + request.getProductIds();
            }
        }, username);
    }

    private StaffCollectionResponse mapToResponse(Collection collection) {
        return StaffCollectionResponse.builder()
                .id(collection.getId())
                .name(collection.getName())
                .slug(collection.getSlug())
                .description(collection.getDescription())
                .bannerUrl(collection.getBannerUrl())
                .startDate(collection.getStartDate())
                .endDate(collection.getEndDate())
                .isActive(collection.getIsActive())
                .statusState(stateResolver.resolve(collection).name())
                .productCount(collection.getCollectionProducts() != null ? collection.getCollectionProducts().size() : 0)
                .createdAt(collection.getCreatedAt())
                .updatedAt(collection.getUpdatedAt())
                .build();
    }

    private ProductGridResponse mapProductToGrid(Product product) {
        String thumbnail = null;
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            thumbnail = product.getImages().stream()
                    .filter(img -> img.getImageType() == vn.hcmute.edu.dp.nhom10.backend.enums.ImageType.thumbnail)
                    .findFirst()
                    .map(ProductImage::getImageUrl)
                    .orElse(product.getImages().get(0).getImageUrl());
        }

        List<String> colors = null;
        if (product.getVariants() != null) {
            colors = product.getVariants().stream()
                    .map(ProductVariant::getColor)
                    .distinct()
                    .collect(Collectors.toList());
        }

        return ProductGridResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .basePrice(product.getBasePrice())
                .salePrice(product.getSalePrice())
                .thumbnailUrl(thumbnail)
                .colors(colors)
                .isActive(product.getIsActive())
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .build();
    }
}
