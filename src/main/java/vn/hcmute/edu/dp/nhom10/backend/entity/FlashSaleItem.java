package vn.hcmute.edu.dp.nhom10.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "flash_sale_items",
       uniqueConstraints = @UniqueConstraint(columnNames = {"campaign_id", "product_id"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class FlashSaleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private FlashSaleCampaign campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "flash_sale_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal flashSalePrice;

    @Column(nullable = false)
    private Integer quota;

    @Column(name = "reserved_quantity", nullable = false)
    @Builder.Default
    private Integer reservedQuantity = 0;

    @Column(name = "sold_quantity", nullable = false)
    @Builder.Default
    private Integer soldQuantity = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "flashSaleItem")
    @Builder.Default
    private List<FlashSaleReservation> reservations = new ArrayList<>();
}
