package vn.hcmute.edu.dp.nhom10.backend.pattern.chain.product;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class ProductPriceValidationHandler extends ProductValidationHandler {

    @Override
    protected void validate(ProductValidationContext context) {
        BigDecimal basePrice = context.getBasePrice();
        BigDecimal salePrice = context.getSalePrice();

        if (salePrice != null && basePrice != null && salePrice.compareTo(basePrice) > 0) {
            throw new IllegalArgumentException("Giá bán khuyến mãi không được lớn hơn giá gốc");
        }
    }
}
