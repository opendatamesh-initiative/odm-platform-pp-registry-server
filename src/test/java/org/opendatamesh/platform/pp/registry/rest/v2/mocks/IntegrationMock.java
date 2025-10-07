package org.opendatamesh.platform.pp.registry.rest.v2.mocks;

/**
 * Base class for integration test mocks.
 * Provides a common interface for resetting mock state between tests.
 */
public abstract class IntegrationMock {

    protected IntegrationMock() {
        // Don't call reset() here - subclasses should call it when needed
        // to avoid initialization order issues
    }

    /**
     * Reset the mock to its initial state.
     * Should be called in @BeforeEach or @AfterEach methods.
     */
    public abstract void reset();
}
