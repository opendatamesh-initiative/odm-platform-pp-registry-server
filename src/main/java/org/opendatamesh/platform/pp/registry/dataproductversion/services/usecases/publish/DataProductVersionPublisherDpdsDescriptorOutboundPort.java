package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.publish;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import org.opendatamesh.dpds.model.DataProductVersion;
import org.opendatamesh.dpds.parser.Parser;
import org.opendatamesh.dpds.parser.ParserFactory;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DescriptorSpec;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.descriptorvalidator.DescriptorValidator;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.descriptorvalidator.DescriptorValidatorFactory;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

class DataProductVersionPublisherDpdsDescriptorOutboundPort implements DataProductVersionPublisherDescriptorOutboundPort {

    /**
     * DPDS top-level JSON property names (bean properties on {@link DataProductVersion}). Extension scope
     * identifiers must not collide with these names (case-insensitive).
     */
    private static final Set<String> RESERVED_DPDS_TOP_LEVEL_PROPERTY_NAMES = Set.of(
            "info",
            "interfacecomponents",
            "internalcomponents",
            "components",
            "tags",
            "externaldocs",
            "dataproductdescriptor"
    );

    private final DescriptorValidatorFactory descriptorValidatorFactory;
    private final Parser parser = ParserFactory.getParser();

    DataProductVersionPublisherDpdsDescriptorOutboundPort(DescriptorValidatorFactory descriptorValidatorFactory) {
        this.descriptorValidatorFactory = descriptorValidatorFactory;
    }

    @Override
    public void validateDescriptor(String descriptorSpec, String descriptorSpecVersion, JsonNode descriptorContent) {
        DescriptorValidator validator = descriptorValidatorFactory.getDescriptorValidator(descriptorSpec, descriptorSpecVersion);
        validator.validateDescriptor(descriptorContent);
    }

    @Override
    public boolean supportsExtensionRootMerge(String descriptorSpec, String descriptorSpecVersion) {
        return isDpds1x(descriptorSpec, descriptorSpecVersion);
    }

