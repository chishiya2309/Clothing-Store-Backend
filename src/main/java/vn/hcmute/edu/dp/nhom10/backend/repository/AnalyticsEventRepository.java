package vn.hcmute.edu.dp.nhom10.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.hcmute.edu.dp.nhom10.backend.dto.projection.ExperimentEventReportProjection;
import vn.hcmute.edu.dp.nhom10.backend.entity.AnalyticsEvent;

import java.time.OffsetDateTime;
import java.util.List;

public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, Long> {

    @Query(value = """
            SELECT
                ev.id AS variantId,
                ev.variant_key AS variantKey,
                ev.name AS variantName,
                ae.event_name AS eventName,
                COUNT(ae.id) AS eventCount,
                COUNT(DISTINCT COALESCE(ae.visitor_id, ae.user_id::text, ae.session_id)) AS uniqueVisitors
            FROM analytics_events ae
            JOIN experiment_variants ev ON ev.id = ae.variant_id
            WHERE ae.experiment_id = :experimentId
              AND ae.created_at >= :startDate
              AND ae.created_at <= :endDate
            GROUP BY ev.id, ev.variant_key, ev.name, ae.event_name
            ORDER BY ev.id, ae.event_name
            """, nativeQuery = true)
    List<ExperimentEventReportProjection> findExperimentEventReport(
            @Param("experimentId") Long experimentId,
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate);
}
