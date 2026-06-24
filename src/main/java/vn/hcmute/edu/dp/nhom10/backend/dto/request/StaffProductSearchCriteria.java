package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import vn.hcmute.edu.dp.nhom10.backend.enums.StaffProductStatus;

public record StaffProductSearchCriteria(
        String keyword,
        Long categoryId,
        StaffProductStatus status
) {
}
