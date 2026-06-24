package vn.hcmute.edu.dp.nhom10.backend.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Page<Product> findByCategoryIdInAndIsActiveTrue(List<Long> categoryIds, Pageable pageable);

    Optional<Product> findBySlugAndIsActiveTrue(String slug);

    @Query("""
            SELECT p FROM Product p
            WHERE p.isActive = true
            AND LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            ORDER BY p.totalSold DESC
            """)
    List<Product> findTopByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            SELECT p FROM Product p
            WHERE p.category.id = :categoryId
              AND p.id != :excludeProductId
              AND p.isActive = true
            ORDER BY p.totalSold DESC
            """)
    List<Product> findPopularByCategoryExcluding(
            @Param("categoryId") Long categoryId,
            @Param("excludeProductId") Long excludeProductId,
            Pageable pageable);

    List<Product> findTop8ByIsActiveTrueOrderByCreatedAtDesc();

    Page<Product> findByIsActiveTrue(Pageable pageable);
}
