package org.opendatamesh.platform.pp.registry.old.v1.policyservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@TestPropertySource(properties = {
        "odm.product-plane.policy-service.active=true",
        "odm.product-plane.policy-service.version=1",
        "odm.product-plane.policy-service.address=http://localhost:9999", // Dummy address to satisfy real config if needed (though we override bean)
        "spring.main.allow-bean-definition-overriding=true"
})
@Import(PolicyServiceV1IT.PolicyServiceTestConfig.class)
public class PolicyServiceV1IT extends RegistryApplicationIT {

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
     * Given: Registry 2.0 sent a validation request with event type "DATA_PRODUCT_INITIALIZATION_REQUESTED"
     * And: The adapter transformed and sent the request to the policy service
     * And: The policy service returns a ValidationResponseResource with result "true"
     * And: All policies in the response have passed validation
     * When: The adapter receives the validation response
     * Then: The adapter should transform the response to Registry 2.0 format
     * And: The adapter should send a "DATA_PRODUCT_INITIALIZATION_APPROVED" event to Registry 2.0
     * And: The event should contain the original dataProductId
     * And: The event should include all policy evaluation results
     * And: The event should be compatible with Registry 2.0 event flow
     */
    @Test
    public void testDataProductInitializationApproved() {
        // Given
        DataProductRes dataProduct = createDataProduct("testDataProductInitializationApproved");
        String dataProductId = dataProduct.getUuid();

        NotificationDispatchRes notification = createNotificationDispatch(
                "DATA_PRODUCT_INITIALIZATION_REQUESTED",
                "DATA_PRODUCT",
                dataProductId,
                createDataProductContent(dataProduct)
        );

        PolicyResValidationResponse validationResponse = new PolicyResValidationResponse();
        validationResponse.setResult(true);
        validationResponse.setPolicyResults(Collections.emptyList());

        when(policyClient.validateInput(any(), eq(true))).thenReturn(validationResponse);

        // When
        ResponseEntity<Void> response = rest.postForEntity(
                apiUrlFromString("/api/v2/up/observer/notifications"),
                new HttpEntity<>(notification),
                Void.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(policyClient).validateInput(any(), eq(true));

        // Verify Approved Event Emitted
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(notificationClient, times(1)).notifyEvent(eventCaptor.capture());
        Object capturedEvent = eventCaptor.getValue();
        EventEmittedDataProductInitializationApproved actualEvent = objectMapper.convertValue(capturedEvent, EventEmittedDataProductInitializationApproved.class);

        EventEmittedDataProductInitializationApproved expectedEvent = new EventEmittedDataProductInitializationApproved();
        expectedEvent.setResourceIdentifier(dataProductId);
        EventEmittedDataProductInitializationApproved.EventContent content = new EventEmittedDataProductInitializationApproved.EventContent();
        EventEmittedDataProductInitializationApproved.DataProductRes dpRes = new EventEmittedDataProductInitializationApproved.DataProductRes();
        dpRes.setUuid(dataProduct.getUuid());
        dpRes.setFqn(dataProduct.getFqn());
        content.setDataProduct(dpRes);
        expectedEvent.setEventContent(content);

        assertThat(actualEvent).usingRecursiveComparison().isEqualTo(expectedEvent);

        // Cleanup
        deleteDataProduct(dataProductId);
    }

    /**
     * Given: Registry 2.0 sent a validation request with event type "DATA_PRODUCT_INITIALIZATION_REQUESTED"
     * And: The adapter transformed and sent the request to the policy service
     * And: The policy service returns a ValidationResponseResource with result "false"
     * And: The response contains at least one blocking policy that failed
     * When: The adapter receives the validation response
     * Then: The adapter should transform the response to Registry 2.0 format
     * And: The adapter should send a "DATA_PRODUCT_INITIALIZATION_REJECTED" event to Registry 2.0
     * And: The event should contain the original dataProductId
     * And: The event should include details of failed blocking policies
     * And: The event should include details of failed non-blocking policies
     * And: The event should be compatible with Registry 2.0 error handling
     */
    @Test
    public void testDataProductInitializationRejected() {
        // Given
        DataProductRes dataProduct = createDataProduct("testDataProductInitializationRejected");
        String dataProductId = dataProduct.getUuid();

        NotificationDispatchRes notification = createNotificationDispatch(
                "DATA_PRODUCT_INITIALIZATION_REQUESTED",
                "DATA_PRODUCT",
                dataProductId,
                createDataProductContent(dataProduct)
        );

        PolicyResValidationResponse validationResponse = new PolicyResValidationResponse();
        validationResponse.setResult(false);

        PolicyResPolicyEvaluationResult policyResult = new PolicyResPolicyEvaluationResult();
        policyResult.setResult(false); // Failed
        PolicyResPolicy policy = new PolicyResPolicy();
        policy.setBlockingFlag(true); // Blocking
        policyResult.setPolicy(policy);

        validationResponse.setPolicyResults(List.of(policyResult));

        when(policyClient.validateInput(any(), eq(true))).thenReturn(validationResponse);

        // When
        ResponseEntity<Void> response = rest.postForEntity(
                apiUrlFromString("/api/v2/up/observer/notifications"),
                new HttpEntity<>(notification),
                Void.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(policyClient).validateInput(any(), eq(true));
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(notificationClient, times(1)).notifyEvent(eventCaptor.capture());
        Object capturedEvent = eventCaptor.getValue();
        EventEmittedDataProductInitializationRejected actualEvent = objectMapper.convertValue(capturedEvent, EventEmittedDataProductInitializationRejected.class);

        EventEmittedDataProductInitializationRejected expectedEvent = new EventEmittedDataProductInitializationRejected();
        expectedEvent.setResourceIdentifier(dataProductId);
        EventEmittedDataProductInitializationRejected.EventContent content = new EventEmittedDataProductInitializationRejected.EventContent();
        EventEmittedDataProductInitializationRejected.DataProductRes dpRes = new EventEmittedDataProductInitializationRejected.DataProductRes();
        dpRes.setUuid(dataProduct.getUuid());
        dpRes.setFqn(dataProduct.getFqn());
        content.setDataProduct(dpRes);
        expectedEvent.setEventContent(content);

        assertThat(actualEvent).usingRecursiveComparison().isEqualTo(expectedEvent);

        // Cleanup
        deleteDataProduct(dataProductId);
    }


    /**
     * Given: Registry 2.0 sent a validation request with event type "DATA_PRODUCT_INITIALIZATION_REQUESTED"
     * And: The adapter transformed and sent the request to the policy service
     * And: The policy service returns a ValidationResponseResource with result "false"
     * And: The response contains some policies that failed but none of them are blocking
     * When: The adapter receives the validation response
     * Then: The adapter should transform the response to Registry 2.0 format
     * And: The adapter should send a "DATA_PRODUCT_INITIALIZATION_APPROVED" event to Registry 2.0
     * And: The event should contain the original dataProductId
     * And: The event should include details of failed blocking policies
     * And: The event should include details of failed non-blocking policies
     * And: The event should be compatible with Registry 2.0 error handling
     */
    @Test
    public void testDataProductInitializationApprovedWithNonBlockingFailure() {
        // Given
        DataProductRes dataProduct = createDataProduct("testDataProductInitializationApprovedWithNonBlockingFailure");
        String dataProductId = dataProduct.getUuid();

        NotificationDispatchRes notification = createNotificationDispatch(
                "DATA_PRODUCT_INITIALIZATION_REQUESTED",
                "DATA_PRODUCT",
                dataProductId,
                createDataProductContent(dataProduct)
        );

        PolicyResValidationResponse validationResponse = new PolicyResValidationResponse();
        validationResponse.setResult(false); // Validated as false (some failed)

        PolicyResPolicyEvaluationResult policyResult = new PolicyResPolicyEvaluationResult();
        policyResult.setResult(false); // Failed
        PolicyResPolicy policy = new PolicyResPolicy();
        policy.setBlockingFlag(false); // Non-Blocking
        policyResult.setPolicy(policy);

        validationResponse.setPolicyResults(List.of(policyResult));

        when(policyClient.validateInput(any(), eq(true))).thenReturn(validationResponse);

        // When
        ResponseEntity<Void> response = rest.postForEntity(
                apiUrlFromString("/api/v2/up/observer/notifications"),
                new HttpEntity<>(notification),
                Void.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(policyClient).validateInput(any(), eq(true));
        // Should be approved because failure is non-blocking
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(notificationClient, times(1)).notifyEvent(eventCaptor.capture());
        Object capturedEvent = eventCaptor.getValue();
        EventEmittedDataProductInitializationApproved actualEvent = objectMapper.convertValue(capturedEvent, EventEmittedDataProductInitializationApproved.class);

        EventEmittedDataProductInitializationApproved expectedEvent = new EventEmittedDataProductInitializationApproved();
        expectedEvent.setResourceIdentifier(dataProductId);
        EventEmittedDataProductInitializationApproved.EventContent content = new EventEmittedDataProductInitializationApproved.EventContent();
        EventEmittedDataProductInitializationApproved.DataProductRes dpRes = new EventEmittedDataProductInitializationApproved.DataProductRes();
        dpRes.setUuid(dataProduct.getUuid());
        dpRes.setFqn(dataProduct.getFqn());
        content.setDataProduct(dpRes);
        expectedEvent.setEventContent(content);

        assertThat(actualEvent).usingRecursiveComparison().isEqualTo(expectedEvent);

        // Cleanup
        deleteDataProduct(dataProductId);
    }

    /**
     * Given: Registry 2.0 sent a validation request with event type "DATA_PRODUCT_VERSION_PUBLICATION_REQUESTED"
     * And: The adapter transformed and sent the request to the policy service
     * And: The policy service returns a ValidationResponseResource with result "true"
     * And: All policies in the response have passed validation
     * When: The adapter receives the validation response
     * Then: The adapter should transform the response to Registry 2.0 format
     * And: The adapter should send a "DATA_PRODUCT_VERSION_PUBLICATION_APPROVED" event to Registry 2.0
     * And: The event should contain the original dataProductId and dataProductVersion
     * And: The event should include all policy evaluation results
     * And: The event should be compatible with Registry 2.0 event flow
     */
    @Test
    public void testDataProductVersionPublicationApproved() {
        // Given
        DataProductRes dataProduct = createDataProduct("testDataProductVersionPublicationApproved");
        String dataProductId = dataProduct.getUuid();
        DataProductVersionRes version = createDataProductVersion(dataProduct, "v1.0.0");
        String versionId = version.getUuid();

        NotificationDispatchRes notification = createNotificationDispatch(
                "DATA_PRODUCT_VERSION_PUBLICATION_REQUESTED",
                "DATA_PRODUCT_VERSION",
                versionId,
                createDataProductVersionContent(version)
        );

        PolicyResValidationResponse validationResponse = new PolicyResValidationResponse();
        validationResponse.setResult(true);
        validationResponse.setPolicyResults(Collections.emptyList());

        when(policyClient.validateInput(any(), eq(true))).thenReturn(validationResponse);

        // When
        ResponseEntity<Void> response = rest.postForEntity(
                apiUrlFromString("/api/v2/up/observer/notifications"),
                new HttpEntity<>(notification),
                Void.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(policyClient).validateInput(any(), eq(true));
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(notificationClient, times(1)).notifyEvent(eventCaptor.capture());
        Object capturedEvent = eventCaptor.getValue();
        EventEmittedDataProductVersionPublicationApproved actualEvent = objectMapper.convertValue(capturedEvent, EventEmittedDataProductVersionPublicationApproved.class);

        EventEmittedDataProductVersionPublicationApproved expectedEvent = new EventEmittedDataProductVersionPublicationApproved();
        expectedEvent.setResourceIdentifier(versionId);
        expectedEvent.setSequenceId(1L);
        EventEmittedDataProductVersionPublicationApproved.EventContent content = new EventEmittedDataProductVersionPublicationApproved.EventContent();
        EventEmittedDataProductVersionPublicationApproved.DataProductVersionRes dpvRes = new EventEmittedDataProductVersionPublicationApproved.DataProductVersionRes();
        dpvRes.setUuid(version.getUuid());
        dpvRes.setTag(version.getTag());
        EventEmittedDataProductVersionPublicationApproved.DataProductRes dpRes = new EventEmittedDataProductVersionPublicationApproved.DataProductRes();
        dpRes.setUuid(dataProduct.getUuid());
        dpRes.setFqn(dataProduct.getFqn());
        dpvRes.setDataProduct(dpRes);
        content.setDataProductVersion(dpvRes);
        expectedEvent.setEventContent(content);

        assertThat(actualEvent).usingRecursiveComparison().isEqualTo(expectedEvent);

        // Cleanup
        deleteDataProductVersion(versionId);
        deleteDataProduct(dataProductId);
    }

    /**
     * Given: Registry 2.0 sent a validation request with event type "DATA_PRODUCT_VERSION_PUBLICATION_REQUESTED"
     * And: The adapter transformed and sent the request to the policy service
     * And: The policy service returns a ValidationResponseResource with result "false"
     * And: The response contains at least one blocking policy that failed
     * When: The adapter receives the validation response
     * Then: The adapter should transform the response to Registry 2.0 format
     * And: The adapter should send a "DATA_PRODUCT_VERSION_PUBLICATION_REJECTED" event to Registry 2.0
     * And: The event should contain the original dataProductId and dataProductVersion
     * And: The event should include details of failed blocking policies
     * And: The event should include details of failed non-blocking policies
     * And: The event should be compatible with Registry 2.0 error handling
     */
    @Test
    public void testDataProductVersionPublicationRejected() {
        // Given
        DataProductRes dataProduct = createDataProduct("testDataProductVersionPublicationRejected");
        String dataProductId = dataProduct.getUuid();
        DataProductVersionRes version = createDataProductVersion(dataProduct, "v1.0.0");
        String versionId = version.getUuid();

        NotificationDispatchRes notification = createNotificationDispatch(
                "DATA_PRODUCT_VERSION_PUBLICATION_REQUESTED",
                "DATA_PRODUCT_VERSION",
                versionId,
                createDataProductVersionContent(version)
        );

        PolicyResValidationResponse validationResponse = new PolicyResValidationResponse();
        validationResponse.setResult(false);

        PolicyResPolicyEvaluationResult policyResult = new PolicyResPolicyEvaluationResult();
        policyResult.setResult(false); // Failed
        PolicyResPolicy policy = new PolicyResPolicy();
        policy.setBlockingFlag(true); // Blocking
        policyResult.setPolicy(policy);

        validationResponse.setPolicyResults(List.of(policyResult));

        when(policyClient.validateInput(any(), eq(true))).thenReturn(validationResponse);

        // When
        ResponseEntity<Void> response = rest.postForEntity(
                apiUrlFromString("/api/v2/up/observer/notifications"),
                new HttpEntity<>(notification),
                Void.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(policyClient).validateInput(any(), eq(true));
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(notificationClient, times(1)).notifyEvent(eventCaptor.capture());
        Object capturedEvent = eventCaptor.getValue();
        EventEmittedDataProductVersionPublicationRejected actualEvent = objectMapper.convertValue(capturedEvent, EventEmittedDataProductVersionPublicationRejected.class);

        EventEmittedDataProductVersionPublicationRejected expectedEvent = new EventEmittedDataProductVersionPublicationRejected();
        expectedEvent.setResourceIdentifier(versionId);
        expectedEvent.setSequenceId(1L);
        EventEmittedDataProductVersionPublicationRejected.EventContent content = new EventEmittedDataProductVersionPublicationRejected.EventContent();
        EventEmittedDataProductVersionPublicationRejected.DataProductVersionRes dpvRes = new EventEmittedDataProductVersionPublicationRejected.DataProductVersionRes();
        dpvRes.setUuid(version.getUuid());
        dpvRes.setTag(version.getTag());
        EventEmittedDataProductVersionPublicationRejected.DataProductRes dpRes = new EventEmittedDataProductVersionPublicationRejected.DataProductRes();
        dpRes.setUuid(dataProduct.getUuid());
        dpRes.setFqn(dataProduct.getFqn());
        dpvRes.setDataProduct(dpRes);
        content.setDataProductVersion(dpvRes);
        expectedEvent.setEventContent(content);

        assertThat(actualEvent).usingRecursiveComparison().isEqualTo(expectedEvent);

        // Cleanup
        deleteDataProductVersion(versionId);
        deleteDataProduct(dataProductId);
    }

    /**
     * Given: Registry 2.0 sent a validation request with event type "DATA_PRODUCT_VERSION_PUBLICATION_REQUESTED"
     * And: The adapter transformed and sent the request to the policy service
     * And: The policy service returns a ValidationResponseResource with result "false"
     * And: The response contains some policies that failed but none of them are blocking
     * When: The adapter receives the validation response
     * Then: The adapter should transform the response to Registry 2.0 format
     * And: The adapter should send a "DATA_PRODUCT_VERSION_PUBLICATION_APPROVED" event to Registry 2.0
     * And: The event should contain the original dataProductId and dataProductVersion
     * And: The event should include details of failed blocking policies
     * And: The event should include details of failed non-blocking policies
     * And: The event should be compatible with Registry 2.0 error handling
     */
    @Test
    public void testDataProductVersionPublicationApprovedWithNonBlockingFailure() {
        // Given
        DataProductRes dataProduct = createDataProduct("testDataProductVersionPublicationApprovedWithNonBlockingFailure");
        String dataProductId = dataProduct.getUuid();
        DataProductVersionRes version = createDataProductVersion(dataProduct, "v1.0.0");
        String versionId = version.getUuid();

        NotificationDispatchRes notification = createNotificationDispatch(
                "DATA_PRODUCT_VERSION_PUBLICATION_REQUESTED",
                "DATA_PRODUCT_VERSION",
                versionId,
                createDataProductVersionContent(version)
        );

        PolicyResValidationResponse validationResponse = new PolicyResValidationResponse();
        validationResponse.setResult(false);

        PolicyResPolicyEvaluationResult policyResult = new PolicyResPolicyEvaluationResult();
        policyResult.setResult(false); // Failed
        PolicyResPolicy policy = new PolicyResPolicy();
        policy.setBlockingFlag(false); // Non-Blocking
        policyResult.setPolicy(policy);

        validationResponse.setPolicyResults(List.of(policyResult));

        when(policyClient.validateInput(any(), eq(true))).thenReturn(validationResponse);

        // When
        ResponseEntity<Void> response = rest.postForEntity(
                apiUrlFromString("/api/v2/up/observer/notifications"),
                new HttpEntity<>(notification),
                Void.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(policyClient).validateInput(any(), eq(true));
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(notificationClient, times(1)).notifyEvent(eventCaptor.capture());
        Object capturedEvent = eventCaptor.getValue();
        EventEmittedDataProductVersionPublicationApproved actualEvent = objectMapper.convertValue(capturedEvent, EventEmittedDataProductVersionPublicationApproved.class);

        EventEmittedDataProductVersionPublicationApproved expectedEvent = new EventEmittedDataProductVersionPublicationApproved();
        expectedEvent.setResourceIdentifier(versionId);
        expectedEvent.setSequenceId(1L);
        EventEmittedDataProductVersionPublicationApproved.EventContent content = new EventEmittedDataProductVersionPublicationApproved.EventContent();
        EventEmittedDataProductVersionPublicationApproved.DataProductVersionRes dpvRes = new EventEmittedDataProductVersionPublicationApproved.DataProductVersionRes();
        dpvRes.setUuid(version.getUuid());
        dpvRes.setTag(version.getTag());
        EventEmittedDataProductVersionPublicationApproved.DataProductRes dpRes = new EventEmittedDataProductVersionPublicationApproved.DataProductRes();
        dpRes.setUuid(dataProduct.getUuid());
        dpRes.setFqn(dataProduct.getFqn());
        dpvRes.setDataProduct(dpRes);
        content.setDataProductVersion(dpvRes);
        expectedEvent.setEventContent(content);

        assertThat(actualEvent).usingRecursiveComparison().isEqualTo(expectedEvent);

        // Cleanup
        deleteDataProductVersion(versionId);
        deleteDataProduct(dataProductId);
    }

    /**
     * Given: Registry 2.0 sent a validation request with event type "DATA_PRODUCT_INITIALIZATION_REQUESTED"
     * When: The adapter handles the notification
     * Then: The adapter should transform the notification to Registry 1.0 format
     * And: The adapter should send a validation request to the policy service
     * And: Old policies should work as expected
     */
    @Test
    public void testRegistry1MappingCompatibility() {
        // Given
        DataProductRes dataProduct = createDataProduct("testRegistry1MappingCompatibility");
        String dataProductId = dataProduct.getUuid();

        NotificationDispatchRes notification = createNotificationDispatch(
                "DATA_PRODUCT_INITIALIZATION_REQUESTED",
                "DATA_PRODUCT",
                dataProductId,
                createDataProductContent(dataProduct)
        );

        PolicyResValidationResponse validationResponse = new PolicyResValidationResponse();
        validationResponse.setResult(true);
        validationResponse.setPolicyResults(Collections.emptyList());

        when(policyClient.validateInput(any(), eq(true))).thenReturn(validationResponse);

        // When
        ResponseEntity<Void> response = rest.postForEntity(
                apiUrlFromString("/api/v2/up/observer/notifications"),
                new HttpEntity<>(notification),
                Void.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Use ArgumentCaptor to verify transformation to PolicyResPolicyEvaluationRequest

        verify(policyClient).validateInput(argThat(request -> {
            assertThat(request.getDataProductId()).isEqualTo(dataProductId);
            // Verify other fields as needed
            return true;
        }), eq(true));

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(notificationClient, times(1)).notifyEvent(eventCaptor.capture());
        Object capturedEvent = eventCaptor.getValue();
        EventEmittedDataProductInitializationApproved actualEvent = objectMapper.convertValue(capturedEvent, EventEmittedDataProductInitializationApproved.class);

        EventEmittedDataProductInitializationApproved expectedEvent = new EventEmittedDataProductInitializationApproved();
        expectedEvent.setResourceIdentifier(dataProductId);
        EventEmittedDataProductInitializationApproved.EventContent content = new EventEmittedDataProductInitializationApproved.EventContent();
        EventEmittedDataProductInitializationApproved.DataProductRes dpRes = new EventEmittedDataProductInitializationApproved.DataProductRes();
        dpRes.setUuid(dataProduct.getUuid());
        dpRes.setFqn(dataProduct.getFqn());
        content.setDataProduct(dpRes);
        expectedEvent.setEventContent(content);

        assertThat(actualEvent).usingRecursiveComparison().isEqualTo(expectedEvent);

        // Cleanup
        deleteDataProduct(dataProductId);
    }

    // Helper methods

    private DataProductRes createDataProduct(String nameSuffix) {
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-" + nameSuffix);
        dataProduct.setDomain("test-domain");
        dataProduct.setFqn("test.fqn." + nameSuffix);
        dataProduct.setDisplayName("Test Product " + nameSuffix);
        dataProduct.setDescription("Test Description");
        dataProduct.setValidationState(DataProductValidationStateRes.PENDING);

        ResponseEntity<DataProductRes> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    private DataProductVersionRes createDataProductVersion(DataProductRes dataProduct, String version) {
        DataProductVersionRes dataProductVersion = new DataProductVersionRes();
        dataProductVersion.setDataProduct(dataProduct);
        dataProductVersion.setTag(version);
        dataProductVersion.setVersionNumber(version);
        dataProductVersion.setName("test-version-" + version);
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setValidationState(DataProductVersionValidationStateRes.PENDING);
        dataProductVersion.setSpec("opendatamesh");
        dataProductVersion.setSpecVersion("1.0.0");

        JsonNode content = objectMapper.createObjectNode()
                .put("name", "test-version-" + version)
                .put("version", version);
        dataProductVersion.setContent(content);

        ResponseEntity<DataProductVersionRes> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS),
                new HttpEntity<>(dataProductVersion),
                DataProductVersionRes.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    private void deleteDataProduct(String uuid) {
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + uuid));
    }

    private void deleteDataProductVersion(String uuid) {
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + uuid));
    }

    private NotificationDispatchRes createNotificationDispatch(String eventType, String resourceType, String resourceIdentifier, JsonNode content) {
        NotificationDispatchRes notification = new NotificationDispatchRes();
        notification.setSequenceId(1L);

        NotificationDispatchRes.NotificationDispatchEventRes event = new NotificationDispatchRes.NotificationDispatchEventRes();
        event.setSequenceId(1L);
        event.setResourceType(resourceType);
        event.setResourceIdentifier(resourceIdentifier);
        event.setType(eventType);
        event.setEventTypeVersion("1.0.0");
        event.setEventContent(content);
        notification.setEvent(event);

        NotificationDispatchRes.NotificationDispatchSubscriptionRes subscription = new NotificationDispatchRes.NotificationDispatchSubscriptionRes();
        subscription.setName("test-observer-policyservice");
        subscription.setDisplayName("Test Observer PolicyService");
        subscription.setObserverBaseUrl("http://localhost:8080");
        subscription.setObserverApiVersion("v2");
        notification.setSubscription(subscription);

        return notification;
    }

    private JsonNode createDataProductContent(DataProductRes dataProduct) {
        ObjectNode content = objectMapper.createObjectNode();
        ObjectNode dataProductNode = objectMapper.createObjectNode();
        dataProductNode.put("uuid", dataProduct.getUuid());
        dataProductNode.put("fqn", dataProduct.getFqn());
        content.set("dataProduct", dataProductNode);
        return content;
    }

    private JsonNode createDataProductVersionContent(DataProductVersionRes dataProductVersion) {
        ObjectNode content = objectMapper.createObjectNode();
        ObjectNode dataProductVersionNode = objectMapper.createObjectNode();
        dataProductVersionNode.put("uuid", dataProductVersion.getUuid());
        dataProductVersionNode.put("tag", dataProductVersion.getTag());

        // Use valid DPDS content from file
        try {
            JsonNode validDpds = loadJsonResource("test-data/dpds-v1.0.0.json");
            dataProductVersionNode.set("content", validDpds);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load DPDS resource", e);
        }

        // Create nested dataProduct object
        ObjectNode dataProductNode = objectMapper.createObjectNode();
        if (dataProductVersion.getDataProduct() != null) {
            dataProductNode.put("uuid", dataProductVersion.getDataProduct().getUuid());
            dataProductNode.put("fqn", dataProductVersion.getDataProduct().getFqn());
        }
        dataProductVersionNode.set("dataProduct", dataProductNode);

        content.set("dataProductVersion", dataProductVersionNode);
        return content;
    }

    private JsonNode loadJsonResource(String path) throws java.io.IOException {
        java.io.File file = org.springframework.util.ResourceUtils.getFile("classpath:" + path);
        return objectMapper.readTree(file);
    }
}