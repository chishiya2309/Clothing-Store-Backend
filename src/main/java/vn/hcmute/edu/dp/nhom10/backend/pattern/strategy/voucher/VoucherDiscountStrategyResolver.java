package vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.voucher;

import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.enums.DiscountType;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class VoucherDiscountStrategyResolver {

    private final Map<DiscountType, VoucherDiscountStrategy> strategies = new EnumMap<>(DiscountType.class);

    public VoucherDiscountStrategyResolver(List<VoucherDiscountStrategy> strategyList) {
        strategyList.forEach(strategy -> strategies.put(strategy.supports(), strategy));
    }

    public VoucherDiscountStrategy resolve(DiscountType discountType) {
        VoucherDiscountStrategy strategy = strategies.get(discountType);
        if (strategy == null) {
            throw new InvalidDataException("Unsupported voucher discount type: " + discountType);
        }
        return strategy;
    }
}
