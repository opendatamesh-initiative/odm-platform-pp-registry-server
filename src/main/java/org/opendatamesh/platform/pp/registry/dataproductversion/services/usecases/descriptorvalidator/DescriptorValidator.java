package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.descriptorvalidator;

import com.fasterxml.jackson.databind.JsonNode;

public interface DescriptorValidator {
    /**
     * Validates the descriptor content. When {@code expectedDataProductFqn} is non-null, the validator
     * must ensure the descriptor's data product FQN (e.g. {@code info.fullyQualifiedName} for DPDS) equals
     * it, and should perform this check as the first validation step when applicable.
     *
     * @param descriptorContent the descriptor content to validate
     * @param expectedDataProductFqn the expected data product FQN, or null to skip FQN consistency check
     */
    void validateDescriptor(JsonNode descriptorContent, String expectedDataProductFqn);
}
