package vn.hcmute.edu.dp.nhom10.backend.dto.projection;

public interface ExperimentEventReportProjection {

    Long getVariantId();

    String getVariantKey();

    String getVariantName();

    String getEventName();

    Long getEventCount();

    Long getUniqueVisitors();
}
