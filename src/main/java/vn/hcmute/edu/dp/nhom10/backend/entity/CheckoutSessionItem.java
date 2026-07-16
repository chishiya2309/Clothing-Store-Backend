package vn.hcmute.edu.dp.nhom10.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import vn.hcmute.edu.dp.nhom10.backend.enums.PriceSource;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "checkout_session_items")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CheckoutSessionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checkout_session_id", nullable = false)
    private CheckoutSession checkoutSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "variant_info", nullable = false, length = 100)
    private String variantInfo;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flash_sale_item_id")
    private FlashSaleItem flashSaleItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "price_source", nullable = false, length = 20)
    @Builder.Default
    private PriceSource priceSource = PriceSource.REGULAR;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
