package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.descriptorvalidator;

import com.fasterxml.jackson.databind.JsonNode;
import org.opendatamesh.dpds.model.DataProductVersion;
import org.opendatamesh.dpds.parser.Parser;
import org.opendatamesh.dpds.parser.ParserFactory;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * Validates DPDS descriptor content. Does not mutate the descriptor.
 */
class DpdsDescriptorValidator implements DescriptorValidator {

    private final Parser parser = ParserFactory.getParser();

    @Override
    public void validateDescriptor(JsonNode descriptorContent, String expectedDataProductFqn) {
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();

        DataProductVersion dataProductVersion;
        try {
            dataProductVersion = parser.deserialize(descriptorContent);
        } catch (IOException e) {
            throw new BadRequestException("Failed to parse DPDS descriptor: " + e.getMessage(), e);
        }

        if (dataProductVersion == null) {
            throw new BadRequestException("Descriptor root is null");
        }

        // First step: when expected FQN is provided, ensure descriptor info.fullyQualifiedName matches (only when info is present; missing info is reported later)
        if (expectedDataProductFqn != null && dataProductVersion.getInfo() != null) {
            String descriptorFqn = dataProductVersion.getInfo().getFullyQualifiedName();
            if (!expectedDataProductFqn.equals(descriptorFqn)) {
                context.addError("info.fullyQualifiedName",
                        "Descriptor info.fullyQualifiedName must match the data product FQN: expected '" + expectedDataProductFqn + "'.");
            }
        }

        String dataProductDescriptor = dataProductVersion.getDataProductDescriptor();
        SemanticVersionValidator semverValidator = new SemanticVersionValidator();
        if (!StringUtils.hasText(dataProductDescriptor)) {
            context.addError("dataProductDescriptor", "Required field is missing or empty");
        } else if (!semverValidator.isValid(dataProductDescriptor)) {
            context.addError("dataProductDescriptor",
                    String.format("Version '%s' does not follow semantic versioning specification (MAJOR.MINOR.PATCH[-PRERELEASE][+BUILD])", dataProductDescriptor));
        }

        if (dataProductVersion.getInfo() == null) {
            context.addError("info", "Required field is missing");
        }
        if (dataProductVersion.getInterfaceComponents() == null) {
            context.addError("interfaceComponents", "Required field is missing");
        }

        DpdsValidationVisitor visitor = new DpdsValidationVisitor(context);
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
}
