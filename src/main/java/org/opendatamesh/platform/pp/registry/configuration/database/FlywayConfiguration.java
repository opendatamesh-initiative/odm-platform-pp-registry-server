package org.opendatamesh.platform.pp.registry.configuration.database;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
public class FlywayConfiguration {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final String VENDOR_PLACEHOLDER = "{vendor}";
    private static final String DB_MIGRATION_VENDOR = "db/migration/" + VENDOR_PLACEHOLDER;

    @Autowired
    private DataSource dataSource;

    @Value("${spring.jpa.properties.hibernate.default_schema}")
    private String defaultSchema;

    @Bean
    public Flyway flyway() {
        logger.info("Initializing Flyway with schema: {}", defaultSchema);
        if (!defaultSchema.toLowerCase().equals(defaultSchema)) {
            throw new IllegalStateException("Default schema must contain lower cases only.");
        }

        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas(defaultSchema)
                .locations(DB_MIGRATION_VENDOR.replace(VENDOR_PLACEHOLDER, readVendor()))
                .load();
        flyway.migrate();
        return flyway;
    }

    private String readVendor() {
        try (var connection = dataSource.getConnection()) {
            DatabaseDriver vendor = DatabaseDriver.fromJdbcUrl(connection.getMetaData().getURL());
            if (vendor == DatabaseDriver.H2) {
                return DatabaseDriver.POSTGRESQL.name().toLowerCase();
            }
            if (vendor != DatabaseDriver.UNKNOWN) {
                return vendor.name().toLowerCase();
            }
            logger.warn("Unrecognized vendor. Using default: {}", DatabaseDriver.POSTGRESQL);
            return DatabaseDriver.POSTGRESQL.name().toLowerCase();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to determine database vendor", e);
        }
    }
}
