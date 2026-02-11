package org.opendatamesh.platform.pp.registry.old.v1.registryservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opendatamesh.dpds.model.DataProductVersionDPDS;
import org.opendatamesh.dpds.model.info.InfoDPDS;
import org.opendatamesh.dpds.parser.IdentifierStrategy;
import org.opendatamesh.dpds.parser.ParserFactory;
import org.opendatamesh.dpds.utils.ObjectMapperFactory;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.dataproduct.services.core.DataProductsService;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersionShort;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DescriptorSpec;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.core.DataProductVersionCrudService;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.core.DataProductVersionsQueryService;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.descriptorvalidator.DescriptorValidator;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.descriptorvalidator.DescriptorValidatorFactory;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.exceptions.NotFoundException;
import org.opendatamesh.platform.pp.registry.old.v1.policyservice.PolicyClientV1;
import org.opendatamesh.platform.pp.registry.old.v1.policyservice.PolicyResPolicyEvaluationRequest;
import org.opendatamesh.platform.pp.registry.old.v1.policyservice.PolicyResValidationResponse;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductSearchOptions;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionSearchOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
class RegistryV1ValidateService {

    @Autowired
    private DescriptorValidatorFactory descriptorValidatorFactory;
    @Autowired
    private RegistryV1Service registryV1Service;
    @Autowired
    private DataProductsService dataProductsService;
    @Autowired
    private DataProductVersionsQueryService dataProductVersionsQueryService;
    @Autowired
    private DataProductVersionCrudService dataProductVersionCrudService;
    @Autowired(required = false)
    private PolicyClientV1 policyClient;
    @Autowired
    private RegistryV1EventTypeBaseMapper eventTypeBaseMapper;
    @Autowired
    private IdentifierStrategy identifierStrategy;

    @Value("${odm.descriptor.parser.version:1}")
    private String descriptorParserVersion;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public RegistryV1DataProductValidationResponseResource validateReport(RegistryV1DataProductValidationRequestResource request) {
        JsonNode descriptorNode = request.getDataProductVersion();
        if (descriptorNode == null || descriptorNode.isNull()) {
            throw new BadRequestException("dataProductVersion is required");
        }

        RegistryV1DataProductValidationResponseResource response = new RegistryV1DataProductValidationResponseResource();

        if (Boolean.TRUE.equals(request.getValidateSyntax())) {
            RegistryV1DataProductValidationResult syntaxResult = runSyntaxValidation(descriptorNode);
            response.setSyntaxValidationResult(syntaxResult);
        }

        if (Boolean.TRUE.equals(request.getValidatePolicies())) {
            Map<String, RegistryV1DataProductValidationResult> policyResults = runPolicyValidation(request);
            response.setPoliciesValidationResults(policyResults);
        }

        return response;
    }

    private RegistryV1DataProductValidationResult runSyntaxValidation(JsonNode descriptorNode) {
        RegistryV1DataProductValidationResult result = new RegistryV1DataProductValidationResult();
        try {
            //Forcing the specification and its version, because only used in legacy environment
            DescriptorValidator validator = descriptorValidatorFactory.getDescriptorValidator(DescriptorSpec.DPDS.name(), "1.0.0");
            validator.validateDescriptor(descriptorNode);
            result.setValidated(true);
            result.setValidationOutput(null);
        } catch (BadRequestException e) {
            result.setValidated(false);
            result.setValidationOutput(e.getMessage());
            result.setBlockingFlag(true);
        }
        return result;
    }

