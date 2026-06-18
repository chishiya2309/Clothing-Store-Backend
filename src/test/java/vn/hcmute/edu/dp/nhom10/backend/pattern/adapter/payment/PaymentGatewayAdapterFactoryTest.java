package vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment;

import org.junit.jupiter.api.Test;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentGatewayAdapterFactoryTest {

    @Test
    void getAdapter_vnpay_returnsVnpayAdapter() {
        PaymentGatewayAdapter vnpayAdapter = adapter(PaymentMethod.vnpay);
        PaymentGatewayAdapterFactory factory = new PaymentGatewayAdapterFactory(List.of(vnpayAdapter));

        assertSame(vnpayAdapter, factory.getAdapter(PaymentMethod.vnpay));
    }

    @Test
    void getAdapter_momo_returnsMomoAdapter() {
        PaymentGatewayAdapter momoAdapter = adapter(PaymentMethod.momo);
        PaymentGatewayAdapterFactory factory = new PaymentGatewayAdapterFactory(List.of(momoAdapter));

        assertSame(momoAdapter, factory.getAdapter(PaymentMethod.momo));
    }

    @Test
    void getAdapter_cod_throwsException() {
        PaymentGatewayAdapterFactory factory = new PaymentGatewayAdapterFactory(List.of(adapter(PaymentMethod.vnpay)));

        assertThrows(InvalidDataException.class, () -> factory.getAdapter(PaymentMethod.cod));
    }

    @Test
    void getAdapter_nullMethod_throwsException() {
        PaymentGatewayAdapterFactory factory = new PaymentGatewayAdapterFactory(List.of(adapter(PaymentMethod.vnpay)));

        assertThrows(InvalidDataException.class, () -> factory.getAdapter(null));
    }

    @Test
    void getAdapter_missingAdapter_throwsException() {
        PaymentGatewayAdapterFactory factory = new PaymentGatewayAdapterFactory(List.of(adapter(PaymentMethod.vnpay)));

        assertThrows(InvalidDataException.class, () -> factory.getAdapter(PaymentMethod.momo));
    }

    @Test
    void constructor_duplicateAdapter_throwsException() {
        PaymentGatewayAdapter firstAdapter = adapter(PaymentMethod.vnpay);
        PaymentGatewayAdapter secondAdapter = adapter(PaymentMethod.vnpay);

        assertThrows(InvalidDataException.class,
                () -> new PaymentGatewayAdapterFactory(List.of(firstAdapter, secondAdapter)));
    }

    @Test
    void constructor_nullAdapterMethod_throwsException() {
        PaymentGatewayAdapter nullMethodAdapter = adapter(null);

        assertThrows(InvalidDataException.class,
                () -> new PaymentGatewayAdapterFactory(List.of(nullMethodAdapter)));
    }

    private PaymentGatewayAdapter adapter(PaymentMethod method) {
        return new PaymentGatewayAdapter() {
            @Override
            public PaymentMethod supportMethod() {
                return method;
            }

            @Override
            public GatewayPaymentCreationResult createPayment(GatewayPaymentCreationCommand command) {
                return null;
            }
        };
    }
}
