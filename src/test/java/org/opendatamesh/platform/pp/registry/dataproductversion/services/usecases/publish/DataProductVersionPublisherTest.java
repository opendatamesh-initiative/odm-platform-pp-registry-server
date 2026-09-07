package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.publish;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductAdditionalRepo;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductValidationState;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersionAdditionalTag;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersionShort;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersionValidationState;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.utils.usecases.TransactionalOutboundPort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataProductVersionPublisherTest {

    @Mock
    private DataProductVersionPublishPresenter presenter;

    @Mock
    private DataProductVersionPublisherNotificationOutboundPort notificationsPort;

    @Mock
    private DataProductVersionPublisherDataProductVersionPersistenceOutboundPort dataProductVersionPersistencePort;

    @Mock
    private DataProductVersionPublisherDescriptorOutboundPort descriptorHandlerPort;

    @Mock
    private DataProductVersionPublisherDataProductPersistenceOutboundPort dataProductPersistencePort;

    @Mock
    private TransactionalOutboundPort transactionalPort;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUpDescriptorPortMocks() {
        lenient().when(descriptorHandlerPort.enrichDescriptorContentIfNeeded(anyString(), anyString(), any(JsonNode.class)))
                .thenAnswer(inv -> inv.getArgument(2));
    }

    private void mockDescriptorPortForSuccessfulPublish(DataProduct dataProduct, DataProductVersion dataProductVersion) {
        when(descriptorHandlerPort.extractFullyQualifiedName(any(JsonNode.class))).thenReturn(dataProduct.getFqn());
        when(descriptorHandlerPort.extractVersionNumber(anyString(), anyString(), any(JsonNode.class))).thenReturn(dataProductVersion.getVersionNumber());
    }

    @Test
    void whenCommandIsNullThenThrowBadRequestException() {
        // Given
        DataProductVersionPublishCommand command = null;
        DataProductVersionPublisher publisher = new DataProductVersionPublisher(
                command, presenter, notificationsPort, dataProductVersionPersistencePort, dataProductPersistencePort, descriptorHandlerPort, transactionalPort);

        // When & Then
        assertThatThrownBy(() -> publisher.execute())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("DataProductVersionPublishCommand cannot be null");

        verifyNoInteractions(dataProductVersionPersistencePort, dataProductPersistencePort, notificationsPort, presenter, transactionalPort);
    }

    @Test
    void whenDataProductVersionIsNullThenThrowBadRequestException() {
        // Given
        DataProductVersionPublishCommand command = new DataProductVersionPublishCommand(null);
        DataProductVersionPublisher publisher = new DataProductVersionPublisher(
                command, presenter, notificationsPort, dataProductVersionPersistencePort, dataProductPersistencePort, descriptorHandlerPort, transactionalPort);

        // When & Then
        assertThatThrownBy(() -> publisher.execute())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("DataProductVersion cannot be null");

        verifyNoInteractions(dataProductVersionPersistencePort, dataProductPersistencePort, notificationsPort, presenter, transactionalPort);
    }

    @Test
    void whenDataProductUuidIsNullThenThrowBadRequestException() {
        // Given
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setDataProductUuid(null);
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setVersionNumber("v1.0.0");
        dataProductVersion.setSpec("dpds");
        dataProductVersion.setSpecVersion("1.0.0");
        
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        DataProductVersionPublishCommand command = new DataProductVersionPublishCommand(dataProductVersion);
        DataProductVersionPublisher publisher = new DataProductVersionPublisher(
                command, presenter, notificationsPort, dataProductVersionPersistencePort, dataProductPersistencePort, descriptorHandlerPort, transactionalPort);

        // When & Then
        assertThatThrownBy(() -> publisher.execute())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Missing DataProduct on DataProductVersion");

        verifyNoInteractions(dataProductVersionPersistencePort, dataProductPersistencePort, notificationsPort, presenter, transactionalPort);
    }

    @Test
    void whenDataProductUuidIsEmptyThenThrowBadRequestException() {
        // Given
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setDataProductUuid("");
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setVersionNumber("v1.0.0");
        dataProductVersion.setSpec("dpds");
        dataProductVersion.setSpecVersion("1.0.0");
        
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        DataProductVersionPublishCommand command = new DataProductVersionPublishCommand(dataProductVersion);
        DataProductVersionPublisher publisher = new DataProductVersionPublisher(
                command, presenter, notificationsPort, dataProductVersionPersistencePort, dataProductPersistencePort, descriptorHandlerPort, transactionalPort);

        // When & Then
        assertThatThrownBy(() -> publisher.execute())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Missing DataProduct on DataProductVersion");

        verifyNoInteractions(dataProductVersionPersistencePort, dataProductPersistencePort, notificationsPort, presenter, transactionalPort);
    }

    @Test
    void whenDataProductUuidIsBlankThenThrowBadRequestException() {
        // Given
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setDataProductUuid("   ");
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setVersionNumber("v1.0.0");
        dataProductVersion.setSpec("dpds");
        dataProductVersion.setSpecVersion("1.0.0");
        
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        DataProductVersionPublishCommand command = new DataProductVersionPublishCommand(dataProductVersion);
        DataProductVersionPublisher publisher = new DataProductVersionPublisher(
                command, presenter, notificationsPort, dataProductVersionPersistencePort, dataProductPersistencePort, descriptorHandlerPort, transactionalPort);

        // When & Then
        assertThatThrownBy(() -> publisher.execute())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Missing DataProduct on DataProductVersion");

        verifyNoInteractions(dataProductVersionPersistencePort, dataProductPersistencePort, notificationsPort, presenter, transactionalPort);
    }

    @Test
    void whenNameIsNullThenThrowBadRequestException() {
        // Given
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setDataProductUuid("data-product-uuid-123");
        dataProductVersion.setName(null);
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setVersionNumber("v1.0.0");
        dataProductVersion.setSpec("dpds");
        dataProductVersion.setSpecVersion("1.0.0");
        
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        DataProductVersionPublishCommand command = new DataProductVersionPublishCommand(dataProductVersion);
        DataProductVersionPublisher publisher = new DataProductVersionPublisher(
                command, presenter, notificationsPort, dataProductVersionPersistencePort, dataProductPersistencePort, descriptorHandlerPort, transactionalPort);

        // When & Then
        assertThatThrownBy(() -> publisher.execute())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Missing Data Product Version name");

        verifyNoInteractions(dataProductVersionPersistencePort, dataProductPersistencePort, notificationsPort, presenter, transactionalPort);
    }

    @Test
    void whenNameIsEmptyThenThrowBadRequestException() {
        // Given
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setDataProductUuid("data-product-uuid-123");
        dataProductVersion.setName("");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setVersionNumber("v1.0.0");
        dataProductVersion.setSpec("dpds");
        dataProductVersion.setSpecVersion("1.0.0");
        
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        DataProductVersionPublishCommand command = new DataProductVersionPublishCommand(dataProductVersion);
        DataProductVersionPublisher publisher = new DataProductVersionPublisher(
                command, presenter, notificationsPort, dataProductVersionPersistencePort, dataProductPersistencePort, descriptorHandlerPort, transactionalPort);

        // When & Then
        assertThatThrownBy(() -> publisher.execute())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Missing Data Product Version name");

        verifyNoInteractions(dataProductVersionPersistencePort, dataProductPersistencePort, notificationsPort, presenter, transactionalPort);
    }

    @Test
    void whenNameIsBlankThenThrowBadRequestException() {
        // Given
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setDataProductUuid("data-product-uuid-123");
        dataProductVersion.setName("   ");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setVersionNumber("v1.0.0");
        dataProductVersion.setSpec("dpds");
        dataProductVersion.setSpecVersion("1.0.0");
        
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        DataProductVersionPublishCommand command = new DataProductVersionPublishCommand(dataProductVersion);
        DataProductVersionPublisher publisher = new DataProductVersionPublisher(
                command, presenter, notificationsPort, dataProductVersionPersistencePort, dataProductPersistencePort, descriptorHandlerPort, transactionalPort);

        // When & Then
        assertThatThrownBy(() -> publisher.execute())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Missing Data Product Version name");

        verifyNoInteractions(dataProductVersionPersistencePort, dataProductPersistencePort, notificationsPort, presenter, transactionalPort);
    }

    @Test
    void whenContentIsNullThenThrowBadRequestException() {
        // Given
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setDataProductUuid("data-product-uuid-123");
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setVersionNumber("v1.0.0");
        dataProductVersion.setSpec("dpds");
        dataProductVersion.setSpecVersion("1.0.0");
        
        dataProductVersion.setContent(null);
        DataProductVersionPublishCommand command = new DataProductVersionPublishCommand(dataProductVersion);
        DataProductVersionPublisher publisher = new DataProductVersionPublisher(
                command, presenter, notificationsPort, dataProductVersionPersistencePort, dataProductPersistencePort, descriptorHandlerPort, transactionalPort);

        // When & Then
        assertThatThrownBy(() -> publisher.execute())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Missing Data Product Version content");

        verifyNoInteractions(dataProductVersionPersistencePort, dataProductPersistencePort, notificationsPort, presenter, transactionalPort);
    }

    @Test
    void whenNoExistingDataProductVersionThenPublishSuccessfully() {
        // Given
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setDataProductUuid("data-product-uuid-123");
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setVersionNumber("v1.0.0");
        dataProductVersion.setSpec("dpds");
        dataProductVersion.setSpecVersion("1.0.0");
        
        // Create a simple JSON content
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        
        DataProductVersionPublishCommand command = new DataProductVersionPublishCommand(dataProductVersion);

        when(dataProductVersionPersistencePort.findByDataProductUuidAndVersionNumber(dataProductVersion.getDataProductUuid(), dataProductVersion.getVersionNumber()))
                .thenReturn(Optional.empty());
        when(dataProductVersionPersistencePort.save(any(DataProductVersion.class))).thenReturn(dataProductVersion);
        when(dataProductVersionPersistencePort.findLatestByDataProductUuidExcludingUuid(dataProductVersion.getDataProductUuid(), dataProductVersion.getUuid()))
                .thenReturn(Optional.empty());
        
        DataProduct dataProduct = new DataProduct();
        dataProduct.setUuid(dataProductVersion.getDataProductUuid());
        dataProduct.setFqn("test.domain.TestProduct");
        dataProduct.setValidationState(DataProductValidationState.APPROVED);
        when(dataProductPersistencePort.findByUuid(dataProductVersion.getDataProductUuid())).thenReturn(dataProduct);
        mockDescriptorPortForSuccessfulPublish(dataProduct, dataProductVersion);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductVersionPublisher publisher = new DataProductVersionPublisher(
                command, presenter, notificationsPort, dataProductVersionPersistencePort, dataProductPersistencePort, descriptorHandlerPort, transactionalPort);

        // When
        publisher.execute();

        // Then
        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(dataProductPersistencePort).findByUuid(dataProductVersion.getDataProductUuid());
        verify(dataProductVersionPersistencePort).findByDataProductUuidAndVersionNumber(dataProductVersion.getDataProductUuid(), dataProductVersion.getVersionNumber());
        verify(dataProductVersionPersistencePort).save(any(DataProductVersion.class));
        verify(dataProductVersionPersistencePort).findLatestByDataProductUuidExcludingUuid(dataProductVersion.getDataProductUuid(), dataProductVersion.getUuid());
        verify(notificationsPort).emitDataProductVersionPublicationRequested(dataProductVersion, null);
        verify(presenter).presentDataProductVersionPublished(dataProductVersion);

        // Verify that validation state is set to PENDING
        verify(dataProductVersionPersistencePort).save(argThat(savedDataProductVersion ->
                DataProductVersionValidationState.PENDING.equals(savedDataProductVersion.getValidationState())));
    }

    @Test
    void whenExistingDataProductVersionIsPendingThenThrowBadRequestException() {
        // Given
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setDataProductUuid("data-product-uuid-123");
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setVersionNumber("v1.0.0");
        dataProductVersion.setSpec("dpds");
        dataProductVersion.setSpecVersion("1.0.0");
        
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        DataProductVersionPublishCommand command = new DataProductVersionPublishCommand(dataProductVersion);

        DataProductVersionShort existingDataProductVersion = new DataProductVersionShort();
        existingDataProductVersion.setUuid("existing-uuid");
        existingDataProductVersion.setDataProductUuid(dataProductVersion.getDataProductUuid());
        existingDataProductVersion.setTag(dataProductVersion.getTag());
        existingDataProductVersion.setVersionNumber(dataProductVersion.getVersionNumber());
        existingDataProductVersion.setValidationState(DataProductVersionValidationState.PENDING);

        when(dataProductVersionPersistencePort.findByDataProductUuidAndVersionNumber(dataProductVersion.getDataProductUuid(), dataProductVersion.getVersionNumber()))
                .thenReturn(Optional.of(existingDataProductVersion));

        // Mock the data product lookup
        DataProduct dataProduct = new DataProduct();
        dataProduct.setUuid(dataProductVersion.getDataProductUuid());
        dataProduct.setFqn("test-domain:test-product");
        dataProduct.setValidationState(DataProductValidationState.APPROVED);
        when(dataProductPersistencePort.findByUuid(dataProductVersion.getDataProductUuid()))
                .thenReturn(dataProduct);
        mockDescriptorPortForSuccessfulPublish(dataProduct, dataProductVersion);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductVersionPublisher publisher = new DataProductVersionPublisher(
                command, presenter, notificationsPort, dataProductVersionPersistencePort, dataProductPersistencePort, descriptorHandlerPort, transactionalPort);

        // When & Then
        assertThatThrownBy(() -> publisher.execute())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Impossible to publish a data product version already existent and in PENDING validation state.");

        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(dataProductVersionPersistencePort).findByDataProductUuidAndVersionNumber(dataProductVersion.getDataProductUuid(), dataProductVersion.getVersionNumber());
        verify(dataProductPersistencePort).findByUuid(dataProductVersion.getDataProductUuid());
        verifyNoMoreInteractions(dataProductVersionPersistencePort, dataProductPersistencePort);
        verifyNoInteractions(notificationsPort, presenter);
    }

    @Test
    void whenExistingDataProductVersionIsApprovedThenThrowBadRequestException() {
        // Given
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setDataProductUuid("data-product-uuid-123");
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setVersionNumber("v1.0.0");
        dataProductVersion.setSpec("dpds");
        dataProductVersion.setSpecVersion("1.0.0");
        
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        DataProductVersionPublishCommand command = new DataProductVersionPublishCommand(dataProductVersion);

        DataProductVersionShort existingDataProductVersion = new DataProductVersionShort();
        existingDataProductVersion.setUuid("existing-uuid");
        existingDataProductVersion.setDataProductUuid(dataProductVersion.getDataProductUuid());
        existingDataProductVersion.setTag(dataProductVersion.getTag());
        existingDataProductVersion.setVersionNumber(dataProductVersion.getVersionNumber());
        existingDataProductVersion.setValidationState(DataProductVersionValidationState.APPROVED);

        when(dataProductVersionPersistencePort.findByDataProductUuidAndVersionNumber(dataProductVersion.getDataProductUuid(), dataProductVersion.getVersionNumber()))
                .thenReturn(Optional.of(existingDataProductVersion));

        // Mock the data product lookup
        DataProduct dataProduct = new DataProduct();
        dataProduct.setUuid(dataProductVersion.getDataProductUuid());
        dataProduct.setFqn("test-domain:test-product");
        dataProduct.setValidationState(DataProductValidationState.APPROVED);
        when(dataProductPersistencePort.findByUuid(dataProductVersion.getDataProductUuid()))
                .thenReturn(dataProduct);
        mockDescriptorPortForSuccessfulPublish(dataProduct, dataProductVersion);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductVersionPublisher publisher = new DataProductVersionPublisher(
                command, presenter, notificationsPort, dataProductVersionPersistencePort, dataProductPersistencePort, descriptorHandlerPort, transactionalPort);

        // When & Then
        assertThatThrownBy(() -> publisher.execute())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Impossible to publish a data product version already existent and APPROVED.");

        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(dataProductVersionPersistencePort).findByDataProductUuidAndVersionNumber(dataProductVersion.getDataProductUuid(), dataProductVersion.getVersionNumber());
        verify(dataProductPersistencePort).findByUuid(dataProductVersion.getDataProductUuid());
        verifyNoMoreInteractions(dataProductVersionPersistencePort, dataProductPersistencePort);
        verifyNoInteractions(notificationsPort, presenter);
    }

    @Test
    void whenExistingDataProductVersionIsRejectedThenDeleteAndPublishSuccessfully() {
        // Given
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setDataProductUuid("data-product-uuid-123");
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setVersionNumber("v1.0.0");
        dataProductVersion.setSpec("dpds");
        dataProductVersion.setSpecVersion("1.0.0");
        
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        DataProductVersionPublishCommand command = new DataProductVersionPublishCommand(dataProductVersion);

        DataProductVersionShort existingDataProductVersion = new DataProductVersionShort();
        existingDataProductVersion.setUuid("existing-uuid");
        existingDataProductVersion.setDataProductUuid(dataProductVersion.getDataProductUuid());
        existingDataProductVersion.setTag(dataProductVersion.getTag());
        existingDataProductVersion.setVersionNumber(dataProductVersion.getVersionNumber());
        existingDataProductVersion.setValidationState(DataProductVersionValidationState.REJECTED);

        when(dataProductVersionPersistencePort.findByDataProductUuidAndVersionNumber(dataProductVersion.getDataProductUuid(), dataProductVersion.getVersionNumber()))
                .thenReturn(Optional.of(existingDataProductVersion));
        when(dataProductVersionPersistencePort.save(any(DataProductVersion.class))).thenReturn(dataProductVersion);
        when(dataProductVersionPersistencePort.findLatestByDataProductUuidExcludingUuid(dataProductVersion.getDataProductUuid(), dataProductVersion.getUuid()))
                .thenReturn(Optional.empty());
        
        DataProduct dataProduct = new DataProduct();
        dataProduct.setUuid(dataProductVersion.getDataProductUuid());
        dataProduct.setFqn("test.domain.TestProduct");
        dataProduct.setValidationState(DataProductValidationState.APPROVED);
        when(dataProductPersistencePort.findByUuid(dataProductVersion.getDataProductUuid())).thenReturn(dataProduct);
        mockDescriptorPortForSuccessfulPublish(dataProduct, dataProductVersion);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductVersionPublisher publisher = new DataProductVersionPublisher(
                command, presenter, notificationsPort, dataProductVersionPersistencePort, dataProductPersistencePort, descriptorHandlerPort, transactionalPort);

        // When
        publisher.execute();

        // Then
        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(dataProductPersistencePort).findByUuid(dataProductVersion.getDataProductUuid());
        verify(dataProductVersionPersistencePort).findByDataProductUuidAndVersionNumber(dataProductVersion.getDataProductUuid(), dataProductVersion.getVersionNumber());
        verify(dataProductVersionPersistencePort).delete(existingDataProductVersion.getUuid());
        verify(dataProductVersionPersistencePort).save(any(DataProductVersion.class));
        verify(dataProductVersionPersistencePort).findLatestByDataProductUuidExcludingUuid(dataProductVersion.getDataProductUuid(), dataProductVersion.getUuid());
        verify(notificationsPort).emitDataProductVersionPublicationRequested(dataProductVersion, null);
        verify(presenter).presentDataProductVersionPublished(dataProductVersion);

        // Verify that validation state is set to PENDING
        verify(dataProductVersionPersistencePort).save(argThat(savedDataProductVersion ->
                DataProductVersionValidationState.PENDING.equals(savedDataProductVersion.getValidationState())));
    }

    @Test
    void whenExecuteThenAllOperationsHappenInTransaction() {
        // Given
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setDataProductUuid("data-product-uuid-123");
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setVersionNumber("v1.0.0");
        dataProductVersion.setSpec("dpds");
        dataProductVersion.setSpecVersion("1.0.0");
        
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        DataProductVersionPublishCommand command = new DataProductVersionPublishCommand(dataProductVersion);

        when(dataProductVersionPersistencePort.findByDataProductUuidAndVersionNumber(dataProductVersion.getDataProductUuid(), dataProductVersion.getVersionNumber()))
                .thenReturn(Optional.empty());
        when(dataProductVersionPersistencePort.save(any(DataProductVersion.class))).thenReturn(dataProductVersion);
        when(dataProductVersionPersistencePort.findLatestByDataProductUuidExcludingUuid(dataProductVersion.getDataProductUuid(), dataProductVersion.getUuid()))
                .thenReturn(Optional.empty());
        
        DataProduct dataProduct = new DataProduct();
        dataProduct.setUuid(dataProductVersion.getDataProductUuid());
        dataProduct.setFqn("test.domain.TestProduct");
        dataProduct.setValidationState(DataProductValidationState.APPROVED);
        when(dataProductPersistencePort.findByUuid(dataProductVersion.getDataProductUuid())).thenReturn(dataProduct);
        mockDescriptorPortForSuccessfulPublish(dataProduct, dataProductVersion);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductVersionPublisher publisher = new DataProductVersionPublisher(
                command, presenter, notificationsPort, dataProductVersionPersistencePort, dataProductPersistencePort, descriptorHandlerPort, transactionalPort);

        // When
        publisher.execute();

        // Then
        verify(transactionalPort).doInTransaction(any(Runnable.class));

        // Verify that all persistence operations happen within the transaction
        verify(dataProductPersistencePort).findByUuid(dataProductVersion.getDataProductUuid());
        verify(dataProductVersionPersistencePort).findByDataProductUuidAndVersionNumber(dataProductVersion.getDataProductUuid(), dataProductVersion.getVersionNumber());
        verify(dataProductVersionPersistencePort).save(any(DataProductVersion.class));
        verify(dataProductVersionPersistencePort).findLatestByDataProductUuidExcludingUuid(dataProductVersion.getDataProductUuid(), dataProductVersion.getUuid());
        verify(notificationsPort).emitDataProductVersionPublicationRequested(dataProductVersion, null);
        verify(presenter).presentDataProductVersionPublished(dataProductVersion);
    }

    @Test
    void whenDataProductIsNotApprovedThenThrowBadRequestException() {
        // Given
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setDataProductUuid("data-product-uuid-123");
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setVersionNumber("v1.0.0");
        dataProductVersion.setSpec("dpds");
        dataProductVersion.setSpecVersion("1.0.0");
        
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        DataProductVersionPublishCommand command = new DataProductVersionPublishCommand(dataProductVersion);

        DataProduct dataProduct = new DataProduct();
        dataProduct.setUuid(dataProductVersion.getDataProductUuid());
        dataProduct.setFqn("test.domain.TestProduct");
        dataProduct.setValidationState(DataProductValidationState.PENDING);
        when(dataProductPersistencePort.findByUuid(dataProductVersion.getDataProductUuid())).thenReturn(dataProduct);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductVersionPublisher publisher = new DataProductVersionPublisher(
                command, presenter, notificationsPort, dataProductVersionPersistencePort, dataProductPersistencePort, descriptorHandlerPort, transactionalPort);

        // When & Then
        assertThatThrownBy(() -> publisher.execute())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Data Product test.domain.TestProduct must be APPROVED in order to publish a Data Product Version.");

        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(dataProductPersistencePort).findByUuid(dataProductVersion.getDataProductUuid());
        verifyNoMoreInteractions(dataProductVersionPersistencePort);
        verifyNoInteractions(notificationsPort, presenter);
    }

    @Test
    void whenPublishSuccessfullyThenPresenterReceivesCorrectDataProductVersion() {
        // Given
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setDataProductUuid("data-product-uuid-123");
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setVersionNumber("v1.0.0");
        dataProductVersion.setSpec("dpds");
        dataProductVersion.setSpecVersion("1.0.0");
        
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        DataProductVersionPublishCommand command = new DataProductVersionPublishCommand(dataProductVersion);

        when(dataProductVersionPersistencePort.findByDataProductUuidAndVersionNumber(dataProductVersion.getDataProductUuid(), dataProductVersion.getVersionNumber()))
                .thenReturn(Optional.empty());
        when(dataProductVersionPersistencePort.save(any(DataProductVersion.class))).thenReturn(dataProductVersion);
        when(dataProductVersionPersistencePort.findLatestByDataProductUuidExcludingUuid(dataProductVersion.getDataProductUuid(), dataProductVersion.getUuid()))
                .thenReturn(Optional.empty());
        
        DataProduct dataProduct = new DataProduct();
        dataProduct.setUuid(dataProductVersion.getDataProductUuid());
        dataProduct.setFqn("test.domain.TestProduct");
        dataProduct.setValidationState(DataProductValidationState.APPROVED);
        when(dataProductPersistencePort.findByUuid(dataProductVersion.getDataProductUuid())).thenReturn(dataProduct);
        mockDescriptorPortForSuccessfulPublish(dataProduct, dataProductVersion);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductVersionPublisher publisher = new DataProductVersionPublisher(
                command, presenter, notificationsPort, dataProductVersionPersistencePort, dataProductPersistencePort, descriptorHandlerPort, transactionalPort);

        // When
        publisher.execute();

        // Then
        verify(presenter).presentDataProductVersionPublished(argThat(presentedDataProductVersion -> {
            assertThat(presentedDataProductVersion)
                    .usingRecursiveComparison()
                    .isEqualTo(dataProductVersion);
            return true;
        }));
    }

    @Test
    void whenPublishSubsequentVersionThenPreviousVersionIsRetrievedAndPassed() {
        // Given
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setDataProductUuid("data-product-uuid-123");
        dataProductVersion.setName("Test Version 2");
        dataProductVersion.setDescription("Test Version 2 Description");
        dataProductVersion.setTag("v2.0.0");
        dataProductVersion.setVersionNumber("2.0.0");
        dataProductVersion.setSpec("dpds");
        dataProductVersion.setSpecVersion("1.0.0");
        
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version 2")
                .put("version", "2.0.0");
        dataProductVersion.setContent(content);
        DataProductVersionPublishCommand command = new DataProductVersionPublishCommand(dataProductVersion);

        DataProductVersionShort previousVersionShort = new DataProductVersionShort();
        previousVersionShort.setUuid("previous-uuid-456");
        previousVersionShort.setDataProductUuid(dataProductVersion.getDataProductUuid());
        previousVersionShort.setTag("v1.0.0");
        previousVersionShort.setVersionNumber("1.0.0");
        previousVersionShort.setName("Test Version 1");
        previousVersionShort.setValidationState(DataProductVersionValidationState.APPROVED);

        DataProductVersion previousVersion = new DataProductVersion();
        previousVersion.setUuid(previousVersionShort.getUuid());
        previousVersion.setDataProductUuid(previousVersionShort.getDataProductUuid());
        previousVersion.setTag(previousVersionShort.getTag());
        previousVersion.setName(previousVersionShort.getName());
        previousVersion.setValidationState(previousVersionShort.getValidationState());

        when(dataProductVersionPersistencePort.findByDataProductUuidAndVersionNumber(dataProductVersion.getDataProductUuid(), dataProductVersion.getVersionNumber()))
                .thenReturn(Optional.empty());
        when(dataProductVersionPersistencePort.save(any(DataProductVersion.class))).thenReturn(dataProductVersion);
        when(dataProductVersionPersistencePort.findLatestByDataProductUuidExcludingUuid(dataProductVersion.getDataProductUuid(), dataProductVersion.getUuid()))
                .thenReturn(Optional.of(previousVersionShort));
        when(dataProductVersionPersistencePort.findByUuid(previousVersionShort.getUuid()))
                .thenReturn(previousVersion);

        DataProduct dataProduct = new DataProduct();
        dataProduct.setUuid(dataProductVersion.getDataProductUuid());
        dataProduct.setFqn("test.domain.TestProduct");
        dataProduct.setValidationState(DataProductValidationState.APPROVED);
        when(dataProductPersistencePort.findByUuid(dataProductVersion.getDataProductUuid())).thenReturn(dataProduct);
        mockDescriptorPortForSuccessfulPublish(dataProduct, dataProductVersion);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductVersionPublisher publisher = new DataProductVersionPublisher(
                command, presenter, notificationsPort, dataProductVersionPersistencePort, dataProductPersistencePort, descriptorHandlerPort, transactionalPort);

        // When
        publisher.execute();

        // Then
        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(dataProductPersistencePort).findByUuid(dataProductVersion.getDataProductUuid());
        verify(dataProductVersionPersistencePort).findByDataProductUuidAndVersionNumber(dataProductVersion.getDataProductUuid(), dataProductVersion.getVersionNumber());
        verify(dataProductVersionPersistencePort).save(any(DataProductVersion.class));
        verify(dataProductVersionPersistencePort).findLatestByDataProductUuidExcludingUuid(dataProductVersion.getDataProductUuid(), dataProductVersion.getUuid());
        verify(dataProductVersionPersistencePort).findByUuid(previousVersionShort.getUuid());
        verify(notificationsPort).emitDataProductVersionPublicationRequested(dataProductVersion, previousVersion);
        verify(presenter).presentDataProductVersionPublished(dataProductVersion);
    }

    @Test
    void whenDescriptorFqnDoesNotMatchDataProductFqnThenThrowBadRequestException() {
        // Given
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setDataProductUuid("data-product-uuid-123");
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setVersionNumber("v1.0.0");
        dataProductVersion.setSpec("dpds");
        dataProductVersion.setSpecVersion("1.0.0");

        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        DataProductVersionPublishCommand command = new DataProductVersionPublishCommand(dataProductVersion);

        DataProduct dataProduct = new DataProduct();
        dataProduct.setUuid(dataProductVersion.getDataProductUuid());
        dataProduct.setFqn("test.domain.ExpectedProduct");
        dataProduct.setValidationState(DataProductValidationState.APPROVED);
        when(dataProductPersistencePort.findByUuid(dataProductVersion.getDataProductUuid())).thenReturn(dataProduct);
        when(descriptorHandlerPort.extractFullyQualifiedName(dataProductVersion.getContent())).thenReturn("wrong.fqn.in.descriptor");

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductVersionPublisher publisher = new DataProductVersionPublisher(
                command, presenter, notificationsPort, dataProductVersionPersistencePort, dataProductPersistencePort, descriptorHandlerPort, transactionalPort);

        // When & Then
        assertThatThrownBy(() -> publisher.execute())
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("The descriptor's info.fullyQualifiedName does not match the Data Product FQN")
                .hasMessageContaining("Expected: test.domain.ExpectedProduct")
                .hasMessageContaining("found: wrong.fqn.in.descriptor");

        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(dataProductPersistencePort).findByUuid(dataProductVersion.getDataProductUuid());
        verify(descriptorHandlerPort).extractFullyQualifiedName(dataProductVersion.getContent());
        verifyNoMoreInteractions(dataProductVersionPersistencePort);
        verifyNoInteractions(notificationsPort, presenter);
    }

    /**
     * Scenario: Missing additional tag is rejected
     * Given the product has additional remotes "infra-repo" and "app-repo"
     * When the client publishes with an additional tag only for "infra-repo"
     * Then the request fails with a bad request
     */
    @Test
    void whenAdditionalRemoteMissingTagThenThrowBadRequestException() {
        DataProductVersion dataProductVersion = basePublishableVersion();
        dataProductVersion.setAdditionalTags(List.of(additionalTag("infra-repo", "1.0.0")));

        DataProduct dataProduct = approvedProduct(dataProductVersion.getDataProductUuid());
        dataProduct.setAdditionalDataProductRepos(List.of(
                additionalRepo("infra-repo"),
                additionalRepo("app-repo")
        ));
        when(dataProductPersistencePort.findByUuid(dataProductVersion.getDataProductUuid())).thenReturn(dataProduct);
        when(descriptorHandlerPort.extractFullyQualifiedName(any(JsonNode.class))).thenReturn(dataProduct.getFqn());
        stubTransaction();

        DataProductVersionPublisher publisher = newPublisher(dataProductVersion);

        assertThatThrownBy(publisher::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Missing additional tag")
                .hasMessageContaining("app-repo");

        verify(dataProductPersistencePort).findByUuid(dataProductVersion.getDataProductUuid());
        verifyNoInteractions(notificationsPort, presenter);
        verify(dataProductVersionPersistencePort, never()).save(any());
    }

    /**
     * Scenario: Unknown additional tag key is rejected
     * Given the product has additional remote "infra-repo"
     * And the client provides a complete tag for "infra-repo"
     * When the client also publishes an additional tag keyed "other-repo"
     * Then the request fails with a bad request
     */
    @Test
    void whenAdditionalTagKeyUnknownThenThrowBadRequestException() {
        DataProductVersion dataProductVersion = basePublishableVersion();
        dataProductVersion.setAdditionalTags(List.of(
                additionalTag("infra-repo", "1.0.0"),
                additionalTag("other-repo", "1.0.0")
        ));

        DataProduct dataProduct = approvedProduct(dataProductVersion.getDataProductUuid());
        dataProduct.setAdditionalDataProductRepos(List.of(additionalRepo("infra-repo")));
        when(dataProductPersistencePort.findByUuid(dataProductVersion.getDataProductUuid())).thenReturn(dataProduct);
        when(descriptorHandlerPort.extractFullyQualifiedName(any(JsonNode.class))).thenReturn(dataProduct.getFqn());
        stubTransaction();

        DataProductVersionPublisher publisher = newPublisher(dataProductVersion);

        assertThatThrownBy(publisher::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Unknown additional tag repository key")
                .hasMessageContaining("other-repo");

        verify(dataProductVersionPersistencePort, never()).save(any());
        verifyNoInteractions(notificationsPort, presenter);
    }

    /**
     * Scenario: Additional tags on a mono-repo product are rejected
     * Given the product has no additional remotes
     * When the client publishes with any additional tag
     * Then the request fails with a bad request
     */
    @Test
    void whenAdditionalTagsPresentWithoutRemotesThenThrowBadRequestException() {
        DataProductVersion dataProductVersion = basePublishableVersion();
        dataProductVersion.setAdditionalTags(List.of(additionalTag("infra-repo", "1.0.0")));

        DataProduct dataProduct = approvedProduct(dataProductVersion.getDataProductUuid());
        dataProduct.setAdditionalDataProductRepos(null);
        when(dataProductPersistencePort.findByUuid(dataProductVersion.getDataProductUuid())).thenReturn(dataProduct);
        when(descriptorHandlerPort.extractFullyQualifiedName(any(JsonNode.class))).thenReturn(dataProduct.getFqn());
        stubTransaction();

        DataProductVersionPublisher publisher = newPublisher(dataProductVersion);

        assertThatThrownBy(publisher::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Additional tags must be empty when the data product has no additional repositories");

        verify(dataProductVersionPersistencePort, never()).save(any());
        verifyNoInteractions(notificationsPort, presenter);
    }

    @Test
    void whenAdditionalTagsCompleteForPolyrepoThenPublishSucceeds() {
        DataProductVersion dataProductVersion = basePublishableVersion();
        dataProductVersion.setAdditionalTags(List.of(
                additionalTag("infra-repo", "1.0.0"),
                additionalTag("app-repo", "1.0.0")
        ));

        DataProduct dataProduct = approvedProduct(dataProductVersion.getDataProductUuid());
        dataProduct.setAdditionalDataProductRepos(List.of(
                additionalRepo("infra-repo"),
                additionalRepo("app-repo")
        ));
        when(dataProductPersistencePort.findByUuid(dataProductVersion.getDataProductUuid())).thenReturn(dataProduct);
        when(dataProductVersionPersistencePort.findByDataProductUuidAndVersionNumber(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(dataProductVersionPersistencePort.save(any(DataProductVersion.class))).thenReturn(dataProductVersion);
        when(dataProductVersionPersistencePort.findLatestByDataProductUuidExcludingUuid(anyString(), anyString()))
                .thenReturn(Optional.empty());
        mockDescriptorPortForSuccessfulPublish(dataProduct, dataProductVersion);
        stubTransaction();

        DataProductVersionPublisher publisher = newPublisher(dataProductVersion);
        publisher.execute();

        verify(dataProductVersionPersistencePort).save(argThat(saved ->
                saved.getAdditionalTags() != null && saved.getAdditionalTags().size() == 2));
        verify(presenter).presentDataProductVersionPublished(dataProductVersion);
        verify(notificationsPort).emitDataProductVersionPublicationRequested(dataProductVersion, null);
    }

    private DataProductVersionPublisher newPublisher(DataProductVersion dataProductVersion) {
        return new DataProductVersionPublisher(
                new DataProductVersionPublishCommand(dataProductVersion),
                presenter,
                notificationsPort,
                dataProductVersionPersistencePort,
                dataProductPersistencePort,
                descriptorHandlerPort,
                transactionalPort);
    }

    private void stubTransaction() {
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));
    }

    private DataProductVersion basePublishableVersion() {
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setDataProductUuid("data-product-uuid-123");
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setVersionNumber("v1.0.0");
        dataProductVersion.setSpec("dpds");
        dataProductVersion.setSpecVersion("1.0.0");
        dataProductVersion.setContent(objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0"));
        return dataProductVersion;
    }

    private DataProduct approvedProduct(String uuid) {
        DataProduct dataProduct = new DataProduct();
        dataProduct.setUuid(uuid);
        dataProduct.setFqn("test.domain.TestProduct");
        dataProduct.setValidationState(DataProductValidationState.APPROVED);
        return dataProduct;
    }

    private DataProductAdditionalRepo additionalRepo(String repositoryKey) {
        DataProductAdditionalRepo repo = new DataProductAdditionalRepo();
        repo.setRepositoryKey(repositoryKey);
        repo.setName(repositoryKey);
        return repo;
    }

    private DataProductVersionAdditionalTag additionalTag(String repositoryKey, String tag) {
        DataProductVersionAdditionalTag additionalTag = new DataProductVersionAdditionalTag();
        additionalTag.setRepositoryKey(repositoryKey);
        additionalTag.setTag(tag);
        return additionalTag;
    }

}
