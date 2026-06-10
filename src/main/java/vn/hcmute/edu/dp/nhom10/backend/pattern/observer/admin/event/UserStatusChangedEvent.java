package vn.hcmute.edu.dp.nhom10.backend.pattern.observer.admin.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserStatusChangedEvent extends ApplicationEvent {
    private final Long userId;
    private final String email;
    private final Boolean oldStatus;
    private final Boolean newStatus;

    public UserStatusChangedEvent(Object source, Long userId, String email, Boolean oldStatus, Boolean newStatus) {
        super(source);
        this.userId = userId;
        this.email = email;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }
}
