package vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MomoResultStatusClassifierTest {

    private final MomoResultStatusClassifier classifier = new MomoResultStatusClassifier();

    @Test
    void classify_mapsMomoResultCodesWithoutTreatingAllNonZeroAsFailed() {
        assertThat(classifier.classify(0)).isEqualTo(MomoResultStatusClassifier.Status.COMPLETED);
        assertThat(classifier.classify(1000)).isEqualTo(MomoResultStatusClassifier.Status.PENDING);
        assertThat(classifier.classify(7000)).isEqualTo(MomoResultStatusClassifier.Status.PENDING);
        assertThat(classifier.classify(7002)).isEqualTo(MomoResultStatusClassifier.Status.PENDING);
        assertThat(classifier.classify(9000)).isEqualTo(MomoResultStatusClassifier.Status.AUTHORIZED);
        assertThat(classifier.classify(49)).isEqualTo(MomoResultStatusClassifier.Status.FAILED);
    }
}
