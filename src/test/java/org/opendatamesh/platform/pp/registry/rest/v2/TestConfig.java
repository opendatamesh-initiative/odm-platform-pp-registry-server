package org.opendatamesh.platform.pp.registry.rest.v2;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Test configuration for integration tests.
 * This class is included in the SpringBootTest configuration to provide
 * test-specific beans and configurations.
 */
@Profile("test")
@Configuration
public class TestConfig {
    // Test configuration can be added here if needed
}
