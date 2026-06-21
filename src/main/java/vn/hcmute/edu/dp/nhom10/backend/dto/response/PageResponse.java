package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> extends PageResponseAbstract {
    private List<T> content;

    @Builder
    public PageResponse(int pageNumber, int pageSize, long totalPages, long totalElements, List<T> content) {
        this.setPageNumber(pageNumber);
        this.setPageSize(pageSize);
        this.setTotalPages(totalPages);
        this.setTotalElements(totalElements);
        this.content = content;
    }
}
