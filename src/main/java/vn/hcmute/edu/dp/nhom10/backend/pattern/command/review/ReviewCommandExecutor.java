package vn.hcmute.edu.dp.nhom10.backend.pattern.command.review;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.entity.ActivityLog;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.repository.ActivityLogRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.UserRepository;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ReviewCommandExecutor {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public <T> T execute(ReviewCommand<T> command, String userEmail) {
        T result = command.execute();

        User user = null;
        if (userEmail != null && !userEmail.isBlank()) {
            user = userRepository.findByEmail(userEmail).orElse(null);
        }

        ActivityLog log = ActivityLog.builder()
                .user(user)
                .action("REVIEW_MODERATION")
                .entityType(command.getClass().getSimpleName())
                .newData(Map.of("description", command.getDescription()))
                .build();

        activityLogRepository.save(log);

        return result;
    }
}
