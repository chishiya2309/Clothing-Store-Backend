package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.UserRole;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Map;

@Getter
@Setter
@Builder
public class StaffOrderStatusTimelineResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private OrderStatus fromStatus;
    private OrderStatus toStatus;
    private Long changedById;
    private String changedByName;
    private String changedByEmail;
    private UserRole changedByRole;
    private String actorLabel;
    private String reason;
    private Map<String, Object> metadata;
    private OffsetDateTime createdAt;
}
