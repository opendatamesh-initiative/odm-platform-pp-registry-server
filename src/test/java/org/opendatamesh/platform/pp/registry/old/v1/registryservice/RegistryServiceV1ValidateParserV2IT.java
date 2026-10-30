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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@TestPropertySource(properties = {
        "odm.product-plane.policy-service.active=true",
        "odm.product-plane.policy-service.version=1",
        "odm.product-plane.policy-service.address=http://localhost:9999",
        "odm.product-plane.registry-service.v1.descriptor.parser.version=2",
        "spring.main.allow-bean-definition-overriding=true"
})
@Import(RegistryServiceV1ValidateParserV2IT.ValidateReportParserV2TestConfig.class)
class RegistryServiceV1ValidateParserV2IT extends RegistryApplicationIT {

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
    Scenario: With descriptor parser version 2, policy validation passes the raw descriptors in event format
      Given the registry is configured with registry-service.v1.descriptor.parser.version=2
      And one data product with one data product version already saved in the registry
      When the client POSTs to /api/v1/pp/registry/validate/report with validatePolicies=true and a descriptor with the same FQN
      Then the policy client is invoked for data product version policy evaluation
      And currentState and afterState are wrapped in the event format (dataProductVersion wrapper), as during publication
      And the wrapped content is the raw descriptor, not the old parser output
    */
    @Test
    void whenParserV2AndValidatingPoliciesThenRawDescriptorsArePassedInEventFormat() throws IOException {
        // Given
        String fqn = "urn:dpds:qualityDemo:dataproducts:test1:1";
        DataProductRes dataProduct = createDataProductWithFqn(fqn, "test1");
        JsonNode storedDescriptor = loadDescriptor("dpds-v1.0.0.json");
        DataProductVersionRes version100 = createDataProductVersionWithContent(dataProduct, "1.0.0", storedDescriptor);

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
        ResponseEntity<RegistryV1DataProductValidationResponseResource> response = rest.postForEntity(
                apiUrlFromString("/api/v1/pp/registry/validate/report"),
                new HttpEntity<>(request),
                RegistryV1DataProductValidationResponseResource.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(policyClient, times(2)).validateInput(requestCaptor.capture(), eq(false));
        PolicyResPolicyEvaluationRequest dpvRequest = requestCaptor.getAllValues().stream()
                .filter(r -> r.getEvent() == PolicyResPolicyEvaluationRequest.EventType.DATA_PRODUCT_VERSION_CREATION)
                .findFirst()
                .orElseThrow();

        assertThat(dpvRequest.getCurrentState()).isNotNull();
        assertThat(dpvRequest.getCurrentState().get("dataProductVersion")).isEqualTo(storedDescriptor);
        assertThat(dpvRequest.getAfterState()).isNotNull();
        assertThat(dpvRequest.getAfterState().get("dataProductVersion")).isEqualTo(passedDescriptor);

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
    static class ValidateReportParserV2TestConfig {
        @Bean
        @Primary
        public PolicyClientV1 mockPolicyClientV1() {
            return mock(PolicyClientV1.class);
        }
    }
}
