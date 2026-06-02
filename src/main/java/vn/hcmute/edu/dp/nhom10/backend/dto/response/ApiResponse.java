package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Setter
@Getter
@Builder
public class ApiResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private int status;
    private String message;
    private Serializable data;
    private OffsetDateTime timestamp;
}