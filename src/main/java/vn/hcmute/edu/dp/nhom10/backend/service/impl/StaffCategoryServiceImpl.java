package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffCategoryRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.StaffCategoryResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.Category;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.pattern.command.catalog.CatalogCommand;
import vn.hcmute.edu.dp.nhom10.backend.pattern.command.catalog.CatalogCommandExecutor;
import vn.hcmute.edu.dp.nhom10.backend.pattern.policy.category.CategoryDeletionPolicy;
import vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.slug.SlugGenerationStrategy;
import vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.slug.VietnameseSlugGenerationStrategy;
import vn.hcmute.edu.dp.nhom10.backend.pattern.visitor.category.CategoryCycleDetectionVisitor;
import vn.hcmute.edu.dp.nhom10.backend.pattern.visitor.category.CategoryDepthVisitor;
import vn.hcmute.edu.dp.nhom10.backend.pattern.visitor.category.CategoryProductCountVisitor;
import vn.hcmute.edu.dp.nhom10.backend.repository.CategoryRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.StaffCategoryService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffCategoryServiceImpl implements StaffCategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryDeletionPolicy categoryDeletionPolicy;
    private final CatalogCommandExecutor commandExecutor;
    private final SlugGenerationStrategy slugStrategy = new VietnameseSlugGenerationStrategy();

    @Override
    @Transactional(readOnly = true)
    public List<StaffCategoryResponse> getCategoryHierarchy() {
        // Retrieve root categories (parent_id is null)
        List<Category> roots = categoryRepository.findByIsActiveTrueAndParentIsNullOrderByDisplayOrderAsc();
        return roots.stream()
                .map(c -> mapToResponse(c, 1))
                .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = {"categories", "newArrivals", "bestSellers"}, allEntries = true)
    public StaffCategoryResponse createCategory(StaffCategoryRequest request, String username) {
        return commandExecutor.execute(new CatalogCommand<StaffCategoryResponse>() {
            @Override
            public StaffCategoryResponse execute() {
                Category parent = null;
                int depth = 1;
                if (request.getParentId() != null) {
                    parent = categoryRepository.findById(request.getParentId())
                            .orElseThrow(() -> new ResourceNotFoundException("Danh mục cha không tồn tại"));
                    depth = calculateDepth(parent) + 1;
                    if (depth > 3) {
                        throw new InvalidDataException("Vượt quá độ sâu danh mục tối đa (tối đa 3 cấp).");
                    }
                }

                String slug = request.getSlug();
                if (slug == null || slug.isBlank()) {
                    slug = slugStrategy.generate(request.getName());
                }

                Category category = Category.builder()
                        .name(request.getName())
                        .slug(slug)
                        .parent(parent)
                        .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                        .description(request.getDescription())
                        .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                        .build();

                Category saved = categoryRepository.save(category);
                return mapToResponse(saved, depth);
            }

            @Override
            public String getDescription() {
                return "Tạo danh mục mới: " + request.getName();
            }
        }, username);
    }

    @Override
    @CacheEvict(value = {"categories", "newArrivals", "bestSellers"}, allEntries = true)
    public StaffCategoryResponse updateCategory(Long id, StaffCategoryRequest request, String username) {
        return commandExecutor.execute(new CatalogCommand<StaffCategoryResponse>() {
            @Override
            public StaffCategoryResponse execute() {
                Category category = categoryRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Danh mục không tồn tại"));

                Category parent = null;
                int depth = 1;
                if (request.getParentId() != null) {
                    if (request.getParentId().equals(id)) {
                        throw new InvalidDataException("Danh mục cha không thể là chính nó.");
                    }
                    parent = categoryRepository.findById(request.getParentId())
                            .orElseThrow(() -> new ResourceNotFoundException("Danh mục cha không tồn tại"));

                    // Cycle check
                    CategoryCycleDetectionVisitor cycleVisitor = new CategoryCycleDetectionVisitor(request.getParentId());
                    if (cycleVisitor.visit(category)) {
                        throw new InvalidDataException("Phát hiện chu trình vòng lặp trong phân cấp danh mục.");
                    }

                    depth = calculateDepth(parent) + 1;

                    // Deepest child depth check
                    CategoryDepthVisitor depthVisitor = new CategoryDepthVisitor();
                    int subtreeDepth = depthVisitor.visit(category);
                    if (depth + (subtreeDepth - 1) > 3) {
                        throw new InvalidDataException("Cấu trúc cập nhật sẽ vượt quá độ sâu tối đa (tối đa 3 cấp).");
                    }
                }

                String slug = request.getSlug();
                if (slug == null || slug.isBlank()) {
                    slug = slugStrategy.generate(request.getName());
                }

                category.setName(request.getName());
                category.setSlug(slug);
                category.setParent(parent);
                category.setDescription(request.getDescription());
                category.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : category.getDisplayOrder());
                category.setIsActive(request.getIsActive() != null ? request.getIsActive() : category.getIsActive());

                Category saved = categoryRepository.save(category);
                return mapToResponse(saved, depth);
            }

            @Override
            public String getDescription() {
                return "Cập nhật danh mục ID: " + id + ", Tên mới: " + request.getName();
            }
        }, username);
    }

    @Override
    @CacheEvict(value = {"categories", "newArrivals", "bestSellers"}, allEntries = true)
    public void deleteCategory(Long id, String username) {
        commandExecutor.execute(new CatalogCommand<Void>() {
            @Override
            public Void execute() {
                Category category = categoryRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Danh mục không tồn tại"));

                categoryDeletionPolicy.checkCanDelete(category);
                categoryRepository.delete(category);
                return null;
            }

            @Override
            public String getDescription() {
                return "Xóa danh mục ID: " + id;
            }
        }, username);
    }

    private int calculateDepth(Category category) {
        int depth = 1;
        Category current = category;
        while (current.getParent() != null) {
            depth++;
            current = current.getParent();
        }
        return depth;
    }

    private StaffCategoryResponse mapToResponse(Category category, int depth) {
        CategoryProductCountVisitor productVisitor = new CategoryProductCountVisitor();

        return StaffCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .displayOrder(category.getDisplayOrder())
                .isActive(category.getIsActive())
                .depth(depth)
                .recursiveProductCount(productVisitor.visit(category))
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .children(category.getChildren() != null ? category.getChildren().stream()
                        .map(c -> mapToResponse(c, depth + 1))
                        .collect(Collectors.toList()) : List.of())
                .build();
    }
}
