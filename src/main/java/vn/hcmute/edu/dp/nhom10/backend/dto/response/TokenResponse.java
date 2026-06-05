package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@Builder
public class TokenResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String accessToken;
    private String refreshToken;
    private long expiresIn;
}
