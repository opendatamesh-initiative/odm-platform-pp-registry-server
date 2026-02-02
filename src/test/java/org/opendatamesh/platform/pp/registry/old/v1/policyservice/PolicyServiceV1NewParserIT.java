package org.opendatamesh.platform.pp.registry.old.v1.policyservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.opendatamesh.platform.pp.registry.client.notification.NotificationClient;
import org.opendatamesh.platform.pp.registry.rest.v2.RegistryApplicationIT;
import org.opendatamesh.platform.pp.registry.rest.v2.RoutesV2;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductValidationStateRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionValidationStateRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.notification.NotificationDispatchRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@TestPropertySource(properties = {
        "odm.product-plane.policy-service.active=true",
        "odm.product-plane.policy-service.version=1",
        "odm.product-plane.policy-service.address=http://localhost:9999", // Dummy address to satisfy real config if needed (though we override bean)
        "odm.product-plane.policy-service.descriptor.parser.version=2",
        "spring.main.allow-bean-definition-overriding=true"
})
@Import(PolicyServiceV1NewParserIT.PolicyServiceTestConfig.class)
public class PolicyServiceV1NewParserIT extends RegistryApplicationIT {

    @Autowired
    private NotificationClient notificationClient;

    @Autowired
    private PolicyClientV1 policyClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @TestConfiguration
    static class PolicyServiceTestConfig {
        @Bean
        @Primary
        public PolicyClientV1 mockPolicyClientV1() {
            return mock(PolicyClientV1.class);
        }
    }

    @BeforeEach
    public void setUp() {
        reset(notificationClient);
        reset(policyClient);
    }

    @AfterEach
    public void tearDown() {
        reset(notificationClient);
        reset(policyClient);
    }

