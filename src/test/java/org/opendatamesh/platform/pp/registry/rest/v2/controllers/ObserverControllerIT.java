package org.opendatamesh.platform.pp.registry.rest.v2.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendatamesh.platform.pp.registry.client.notification.NotificationClient;
import org.opendatamesh.platform.pp.registry.rest.v2.RegistryApplicationIT;
import org.opendatamesh.platform.pp.registry.rest.v2.RoutesV2;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductValidationStateRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionValidationStateRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.notification.NotificationDispatchRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ObserverControllerIT extends RegistryApplicationIT {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private NotificationClient notificationClient;

    @BeforeEach
    public void setUp() {
        reset(notificationClient);
    }

    @AfterEach
    public void tearDown() {
        reset(notificationClient);
    }

    @Test
    public void whenReceiveDataProductInitializationApprovedThenDispatchToApprover() {
        // Given - Create a data product in PENDING state
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("whenReceiveDataProductInitializationApprovedThenDispatchToApprover-product");
        dataProduct.setDomain("whenReceiveDataProductInitializationApprovedThenDispatchToApprover-domain");
        dataProduct.setFqn("whenReceiveDataProductInitializationApprovedThenDispatchToApprover.fqn");
        dataProduct.setDisplayName("Test Display Name");
        dataProduct.setDescription("Test Description");
        dataProduct.setValidationState(DataProductValidationStateRes.PENDING);

        ResponseEntity<DataProductRes> createResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        DataProductRes createdDataProduct = createResponse.getBody();
        String dataProductId = createdDataProduct.getUuid();

        // Create notification dispatch
        NotificationDispatchRes notification = createNotificationDispatch(
                "DATA_PRODUCT_INITIALIZATION_APPROVED",
                "DATA_PRODUCT",
                dataProductId,
                createDataProductContent(createdDataProduct)
        );

        // When
        ResponseEntity<Void> response = rest.postForEntity(
                apiUrlFromString("/api/v2/up/observer/notifications"),
                new HttpEntity<>(notification),
                Void.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Verify that the data product was approved
        ResponseEntity<DataProductRes> getResponse = rest.getForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId),
                DataProductRes.class
        );
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        if (getResponse.getBody() != null) {
            assertThat(getResponse.getBody().getValidationState()).isEqualTo(DataProductValidationStateRes.APPROVED);
        }

        // Verify that notifySuccess was called with the correct notificationId
        verify(notificationClient).processingSuccess(notification.getSequenceId());
        verify(notificationClient, never()).processingFailure(notification.getSequenceId());

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
    }

    @Test
    public void whenReceiveDataProductInitializationRequestedAndPolicyServiceActiveThenNoEventEmitted() {
        // Given - Create a data product
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("whenReceiveDataProductInitializationRequestedAndPolicyServiceActiveThenNoEventEmitted-product");
        dataProduct.setDomain("whenReceiveDataProductInitializationRequestedAndPolicyServiceActiveThenNoEventEmitted-domain");
        dataProduct.setFqn("whenReceiveDataProductInitializationRequestedAndPolicyServiceActiveThenNoEventEmitted.fqn");
        dataProduct.setDisplayName("Test Display Name");
        dataProduct.setDescription("Test Description");
        dataProduct.setValidationState(DataProductValidationStateRes.PENDING);

        ResponseEntity<DataProductRes> createResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        DataProductRes createdDataProduct = createResponse.getBody();
        String dataProductId = createdDataProduct.getUuid();

        // Create notification dispatch
        NotificationDispatchRes notification = createNotificationDispatch(
                "DATA_PRODUCT_INITIALIZATION_REQUESTED",
                "DATA_PRODUCT",
                dataProductId,
                createDataProductContent(createdDataProduct)
        );

        // When
        ResponseEntity<Void> response = rest.postForEntity(
                apiUrlFromString("/api/v2/up/observer/notifications"),
                new HttpEntity<>(notification),
                Void.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Verify that notifySuccess was called with the correct notificationId
        verify(notificationClient).processingSuccess(notification.getSequenceId());
        verify(notificationClient, never()).processingFailure(notification.getSequenceId());

        // Verify that no event was emitted (policy service is active, so auto-approval dispatcher is not instantiated)
        verify(notificationClient, never()).notifyEvent(any());

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
    }

    @Test
    public void whenReceiveDataProductVersionPublicationRequestedAndPolicyServiceActiveThenNoEventEmitted() throws IOException {
        // Given - Create a data product first
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("whenReceiveDataProductVersionPublicationRequestedAndPolicyServiceActiveThenNoEventEmitted-product");
        dataProduct.setDomain("whenReceiveDataProductVersionPublicationRequestedAndPolicyServiceActiveThenNoEventEmitted-domain");
        dataProduct.setFqn("whenReceiveDataProductVersionPublicationRequestedAndPolicyServiceActiveThenNoEventEmitted.fqn");
        dataProduct.setDisplayName("Test Display Name");
        dataProduct.setDescription("Test Description");
        dataProduct.setValidationState(DataProductValidationStateRes.APPROVED);

        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String dataProductId = dataProductResponse.getBody().getUuid();

        // Create a data product version
        DataProductVersionRes dataProductVersion = new DataProductVersionRes();
        dataProductVersion.setName("test-version-publication-requested");
        dataProductVersion.setDescription("Test version description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setVersionNumber("1.0.0");
        dataProductVersion.setValidationState(DataProductVersionValidationStateRes.PENDING);
        dataProductVersion.setDataProduct(dataProductResponse.getBody());
        dataProductVersion.setSpec("dpds");
        dataProductVersion.setSpecVersion("1.0.0");
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "test-version-publication-requested")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);

        ResponseEntity<DataProductVersionRes> versionResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS),
                new HttpEntity<>(dataProductVersion),
                DataProductVersionRes.class
        );
        assertThat(versionResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        DataProductVersionRes createdVersion = versionResponse.getBody();
        String versionId = createdVersion.getUuid();

        // Create notification dispatch
        NotificationDispatchRes notification = createNotificationDispatch(
                "DATA_PRODUCT_VERSION_PUBLICATION_REQUESTED",
                "DATA_PRODUCT_VERSION",
                versionId,
                createDataProductVersionContent(createdVersion)
        );

        // When
        ResponseEntity<Void> response = rest.postForEntity(
                apiUrlFromString("/api/v2/up/observer/notifications"),
                new HttpEntity<>(notification),
                Void.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Verify that notifySuccess was called with the correct notificationId
        verify(notificationClient).processingSuccess(notification.getSequenceId());
        verify(notificationClient, never()).processingFailure(notification.getSequenceId());

        // Verify that no event was emitted (policy service is active, so auto-approval dispatcher is not instantiated)
        verify(notificationClient, never()).notifyEvent(any());

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + versionId));
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
    }

    @Test
    public void whenReceiveDataProductInitializationRejectedThenDispatchToRejector() {
        // Given - Create a data product in PENDING state
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("whenReceiveDataProductInitializationRejectedThenDispatchToRejector-product");
        dataProduct.setDomain("whenReceiveDataProductInitializationRejectedThenDispatchToRejector-domain");
        dataProduct.setFqn("whenReceiveDataProductInitializationRejectedThenDispatchToRejector.fqn");
        dataProduct.setDisplayName("Test Display Name");
        dataProduct.setDescription("Test Description");
        dataProduct.setValidationState(DataProductValidationStateRes.PENDING);

        ResponseEntity<DataProductRes> createResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        DataProductRes createdDataProduct = createResponse.getBody();
        String dataProductId = createdDataProduct.getUuid();

        // Create notification dispatch
        NotificationDispatchRes notification = createNotificationDispatch(
                "DATA_PRODUCT_INITIALIZATION_REJECTED",
                "DATA_PRODUCT",
                dataProductId,
                createDataProductContent(createdDataProduct)
        );

        // When
        ResponseEntity<Void> response = rest.postForEntity(
                apiUrlFromString("/api/v2/up/observer/notifications"),
                new HttpEntity<>(notification),
                Void.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Verify that the data product was rejected
        ResponseEntity<DataProductRes> getResponse = rest.getForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId),
                DataProductRes.class
        );
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        if (getResponse.getBody() != null) {
            assertThat(getResponse.getBody().getValidationState()).isEqualTo(DataProductValidationStateRes.REJECTED);
        }

        // Verify that notifySuccess was called with the correct notificationId
        verify(notificationClient).processingSuccess(notification.getSequenceId());
        verify(notificationClient, never()).processingFailure(notification.getSequenceId());

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
    }

    @Test
    public void whenReceiveDataProductVersionPublicationApprovedThenDispatchToApprover() throws IOException {
        // Given - Create a data product first
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("whenReceiveDataProductVersionPublicationApprovedThenDispatchToApprover-product");
        dataProduct.setDomain("whenReceiveDataProductVersionPublicationApprovedThenDispatchToApprover-domain");
        dataProduct.setFqn("whenReceiveDataProductVersionPublicationApprovedThenDispatchToApprover.fqn");
        dataProduct.setDisplayName("Test Display Name");
        dataProduct.setDescription("Test Description");
        dataProduct.setValidationState(DataProductValidationStateRes.APPROVED);

        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String dataProductId = dataProductResponse.getBody().getUuid();

        // Create a data product version in PENDING state
        DataProductVersionRes dataProductVersion = new DataProductVersionRes();
        dataProductVersion.setName("test-version-approved");
        dataProductVersion.setDescription("Test version description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setVersionNumber("1.0.0");
        dataProductVersion.setValidationState(DataProductVersionValidationStateRes.PENDING);
        dataProductVersion.setDataProduct(dataProductResponse.getBody());
        dataProductVersion.setSpec("dpds");
        dataProductVersion.setSpecVersion("1.0.0");
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "test-version-approved")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);

        ResponseEntity<DataProductVersionRes> versionResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS),
                new HttpEntity<>(dataProductVersion),
                DataProductVersionRes.class
        );
        assertThat(versionResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        DataProductVersionRes createdVersion = versionResponse.getBody();
        String versionId = createdVersion.getUuid();

        // Create notification dispatch
        NotificationDispatchRes notification = createNotificationDispatch(
                "DATA_PRODUCT_VERSION_PUBLICATION_APPROVED",
                "DATA_PRODUCT_VERSION",
                versionId,
                createDataProductVersionContent(createdVersion)
        );

        // When
        ResponseEntity<Void> response = rest.postForEntity(
                apiUrlFromString("/api/v2/up/observer/notifications"),
                new HttpEntity<>(notification),
                Void.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Verify that the data product version was approved
        ResponseEntity<DataProductVersionRes> getResponse = rest.getForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + versionId),
                DataProductVersionRes.class
        );
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        if (getResponse.getBody() != null) {
            assertThat(getResponse.getBody().getValidationState()).isEqualTo(DataProductVersionValidationStateRes.APPROVED);
        }

        // Verify that notifySuccess was called with the correct notificationId
        verify(notificationClient).processingSuccess(notification.getSequenceId());
        verify(notificationClient, never()).processingFailure(notification.getSequenceId());

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + versionId));
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
    }

    @Test
    public void whenReceiveDataProductVersionPublicationRejectedThenDispatchToRejector() throws IOException {
        // Given - Create a data product first
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("whenReceiveDataProductVersionPublicationRejectedThenDispatchToRejector-product");
        dataProduct.setDomain("whenReceiveDataProductVersionPublicationRejectedThenDispatchToRejector-domain");
        dataProduct.setFqn("whenReceiveDataProductVersionPublicationRejectedThenDispatchToRejector.fqn");
        dataProduct.setDisplayName("Test Display Name");
        dataProduct.setDescription("Test Description");
        dataProduct.setValidationState(DataProductValidationStateRes.APPROVED);

        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String dataProductId = dataProductResponse.getBody().getUuid();

        // Create a data product version in PENDING state
        DataProductVersionRes dataProductVersion = new DataProductVersionRes();
        dataProductVersion.setName("test-version-rejected");
        dataProductVersion.setDescription("Test version description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setVersionNumber("1.0.0");
        dataProductVersion.setValidationState(DataProductVersionValidationStateRes.PENDING);
        dataProductVersion.setDataProduct(dataProductResponse.getBody());
        dataProductVersion.setSpec("dpds");
        dataProductVersion.setSpecVersion("1.0.0");
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "test-version-rejected")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);

        ResponseEntity<DataProductVersionRes> versionResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS),
                new HttpEntity<>(dataProductVersion),
                DataProductVersionRes.class
        );
        assertThat(versionResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        DataProductVersionRes createdVersion = versionResponse.getBody();
        String versionId = createdVersion.getUuid();

        // Create notification dispatch
        NotificationDispatchRes notification = createNotificationDispatch(
                "DATA_PRODUCT_VERSION_PUBLICATION_REJECTED",
                "DATA_PRODUCT_VERSION",
                versionId,
                createDataProductVersionContent(createdVersion)
        );

        // When
        ResponseEntity<Void> response = rest.postForEntity(
                apiUrlFromString("/api/v2/up/observer/notifications"),
                new HttpEntity<>(notification),
                Void.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Verify that the data product version was rejected
        ResponseEntity<DataProductVersionRes> getResponse = rest.getForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + versionId),
                DataProductVersionRes.class
        );
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        if (getResponse.getBody() != null) {
            assertThat(getResponse.getBody().getValidationState()).isEqualTo(DataProductVersionValidationStateRes.REJECTED);
        }

        // Verify that notifySuccess was called with the correct notificationId
        verify(notificationClient).processingSuccess(notification.getSequenceId());
        verify(notificationClient, never()).processingFailure(notification.getSequenceId());

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + versionId));
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
    }

    @Test
    public void whenReceiveUnsupportedEventTypeThenProcessingSuccess() {
        // Given - Create a notification dispatch with a valid event type that has no dispatcher
        // Using an emitted event type (DATA_PRODUCT_INITIALIZED) which is valid but has no dispatcher
        NotificationDispatchRes notification = createNotificationDispatch(
                "DATA_PRODUCT_INITIALIZED",
                "DATA_PRODUCT",
                "test-resource-id",
                objectMapper.createObjectNode()
        );

        // When
        ResponseEntity<Void> response = rest.postForEntity(
                apiUrlFromString("/api/v2/up/observer/notifications"),
                new HttpEntity<>(notification),
                Void.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Verify that processingSuccess was called since no dispatcher was found
        verify(notificationClient).processingSuccess(notification.getSequenceId());
        verify(notificationClient, never()).processingFailure(notification.getSequenceId());
    }

    @Test
    public void whenReceiveInvalidEventTypeThenProcessingSuccess() {
        // Given - Create a notification dispatch with an invalid event type that cannot be converted to EventTypeRes
        NotificationDispatchRes notification = createNotificationDispatch(
                "INVALID_EVENT_TYPE_THAT_DOES_NOT_EXIST",
                "DATA_PRODUCT",
                "test-resource-id",
                objectMapper.createObjectNode()
        );

        // When
        ResponseEntity<Void> response = rest.postForEntity(
                apiUrlFromString("/api/v2/up/observer/notifications"),
                new HttpEntity<>(notification),
                Void.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Verify that processingSuccess was called since invalid event type is handled gracefully
        verify(notificationClient).processingSuccess(notification.getSequenceId());
        verify(notificationClient, never()).processingFailure(notification.getSequenceId());
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
        subscription.setName("test-observer");
        subscription.setDisplayName("Test Observer");
        subscription.setObserverBaseUrl("http://localhost:8080");
        subscription.setObserverApiVersion("v2");
        notification.setSubscription(subscription);

        return notification;
    }

    private JsonNode createDataProductContent(DataProductRes dataProduct) {
        JsonNode content = objectMapper.createObjectNode();
        JsonNode dataProductNode = objectMapper.createObjectNode();
        ((com.fasterxml.jackson.databind.node.ObjectNode) dataProductNode).put("uuid", dataProduct.getUuid());
        ((com.fasterxml.jackson.databind.node.ObjectNode) dataProductNode).put("fqn", dataProduct.getFqn());
        ((com.fasterxml.jackson.databind.node.ObjectNode) content).set("dataProduct", dataProductNode);
        return content;
    }

    private JsonNode createDataProductVersionContent(DataProductVersionRes dataProductVersion) {
        JsonNode content = objectMapper.createObjectNode();
        JsonNode dataProductVersionNode = objectMapper.createObjectNode();
        ((com.fasterxml.jackson.databind.node.ObjectNode) dataProductVersionNode).put("uuid", dataProductVersion.getUuid());
        ((com.fasterxml.jackson.databind.node.ObjectNode) dataProductVersionNode).put("tag", dataProductVersion.getTag());
        
        // Create nested dataProduct object
        JsonNode dataProductNode = objectMapper.createObjectNode();
        if (dataProductVersion.getDataProduct() != null) {
            ((com.fasterxml.jackson.databind.node.ObjectNode) dataProductNode).put("uuid", dataProductVersion.getDataProduct().getUuid());
            ((com.fasterxml.jackson.databind.node.ObjectNode) dataProductNode).put("fqn", dataProductVersion.getDataProduct().getFqn());
        }
        ((com.fasterxml.jackson.databind.node.ObjectNode) dataProductVersionNode).set("dataProduct", dataProductNode);
        
        ((com.fasterxml.jackson.databind.node.ObjectNode) content).set("dataProductVersion", dataProductVersionNode);
        return content;
    }
}