    /**
     * Parses the enriched DPDS descriptor, merges each extension snapshot scope into
     * {@link org.opendatamesh.dpds.model.core.ComponentBase#getAdditionalProperties()}, and serializes again.
     * Skips when the snapshot is null/absent or not an object. Rejects scope ids that match standard DPDS
     * top-level property names, and rejects overwrites of existing additional-property entries that differ
     * from the snapshot (no silent clobber).
     */
    @Override
    public JsonNode mergeExtensionPropertiesSnapshotAtDescriptorRoot(JsonNode enrichedDescriptor, JsonNode extensionSnapshot) {
        if (extensionSnapshot == null || extensionSnapshot.isNull() || extensionSnapshot.isMissingNode() || !extensionSnapshot.isObject()) {
            return enrichedDescriptor;
        }
        if (enrichedDescriptor == null || !enrichedDescriptor.isObject()) {
            return enrichedDescriptor;
        }
        DataProductVersion model;
        try {
            model = parser.deserialize(enrichedDescriptor);
        } catch (IOException e) {
            throw new BadRequestException("Failed to parse DPDS descriptor: " + e.getMessage(), e);
        }
        if (model == null) {
            return enrichedDescriptor;
        }
        Map<String, JsonNode> additional = model.getAdditionalProperties();
        Map<String, JsonNode> merged = new LinkedHashMap<>();
        if (additional != null) {
            merged.putAll(additional);
        }
        for (Iterator<String> fn = extensionSnapshot.fieldNames(); fn.hasNext(); ) {
            String scope = fn.next();
            if (isReservedDpdsTopLevelPropertyName(scope)) {
                throw new BadRequestException(
                        "Cannot publish: extension property scope '" + scope
                                + "' matches a standard DPDS top-level field name; choose a different scope identifier.");
            }
            JsonNode snapshotValue = extensionSnapshot.get(scope);
            JsonNode existing = merged.get(scope);
            if (existing != null && !existing.isNull() && !existing.equals(snapshotValue)) {
                throw new BadRequestException(
                        "Cannot publish: extension property scope '" + scope
                                + "' collides with an existing additional property on the descriptor that does not match the snapshot.");
            }
            if (snapshotValue == null || snapshotValue.isNull() || snapshotValue.isMissingNode()) {
                merged.put(scope, NullNode.getInstance());
            } else {
                merged.put(scope, snapshotValue.deepCopy());
            }
        }
        model.setAdditionalProperties(merged);
        try {
            return parser.serialize(model);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isReservedDpdsTopLevelPropertyName(String scope) {
        return scope != null && RESERVED_DPDS_TOP_LEVEL_PROPERTY_NAMES.contains(scope.toLowerCase(Locale.ROOT));
    }

    private static boolean isDpds1x(String descriptorSpec, String descriptorSpecVersion) {
        return StringUtils.hasText(descriptorSpec)
                && StringUtils.hasText(descriptorSpecVersion)
                && descriptorSpec.equalsIgnoreCase(DescriptorSpec.DPDS.name())
                && descriptorSpecVersion.matches("1\\..*");
    }

    @Override
    public JsonNode enrichDescriptorContentIfNeeded(String descriptorSpec, String descriptorSpecVersion, JsonNode descriptorContent) {
        if (!StringUtils.hasText(descriptorSpec) || !StringUtils.hasText(descriptorSpecVersion)) {
            return descriptorContent;
        }
        if (!isDpds1x(descriptorSpec, descriptorSpecVersion)) {
            return descriptorContent;
        }

        DataProductVersion dataProductVersion;
        try {
            dataProductVersion = parser.deserialize(descriptorContent);
        } catch (IOException e) {
            throw new BadRequestException("Failed to parse DPDS descriptor: " + e.getMessage(), e);
        }
        if (dataProductVersion == null) {
            return descriptorContent;
        }

        DpdsFieldGenerationVisitor visitor = new DpdsFieldGenerationVisitor();
        if (dataProductVersion.getInfo() != null) {
            dataProductVersion.getInfo().accept(visitor);
        }
        if (dataProductVersion.getInterfaceComponents() != null) {
            dataProductVersion.getInterfaceComponents().accept(visitor);
        }
        if (dataProductVersion.getInternalComponents() != null) {
            dataProductVersion.getInternalComponents().accept(visitor);
        }
        if (dataProductVersion.getComponents() != null) {
            dataProductVersion.getComponents().accept(visitor);
        }

        try {
            return parser.serialize(dataProductVersion);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String extractVersionNumber(String descriptorSpec, String descriptorSpecVersion, JsonNode descriptorContent) {
        if (!StringUtils.hasText(descriptorSpec) || !StringUtils.hasText(descriptorSpecVersion)) {
            throw new BadRequestException("Descriptor specification and version are required to extract the version number");
        }
        if (!isDpds1x(descriptorSpec, descriptorSpecVersion)) {
            throw new BadRequestException(String.format(
                    "Version extraction is not supported for descriptor specification %s version %s. Currently only DPDS 1.x is supported.",
                    descriptorSpec, descriptorSpecVersion));
        }

        DataProductVersion dataProductVersion;
        try {
            dataProductVersion = parser.deserialize(descriptorContent);
        } catch (IOException e) {
            throw new BadRequestException("Failed to parse DPDS descriptor: " + e.getMessage(), e);
        }

        if (dataProductVersion == null || dataProductVersion.getInfo() == null) {
            throw new BadRequestException("DPDS descriptor is missing the 'info' section");
        }

        String version = dataProductVersion.getInfo().getVersion();
        if (version == null || version.isEmpty()) {
            throw new BadRequestException("DPDS descriptor is missing the version number in the 'info' section");
        }

        return version;
    }

    @Override
    public String extractFullyQualifiedName(JsonNode descriptorContent) {
        DataProductVersion dataProductVersion;
        try {
            dataProductVersion = parser.deserialize(descriptorContent);
        } catch (IOException e) {
            throw new BadRequestException("Failed to parse DPDS descriptor: " + e.getMessage(), e);
        }

        if (dataProductVersion == null || dataProductVersion.getInfo() == null) {
            throw new BadRequestException("DPDS descriptor is missing the 'info' section");
        }

        String fullyQualifiedName = dataProductVersion.getInfo().getFullyQualifiedName();
        if (fullyQualifiedName == null || fullyQualifiedName.isEmpty()) {
            throw new BadRequestException("DPDS descriptor is missing the fullyQualifiedName in the 'info' section");
        }

        return fullyQualifiedName;
    }
}
