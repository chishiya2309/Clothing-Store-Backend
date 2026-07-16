package vn.hcmute.edu.dp.nhom10.backend.pattern.reservation.inventory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.CheckoutItemSnapshot;
import vn.hcmute.edu.dp.nhom10.backend.entity.CheckoutSession;
import vn.hcmute.edu.dp.nhom10.backend.entity.InventoryReservation;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;
import vn.hcmute.edu.dp.nhom10.backend.enums.ReservationStatus;
import vn.hcmute.edu.dp.nhom10.backend.exception.InsufficientStockException;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.repository.CheckoutSessionRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.InventoryReservationRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductVariantRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.InventoryReservationService;
import vn.hcmute.edu.dp.nhom10.backend.service.FlashSaleReservationService;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class InventoryReservationServiceImpl implements InventoryReservationService {

    private final CheckoutSessionRepository checkoutSessionRepository;
    private final InventoryReservationRepository inventoryReservationRepository;
    private final ProductVariantRepository productVariantRepository;
    private final FlashSaleReservationService flashSaleReservationService;

    @Override
    @Transactional
    public void reserveStock(Long checkoutSessionId, List<CheckoutItemSnapshot> items, OffsetDateTime expiresAt) {
        if (checkoutSessionId == null) {
            throw new IllegalArgumentException("Checkout session ID is required");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Checkout items must not be empty");
        }
        OffsetDateTime now = now();
        if (expiresAt == null || !expiresAt.isAfter(now)) {
            throw new IllegalArgumentException("Reservation expiry time must be in the future");
        }

        Map<Long, Integer> requestedQuantities = collectRequestedQuantities(items);
        CheckoutSession checkoutSession = checkoutSessionRepository.findByIdForUpdate(checkoutSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Checkout session not found with ID: " + checkoutSessionId));

        if (inventoryReservationRepository.existsByCheckoutSession_Id(checkoutSessionId)) {
            throw new InvalidDataException("Inventory reservation already exists for checkout session: " + checkoutSessionId);
        }

        List<Long> sortedVariantIds = requestedQuantities.keySet().stream()
                .sorted()
                .toList();
        List<ProductVariant> variants = productVariantRepository.findAllByIdInForUpdate(sortedVariantIds);
        Map<Long, ProductVariant> variantById = toVariantMap(sortedVariantIds, variants);

        List<InventoryReservation> reservations = new ArrayList<>();
        for (Long productVariantId : sortedVariantIds) {
            ProductVariant variant = variantById.get(productVariantId);
            if (!Boolean.TRUE.equals(variant.getIsActive())) {
                throw new IllegalArgumentException("Product variant is inactive: " + productVariantId);
            }

            Integer requestedQuantity = requestedQuantities.get(productVariantId);
            Long activeReservedQuantity = inventoryReservationRepository.sumReservedQuantity(
                    productVariantId,
                    ReservationStatus.active,
                    now
            );
            long availableQuantity = safeStockQuantity(variant) - safeReservedQuantity(activeReservedQuantity);
            if (availableQuantity < requestedQuantity) {
                throw new InsufficientStockException(
                        "Product variant " + productVariantId + " does not have enough stock. Available: "
                                + availableQuantity + ", requested: " + requestedQuantity
                );
            }

            reservations.add(InventoryReservation.builder()
                    .checkoutSession(checkoutSession)
                    .productVariant(variant)
                    .quantity(requestedQuantity)
                    .status(ReservationStatus.active)
                    .expiresAt(expiresAt)
                    .build());
        }

        inventoryReservationRepository.saveAll(reservations);
    }

    @Override
    @Transactional
    public void consumeStockReservation(String checkoutCode) {
        String normalizedCheckoutCode = normalizeCheckoutCode(checkoutCode);
        CheckoutSession checkoutSession = checkoutSessionRepository.findByCheckoutCodeForUpdate(normalizedCheckoutCode)
                .orElseThrow(() -> new ResourceNotFoundException("Checkout session not found with code: " + normalizedCheckoutCode));

        List<InventoryReservation> reservations = inventoryReservationRepository
                .findAllByCheckoutSessionIdForUpdate(checkoutSession.getId());
        if (reservations.isEmpty()) {
            throw new ResourceNotFoundException("Inventory reservation not found for checkout code: " + normalizedCheckoutCode);
        }
        if (reservations.stream().allMatch(this::isConsumed)) {
            return;
        }

        OffsetDateTime now = now();
        List<InventoryReservation> activeReservations = new ArrayList<>();
        for (InventoryReservation reservation : reservations) {
            ReservationStatus status = reservation.getStatus();
            if (status == ReservationStatus.consumed) {
                continue;
            }
            if (status == ReservationStatus.released || status == ReservationStatus.expired) {
                throw new InvalidDataException("Inventory reservation cannot be consumed because it is " + status);
            }
            if (status != ReservationStatus.active) {
                throw new InvalidDataException("Inventory reservation status is invalid: " + status);
            }
            if (reservation.getExpiresAt() == null || !reservation.getExpiresAt().isAfter(now)) {
                throw new InvalidDataException("Inventory reservation has expired");
            }
            activeReservations.add(reservation);
        }

        List<Long> sortedVariantIds = activeReservations.stream()
                .map(this::getProductVariantId)
                .distinct()
                .sorted()
                .toList();
        List<ProductVariant> lockedVariants = productVariantRepository.findAllByIdInForUpdate(sortedVariantIds);
        Map<Long, ProductVariant> variantById = toVariantMap(sortedVariantIds, lockedVariants);

        List<ProductVariant> variantsToSave = new ArrayList<>();
        for (InventoryReservation reservation : activeReservations) {
            Long productVariantId = getProductVariantId(reservation);
            ProductVariant variant = variantById.get(productVariantId);
            Integer quantity = reservation.getQuantity();
            if (quantity == null || quantity <= 0) {
                throw new InvalidDataException("Inventory reservation quantity is invalid: " + reservation.getId());
            }

            int stockQuantity = safeStockQuantity(variant);
            if (stockQuantity < quantity) {
                throw new InsufficientStockException(
                        "Product variant " + productVariantId + " does not have enough stock to consume reservation"
                );
            }

            variant.setStockQuantity(stockQuantity - quantity);
            reservation.setStatus(ReservationStatus.consumed);
            variantsToSave.add(variant);
        }

        productVariantRepository.saveAll(variantsToSave);
        inventoryReservationRepository.saveAll(activeReservations);
        flashSaleReservationService.consumeQuota(normalizedCheckoutCode);
    }

    @Override
    @Transactional
    public void releaseStockReservation(String checkoutCode) {
        String normalizedCheckoutCode = normalizeCheckoutCode(checkoutCode);
        CheckoutSession checkoutSession = checkoutSessionRepository.findByCheckoutCodeForUpdate(normalizedCheckoutCode)
                .orElseThrow(() -> new ResourceNotFoundException("Checkout session not found with code: " + normalizedCheckoutCode));

        List<InventoryReservation> reservations = inventoryReservationRepository
                .findAllByCheckoutSessionIdForUpdate(checkoutSession.getId());

        List<InventoryReservation> changedReservations = reservations.stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.active)
                .peek(reservation -> reservation.setStatus(ReservationStatus.released))
                .toList();

        if (!changedReservations.isEmpty()) {
            inventoryReservationRepository.saveAll(changedReservations);
        }
        flashSaleReservationService.releaseQuota(normalizedCheckoutCode);
    }

    private Map<Long, Integer> collectRequestedQuantities(List<CheckoutItemSnapshot> items) {
        Map<Long, Integer> requestedQuantities = new LinkedHashMap<>();
        for (CheckoutItemSnapshot item : items) {
            if (item == null) {
                throw new IllegalArgumentException("Checkout item must not be null");
            }
            Long productVariantId = item.productVariantId();
            if (productVariantId == null) {
                throw new IllegalArgumentException("Product variant ID is required");
            }
            Integer quantity = item.quantity();
            if (quantity == null || quantity <= 0) {
                throw new IllegalArgumentException("Requested quantity must be greater than 0");
            }
            if (requestedQuantities.putIfAbsent(productVariantId, quantity) != null) {
                throw new IllegalArgumentException("Duplicate product variant ID in checkout items: " + productVariantId);
            }
        }
        return requestedQuantities;
    }

    private Map<Long, ProductVariant> toVariantMap(List<Long> expectedVariantIds, List<ProductVariant> variants) {
        Map<Long, ProductVariant> variantById = new LinkedHashMap<>();
        for (ProductVariant variant : variants) {
            variantById.put(variant.getId(), variant);
        }

        for (Long expectedVariantId : expectedVariantIds) {
            if (!variantById.containsKey(expectedVariantId)) {
                throw new ResourceNotFoundException("Product variant not found with ID: " + expectedVariantId);
            }
        }
        return variantById;
    }

    private Long getProductVariantId(InventoryReservation reservation) {
        if (reservation.getProductVariant() == null || reservation.getProductVariant().getId() == null) {
            throw new ResourceNotFoundException("Product variant not found for inventory reservation: " + reservation.getId());
        }
        return reservation.getProductVariant().getId();
    }

    private boolean isConsumed(InventoryReservation reservation) {
        return reservation.getStatus() == ReservationStatus.consumed;
    }

    private String normalizeCheckoutCode(String checkoutCode) {
        if (checkoutCode == null || checkoutCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Checkout code is required");
        }
        return checkoutCode.trim();
    }

    private int safeStockQuantity(ProductVariant variant) {
        return Objects.requireNonNullElse(variant.getStockQuantity(), 0);
    }

    private long safeReservedQuantity(Long reservedQuantity) {
        return reservedQuantity == null ? 0L : reservedQuantity;
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now();
    }
}
