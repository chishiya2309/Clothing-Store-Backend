package vn.hcmute.edu.dp.nhom10.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.hcmute.edu.dp.nhom10.backend.entity.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByIsActiveTrueAndParentIsNullOrderByDisplayOrderAsc();

    Optional<Category> findBySlugAndIsActiveTrue(String slug);

    @Query(value = """
            WITH RECURSIVE category_tree AS (
                SELECT id FROM categories WHERE id = :categoryId
                UNION ALL
                SELECT c.id FROM categories c
                INNER JOIN category_tree ct ON c.parent_id = ct.id
            )
            SELECT id FROM category_tree
            """, nativeQuery = true)
    List<Long> findAllDescendantIds(@Param("categoryId") Long categoryId);
}
