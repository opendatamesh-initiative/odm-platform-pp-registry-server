package org.opendatamesh.platform.pp.registry.old.v1.registryservice;

import com.fasterxml.jackson.databind.JsonNode;
import org.opendatamesh.dpds.parser.IdentifierStrategy;
import org.opendatamesh.dpds.parser.ParserFactory;
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
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductSearchOptions;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionSearchOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    // Values accepted in the policyEventTypes filter of the validation request
    private static final String DATA_PRODUCT_CREATION = "DATA_PRODUCT_CREATION";
    private static final String DATA_PRODUCT_UPDATE = "DATA_PRODUCT_UPDATE";
    private static final String DATA_PRODUCT_VERSION_CREATION = "DATA_PRODUCT_VERSION_CREATION";

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
    private RegistryV1PolicyValidationOutboundPort policyValidationOutboundPort;
    @Autowired
    private IdentifierStrategy identifierStrategy;

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
        if (policyValidationOutboundPort == null) {
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

    private Map<String, RegistryV1DataProductValidationResult> validateDataProductPolicies(RegistryV1DataProductResource dataProduct, List<String> policyEventTypes) {
        Optional<RegistryV1DataProductResource> existentDataProduct = findExistentDataProduct(dataProduct.getId());
        if (existentDataProduct.isPresent()) {
            return isPolicyEventTypeRequested(policyEventTypes, DATA_PRODUCT_UPDATE)
                    ? policyValidationOutboundPort.validateDataProductUpdate(existentDataProduct.get(), dataProduct)
                    : Collections.emptyMap();
        }
        return isPolicyEventTypeRequested(policyEventTypes, DATA_PRODUCT_CREATION)
                ? policyValidationOutboundPort.validateDataProductCreation(dataProduct)
                : Collections.emptyMap();
    }

    private Map<String, RegistryV1DataProductValidationResult> validateDataProductVersionPolicies(RegistryV1DataProductResource registryV1DataProduct, RegistryV1DataProductValidationRequestResource request) {
        if (!isPolicyEventTypeRequested(request.getPolicyEventTypes(), DATA_PRODUCT_VERSION_CREATION)) {
            return Collections.emptyMap();
        }
        JsonNode currentDescriptor = getMostRecentDataProductVersion(registryV1DataProduct)
                .map(mostRecentDataProductVersion -> dataProductVersionCrudService.findOne(mostRecentDataProductVersion.getUuid()).getContent())
                .orElse(null);
        return policyValidationOutboundPort.validateDataProductVersionCreation(currentDescriptor, request.getDataProductVersion());
    }

    private boolean isPolicyEventTypeRequested(List<String> policyEventTypes, String policyEventType) {
        return policyEventTypes.isEmpty() || policyEventTypes.contains(policyEventType);
    }

    private Optional<RegistryV1DataProductResource> findExistentDataProduct(String dataProductId) {
        try {
            return Optional.of(registryV1Service.getDataProduct(dataProductId));
        } catch (NotFoundException e) {
            return Optional.empty();
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
