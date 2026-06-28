package vn.hcmute.edu.dp.nhom10.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.hcmute.edu.dp.nhom10.backend.entity.CollectionProduct;

import java.util.List;
import java.util.Optional;

public interface CollectionProductRepository extends JpaRepository<CollectionProduct, Long> {
    
    @Query("SELECT cp FROM CollectionProduct cp JOIN FETCH cp.product p WHERE cp.collection.id = :collectionId ORDER BY cp.displayOrder ASC")
    List<CollectionProduct> findByCollectionIdWithProduct(@Param("collectionId") Long collectionId);

    Optional<CollectionProduct> findByCollectionIdAndProductId(Long collectionId, Long productId);

    boolean existsByCollectionIdAndProductId(Long collectionId, Long productId);
    
    @Query("SELECT COALESCE(MAX(cp.displayOrder), 0) FROM CollectionProduct cp WHERE cp.collection.id = :collectionId")
    Integer getMaxDisplayOrderByCollectionId(@Param("collectionId") Long collectionId);
}
