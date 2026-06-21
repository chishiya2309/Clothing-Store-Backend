package vn.hcmute.edu.dp.nhom10.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByCategoryIdInAndIsActiveTrue(List<Long> categoryIds, Pageable pageable);

    Optional<Product> findBySlugAndIsActiveTrue(String slug);
}
