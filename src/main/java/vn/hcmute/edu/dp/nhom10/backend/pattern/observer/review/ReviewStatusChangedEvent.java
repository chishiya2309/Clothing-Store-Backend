package vn.hcmute.edu.dp.nhom10.backend.pattern.observer.review;

import org.springframework.context.ApplicationEvent;
import vn.hcmute.edu.dp.nhom10.backend.entity.Review;

public class ReviewStatusChangedEvent extends ApplicationEvent {
    private final Review review;

    public ReviewStatusChangedEvent(Object source, Review review) {
        super(source);
        this.review = review;
    }

    public Review getReview() {
        return review;
    }
}
