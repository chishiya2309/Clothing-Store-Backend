package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.dto.response.ProductDetailResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.ProductSearchCriteria;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PageResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ProductGridResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ProductSearchDto;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ProductSuggestionDto;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    /**
     * Xem chi tiết sản phẩm theo slug.
     * Chỉ trả về sản phẩm đang active.
     *
     * @throws vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException nếu không tìm thấy
     */
    ProductDetailResponse getProductBySlug(String slug);

    PageResponse<ProductGridResponse> getProductsByCategorySlug(String slug, int page, int size);

    /** UC-11, UC-12, UC-13: Tìm kiếm + lọc + sắp xếp sản phẩm */
    PageResponse<ProductGridResponse> searchProducts(ProductSearchCriteria criteria, int page, int size);

    /** UC-11: Autocomplete gợi ý khi gõ từ khóa */
    List<ProductGridResponse> getAutocompleteSuggestions(String keyword, int limit);

    /** Lấy danh sách Hàng Mới Về (áp dụng cache) */
    List<ProductGridResponse> getNewArrivals();

    /** Lấy danh sách Sản phẩm bán chạy nhất */
    List<ProductGridResponse> getBestSellers(int limit);

    /** Tìm kiếm sản phẩm nâng cao / full-text search */
    PageResponse<ProductSearchDto> searchProductsFullText(
            String q, String sortBy, int page, int size,
            String categorySlug, BigDecimal minPrice, BigDecimal maxPrice,
            List<String> colors, List<String> sizes, List<String> brands);

    /** Autocomplete trả về danh sách gợi ý sản phẩm rút gọn */
    List<ProductSuggestionDto> getAutocompleteSuggestionsList(String q);
}
