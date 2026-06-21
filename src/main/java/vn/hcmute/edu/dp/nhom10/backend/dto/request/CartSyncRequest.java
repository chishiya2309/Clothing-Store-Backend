package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CartSyncRequest(
        @NotNull(message = "Items list cannot be null")
        @Valid
        List<CartSyncItem> items
) {
}
