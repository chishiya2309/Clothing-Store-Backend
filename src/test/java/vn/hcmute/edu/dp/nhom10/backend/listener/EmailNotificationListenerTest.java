package vn.hcmute.edu.dp.nhom10.backend.listener;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.hcmute.edu.dp.nhom10.backend.event.PasswordResetRequestedEvent;
import vn.hcmute.edu.dp.nhom10.backend.service.EmailService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class EmailNotificationListenerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailNotificationListener listener;

    @Test
    void handlePasswordResetRequestedEvent_forwardsStoredEmailAndToken() {
        PasswordResetRequestedEvent event = new PasswordResetRequestedEvent(
                this, "aKi23092005@gMaIL.CoM", "Test User", "reset-token");

        listener.handlePasswordResetRequestedEvent(event);

        verify(emailService).sendPasswordResetEmail(event.getEmail(), event.getFullName(), event.getToken());
        verifyNoMoreInteractions(emailService);
    }
}
