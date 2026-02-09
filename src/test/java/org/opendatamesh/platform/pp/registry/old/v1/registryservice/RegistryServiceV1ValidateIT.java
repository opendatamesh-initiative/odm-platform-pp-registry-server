package org.opendatamesh.platform.pp.registry.old.v1.registryservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.opendatamesh.platform.pp.registry.old.v1.policyservice.*;
import org.opendatamesh.platform.pp.registry.rest.v2.RegistryApplicationIT;
import org.opendatamesh.platform.pp.registry.rest.v2.RoutesV2;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionValidationStateRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@TestPropertySource(properties = {
        "odm.product-plane.policy-service.active=true",
        "odm.product-plane.policy-service.version=1",
        "odm.product-plane.policy-service.address=http://localhost:9999",
        "odm.descriptor.parser.version=1",
        "spring.main.allow-bean-definition-overriding=true"
})
@Import(RegistryServiceV1ValidateIT.ValidateReportTestConfig.class)
class RegistryServiceV1ValidateIT extends RegistryApplicationIT {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private PolicyClientV1 policyClient;

    @BeforeEach
    void setUp() {
        if (policyClient != null) {
            reset(policyClient);
        }
    }

    private JsonNode loadDescriptor(String resourcePath) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test-data/" + resourcePath);
        if (inputStream == null) {
            throw new IllegalArgumentException("Resource not found: test-data/" + resourcePath);
        }
        return objectMapper.readTree(inputStream);
    }

    /*
    Scenario: V1 validation report returns success when only syntax validation is requested
      Given a well-formed DPDS descriptor (e.g. minimal valid descriptor for enrichment)
      When the client POSTs to /api/v1/pp/registry/validate/report with validateSyntax=true and validatePolicies=false
      Then the response status is 200 OK
      And syntaxValidationResult.validated is true
      And syntaxValidationResult contains the descriptor structure validation outcome
      And policiesValidationResults is empty because policy validation was not requested
    */
    @Test
    void whenValidDescriptorAndSyntaxOnlyThen200AndSyntaxValidatedTrue() throws IOException {
        // Given a valid DPDS descriptor
        JsonNode validDescriptor = loadDescriptor("dpds-minimal-for-enrichment-v1.0.0.json");
        RegistryV1DataProductValidationRequestResource request = new RegistryV1DataProductValidationRequestResource();
        request.setValidateSyntax(true);
        request.setValidatePolicies(false);
        request.setDataProductVersion(validDescriptor);

        // When POST /api/v1/pp/registry/validate/report
        ResponseEntity<RegistryV1DataProductValidationResponseResource> response = rest.postForEntity(
                apiUrlFromString("/api/v1/pp/registry/validate/report"),
                new HttpEntity<>(request),
                RegistryV1DataProductValidationResponseResource.class
        );

        // Then the response status is 200, syntaxValidationResult.validated is true, policiesValidationResults is empty
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSyntaxValidationResult()).isNotNull();
        assertThat(response.getBody().getSyntaxValidationResult().isValidated()).isTrue();
        assertThat(response.getBody().getPoliciesValidationResults()).isEmpty();
    }

    /*
    Scenario: V1 validation report returns policy results when only policy validation is requested
      Given a valid DPDS descriptor and the policy service is configured and returns success for the evaluated policies
      When the client POSTs to /api/v1/pp/registry/validate/report with validateSyntax=false and validatePolicies=true
      Then the response status is 200 OK
      And syntaxValidationResult is null (syntax was not validated)
      And policiesValidationResults is non-empty and contains an entry per evaluated policy (e.g. "test-policy")
      And each policy result reflects validated=true when the policy service returned success
      And the policy client is invoked (e.g. twice: data product and data product version policies)
    */
    @Test
    void whenValidDescriptorAndPolicyOnlyThen200AndPoliciesPopulated() throws IOException {
        // Given a valid DPDS descriptor
        JsonNode validDescriptor = loadDescriptor("dpds-v1.0.0.json");
        RegistryV1DataProductValidationRequestResource request = new RegistryV1DataProductValidationRequestResource();
        request.setValidateSyntax(false);
        request.setValidatePolicies(true);
        request.setDataProductVersion(validDescriptor);

        PolicyResValidationResponse mockPolicyResponse = new PolicyResValidationResponse();
        mockPolicyResponse.setResult(true);
        List<PolicyResPolicyEvaluationResult> results = new ArrayList<>();
        PolicyResPolicyEvaluationResult result = new PolicyResPolicyEvaluationResult();
        result.setResult(true);
        result.setOutputObject("{\"allowed\":true,\"message\":\"Policy passed\"}");
        PolicyResPolicy policy = new PolicyResPolicy();
        policy.setName("test-policy");
        policy.setBlockingFlag(false);
        result.setPolicy(policy);
        results.add(result);
        mockPolicyResponse.setPolicyResults(results);
        when(policyClient.validateInput(any(), eq(false))).thenReturn(mockPolicyResponse);

        // When POST /api/v1/pp/registry/validate/report
        ResponseEntity<RegistryV1DataProductValidationResponseResource> response = rest.postForEntity(
                apiUrlFromString("/api/v1/pp/registry/validate/report"),
                new HttpEntity<>(request),
                RegistryV1DataProductValidationResponseResource.class
        );

        // Then the response status is 200, syntaxValidationResult absent, policiesValidationResults populated
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSyntaxValidationResult()).isNull();
        assertThat(response.getBody().getPoliciesValidationResults()).isNotEmpty();
        assertThat(response.getBody().getPoliciesValidationResults()).containsKey("test-policy");
        assertThat(response.getBody().getPoliciesValidationResults().get("test-policy").isValidated()).isTrue();
        verify(policyClient, times(2)).validateInput(any(), eq(false));
    }

    /*
    Scenario: V1 validation report returns both syntax and policy results when both validations are requested
      Given a valid DPDS descriptor and the policy service is mocked to return success for all policies
      When the client POSTs to /api/v1/pp/registry/validate/report with validateSyntax=true and validatePolicies=true
      Then the response status is 200 OK
      And syntaxValidationResult is present and syntaxValidationResult.validated is true
      And policiesValidationResults is present and populated from the policy service (e.g. "default-policy")
      And the policy client is invoked for both data product and data product version policy evaluation
    */
    @Test
    void whenValidDescriptorAndSyntaxAndPolicyThen200AndBothResultsPresent() throws IOException {
        // Given a valid DPDS descriptor
        JsonNode validDescriptor = loadDescriptor("dpds-v1.0.0.json");
        RegistryV1DataProductValidationRequestResource request = new RegistryV1DataProductValidationRequestResource();
        request.setValidateSyntax(true);
        request.setValidatePolicies(true);
        request.setDataProductVersion(validDescriptor);

        PolicyResValidationResponse mockPolicyResponse = new PolicyResValidationResponse();
        mockPolicyResponse.setResult(true);
        List<PolicyResPolicyEvaluationResult> policyResults = new ArrayList<>();
        PolicyResPolicyEvaluationResult policyResult = new PolicyResPolicyEvaluationResult();
        policyResult.setResult(true);
        policyResult.setOutputObject("{\"allowed\":true}");
        PolicyResPolicy policy = new PolicyResPolicy();
        policy.setName("default-policy");
        policy.setBlockingFlag(false);
        policyResult.setPolicy(policy);
        policyResults.add(policyResult);
        mockPolicyResponse.setPolicyResults(policyResults);
        when(policyClient.validateInput(any(), eq(false))).thenReturn(mockPolicyResponse);

        // When POST /api/v1/pp/registry/validate/report
        ResponseEntity<RegistryV1DataProductValidationResponseResource> response = rest.postForEntity(
                apiUrlFromString("/api/v1/pp/registry/validate/report"),
                new HttpEntity<>(request),
                RegistryV1DataProductValidationResponseResource.class
        );

        // Then the response status is 200, both syntax and policy results present
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSyntaxValidationResult()).isNotNull();
        assertThat(response.getBody().getSyntaxValidationResult().isValidated()).isTrue();
        assertThat(response.getBody().getPoliciesValidationResults()).isNotNull();
        // Policy client is called twice: once for data product policies, once for data product version policies
        verify(policyClient, times(2)).validateInput(any(), eq(false));
    }

    /*
    Scenario: V1 validation report returns syntax failure details when the descriptor is structurally invalid
      Given a DPDS descriptor that fails structural validation (e.g. missing required "info" or invalid components)
      When the client POSTs to /api/v1/pp/registry/validate/report with validateSyntax=true (and validatePolicies=false)
      Then the response status is 200 OK (validation report always returns 200; validity is in the body)
      And syntaxValidationResult.validated is false
      And syntaxValidationResult.validationOutput is present and contains error information (e.g. references to "info")
    */
    @Test
    void whenInvalidDescriptorAndValidateSyntaxThen200AndSyntaxValidatedFalse() throws IOException {
        // Given an invalid descriptor (missing info)
        JsonNode invalidDescriptor = loadDescriptor("dpds-invalid-missing-info.json");
        RegistryV1DataProductValidationRequestResource request = new RegistryV1DataProductValidationRequestResource();
        request.setValidateSyntax(true);
        request.setValidatePolicies(false);
        request.setDataProductVersion(invalidDescriptor);

        // When POST /api/v1/pp/registry/validate/report with validateSyntax=true
        ResponseEntity<RegistryV1DataProductValidationResponseResource> response = rest.postForEntity(
                apiUrlFromString("/api/v1/pp/registry/validate/report"),
                new HttpEntity<>(request),
                RegistryV1DataProductValidationResponseResource.class
        );

        // Then the response status is 200, syntaxValidationResult.validated is false, validationOutput contains error info
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSyntaxValidationResult()).isNotNull();
        assertThat(response.getBody().getSyntaxValidationResult().isValidated()).isFalse();
        assertThat(response.getBody().getSyntaxValidationResult().getValidationOutput()).isNotNull();
        assertThat(response.getBody().getSyntaxValidationResult().getValidationOutput().toString()).contains("info");
    }

    /*
    Scenario: V1 validation report returns policy failure details when the policy service reports a violation
      Given a valid DPDS descriptor and the policy service is mocked to return a failing result (e.g. blocking policy violation)
      When the client POSTs to /api/v1/pp/registry/validate/report with validatePolicies=true
      Then the response status is 200 OK (validation report returns 200; policy validity is in the body)
      And policiesValidationResults is non-empty and contains the failing policy by name (e.g. "blocking-policy")
      And the corresponding policy result has validated=false, reflecting the policy service failure
    */
    @Test
    void whenValidDescriptorAndPolicyReturnsFailureThen200AndPolicyResultValidatedFalse() throws IOException {
        // Given a valid descriptor
        JsonNode validDescriptor = loadDescriptor("dpds-v1.0.0.json");
        RegistryV1DataProductValidationRequestResource request = new RegistryV1DataProductValidationRequestResource();
        request.setValidateSyntax(false);
        request.setValidatePolicies(true);
        request.setDataProductVersion(validDescriptor);

        PolicyResValidationResponse mockPolicyResponse = new PolicyResValidationResponse();
        mockPolicyResponse.setResult(false);
        List<PolicyResPolicyEvaluationResult> results = new ArrayList<>();
        PolicyResPolicyEvaluationResult failedResult = new PolicyResPolicyEvaluationResult();
        failedResult.setResult(false);
        failedResult.setOutputObject("{\"allowed\":false,\"reason\":\"Policy violation\"}");
        PolicyResPolicy policy = new PolicyResPolicy();
        policy.setName("blocking-policy");
        policy.setBlockingFlag(true);
        failedResult.setPolicy(policy);
        results.add(failedResult);
        mockPolicyResponse.setPolicyResults(results);
        when(policyClient.validateInput(any(), eq(false))).thenReturn(mockPolicyResponse);

        // When POST /api/v1/pp/registry/validate/report
        ResponseEntity<RegistryV1DataProductValidationResponseResource> response = rest.postForEntity(
                apiUrlFromString("/api/v1/pp/registry/validate/report"),
                new HttpEntity<>(request),
                RegistryV1DataProductValidationResponseResource.class
        );

        // Then the response status is 200, at least one policy result with validated=false
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPoliciesValidationResults()).isNotEmpty();
        assertThat(response.getBody().getPoliciesValidationResults().get("blocking-policy").isValidated()).isFalse();
        verify(policyClient, times(2)).validateInput(any(), eq(false));
    }

    /*
    Scenario: V1 validation report returns 500 when the policy service client throws an unexpected exception
      Given a valid DPDS descriptor and validatePolicies=true so that the policy client will be invoked
      When the policy client throws an unexpected exception (e.g. RuntimeException: "Policy service unavailable")
      Then the response status is 500 Internal Server Error
      And the response body contains error type "ServerError" indicating an unexpected server-side failure
    */
    @Test
    void whenPolicyClientThrowsUnexpectedExceptionThen500() throws IOException {
        JsonNode validDescriptor = loadDescriptor("dpds-minimal-for-enrichment-v1.0.0.json");
        RegistryV1DataProductValidationRequestResource request = new RegistryV1DataProductValidationRequestResource();
        request.setValidateSyntax(false);
        request.setValidatePolicies(true);
        request.setDataProductVersion(validDescriptor);

        doThrow(new RuntimeException("Policy service unavailable"))
                .when(policyClient).validateInput(any(), eq(false));

        ResponseEntity<Map<String, Object>> response = rest.exchange(
                apiUrlFromString("/api/v1/pp/registry/validate/report"),
                HttpMethod.POST,
                new HttpEntity<>(request),
                new ParameterizedTypeReference<Map<String, Object>>() {
                }
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsEntry("error", "ServerError");
    }

    /*
    Scenario: Policy validation uses the most recent existing data product version as currentState (beforeState) and the passed descriptor as afterState
      Given one data product (one FQN) with two data product versions already saved in the registry (e.g. 1.0.0 and 1.1.0, same DP)
      When the client POSTs to /api/v1/pp/registry/validate/report with validatePolicies=true and a descriptor with the same FQN (e.g. version 2.0.0)
      Then the policy client is invoked for data product version policy evaluation
      And the evaluation request has currentState set to the event-form of the most recently created stored DPV (e.g. version 1.1.0)
      And the evaluation request has afterState set to the event-form of the passed descriptor (e.g. version 2.0.0)
    */
    @Test
    void whenTwoDataProductVersionsExistThenPolicyValidatedWithMostRecentAsCurrentStateAndPassedDescriptorAsAfterState() throws IOException {
        // Given: one data product (one FQN) and two DPVs (1.0.0 then 1.1.0), so the most recent is 1.1.0
        String fqn = "urn:dpds:qualityDemo:dataproducts:test1:1";
        DataProductRes dataProduct = createDataProductWithFqn(fqn, "test1");
        JsonNode dpdsV100 = loadDescriptor("dpds-v1.0.0.json");
        DataProductVersionRes version100 = createDataProductVersionWithContent(dataProduct, "1.0.0", dpdsV100);
        JsonNode dpdsV110 = loadDescriptor("dpds-v1.1.0.json");
        DataProductVersionRes version110 = createDataProductVersionWithContent(dataProduct, "1.1.0", dpdsV110);

        JsonNode passedDescriptor = loadDescriptor("dpds-v1.2.0.json");
        RegistryV1DataProductValidationRequestResource request = new RegistryV1DataProductValidationRequestResource();
        request.setValidateSyntax(false);
        request.setValidatePolicies(true);
        request.setDataProductVersion(passedDescriptor);

        PolicyResValidationResponse mockPolicyResponse = new PolicyResValidationResponse();
        mockPolicyResponse.setResult(true);
        List<PolicyResPolicyEvaluationResult> results = new ArrayList<>();
        PolicyResPolicyEvaluationResult result = new PolicyResPolicyEvaluationResult();
        result.setResult(true);
        PolicyResPolicy policy = new PolicyResPolicy();
        policy.setName("dpv-policy");
        policy.setBlockingFlag(false);
        result.setPolicy(policy);
        results.add(result);
        mockPolicyResponse.setPolicyResults(results);
        when(policyClient.validateInput(any(), eq(false))).thenReturn(mockPolicyResponse);

        ArgumentCaptor<PolicyResPolicyEvaluationRequest> requestCaptor = ArgumentCaptor.forClass(PolicyResPolicyEvaluationRequest.class);

        // When
        ResponseEntity<RegistryV1DataProductValidationResponseResource> response = rest.postForEntity(
                apiUrlFromString("/api/v1/pp/registry/validate/report"),
                new HttpEntity<>(request),
                RegistryV1DataProductValidationResponseResource.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(policyClient, times(2)).validateInput(requestCaptor.capture(), eq(false));

        PolicyResPolicyEvaluationRequest dpvRequest = requestCaptor.getAllValues().stream()
                .filter(r -> r.getEvent() == PolicyResPolicyEvaluationRequest.EventType.DATA_PRODUCT_VERSION_CREATION)
                .findFirst()
                .orElseThrow();
        // Then: currentState is the most recent stored DPV (1.1.0), afterState is the passed descriptor (2.0.0)
        assertThat(dpvRequest.getCurrentState()).isNotNull();
        assertThat(versionFromEventInfo(dpvRequest.getCurrentState())).isEqualTo("1.1.0");
        assertThat(dpvRequest.getAfterState()).isNotNull();
        assertThat(versionFromEventInfo(dpvRequest.getAfterState())).isEqualTo("1.2.0");

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + version100.getUuid()));
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + version110.getUuid()));
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProduct.getUuid()));
    }

    /*
    Scenario: When validating policies, both existing data product version content and passed descriptor are parsed with the old parser and passed to the policy client in the event format
      Given one data product (one FQN) with two data product versions already saved in the registry (same DP, different versions)
      When the client POSTs to /api/v1/pp/registry/validate/report with validatePolicies=true and a descriptor (same FQN, e.g. new version)
      Then the policy client is invoked for data product version policy evaluation
      And the evaluation request currentState and afterState are JsonNodes in event format (dataProductVersion wrapper)
      And the inner structure is the old parser output (e.g. info version, info.fullyQualifiedName), not raw request JSON
    */
    @Test
    void whenValidatingPoliciesThenExistingDpvAndPassedDescriptorAreParsedWithOldParserAndPassedInEventFormat() throws IOException {
        // Given: one data product (one FQN) and two DPVs (1.0.0 and 1.1.0), so the most recent is 1.1.0
        String fqn = "urn:dpds:qualityDemo:dataproducts:test1:1";
        DataProductRes dataProduct = createDataProductWithFqn(fqn, "test1");
        DataProductVersionRes version100 = createDataProductVersionWithContent(dataProduct, "1.0.0", loadDescriptor("dpds-v1.0.0.json"));

        JsonNode passedDescriptor = loadDescriptor("dpds-v1.1.0.json");

        RegistryV1DataProductValidationRequestResource request = new RegistryV1DataProductValidationRequestResource();
        request.setValidateSyntax(false);
        request.setValidatePolicies(true);
        request.setDataProductVersion(passedDescriptor);

        PolicyResValidationResponse mockPolicyResponse = new PolicyResValidationResponse();
        mockPolicyResponse.setResult(true);
        mockPolicyResponse.setPolicyResults(List.of());
        when(policyClient.validateInput(any(), eq(false))).thenReturn(mockPolicyResponse);

        ArgumentCaptor<PolicyResPolicyEvaluationRequest> requestCaptor = ArgumentCaptor.forClass(PolicyResPolicyEvaluationRequest.class);

        // When
        rest.postForEntity(
                apiUrlFromString("/api/v1/pp/registry/validate/report"),
                new HttpEntity<>(request),
                RegistryV1DataProductValidationResponseResource.class
        );

        verify(policyClient, times(2)).validateInput(requestCaptor.capture(), eq(false));
        PolicyResPolicyEvaluationRequest dpvRequest = requestCaptor.getAllValues().stream()
                .filter(r -> r.getEvent() == PolicyResPolicyEvaluationRequest.EventType.DATA_PRODUCT_VERSION_CREATION)
                .findFirst()
                .orElseThrow();

        // Then: both states are in event format (dataProductVersion wrapper) with old-parser structure (info, etc.)
        assertThat(dpvRequest.getCurrentState()).isNotNull();
        JsonNode expectedCurrentState = loadDescriptor("old/descriptor-with-rawcontent.json");
        assertThat(dpvRequest.getCurrentState().get("dataProductVersion")).usingRecursiveComparison().isEqualTo(expectedCurrentState);
        assertThat(dpvRequest.getAfterState()).isNotNull();
        JsonNode expectedAfterState = loadDescriptor("old/descriptor-with-rawcontent-1.1.0.json");
        assertThat(dpvRequest.getAfterState().get("dataProductVersion")).usingRecursiveComparison().isEqualTo(expectedAfterState);

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + version100.getUuid()));
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProduct.getUuid()));
    }

    private DataProductRes createDataProductWithFqn(String fqn, String uniqueName) {
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName(uniqueName);
        dataProduct.setDomain("qualityDemo");
        dataProduct.setFqn(fqn);
        dataProduct.setDisplayName("Test Product");
        dataProduct.setDescription("Test");
        ResponseEntity<DataProductRes> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    /**
     * Extracts version from event request state (dataProductVersion.info); supports both "version" and "versionNumber" in serialized output.
     */
    private static String versionFromEventInfo(JsonNode state) {
        if (state == null || !state.has("dataProductVersion")) return "";
        JsonNode info = state.path("dataProductVersion").path("info");
        if (info.has("versionNumber") && !info.path("versionNumber").isMissingNode())
            return info.path("versionNumber").asText();
        if (info.has("version") && !info.path("version").isMissingNode()) return info.path("version").asText();
        return "";
    }

    private DataProductVersionRes createDataProductVersionWithContent(DataProductRes dataProduct, String version, JsonNode content) {
        DataProductVersionRes dataProductVersion = new DataProductVersionRes();
        dataProductVersion.setDataProduct(dataProduct);
        dataProductVersion.setTag(version);
        dataProductVersion.setVersionNumber(version);
        dataProductVersion.setName("test-version-" + version);
        dataProductVersion.setDescription("Test");
        dataProductVersion.setValidationState(DataProductVersionValidationStateRes.PENDING);
        dataProductVersion.setSpec("dpds");
        dataProductVersion.setSpecVersion("1.0.0");
        dataProductVersion.setContent(content);
        ResponseEntity<DataProductVersionRes> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS),
                new HttpEntity<>(dataProductVersion),
                DataProductVersionRes.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }


    @TestConfiguration
    static class ValidateReportTestConfig {
        @Bean
        @Primary
        public PolicyClientV1 mockPolicyClientV1() {
            return mock(PolicyClientV1.class);
        }
    }
}
