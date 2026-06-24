package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import lombok.*;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionResponse implements Serializable {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String bannerUrl;
}
