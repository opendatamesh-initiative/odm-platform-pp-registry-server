package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.publish;

import com.fasterxml.jackson.databind.JsonNode;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;

interface DataProductVersionPublisherDescriptorOutboundPort {

    /**
     * Validates the descriptor content according to the specification.
     *
     * @param descriptorSpec the descriptor specification (e.g. DPDS)
     * @param descriptorSpecVersion the descriptor spec version (e.g. 1.0.0)
     * @param descriptorContent the descriptor content to validate
     * @throws BadRequestException if the descriptor is invalid
     */
    void validateDescriptor(String descriptorSpec, String descriptorSpecVersion, JsonNode descriptorContent);

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

    /**
     * Extracts the fully qualified name from the descriptor content.
     *
     * @param descriptorContent the descriptor content
     * @return the fully qualified name
     * @throws BadRequestException if the fully qualified name cannot be extracted
     */
    String extractFullyQualifiedName(JsonNode descriptorContent);
}
