package vn.hcmute.edu.dp.nhom10.backend.controller.admin;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ApiResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;
import vn.hcmute.edu.dp.nhom10.backend.pattern.observer.ProductPriceManager;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@Tag(name = "Admin Products", description = "Quản lý sản phẩm (Admin)")
@Slf4j(topic = "ADMIN-PRODUCT-CONTROLLER")
public class AdminProductSaleController {

    private final ProductPriceManager productPriceManager;

    @PostMapping("/{id}/sale")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse setSalePrice(
            @PathVariable Long id,
            @RequestParam BigDecimal salePrice) {
            
        log.info("Setting sale price for product ID {} to {}", id, salePrice);
        Product updatedProduct = productPriceManager.setSalePrice(id, salePrice);

        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Set sale price successfully and notified observers!")
                .data((java.io.Serializable) new java.util.HashMap<>(java.util.Map.of(
                        "productId", updatedProduct.getId(), 
                        "salePrice", updatedProduct.getSalePrice() != null ? updatedProduct.getSalePrice() : "null"
                )))
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
