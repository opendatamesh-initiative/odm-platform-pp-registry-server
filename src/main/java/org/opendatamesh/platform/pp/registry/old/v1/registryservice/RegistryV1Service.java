package org.opendatamesh.platform.pp.registry.old.v1.registryservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opendatamesh.dpds.exceptions.ParseException;
import org.opendatamesh.dpds.location.DescriptorLocation;
import org.opendatamesh.dpds.location.UriLocation;
import org.opendatamesh.dpds.model.DataProductVersionDPDS;
import org.opendatamesh.dpds.parser.*;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.core.DataProductVersionCrudService;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.core.DataProductVersionsQueryService;
import org.opendatamesh.platform.pp.registry.descriptorvariable.services.DescriptorVariableUseCasesService;
import org.opendatamesh.platform.pp.registry.descriptorvariable.services.core.DescriptorVariableCrudService;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.exceptions.InternalException;
import org.opendatamesh.platform.pp.registry.exceptions.NotFoundException;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionSearchOptions;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.descriptorvariable.DescriptorVariableRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.descriptorvariable.DescriptorVariableSearchOptions;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.descriptorvariable.usecases.store.StoreDescriptorVariableCommandRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.descriptorvariable.usecases.store.StoreDescriptorVariableResultRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
class RegistryV1Service {
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    @Autowired
    private DataProductVersionsQueryService dpvQueryService;

    @Autowired
    private DataProductVersionCrudService dpvCrudService;

    @Autowired
    private DescriptorVariableCrudService descriptorVariableCrudService;

    @Autowired
    private DescriptorVariableUseCasesService descriptorVariableUseCasesService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getDataProductVersion(String id, String version, String format) {
        if (StringUtils.hasText(format) && !(format.equalsIgnoreCase("normalized") || format.equalsIgnoreCase("canonical"))) {
            throw new BadRequestException("Format [" + format + "] is not supported");
        }

        DataProductVersion dataProductVersion = findDataProductVersion(id, version);

        DataProductVersionDPDS dataProductVersionDPDS = parseDescriptorWithOldParser(dataProductVersion.getContent());

        String serializedContent = serializeOldDataProductVersionUsingOldParser(format, dataProductVersionDPDS);

        return replaceVariablesOnSerializedContent(serializedContent, dataProductVersion.getUuid());
    }

    public List<RegistryV1VariableResource> getVariables(String id, String version) {
        DataProductVersion dataProductVersion = findDataProductVersion(id, version);
        String versionUuid = dataProductVersion.getUuid();

        // Get existing variables
        DescriptorVariableSearchOptions searchOptions = new DescriptorVariableSearchOptions();
        searchOptions.setDataProductVersionUuid(versionUuid);

        Page<DescriptorVariableRes> variablesPage = descriptorVariableCrudService.findAllResourcesFiltered(
                Pageable.unpaged(), searchOptions);

        List<DescriptorVariableRes> allVariables = new ArrayList<>(variablesPage.getContent());
        Set<String> existingVariableKeys = allVariables.stream()
                .map(DescriptorVariableRes::getVariableKey)
                .collect(Collectors.toSet());

        // Extract variable placeholders from descriptor
        Set<String> descriptorVariableKeys = extractVariableKeysFromDescriptor(dataProductVersion.getContent());

        // Create missing variables with empty values
        List<DescriptorVariableRes> variablesToCreate = new ArrayList<>();
        for (String variableKey : descriptorVariableKeys) {
            if (!existingVariableKeys.contains(variableKey)) {
                DescriptorVariableRes newVariable = new DescriptorVariableRes();
                newVariable.setDataProductVersionUuid(versionUuid);
                newVariable.setVariableKey(variableKey);
                newVariable.setVariableValue("${" + variableKey + "}"); //INIT WITH KEY as VALUE
                variablesToCreate.add(newVariable);
            }
        }

        // Create missing variables if any
        if (!variablesToCreate.isEmpty()) {
            StoreDescriptorVariableCommandRes storeCommand = new StoreDescriptorVariableCommandRes();
            storeCommand.setDescriptorVariables(variablesToCreate);
            StoreDescriptorVariableResultRes result = descriptorVariableUseCasesService.storeDescriptorVariable(storeCommand);
            if (result.getDescriptorVariables() != null) {
                allVariables.addAll(result.getDescriptorVariables());
            }
        }

        // Return all variables (existing and newly created)
        return allVariables.stream()
                .map(this::toRegistryV1VariableResource)
                .toList();
    }

    public RegistryV1VariableResource updateVariable(String id, String version, Long variableId, String variableValue) {
        DataProductVersion dataProductVersion = findDataProductVersion(id, version);
        String versionUuid = dataProductVersion.getUuid();

        // Retrieve the existing variable
        DescriptorVariableRes existingVariable = descriptorVariableCrudService.findOneResource(variableId);

        // Verify that the variable belongs to the correct data product version
        if (!existingVariable.getDataProductVersionUuid().equals(versionUuid)) {
            throw new BadRequestException(
                    String.format("Variable with id %d does not belong to data product version %s", variableId, versionUuid));
        }

        // Update the variable value
        existingVariable.setVariableValue(variableValue);

        // Store the updated variable using the use case service
        StoreDescriptorVariableCommandRes storeCommand = new StoreDescriptorVariableCommandRes();
        List<DescriptorVariableRes> variables = new ArrayList<>();
        variables.add(existingVariable);
        storeCommand.setDescriptorVariables(variables);

        StoreDescriptorVariableResultRes result = descriptorVariableUseCasesService.storeDescriptorVariable(storeCommand);

        // Return the updated variable
        if (result.getDescriptorVariables() == null || result.getDescriptorVariables().isEmpty()) {
            throw new InternalException("Failed to update variable");
        }

        DescriptorVariableRes updatedVariable = result.getDescriptorVariables().get(0);
        return toRegistryV1VariableResource(updatedVariable);
    }