    /**
     * Scenario: Descriptor parser version 2 — descriptor passed as-is to policy service
     * Given: Registry 2.0 sent a validation request with event type "DATA_PRODUCT_VERSION_PUBLICATION_REQUESTED"
     * And: The application has odm.product-plane.policy-service.descriptor.parser.version set to 2
     * When: The adapter handles the notification
     * Then: The adapter should send a validation request to the policy service
     * And: The policy service should receive the data product descriptor exactly as stored in the Data Product Version (no parsing with the old 1.x parser)
     */
    @Test
    public void testDescriptorPassedAsIsWhenParserVersionIs2() throws java.io.IOException {
        // Given: data product and version with descriptor from dpds-v1.0.0.json (as stored)
        String nameSuffix = "testDescriptorPassedAsIsWhenParserVersionIs2";
        String tag = "1.0.0";
        JsonNode descriptor = loadJsonResource("test-data/dpds-v1.0.0.json");

        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-" + nameSuffix);
        dataProduct.setDomain("test-domain");
        dataProduct.setFqn("test.fqn." + nameSuffix);
        dataProduct.setDisplayName("Test Product " + nameSuffix);
        dataProduct.setDescription("Test Description");
        dataProduct.setValidationState(DataProductValidationStateRes.PENDING);
        ResponseEntity<DataProductRes> dpResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dpResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        DataProductRes createdDataProduct = dpResponse.getBody();
        String dataProductId = createdDataProduct.getUuid();

        DataProductVersionRes dataProductVersion = new DataProductVersionRes();
        dataProductVersion.setDataProduct(createdDataProduct);
        dataProductVersion.setTag(tag);
        dataProductVersion.setVersionNumber(tag);
        dataProductVersion.setName("test-version-" + tag);
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setValidationState(DataProductVersionValidationStateRes.PENDING);
        dataProductVersion.setSpec("opendatamesh");
        dataProductVersion.setSpecVersion("1.0.0");
        dataProductVersion.setContent(descriptor);

        ResponseEntity<DataProductVersionRes> dpvResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS),
                new HttpEntity<>(dataProductVersion),
                DataProductVersionRes.class
        );
        assertThat(dpvResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        DataProductVersionRes createdVersion = dpvResponse.getBody();

        EventReceivedDataProductVersionPublicationRequested.EventContent eventContent = new EventReceivedDataProductVersionPublicationRequested.EventContent();
        eventContent.setDataProductVersion(toEventDataProductVersionRes(createdVersion));

        EventReceivedDataProductVersionPublicationRequested receivedEvent = new EventReceivedDataProductVersionPublicationRequested();
        receivedEvent.setResourceIdentifier(dataProductId);
        receivedEvent.setEventContent(eventContent);

        NotificationDispatchRes notification = new NotificationDispatchRes();
        notification.setSequenceId(1L);
        NotificationDispatchRes.NotificationDispatchEventRes dispatchEvent = new NotificationDispatchRes.NotificationDispatchEventRes();
        dispatchEvent.setSequenceId(1L);
        dispatchEvent.setResourceType(receivedEvent.getResourceType());
        dispatchEvent.setResourceIdentifier(receivedEvent.getResourceIdentifier());
        dispatchEvent.setType(receivedEvent.getType());
        dispatchEvent.setEventTypeVersion(receivedEvent.getEventTypeVersion());
        dispatchEvent.setEventContent(objectMapper.valueToTree(receivedEvent.getEventContent()));
        notification.setEvent(dispatchEvent);
        NotificationDispatchRes.NotificationDispatchSubscriptionRes subscription = new NotificationDispatchRes.NotificationDispatchSubscriptionRes();
        subscription.setName("test-observer-policyservice");
        subscription.setDisplayName("Test Observer PolicyService");
        subscription.setObserverBaseUrl("http://localhost:8080");
        subscription.setObserverApiVersion("v2");
        notification.setSubscription(subscription);

        PolicyResValidationResponse validationResponse = new PolicyResValidationResponse();
        validationResponse.setResult(true);
        validationResponse.setPolicyResults(Collections.emptyList());
        when(policyClient.validateInput(any(), eq(true))).thenReturn(validationResponse);

        // When
        ResponseEntity<Void> notifyResponse = rest.postForEntity(
                apiUrlFromString("/api/v2/up/observer/notifications"),
                new HttpEntity<>(notification),
                Void.class
        );

        // Then: adapter sends validation request
        assertThat(notifyResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ArgumentCaptor<PolicyResPolicyEvaluationRequest> requestCaptor = ArgumentCaptor.forClass(PolicyResPolicyEvaluationRequest.class);
        verify(policyClient).validateInput(requestCaptor.capture(), eq(true));
        PolicyResPolicyEvaluationRequest sentRequest = requestCaptor.getValue();

        // And: descriptor in the request is exactly as stored (no old parser transformation)
        assertThat(sentRequest.getAfterState()).isNotNull();
        assertThat(sentRequest.getAfterState().get("dataProductVersion")).isEqualTo(createdVersion.getContent());

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + dpvResponse.getBody().getUuid()));
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
    }


    private EventReceivedDataProductVersionPublicationRequested.DataProductVersionRes toEventDataProductVersionRes(DataProductVersionRes v) {
        EventReceivedDataProductVersionPublicationRequested.DataProductVersionRes res = new EventReceivedDataProductVersionPublicationRequested.DataProductVersionRes();
        res.setUuid(v.getUuid());
        res.setTag(v.getTag());
        res.setName(v.getName());
        res.setDescription(v.getDescription());
        res.setSpec(v.getSpec());
        res.setSpecVersion(v.getSpecVersion());
        res.setContent(v.getContent());
        res.setValidationState(v.getValidationState() != null ? v.getValidationState().name() : null);
        if (v.getDataProduct() != null) {
            EventReceivedDataProductVersionPublicationRequested.DataProductRes dpRes = new EventReceivedDataProductVersionPublicationRequested.DataProductRes();
            dpRes.setUuid(v.getDataProduct().getUuid());
            dpRes.setFqn(v.getDataProduct().getFqn());
            res.setDataProduct(dpRes);
        }
        return res;
    }

    private JsonNode loadJsonResource(String path) throws java.io.IOException {
        java.io.File file = org.springframework.util.ResourceUtils.getFile("classpath:" + path);
        return objectMapper.readTree(file);
    }

}