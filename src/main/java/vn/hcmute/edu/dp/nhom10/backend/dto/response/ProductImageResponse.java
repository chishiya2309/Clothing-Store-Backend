package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class ProductImageResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String imageUrl;
    private String imageType;
    private Integer displayOrder;
    private String altText;
}
