package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.publish;

import com.fasterxml.jackson.databind.JsonNode;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;

interface DataProductVersionPublisherDescriptorOutboundPort {

    /**
     * Validates the descriptor content according to the specification.
     * When {@code expectedDataProductFqn} is non-null, the implementation must ensure it equals
     * the descriptor's data product FQN (e.g. {@code info.fullyQualifiedName} for DPDS), and must
     * perform this check first before any other validation.
     *
     * @param descriptorSpec the descriptor specification (e.g. DPDS)
     * @param descriptorSpecVersion the descriptor spec version (e.g. 1.0.0)
     * @param descriptorContent the descriptor content to validate
     * @param expectedDataProductFqn the FQN of the data product that owns the version; when non-null, must match the descriptor's data product FQN
     * @throws BadRequestException if the descriptor is invalid
     */
    void validateDescriptor(String descriptorSpec, String descriptorSpecVersion, JsonNode descriptorContent, String expectedDataProductFqn);

    /**
     * Enriches descriptor content with auto-generated fields (e.g. id, fullyQualifiedName, entityType)
     * when applicable for the given spec and spec version. Returns the same content unchanged if
     * no enrichment is supported.
     *
     * @param descriptorSpec the descriptor specification
     * @param descriptorSpecVersion the descriptor spec version
     * @param descriptorContent the descriptor content
     * @return the descriptor content, possibly with generated fields filled in
     */
    JsonNode enrichDescriptorContentIfNeeded(String descriptorSpec, String descriptorSpecVersion, JsonNode descriptorContent);

    /**
     * Extracts the version number from the descriptor content.
     *
     * @param descriptorContent the descriptor content
     * @return the version number
     * @throws BadRequestException if the version number cannot be extracted
     */
    String extractVersionNumber(JsonNode descriptorContent);
}
