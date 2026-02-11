package org.opendatamesh.platform.pp.registry.rest.v2.controllers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.opendatamesh.platform.pp.registry.client.notification.NotificationClient;
import org.opendatamesh.platform.pp.registry.rest.v2.RegistryApplicationIT;
import org.opendatamesh.platform.pp.registry.rest.v2.RoutesV2;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductValidationStateRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.events.emitted.EmittedEventDataProductDeletedRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.events.emitted.EmittedEventDataProductInitializationRequestedRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.events.emitted.EmittedEventDataProductInitializedRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.usecases.approve.DataProductApproveCommandRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.usecases.approve.DataProductApproveResultRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.usecases.delete.DataProductDeleteCommandRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.usecases.init.DataProductInitCommandRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.usecases.init.DataProductInitResultRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.usecases.reject.DataProductRejectCommandRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.usecases.reject.DataProductRejectResultRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.usecases.updatefields.DataProductDocumentationFieldsRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.usecases.updatefields.DataProductDocumentationFieldsUpdateCommandRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.usecases.updatefields.DataProductDocumentationFieldsUpdateResultRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.EventTypeRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.EventTypeVersion;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.ResourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class DataProductUseCaseControllerIT extends RegistryApplicationIT {
    
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
    
    // ========== INIT ENDPOINT TESTS ==========

    @Test
    public void whenInitializeDataProductWithValidDataThenReturnCreatedDataProduct() {
        // Given
        DataProductRes expectedDataProduct = new DataProductRes();
        expectedDataProduct.setName("test-init-product");
        expectedDataProduct.setDomain("test-init-domain");
        expectedDataProduct.setFqn("test-init-domain:test-init-product");
        expectedDataProduct.setDisplayName("test-init-product Display Name");
        expectedDataProduct.setDescription("Test Description for test-init-product");
        
        DataProductInitCommandRes initCommand = new DataProductInitCommandRes();
        initCommand.setDataProduct(expectedDataProduct);

        // When
        ResponseEntity<DataProductInitResultRes> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/init"),
                new HttpEntity<>(initCommand),
                DataProductInitResultRes.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDataProduct()).isNotNull();
        
        // Verify the response contains the expected values
        DataProductRes actualDataProduct = response.getBody().getDataProduct();
        assertThat(actualDataProduct.getUuid()).isNotNull();
        assertThat(actualDataProduct.getName()).isEqualTo(expectedDataProduct.getName());
        assertThat(actualDataProduct.getDomain()).isEqualTo(expectedDataProduct.getDomain());
        assertThat(actualDataProduct.getFqn()).isEqualTo(expectedDataProduct.getFqn());
        assertThat(actualDataProduct.getDisplayName()).isEqualTo(expectedDataProduct.getDisplayName());
        assertThat(actualDataProduct.getDescription()).isEqualTo(expectedDataProduct.getDescription());
        assertThat(actualDataProduct.getValidationState()).isEqualTo(DataProductValidationStateRes.PENDING);

        // Verify notification was sent
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(notificationClient).notifyEvent(eventCaptor.capture());
        Object capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent).isInstanceOf(EmittedEventDataProductInitializationRequestedRes.class);
        EmittedEventDataProductInitializationRequestedRes event = (EmittedEventDataProductInitializationRequestedRes) capturedEvent;
        assertThat(event.getResourceType()).isEqualTo(ResourceType.DATA_PRODUCT);
        assertThat(event.getResourceIdentifier()).isEqualTo(actualDataProduct.getUuid());
        assertThat(event.getType()).isEqualTo(EventTypeRes.DATA_PRODUCT_INITIALIZATION_REQUESTED);
        assertThat(event.getEventTypeVersion()).isEqualTo(EventTypeVersion.V2_0_0.toString());
        assertThat(event.getEventContent()).isNotNull();
        assertThat(event.getEventContent().getDataProduct()).isNotNull();
        assertThat(event.getEventContent().getDataProduct().getUuid()).isEqualTo(actualDataProduct.getUuid());
        assertThat(event.getEventContent().getDataProduct().getFqn()).isEqualTo(expectedDataProduct.getFqn());

        // Cleanup
        cleanupDataProduct(response.getBody().getDataProduct().getUuid());
    }

    @Test
    public void whenInitializeDataProductWithNullDataProductThenReturnBadRequest() {
        // Given
        DataProductInitCommandRes initCommand = new DataProductInitCommandRes();
        initCommand.setDataProduct(null);

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/init"),
                new HttpEntity<>(initCommand),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void whenInitializeDataProductWithNullFqnThenReturnBadRequest() {
        // Given
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-init-product");
        dataProduct.setDomain("test-init-domain");
        dataProduct.setFqn(null);
        dataProduct.setDisplayName("test-init-product Display Name");
        dataProduct.setDescription("Test Description for test-init-product");
        
        DataProductInitCommandRes initCommand = new DataProductInitCommandRes();
        initCommand.setDataProduct(dataProduct);

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/init"),
                new HttpEntity<>(initCommand),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void whenInitializeDataProductWithExistingFqnThenReturnBadRequest() {
        // Given - First create a data product
        DataProductRes dataProduct1 = new DataProductRes();
        dataProduct1.setName("test-init-product-1");
        dataProduct1.setDomain("test-init-domain");
        dataProduct1.setFqn("test-init-domain:test-init-product-1");
        dataProduct1.setDisplayName("test-init-product-1 Display Name");
        dataProduct1.setDescription("Test Description for test-init-product-1");
        
        DataProductInitCommandRes initCommand1 = new DataProductInitCommandRes();
        initCommand1.setDataProduct(dataProduct1);

        ResponseEntity<DataProductInitResultRes> response1 = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/init"),
                new HttpEntity<>(initCommand1),
                DataProductInitResultRes.class
        );

        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String createdUuid = response1.getBody().getDataProduct().getUuid();

        // Given - Try to create another data product with the same FQN
        DataProductRes dataProduct2 = new DataProductRes();
        dataProduct2.setName("test-init-product-2");
        dataProduct2.setDomain("test-init-domain");
        dataProduct2.setFqn(dataProduct1.getFqn()); // Same FQN
        dataProduct2.setDisplayName("test-init-product-2 Display Name");
        dataProduct2.setDescription("Test Description for test-init-product-2");
        
        DataProductInitCommandRes initCommand2 = new DataProductInitCommandRes();
        initCommand2.setDataProduct(dataProduct2);

        // When
        ResponseEntity<String> response2 = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/init"),
                new HttpEntity<>(initCommand2),
                String.class
        );

        // Then
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Cleanup
        cleanupDataProduct(createdUuid);
    }

    @Test
    public void whenInitializeDataProductWithExistingRejectedDataProductThenReplaceRejectedProduct() {
        // Given - First create and reject a data product
        DataProductRes rejectedDataProduct = new DataProductRes();
        rejectedDataProduct.setName("test-rejected-product");
        rejectedDataProduct.setDomain("test-replace-domain");
        rejectedDataProduct.setFqn("test-replace-domain:test-rejected-product");
        rejectedDataProduct.setDisplayName("test-rejected-product Display Name");
        rejectedDataProduct.setDescription("Test Description for test-rejected-product");
        
        DataProductInitCommandRes initCommand1 = new DataProductInitCommandRes();
        initCommand1.setDataProduct(rejectedDataProduct);

        ResponseEntity<DataProductInitResultRes> initResponse1 = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/init"),
                new HttpEntity<>(initCommand1),
                DataProductInitResultRes.class
        );

        assertThat(initResponse1.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String rejectedUuid = initResponse1.getBody().getDataProduct().getUuid();

        // Reject the data product
        DataProductRejectCommandRes rejectCommand = new DataProductRejectCommandRes();
        rejectCommand.setDataProduct(initResponse1.getBody().getDataProduct());

        ResponseEntity<DataProductRejectResultRes> rejectResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/reject"),
                new HttpEntity<>(rejectCommand),
                DataProductRejectResultRes.class
        );

        assertThat(rejectResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Given - Now try to initialize a new data product with the same FQN
        DataProductRes newDataProduct = new DataProductRes();
        newDataProduct.setName("test-new-product");
        newDataProduct.setDomain("test-replace-domain");
        newDataProduct.setFqn("test-replace-domain:test-rejected-product"); // Same FQN as rejected product
        newDataProduct.setDisplayName("test-new-product Display Name");
        newDataProduct.setDescription("Test Description for test-new-product");
        
        DataProductInitCommandRes initCommand2 = new DataProductInitCommandRes();
        initCommand2.setDataProduct(newDataProduct);

        // When
        ResponseEntity<DataProductInitResultRes> initResponse2 = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/init"),
                new HttpEntity<>(initCommand2),
                DataProductInitResultRes.class
        );

        // Then
        assertThat(initResponse2.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(initResponse2.getBody()).isNotNull();
        assertThat(initResponse2.getBody().getDataProduct()).isNotNull();
        
        // Verify the response contains the expected values for the new product
        DataProductRes actualDataProduct = initResponse2.getBody().getDataProduct();
        assertThat(actualDataProduct.getUuid()).isNotNull();
        assertThat(actualDataProduct.getUuid()).isNotEqualTo(rejectedUuid); // Should be a new UUID
        assertThat(actualDataProduct.getName()).isEqualTo(newDataProduct.getName());
        assertThat(actualDataProduct.getDomain()).isEqualTo(newDataProduct.getDomain());
        assertThat(actualDataProduct.getFqn()).isEqualTo(newDataProduct.getFqn());
        assertThat(actualDataProduct.getDisplayName()).isEqualTo(newDataProduct.getDisplayName());
        assertThat(actualDataProduct.getDescription()).isEqualTo(newDataProduct.getDescription());
        assertThat(actualDataProduct.getValidationState()).isEqualTo(DataProductValidationStateRes.PENDING);

        // Verify that the rejected product was deleted by trying to get it
        ResponseEntity<String> getRejectedResponse = rest.getForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/" + rejectedUuid),
                String.class
        );
        assertThat(getRejectedResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // Cleanup
        cleanupDataProduct(actualDataProduct.getUuid());
    }

    // ========== APPROVE ENDPOINT TESTS ==========

    @Test
    public void whenApproveDataProductWithValidPendingDataProductThenReturnApprovedDataProduct() {
        // Given - First initialize a data product
        DataProductRes expectedDataProduct = new DataProductRes();
        expectedDataProduct.setName("test-approve-product");
        expectedDataProduct.setDomain("test-approve-domain");
        expectedDataProduct.setFqn("test-approve-domain:test-approve-product");
        expectedDataProduct.setDisplayName("test-approve-product Display Name");
        expectedDataProduct.setDescription("Test Description for test-approve-product");
        
        DataProductInitCommandRes initCommand = new DataProductInitCommandRes();
        initCommand.setDataProduct(expectedDataProduct);

        ResponseEntity<DataProductInitResultRes> initResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/init"),
                new HttpEntity<>(initCommand),
                DataProductInitResultRes.class
        );

        assertThat(initResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String createdUuid = initResponse.getBody().getDataProduct().getUuid();

        // Given - Create approval command
        DataProductApproveCommandRes approveCommand = new DataProductApproveCommandRes();
        approveCommand.setDataProduct(initResponse.getBody().getDataProduct());

        // When
        ResponseEntity<DataProductApproveResultRes> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/approve"),
                new HttpEntity<>(approveCommand),
                DataProductApproveResultRes.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDataProduct()).isNotNull();
        
        // Verify the response contains the expected values
        DataProductRes actualDataProduct = response.getBody().getDataProduct();
        assertThat(actualDataProduct.getUuid()).isEqualTo(createdUuid);
        assertThat(actualDataProduct.getName()).isEqualTo(expectedDataProduct.getName());
        assertThat(actualDataProduct.getDomain()).isEqualTo(expectedDataProduct.getDomain());
        assertThat(actualDataProduct.getFqn()).isEqualTo(expectedDataProduct.getFqn());
        assertThat(actualDataProduct.getDisplayName()).isEqualTo(expectedDataProduct.getDisplayName());
        assertThat(actualDataProduct.getDescription()).isEqualTo(expectedDataProduct.getDescription());
        assertThat(actualDataProduct.getValidationState()).isEqualTo(DataProductValidationStateRes.APPROVED);

        // Verify notifications were sent (init and approve)
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(notificationClient, times(2)).notifyEvent(eventCaptor.capture());
        java.util.List<Object> capturedEvents = eventCaptor.getAllValues();
        // Verify the last event (approve notification)
        Object capturedEvent = capturedEvents.get(capturedEvents.size() - 1);
        assertThat(capturedEvent).isInstanceOf(EmittedEventDataProductInitializedRes.class);
        EmittedEventDataProductInitializedRes event = (EmittedEventDataProductInitializedRes) capturedEvent;
        assertThat(event.getResourceType()).isEqualTo(ResourceType.DATA_PRODUCT);
        assertThat(event.getResourceIdentifier()).isEqualTo(createdUuid);
        assertThat(event.getType()).isEqualTo(EventTypeRes.DATA_PRODUCT_INITIALIZED);
        assertThat(event.getEventTypeVersion()).isEqualTo(EventTypeVersion.V2_0_0.toString());
        assertThat(event.getEventContent()).isNotNull();
        assertThat(event.getEventContent().getDataProduct()).isNotNull();
        assertThat(event.getEventContent().getDataProduct().getUuid()).isEqualTo(createdUuid);
        assertThat(event.getEventContent().getDataProduct().getValidationState()).isEqualTo(DataProductValidationStateRes.APPROVED);

        // Cleanup
        cleanupDataProduct(createdUuid);
    }

    @Test
    public void whenApproveDataProductWithNonExistentDataProductThenReturnBadRequest() {
        // Given
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-approve-nonexistent");
        dataProduct.setDomain("test-approve-domain");
        dataProduct.setFqn("test-approve-domain:test-approve-nonexistent");
        dataProduct.setDisplayName("test-approve-nonexistent Display Name");
        dataProduct.setDescription("Test Description for test-approve-nonexistent");
        
        DataProductApproveCommandRes approveCommand = new DataProductApproveCommandRes();
        approveCommand.setDataProduct(dataProduct);

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/approve"),
                new HttpEntity<>(approveCommand),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void whenApproveDataProductWithNullDataProductThenReturnBadRequest() {
        // Given
        DataProductApproveCommandRes approveCommand = new DataProductApproveCommandRes();
        approveCommand.setDataProduct(null);

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/approve"),
                new HttpEntity<>(approveCommand),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void whenApproveDataProductWithNullFqnThenReturnBadRequest() {
        // Given
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-approve-product");
        dataProduct.setDomain("test-approve-domain");
        dataProduct.setFqn(null);
        dataProduct.setDisplayName("test-approve-product Display Name");
        dataProduct.setDescription("Test Description for test-approve-product");
        
        DataProductApproveCommandRes approveCommand = new DataProductApproveCommandRes();
        approveCommand.setDataProduct(dataProduct);

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/approve"),
                new HttpEntity<>(approveCommand),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void whenApproveDataProductWithAlreadyApprovedDataProductThenReturnBadRequest() {
        // Given - First initialize and validate a data product
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-approve-already-approved");
        dataProduct.setDomain("test-approve-domain");
        dataProduct.setFqn("test-approve-domain:test-approve-already-approved");
        dataProduct.setDisplayName("test-approve-already-approved Display Name");
        dataProduct.setDescription("Test Description for test-approve-already-approved");
        
        DataProductInitCommandRes initCommand = new DataProductInitCommandRes();
        initCommand.setDataProduct(dataProduct);

        ResponseEntity<DataProductInitResultRes> initResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/init"),
                new HttpEntity<>(initCommand),
                DataProductInitResultRes.class
        );

        assertThat(initResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String createdUuid = initResponse.getBody().getDataProduct().getUuid();

        // Approve the data product
        DataProductApproveCommandRes approveCommand1 = new DataProductApproveCommandRes();
        approveCommand1.setDataProduct(initResponse.getBody().getDataProduct());

        ResponseEntity<DataProductApproveResultRes> approveResponse1 = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/approve"),
                new HttpEntity<>(approveCommand1),
                DataProductApproveResultRes.class
        );

        assertThat(approveResponse1.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Given - Try to approve the same data product again
        DataProductApproveCommandRes approveCommand2 = new DataProductApproveCommandRes();
        approveCommand2.setDataProduct(approveResponse1.getBody().getDataProduct());

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/approve"),
                new HttpEntity<>(approveCommand2),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Cleanup
        cleanupDataProduct(createdUuid);
    }

    // ========== REJECT ENDPOINT TESTS ==========

    @Test
    public void whenRejectDataProductWithValidPendingDataProductThenReturnRejectedDataProduct() {
        // Given - First initialize a data product
        DataProductRes expectedDataProduct = new DataProductRes();
        expectedDataProduct.setName("test-reject-product");
        expectedDataProduct.setDomain("test-reject-domain");
        expectedDataProduct.setFqn("test-reject-domain:test-reject-product");
        expectedDataProduct.setDisplayName("test-reject-product Display Name");
        expectedDataProduct.setDescription("Test Description for test-reject-product");
        
        DataProductInitCommandRes initCommand = new DataProductInitCommandRes();
        initCommand.setDataProduct(expectedDataProduct);

        ResponseEntity<DataProductInitResultRes> initResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/init"),
                new HttpEntity<>(initCommand),
                DataProductInitResultRes.class
        );

        assertThat(initResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String createdUuid = initResponse.getBody().getDataProduct().getUuid();

        // Given - Create rejection command
        DataProductRejectCommandRes rejectCommand = new DataProductRejectCommandRes();
        rejectCommand.setDataProduct(initResponse.getBody().getDataProduct());

        // When
        ResponseEntity<DataProductRejectResultRes> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/reject"),
                new HttpEntity<>(rejectCommand),
                DataProductRejectResultRes.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDataProduct()).isNotNull();
        
        // Verify the response contains the expected values
        DataProductRes actualDataProduct = response.getBody().getDataProduct();
        assertThat(actualDataProduct.getUuid()).isEqualTo(createdUuid);
        assertThat(actualDataProduct.getName()).isEqualTo(expectedDataProduct.getName());
        assertThat(actualDataProduct.getDomain()).isEqualTo(expectedDataProduct.getDomain());
        assertThat(actualDataProduct.getFqn()).isEqualTo(expectedDataProduct.getFqn());
        assertThat(actualDataProduct.getDisplayName()).isEqualTo(expectedDataProduct.getDisplayName());
        assertThat(actualDataProduct.getDescription()).isEqualTo(expectedDataProduct.getDescription());
        assertThat(actualDataProduct.getValidationState()).isEqualTo(DataProductValidationStateRes.REJECTED);

        // Cleanup
        cleanupDataProduct(createdUuid);
    }

    @Test
    public void whenRejectDataProductWithNonExistentDataProductThenReturnBadRequest() {
        // Given
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-reject-nonexistent");
        dataProduct.setDomain("test-reject-domain");
        dataProduct.setFqn("test-reject-domain:test-reject-nonexistent");
        dataProduct.setDisplayName("test-reject-nonexistent Display Name");
        dataProduct.setDescription("Test Description for test-reject-nonexistent");
        
        DataProductRejectCommandRes rejectCommand = new DataProductRejectCommandRes();
        rejectCommand.setDataProduct(dataProduct);

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/reject"),
                new HttpEntity<>(rejectCommand),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void whenRejectDataProductWithNullDataProductThenReturnBadRequest() {
        // Given
        DataProductRejectCommandRes rejectCommand = new DataProductRejectCommandRes();
        rejectCommand.setDataProduct(null);

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/reject"),
                new HttpEntity<>(rejectCommand),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void whenRejectDataProductWithNullFqnThenReturnBadRequest() {
        // Given
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-reject-product");
        dataProduct.setDomain("test-reject-domain");
        dataProduct.setFqn(null);
        dataProduct.setDisplayName("test-reject-product Display Name");
        dataProduct.setDescription("Test Description for test-reject-product");
        
        DataProductRejectCommandRes rejectCommand = new DataProductRejectCommandRes();
        rejectCommand.setDataProduct(dataProduct);

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/reject"),
                new HttpEntity<>(rejectCommand),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void whenRejectDataProductWithAlreadyApprovedDataProductThenReturnBadRequest() {
        // Given - First initialize and approve a data product
        DataProductRes expectedDataProduct = new DataProductRes();
        expectedDataProduct.setName("test-reject-approved");
        expectedDataProduct.setDomain("test-reject-domain");
        expectedDataProduct.setFqn("test-reject-domain:test-reject-approved");
        expectedDataProduct.setDisplayName("test-reject-approved Display Name");
        expectedDataProduct.setDescription("Test Description for test-reject-approved");
        
        DataProductInitCommandRes initCommand = new DataProductInitCommandRes();
        initCommand.setDataProduct(expectedDataProduct);

        ResponseEntity<DataProductInitResultRes> initResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/init"),
                new HttpEntity<>(initCommand),
                DataProductInitResultRes.class
        );

        assertThat(initResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String createdUuid = initResponse.getBody().getDataProduct().getUuid();

        // Approve the data product
        DataProductApproveCommandRes approveCommand = new DataProductApproveCommandRes();
        approveCommand.setDataProduct(initResponse.getBody().getDataProduct());

        ResponseEntity<DataProductApproveResultRes> approveResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/approve"),
                new HttpEntity<>(approveCommand),
                DataProductApproveResultRes.class
        );

        assertThat(approveResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Given - Now try to reject the approved data product
        DataProductRejectCommandRes rejectCommand = new DataProductRejectCommandRes();
        rejectCommand.setDataProduct(approveResponse.getBody().getDataProduct());

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/reject"),
                new HttpEntity<>(rejectCommand),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("can be rejected only if in PENDING state");

        // Cleanup
        cleanupDataProduct(createdUuid);
    }

    @Test
    public void whenRejectDataProductWithAlreadyRejectedDataProductThenReturnBadRequest() {
        // Given - First initialize and reject a data product
        DataProductRes expectedDataProduct = new DataProductRes();
        expectedDataProduct.setName("test-reject-already-rejected");
        expectedDataProduct.setDomain("test-reject-domain");
        expectedDataProduct.setFqn("test-reject-domain:test-reject-already-rejected");
        expectedDataProduct.setDisplayName("test-reject-already-rejected Display Name");
        expectedDataProduct.setDescription("Test Description for test-reject-already-rejected");
        
        DataProductInitCommandRes initCommand = new DataProductInitCommandRes();
        initCommand.setDataProduct(expectedDataProduct);

        ResponseEntity<DataProductInitResultRes> initResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/init"),
                new HttpEntity<>(initCommand),
                DataProductInitResultRes.class
        );

        assertThat(initResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String createdUuid = initResponse.getBody().getDataProduct().getUuid();

        // Reject the data product
        DataProductRejectCommandRes rejectCommand1 = new DataProductRejectCommandRes();
        rejectCommand1.setDataProduct(initResponse.getBody().getDataProduct());

        ResponseEntity<DataProductRejectResultRes> rejectResponse1 = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/reject"),
                new HttpEntity<>(rejectCommand1),
                DataProductRejectResultRes.class
        );

        assertThat(rejectResponse1.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Given - Try to reject the same data product again
        DataProductRejectCommandRes rejectCommand2 = new DataProductRejectCommandRes();
        rejectCommand2.setDataProduct(rejectResponse1.getBody().getDataProduct());

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/reject"),
                new HttpEntity<>(rejectCommand2),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("can be rejected only if in PENDING state");

        // Cleanup
        cleanupDataProduct(createdUuid);
    }

    // ========== DELETE ENDPOINT TESTS ==========

    @Test
    public void whenDeleteDataProductWithValidUuidThenReturnNoContent() {
        // Given - First initialize a data product
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-delete-product");
        dataProduct.setDomain("test-delete-domain");
        dataProduct.setFqn("test-delete-domain:test-delete-product");
        dataProduct.setDisplayName("test-delete-product Display Name");
        dataProduct.setDescription("Test Description for test-delete-product");
        
        DataProductInitCommandRes initCommand = new DataProductInitCommandRes();
        initCommand.setDataProduct(dataProduct);

        ResponseEntity<DataProductInitResultRes> initResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/init"),
                new HttpEntity<>(initCommand),
                DataProductInitResultRes.class
        );

        assertThat(initResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String createdUuid = initResponse.getBody().getDataProduct().getUuid();
        String createdFqn = initResponse.getBody().getDataProduct().getFqn();

        // Given - Create delete command with UUID
        DataProductDeleteCommandRes deleteCommand = new DataProductDeleteCommandRes();
        deleteCommand.setDataProductUuid(createdUuid);

        // When
        ResponseEntity<Void> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/delete"),
                new HttpEntity<>(deleteCommand),
                Void.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify that the data product was actually deleted
        ResponseEntity<String> getResponse = rest.getForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/" + createdUuid),
                String.class
        );
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // Verify notifications were sent (init and delete)
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(notificationClient, times(2)).notifyEvent(eventCaptor.capture());
        java.util.List<Object> capturedEvents = eventCaptor.getAllValues();
        // Verify the last event (delete notification)
        Object capturedEvent = capturedEvents.get(capturedEvents.size() - 1);
        assertThat(capturedEvent).isInstanceOf(EmittedEventDataProductDeletedRes.class);
        EmittedEventDataProductDeletedRes event = (EmittedEventDataProductDeletedRes) capturedEvent;
        assertThat(event.getResourceType()).isEqualTo(ResourceType.DATA_PRODUCT);
        assertThat(event.getResourceIdentifier()).isEqualTo(createdUuid);
        assertThat(event.getType()).isEqualTo(EventTypeRes.DATA_PRODUCT_DELETED);
        assertThat(event.getEventTypeVersion()).isEqualTo(EventTypeVersion.V2_0_0.toString());
        assertThat(event.getEventContent()).isNotNull();
        assertThat(event.getEventContent().getDataProductUuid()).isEqualTo(createdUuid);
        assertThat(event.getEventContent().getDataProductFqn()).isEqualTo(createdFqn);
    }

    @Test
    public void whenDeleteDataProductWithValidFqnThenReturnNoContent() {
        // Given - First initialize a data product
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-delete-fqn-product");
        dataProduct.setDomain("test-delete-domain");
        dataProduct.setFqn("test-delete-domain:test-delete-fqn-product");
        dataProduct.setDisplayName("test-delete-fqn-product Display Name");
        dataProduct.setDescription("Test Description for test-delete-fqn-product");
        
        DataProductInitCommandRes initCommand = new DataProductInitCommandRes();
        initCommand.setDataProduct(dataProduct);

        ResponseEntity<DataProductInitResultRes> initResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/init"),
                new HttpEntity<>(initCommand),
                DataProductInitResultRes.class
        );

        assertThat(initResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String createdUuid = initResponse.getBody().getDataProduct().getUuid();
        String createdFqn = initResponse.getBody().getDataProduct().getFqn();

        // Given - Create delete command with FQN
        DataProductDeleteCommandRes deleteCommand = new DataProductDeleteCommandRes();
        deleteCommand.setDataProductFqn(createdFqn);

        // When
        ResponseEntity<Void> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/delete"),
                new HttpEntity<>(deleteCommand),
                Void.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify that the data product was actually deleted
        ResponseEntity<String> getResponse = rest.getForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/" + createdUuid),
                String.class
        );
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void whenDeleteDataProductWithBothUuidAndFqnThenReturnNoContent() {
        // Given - First initialize a data product
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-delete-both-product");
        dataProduct.setDomain("test-delete-domain");
        dataProduct.setFqn("test-delete-domain:test-delete-both-product");
        dataProduct.setDisplayName("test-delete-both-product Display Name");
        dataProduct.setDescription("Test Description for test-delete-both-product");
        
        DataProductInitCommandRes initCommand = new DataProductInitCommandRes();
        initCommand.setDataProduct(dataProduct);

        ResponseEntity<DataProductInitResultRes> initResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/init"),
                new HttpEntity<>(initCommand),
                DataProductInitResultRes.class
        );

        assertThat(initResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String createdUuid = initResponse.getBody().getDataProduct().getUuid();
        String createdFqn = initResponse.getBody().getDataProduct().getFqn();

        // Given - Create delete command with both UUID and FQN (UUID should be preferred)
        DataProductDeleteCommandRes deleteCommand = new DataProductDeleteCommandRes();
        deleteCommand.setDataProductUuid(createdUuid);
        deleteCommand.setDataProductFqn(createdFqn);

        // When
        ResponseEntity<Void> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/delete"),
                new HttpEntity<>(deleteCommand),
                Void.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify that the data product was actually deleted
        ResponseEntity<String> getResponse = rest.getForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/" + createdUuid),
                String.class
        );
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void whenDeleteDataProductWithNonExistentUuidThenReturnNotFound() {
        // Given
        DataProductDeleteCommandRes deleteCommand = new DataProductDeleteCommandRes();
        deleteCommand.setDataProductUuid("non-existent-uuid-123");

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/delete"),
                new HttpEntity<>(deleteCommand),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void whenDeleteDataProductWithNonExistentFqnThenReturnNotFound() {
        // Given
        DataProductDeleteCommandRes deleteCommand = new DataProductDeleteCommandRes();
        deleteCommand.setDataProductFqn("non.existent:non-existent-product");

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/delete"),
                new HttpEntity<>(deleteCommand),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void whenDeleteDataProductWithNullUuidAndFqnThenReturnBadRequest() {
        // Given
        DataProductDeleteCommandRes deleteCommand = new DataProductDeleteCommandRes();
        deleteCommand.setDataProductUuid(null);
        deleteCommand.setDataProductFqn(null);

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/delete"),
                new HttpEntity<>(deleteCommand),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void whenDeleteDataProductWithEmptyUuidAndFqnThenReturnBadRequest() {
        // Given
        DataProductDeleteCommandRes deleteCommand = new DataProductDeleteCommandRes();
        deleteCommand.setDataProductUuid("");
        deleteCommand.setDataProductFqn("");

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/delete"),
                new HttpEntity<>(deleteCommand),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ========== UPDATE FIELDS ENDPOINT TESTS ==========

    @Test
    public void whenUpdateDocumentationFieldsDataProductWithValidDataThenReturnUpdatedDataProduct() {
        // Given - First initialize a data product
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-update-fields-product");
        dataProduct.setDomain("test-update-fields-domain");
        dataProduct.setFqn("test-update-fields-domain:test-update-fields-product");
        dataProduct.setDisplayName("Original Display Name");
        dataProduct.setDescription("Original Description");

        DataProductInitCommandRes initCommand = new DataProductInitCommandRes();
        initCommand.setDataProduct(dataProduct);

        ResponseEntity<DataProductInitResultRes> initResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/init"),
                new HttpEntity<>(initCommand),
                DataProductInitResultRes.class
        );

        assertThat(initResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        DataProductRes createdDataProduct = initResponse.getBody().getDataProduct();
        String createdUuid = createdDataProduct.getUuid();

        // Given - Update displayName and description
        DataProductDocumentationFieldsRes updateFields = new DataProductDocumentationFieldsRes();
        updateFields.setUuid(createdUuid);
        updateFields.setDisplayName("Updated Display Name");
        updateFields.setDescription("Updated Description");

        DataProductDocumentationFieldsUpdateCommandRes updateCommand = new DataProductDocumentationFieldsUpdateCommandRes();
        updateCommand.setDataProduct(updateFields);

        // When
        ResponseEntity<DataProductDocumentationFieldsUpdateResultRes> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/update-documentation-fields"),
                new HttpEntity<>(updateCommand),
                DataProductDocumentationFieldsUpdateResultRes.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDataProduct()).isNotNull();

        DataProductRes actualDataProduct = response.getBody().getDataProduct();
        assertThat(actualDataProduct.getUuid()).isEqualTo(createdUuid);
        assertThat(actualDataProduct.getDisplayName()).isEqualTo("Updated Display Name");
        assertThat(actualDataProduct.getDescription()).isEqualTo("Updated Description");
        assertThat(actualDataProduct.getName()).isEqualTo(dataProduct.getName());
        assertThat(actualDataProduct.getFqn()).isEqualTo(dataProduct.getFqn());

        // Cleanup
        cleanupDataProduct(createdUuid);
    }

    @Test
    public void whenUpdateDocumentationFieldsDataProductWithNullUuidThenReturnBadRequest() {
        // Given - First initialize a data product
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-update-fields-nulluuid");
        dataProduct.setDomain("test-update-fields-domain");
        dataProduct.setFqn("test-update-fields-domain:test-update-fields-nulluuid");
        dataProduct.setDisplayName("Display Name");
        dataProduct.setDescription("Description");

        DataProductInitCommandRes initCommand = new DataProductInitCommandRes();
        initCommand.setDataProduct(dataProduct);

        ResponseEntity<DataProductInitResultRes> initResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/init"),
                new HttpEntity<>(initCommand),
                DataProductInitResultRes.class
        );

        assertThat(initResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String createdUuid = initResponse.getBody().getDataProduct().getUuid();

        // Given - Update command with null uuid
        DataProductDocumentationFieldsRes updateFields = new DataProductDocumentationFieldsRes();
        updateFields.setUuid(null);
        updateFields.setDisplayName("Updated Display Name");

        DataProductDocumentationFieldsUpdateCommandRes updateCommand = new DataProductDocumentationFieldsUpdateCommandRes();
        updateCommand.setDataProduct(updateFields);

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/update-documentation-fields"),
                new HttpEntity<>(updateCommand),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("UUID is required for data product fields update");

        // Cleanup
        cleanupDataProduct(createdUuid);
    }

    @Test
    public void whenUpdateDocumentationFieldsDataProductWithNonExistentUuidThenReturnNotFound() {
        // Given - Update command with non-existent uuid
        DataProductDocumentationFieldsRes updateFields = new DataProductDocumentationFieldsRes();
        updateFields.setUuid("non-existent-uuid-12345");
        updateFields.setDisplayName("Updated Display Name");

        DataProductDocumentationFieldsUpdateCommandRes updateCommand = new DataProductDocumentationFieldsUpdateCommandRes();
        updateCommand.setDataProduct(updateFields);

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/update-documentation-fields"),
                new HttpEntity<>(updateCommand),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ========== HELPER METHODS ==========

    private void cleanupDataProduct(String uuid) {
        try {
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + uuid));
        } catch (Exception e) {
            // Ignore cleanup errors in tests
        }
    }
}
