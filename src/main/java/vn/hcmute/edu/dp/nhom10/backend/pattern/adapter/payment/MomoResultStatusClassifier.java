package vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment;

import org.springframework.stereotype.Component;

@Component
public class MomoResultStatusClassifier {

    public Status classify(int resultCode) {
        return switch (resultCode) {
            case 0 -> Status.COMPLETED;
            case 1000, 7000, 7002 -> Status.PENDING;
            case 9000 -> Status.AUTHORIZED;
            default -> Status.FAILED;
        };
    }

    public enum Status {
        COMPLETED,
        PENDING,
        AUTHORIZED,
        FAILED
    }
}
