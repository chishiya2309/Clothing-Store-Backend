package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import lombok.*;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSuggestionDto implements Serializable {
    private String name;
    private String slug;
}
