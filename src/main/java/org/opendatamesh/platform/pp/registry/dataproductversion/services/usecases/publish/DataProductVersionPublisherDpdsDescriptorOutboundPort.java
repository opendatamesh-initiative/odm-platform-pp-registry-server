package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.publish;

import com.fasterxml.jackson.databind.JsonNode;
import org.opendatamesh.dpds.model.DataProductVersion;
import org.opendatamesh.dpds.parser.Parser;
import org.opendatamesh.dpds.parser.ParserFactory;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.springframework.util.StringUtils;

import java.io.IOException;

class DataProductVersionPublisherDpdsDescriptorOutboundPort implements DataProductVersionPublisherDescriptorOutboundPort {

    private final Parser parser;
    private final SemanticVersionValidator semverValidator = new SemanticVersionValidator();

    DataProductVersionPublisherDpdsDescriptorOutboundPort() {
        this.parser = ParserFactory.getParser();
    }

    @Override
    public void validateDescriptor(JsonNode descriptorContent) {
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        
        // Parse JsonNode to DataProductVersion
        DataProductVersion dataProductVersion;
        try {
            dataProductVersion = parser.deserialize(descriptorContent);
        } catch (IOException e) {
            throw new BadRequestException("Failed to parse DPDS descriptor: " + e.getMessage(), e);
        }

        if (dataProductVersion == null) {
            throw new BadRequestException("Descriptor root is null");
        }

        // Validate root-level required field: dataProductDescriptor
        String dataProductDescriptor = dataProductVersion.getDataProductDescriptor();
        if (!StringUtils.hasText(dataProductDescriptor)) {
            context.addError("dataProductDescriptor", "Required field is missing or empty");
        } else if (!semverValidator.isValid(dataProductDescriptor)) {
            context.addError("dataProductDescriptor", String.format("Version '%s' does not follow semantic versioning specification (MAJOR.MINOR.PATCH[-PRERELEASE][+BUILD])", dataProductDescriptor));
        }

        // Validate required top-level fields exist
        if (dataProductVersion.getInfo() == null) {
            context.addError("info", "Required field is missing");
        }

        if (dataProductVersion.getInterfaceComponents() == null) {
            context.addError("interfaceComponents", "Required field is missing");
        }

        // Visit child components if they exist
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        
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

        context.throwIfHasErrors();
    }

    @Override
    public String extractVersionNumber(JsonNode descriptorContent) {
        // Parse JsonNode to DataProductVersion
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
}
