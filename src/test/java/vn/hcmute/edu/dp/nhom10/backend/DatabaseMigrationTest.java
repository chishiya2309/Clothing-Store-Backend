package vn.hcmute.edu.dp.nhom10.backend;

import org.junit.jupiter.api.Test;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class DatabaseMigrationTest {

    @Test
    public void runMigration() throws Exception {
        // Read .env file
        Map<String, String> env = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(".env"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eqIdx = line.indexOf('=');
                if (eqIdx > 0) {
                    String key = line.substring(0, eqIdx).trim();
                    String value = line.substring(eqIdx + 1).trim();
                    // Strip optional quotes
                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1);
                    } else if (value.startsWith("'") && value.endsWith("'")) {
                        value = value.substring(1, value.length() - 1);
                    }
                    env.put(key, value);
                }
            }
        }

        String dbUrl = env.get("DB_URL");
        String dbUsername = env.get("DB_USERNAME");
        String dbPassword = env.get("DB_PASSWORD");

        if (dbUrl == null || dbUsername == null || dbPassword == null) {
            throw new IllegalStateException("Database configuration variables (DB_URL, DB_USERNAME, DB_PASSWORD) not found in .env file");
        }

        System.out.println("Connecting to database: " + dbUrl);
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
             Statement stmt = conn.createStatement()) {
            
            System.out.println("Connected successfully. Running migration SQL...");

            // 1. Enable extensions
            stmt.execute("CREATE EXTENSION IF NOT EXISTS \"pg_trgm\"");
            stmt.execute("CREATE EXTENSION IF NOT EXISTS \"unaccent\"");
            System.out.println("Enabled pg_trgm and unaccent extensions.");

            // 2. Add brand column
            stmt.execute("ALTER TABLE products ADD COLUMN IF NOT EXISTS brand VARCHAR(100)");
            System.out.println("Added brand column to products.");

            // 3. Create indices
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_products_name_trgm ON products USING GIN (name gin_trgm_ops)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_products_brand_trgm ON products USING GIN (brand gin_trgm_ops)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_products_description_trgm ON products USING GIN (description gin_trgm_ops)");
            System.out.println("Created trigram indexes.");

            // 4. Update brand sample data
            stmt.execute("UPDATE products SET brand = 'Clothy' WHERE id IN (1, 2, 3, 4, 5) AND brand IS NULL");
            stmt.execute("UPDATE products SET brand = 'Coolmate' WHERE id IN (6, 7, 8, 9, 10) AND brand IS NULL");
            stmt.execute("UPDATE products SET brand = 'Levis' WHERE id IN (11, 12, 13, 14, 15) AND brand IS NULL");
            stmt.execute("UPDATE products SET brand = 'Lacoste' WHERE id IN (16, 17, 18, 19, 20) AND brand IS NULL");
            stmt.execute("UPDATE products SET brand = 'Uniqlo' WHERE brand IS NULL");
            System.out.println("Seeded sample brand data.");

            System.out.println("Migration finished successfully!");
        }
    }
}
