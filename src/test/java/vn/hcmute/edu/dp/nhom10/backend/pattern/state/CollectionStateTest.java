package vn.hcmute.edu.dp.nhom10.backend.pattern.state;

import org.junit.jupiter.api.Test;
import vn.hcmute.edu.dp.nhom10.backend.entity.Collection;
import vn.hcmute.edu.dp.nhom10.backend.enums.CollectionStatusState;
import vn.hcmute.edu.dp.nhom10.backend.pattern.state.collection.CollectionStateResolver;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CollectionStateTest {

    @Test
    public void testResolveCollectionStates() {
        CollectionStateResolver resolver = new CollectionStateResolver();

        // 1. Inactive state
        Collection c1 = Collection.builder().isActive(false).build();
        assertEquals(CollectionStatusState.INACTIVE, resolver.resolve(c1));

        // 2. Active state (no dates set)
        Collection c2 = Collection.builder().isActive(true).build();
        assertEquals(CollectionStatusState.ACTIVE, resolver.resolve(c2));

        // 3. Active state (current time in range)
        Collection c3 = Collection.builder()
                .isActive(true)
                .startDate(OffsetDateTime.now().minusDays(1))
                .endDate(OffsetDateTime.now().plusDays(1))
                .build();
        assertEquals(CollectionStatusState.ACTIVE, resolver.resolve(c3));

        // 4. Scheduled state (startDate is in the future)
        Collection c4 = Collection.builder()
                .isActive(true)
                .startDate(OffsetDateTime.now().plusDays(2))
                .build();
        assertEquals(CollectionStatusState.SCHEDULED, resolver.resolve(c4));

        // 5. Expired state (endDate is in the past)
        Collection c5 = Collection.builder()
                .isActive(true)
                .endDate(OffsetDateTime.now().minusDays(1))
                .build();
        assertEquals(CollectionStatusState.EXPIRED, resolver.resolve(c5));
    }
}
