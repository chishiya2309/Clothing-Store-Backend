package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.dto.response.PageResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ProductGridResponse;

public interface ProductService {
    PageResponse<ProductGridResponse> getProductsByCategorySlug(String slug, int page, int size);
}
