package org.opendatamesh.platform.pp.registry.old.v1.registryservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.opendatamesh.platform.pp.registry.rest.v2.RegistryApplicationIT;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = {
        "odm.product-plane.policy-service.active=false",
        "odm.product-plane.policy-service.descriptor.parser.version=1",
        "spring.main.allow-bean-definition-overriding=true"
})
class RegistryServiceV1ValidatePolicyDisabledIT extends RegistryApplicationIT {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private JsonNode loadDescriptor(String resourcePath) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test-data/" + resourcePath);
        if (inputStream == null) {
            throw new IllegalArgumentException("Resource not found: test-data/" + resourcePath);
        }
        return objectMapper.readTree(inputStream);
    }

    /*
    Scenario: Policy client is disabled but request specifies validatePolicies=true
      Given the policy service is disabled (odm.product-plane.policy-service.active=false)
      When POST /api/v1/pp/registry/validate/report with validatePolicies=true
      Then the response status is 200
      And policiesValidationResults contains PolicyServiceNotActive with validated=true and explanatory message
    */
    @Test
    void whenValidatePoliciesRequestedButPolicyClientDisabledThen200AndPolicyServiceNotActiveInResults() throws IOException {
        JsonNode validDescriptor = loadDescriptor("dpds-v1.0.0.json");
        RegistryV1DataProductValidationRequestResource request = new RegistryV1DataProductValidationRequestResource();
        request.setValidateSyntax(false);
        request.setValidatePolicies(true);
        request.setDataProductVersion(validDescriptor);

        ResponseEntity<RegistryV1DataProductValidationResponseResource> response = rest.postForEntity(
                apiUrlFromString("/api/v1/pp/registry/validate/report"),
                new HttpEntity<>(request),
                RegistryV1DataProductValidationResponseResource.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPoliciesValidationResults()).isNotEmpty();
        assertThat(response.getBody().getPoliciesValidationResults()).containsKey("PolicyServiceNotActive");
        RegistryV1DataProductValidationResult policyNotActiveResult = response.getBody().getPoliciesValidationResults().get("PolicyServiceNotActive");
        assertThat(policyNotActiveResult.isValidated()).isFalse();
        assertThat(policyNotActiveResult.getValidationOutput()).isNotNull();
        assertThat(policyNotActiveResult.getValidationOutput().toString()).contains("Policy Service is not activated");
    }

}
