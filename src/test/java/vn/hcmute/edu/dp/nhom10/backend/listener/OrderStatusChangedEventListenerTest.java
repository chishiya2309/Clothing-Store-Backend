package vn.hcmute.edu.dp.nhom10.backend.listener;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.UserRole;
import vn.hcmute.edu.dp.nhom10.backend.event.OrderStatusChangedEvent;
import vn.hcmute.edu.dp.nhom10.backend.repository.UserRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.EmailService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderStatusChangedEventListenerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderStatusChangedEventListener listener;

    @Test
    void handleOrderStatusChanged_cancelledSendsCustomerAndDistinctAdminEmails() {
        OrderStatusChangedEvent event = cancelledEvent();
        when(userRepository.findAllByRoleAndIsActiveTrue(UserRole.admin)).thenReturn(List.of(
                admin("admin1@test.com"),
                admin("admin1@test.com"),
                admin("admin2@test.com"),
                admin(" ")
        ));

        listener.handleOrderStatusChanged(event);

        verify(emailService).sendOrderCancellationEmailToCustomer(
                "alice@test.com",
                "ORD-1",
                "Customer requested cancellation",
                true
        );
        verify(emailService).sendOrderCancellationEmailToAdmin(
                eq("admin1@test.com"),
                eq("ORD-1"),
                eq("alice@test.com"),
                eq("staff@test.com"),
                eq("processing"),
                eq("Customer requested cancellation"),
                eq("vnpay"),
                eq("completed"),
                eq(new BigDecimal("250000.00")),
                eq(true)
        );
        verify(emailService).sendOrderCancellationEmailToAdmin(
                eq("admin2@test.com"),
                eq("ORD-1"),
                eq("alice@test.com"),
                eq("staff@test.com"),
                eq("processing"),
                eq("Customer requested cancellation"),
                eq("vnpay"),
                eq("completed"),
                eq(new BigDecimal("250000.00")),
                eq(true)
        );
    }

    @Test
    void handleOrderStatusChanged_customerEmailFailureDoesNotStopAdminEmails() {
        OrderStatusChangedEvent event = cancelledEvent();
        when(userRepository.findAllByRoleAndIsActiveTrue(UserRole.admin)).thenReturn(List.of(admin("admin@test.com")));
        doThrow(new RuntimeException("Email failed"))
                .when(emailService)
                .sendOrderCancellationEmailToCustomer(any(), any(), any(), eq(true));

        assertDoesNotThrow(() -> listener.handleOrderStatusChanged(event));

        verify(emailService).sendOrderCancellationEmailToAdmin(
                eq("admin@test.com"),
                eq("ORD-1"),
                eq("alice@test.com"),
                eq("staff@test.com"),
                eq("processing"),
                eq("Customer requested cancellation"),
                eq("vnpay"),
                eq("completed"),
                eq(new BigDecimal("250000.00")),
                eq(true)
        );
    }

    @Test
    void handleOrderStatusChanged_adminEmailFailureDoesNotStopNextAdminEmail() {
        OrderStatusChangedEvent event = cancelledEvent();
        when(userRepository.findAllByRoleAndIsActiveTrue(UserRole.admin)).thenReturn(List.of(
                admin("admin1@test.com"),
                admin("admin2@test.com")
        ));
        doThrow(new RuntimeException("Admin email failed"))
                .when(emailService)
                .sendOrderCancellationEmailToAdmin(
                        eq("admin1@test.com"),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        eq(true)
                );

        assertDoesNotThrow(() -> listener.handleOrderStatusChanged(event));

        verify(emailService).sendOrderCancellationEmailToAdmin(
                eq("admin2@test.com"),
                eq("ORD-1"),
                eq("alice@test.com"),
                eq("staff@test.com"),
                eq("processing"),
                eq("Customer requested cancellation"),
                eq("vnpay"),
                eq("completed"),
                eq(new BigDecimal("250000.00")),
                eq(true)
        );
    }

    @Test
    void handleOrderStatusChanged_nonCancelledEventIsIgnored() {
        OrderStatusChangedEvent event = new OrderStatusChangedEvent(
                1L,
                "ORD-1",
                10L,
                "alice@test.com",
                5L,
                "staff@test.com",
                OrderStatus.pending,
                OrderStatus.processing,
                null,
                OffsetDateTime.parse("2026-01-10T09:30:00Z")
        );

        listener.handleOrderStatusChanged(event);

        verify(emailService, never()).sendOrderCancellationEmailToCustomer(any(), any(), any(), anyBoolean());
        verify(emailService, never()).sendOrderCancellationEmailToAdmin(any(), any(), any(), any(), any(), any(), any(), any(), any(), anyBoolean());
        verify(userRepository, never()).findAllByRoleAndIsActiveTrue(any());
    }

    private OrderStatusChangedEvent cancelledEvent() {
        return new OrderStatusChangedEvent(
                1L,
                "ORD-1",
                10L,
                "alice@test.com",
                5L,
                "staff@test.com",
                OrderStatus.processing,
                OrderStatus.cancelled,
                "Customer requested cancellation",
                PaymentMethod.vnpay,
                PaymentStatus.completed,
                new BigDecimal("250000.00"),
                true,
                OffsetDateTime.parse("2026-01-10T09:30:00Z")
        );
    }

    private User admin(String email) {
        return User.builder()
                .email(email)
                .role(UserRole.admin)
                .isActive(true)
                .build();
    }
}
