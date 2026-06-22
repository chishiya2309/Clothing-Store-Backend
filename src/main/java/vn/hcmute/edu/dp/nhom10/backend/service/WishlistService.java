package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.dto.response.PageResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ProductGridResponse;

public interface WishlistService {
    PageResponse<ProductGridResponse> getUserWishlist(String email, int page, int size);
    
    void toggleWishlist(String email, Long productId);
    
    boolean checkWishlist(String email, Long productId);
    
    java.util.List<Long> getWishlistProductIds(String email);
}
