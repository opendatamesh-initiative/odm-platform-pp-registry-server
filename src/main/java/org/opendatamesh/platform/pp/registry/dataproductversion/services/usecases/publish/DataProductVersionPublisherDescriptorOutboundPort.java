package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.publish;

import com.fasterxml.jackson.databind.JsonNode;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;

interface DataProductVersionPublisherDescriptorOutboundPort {

    /**
     * Validates the descriptor content according to the specification.
     *
     * @param descriptorContent the descriptor content to validate
     * @throws BadRequestException if the descriptor is invalid
     */
    void validateDescriptor(JsonNode descriptorContent);

    /**
     * Extracts the version number from the descriptor content.
     *
     * @param descriptorContent the descriptor content
     * @return the version number
     * @throws BadRequestException if the version number cannot be extracted
     */
    String extractVersionNumber(JsonNode descriptorContent);
}
