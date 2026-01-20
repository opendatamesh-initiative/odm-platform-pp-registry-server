package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.resolvevariables;

import java.util.List;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DescriptorSpec;
import org.opendatamesh.platform.pp.registry.descriptorvariable.entities.DescriptorVariable;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DataProductVersionVariablesResolverDescriptorOutboundPortImpl implements DataProductVersionVariablesResolverDescriptorOutboundPort {
    private static final Logger logger = LoggerFactory.getLogger(DataProductVersionVariablesResolverDescriptorOutboundPortImpl.class);
    private static final String SUPPORTED_SPECIFICATION = DescriptorSpec.DPDS.name();
    private static final String SUPPORTED_SPECIFICATION_VERSION = "1.*.*";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public JsonNode resolveDescriptor(DataProductVersion dataProductVersion, List<DescriptorVariable> descriptorVariables) {
        if (SUPPORTED_SPECIFICATION.equalsIgnoreCase(dataProductVersion.getSpec()) && dataProductVersion.getSpecVersion().matches(SUPPORTED_SPECIFICATION_VERSION)) {
            return resolveOdmDescriptor(dataProductVersion.getContent(), descriptorVariables);
        } else {
            throw new BadRequestException("[DataProductVersion Variable Resolver] Unsupported specification " + dataProductVersion.getSpec() + " version " + dataProductVersion.getSpecVersion());
        }
    }

    private JsonNode resolveOdmDescriptor(JsonNode content, List<DescriptorVariable> descriptorVariables) {
        try {
            String descriptorRawContent = objectMapper.writeValueAsString(content);

            for (DescriptorVariable variable : descriptorVariables) {
                if (variable.getVariableValue() != null) {
                    String sanitizedValue = sanitizeJsonStringValue(variable.getVariableValue());
                    descriptorRawContent = descriptorRawContent.replace(
                            "${" + variable.getVariableKey() + "}",
                            sanitizedValue
                    );
                    logger.info("[DataProductVersion Variable Resolver] Replaced variable: {}", variable.getVariableKey());
                }
            }

            // Parse back to JsonNode
            return objectMapper.readTree(descriptorRawContent);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("[DataProductVersion Variable Resolver] Failed to resolve variables in descriptor", e);
        }
    }

    private String sanitizeJsonStringValue(String value) {
        // Use Jackson's JsonStringEncoder to properly escape the string value for JSON
        // This escapes quotes, backslashes, control characters, etc. according to JSON spec
        JsonStringEncoder encoder = JsonStringEncoder.getInstance();
        char[] escapedChars = encoder.quoteAsString(value);
        return new String(escapedChars);
    }
}
