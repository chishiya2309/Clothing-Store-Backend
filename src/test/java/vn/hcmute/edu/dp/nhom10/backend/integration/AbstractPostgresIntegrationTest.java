package vn.hcmute.edu.dp.nhom10.backend.integration;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.testcontainers.containers.PostgreSQLContainer;
import vn.hcmute.edu.dp.nhom10.backend.entity.PaymentAttempt;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentAttemptStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.event.OrderCreatedEvent;
import vn.hcmute.edu.dp.nhom10.backend.exception.PaymentInitializationException;
import vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment.GatewayPaymentCreationCommand;
import vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment.GatewayPaymentCreationResult;
import vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment.PaymentGatewayAdapter;
import vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment.PaymentGatewayAdapterFactory;
import vn.hcmute.edu.dp.nhom10.backend.repository.PaymentAttemptRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@SpringBootTest
@ActiveProfiles("integration")
@Sql(scripts = "/db/cleanup_place_order.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import(AbstractPostgresIntegrationTest.IntegrationTestConfiguration.class)
public abstract class AbstractPostgresIntegrationTest {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("clothing_store_it")
            .withUsername("test")
            .withPassword("test");

    private static final AtomicBoolean GATEWAY_FAILS = new AtomicBoolean(false);
    private static final AtomicBoolean GATEWAY_OBSERVED_PENDING_ATTEMPT = new AtomicBoolean(false);
    private static final AtomicBoolean SCHEMA_INITIALIZED = new AtomicBoolean(false);

    @Autowired
    protected OrderCreatedEventProbe orderCreatedEventProbe;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        startPostgresContainer();
        initializeDatabaseSchema();

        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.jpa.show-sql", () -> "false");
        registry.add("spring.sql.init.mode", () -> "never");
        registry.add("checkout.cleanup.enabled", () -> "false");
        registry.add("checkout.reservation-ttl-minutes", () -> "15");
        registry.add("payment.return-url", () -> "https://frontend.test/payment-return");
        registry.add("payment.callback-url", () -> "https://backend.test/payment-callback");
        registry.add("jwt.secret", () -> "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=");
        registry.add("jwt.expiration", () -> "900000");
        registry.add("brevo.api-key", () -> "test-api-key");
        registry.add("brevo.sender-email", () -> "noreply@example.test");
        registry.add("brevo.sender-name", () -> "Clothing Store Test");
        registry.add("aws.s3.access-key", () -> "test-access-key");
        registry.add("aws.s3.secret-key", () -> "test-secret-key");
        registry.add("aws.s3.region", () -> "ap-southeast-1");
        registry.add("aws.s3.bucket-name", () -> "test-bucket");
        registry.add("app.verification-token-ttl", () -> "900");
        registry.add("app.refresh-token-ttl", () -> "604800");
        registry.add("app.remember-me-token-ttl", () -> "2592000");
        registry.add("google.client-id", () -> "google-client-id-test");
        registry.add("spring.data.redis.host", () -> "localhost");
        registry.add("spring.data.redis.port", () -> "6379");
    }

    @BeforeEach
    void resetIntegrationState() {
        GATEWAY_FAILS.set(false);
        GATEWAY_OBSERVED_PENDING_ATTEMPT.set(false);
        orderCreatedEventProbe.clear();
    }

    protected void makeGatewayFail() {
        GATEWAY_FAILS.set(true);
    }

    protected boolean gatewayObservedPendingAttempt() {
        return GATEWAY_OBSERVED_PENDING_ATTEMPT.get();
    }

    private static void initializeDatabaseSchema() {
        if (!SCHEMA_INITIALIZED.compareAndSet(false, true)) {
            return;
        }

        try (Connection connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(),
                POSTGRES.getUsername(),
                POSTGRES.getPassword()
        );
             Statement statement = connection.createStatement()) {
            for (String scriptLocation : List.of(
                    "db/database_schema.sql",
                    "db/phase1_checkout_schema_patch.sql"
            )) {
                String script = readClasspathResource(scriptLocation);
                for (String sql : splitPostgresStatements(script)) {
                    statement.execute(sql);
                }
            }
        } catch (IOException | SQLException e) {
            SCHEMA_INITIALIZED.set(false);
            throw new IllegalStateException("Failed to initialize PostgreSQL integration schema", e);
        }
    }

    private static void startPostgresContainer() {
        if (!POSTGRES.isRunning()) {
            POSTGRES.start();
        }
    }

    private static String readClasspathResource(String location) throws IOException {
        ClassPathResource resource = new ClassPathResource(location);
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static List<String> splitPostgresStatements(String script) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingleQuote = false;
        boolean inLineComment = false;
        boolean inBlockComment = false;
        String dollarQuoteTag = null;

        for (int i = 0; i < script.length(); i++) {
            char currentChar = script.charAt(i);
            char nextChar = i + 1 < script.length() ? script.charAt(i + 1) : '\0';

            if (inLineComment) {
                current.append(currentChar);
                if (currentChar == '\n') {
                    inLineComment = false;
                }
                continue;
            }

            if (inBlockComment) {
                current.append(currentChar);
                if (currentChar == '*' && nextChar == '/') {
                    current.append(nextChar);
                    i++;
                    inBlockComment = false;
                }
                continue;
            }

            if (dollarQuoteTag != null) {
                if (script.startsWith(dollarQuoteTag, i)) {
                    current.append(dollarQuoteTag);
                    i += dollarQuoteTag.length() - 1;
                    dollarQuoteTag = null;
                } else {
                    current.append(currentChar);
                }
                continue;
            }

            if (inSingleQuote) {
                current.append(currentChar);
                if (currentChar == '\'' && nextChar == '\'') {
                    current.append(nextChar);
                    i++;
                } else if (currentChar == '\'') {
                    inSingleQuote = false;
                }
                continue;
            }

            if (currentChar == '-' && nextChar == '-') {
                current.append(currentChar).append(nextChar);
                i++;
                inLineComment = true;
                continue;
            }

            if (currentChar == '/' && nextChar == '*') {
                current.append(currentChar).append(nextChar);
                i++;
                inBlockComment = true;
                continue;
            }

            if (currentChar == '\'') {
                current.append(currentChar);
                inSingleQuote = true;
                continue;
            }

            if (currentChar == '$') {
                String tag = findDollarQuoteTag(script, i);
                if (tag != null) {
                    current.append(tag);
                    i += tag.length() - 1;
                    dollarQuoteTag = tag;
                    continue;
                }
            }

            if (currentChar == ';') {
                addStatement(statements, current);
                current.setLength(0);
                continue;
            }

            current.append(currentChar);
        }

        addStatement(statements, current);
        return statements;
    }

    private static String findDollarQuoteTag(String script, int start) {
        int end = script.indexOf('$', start + 1);
        if (end < 0) {
            return null;
        }
        String tagName = script.substring(start + 1, end);
        if (!tagName.chars().allMatch(ch -> Character.isLetterOrDigit(ch) || ch == '_')) {
            return null;
        }
        return script.substring(start, end + 1);
    }

    private static void addStatement(List<String> statements, StringBuilder current) {
        String statement = current.toString().trim();
        if (!statement.isEmpty()) {
            statements.add(statement);
        }
    }

    @TestConfiguration
    static class IntegrationTestConfiguration {

        @Bean
        @Primary
        @ConditionalOnProperty(name = "test.fake-payment-gateway.enabled", havingValue = "true", matchIfMissing = true)
        PaymentGatewayAdapterFactory paymentGatewayAdapterFactory(PaymentAttemptRepository paymentAttemptRepository) {
            PaymentGatewayAdapter fakeVnPayAdapter = new PaymentGatewayAdapter() {
                @Override
                public PaymentMethod supportMethod() {
                    return PaymentMethod.vnpay;
                }

                @Override
                public boolean isAvailable() {
                    return true;
                }

                @Override
                public GatewayPaymentCreationResult createPayment(GatewayPaymentCreationCommand command) {
                    if (GATEWAY_FAILS.get()) {
                        throw new PaymentInitializationException("Fake gateway failure");
                    }

                    PaymentAttempt pendingAttempt = paymentAttemptRepository
                            .findByPaymentReference(command.paymentReference())
                            .orElseThrow(() -> new PaymentInitializationException("Pending payment attempt was not committed"));
                    if (pendingAttempt.getStatus() == PaymentAttemptStatus.pending
                            && pendingAttempt.getPaymentUrl() == null) {
                        GATEWAY_OBSERVED_PENDING_ATTEMPT.set(true);
                    }

                    return new GatewayPaymentCreationResult(
                            "https://gateway.test/pay/" + command.paymentReference(),
                            "GTW-" + command.paymentReference(),
                            Map.of("checkoutCode", command.checkoutCode())
                    );
                }
            };
            PaymentGatewayAdapter fakeMomoAdapter = new PaymentGatewayAdapter() {
                @Override
                public PaymentMethod supportMethod() {
                    return PaymentMethod.momo;
                }

                @Override
                public boolean isAvailable() {
                    return true;
                }

                @Override
                public GatewayPaymentCreationResult createPayment(GatewayPaymentCreationCommand command) {
                    if (GATEWAY_FAILS.get()) {
                        throw new PaymentInitializationException("Fake gateway failure");
                    }

                    PaymentAttempt pendingAttempt = paymentAttemptRepository
                            .findByPaymentReference(command.paymentReference())
                            .orElseThrow(() -> new PaymentInitializationException("Pending payment attempt was not committed"));
                    if (pendingAttempt.getStatus() == PaymentAttemptStatus.pending
                            && pendingAttempt.getPaymentUrl() == null) {
                        GATEWAY_OBSERVED_PENDING_ATTEMPT.set(true);
                    }

                    return new GatewayPaymentCreationResult(
                            "https://test-payment.momo.vn/pay/" + command.paymentReference(),
                            null,
                            Map.of(
                                    "gateway", "momo",
                                    "checkoutCode", command.checkoutCode(),
                                    "orderId", command.paymentReference(),
                                    "requestId", command.paymentReference()
                            )
                    );
                }
            };
            return new PaymentGatewayAdapterFactory(List.of(fakeVnPayAdapter, fakeMomoAdapter));
        }

        @Bean
        OrderCreatedEventProbe orderCreatedEventProbe() {
            return new OrderCreatedEventProbe();
        }
    }

    public static class OrderCreatedEventProbe {
        private final List<OrderCreatedEvent> events = new ArrayList<>();

        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        public synchronized void onOrderCreated(OrderCreatedEvent event) {
            events.add(event);
        }

        public synchronized void clear() {
            events.clear();
        }

        public synchronized List<OrderCreatedEvent> events() {
            return List.copyOf(events);
        }
    }
}
