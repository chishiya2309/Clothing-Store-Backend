package vn.hcmute.edu.dp.nhom10.backend.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.ResetPasswordRequest;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.enums.UserRole;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.repository.UserRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.AuthService;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PasswordResetSessionIT extends AbstractPostgresIntegrationTest {
    @Autowired UserRepository users;
    @Autowired AuthService auth;
    @Autowired PasswordEncoder passwords;
    @Autowired DataSource dataSource;
    @MockitoBean RedisTemplate<String, Object> redis;
    private User user;
    private Map<String, Object> values;
    private final ResetPasswordRequest request = new ResetPasswordRequest("reset", "NewPass123", "NewPass123");

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setupSession() {
        user = users.saveAndFlush(User.builder().email("session@test.com").fullName("Session Test")
                .role(UserRole.customer).loyaltyPoints(0).isActive(true).emailVerified(true)
                .passwordHash(passwords.encode("OldPass123")).build());
        values = new ConcurrentHashMap<>();
        values.put("password_reset:reset", user.getId().toString());
        values.put("refresh_token:old", user.getId() + ":0");
        ValueOperations<String, Object> ops = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(ops);
        when(ops.get(anyString())).thenAnswer(call -> values.get(call.getArgument(0)));
        when(redis.delete(anyString())).thenAnswer(call -> values.remove(call.getArgument(0)) != null);
    }

    @Test
    void passwordAndRevocationCommitTogetherAndResetTokenIsSingleUse() {
        auth.resetPassword(request);
        User changed = users.findById(user.getId()).orElseThrow();
        assertTrue(passwords.matches("NewPass123", changed.getPasswordHash()));
        assertEquals(1, changed.getSessionVersion());
        assertThrows(InvalidDataException.class, () -> auth.resetPassword(request));
        assertThrows(BadCredentialsException.class, () -> auth.refreshToken("old"));
        verify(redis, never()).keys(anyString());
    }

    @Test
    void failureAfterFlushRollsBackPasswordAndSessionVersion() {
        when(redis.delete("password_reset:reset")).thenThrow(new IllegalStateException("Redis unavailable"));
        assertThrows(IllegalStateException.class, () -> auth.resetPassword(request));
        User unchanged = users.findById(user.getId()).orElseThrow();
        assertTrue(passwords.matches("OldPass123", unchanged.getPasswordHash()));
        assertEquals(0, unchanged.getSessionVersion());
        assertEquals(user.getRowVersion(), unchanged.getRowVersion());
    }

    @Test
    void staleEntityCannotRestoreOldPasswordOrSessionVersion() {
        User stale = users.findById(user.getId()).orElseThrow();
        auth.resetPassword(request);
        stale.setFullName("Late profile update");
        assertThrows(ObjectOptimisticLockingFailureException.class, () -> users.saveAndFlush(stale));
        User changed = users.findById(user.getId()).orElseThrow();
        assertEquals(1, changed.getSessionVersion());
        assertTrue(passwords.matches("NewPass123", changed.getPasswordHash()));
    }

    @Test
    void concurrentResetRequestsConsumeTheTokenOnce() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        Callable<Boolean> reset = () -> {
            if (!start.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("Start timeout");
            try { auth.resetPassword(request); return true; }
            catch (InvalidDataException ex) { return false; }
        };
        try {
            Future<Boolean> a = executor.submit(reset);
            Future<Boolean> b = executor.submit(reset);
            start.countDown();
            assertNotEquals(a.get(15, TimeUnit.SECONDS), b.get(15, TimeUnit.SECONDS));
            assertEquals(1, users.findById(user.getId()).orElseThrow().getSessionVersion());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void migrationSupportsExistingTablesAndPreservesVersionsOnRepeatedRuns() throws Exception {
        String patch = new ClassPathResource("db/password_reset_session_version_patch.sql")
                .getContentAsString(StandardCharsets.UTF_8);
        try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA session_migration_test");
            try {
                statement.execute("SET search_path TO session_migration_test");
                statement.execute("CREATE TABLE users (id BIGINT PRIMARY KEY)");
                statement.execute("INSERT INTO users VALUES (1)");
                statement.execute(patch);
                try (var result = statement.executeQuery("SELECT session_version, row_version FROM users")) {
                    assertTrue(result.next());
                    assertEquals(0, result.getLong(1));
                    assertEquals(0, result.getLong(2));
                }
                statement.execute("UPDATE users SET session_version = 5, row_version = 6");
                statement.execute(patch);
                try (var result = statement.executeQuery("SELECT session_version, row_version FROM users")) {
                    assertTrue(result.next());
                    assertEquals(5, result.getLong(1));
                    assertEquals(6, result.getLong(2));
                }
            } finally {
                statement.execute("SET search_path TO public");
                statement.execute("DROP SCHEMA session_migration_test CASCADE");
            }
        }
    }
}
