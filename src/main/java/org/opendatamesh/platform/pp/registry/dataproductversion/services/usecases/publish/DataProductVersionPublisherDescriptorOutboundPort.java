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
     * Extracts the version number from the descriptor content for the given specification.
     *
     * @param descriptorSpec the descriptor specification (e.g. DPDS)
     * @param descriptorSpecVersion the descriptor spec version (e.g. 1.0.0)
     * @param descriptorContent the descriptor content
     * @return the version number
     * @throws BadRequestException if the version number cannot be extracted or the spec is not supported
     */
    String extractVersionNumber(String descriptorSpec, String descriptorSpecVersion, JsonNode descriptorContent);

    /**
     * Extracts the fully qualified name from the descriptor content.
     *
     * @param descriptorContent the descriptor content
     * @return the fully qualified name
     * @throws BadRequestException if the fully qualified name cannot be extracted
     */
    String extractFullyQualifiedName(JsonNode descriptorContent);

    /**
     * Whether extension snapshot keys may be merged into the published DPDS 1.x descriptor for this
     * spec/version (via the parsed model's additional properties). When {@code false},
     * {@link #mergeExtensionPropertiesSnapshotAtDescriptorRoot} must not be invoked by the publisher.
     */
    default boolean supportsExtensionRootMerge(String descriptorSpec, String descriptorSpecVersion) {
        return false;
    }

    /**
     * Merges a publish-time extension snapshot into the parsed DPDS {@code DataProductVersion}'s
     * {@link org.opendatamesh.dpds.model.core.ComponentBase#getAdditionalProperties() additionalProperties}
     * map (deserialize → merge → serialize). When {@link #supportsExtensionRootMerge} is false, the default
     * implementation returns the enriched descriptor unchanged.
     *
     * @param enrichedDescriptor descriptor JSON after enrichment
     * @param extensionSnapshot  snapshot copied from the parent data product (nullable)
     * @return merged descriptor JSON for validation and persistence
     * @throws BadRequestException if a scope id is reserved for standard DPDS fields, or if an existing
     *                               additional property entry differs from the snapshot
     */
    default JsonNode mergeExtensionPropertiesSnapshotAtDescriptorRoot(JsonNode enrichedDescriptor, JsonNode extensionSnapshot) {
        return enrichedDescriptor;
    }
}
