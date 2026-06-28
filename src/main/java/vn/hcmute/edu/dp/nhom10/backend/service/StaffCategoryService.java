package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffCategoryRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.StaffCategoryResponse;

import java.util.List;

public interface StaffCategoryService {
    List<StaffCategoryResponse> getCategoryHierarchy();
    StaffCategoryResponse createCategory(StaffCategoryRequest request, String username);
    StaffCategoryResponse updateCategory(Long id, StaffCategoryRequest request, String username);
    void deleteCategory(Long id, String username);
    void updateCategoryOrders(List<vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffCategoryOrderRequest> requests, String username);
}
