package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.descriptorvalidator;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DescriptorSpec;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class DescriptorValidatorFactory {

    public DescriptorValidator getDescriptorValidator(String descriptorSpec, String specVersion) {
        if (!StringUtils.hasText(descriptorSpec)) {
            throw new BadRequestException("Descriptor spec is required");
        }
        if (!StringUtils.hasText(specVersion)) {
            throw new BadRequestException("Descriptor spec version is required");
        }
        if (descriptorSpec.equalsIgnoreCase(DescriptorSpec.DPDS.name()) && specVersion.matches("1\\..*")) {
            return new DpdsDescriptorValidator();
        }
        throw new BadRequestException(
                String.format("Unsupported descriptor specification: %s version %s. Currently only DPDS 1.x is supported.", descriptorSpec, specVersion));
    }
}
