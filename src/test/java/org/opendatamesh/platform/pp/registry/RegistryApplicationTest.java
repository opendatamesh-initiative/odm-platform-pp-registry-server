package org.opendatamesh.platform.pp.registry;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class RegistryApplicationTest {

    @Test
    void contextLoads() {
        // This test verifies that the Spring Boot application context loads successfully
        assertTrue(true, "Application context should load successfully");
    }

    @Test
    void dummyTest() {
        // Simple dummy test to verify test infrastructure is working
        String message = "Hello from Registry Server tests!";
        assertTrue(message.contains("Registry Server"), "Message should contain 'Registry Server'");
        assertTrue(message.length() > 0, "Message should not be empty");
    }
}
