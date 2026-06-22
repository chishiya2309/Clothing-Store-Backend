package vn.hcmute.edu.dp.nhom10.backend.pattern.facade.checkout;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.AddressSnapshot;
import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.CheckoutData;
import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.CheckoutItemSnapshot;
import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.ReservedCheckoutResult;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.ConfirmCheckoutRequestDTO;
import vn.hcmute.edu.dp.nhom10.backend.entity.CheckoutSession;
import vn.hcmute.edu.dp.nhom10.backend.entity.CheckoutSessionItem;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.entity.Voucher;
import vn.hcmute.edu.dp.nhom10.backend.entity.VoucherReservation;
import vn.hcmute.edu.dp.nhom10.backend.enums.CheckoutSessionStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.repository.CheckoutSessionItemRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.CheckoutSessionRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.UserRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.VoucherReservationRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.CheckoutDataService;
import vn.hcmute.edu.dp.nhom10.backend.service.CheckoutService;
import vn.hcmute.edu.dp.nhom10.backend.service.InventoryReservationService;
import vn.hcmute.edu.dp.nhom10.backend.service.VoucherReservationService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckoutServiceFacade implements CheckoutService {

    private static final int CHECKOUT_CODE_RETRY_LIMIT = 5;

    private final CheckoutDataService checkoutDataService;
    private final InventoryReservationService inventoryReservationService;
    private final VoucherReservationService voucherService;
    private final CheckoutSessionRepository checkoutSessionRepository;
    private final CheckoutSessionItemRepository checkoutSessionItemRepository;
    private final UserRepository userRepository;
    private final VoucherReservationRepository voucherReservationRepository;
    private final EntityManager entityManager;

    @Value("${checkout.reservation-ttl-minutes:15}")
    private long reservationTtlMinutes;

    @Override
    @Transactional
    public ReservedCheckoutResult prepareCheckout(ConfirmCheckoutRequestDTO requestDTO, Long userId) {
        validateRequest(requestDTO, userId);

        CheckoutData checkoutData = checkoutDataService.getCheckoutData(userId, requestDTO.addressId());
        validateCheckoutData(checkoutData);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(reservationTtlMinutes);
        BigDecimal subtotal = requireAmount(checkoutData.subtotal(), "Subtotal");
        BigDecimal shippingFee = requireAmount(checkoutData.shippingFee(), "Shipping fee");
        BigDecimal initialTotalAmount = calculateTotalAmount(subtotal, shippingFee, BigDecimal.ZERO);

        CheckoutSession checkoutSession = createCheckoutSession(
                requestDTO,
                checkoutData,
                user,
                subtotal,
                shippingFee,
                initialTotalAmount,
                expiresAt
        );
        final CheckoutSession savedCheckoutSession = checkoutSessionRepository.save(checkoutSession);

        inventoryReservationService.reserveStock(savedCheckoutSession.getId(), checkoutData.items(), expiresAt);

        String voucherCode = normalizeVoucherCode(requestDTO.voucherCode());
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (voucherCode != null) {
            discountAmount = requireAmount(
                    voucherService.reserveVoucher(savedCheckoutSession.getId(), voucherCode, subtotal, expiresAt),
                    "Discount amount"
            );
            savedCheckoutSession.setVoucher(findReservedVoucher(savedCheckoutSession.getId()));
        }

        BigDecimal totalAmount = calculateTotalAmount(subtotal, shippingFee, discountAmount);
        List<CheckoutSessionItem> sessionItems = checkoutData.items().stream()
                .map(item -> toCheckoutSessionItem(savedCheckoutSession, item))
                .toList();
        checkoutSessionItemRepository.saveAll(sessionItems);

        savedCheckoutSession.setDiscountAmount(discountAmount);
        savedCheckoutSession.setTotalAmount(totalAmount);
        savedCheckoutSession.setStatus(CheckoutSessionStatus.reserved);
        CheckoutSession reservedCheckoutSession = checkoutSessionRepository.save(savedCheckoutSession);

        return new ReservedCheckoutResult(
                reservedCheckoutSession.getId(),
                reservedCheckoutSession.getCheckoutCode(),
                reservedCheckoutSession.getPaymentMethod(),
                reservedCheckoutSession.getSubtotal(),
                reservedCheckoutSession.getShippingFee(),
                reservedCheckoutSession.getDiscountAmount(),
                reservedCheckoutSession.getTotalAmount(),
                reservedCheckoutSession.getExpiresAt()
        );
    }

    private void validateRequest(ConfirmCheckoutRequestDTO requestDTO, Long userId) {
        if (requestDTO == null) {
            throw new IllegalArgumentException("Checkout request is required");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (requestDTO.addressId() == null) {
            throw new IllegalArgumentException("Address ID is required");
        }
        if (requestDTO.paymentMethod() == null) {
            throw new IllegalArgumentException("Payment method is required");
        }
        if (requestDTO.paymentMethod() != PaymentMethod.cod
                && requestDTO.paymentMethod() != PaymentMethod.vnpay
                && requestDTO.paymentMethod() != PaymentMethod.momo) {
            throw new IllegalArgumentException("Payment method is not supported");
        }
        if (reservationTtlMinutes <= 0) {
            throw new InvalidDataException("Checkout reservation TTL must be greater than 0");
        }
    }

    private void validateCheckoutData(CheckoutData checkoutData) {
        if (checkoutData == null) {
            throw new InvalidDataException("Checkout data is required");
        }
        if (checkoutData.addressSnapshot() == null) {
            throw new InvalidDataException("Checkout address snapshot is required");
        }
        if (checkoutData.isEmpty()) {
            throw new InvalidDataException("Checkout items must not be empty");
        }
    }

    private CheckoutSession createCheckoutSession(
            ConfirmCheckoutRequestDTO requestDTO,
            CheckoutData checkoutData,
            User user,
            BigDecimal subtotal,
            BigDecimal shippingFee,
            BigDecimal initialTotalAmount,
            OffsetDateTime expiresAt
    ) {
        AddressSnapshot address = checkoutData.addressSnapshot();
        return CheckoutSession.builder()
                .checkoutCode(generateCheckoutCode())
                .user(user)
                .shippingName(address.recipientName())
                .shippingPhone(address.phone())
                .shippingProvince(address.province())
                .shippingDistrict(address.district())
                .shippingWard(address.ward())
                .shippingAddress(address.streetAddress())
                .subtotal(subtotal)
                .shippingFee(shippingFee)
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(initialTotalAmount)
                .voucher(null)
                .paymentMethod(requestDTO.paymentMethod())
                .status(CheckoutSessionStatus.creating)
                .expiresAt(expiresAt)
                .build();
    }

    private CheckoutSessionItem toCheckoutSessionItem(CheckoutSession checkoutSession, CheckoutItemSnapshot item) {
        if (item.productVariantId() == null) {
            throw new InvalidDataException("Product variant ID is required in checkout item snapshot");
        }
        ProductVariant productVariant = entityManager.getReference(ProductVariant.class, item.productVariantId());
        return CheckoutSessionItem.builder()
                .checkoutSession(checkoutSession)
                .productVariant(productVariant)
                .productName(item.productName())
                .variantInfo(item.variantInfo())
                .quantity(item.quantity())
                .unitPrice(requireAmount(item.unitPrice(), "Checkout item unit price"))
                .subtotal(requireAmount(item.subtotal(), "Checkout item subtotal"))
                .build();
    }

    private Voucher findReservedVoucher(Long checkoutSessionId) {
        VoucherReservation voucherReservation = voucherReservationRepository.findByCheckoutSessionId(checkoutSessionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Voucher reservation not found for checkout session: " + checkoutSessionId
                ));
        return voucherReservation.getVoucher();
    }

    private BigDecimal calculateTotalAmount(BigDecimal subtotal, BigDecimal shippingFee, BigDecimal discountAmount) {
        if (discountAmount.signum() < 0) {
            throw new InvalidDataException("Discount amount must not be negative");
        }
        if (discountAmount.compareTo(subtotal) > 0) {
            throw new InvalidDataException("Discount amount must not exceed subtotal");
        }
        BigDecimal totalAmount = subtotal.add(shippingFee).subtract(discountAmount);
        if (totalAmount.signum() < 0) {
            throw new InvalidDataException("Checkout total amount must not be negative");
        }
        return totalAmount;
    }

    private BigDecimal requireAmount(BigDecimal amount, String fieldName) {
        if (amount == null) {
            throw new InvalidDataException(fieldName + " is required");
        }
        if (amount.signum() < 0) {
            throw new InvalidDataException(fieldName + " must not be negative");
        }
        return amount;
    }

    private String normalizeVoucherCode(String voucherCode) {
        if (voucherCode == null || voucherCode.trim().isEmpty()) {
            return null;
        }
        return voucherCode.trim();
    }

    private String generateCheckoutCode() {
        for (int i = 0; i < CHECKOUT_CODE_RETRY_LIMIT; i++) {
            String code = "CHK-" + UUID.randomUUID().toString().replace("-", "").substring(0, 26);
            if (!checkoutSessionRepository.existsByCheckoutCode(code)) {
                return code;
            }
        }
        throw new InvalidDataException("Unable to generate unique checkout code");
    }
}
