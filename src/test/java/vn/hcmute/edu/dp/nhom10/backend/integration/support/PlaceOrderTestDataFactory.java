package vn.hcmute.edu.dp.nhom10.backend.integration.support;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.ConfirmCheckoutRequestDTO;
import vn.hcmute.edu.dp.nhom10.backend.entity.Address;
import vn.hcmute.edu.dp.nhom10.backend.entity.CartItem;
import vn.hcmute.edu.dp.nhom10.backend.entity.Category;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.entity.Voucher;
import vn.hcmute.edu.dp.nhom10.backend.enums.DiscountType;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.enums.UserRole;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PlaceOrderTestDataFactory {

    private static final BigDecimal DEFAULT_BASE_PRICE = new BigDecimal("100000.00");
    private static final BigDecimal DEFAULT_ADDITIONAL_PRICE = new BigDecimal("20000.00");
    private static final BigDecimal DEFAULT_UNIT_PRICE = new BigDecimal("120000.00");
    private static final BigDecimal DEFAULT_VOUCHER_DISCOUNT = new BigDecimal("30000.00");

    private final EntityManager entityManager;

    @Transactional
    public CheckoutFixture createCheckoutFixture(
            int stockQuantity,
            int cartQuantity,
            boolean withVoucher
    ) {
        ProductVariantFixture variant = createProductVariant(stockQuantity);
        CheckoutActor actor = createActorForVariant(variant.productVariantId(), cartQuantity);
        String voucherCode = withVoucher ? createFixedVoucherCode(DEFAULT_VOUCHER_DISCOUNT, 1) : null;

        return new CheckoutFixture(
                actor.userId(),
                actor.addressId(),
                variant.productVariantId(),
                voucherCode,
                cartQuantity,
                variant.unitPrice(),
                variant.unitPrice().multiply(BigDecimal.valueOf(cartQuantity))
        );
    }

    @Transactional
    public SharedVariantFixture createSharedVariantFixture(
            int stockQuantity,
            int cartQuantity,
            int actorCount
    ) {
        ProductVariantFixture variant = createProductVariant(stockQuantity);
        List<CheckoutActor> actors = new ArrayList<>();
        for (int i = 0; i < actorCount; i++) {
            actors.add(createActorForVariant(variant.productVariantId(), cartQuantity));
        }

        return new SharedVariantFixture(
                variant.productVariantId(),
                variant.unitPrice(),
                actors
        );
    }

    @Transactional
    public ProductVariantFixture createProductVariant(int stockQuantity) {
        String suffix = suffix();
        Category category = Category.builder()
                .name("IT Category " + suffix)
                .slug("it-category-" + suffix)
                .displayOrder(0)
                .isActive(true)
                .build();
        entityManager.persist(category);

        Product product = Product.builder()
                .name("IT Product " + suffix)
                .slug("it-product-" + suffix)
                .category(category)
                .basePrice(DEFAULT_BASE_PRICE)
                .salePrice(null)
                .isActive(true)
                .isFeatured(false)
                .totalSold(0)
                .averageRating(BigDecimal.ZERO)
                .build();
        entityManager.persist(product);

        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .sku("IT-SKU-" + suffix)
                .size("M")
                .color("Blue-" + suffix)
                .stockQuantity(stockQuantity)
                .additionalPrice(DEFAULT_ADDITIONAL_PRICE)
                .isActive(true)
                .build();
        entityManager.persist(variant);
        entityManager.flush();

        return new ProductVariantFixture(
                variant.getId(),
                DEFAULT_UNIT_PRICE,
                stockQuantity
        );
    }

    @Transactional
    public CheckoutActor createActorForVariant(Long productVariantId, int cartQuantity) {
        String suffix = suffix();
        User user = User.builder()
                .email("it-user-" + suffix + "@example.test")
                .passwordHash("password-hash")
                .fullName("Integration User " + suffix)
                .phone("0900000000")
                .role(UserRole.customer)
                .loyaltyPoints(0)
                .authProvider("email")
                .emailVerified(true)
                .isActive(true)
                .build();
        entityManager.persist(user);

        Address address = Address.builder()
                .user(user)
                .recipientName("Integration Receiver " + suffix)
                .phone("0900000001")
                .province("Ho Chi Minh")
                .district("Thu Duc")
                .ward("Linh Trung")
                .streetAddress("1 Vo Van Ngan")
                .isDefault(true)
                .build();
        entityManager.persist(address);

        ProductVariant variant = entityManager.getReference(ProductVariant.class, productVariantId);
        CartItem cartItem = CartItem.builder()
                .user(user)
                .productVariant(variant)
                .quantity(cartQuantity)
                .build();
        entityManager.persist(cartItem);
        entityManager.flush();

        return new CheckoutActor(user.getId(), address.getId(), productVariantId, cartQuantity);
    }

    @Transactional
    public String createFixedVoucherCode(BigDecimal discountAmount, int usageLimit) {
        String code = "ITV-" + suffix();
        Voucher voucher = Voucher.builder()
                .code(code)
                .discountType(DiscountType.fixed_amount)
                .discountValue(discountAmount)
                .maxDiscountAmount(null)
                .minOrderAmount(BigDecimal.ZERO)
                .startDate(OffsetDateTime.now().minusDays(1))
                .endDate(OffsetDateTime.now().plusDays(1))
                .usageLimit(usageLimit)
                .timesUsed(0)
                .isActive(true)
                .build();
        entityManager.persist(voucher);
        entityManager.flush();
        return code;
    }

    private String suffix() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public record CheckoutFixture(
            Long userId,
            Long addressId,
            Long productVariantId,
            String voucherCode,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {
        public ConfirmCheckoutRequestDTO request(PaymentMethod paymentMethod) {
            return new ConfirmCheckoutRequestDTO(addressId, voucherCode, paymentMethod);
        }

        public BigDecimal expectedDiscount() {
            return voucherCode == null ? BigDecimal.ZERO : DEFAULT_VOUCHER_DISCOUNT;
        }
    }

    public record ProductVariantFixture(
            Long productVariantId,
            BigDecimal unitPrice,
            int stockQuantity
    ) {
    }

    public record SharedVariantFixture(
            Long productVariantId,
            BigDecimal unitPrice,
            List<CheckoutActor> actors
    ) {
    }

    public record CheckoutActor(
            Long userId,
            Long addressId,
            Long productVariantId,
            int quantity
    ) {
        public ConfirmCheckoutRequestDTO request(PaymentMethod paymentMethod) {
            return new ConfirmCheckoutRequestDTO(addressId, null, paymentMethod);
        }
    }
}