    private String replaceVariablesOnSerializedContent(String serializedContent, String versionUuid) {
        // Retrieve all variables for this data product version
        DescriptorVariableSearchOptions searchOptions = new DescriptorVariableSearchOptions();
        searchOptions.setDataProductVersionUuid(versionUuid);

        Page<DescriptorVariableRes> variablesPage = descriptorVariableCrudService.findAllResourcesFiltered(
                Pageable.unpaged(), searchOptions);

        List<DescriptorVariableRes> variables = variablesPage.getContent();

        // Replace each variable placeholder with its actual value
        String result = serializedContent;
        for (DescriptorVariableRes variable : variables) {
            if (variable.getVariableValue() != null && StringUtils.hasText(variable.getVariableKey())) {
                String sanitizedValue = sanitizeJsonStringValue(variable.getVariableValue());
                String placeholder = "${" + variable.getVariableKey() + "}";
                result = result.replace(placeholder, sanitizedValue);
            }
        }

        return result;
    }

    private String sanitizeJsonStringValue(String value) {
        // Use Jackson's JsonStringEncoder to properly escape the string value for JSON
        // This escapes quotes, backslashes, control characters, etc. according to JSON spec
        JsonStringEncoder encoder = JsonStringEncoder.getInstance();
        char[] escapedChars = encoder.quoteAsString(value);
        return new String(escapedChars);
    }

    private String serializeOldDataProductVersionUsingOldParser(String format, DataProductVersionDPDS dataProductVersionDPDS) {
        if (format == null) format = "canonical";
        String serializedContent = null;
        try {
            serializedContent = DPDSSerializer.DEFAULT_JSON_SERIALIZER.serialize(dataProductVersionDPDS, format);
        } catch (JsonProcessingException e) {
            throw new InternalException("Impossible to serialize data product version raw content", e);
        }
        return serializedContent;
    }

    private DataProductVersion findDataProductVersion(String id, String version) {
        DataProductVersionSearchOptions dataProductVersionSearchOptions = new DataProductVersionSearchOptions();
        dataProductVersionSearchOptions.setDataProductUuid(id);
        dataProductVersionSearchOptions.setVersionNumber(version);
        return dpvQueryService.findAllShort(Pageable.ofSize(1), dataProductVersionSearchOptions)
                .stream()
                .findFirst()
                .map(dpvShort -> dpvCrudService.findOne(dpvShort.getUuid()))
                .orElseThrow(() -> new NotFoundException("Data Product Version not found."));
    }

    private RegistryV1VariableResource toRegistryV1VariableResource(DescriptorVariableRes descriptorVariableRes) {
        RegistryV1VariableResource resource = new RegistryV1VariableResource();
        resource.setId(descriptorVariableRes.getSequenceId());
        resource.setVariableName(descriptorVariableRes.getVariableKey());
        resource.setVariableValue(descriptorVariableRes.getVariableValue());
        return resource;
    }

    private DataProductVersionDPDS parseDescriptorWithOldParser(JsonNode descriptorJson) {
        try {
            // Convert JsonNode to String
            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            String descriptorContent = mapper.writeValueAsString(descriptorJson);

            // Create DPDSParser
            DPDSParser descriptorParser = new DPDSParser(
                    "https://raw.githubusercontent.com/opendatamesh-initiative/odm-specification-dpdescriptor/main/schemas/",
                    "1.0.0",
                    "1.0.0"
            );

            // Create DescriptorLocation from content
            DescriptorLocation location = new UriLocation(descriptorContent);

            // Configure parse options
            ParseOptions options = new ParseOptions();
            options.setIdentifierStrategy(IdentifierStrategyFactory.getDefault());

            // Parse descriptor
            ParseResult result = descriptorParser.parse(location, options);
            return result.getDescriptorDocument();
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to convert descriptor JSON to string", e);
        } catch (ParseException e) {
            throw new IllegalStateException("Failed to parse data product version descriptor", e);
        }
    }

    /**
     * Extracts all variable keys from the descriptor by finding all ${variableName} patterns.
     *
     * @param descriptorJson The descriptor as JsonNode
     * @return A set of unique variable keys found in the descriptor
     */
    private Set<String> extractVariableKeysFromDescriptor(JsonNode descriptorJson) {
        Set<String> variableKeys = new HashSet<>();
        try {
            // Convert JsonNode to String to search for variable patterns
            String descriptorContent = objectMapper.writeValueAsString(descriptorJson);

            // Find all ${variableName} patterns
            Matcher matcher = VARIABLE_PATTERN.matcher(descriptorContent);
            while (matcher.find()) {
                String variableKey = matcher.group(1);
                if (StringUtils.hasText(variableKey)) {
                    variableKeys.add(variableKey);
                }
            }
        } catch (JsonProcessingException e) {
            throw new InternalException("Failed to extract variables from descriptor", e);
        }
        return variableKeys;
    }
}
