package vn.hcmute.edu.dp.nhom10.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.hcmute.edu.dp.nhom10.backend.entity.Collection;

import java.util.Optional;

public interface CollectionRepository extends JpaRepository<Collection, Long> {
    Optional<Collection> findBySlugAndIsActiveTrue(String slug);
}
