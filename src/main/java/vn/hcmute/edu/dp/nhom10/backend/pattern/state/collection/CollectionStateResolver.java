package vn.hcmute.edu.dp.nhom10.backend.pattern.state.collection;

import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.entity.Collection;
import vn.hcmute.edu.dp.nhom10.backend.enums.CollectionStatusState;

import java.time.OffsetDateTime;

@Component
public class CollectionStateResolver {

    public CollectionStatusState resolve(Collection collection) {
        if (collection == null) return CollectionStatusState.INACTIVE;
        if (!Boolean.TRUE.equals(collection.getIsActive())) {
            return CollectionStatusState.INACTIVE;
        }
        
        OffsetDateTime now = OffsetDateTime.now();
        if (collection.getStartDate() != null && now.isBefore(collection.getStartDate())) {
            return CollectionStatusState.SCHEDULED;
        }
        if (collection.getEndDate() != null && now.isAfter(collection.getEndDate())) {
            return CollectionStatusState.EXPIRED;
        }
        
        return CollectionStatusState.ACTIVE;
    }
}
