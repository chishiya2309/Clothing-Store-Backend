package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse implements Serializable {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private List<CategoryResponse> children;
}
