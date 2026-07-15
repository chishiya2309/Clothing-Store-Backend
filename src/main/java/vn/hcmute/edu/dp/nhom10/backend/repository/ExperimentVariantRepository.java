package vn.hcmute.edu.dp.nhom10.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.hcmute.edu.dp.nhom10.backend.entity.ExperimentVariant;

import java.util.List;
import java.util.Optional;

public interface ExperimentVariantRepository extends JpaRepository<ExperimentVariant, Long> {

    List<ExperimentVariant> findByExperimentIdAndIsActiveTrueOrderByIdAsc(Long experimentId);

    Optional<ExperimentVariant> findByExperimentIdAndKey(Long experimentId, String key);
}