    private Map<String, RegistryV1DataProductValidationResult> runPolicyValidation(RegistryV1DataProductValidationRequestResource request) {
        if (policyClient == null) {
            log.warn("Policy Service is not activated on Registry Service");
            return Map.of("PolicyServiceNotActive", new RegistryV1DataProductValidationResult(true, "Policy Service is not activated on Registry Service", false));
        }

        RegistryV1DataProductResource registryV1DataProduct = extractRegistryV1DataProductFromDescriptor(request);
        Map<String, RegistryV1DataProductValidationResult> dataProductValidationResults = validateDataProductPolicies(registryV1DataProduct, request.getPolicyEventTypes());
        Map<String, RegistryV1DataProductValidationResult> dataProductVersionValidationResults = validateDataProductVersionPolicies(registryV1DataProduct, request);

        return Stream.of(dataProductValidationResults, dataProductVersionValidationResults)
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, this::mergeValidationResults));
    }


    private Map<String, RegistryV1DataProductValidationResult> validateDataProductVersionPolicies(RegistryV1DataProductResource registryV1DataProduct, RegistryV1DataProductValidationRequestResource request) {
        if (!request.getPolicyEventTypes().isEmpty() && !request.getPolicyEventTypes().contains(PolicyResPolicyEvaluationRequest.EventType.DATA_PRODUCT_VERSION_CREATION.name())) {
            return Collections.emptyMap();
        }

        Optional<DataProductVersionShort> mostRecentDataProductVersion = getMostRecentDataProductVersion(registryV1DataProduct);
        JsonNode newDpds = request.getDataProductVersion();
        if (descriptorParserVersion.matches("^1(\\\\..+){0,2}$")) {
            log.info("Using old descriptor parser to parse Data Product Version content.");
            try {
                newDpds = toJsonNode(eventTypeBaseMapper.toEventResource(registryV1Service.parseDescriptorWithOldParser(request.getDataProductVersion())));
            } catch (Exception e) {
                log.warn("Error when parsing descriptor using old parser: {}, returning the unmodified descriptor.", e.getMessage(), e);
            }
        }
        if (mostRecentDataProductVersion.isEmpty()) {
            PolicyResPolicyEvaluationRequest evaluationRequest = buildDataProductVersionRequest(null, newDpds);
            PolicyResValidationResponse validationResponseResource = policyClient.validateInput(evaluationRequest, false);
            if (validationResponseResource != null && validationResponseResource.getPolicyResults() != null) {
                return policyValidationResponseToDataProductValidationResult(validationResponseResource);
            }
            return Collections.emptyMap();
        } else {
            org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion modstRecentDpv = dataProductVersionCrudService.findOne(mostRecentDataProductVersion.get().getUuid());
            JsonNode oldDpds = modstRecentDpv.getContent();
            if (descriptorParserVersion.matches("^1(\\\\..+){0,2}$")) {
                log.info("Using old descriptor parser to parse Data Product Version content.");
                try {
                    oldDpds = toJsonNode(eventTypeBaseMapper.toEventResource(registryV1Service.parseDescriptorWithOldParser(modstRecentDpv.getContent())));
                } catch (Exception e) {
                    log.warn("Error when parsing descriptor using old parser: {}, returning the unmodified descriptor.", e.getMessage(), e);
                }
            }
            PolicyResPolicyEvaluationRequest evaluationRequest = buildDataProductVersionRequest(oldDpds, newDpds);
            PolicyResValidationResponse validationResponseResource = policyClient.validateInput(evaluationRequest, false);
            if (validationResponseResource != null && validationResponseResource.getPolicyResults() != null) {
                return policyValidationResponseToDataProductValidationResult(validationResponseResource);
            }
            return Collections.emptyMap();
        }
    }

    private Optional<DataProductVersionShort> getMostRecentDataProductVersion(RegistryV1DataProductResource registryV1DataProduct) {
        DataProductSearchOptions dataProductSearchOptions = new DataProductSearchOptions();
        dataProductSearchOptions.setFqn(registryV1DataProduct.getFullyQualifiedName());
        Optional<DataProduct> dataProduct = dataProductsService.findAllFiltered(Pageable.ofSize(1), dataProductSearchOptions).stream().findFirst();
        if (dataProduct.isEmpty()) {
            return Optional.empty();
        }
        DataProductVersionSearchOptions dataProductVersionSearchOptions = new DataProductVersionSearchOptions();
        dataProductVersionSearchOptions.setDataProductUuid(dataProduct.get().getUuid());
        Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdAt"));
        return dataProductVersionsQueryService.findAllShort(pageable, dataProductVersionSearchOptions).stream().findFirst();
    }

    private RegistryV1DataProductResource extractRegistryV1DataProductFromDescriptor(RegistryV1DataProductValidationRequestResource request) {
        org.opendatamesh.dpds.model.DataProductVersion descriptor = null;
        try {
            descriptor = ParserFactory.getParser().deserialize(request.getDataProductVersion());
        } catch (IOException e) {
            throw new BadRequestException(e.getMessage(), e);
        }
        RegistryV1DataProductResource registryV1DataProduct = new RegistryV1DataProductResource();
        registryV1DataProduct.setFullyQualifiedName(descriptor.getInfo().getFullyQualifiedName());
        registryV1DataProduct.setDomain(descriptor.getInfo().getDomain());
        registryV1DataProduct.setDescription(descriptor.getInfo().getDescription());
        registryV1DataProduct.setId(descriptor.getInfo().getId() != null ? descriptor.getInfo().getId() : identifierStrategy.getId(descriptor.getInfo().getFullyQualifiedName()));
        return registryV1DataProduct;
    }

    private Map<String, RegistryV1DataProductValidationResult> validateDataProductPolicies(RegistryV1DataProductResource dataProduct, List<String> policyEventTypes) {
        try {
            RegistryV1DataProductResource existentDataProduct = registryV1Service.getDataProduct(dataProduct.getId());
            if (policyEventTypes.isEmpty() || policyEventTypes.contains(PolicyResPolicyEvaluationRequest.EventType.DATA_PRODUCT_UPDATE.name())) {
                PolicyResPolicyEvaluationRequest dataProductUpdateValidationRequest = buildDataProductUpdateRequest(existentDataProduct, dataProduct);
                PolicyResValidationResponse validationResponse = policyClient.validateInput(dataProductUpdateValidationRequest, false);
                if (validationResponse != null && validationResponse.getPolicyResults() != null) {
                    return policyValidationResponseToDataProductValidationResult(validationResponse);
                }
            }
        } catch (NotFoundException e) {
            if (policyEventTypes.isEmpty() || policyEventTypes.contains(PolicyResPolicyEvaluationRequest.EventType.DATA_PRODUCT_CREATION.name())) {
                PolicyResPolicyEvaluationRequest dataProductCreationValidationRequest = buildDataProductRequest(dataProduct);
                PolicyResValidationResponse validationResponse = policyClient.validateInput(dataProductCreationValidationRequest, false);
                if (validationResponse != null && validationResponse.getPolicyResults() != null) {
                    return policyValidationResponseToDataProductValidationResult(validationResponse);
                }
            }
        }
        return Collections.emptyMap();
    }


    private PolicyResPolicyEvaluationRequest buildDataProductVersionRequest(JsonNode oldDpds, JsonNode newDpds) {
        PolicyResPolicyEvaluationRequest evaluationRequest = new PolicyResPolicyEvaluationRequest();
        evaluationRequest.setResourceType(PolicyResPolicyEvaluationRequest.ResourceType.DATA_PRODUCT_DESCRIPTOR);
        evaluationRequest.setAfterState(newDpds);
        setIdAndVersionNumberFromRawDescriptor(newDpds, evaluationRequest);
        evaluationRequest.setEvent(PolicyResPolicyEvaluationRequest.EventType.DATA_PRODUCT_VERSION_CREATION);
        if (oldDpds != null) {
            evaluationRequest.setCurrentState(oldDpds);
        }
        return evaluationRequest;
    }

    private void setIdAndVersionNumberFromRawDescriptor(JsonNode newDpds, PolicyResPolicyEvaluationRequest evaluationRequest) {
        JsonNode info = newDpds.has("dataProductVersion") ? newDpds.get("dataProductVersion").get("info") : newDpds.get("info");
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

    private PolicyResPolicyEvaluationRequest buildDataProductUpdateRequest(RegistryV1DataProductResource oldDataProduct, RegistryV1DataProductResource newDataProduct) {
        PolicyResPolicyEvaluationRequest evaluationRequest = new PolicyResPolicyEvaluationRequest();
        evaluationRequest.setResourceType(PolicyResPolicyEvaluationRequest.ResourceType.DATA_PRODUCT_DESCRIPTOR);
        DataProductVersionDPDS oldDpdsHead = descriptorFromDataProduct(oldDataProduct);
        DataProductVersionDPDS newDpdsHead = descriptorFromDataProduct(newDataProduct);
        evaluationRequest.setCurrentState(toJsonNode(eventTypeBaseMapper.toEventResource(oldDpdsHead)));
        evaluationRequest.setAfterState(toJsonNode(eventTypeBaseMapper.toEventResource(newDpdsHead)));
        evaluationRequest.setDataProductId(oldDataProduct.getId());
        evaluationRequest.setEvent(PolicyResPolicyEvaluationRequest.EventType.DATA_PRODUCT_UPDATE);
        return evaluationRequest;
    }

    private PolicyResPolicyEvaluationRequest buildDataProductRequest(RegistryV1DataProductResource dataProduct) {
        PolicyResPolicyEvaluationRequest evaluationRequest = new PolicyResPolicyEvaluationRequest();
        evaluationRequest.setResourceType(PolicyResPolicyEvaluationRequest.ResourceType.DATA_PRODUCT_DESCRIPTOR);
        DataProductVersionDPDS dpdsHead = descriptorFromDataProduct(dataProduct);
        evaluationRequest.setAfterState(toJsonNode(eventTypeBaseMapper.toEventResource(dpdsHead)));
        evaluationRequest.setDataProductId(dataProduct.getId());
        evaluationRequest.setEvent(PolicyResPolicyEvaluationRequest.EventType.DATA_PRODUCT_CREATION);
        return evaluationRequest;
    }


    private DataProductVersionDPDS descriptorFromDataProduct(RegistryV1DataProductResource dataProduct) {
        DataProductVersionDPDS dpdsHead = new DataProductVersionDPDS();
        InfoDPDS dpdsHeadInfo = new InfoDPDS();
        dpdsHeadInfo.setDomain(dataProduct.getDomain());
        dpdsHeadInfo.setFullyQualifiedName(dataProduct.getFullyQualifiedName());
        dpdsHeadInfo.setDescription(dataProduct.getDescription());
        dpdsHead.setInfo(dpdsHeadInfo);
        return dpdsHead;
    }

    private JsonNode toJsonNode(Object object) {
        try {
            return ObjectMapperFactory.JSON_MAPPER.readTree(ObjectMapperFactory.JSON_MAPPER.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            throw new BadRequestException(e.getMessage(), e);
        }
    }

    private Map<String, RegistryV1DataProductValidationResult> policyValidationResponseToDataProductValidationResult(
            PolicyResValidationResponse validationResponseResource) {
        return validationResponseResource.getPolicyResults().stream()
                .map(policyEvaluationResult -> {
                    try {
                        return new AbstractMap.SimpleEntry<>(
                                policyEvaluationResult.getPolicy().getName(),
                                new RegistryV1DataProductValidationResult(
                                        policyEvaluationResult.getPolicy().getBlockingFlag(),
                                        policyEvaluationResult.getOutputObject() != null ? new ObjectMapper().readTree(policyEvaluationResult.getOutputObject()) : null,
                                        policyEvaluationResult.getResult()

                                )
                        );
                    } catch (JsonProcessingException e) {
                        return new AbstractMap.SimpleEntry<>(
                                policyEvaluationResult.getPolicy().getName(),
                                new RegistryV1DataProductValidationResult(
                                        policyEvaluationResult.getPolicy().getBlockingFlag(),
                                        policyEvaluationResult.getOutputObject(),
                                        policyEvaluationResult.getResult()

                                )
                        );
                    }
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Merges two validation results for the same policy: if either is failed, the result is failed
     * with merged output; otherwise passed with merged output. Blocking if either is blocking.
     */
    private RegistryV1DataProductValidationResult mergeValidationResults(
            RegistryV1DataProductValidationResult a, RegistryV1DataProductValidationResult b) {
        boolean validated = a.isValidated() && b.isValidated();
        Boolean blockingFlag;
        if (Boolean.TRUE.equals(a.getBlockingFlag()) || Boolean.TRUE.equals(b.getBlockingFlag())) {
            blockingFlag = Boolean.TRUE;
        } else {
            blockingFlag = a.getBlockingFlag() != null ? a.getBlockingFlag() : b.getBlockingFlag();
        }
        Object mergedOutput = mergeValidationOutput(a.getValidationOutput(), b.getValidationOutput());
        return new RegistryV1DataProductValidationResult(blockingFlag, mergedOutput, validated);
    }

    private Object mergeValidationOutput(Object output1, Object output2) {
        if (output1 == null && output2 == null) {
            return null;
        }
        if (output1 == null) {
            return output2;
        }
        if (output2 == null) {
            return output1;
        }
        String s1 = output1.toString();
        String s2 = output2.toString();
        if (s1.isEmpty()) {
            return s2.isEmpty() ? null : output2;
        }
        if (s2.isEmpty()) {
            return output1;
        }
        return s1 + "\n" + s2;
    }
}
