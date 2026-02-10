package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.publish;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opendatamesh.dpds.model.DataProductVersion;
import org.opendatamesh.dpds.parser.Parser;
import org.opendatamesh.dpds.parser.ParserFactory;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DescriptorSpec;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.descriptorvalidator.DescriptorValidator;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.descriptorvalidator.DescriptorValidatorFactory;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.springframework.util.StringUtils;

import java.io.IOException;

class DataProductVersionPublisherDpdsDescriptorOutboundPort implements DataProductVersionPublisherDescriptorOutboundPort {

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
    public JsonNode enrichDescriptorContentIfNeeded(String descriptorSpec, String descriptorSpecVersion, JsonNode descriptorContent) {
        if (!StringUtils.hasText(descriptorSpec) || !StringUtils.hasText(descriptorSpecVersion)) {
            return descriptorContent;
        }
        if (!descriptorSpec.equalsIgnoreCase(DescriptorSpec.DPDS.name()) || !descriptorSpecVersion.matches("1\\..*")) {
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
    public String extractVersionNumber(JsonNode descriptorContent) {
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
