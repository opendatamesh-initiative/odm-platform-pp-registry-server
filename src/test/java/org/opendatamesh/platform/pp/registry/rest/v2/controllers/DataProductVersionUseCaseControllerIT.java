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
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.events.emitted.EmittedEventDataProductVersionDeletedRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.events.emitted.EmittedEventDataProductVersionPublicationRequestedRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.events.emitted.EmittedEventDataProductVersionPublishedRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.approve.DataProductVersionApproveCommandRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.approve.DataProductVersionApproveResultRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.delete.DataProductVersionDeleteCommandRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.documentationfieldsupdate.DataProductVersionDocumentationFieldsRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.documentationfieldsupdate.DataProductVersionDocumentationFieldsUpdateCommandRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.documentationfieldsupdate.DataProductVersionDocumentationFieldsUpdateResultRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.publish.DataProductVersionPublishCommandRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.publish.DataProductVersionPublishResultRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.reject.DataProductVersionRejectCommandRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.reject.DataProductVersionRejectResultRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.EventTypeRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.EventTypeVersion;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.ResourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class DataProductVersionUseCaseControllerIT extends RegistryApplicationIT {

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

    // ========== PUBLISH ENDPOINT TESTS ==========

    @Test
    public void whenPublishDataProductVersionWithValidDataThenReturnCreatedDataProductVersion() {
        // Given - First create a data product
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-publish-product");
        dataProduct.setDomain("test-publish-domain");
        dataProduct.setFqn("test-publish-domain:test-publish-product");
        dataProduct.setDisplayName("test-publish-product Display Name");
        dataProduct.setDescription("Test Description for test-publish-product");
        dataProduct.setValidationState(DataProductValidationStateRes.APPROVED);

        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        DataProductRes createdDataProduct = dataProductResponse.getBody();
        
        // Create data product version
        DataProductVersionRes expectedDataProductVersion = new DataProductVersionRes();
        expectedDataProductVersion.setDataProduct(createdDataProduct);
        expectedDataProductVersion.setName("Test Version");
        expectedDataProductVersion.setDescription("Test Version Description");
        expectedDataProductVersion.setTag("v1.0.0");
        expectedDataProductVersion.setSpec("opendatamesh");
        expectedDataProductVersion.setSpecVersion("1.0.0");
        expectedDataProductVersion.setCreatedBy("createdUser");
        expectedDataProductVersion.setUpdatedBy("updatedUser");

        // Create a simple JSON content
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        expectedDataProductVersion.setContent(content);
        
        DataProductVersionPublishCommandRes publishCommand = new DataProductVersionPublishCommandRes();
        publishCommand.setDataProductVersion(expectedDataProductVersion);

        // When
        ResponseEntity<DataProductVersionPublishResultRes> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/publish"),
                new HttpEntity<>(publishCommand),
                DataProductVersionPublishResultRes.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDataProductVersion()).isNotNull();
        
        // Verify the response contains the expected values
        DataProductVersionRes actualDataProductVersion = response.getBody().getDataProductVersion();
        assertThat(actualDataProductVersion.getUuid()).isNotNull();
        assertThat(actualDataProductVersion.getName()).isEqualTo(expectedDataProductVersion.getName());
        assertThat(actualDataProductVersion.getDescription()).isEqualTo(expectedDataProductVersion.getDescription());
        assertThat(actualDataProductVersion.getTag()).isEqualTo(expectedDataProductVersion.getTag());
        assertThat(actualDataProductVersion.getSpec()).isEqualTo(expectedDataProductVersion.getSpec());
        assertThat(actualDataProductVersion.getSpecVersion()).isEqualTo(expectedDataProductVersion.getSpecVersion());
        assertThat(actualDataProductVersion.getValidationState()).isEqualTo(DataProductVersionValidationStateRes.PENDING);

        // Verify notification was sent
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(notificationClient).notifyEvent(eventCaptor.capture());
        Object capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent).isInstanceOf(EmittedEventDataProductVersionPublicationRequestedRes.class);
        EmittedEventDataProductVersionPublicationRequestedRes event = (EmittedEventDataProductVersionPublicationRequestedRes) capturedEvent;
        assertThat(event.getResourceType()).isEqualTo(ResourceType.DATA_PRODUCT_VERSION);
        assertThat(event.getResourceIdentifier()).isEqualTo(actualDataProductVersion.getUuid());
        assertThat(event.getType()).isEqualTo(EventTypeRes.DATA_PRODUCT_VERSION_PUBLICATION_REQUESTED);
        assertThat(event.getEventTypeVersion()).isEqualTo(EventTypeVersion.V2_0_0);
        assertThat(event.getEventContent()).isNotNull();
        assertThat(event.getEventContent().getDataProductVersion()).isNotNull();
        assertThat(event.getEventContent().getDataProductVersion().getUuid()).isEqualTo(actualDataProductVersion.getUuid());
        assertThat(event.getEventContent().getDataProductVersion().getTag()).isEqualTo(expectedDataProductVersion.getTag());

        // Cleanup
        cleanupDataProduct(createdDataProduct.getUuid());
    }

    @Test
    public void whenPublishDataProductVersionWithNullDataProductVersionThenReturnBadRequest() {
        // Given
        DataProductVersionPublishCommandRes publishCommand = new DataProductVersionPublishCommandRes();
        publishCommand.setDataProductVersion(null);

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/publish"),
                new HttpEntity<>(publishCommand),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void whenPublishDataProductVersionWithNullDataProductThenReturnBadRequest() {
        // Given
        DataProductVersionRes dataProductVersion = new DataProductVersionRes();
        dataProductVersion.setDataProduct(null);
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        
        DataProductVersionPublishCommandRes publishCommand = new DataProductVersionPublishCommandRes();
        publishCommand.setDataProductVersion(dataProductVersion);

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/publish"),
                new HttpEntity<>(publishCommand),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void whenPublishDataProductVersionWithNullNameThenReturnBadRequest() {
        // Given - First create a data product
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-publish-product-null-name");
        dataProduct.setDomain("test-publish-domain-null-name");
        dataProduct.setFqn("test-publish-domain-null-name:test-publish-product-null-name");
        dataProduct.setDisplayName("test-publish-product-null-name Display Name");
        dataProduct.setDescription("Test Description for test-publish-product-null-name");

        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        DataProductRes createdDataProduct = dataProductResponse.getBody();
        
        DataProductVersionRes dataProductVersion = new DataProductVersionRes();
        dataProductVersion.setDataProduct(createdDataProduct);
        dataProductVersion.setName(null);
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        
        DataProductVersionPublishCommandRes publishCommand = new DataProductVersionPublishCommandRes();
        publishCommand.setDataProductVersion(dataProductVersion);

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/publish"),
                new HttpEntity<>(publishCommand),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Cleanup
        cleanupDataProduct(createdDataProduct.getUuid());
    }

    @Test
    public void whenPublishDataProductVersionWithNullTagThenReturnBadRequest() {
        // Given - First create a data product
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-publish-product-null-tag");
        dataProduct.setDomain("test-publish-domain-null-tag");
        dataProduct.setFqn("test-publish-domain-null-tag:test-publish-product-null-tag");
        dataProduct.setDisplayName("test-publish-product-null-tag Display Name");
        dataProduct.setDescription("Test Description for test-publish-product-null-tag");

        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        DataProductRes createdDataProduct = dataProductResponse.getBody();
        
        DataProductVersionRes dataProductVersion = new DataProductVersionRes();
        dataProductVersion.setDataProduct(createdDataProduct);
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag(null);
        
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        
        DataProductVersionPublishCommandRes publishCommand = new DataProductVersionPublishCommandRes();
        publishCommand.setDataProductVersion(dataProductVersion);

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/publish"),
                new HttpEntity<>(publishCommand),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Cleanup
        cleanupDataProduct(createdDataProduct.getUuid());
    }

    @Test
    public void whenPublishDataProductVersionWithNullContentThenReturnBadRequest() {
        // Given - First create a data product
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-publish-product-null-content");
        dataProduct.setDomain("test-publish-domain-null-content");
        dataProduct.setFqn("test-publish-domain-null-content:test-publish-product-null-content");
        dataProduct.setDisplayName("test-publish-product-null-content Display Name");
        dataProduct.setDescription("Test Description for test-publish-product-null-content");

        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        DataProductRes createdDataProduct = dataProductResponse.getBody();
        
        DataProductVersionRes dataProductVersion = new DataProductVersionRes();
        dataProductVersion.setDataProduct(createdDataProduct);
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setContent(null);
        
        DataProductVersionPublishCommandRes publishCommand = new DataProductVersionPublishCommandRes();
        publishCommand.setDataProductVersion(dataProductVersion);

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/publish"),
                new HttpEntity<>(publishCommand),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Cleanup
        cleanupDataProduct(createdDataProduct.getUuid());
    }

    @Test
    public void whenPublishDataProductVersionWithExistingPendingVersionThenReturnBadRequest() {
        // Given - First create a data product
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-publish-product");
        dataProduct.setDomain("test-publish-domain");
        dataProduct.setFqn("test-publish-domain:test-publish-product");
        dataProduct.setDisplayName("test-publish-product Display Name");
        dataProduct.setDescription("Test Description for test-publish-product");
        dataProduct.setValidationState(DataProductValidationStateRes.APPROVED);
        
        // Create the data product first
        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        DataProductRes createdDataProduct = dataProductResponse.getBody();
        
        // Publish first version
        DataProductVersionRes firstVersion = new DataProductVersionRes();
        firstVersion.setDataProduct(createdDataProduct);
        firstVersion.setName("Test Version 1");
        firstVersion.setDescription("Test Version 1 Description");
        firstVersion.setTag("v1.0.0");
        firstVersion.setSpec("opendatamesh");
        firstVersion.setSpecVersion("1.0.0");
        
        // Create a simple JSON content
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version 1")
                .put("version", "1.0.0");
        firstVersion.setContent(content);
        DataProductVersionPublishCommandRes firstPublishCommand = new DataProductVersionPublishCommandRes();
        firstPublishCommand.setDataProductVersion(firstVersion);

        ResponseEntity<DataProductVersionPublishResultRes> firstResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/publish"),
                new HttpEntity<>(firstPublishCommand),
                DataProductVersionPublishResultRes.class
        );

        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Try to publish another version with the same tag
        DataProductVersionRes secondVersion = new DataProductVersionRes();
        secondVersion.setDataProduct(createdDataProduct);
        secondVersion.setName("Test Version 2");
        secondVersion.setDescription("Test Version 2 Description");
        secondVersion.setTag("v1.0.0"); // Same tag
        secondVersion.setSpec("opendatamesh");
        secondVersion.setSpecVersion("1.0.0");
        
        // Create a simple JSON content
        JsonNode content2 = objectMapper.createObjectNode()
                .put("name", "Test Version 2")
                .put("version", "1.0.0");
        secondVersion.setContent(content2);
        DataProductVersionPublishCommandRes secondPublishCommand = new DataProductVersionPublishCommandRes();
        secondPublishCommand.setDataProductVersion(secondVersion);

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/publish"),
                new HttpEntity<>(secondPublishCommand),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Cleanup
        cleanupDataProduct(dataProduct.getUuid());
    }

    @Test
    public void whenPublishDataProductVersionWithNonApprovedDataProductThenReturnBadRequest() {
        // Given - First create a data product (it will be in PENDING state by default)
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-publish-non-approved-product");
        dataProduct.setDomain("test-publish-non-approved-domain");
        dataProduct.setFqn("test-publish-non-approved-domain:test-publish-non-approved-product");
        dataProduct.setDisplayName("test-publish-non-approved-product Display Name");
        dataProduct.setDescription("Test Description for test-publish-non-approved-product");
        dataProduct.setValidationState(DataProductValidationStateRes.PENDING);

        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        DataProductRes createdDataProduct = dataProductResponse.getBody();
        
        // Verify the data product is in PENDING state (not approved)
        assertThat(createdDataProduct.getValidationState()).isEqualTo(DataProductValidationStateRes.PENDING);
        
        // Create data product version
        DataProductVersionRes dataProductVersion = new DataProductVersionRes();
        dataProductVersion.setDataProduct(createdDataProduct);
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setSpec("opendatamesh");
        dataProductVersion.setSpecVersion("1.0.0");
        
        // Create a simple JSON content
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        
        DataProductVersionPublishCommandRes publishCommand = new DataProductVersionPublishCommandRes();
        publishCommand.setDataProductVersion(dataProductVersion);

        // When - Try to publish the data product version
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/publish"),
                new HttpEntity<>(publishCommand),
                String.class
        );

        // Then - Should return Bad Request because the data product is not approved
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("must be APPROVED in order to publish a Data Product Version");

        // Cleanup
        cleanupDataProduct(createdDataProduct.getUuid());
    }

    // ========== UPDATE DOCUMENTATION FIELDS ENDPOINT TESTS ==========

    @Test
    public void whenUpdateDataProductVersionWithValidDataThenReturnUpdatedDataProductVersion(){
        // Given - First create a data product
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-publish-update-product");
        dataProduct.setDomain("test-publish-update-domain");
        dataProduct.setFqn("test-publish-update-domain:test-publish-product");
        dataProduct.setDisplayName("test-publish-update-product Display Name");
        dataProduct.setDescription("Test Description for test-publish-update-product");
        dataProduct.setValidationState(DataProductValidationStateRes.APPROVED);

        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        DataProductRes createdDataProduct = dataProductResponse.getBody();

        // Create data product version to be modified
        DataProductVersionRes expectedDataProductVersion = new DataProductVersionRes();
        expectedDataProductVersion.setDataProduct(createdDataProduct);
        expectedDataProductVersion.setName("Test Version");
        expectedDataProductVersion.setDescription("Test Version Description");
        expectedDataProductVersion.setTag("v1.0.0");
        expectedDataProductVersion.setSpec("opendatamesh");
        expectedDataProductVersion.setSpecVersion("1.0.0");
        expectedDataProductVersion.setCreatedBy("createdUser");
        expectedDataProductVersion.setUpdatedBy("updatedUser");

        // Create a simple JSON content
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        expectedDataProductVersion.setContent(content);

        DataProductVersionPublishCommandRes publishCommand = new DataProductVersionPublishCommandRes();
        publishCommand.setDataProductVersion(expectedDataProductVersion);

        // When
        ResponseEntity<DataProductVersionPublishResultRes> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/publish"),
                new HttpEntity<>(publishCommand),
                DataProductVersionPublishResultRes.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDataProductVersion()).isNotNull();
        DataProductVersionRes publishedVersion = response.getBody().getDataProductVersion();

        // Create data product version to update the previous
        DataProductVersionDocumentationFieldsRes updatedDataProductVersion = new DataProductVersionDocumentationFieldsRes();
        updatedDataProductVersion.setUuid(publishedVersion.getUuid()); // Set UUID from published version
        updatedDataProductVersion.setName("Test Version updated");
        updatedDataProductVersion.setDescription("Test Version Description Updated");
        updatedDataProductVersion.setUpdatedBy("updatedUserUpdated");

        // Set content (required by validation)
        JsonNode updatedContent = objectMapper.createObjectNode()
                .put("name", "Test Version updated")
                .put("version", "1.0.0");

        DataProductVersionDocumentationFieldsUpdateCommandRes updateCommand = new DataProductVersionDocumentationFieldsUpdateCommandRes();
        updateCommand.setDataProductVersion(updatedDataProductVersion);

        // When
        ResponseEntity<DataProductVersionDocumentationFieldsUpdateResultRes> responseUpdate = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/update-documentation-fields"),
                new HttpEntity<>(updateCommand),
                DataProductVersionDocumentationFieldsUpdateResultRes.class
        );

        // Then
        assertThat(responseUpdate.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseUpdate.getBody()).isNotNull();
        assertThat(responseUpdate.getBody().getDataProductVersion()).isNotNull();

        // Verify the response contains the expected values
        DataProductVersionRes actualDataProductVersion = responseUpdate.getBody().getDataProductVersion();
        assertThat(actualDataProductVersion.getUuid()).isNotNull();
        assertThat(actualDataProductVersion.getName()).isEqualTo(updatedDataProductVersion.getName());
        assertThat(actualDataProductVersion.getDescription()).isEqualTo(updatedDataProductVersion.getDescription());

        // Cleanup
        cleanupDataProduct(createdDataProduct.getUuid());

    }

    @Test
    public void whenUpdateDataProductVersionWithNullNameThenReturnBadRequest(){
        // Given - First create a data product
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-publish-update-nullname-product");
        dataProduct.setDomain("test-publish-update-nullname-domain");
        dataProduct.setFqn("test-publish-update-nullname-domain:test-publish-product");
        dataProduct.setDisplayName("test-publish-update-nullname-product Display Name");
        dataProduct.setDescription("Test Description for test-publish-update-nullname-product");
        dataProduct.setValidationState(DataProductValidationStateRes.APPROVED);

        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        DataProductRes createdDataProduct = dataProductResponse.getBody();

        // Create data product version to be modified
        DataProductVersionRes expectedDataProductVersion = new DataProductVersionRes();
        expectedDataProductVersion.setDataProduct(createdDataProduct);
        expectedDataProductVersion.setName("Test Version");
        expectedDataProductVersion.setDescription("Test Version Description");
        expectedDataProductVersion.setTag("v1.0.0");
        expectedDataProductVersion.setSpec("opendatamesh");
        expectedDataProductVersion.setSpecVersion("1.0.0");
        expectedDataProductVersion.setCreatedBy("createdUser");
        expectedDataProductVersion.setUpdatedBy("updatedUser");

        // Create a simple JSON content
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        expectedDataProductVersion.setContent(content);

        DataProductVersionPublishCommandRes publishCommand = new DataProductVersionPublishCommandRes();
        publishCommand.setDataProductVersion(expectedDataProductVersion);

        // When
        ResponseEntity<DataProductVersionPublishResultRes> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/publish"),
                new HttpEntity<>(publishCommand),
                DataProductVersionPublishResultRes.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDataProductVersion()).isNotNull();
        DataProductVersionRes publishedVersion = response.getBody().getDataProductVersion();

        // Create data product version to update the previous
        DataProductVersionDocumentationFieldsRes updatedDataProductVersion = new DataProductVersionDocumentationFieldsRes();
        updatedDataProductVersion.setUuid(publishedVersion.getUuid()); // Set UUID from published version
        updatedDataProductVersion.setName(null);
        updatedDataProductVersion.setDescription("Test Version Description Updated");
        updatedDataProductVersion.setUpdatedBy("updatedUserUpdated");

        // Set content (required by validation)
        JsonNode updatedContent = objectMapper.createObjectNode()
                .put("name", "Test Version updated")
                .put("version", "1.0.0");

        DataProductVersionDocumentationFieldsUpdateCommandRes updateCommand = new DataProductVersionDocumentationFieldsUpdateCommandRes();
        updateCommand.setDataProductVersion(updatedDataProductVersion);

        // When
        ResponseEntity<String> responseUpdate = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/update-documentation-fields"),
                new HttpEntity<>(updateCommand),
                String.class
        );

        // Then
        assertThat(responseUpdate.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseUpdate.getBody()).contains("Version name is required for data product version documentation fields update");

        // Cleanup
        cleanupDataProduct(createdDataProduct.getUuid());
    }

    @Test
    public void whenUpdateDataProductVersionWithNoExistingUuidThenReturnNotFound(){
        // Given - First create a data product
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-publish-update-erroruuid-product");
        dataProduct.setDomain("test-publish-update-erroruuid-domain");
        dataProduct.setFqn("test-publish-update-erroruuid-domain:test-publish-product");
        dataProduct.setDisplayName("test-publish-update-erroruuid-product Display Name");
        dataProduct.setDescription("Test Description for test-publish-update-erroruuid-product");
        dataProduct.setValidationState(DataProductValidationStateRes.APPROVED);

        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        DataProductRes createdDataProduct = dataProductResponse.getBody();

        // Create data product version to be modified
        DataProductVersionRes expectedDataProductVersion = new DataProductVersionRes();
        expectedDataProductVersion.setDataProduct(createdDataProduct);
        expectedDataProductVersion.setName("Test Version");
        expectedDataProductVersion.setDescription("Test Version Description");
        expectedDataProductVersion.setTag("v1.0.0");
        expectedDataProductVersion.setSpec("opendatamesh");
        expectedDataProductVersion.setSpecVersion("1.0.0");
        expectedDataProductVersion.setCreatedBy("createdUser");
        expectedDataProductVersion.setUpdatedBy("updatedUser");

        // Create a simple JSON content
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        expectedDataProductVersion.setContent(content);

        DataProductVersionPublishCommandRes publishCommand = new DataProductVersionPublishCommandRes();
        publishCommand.setDataProductVersion(expectedDataProductVersion);

        // When
        ResponseEntity<DataProductVersionPublishResultRes> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/publish"),
                new HttpEntity<>(publishCommand),
                DataProductVersionPublishResultRes.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDataProductVersion()).isNotNull();
        DataProductVersionRes publishedVersion = response.getBody().getDataProductVersion();

        // Create data product version to update the previous
        DataProductVersionDocumentationFieldsRes updatedDataProductVersion = new DataProductVersionDocumentationFieldsRes();
        updatedDataProductVersion.setUuid("uuid-error");
        updatedDataProductVersion.setName("Test Version Name updated");
        updatedDataProductVersion.setDescription("Test Version Description Updated");
        updatedDataProductVersion.setUpdatedBy("updatedUserUpdated");

        // Set content (required by validation)
        JsonNode updatedContent = objectMapper.createObjectNode()
                .put("name", "Test Version updated")
                .put("version", "1.0.0");

        DataProductVersionDocumentationFieldsUpdateCommandRes updateCommand = new DataProductVersionDocumentationFieldsUpdateCommandRes();
        updateCommand.setDataProductVersion(updatedDataProductVersion);

        // When
        ResponseEntity<String> responseUpdate = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/update-documentation-fields"),
                new HttpEntity<>(updateCommand),
                String.class
        );

        // Then
        assertThat(responseUpdate.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // Cleanup
        cleanupDataProduct(createdDataProduct.getUuid());
    }

    @Test
    public void whenUpdateDataProductVersionWithNullUuidThenReturnBadRequest(){
        // Given - First create a data product
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-publish-update-nulluuid-product");
        dataProduct.setDomain("test-publish-update-nulluuid-domain");
        dataProduct.setFqn("test-publish-update-nulluuid-domain:test-publish-product");
        dataProduct.setDisplayName("test-publish-update-nulluuid-product Display Name");
        dataProduct.setDescription("Test Description for test-publish-update-nulluuid-product");
        dataProduct.setValidationState(DataProductValidationStateRes.APPROVED);

        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        DataProductRes createdDataProduct = dataProductResponse.getBody();

        // Create data product version to be modified
        DataProductVersionRes expectedDataProductVersion = new DataProductVersionRes();
        expectedDataProductVersion.setDataProduct(createdDataProduct);
        expectedDataProductVersion.setName("Test Version");
        expectedDataProductVersion.setDescription("Test Version Description");
        expectedDataProductVersion.setTag("v1.0.0");
        expectedDataProductVersion.setSpec("opendatamesh");
        expectedDataProductVersion.setSpecVersion("1.0.0");
        expectedDataProductVersion.setCreatedBy("createdUser");
        expectedDataProductVersion.setUpdatedBy("updatedUser");

        // Create a simple JSON content
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        expectedDataProductVersion.setContent(content);

        DataProductVersionPublishCommandRes publishCommand = new DataProductVersionPublishCommandRes();
        publishCommand.setDataProductVersion(expectedDataProductVersion);

        // When
        ResponseEntity<DataProductVersionPublishResultRes> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/publish"),
                new HttpEntity<>(publishCommand),
                DataProductVersionPublishResultRes.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDataProductVersion()).isNotNull();
        DataProductVersionRes publishedVersion = response.getBody().getDataProductVersion();

        // Create data product version to update the previous
        DataProductVersionDocumentationFieldsRes updatedDataProductVersion = new DataProductVersionDocumentationFieldsRes();
        updatedDataProductVersion.setUuid(null);
        updatedDataProductVersion.setName("Test Version Name updated");
        updatedDataProductVersion.setDescription("Test Version Description Updated");
        updatedDataProductVersion.setUpdatedBy("updatedUserUpdated");

        DataProductVersionDocumentationFieldsUpdateCommandRes updateCommand = new DataProductVersionDocumentationFieldsUpdateCommandRes();
        updateCommand.setDataProductVersion(updatedDataProductVersion);

        // When
        ResponseEntity<String> responseUpdate = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/update-documentation-fields"),
                new HttpEntity<>(updateCommand),
                String.class
        );

        // Then
        assertThat(responseUpdate.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseUpdate.getBody()).contains("UUID is required for data product version documentation fields update");

        // Cleanup
        cleanupDataProduct(createdDataProduct.getUuid());
    }

    // ========== APPROVE ENDPOINT TESTS ==========

    @Test
    public void whenApproveDataProductVersionWithValidDataThenReturnApprovedDataProductVersion() {
        // Given - First create and publish a data product version
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-approve-product");
        dataProduct.setDomain("test-approve-domain");
        dataProduct.setFqn("test-approve-domain:test-approve-product");
        dataProduct.setDisplayName("test-approve-product Display Name");
        dataProduct.setDescription("Test Description for test-approve-product");
        dataProduct.setValidationState(DataProductValidationStateRes.APPROVED);
        
        // Create the data product first
        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        DataProductRes createdDataProduct = dataProductResponse.getBody();
        
        // Create and publish a data product version
        DataProductVersionRes dataProductVersion = new DataProductVersionRes();
        dataProductVersion.setDataProduct(createdDataProduct);
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setSpec("opendatamesh");
        dataProductVersion.setSpecVersion("1.0.0");
        
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        
        DataProductVersionPublishCommandRes publishCommand = new DataProductVersionPublishCommandRes();
        publishCommand.setDataProductVersion(dataProductVersion);

        ResponseEntity<DataProductVersionPublishResultRes> publishResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/publish"),
                new HttpEntity<>(publishCommand),
                DataProductVersionPublishResultRes.class
        );
        assertThat(publishResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        DataProductVersionRes publishedVersion = publishResponse.getBody().getDataProductVersion();
        
        DataProductVersionApproveCommandRes approveCommand = new DataProductVersionApproveCommandRes();
        approveCommand.setDataProductVersion(publishedVersion);

        // When
        ResponseEntity<DataProductVersionApproveResultRes> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/approve"),
                new HttpEntity<>(approveCommand),
                DataProductVersionApproveResultRes.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDataProductVersion()).isNotNull();
        
        // Verify the response contains the expected values
        DataProductVersionRes actualDataProductVersion = response.getBody().getDataProductVersion();
        assertThat(actualDataProductVersion.getUuid()).isEqualTo(publishedVersion.getUuid());
        assertThat(actualDataProductVersion.getValidationState()).isEqualTo(DataProductVersionValidationStateRes.APPROVED);

        // Verify notifications were sent (publish and approve)
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(notificationClient, times(2)).notifyEvent(eventCaptor.capture());
        java.util.List<Object> capturedEvents = eventCaptor.getAllValues();
        // Verify the last event (approve notification)
        Object capturedEvent = capturedEvents.get(capturedEvents.size() - 1);
        assertThat(capturedEvent).isInstanceOf(EmittedEventDataProductVersionPublishedRes.class);
        EmittedEventDataProductVersionPublishedRes event = (EmittedEventDataProductVersionPublishedRes) capturedEvent;
        assertThat(event.getResourceType()).isEqualTo(ResourceType.DATA_PRODUCT_VERSION);
        assertThat(event.getResourceIdentifier()).isEqualTo(publishedVersion.getUuid());
        assertThat(event.getType()).isEqualTo(EventTypeRes.DATA_PRODUCT_VERSION_PUBLISHED);
        assertThat(event.getEventTypeVersion()).isEqualTo(EventTypeVersion.V2_0_0);
        assertThat(event.getEventContent()).isNotNull();
        assertThat(event.getEventContent().getDataProductVersion()).isNotNull();
        assertThat(event.getEventContent().getDataProductVersion().getUuid()).isEqualTo(publishedVersion.getUuid());
        assertThat(event.getEventContent().getDataProductVersion().getValidationState()).isEqualTo(DataProductVersionValidationStateRes.APPROVED);

        // Cleanup
        cleanupDataProduct(createdDataProduct.getUuid());
    }

    @Test
    public void whenApproveDataProductVersionWithNullDataProductVersionThenReturnBadRequest() {
        // Given
        DataProductVersionApproveCommandRes approveCommand = new DataProductVersionApproveCommandRes();
        approveCommand.setDataProductVersion(null);

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/approve"),
                new HttpEntity<>(approveCommand),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void whenApproveDataProductVersionWithNullUuidThenReturnBadRequest() {
        // Given
        DataProductVersionRes dataProductVersion = new DataProductVersionRes();
        dataProductVersion.setUuid(null);
        
        DataProductVersionApproveCommandRes approveCommand = new DataProductVersionApproveCommandRes();
        approveCommand.setDataProductVersion(dataProductVersion);

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/approve"),
                new HttpEntity<>(approveCommand),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void whenApproveAlreadyApprovedDataProductVersionThenReturnBadRequest() {
        // Given - First create, publish and approve a data product version
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-approve-product-2");
        dataProduct.setDomain("test-approve-domain-2");
        dataProduct.setFqn("test-approve-domain-2:test-approve-product-2");
        dataProduct.setDisplayName("test-approve-product Display Name");
        dataProduct.setDescription("Test Description for test-approve-product");
        dataProduct.setValidationState(DataProductValidationStateRes.APPROVED);
        
        // Create the data product first
        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        DataProductRes createdDataProduct = dataProductResponse.getBody();
        
        // Create and publish a data product version
        DataProductVersionRes dataProductVersion = new DataProductVersionRes();
        dataProductVersion.setDataProduct(createdDataProduct);
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setSpec("opendatamesh");
        dataProductVersion.setSpecVersion("1.0.0");
        
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        
        DataProductVersionPublishCommandRes publishCommand = new DataProductVersionPublishCommandRes();
        publishCommand.setDataProductVersion(dataProductVersion);

        ResponseEntity<DataProductVersionPublishResultRes> publishResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/publish"),
                new HttpEntity<>(publishCommand),
                DataProductVersionPublishResultRes.class
        );
        assertThat(publishResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        DataProductVersionRes publishedVersion = publishResponse.getBody().getDataProductVersion();
        
        // Approve the published version
        DataProductVersionApproveCommandRes approveCommand = new DataProductVersionApproveCommandRes();
        approveCommand.setDataProductVersion(publishedVersion);

        ResponseEntity<DataProductVersionApproveResultRes> approveResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/approve"),
                new HttpEntity<>(approveCommand),
                DataProductVersionApproveResultRes.class
        );
        assertThat(approveResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        DataProductVersionRes approvedVersion = approveResponse.getBody().getDataProductVersion();
        
        DataProductVersionApproveCommandRes approveCommand2 = new DataProductVersionApproveCommandRes();
        approveCommand2.setDataProductVersion(approvedVersion);

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/approve"),
                new HttpEntity<>(approveCommand2),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Cleanup
        cleanupDataProduct(createdDataProduct.getUuid());
    }

    // ========== REJECT ENDPOINT TESTS ==========

    @Test
    public void whenRejectDataProductVersionWithValidDataThenReturnRejectedDataProductVersion() {
        // Given - First create and publish a data product version
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-reject-product");
        dataProduct.setDomain("test-reject-domain");
        dataProduct.setFqn("test-reject-domain:test-reject-product");
        dataProduct.setDisplayName("test-reject-product Display Name");
        dataProduct.setDescription("Test Description for test-reject-product");
        dataProduct.setValidationState(DataProductValidationStateRes.APPROVED);
        
        // Create the data product first
        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        DataProductRes createdDataProduct = dataProductResponse.getBody();
        
        // Create and publish a data product version
        DataProductVersionRes dataProductVersion = new DataProductVersionRes();
        dataProductVersion.setDataProduct(createdDataProduct);
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setSpec("opendatamesh");
        dataProductVersion.setSpecVersion("1.0.0");
        
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        
        DataProductVersionPublishCommandRes publishCommand = new DataProductVersionPublishCommandRes();
        publishCommand.setDataProductVersion(dataProductVersion);

        ResponseEntity<DataProductVersionPublishResultRes> publishResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/publish"),
                new HttpEntity<>(publishCommand),
                DataProductVersionPublishResultRes.class
        );
        assertThat(publishResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        DataProductVersionRes publishedVersion = publishResponse.getBody().getDataProductVersion();
        
        DataProductVersionRejectCommandRes rejectCommand = new DataProductVersionRejectCommandRes();
        rejectCommand.setDataProductVersion(publishedVersion);

        // When
        ResponseEntity<DataProductVersionRejectResultRes> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/reject"),
                new HttpEntity<>(rejectCommand),
                DataProductVersionRejectResultRes.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDataProductVersion()).isNotNull();
        
        // Verify the response contains the expected values
        DataProductVersionRes actualDataProductVersion = response.getBody().getDataProductVersion();
        assertThat(actualDataProductVersion.getUuid()).isEqualTo(publishedVersion.getUuid());
        assertThat(actualDataProductVersion.getValidationState()).isEqualTo(DataProductVersionValidationStateRes.REJECTED);

        // Cleanup
        cleanupDataProduct(createdDataProduct.getUuid());
    }

    @Test
    public void whenRejectDataProductVersionWithNullDataProductVersionThenReturnBadRequest() {
        // Given
        DataProductVersionRejectCommandRes rejectCommand = new DataProductVersionRejectCommandRes();
        rejectCommand.setDataProductVersion(null);

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/reject"),
                new HttpEntity<>(rejectCommand),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void whenRejectDataProductVersionWithNullUuidThenReturnBadRequest() {
        // Given
        DataProductVersionRes dataProductVersion = new DataProductVersionRes();
        dataProductVersion.setUuid(null);
        
        DataProductVersionRejectCommandRes rejectCommand = new DataProductVersionRejectCommandRes();
        rejectCommand.setDataProductVersion(dataProductVersion);

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/reject"),
                new HttpEntity<>(rejectCommand),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void whenRejectAlreadyRejectedDataProductVersionThenReturnBadRequest() {
        // Given - First create, publish and reject a data product version
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-reject-product-2");
        dataProduct.setDomain("test-reject-domain-2");
        dataProduct.setFqn("test-reject-domain-2:test-reject-product-2");
        dataProduct.setDisplayName("test-reject-product Display Name");
        dataProduct.setDescription("Test Description for test-reject-product");
        dataProduct.setValidationState(DataProductValidationStateRes.APPROVED);
        
        // Create the data product first
        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        DataProductRes createdDataProduct = dataProductResponse.getBody();
        
        // Create and publish a data product version
        DataProductVersionRes dataProductVersion = new DataProductVersionRes();
        dataProductVersion.setDataProduct(createdDataProduct);
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setSpec("opendatamesh");
        dataProductVersion.setSpecVersion("1.0.0");
        
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        
        DataProductVersionPublishCommandRes publishCommand = new DataProductVersionPublishCommandRes();
        publishCommand.setDataProductVersion(dataProductVersion);

        ResponseEntity<DataProductVersionPublishResultRes> publishResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/publish"),
                new HttpEntity<>(publishCommand),
                DataProductVersionPublishResultRes.class
        );
        assertThat(publishResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        DataProductVersionRes publishedVersion = publishResponse.getBody().getDataProductVersion();
        
        // Reject the published version
        DataProductVersionRejectCommandRes rejectCommand = new DataProductVersionRejectCommandRes();
        rejectCommand.setDataProductVersion(publishedVersion);

        ResponseEntity<DataProductVersionRejectResultRes> rejectResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/reject"),
                new HttpEntity<>(rejectCommand),
                DataProductVersionRejectResultRes.class
        );
        assertThat(rejectResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        DataProductVersionRes rejectedVersion = rejectResponse.getBody().getDataProductVersion();
        
        DataProductVersionRejectCommandRes rejectCommand2 = new DataProductVersionRejectCommandRes();
        rejectCommand2.setDataProductVersion(rejectedVersion);

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/reject"),
                new HttpEntity<>(rejectCommand2),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("can be rejected only if in PENDING state");

        // Cleanup
        cleanupDataProduct(createdDataProduct.getUuid());
    }

    // ========== DELETE ENDPOINT TESTS ==========

    @Test
    public void whenDeleteDataProductVersionWithValidUuidThenReturnNoContent() {
        // Given - First create a data product and publish a version
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-delete-product");
        dataProduct.setDomain("test-delete-domain");
        dataProduct.setFqn("test-delete-domain:test-delete-product");
        dataProduct.setDisplayName("test-delete-product Display Name");
        dataProduct.setDescription("Test Description for test-delete-product");
        dataProduct.setValidationState(DataProductValidationStateRes.APPROVED);

        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        DataProductRes createdDataProduct = dataProductResponse.getBody();

        // Create and publish a data product version
        DataProductVersionRes dataProductVersion = new DataProductVersionRes();
        dataProductVersion.setDataProduct(createdDataProduct);
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setSpec("opendatamesh");
        dataProductVersion.setSpecVersion("1.0.0");

        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);

        DataProductVersionPublishCommandRes publishCommand = new DataProductVersionPublishCommandRes();
        publishCommand.setDataProductVersion(dataProductVersion);

        ResponseEntity<DataProductVersionPublishResultRes> publishResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/publish"),
                new HttpEntity<>(publishCommand),
                DataProductVersionPublishResultRes.class
        );
        assertThat(publishResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        DataProductVersionRes publishedVersion = publishResponse.getBody().getDataProductVersion();
        String createdVersionUuid = publishedVersion.getUuid();
        String createdFqn = createdDataProduct.getFqn();
        String createdTag = publishedVersion.getTag();

        // Given - Create delete command with UUID
        DataProductVersionDeleteCommandRes deleteCommand = new DataProductVersionDeleteCommandRes();
        deleteCommand.setDataProductVersionUuid(createdVersionUuid);

        // When
        ResponseEntity<Void> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/delete"),
                new HttpEntity<>(deleteCommand),
                Void.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify that the data product version was actually deleted
        ResponseEntity<String> getResponse = rest.getForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + createdVersionUuid),
                String.class
        );
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // Verify notifications were sent (publish and delete)
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(notificationClient, times(2)).notifyEvent(eventCaptor.capture());
        java.util.List<Object> capturedEvents = eventCaptor.getAllValues();
        // Verify the last event (delete notification)
        Object capturedEvent = capturedEvents.get(capturedEvents.size() - 1);
        assertThat(capturedEvent).isInstanceOf(EmittedEventDataProductVersionDeletedRes.class);
        EmittedEventDataProductVersionDeletedRes event = (EmittedEventDataProductVersionDeletedRes) capturedEvent;
        assertThat(event.getResourceType()).isEqualTo(ResourceType.DATA_PRODUCT_VERSION);
        assertThat(event.getResourceIdentifier()).isEqualTo(createdVersionUuid);
        assertThat(event.getType()).isEqualTo(EventTypeRes.DATA_PRODUCT_VERSION_DELETED);
        assertThat(event.getEventTypeVersion()).isEqualTo(EventTypeVersion.V2_0_0);
        assertThat(event.getEventContent()).isNotNull();
        assertThat(event.getEventContent().getDataProductVersionUuid()).isEqualTo(createdVersionUuid);
        assertThat(event.getEventContent().getDataProductFqn()).isEqualTo(createdFqn);
        assertThat(event.getEventContent().getDataProductVersionTag()).isEqualTo(createdTag);

        // Cleanup
        cleanupDataProduct(createdDataProduct.getUuid());
    }

    @Test
    public void whenDeleteDataProductVersionWithValidFqnAndTagThenReturnNoContent() {
        // Given - First create a data product and publish a version
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-delete-fqn-product");
        dataProduct.setDomain("test-delete-domain");
        dataProduct.setFqn("test-delete-domain:test-delete-fqn-product");
        dataProduct.setDisplayName("test-delete-fqn-product Display Name");
        dataProduct.setDescription("Test Description for test-delete-fqn-product");
        dataProduct.setValidationState(DataProductValidationStateRes.APPROVED);

        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        DataProductRes createdDataProduct = dataProductResponse.getBody();
        String createdFqn = createdDataProduct.getFqn();

        // Create and publish a data product version
        DataProductVersionRes dataProductVersion = new DataProductVersionRes();
        dataProductVersion.setDataProduct(createdDataProduct);
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setSpec("opendatamesh");
        dataProductVersion.setSpecVersion("1.0.0");

        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);

        DataProductVersionPublishCommandRes publishCommand = new DataProductVersionPublishCommandRes();
        publishCommand.setDataProductVersion(dataProductVersion);

        ResponseEntity<DataProductVersionPublishResultRes> publishResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/publish"),
                new HttpEntity<>(publishCommand),
                DataProductVersionPublishResultRes.class
        );
        assertThat(publishResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        DataProductVersionRes publishedVersion = publishResponse.getBody().getDataProductVersion();
        String createdVersionUuid = publishedVersion.getUuid();
        String createdTag = publishedVersion.getTag();

        // Given - Create delete command with FQN and tag
        DataProductVersionDeleteCommandRes deleteCommand = new DataProductVersionDeleteCommandRes();
        deleteCommand.setDataProductFqn(createdFqn);
        deleteCommand.setDataProductVersionTag(createdTag);

        // When
        ResponseEntity<Void> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/delete"),
                new HttpEntity<>(deleteCommand),
                Void.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify that the data product version was actually deleted
        ResponseEntity<String> getResponse = rest.getForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + createdVersionUuid),
                String.class
        );
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // Cleanup
        cleanupDataProduct(createdDataProduct.getUuid());
    }

    @Test
    public void whenDeleteDataProductVersionWithBothUuidAndFqnTagThenReturnNoContent() {
        // Given - First create a data product and publish a version
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-delete-both-product");
        dataProduct.setDomain("test-delete-domain");
        dataProduct.setFqn("test-delete-domain:test-delete-both-product");
        dataProduct.setDisplayName("test-delete-both-product Display Name");
        dataProduct.setDescription("Test Description for test-delete-both-product");
        dataProduct.setValidationState(DataProductValidationStateRes.APPROVED);

        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        DataProductRes createdDataProduct = dataProductResponse.getBody();
        String createdFqn = createdDataProduct.getFqn();

        // Create and publish a data product version
        DataProductVersionRes dataProductVersion = new DataProductVersionRes();
        dataProductVersion.setDataProduct(createdDataProduct);
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setSpec("opendatamesh");
        dataProductVersion.setSpecVersion("1.0.0");

        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);

        DataProductVersionPublishCommandRes publishCommand = new DataProductVersionPublishCommandRes();
        publishCommand.setDataProductVersion(dataProductVersion);

        ResponseEntity<DataProductVersionPublishResultRes> publishResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/publish"),
                new HttpEntity<>(publishCommand),
                DataProductVersionPublishResultRes.class
        );
        assertThat(publishResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        DataProductVersionRes publishedVersion = publishResponse.getBody().getDataProductVersion();
        String createdVersionUuid = publishedVersion.getUuid();
        String createdTag = publishedVersion.getTag();

        // Given - Create delete command with both UUID and FQN+tag (UUID should be preferred)
        DataProductVersionDeleteCommandRes deleteCommand = new DataProductVersionDeleteCommandRes();
        deleteCommand.setDataProductVersionUuid(createdVersionUuid);
        deleteCommand.setDataProductFqn(createdFqn);
        deleteCommand.setDataProductVersionTag(createdTag);

        // When
        ResponseEntity<Void> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/delete"),
                new HttpEntity<>(deleteCommand),
                Void.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify that the data product version was actually deleted
        ResponseEntity<String> getResponse = rest.getForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + createdVersionUuid),
                String.class
        );
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // Cleanup
        cleanupDataProduct(createdDataProduct.getUuid());
    }

    @Test
    public void whenDeleteDataProductVersionWithNonExistentUuidThenReturnNotFound() {
        // Given
        DataProductVersionDeleteCommandRes deleteCommand = new DataProductVersionDeleteCommandRes();
        deleteCommand.setDataProductVersionUuid("non-existent-uuid-123");

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/delete"),
                new HttpEntity<>(deleteCommand),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void whenDeleteDataProductVersionWithNonExistentFqnThenReturnNotFound() {
        // Given
        DataProductVersionDeleteCommandRes deleteCommand = new DataProductVersionDeleteCommandRes();
        deleteCommand.setDataProductFqn("non.existent:non-existent-product");
        deleteCommand.setDataProductVersionTag("v1.0.0");

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/delete"),
                new HttpEntity<>(deleteCommand),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void whenDeleteDataProductVersionWithNonExistentTagThenReturnNotFound() {
        // Given - First create a data product
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-delete-nonexistent-tag-product");
        dataProduct.setDomain("test-delete-domain");
        dataProduct.setFqn("test-delete-domain:test-delete-nonexistent-tag-product");
        dataProduct.setDisplayName("test-delete-nonexistent-tag-product Display Name");
        dataProduct.setDescription("Test Description for test-delete-nonexistent-tag-product");
        dataProduct.setValidationState(DataProductValidationStateRes.APPROVED);

        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        DataProductRes createdDataProduct = dataProductResponse.getBody();
        String createdFqn = createdDataProduct.getFqn();

        // Given - Create delete command with non-existent tag
        DataProductVersionDeleteCommandRes deleteCommand = new DataProductVersionDeleteCommandRes();
        deleteCommand.setDataProductFqn(createdFqn);
        deleteCommand.setDataProductVersionTag("non-existent-tag");

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/delete"),
                new HttpEntity<>(deleteCommand),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // Cleanup
        cleanupDataProduct(createdDataProduct.getUuid());
    }

    @Test
    public void whenDeleteDataProductVersionWithNullUuidAndFqnTagThenReturnBadRequest() {
        // Given
        DataProductVersionDeleteCommandRes deleteCommand = new DataProductVersionDeleteCommandRes();
        deleteCommand.setDataProductVersionUuid(null);
        deleteCommand.setDataProductFqn(null);
        deleteCommand.setDataProductVersionTag(null);

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/delete"),
                new HttpEntity<>(deleteCommand),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void whenDeleteDataProductVersionWithEmptyUuidAndFqnTagThenReturnBadRequest() {
        // Given
        DataProductVersionDeleteCommandRes deleteCommand = new DataProductVersionDeleteCommandRes();
        deleteCommand.setDataProductVersionUuid("");
        deleteCommand.setDataProductFqn("");
        deleteCommand.setDataProductVersionTag("");

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/delete"),
                new HttpEntity<>(deleteCommand),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void whenDeleteDataProductVersionWithOnlyFqnThenReturnBadRequest() {
        // Given
        DataProductVersionDeleteCommandRes deleteCommand = new DataProductVersionDeleteCommandRes();
        deleteCommand.setDataProductFqn("test.domain:test-product");
        deleteCommand.setDataProductVersionTag(null);

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/delete"),
                new HttpEntity<>(deleteCommand),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void whenDeleteDataProductVersionWithOnlyTagThenReturnBadRequest() {
        // Given
        DataProductVersionDeleteCommandRes deleteCommand = new DataProductVersionDeleteCommandRes();
        deleteCommand.setDataProductFqn(null);
        deleteCommand.setDataProductVersionTag("v1.0.0");

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/delete"),
                new HttpEntity<>(deleteCommand),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private void cleanupDataProduct(String uuid) {
        try {
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + uuid));
        } catch (Exception e) {
            // Ignore cleanup errors in tests
        }
    }
}
