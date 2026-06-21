package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.dto.request.ProductSearchCriteria;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PageResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ProductGridResponse;

import java.util.List;

public interface ProductService {

    PageResponse<ProductGridResponse> getProductsByCategorySlug(String slug, int page, int size);

    /** UC-11, UC-12, UC-13: Tìm kiếm + lọc + sắp xếp sản phẩm */
    PageResponse<ProductGridResponse> searchProducts(ProductSearchCriteria criteria, int page, int size);

    /** UC-11: Autocomplete gợi ý khi gõ từ khóa */
    List<ProductGridResponse> getAutocompleteSuggestions(String keyword, int limit);
}
