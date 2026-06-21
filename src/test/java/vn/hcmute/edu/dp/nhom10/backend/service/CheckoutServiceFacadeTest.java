package vn.hcmute.edu.dp.nhom10.backend.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
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
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.pattern.facade.checkout.CheckoutServiceFacade;
import vn.hcmute.edu.dp.nhom10.backend.repository.CheckoutSessionItemRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.CheckoutSessionRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.UserRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.VoucherReservationRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceFacadeTest {

    @Mock
    private CheckoutDataService checkoutDataService;

    @Mock
    private InventoryReservationService inventoryReservationService;

    @Mock
    private VoucherReservationService voucherService;

    @Mock
    private CheckoutSessionRepository checkoutSessionRepository;

    @Mock
    private CheckoutSessionItemRepository checkoutSessionItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VoucherReservationRepository voucherReservationRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private CheckoutServiceFacade checkoutService;

    private final List<CheckoutSessionStatus> savedStatuses = new ArrayList<>();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(checkoutService, "reservationTtlMinutes", 15L);
        lenient().when(checkoutSessionRepository.save(any(CheckoutSession.class))).thenAnswer(invocation -> {
            CheckoutSession checkoutSession = invocation.getArgument(0);
            savedStatuses.add(checkoutSession.getStatus());
            if (checkoutSession.getId() == null) {
                checkoutSession.setId(1L);
            }
            return checkoutSession;
        });
    }

    @Test
    void prepareCheckout_withoutVoucher_success() {
        mockSuccessfulBaseFlow();

        ReservedCheckoutResult result = checkoutService.prepareCheckout(request(null), 10L);

        assertNotNull(result);
        assertEquals(1L, result.checkoutSessionId());
        assertEquals(PaymentMethod.cod, result.paymentMethod());
        assertEquals(money("200000.00"), result.subtotal());
        assertEquals(BigDecimal.ZERO, result.discountAmount());
        assertEquals(money("220000.00"), result.totalAmount());
    }

    @Test
    void prepareCheckout_withVoucher_success() {
        mockSuccessfulBaseFlow();
        Voucher voucher = Voucher.builder().id(100L).code("SAVE10").build();
        when(voucherService.reserveVoucher(eq(1L), eq("SAVE10"), eq(money("200000.00")), any(OffsetDateTime.class)))
                .thenReturn(money("30000.00"));
        when(voucherReservationRepository.findByCheckoutSessionId(1L))
                .thenReturn(Optional.of(VoucherReservation.builder().voucher(voucher).build()));

        ReservedCheckoutResult result = checkoutService.prepareCheckout(request("SAVE10"), 10L);

        assertEquals(money("30000.00"), result.discountAmount());
        assertEquals(money("190000.00"), result.totalAmount());
        assertEquals(voucher, captureLastSavedSession().getVoucher());
    }

    @Test
    void prepareCheckout_nullVoucherCode_doesNotCallVoucherService() {
        mockSuccessfulBaseFlow();

        checkoutService.prepareCheckout(request(null), 10L);

        verifyNoInteractions(voucherService);
        verify(voucherReservationRepository, never()).findByCheckoutSessionId(anyLong());
    }

    @Test
    void prepareCheckout_blankVoucherCode_doesNotCallVoucherService() {
        mockSuccessfulBaseFlow();

        checkoutService.prepareCheckout(request(" "), 10L);

        verifyNoInteractions(voucherService);
        verify(voucherReservationRepository, never()).findByCheckoutSessionId(anyLong());
    }

    @Test
    void prepareCheckout_trimsVoucherCode() {
        mockSuccessfulBaseFlow();
        when(voucherService.reserveVoucher(eq(1L), eq("SAVE10"), eq(money("200000.00")), any(OffsetDateTime.class)))
                .thenReturn(money("10000.00"));
        when(voucherReservationRepository.findByCheckoutSessionId(1L))
                .thenReturn(Optional.of(VoucherReservation.builder().voucher(Voucher.builder().id(100L).build()).build()));

        checkoutService.prepareCheckout(request(" SAVE10 "), 10L);

        verify(voucherService).reserveVoucher(eq(1L), eq("SAVE10"), eq(money("200000.00")), any(OffsetDateTime.class));
    }

    @Test
    void prepareCheckout_nullRequest_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> checkoutService.prepareCheckout(null, 10L));

        verifyNoInteractions(checkoutDataService, inventoryReservationService, voucherService);
    }

    @Test
    void prepareCheckout_nullUserId_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> checkoutService.prepareCheckout(request(null), null));

        verifyNoInteractions(checkoutDataService, inventoryReservationService, voucherService);
    }

    @Test
    void prepareCheckout_nullAddressId_throwsException() {
        ConfirmCheckoutRequestDTO request = new ConfirmCheckoutRequestDTO(null, null, PaymentMethod.cod);

        assertThrows(IllegalArgumentException.class, () -> checkoutService.prepareCheckout(request, 10L));

        verifyNoInteractions(checkoutDataService, inventoryReservationService, voucherService);
    }

    @Test
    void prepareCheckout_nullPaymentMethod_throwsException() {
        ConfirmCheckoutRequestDTO request = new ConfirmCheckoutRequestDTO(1L, null, null);

        assertThrows(IllegalArgumentException.class, () -> checkoutService.prepareCheckout(request, 10L));

        verifyNoInteractions(checkoutDataService, inventoryReservationService, voucherService);
    }

    @Test
    void prepareCheckout_userNotFound_throwsException() {
        when(checkoutDataService.getCheckoutData(10L, 1L)).thenReturn(checkoutData());
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> checkoutService.prepareCheckout(request(null), 10L));

        verify(checkoutSessionRepository, never()).save(any());
        verifyNoInteractions(inventoryReservationService, voucherService);
    }

    @Test
    void prepareCheckout_checkoutDataServiceFails_doesNotCreateSession() {
        when(checkoutDataService.getCheckoutData(10L, 1L)).thenThrow(new IllegalArgumentException("Cart is empty"));

        assertThrows(IllegalArgumentException.class, () -> checkoutService.prepareCheckout(request(null), 10L));

        verify(checkoutSessionRepository, never()).save(any());
        verifyNoInteractions(inventoryReservationService, voucherService);
    }

    @Test
    void prepareCheckout_inventoryFails_doesNotReserveVoucherOrSaveItemsOrReserveSession() {
        mockSuccessfulBaseFlow();
        org.mockito.Mockito.doThrow(new IllegalArgumentException("Stock unavailable"))
                .when(inventoryReservationService).reserveStock(eq(1L), any(), any(OffsetDateTime.class));

        assertThrows(IllegalArgumentException.class, () -> checkoutService.prepareCheckout(request("SAVE10"), 10L));

        verifyNoInteractions(voucherService);
        verify(checkoutSessionItemRepository, never()).saveAll(any());
        assertEquals(List.of(CheckoutSessionStatus.creating), savedStatuses);
    }

    @Test
    void prepareCheckout_voucherFails_doesNotSaveItemsOrReserveSession() {
        mockSuccessfulBaseFlow();
        when(voucherService.reserveVoucher(eq(1L), eq("SAVE10"), eq(money("200000.00")), any(OffsetDateTime.class)))
                .thenThrow(new IllegalArgumentException("Invalid voucher"));

        assertThrows(IllegalArgumentException.class, () -> checkoutService.prepareCheckout(request("SAVE10"), 10L));

        verify(checkoutSessionItemRepository, never()).saveAll(any());
        assertEquals(List.of(CheckoutSessionStatus.creating), savedStatuses);
    }

    @Test
    void prepareCheckout_snapshotSaveFails_doesNotReserveSession() {
        mockSuccessfulBaseFlow();
        when(checkoutSessionItemRepository.saveAll(any())).thenThrow(new IllegalArgumentException("Cannot save item"));

        assertThrows(IllegalArgumentException.class, () -> checkoutService.prepareCheckout(request(null), 10L));

        assertEquals(List.of(CheckoutSessionStatus.creating), savedStatuses);
    }

    @Test
    void prepareCheckout_calculatesTotal() {
        mockSuccessfulBaseFlow();
        when(voucherService.reserveVoucher(eq(1L), eq("SAVE10"), eq(money("200000.00")), any(OffsetDateTime.class)))
                .thenReturn(money("50000.00"));
        when(voucherReservationRepository.findByCheckoutSessionId(1L))
                .thenReturn(Optional.of(VoucherReservation.builder().voucher(Voucher.builder().id(100L).build()).build()));

        ReservedCheckoutResult result = checkoutService.prepareCheckout(request("SAVE10"), 10L);

        assertEquals(money("170000.00"), result.totalAmount());
    }

    @Test
    void prepareCheckout_mapsAddressSnapshot() {
        mockSuccessfulBaseFlow();

        checkoutService.prepareCheckout(request(null), 10L);

        CheckoutSession session = captureFirstSavedSession();
        assertEquals("Nguyen Van A", session.getShippingName());
        assertEquals("0900000000", session.getShippingPhone());
        assertEquals("Ho Chi Minh", session.getShippingProvince());
        assertEquals("District 1", session.getShippingDistrict());
        assertEquals("Ben Nghe", session.getShippingWard());
        assertEquals("1 Le Loi", session.getShippingAddress());
    }

    @Test
    void prepareCheckout_mapsItemSnapshot() {
        mockSuccessfulBaseFlow();

        checkoutService.prepareCheckout(request(null), 10L);

        CheckoutSessionItem item = captureSavedItem();
        assertEquals("T-Shirt", item.getProductName());
        assertEquals("Size: M, Color: Black", item.getVariantInfo());
        assertEquals(2, item.getQuantity());
        assertEquals(money("100000.00"), item.getUnitPrice());
        assertEquals(money("200000.00"), item.getSubtotal());
    }

    @Test
    void prepareCheckout_initialSessionIsCreating() {
        mockSuccessfulBaseFlow();

        checkoutService.prepareCheckout(request(null), 10L);

        assertEquals(CheckoutSessionStatus.creating, savedStatuses.get(0));
    }

    @Test
    void prepareCheckout_finalSessionIsReserved() {
        mockSuccessfulBaseFlow();

        checkoutService.prepareCheckout(request(null), 10L);

        assertEquals(CheckoutSessionStatus.reserved, savedStatuses.get(savedStatuses.size() - 1));
    }

    @Test
    void prepareCheckout_inventoryAndVoucherUseSameExpiresAt() {
        mockSuccessfulBaseFlow();
        when(voucherService.reserveVoucher(eq(1L), eq("SAVE10"), eq(money("200000.00")), any(OffsetDateTime.class)))
                .thenReturn(money("10000.00"));
        when(voucherReservationRepository.findByCheckoutSessionId(1L))
                .thenReturn(Optional.of(VoucherReservation.builder().voucher(Voucher.builder().id(100L).build()).build()));

        checkoutService.prepareCheckout(request("SAVE10"), 10L);

        ArgumentCaptor<OffsetDateTime> inventoryExpiresAt = ArgumentCaptor.forClass(OffsetDateTime.class);
        ArgumentCaptor<OffsetDateTime> voucherExpiresAt = ArgumentCaptor.forClass(OffsetDateTime.class);
        verify(inventoryReservationService).reserveStock(eq(1L), any(), inventoryExpiresAt.capture());
        verify(voucherService).reserveVoucher(eq(1L), eq("SAVE10"), eq(money("200000.00")), voucherExpiresAt.capture());
        assertEquals(inventoryExpiresAt.getValue(), voucherExpiresAt.getValue());
    }

    @Test
    void prepareCheckout_withoutVoucherDiscountIsZero() {
        mockSuccessfulBaseFlow();

        ReservedCheckoutResult result = checkoutService.prepareCheckout(request(null), 10L);

        assertEquals(BigDecimal.ZERO, result.discountAmount());
    }

    @Test
    void prepareCheckout_doesNotUseOrderOrPaymentCollaborators() {
        mockSuccessfulBaseFlow();

        checkoutService.prepareCheckout(request(null), 10L);

        InOrder inOrder = inOrder(checkoutDataService, checkoutSessionRepository, inventoryReservationService,
                checkoutSessionItemRepository);
        inOrder.verify(checkoutDataService).getCheckoutData(10L, 1L);
        inOrder.verify(checkoutSessionRepository).save(any(CheckoutSession.class));
        inOrder.verify(inventoryReservationService).reserveStock(eq(1L), any(), any(OffsetDateTime.class));
        inOrder.verify(checkoutSessionItemRepository).saveAll(any());
    }

    private void mockSuccessfulBaseFlow() {
        when(checkoutDataService.getCheckoutData(10L, 1L)).thenReturn(checkoutData());
        when(userRepository.findById(10L)).thenReturn(Optional.of(User.builder().id(10L).build()));
        lenient().when(entityManager.getReference(ProductVariant.class, 100L))
                .thenReturn(ProductVariant.builder().id(100L).build());
    }

    private ConfirmCheckoutRequestDTO request(String voucherCode) {
        return new ConfirmCheckoutRequestDTO(1L, voucherCode, PaymentMethod.cod);
    }

    private CheckoutData checkoutData() {
        return new CheckoutData(
                10L,
                1L,
                new AddressSnapshot(
                        "Nguyen Van A",
                        "0900000000",
                        "Ho Chi Minh",
                        "District 1",
                        "Ben Nghe",
                        "1 Le Loi"
                ),
                List.of(new CheckoutItemSnapshot(
                        11L,
                        100L,
                        "T-Shirt",
                        "Size: M, Color: Black",
                        2,
                        money("100000.00"),
                        money("200000.00")
                )),
                money("200000.00"),
                money("20000.00")
        );
    }

    private CheckoutSession captureFirstSavedSession() {
        ArgumentCaptor<CheckoutSession> captor = ArgumentCaptor.forClass(CheckoutSession.class);
        verify(checkoutSessionRepository, times(2)).save(captor.capture());
        return captor.getAllValues().get(0);
    }

    private CheckoutSession captureLastSavedSession() {
        ArgumentCaptor<CheckoutSession> captor = ArgumentCaptor.forClass(CheckoutSession.class);
        verify(checkoutSessionRepository, times(2)).save(captor.capture());
        return captor.getAllValues().get(1);
    }

    private CheckoutSessionItem captureSavedItem() {
        ArgumentCaptor<Iterable<CheckoutSessionItem>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(checkoutSessionItemRepository).saveAll(captor.capture());
        return captor.getValue().iterator().next();
    }

    private BigDecimal money(String value) {
        return new BigDecimal(value);
    }
}
