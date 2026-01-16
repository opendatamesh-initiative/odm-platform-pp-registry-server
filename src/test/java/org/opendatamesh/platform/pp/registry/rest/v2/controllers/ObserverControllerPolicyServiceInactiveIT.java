package org.opendatamesh.platform.pp.registry.rest.v2.controllers;

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
import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.EventTypeRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.EventTypeVersion;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.ResourceType;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.autoapprove.EmittedEventDataProductInitializationApprovedRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.autoapprove.EmittedEventDataProductVersionPublicationApprovedRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.notification.NotificationDispatchRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@TestPropertySource(properties = "odm.product-plane.policy-service.active=false")
public class ObserverControllerPolicyServiceInactiveIT extends RegistryApplicationIT {

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
    public void whenReceiveDataProductInitializationRequestedThenDispatchToInitializationApprover() {
        // Given - Create a data product
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("whenReceiveDataProductInitializationRequestedThenDispatchToInitializationApprover-product");
        dataProduct.setDomain("whenReceiveDataProductInitializationRequestedThenDispatchToInitializationApprover-domain");
        dataProduct.setFqn("whenReceiveDataProductInitializationRequestedThenDispatchToInitializationApprover.fqn");
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

        // Verify that the DATA_PRODUCT_INITIALIZATION_APPROVED event was emitted
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(notificationClient).notifyEvent(eventCaptor.capture());
        Object capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent).isInstanceOf(EmittedEventDataProductInitializationApprovedRes.class);
        EmittedEventDataProductInitializationApprovedRes event = (EmittedEventDataProductInitializationApprovedRes) capturedEvent;
        assertThat(event.getResourceType()).isEqualTo(ResourceType.DATA_PRODUCT);
        assertThat(event.getResourceIdentifier()).isEqualTo(dataProductId);
        assertThat(event.getType()).isEqualTo(EventTypeRes.DATA_PRODUCT_INITIALIZATION_APPROVED);
        assertThat(event.getEventTypeVersion()).isEqualTo(EventTypeVersion.V2_0_0.toString());
        assertThat(event.getEventContent()).isNotNull();
        if (event.getEventContent().getDataProduct() != null) {
            assertThat(event.getEventContent().getDataProduct().getUuid()).isEqualTo(dataProductId);
            assertThat(event.getEventContent().getDataProduct().getFqn()).isNotNull();
        }

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
    }

    @Test
    public void whenReceiveDataProductVersionPublicationRequestedThenDispatchToPublicationApprover() {
        // Given - Create a data product first
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("whenReceiveDataProductVersionPublicationRequestedThenDispatchToPublicationApprover-product");
        dataProduct.setDomain("whenReceiveDataProductVersionPublicationRequestedThenDispatchToPublicationApprover-domain");
        dataProduct.setFqn("whenReceiveDataProductVersionPublicationRequestedThenDispatchToPublicationApprover.fqn");
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
        dataProductVersion.setSpec("opendatamesh");
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

        // Verify that the DATA_PRODUCT_VERSION_PUBLICATION_APPROVED event was emitted
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(notificationClient).notifyEvent(eventCaptor.capture());
        Object capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent).isInstanceOf(EmittedEventDataProductVersionPublicationApprovedRes.class);
        EmittedEventDataProductVersionPublicationApprovedRes event = (EmittedEventDataProductVersionPublicationApprovedRes) capturedEvent;
        assertThat(event.getResourceType()).isEqualTo(ResourceType.DATA_PRODUCT_VERSION);
        assertThat(event.getResourceIdentifier()).isEqualTo(versionId);
        assertThat(event.getType()).isEqualTo(EventTypeRes.DATA_PRODUCT_VERSION_PUBLICATION_APPROVED);
        assertThat(event.getEventTypeVersion()).isEqualTo(EventTypeVersion.V2_0_0.toString());
        assertThat(event.getEventContent()).isNotNull();
        if (event.getEventContent().getDataProductVersion() != null) {
            assertThat(event.getEventContent().getDataProductVersion().getUuid()).isEqualTo(versionId);
            assertThat(event.getEventContent().getDataProductVersion().getTag()).isNotNull();
            if (event.getEventContent().getDataProductVersion().getDataProduct() != null) {
                assertThat(event.getEventContent().getDataProductVersion().getDataProduct().getUuid()).isNotNull();
                assertThat(event.getEventContent().getDataProductVersion().getDataProduct().getFqn()).isNotNull();
            }
        }

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + versionId));
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
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

