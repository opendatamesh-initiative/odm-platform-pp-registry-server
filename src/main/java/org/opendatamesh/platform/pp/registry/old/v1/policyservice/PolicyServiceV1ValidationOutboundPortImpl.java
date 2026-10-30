package org.opendatamesh.platform.pp.registry.old.v1.policyservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.opendatamesh.dpds.exceptions.ParseException;
import org.opendatamesh.dpds.location.DescriptorLocation;
import org.opendatamesh.dpds.location.UriLocation;
import org.opendatamesh.dpds.model.DataProductVersionDPDS;
import org.opendatamesh.dpds.model.info.InfoDPDS;
import org.opendatamesh.dpds.parser.DPDSParser;
import org.opendatamesh.dpds.parser.IdentifierStrategy;
import org.opendatamesh.dpds.parser.IdentifierStrategyFactory;
import org.opendatamesh.dpds.parser.ParseOptions;
import org.opendatamesh.dpds.parser.ParseResult;
import org.opendatamesh.dpds.utils.ObjectMapperFactory;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.old.v1.policyservice.NotificationEventHandlerDpvPublicationRequested.RegistryV1DataProductVersionEventState;
import org.opendatamesh.platform.pp.registry.old.v1.registryservice.RegistryV1DataProductResource;
import org.opendatamesh.platform.pp.registry.old.v1.registryservice.RegistryV1DataProductValidationResult;
import org.opendatamesh.platform.pp.registry.old.v1.registryservice.RegistryV1PolicyValidationOutboundPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Fulfils the Registry V1 policy validations through the Policy Service V1:
 * builds the V1 evaluation requests (event format, descriptor parsing) and maps the V1 results back.
 * Results are never stored, because validation is a dry run.
 */
class PolicyServiceV1ValidationOutboundPortImpl implements RegistryV1PolicyValidationOutboundPort {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final PolicyClientV1 policyClient;
    private final IdentifierStrategy identifierStrategy;
    private final String descriptorParserVersion;

    PolicyServiceV1ValidationOutboundPortImpl(PolicyClientV1 policyClient, IdentifierStrategy identifierStrategy, String descriptorParserVersion) {
        this.policyClient = policyClient;
        this.identifierStrategy = identifierStrategy;
        this.descriptorParserVersion = descriptorParserVersion;
    }

    @Override
    public Map<String, RegistryV1DataProductValidationResult> validateDataProductCreation(RegistryV1DataProductResource dataProduct) {
        PolicyResPolicyEvaluationRequest evaluationRequest = new PolicyResPolicyEvaluationRequest();
        evaluationRequest.setResourceType(PolicyResPolicyEvaluationRequest.ResourceType.DATA_PRODUCT_DESCRIPTOR);
        evaluationRequest.setAfterState(dataProductAsEventState(dataProduct));
        evaluationRequest.setDataProductId(dataProduct.getId());
        evaluationRequest.setEvent(PolicyResPolicyEvaluationRequest.EventType.DATA_PRODUCT_CREATION);
        return evaluate(evaluationRequest);
    }

    @Override
    public Map<String, RegistryV1DataProductValidationResult> validateDataProductUpdate(RegistryV1DataProductResource currentDataProduct,
                                                                                        RegistryV1DataProductResource updatedDataProduct) {
        PolicyResPolicyEvaluationRequest evaluationRequest = new PolicyResPolicyEvaluationRequest();
        evaluationRequest.setResourceType(PolicyResPolicyEvaluationRequest.ResourceType.DATA_PRODUCT_DESCRIPTOR);
        evaluationRequest.setCurrentState(dataProductAsEventState(currentDataProduct));
        evaluationRequest.setAfterState(dataProductAsEventState(updatedDataProduct));
        evaluationRequest.setDataProductId(currentDataProduct.getId());
        evaluationRequest.setEvent(PolicyResPolicyEvaluationRequest.EventType.DATA_PRODUCT_UPDATE);
        return evaluate(evaluationRequest);
    }

    @Override
    public Map<String, RegistryV1DataProductValidationResult> validateDataProductVersionCreation(JsonNode currentDescriptor,
                                                                                                 JsonNode newDescriptor) {
        JsonNode afterState = descriptorAsEventState(newDescriptor);
        PolicyResPolicyEvaluationRequest evaluationRequest = new PolicyResPolicyEvaluationRequest();
        evaluationRequest.setResourceType(PolicyResPolicyEvaluationRequest.ResourceType.DATA_PRODUCT_DESCRIPTOR);
        evaluationRequest.setAfterState(afterState);
        setIdAndVersionNumberFromEventState(afterState, evaluationRequest);
        evaluationRequest.setEvent(PolicyResPolicyEvaluationRequest.EventType.DATA_PRODUCT_VERSION_CREATION);
        if (currentDescriptor != null) {
            evaluationRequest.setCurrentState(descriptorAsEventState(currentDescriptor));
        }
        return evaluate(evaluationRequest);
    }

