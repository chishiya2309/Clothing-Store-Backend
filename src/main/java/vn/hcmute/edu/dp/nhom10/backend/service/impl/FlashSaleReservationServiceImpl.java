package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.CheckoutItemSnapshot;
import vn.hcmute.edu.dp.nhom10.backend.entity.CheckoutSession;
import vn.hcmute.edu.dp.nhom10.backend.entity.FlashSaleItem;
import vn.hcmute.edu.dp.nhom10.backend.entity.FlashSaleReservation;
import vn.hcmute.edu.dp.nhom10.backend.enums.PriceSource;
import vn.hcmute.edu.dp.nhom10.backend.enums.ReservationStatus;
import vn.hcmute.edu.dp.nhom10.backend.exception.InsufficientStockException;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.repository.CheckoutSessionRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.FlashSaleItemRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.FlashSaleReservationRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.FlashSaleReservationService;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FlashSaleReservationServiceImpl implements FlashSaleReservationService {
    private final CheckoutSessionRepository checkoutSessionRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;
    private final FlashSaleReservationRepository reservationRepository;

    @Override
    @Transactional
    public void reserveQuota(Long checkoutSessionId, List<CheckoutItemSnapshot> items, OffsetDateTime expiresAt) {
        if (checkoutSessionId == null || expiresAt == null || !expiresAt.isAfter(OffsetDateTime.now())) {
            throw new IllegalArgumentException("Valid checkout session and future expiry are required");
        }
        Map<Long, Integer> requested = collect(items);
        if (requested.isEmpty()) return;

        CheckoutSession checkout = checkoutSessionRepository.findByIdForUpdate(checkoutSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Checkout session not found with ID: " + checkoutSessionId));
        if (reservationRepository.existsByCheckoutSessionId(checkoutSessionId)) {
            throw new InvalidDataException("Flash sale reservation already exists for checkout session: " + checkoutSessionId);
        }

        List<Long> ids = requested.keySet().stream().sorted().toList();
        Map<Long, FlashSaleItem> locked = new LinkedHashMap<>();
        flashSaleItemRepository.findAllByIdInForUpdate(ids).forEach(item -> locked.put(item.getId(), item));
        List<FlashSaleReservation> reservations = new ArrayList<>();
        for (Long id : ids) {
            FlashSaleItem item = locked.get(id);
            if (item == null) throw new ResourceNotFoundException("Flash sale item not found with ID: " + id);
            int quantity = requested.get(id);
            int quota = value(item.getQuota());
            int reserved = value(item.getReservedQuantity());
            int sold = value(item.getSoldQuantity());
            int available = quota - reserved - sold;
            if (available < quantity) {
                throw new InsufficientStockException("Flash sale quota is insufficient. Available: " + available
                        + ", requested: " + quantity);
            }
            item.setReservedQuantity(reserved + quantity);
            reservations.add(FlashSaleReservation.builder().checkoutSession(checkout).flashSaleItem(item)
                    .quantity(quantity).status(ReservationStatus.active).expiresAt(expiresAt).build());
        }
        flashSaleItemRepository.saveAll(locked.values());
        reservationRepository.saveAll(reservations);
    }

    private Map<Long, Integer> collect(List<CheckoutItemSnapshot> items) {
        Map<Long, Integer> result = new LinkedHashMap<>();
        if (items == null) throw new IllegalArgumentException("Checkout items are required");
        for (CheckoutItemSnapshot item : items) {
            if (item == null) throw new IllegalArgumentException("Checkout item is required");
            if (item.priceSource() != PriceSource.FLASH_SALE) continue;
            if (item.flashSaleItemId() == null || item.quantity() == null || item.quantity() <= 0) {
                throw new IllegalArgumentException("Flash sale item and positive quantity are required");
            }
            result.merge(item.flashSaleItemId(), item.quantity(), Integer::sum);
        }
        return result;
    }

    private int value(Integer number) { return number == null ? 0 : number; }
}
