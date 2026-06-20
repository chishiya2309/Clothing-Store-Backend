package vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment;

import org.junit.jupiter.api.Test;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.exception.PaymentGatewayUnavailableException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentGatewayAdapterFactoryTest {

    @Test
    void getAdapter_vnpay_returnsVnpayAdapter() {
        PaymentGatewayAdapter vnpayAdapter = adapter(PaymentMethod.vnpay, true);
        PaymentGatewayAdapterFactory factory = new PaymentGatewayAdapterFactory(List.of(vnpayAdapter));

        assertSame(vnpayAdapter, factory.getAdapter(PaymentMethod.vnpay));
    }

    @Test
    void getAdapter_momo_returnsMomoAdapter() {
        PaymentGatewayAdapter momoAdapter = adapter(PaymentMethod.momo, true);
        PaymentGatewayAdapterFactory factory = new PaymentGatewayAdapterFactory(List.of(momoAdapter));

        assertSame(momoAdapter, factory.getAdapter(PaymentMethod.momo));
    }

    @Test
    void getAdapter_cod_throwsException() {
        PaymentGatewayAdapterFactory factory = new PaymentGatewayAdapterFactory(List.of(adapter(PaymentMethod.vnpay, true)));

        assertThrows(InvalidDataException.class, () -> factory.getAdapter(PaymentMethod.cod));
    }

    @Test
    void getAdapter_nullMethod_throwsException() {
        PaymentGatewayAdapterFactory factory = new PaymentGatewayAdapterFactory(List.of(adapter(PaymentMethod.vnpay, true)));

        assertThrows(InvalidDataException.class, () -> factory.getAdapter(null));
    }

    @Test
    void getAdapter_missingAdapter_throwsException() {
        PaymentGatewayAdapterFactory factory = new PaymentGatewayAdapterFactory(List.of(adapter(PaymentMethod.vnpay, true)));

        assertThrows(InvalidDataException.class, () -> factory.getAdapter(PaymentMethod.momo));
    }

    @Test
    void constructor_duplicateAdapter_throwsException() {
        PaymentGatewayAdapter firstAdapter = adapter(PaymentMethod.vnpay, true);
        PaymentGatewayAdapter secondAdapter = adapter(PaymentMethod.vnpay, true);

        assertThrows(InvalidDataException.class,
                () -> new PaymentGatewayAdapterFactory(List.of(firstAdapter, secondAdapter)));
    }

    @Test
    void constructor_nullAdapterMethod_throwsException() {
        PaymentGatewayAdapter nullMethodAdapter = adapter(null, true);

        assertThrows(InvalidDataException.class,
                () -> new PaymentGatewayAdapterFactory(List.of(nullMethodAdapter)));
    }

    @Test
    void isAvailable_vnpayAvailable_returnsTrue() {
        PaymentGatewayAdapterFactory factory = new PaymentGatewayAdapterFactory(List.of(adapter(PaymentMethod.vnpay, true)));

        assertTrue(factory.isAvailable(PaymentMethod.vnpay));
    }

    @Test
    void isAvailable_vnpayUnavailable_returnsFalse() {
        PaymentGatewayAdapterFactory factory = new PaymentGatewayAdapterFactory(List.of(adapter(PaymentMethod.vnpay, false)));

        assertFalse(factory.isAvailable(PaymentMethod.vnpay));
    }

    @Test
    void isAvailable_momoAvailable_returnsTrue() {
        PaymentGatewayAdapterFactory factory = new PaymentGatewayAdapterFactory(List.of(adapter(PaymentMethod.momo, true)));

        assertTrue(factory.isAvailable(PaymentMethod.momo));
    }

    @Test
    void isAvailable_momoUnavailable_returnsFalse() {
        PaymentGatewayAdapterFactory factory = new PaymentGatewayAdapterFactory(List.of(adapter(PaymentMethod.momo, false)));

        assertFalse(factory.isAvailable(PaymentMethod.momo));
    }

    @Test
    void isAvailable_cod_returnsTrue() {
        PaymentGatewayAdapterFactory factory = new PaymentGatewayAdapterFactory(List.of());

        assertTrue(factory.isAvailable(PaymentMethod.cod));
    }

    @Test
    void requireAvailable_availableOnlineMethod_doesNotThrow() {
        PaymentGatewayAdapterFactory factory = new PaymentGatewayAdapterFactory(List.of(adapter(PaymentMethod.vnpay, true)));

        factory.requireAvailable(PaymentMethod.vnpay);
    }

    @Test
    void requireAvailable_unavailableOnlineMethod_throwsException() {
        PaymentGatewayAdapterFactory factory = new PaymentGatewayAdapterFactory(List.of(adapter(PaymentMethod.vnpay, false)));

        assertThrows(PaymentGatewayUnavailableException.class,
                () -> factory.requireAvailable(PaymentMethod.vnpay));
    }

    @Test
    void requireAvailable_missingOnlineMethod_throwsException() {
        PaymentGatewayAdapterFactory factory = new PaymentGatewayAdapterFactory(List.of(adapter(PaymentMethod.vnpay, true)));

        assertThrows(PaymentGatewayUnavailableException.class,
                () -> factory.requireAvailable(PaymentMethod.momo));
    }

    @Test
    void requireAvailable_cod_doesNotRequireAdapter() {
        PaymentGatewayAdapterFactory factory = new PaymentGatewayAdapterFactory(List.of());

        factory.requireAvailable(PaymentMethod.cod);
    }

    private PaymentGatewayAdapter adapter(PaymentMethod method, boolean available) {
        return new PaymentGatewayAdapter() {
            @Override
            public PaymentMethod supportMethod() {
                return method;
            }

            @Override
            public boolean isAvailable() {
                return available;
            }

            @Override
            public GatewayPaymentCreationResult createPayment(GatewayPaymentCreationCommand command) {
                return null;
            }
        };
    }
}
