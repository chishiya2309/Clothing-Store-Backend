package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffCollectionProductsRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffCollectionRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PageResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.StaffCollectionDetailResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.StaffCollectionResponse;

public interface StaffCollectionService {
    PageResponse<StaffCollectionResponse> getCollections(int page, int size, String keyword);
    StaffCollectionDetailResponse getCollectionDetail(Long id);
    StaffCollectionResponse createCollection(StaffCollectionRequest request, String username);
    StaffCollectionResponse updateCollection(Long id, StaffCollectionRequest request, String username);
    void deleteCollection(Long id, String username);

    // Manage products in collection
    StaffCollectionDetailResponse addProductsToCollection(Long id, StaffCollectionProductsRequest request, String username);
    StaffCollectionDetailResponse removeProductsFromCollection(Long id, StaffCollectionProductsRequest request, String username);
}