    private Map<String, RegistryV1DataProductValidationResult> evaluate(PolicyResPolicyEvaluationRequest evaluationRequest) {
        PolicyResValidationResponse validationResponse = policyClient.validateInput(evaluationRequest, false);
        if (validationResponse == null || validationResponse.getPolicyResults() == null) {
            return Collections.emptyMap();
        }
        return toValidationResults(validationResponse);
    }

    /*
     * Builds the event state { "dataProductVersion": { ... } } expected by Policy Service V1 consumers,
     * the same format used during publication (see NotificationEventHandlerDpvPublicationRequested).
     * With descriptor parser version 1 the content is the output of the old 1.x parser, to keep existing policies working.
     */
    private JsonNode descriptorAsEventState(JsonNode descriptor) {
        if (descriptorParserVersion.matches("^1(\\\\..+){0,2}$")) {
            log.info("Using old descriptor parser to parse Data Product Version content.");
            try {
                return toJsonNode(new RegistryV1DataProductVersionEventState(parseDescriptorWithOldParser(descriptor)));
            } catch (Exception e) {
                log.warn("Error when parsing descriptor using old parser: {}, returning the unmodified descriptor.", e.getMessage(), e);
            }
        }
        ObjectNode state = ObjectMapperFactory.JSON_MAPPER.createObjectNode();
        state.set("dataProductVersion", descriptor.isNull() ? descriptor : descriptor.deepCopy());
        return state;
    }

    private JsonNode dataProductAsEventState(RegistryV1DataProductResource dataProduct) {
        InfoDPDS info = new InfoDPDS();
        info.setDomain(dataProduct.getDomain());
        info.setFullyQualifiedName(dataProduct.getFullyQualifiedName());
        info.setDescription(dataProduct.getDescription());
        DataProductVersionDPDS dataProductVersion = new DataProductVersionDPDS();
        dataProductVersion.setInfo(info);
        return toJsonNode(new RegistryV1DataProductVersionEventState(dataProductVersion));
    }

    private void setIdAndVersionNumberFromEventState(JsonNode eventState, PolicyResPolicyEvaluationRequest evaluationRequest) {
        JsonNode info = eventState.path("dataProductVersion").get("info");
        if (info != null && !info.isNull()) {
            String dataProductId = info.has("id") && !info.get("id").isNull() && info.get("id").asText() != null && !info.get("id").asText().isEmpty()
                    ? info.get("id").asText()
                    : (info.has("fullyQualifiedName") && !info.get("fullyQualifiedName").isNull() ? identifierStrategy.getId(info.get("fullyQualifiedName").asText()) : null);
            String versionNumber = info.has("version") && !info.get("version").isNull()
                    ? info.get("version").asText()
                    : (info.has("versionNumber") && !info.get("versionNumber").isNull() ? info.get("versionNumber").asText() : null);
            evaluationRequest.setDataProductId(dataProductId);
            evaluationRequest.setDataProductVersion(versionNumber);
        }
    }

    private Map<String, RegistryV1DataProductValidationResult> toValidationResults(PolicyResValidationResponse validationResponse) {
        return validationResponse.getPolicyResults().stream()
                .map(policyEvaluationResult -> new AbstractMap.SimpleEntry<>(
                        policyEvaluationResult.getPolicy().getName(),
                        new RegistryV1DataProductValidationResult(
                                policyEvaluationResult.getPolicy().getBlockingFlag(),
                                outputAsJsonWhenPossible(policyEvaluationResult.getOutputObject()),
                                policyEvaluationResult.getResult()
                        )
                ))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Object outputAsJsonWhenPossible(String outputObject) {
        if (outputObject == null) {
            return null;
        }
        try {
            return new ObjectMapper().readTree(outputObject);
        } catch (JsonProcessingException e) {
            return outputObject;
        }
    }

    private DataProductVersionDPDS parseDescriptorWithOldParser(JsonNode descriptorJson) {
        try {
            String descriptorContent = new ObjectMapper().writeValueAsString(descriptorJson);
            DPDSParser descriptorParser = new DPDSParser(
                    "https://raw.githubusercontent.com/opendatamesh-initiative/odm-specification-dpdescriptor/main/schemas/",
                    "1.0.0",
                    "1.0.0"
            );
            DescriptorLocation location = new UriLocation(descriptorContent);
            ParseOptions options = new ParseOptions();
            options.setIdentifierStrategy(IdentifierStrategyFactory.getDefault());
            ParseResult result = descriptorParser.parse(location, options);
            return result.getDescriptorDocument();
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to convert descriptor JSON to string", e);
        } catch (ParseException e) {
            throw new IllegalStateException("Failed to parse data product version descriptor", e);
        }
    }

    private JsonNode toJsonNode(Object object) {
        try {
            return ObjectMapperFactory.JSON_MAPPER.readTree(ObjectMapperFactory.JSON_MAPPER.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            throw new BadRequestException(e.getMessage(), e);
        }
    }
}
