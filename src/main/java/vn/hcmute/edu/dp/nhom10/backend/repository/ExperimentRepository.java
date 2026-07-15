package vn.hcmute.edu.dp.nhom10.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.hcmute.edu.dp.nhom10.backend.entity.Experiment;

import java.util.Optional;

public interface ExperimentRepository extends JpaRepository<Experiment, Long> {

    Optional<Experiment> findByKey(String key);

    boolean existsByKey(String key);
}
