package org.opendatamesh.platform.pp.registry.old.v1.registryservice;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

/**
 * Policy evaluations needed by the Registry V1 validation API, expressed in Registry V1 terms.
 * <p>
 * Implementations own how the evaluation requests are built and sent to a specific Policy Service version,
 * so the Registry V1 layer does not depend on any Policy Service API.
 * Every method returns the validation results keyed by policy name.
 */
public interface RegistryV1PolicyValidationOutboundPort {

    Map<String, RegistryV1DataProductValidationResult> validateDataProductCreation(RegistryV1DataProductResource dataProduct);

    Map<String, RegistryV1DataProductValidationResult> validateDataProductUpdate(RegistryV1DataProductResource currentDataProduct,
                                                                                 RegistryV1DataProductResource updatedDataProduct);

    /**
     * @param currentDescriptor the raw descriptor of the most recent existing version, or null when there is none
     * @param newDescriptor     the raw descriptor being validated
     */
    Map<String, RegistryV1DataProductValidationResult> validateDataProductVersionCreation(JsonNode currentDescriptor,
                                                                                          JsonNode newDescriptor);
}
