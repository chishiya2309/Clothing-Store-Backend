package vn.hcmute.edu.dp.nhom10.backend.pattern.command;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.hcmute.edu.dp.nhom10.backend.entity.ActivityLog;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.pattern.command.catalog.CatalogCommand;
import vn.hcmute.edu.dp.nhom10.backend.pattern.command.catalog.CatalogCommandExecutor;
import vn.hcmute.edu.dp.nhom10.backend.repository.ActivityLogRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CatalogCommandTest {

    @Mock
    private ActivityLogRepository activityLogRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CatalogCommandExecutor commandExecutor;

    @Test
    public void testCommandExecutionAndLogging() {
        String email = "staff@store.com";
        User user = User.builder().id(5L).email(email).build();
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        CatalogCommand<String> mockCommand = mock(CatalogCommand.class);
        when(mockCommand.execute()).thenReturn("SuccessResult");
        when(mockCommand.getDescription()).thenReturn("Tested Catalog Action");

        String result = commandExecutor.execute(mockCommand, email);

        assertEquals("SuccessResult", result);
        verify(mockCommand, times(1)).execute();
        verify(activityLogRepository, times(1)).save(any(ActivityLog.class));
    }
}
