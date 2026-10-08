package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import vn.hcmute.edu.dp.nhom10.backend.enums.DiscountType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class VoucherRequestValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void createRequest_cheapestItemFreeAllowsZeroDiscountValue() {
        CreateVoucherRequest request = new CreateVoucherRequest(
                "CHEAPEST",
                DiscountType.cheapest_item_free,
                BigDecimal.ZERO,
                BigDecimal.valueOf(50000),
                BigDecimal.valueOf(50000),
                OffsetDateTime.now().plusMinutes(1),
                OffsetDateTime.now().plusDays(1),
                10,
                true
        );

        Set<ConstraintViolation<CreateVoucherRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void updateRequest_cheapestItemFreeAllowsZeroDiscountValue() {
        UpdateVoucherRequest request = new UpdateVoucherRequest(
                DiscountType.cheapest_item_free,
                BigDecimal.ZERO,
                BigDecimal.valueOf(50000),
                BigDecimal.valueOf(50000),
                OffsetDateTime.now().plusMinutes(1),
                OffsetDateTime.now().plusDays(1),
                10,
                true
        );

        Set<ConstraintViolation<UpdateVoucherRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }
}
