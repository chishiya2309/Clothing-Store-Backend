package vn.hcmute.edu.dp.nhom10.backend.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.event.PasswordResetRequestedEvent;
import vn.hcmute.edu.dp.nhom10.backend.event.UserRegisteredEvent;
import vn.hcmute.edu.dp.nhom10.backend.service.EmailService;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "EMAIL-LISTENER")
public class EmailNotificationListener {

    private final EmailService emailService;

    @Async
    @EventListener
    public void handleUserRegisteredEvent(UserRegisteredEvent event) {
        log.info("Received UserRegisteredEvent for email: {}", event.getEmail());
        emailService.sendVerificationEmail(event.getEmail(), event.getFullName(), event.getToken());
    }

    @Async
    @EventListener
    public void handlePasswordResetRequestedEvent(PasswordResetRequestedEvent event) {
        log.info("Received PasswordResetRequestedEvent for email: {}", event.getEmail());
        emailService.sendPasswordResetEmail(event.getEmail(), event.getFullName(), event.getToken());
    }
}
