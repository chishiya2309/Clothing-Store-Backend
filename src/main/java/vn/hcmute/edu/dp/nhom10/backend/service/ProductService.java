package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.dto.response.ProductDetailResponse;

public interface ProductService {

    /**
     * Xem chi tiết sản phẩm theo slug.
     * Chỉ trả về sản phẩm đang active.
     *
     * @throws vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException nếu không tìm thấy
     */
    ProductDetailResponse getProductBySlug(String slug);
}
