package vn.hcmute.edu.dp.nhom10.backend.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserRegisteredEvent extends ApplicationEvent {
    private final String email;
    private final String fullName;
    private final String token;

    public UserRegisteredEvent(Object source, String email, String fullName, String token) {
        super(source);
        this.email = email;
        this.fullName = fullName;
        this.token = token;
    }
}
