package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.InventoryReportResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PageResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.Category;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;
import vn.hcmute.edu.dp.nhom10.backend.enums.InventoryReportStatus;
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductVariantRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.InventoryReportService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryReportServiceImpl implements InventoryReportService {
    private static final int LOW_STOCK_THRESHOLD = 10;
    private static final int MAX_PAGE_SIZE = 100;
    private static final String DEFAULT_SORT = "stockAsc";

    private final ProductVariantRepository productVariantRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InventoryReportResponse> getInventoryReport(
            InventoryReportStatus status,
            Long categoryId,
            String keyword,
            int page,
            int size,
            String sortBy
    ) {
        validatePageRequest(page, size);

        StockRange stockRange = resolveStockRange(status);
        PageRequest pageRequest = PageRequest.of(page, size, resolveSort(sortBy));
        Page<ProductVariant> variantPage = productVariantRepository.findInventoryReport(
                categoryId,
                normalizeKeyword(keyword),
                stockRange.minStock(),
                stockRange.maxStock(),
                pageRequest
        );

        List<InventoryReportResponse> content = variantPage.getContent().stream()
                .map(this::toResponse)
                .toList();

        return PageResponse.<InventoryReportResponse>builder()
                .pageNumber(variantPage.getNumber())
                .pageSize(variantPage.getSize())
                .totalElements(variantPage.getTotalElements())
                .totalPages(variantPage.getTotalPages())
                .content(content)
                .build();
    }

    private void validatePageRequest(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be greater than or equal to 0");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("size must be between 1 and " + MAX_PAGE_SIZE);
        }
    }

    private StockRange resolveStockRange(InventoryReportStatus status) {
        if (status == null) {
            return new StockRange(null, null);
        }

        return switch (status) {
            case OUT_OF_STOCK -> new StockRange(0, 0);
            case LOW_STOCK -> new StockRange(1, LOW_STOCK_THRESHOLD - 1);
            case IN_STOCK -> new StockRange(LOW_STOCK_THRESHOLD, null);
        };
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }

    private Sort resolveSort(String sortBy) {
        String normalized = (sortBy == null || sortBy.isBlank()) ? DEFAULT_SORT : sortBy.trim();

        return switch (normalized) {
            case "stockAsc" -> Sort.by(Sort.Direction.ASC, "stockQuantity").and(Sort.by("id"));
            case "stockDesc" -> Sort.by(Sort.Direction.DESC, "stockQuantity").and(Sort.by("id"));
            case "productNameAsc" -> Sort.by(Sort.Direction.ASC, "product.name")
                    .and(Sort.by(Sort.Direction.ASC, "size"))
                    .and(Sort.by(Sort.Direction.ASC, "color"))
                    .and(Sort.by("id"));
            case "productNameDesc" -> Sort.by(Sort.Direction.DESC, "product.name")
                    .and(Sort.by(Sort.Direction.ASC, "size"))
                    .and(Sort.by(Sort.Direction.ASC, "color"))
                    .and(Sort.by("id"));
            case "updatedDesc" -> Sort.by(Sort.Direction.DESC, "updatedAt").and(Sort.by("id"));
            case "skuAsc" -> Sort.by(Sort.Direction.ASC, "sku").and(Sort.by("id"));
            default -> throw new IllegalArgumentException("Unsupported sortBy: " + sortBy);
        };
    }

    private InventoryReportResponse toResponse(ProductVariant variant) {
        Product product = variant.getProduct();
        if (product == null) {
            throw new IllegalStateException("Inventory report product must not be null");
        }

        Category category = product.getCategory();
        InventoryReportStatus status = resolveStatus(variant.getStockQuantity());

        return new InventoryReportResponse(
                buildProductCode(product),
                product.getName(),
                buildVariantInfo(variant),
                variant.getStockQuantity(),
                status,
                resolveStatusLabel(status),
                category != null ? category.getId() : null,
                category != null ? category.getName() : null,
                variant.getId(),
                variant.getSku()
        );
    }

    private String buildProductCode(Product product) {
        if (product.getId() == null) {
            throw new IllegalStateException("Inventory report product id must not be null");
        }
        return "SP" + String.format("%03d", product.getId());
    }

    private String buildVariantInfo(ProductVariant variant) {
        String size = normalizePart(variant.getSize());
        String color = normalizePart(variant.getColor());

        if (size.isEmpty()) {
            return color;
        }
        if (color.isEmpty()) {
            return size;
        }
        return size + " / " + color;
    }

    private String normalizePart(String value) {
        return value == null ? "" : value.trim();
    }

    private InventoryReportStatus resolveStatus(Integer stockQuantity) {
        if (stockQuantity == null || stockQuantity < 0) {
            throw new IllegalStateException("Invalid inventory stock quantity: " + stockQuantity);
        }

        if (stockQuantity == 0) {
            return InventoryReportStatus.OUT_OF_STOCK;
        }

        if (stockQuantity < LOW_STOCK_THRESHOLD) {
            return InventoryReportStatus.LOW_STOCK;
        }

        return InventoryReportStatus.IN_STOCK;
    }

    private String resolveStatusLabel(InventoryReportStatus status) {
        return switch (status) {
            case OUT_OF_STOCK -> "Hết hàng";
            case LOW_STOCK -> "Sắp hết";
            case IN_STOCK -> "Còn hàng";
        };
    }

    private record StockRange(Integer minStock, Integer maxStock) {
    }
}
