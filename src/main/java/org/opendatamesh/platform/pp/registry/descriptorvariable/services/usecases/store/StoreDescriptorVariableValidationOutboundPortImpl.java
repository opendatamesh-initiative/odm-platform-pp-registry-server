package org.opendatamesh.platform.pp.registry.descriptorvariable.services.usecases.store;

import com.fasterxml.jackson.databind.JsonNode;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DescriptorSpec;
import org.opendatamesh.platform.pp.registry.descriptorvariable.entities.DescriptorVariable;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.List;

class StoreDescriptorVariableValidationOutboundPortImpl implements StoreDescriptorVariableValidationOutboundPort {

    private static final String SUPPORTED_SPECIFICATION = DescriptorSpec.DPDS.name();
    private static final String SUPPORTED_SPECIFICATION_VERSION = "1.*.*";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    StoreDescriptorVariableValidationOutboundPortImpl() {
    }

    @Override
    public void validateVariablesCanBeAppliedToDescriptor(DataProductVersion dataProductVersion, List<DescriptorVariable> descriptorVariables) {
        if (SUPPORTED_SPECIFICATION.equalsIgnoreCase(dataProductVersion.getSpec()) && dataProductVersion.getSpecVersion().matches(SUPPORTED_SPECIFICATION_VERSION)) {
            validateVariablesCanBeAppliedToOdmDescriptor(dataProductVersion.getContent(), descriptorVariables);
        } else {
            logger.warn("[Store Descriptor Variable use case] Unable to verify variable presence in descriptor." +
                    " Unsupported specification: {}", dataProductVersion.getSpec());
        }
    }

    private void validateVariablesCanBeAppliedToOdmDescriptor(JsonNode descriptor, List<DescriptorVariable> variables) {
        String descriptorString = descriptor.toString();
        for (DescriptorVariable variable : variables) {
            String variableKey = variable.getVariableKey();
            if (!StringUtils.hasText(variableKey)) {
                throw new BadRequestException("DescriptorVariable is missing variable key");
            }

            String variablePattern = "${" + variableKey + "}";
            if (!descriptorString.contains(variablePattern)) {
                throw new BadRequestException(
                        String.format("Variable key '%s' not found in descriptor. Expected pattern: %s",
                                variableKey, variablePattern));
            }
        }
    }
}
