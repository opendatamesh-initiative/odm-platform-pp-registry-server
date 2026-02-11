package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.descriptorvalidator;

import com.fasterxml.jackson.databind.JsonNode;

public interface DescriptorValidator {
    void validateDescriptor(JsonNode descriptorContent);
}
