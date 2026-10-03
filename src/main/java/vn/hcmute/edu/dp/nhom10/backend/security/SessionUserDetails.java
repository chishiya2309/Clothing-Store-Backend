package vn.hcmute.edu.dp.nhom10.backend.security;

import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;

import java.util.List;
import java.util.Locale;

@Getter
public class SessionUserDetails extends org.springframework.security.core.userdetails.User {
    private final Long id;
    private final String fullName;
    private final String role;
    private final long sessionVersion;

    public SessionUserDetails(User user) {
        super(user.getEmail(), user.getPasswordHash() == null ? "" : user.getPasswordHash(),
                Boolean.TRUE.equals(user.getEmailVerified()), true, true,
                Boolean.TRUE.equals(user.getIsActive()),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name().toUpperCase(Locale.ROOT))));
        id = user.getId();
        fullName = user.getFullName();
        role = user.getRole().name();
        sessionVersion = user.getSessionVersion();
    }
}
