package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffCreateProductRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffProductSearchCriteria;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffUpdateProductRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffUpdateProductVisibilityRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffUpdateStockRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PageResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.StaffProductDetailResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.StaffProductListItemResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.StaffStockUpdateResponse;

public interface StaffProductService {

    PageResponse<StaffProductListItemResponse> getProducts(
            StaffProductSearchCriteria criteria,
            int page,
            int size,
            String sortBy,
            String sortDir
    );

    StaffProductDetailResponse getProductDetail(Long productId);

    StaffProductDetailResponse createProduct(StaffCreateProductRequest request);

    StaffProductDetailResponse updateProduct(Long productId, StaffUpdateProductRequest request);

    StaffProductDetailResponse updateVisibility(Long productId, StaffUpdateProductVisibilityRequest request);

    void deleteProduct(Long productId);

    StaffStockUpdateResponse updateStock(Long productId, Long variantId, StaffUpdateStockRequest request);
}
