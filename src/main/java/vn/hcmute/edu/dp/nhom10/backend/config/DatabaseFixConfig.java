package vn.hcmute.edu.dp.nhom10.backend.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DatabaseFixConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseFixConfig.class);
    private final JdbcTemplate jdbcTemplate;

    public DatabaseFixConfig(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void fixDatabaseSchema() {
        try {
            log.info("Running Database Schema Fixes...");
            // Fix image_url to be TEXT instead of VARCHAR(255)
            jdbcTemplate.execute("ALTER TABLE product_images ALTER COLUMN image_url TYPE TEXT;");
            log.info("Successfully altered image_url to TEXT in product_images");

            // Just in case, also alter alt_text
            jdbcTemplate.execute("ALTER TABLE product_images ALTER COLUMN alt_text TYPE TEXT;");
            log.info("Successfully altered alt_text to TEXT in product_images");

        } catch (Exception e) {
            log.warn("Could not alter table (it might already be altered or this is not postgres): {}", e.getMessage());
        }
    }
}
