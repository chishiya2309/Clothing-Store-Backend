package vn.hcmute.edu.dp.nhom10.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.hcmute.edu.dp.nhom10.backend.entity.Wishlist;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    
    Page<Wishlist> findByUserId(Long userId, Pageable pageable);
    
    Optional<Wishlist> findByUserIdAndProductId(Long userId, Long productId);
    
    boolean existsByUserIdAndProductId(Long userId, Long productId);
    
    List<Wishlist> findByProductId(Long productId);
    
    void deleteByUserIdAndProductId(Long userId, Long productId);
}
