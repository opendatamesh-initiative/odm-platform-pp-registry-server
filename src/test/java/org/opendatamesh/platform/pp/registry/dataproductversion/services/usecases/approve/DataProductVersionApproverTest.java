package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.approve;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductValidationState;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersionValidationState;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.utils.usecases.TransactionalOutboundPort;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class DataProductVersionApproverTest {

    @Mock
    private DataProductVersionApprovePresenter presenter;

    @Mock
    private DataProductVersionApproverNotificationOutboundPort notificationsPort;

    @Mock
    private DataProductVersionApproverPersistenceOutboundPort persistencePort;

    @Mock
    private TransactionalOutboundPort transactionalPort;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private DataProductVersion createDataProductVersionWithDataProduct(String uuid, String name, String tag, 
                                                                      DataProductVersionValidationState versionState,
                                                                      DataProductValidationState dataProductState) {
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid(uuid);
        dataProductVersion.setDataProductUuid("data-product-uuid-123");
        dataProductVersion.setName(name);
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag(tag);
        dataProductVersion.setSpec("opendatamesh");
        dataProductVersion.setSpecVersion("1.0.0");
        dataProductVersion.setValidationState(versionState);
        
        JsonNode content = objectMapper.createObjectNode()
                .put("name", name)
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        
        // Set up the DataProduct relationship
        DataProduct dataProduct = new DataProduct();
        dataProduct.setUuid("data-product-uuid-123");
        dataProduct.setFqn("test.domain:test-product");
        dataProduct.setName("Test Product");
        dataProduct.setDomain("test.domain");
        dataProduct.setValidationState(dataProductState);
        dataProductVersion.setDataProduct(dataProduct);
        
        return dataProductVersion;
    }

    @Test
    void whenCommandIsNullThenThrowBadRequestException() {
        // Given
        DataProductVersionApproveCommand command = null;
        DataProductVersionApprover approver = new DataProductVersionApprover(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(approver::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("DataProductVersionApproveCommand cannot be null");

        verifyNoInteractions(persistencePort, notificationsPort, presenter, transactionalPort);
    }

    @Test
    void whenDataProductVersionIsNullThenThrowBadRequestException() {
        // Given
        DataProductVersionApproveCommand command = new DataProductVersionApproveCommand(null);
        DataProductVersionApprover approver = new DataProductVersionApprover(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(approver::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("DataProductVersion cannot be null");

        verifyNoInteractions(persistencePort, notificationsPort, presenter, transactionalPort);
    }

    @Test
    void whenDataProductVersionUuidIsNullThenThrowBadRequestException() {
        // Given
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setDataProductUuid("data-product-uuid-123");
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setSpec("opendatamesh");
        dataProductVersion.setSpecVersion("1.0.0");
        
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        dataProductVersion.setUuid(null);
        DataProductVersionApproveCommand command = new DataProductVersionApproveCommand(dataProductVersion);
        DataProductVersionApprover approver = new DataProductVersionApprover(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(approver::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("UUID is required for data product version approval");

        verifyNoInteractions(persistencePort, notificationsPort, presenter, transactionalPort);
    }

    @Test
    void whenDataProductVersionUuidIsEmptyThenThrowBadRequestException() {
        // Given
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setDataProductUuid("data-product-uuid-123");
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setSpec("opendatamesh");
        dataProductVersion.setSpecVersion("1.0.0");
        
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        dataProductVersion.setUuid("");
        DataProductVersionApproveCommand command = new DataProductVersionApproveCommand(dataProductVersion);
        DataProductVersionApprover approver = new DataProductVersionApprover(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(approver::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("UUID is required for data product version approval");

        verifyNoInteractions(persistencePort, notificationsPort, presenter, transactionalPort);
    }

    @Test
    void whenDataProductVersionUuidIsBlankThenThrowBadRequestException() {
        // Given
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setDataProductUuid("data-product-uuid-123");
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setSpec("opendatamesh");
        dataProductVersion.setSpecVersion("1.0.0");
        
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        dataProductVersion.setUuid("   ");
        DataProductVersionApproveCommand command = new DataProductVersionApproveCommand(dataProductVersion);
        DataProductVersionApprover approver = new DataProductVersionApprover(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(approver::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("UUID is required for data product version approval");

        verifyNoInteractions(persistencePort, notificationsPort, presenter, transactionalPort);
    }

    @Test
    void whenDataProductVersionIsPendingAndDataProductIsApprovedThenApproveSuccessfully() {
        // Given
        DataProductVersion dataProductVersion = createDataProductVersionWithDataProduct(
                "test-uuid-123", "Test Version", "v1.0.0", 
                DataProductVersionValidationState.PENDING, 
                DataProductValidationState.APPROVED);
        DataProductVersionApproveCommand command = new DataProductVersionApproveCommand(dataProductVersion);

        when(persistencePort.findByUuid(dataProductVersion.getUuid())).thenReturn(dataProductVersion);
        when(persistencePort.save(any(DataProductVersion.class))).thenReturn(dataProductVersion);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductVersionApprover approver = new DataProductVersionApprover(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        // When
        approver.execute();

        // Then
        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(persistencePort).findByUuid(dataProductVersion.getUuid());
        verify(persistencePort).save(any(DataProductVersion.class));
        verify(notificationsPort).emitDataProductVersionPublished(dataProductVersion);
        verify(presenter).presentDataProductVersionApproved(dataProductVersion);

        // Verify that validation state is set to APPROVED
        verify(persistencePort).save(argThat(savedDataProductVersion ->
                DataProductVersionValidationState.APPROVED.equals(savedDataProductVersion.getValidationState())));
    }

    @Test
    void whenDataProductVersionIsRejectedThenThrowBadRequestException() {
        // Given
        DataProductVersion dataProductVersion = createDataProductVersionWithDataProduct(
                "test-uuid-123", "Test Version", "v1.0.0", 
                DataProductVersionValidationState.REJECTED, 
                DataProductValidationState.APPROVED);
        DataProductVersionApproveCommand command = new DataProductVersionApproveCommand(dataProductVersion);

        when(persistencePort.findByUuid(dataProductVersion.getUuid())).thenReturn(dataProductVersion);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductVersionApprover approver = new DataProductVersionApprover(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(approver::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Data Product Version Test Version v1.0.0 can be approved only if in PENDING state.");

        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(persistencePort).findByUuid(dataProductVersion.getUuid());
        verifyNoInteractions(notificationsPort, presenter);
    }

    @Test
    void whenDataProductVersionIsAlreadyApprovedThenThrowBadRequestException() {
        // Given
        DataProductVersion dataProductVersion = createDataProductVersionWithDataProduct(
                "test-uuid-123", "Test Version", "v1.0.0", 
                DataProductVersionValidationState.APPROVED, 
                DataProductValidationState.APPROVED);
        DataProductVersionApproveCommand command = new DataProductVersionApproveCommand(dataProductVersion);

        when(persistencePort.findByUuid(dataProductVersion.getUuid())).thenReturn(dataProductVersion);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductVersionApprover approver = new DataProductVersionApprover(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(approver::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Data Product Version Test Version v1.0.0 can be approved only if in PENDING state.");

        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(persistencePort).findByUuid(dataProductVersion.getUuid());
        verifyNoInteractions(notificationsPort, presenter);
    }

    @Test
    void whenExecuteThenAllOperationsHappenInTransaction() {
        // Given
        DataProductVersion dataProductVersion = createDataProductVersionWithDataProduct(
                "test-uuid-123", "Test Version", "v1.0.0", 
                DataProductVersionValidationState.PENDING, 
                DataProductValidationState.APPROVED);
        DataProductVersionApproveCommand command = new DataProductVersionApproveCommand(dataProductVersion);

        when(persistencePort.findByUuid(dataProductVersion.getUuid())).thenReturn(dataProductVersion);
        when(persistencePort.save(any(DataProductVersion.class))).thenReturn(dataProductVersion);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductVersionApprover approver = new DataProductVersionApprover(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        // When
        approver.execute();

        // Then
        verify(transactionalPort).doInTransaction(any(Runnable.class));

        // Verify that all persistence operations happen within the transaction
        verify(persistencePort).findByUuid(dataProductVersion.getUuid());
        verify(persistencePort).save(any(DataProductVersion.class));
        verify(notificationsPort).emitDataProductVersionPublished(dataProductVersion);
        verify(presenter).presentDataProductVersionApproved(dataProductVersion);
    }

    @Test
    void whenApproveSuccessfullyThenPresenterReceivesCorrectDataProductVersion() {
        // Given
        DataProductVersion dataProductVersion = createDataProductVersionWithDataProduct(
                "test-uuid-123", "Test Version", "v1.0.0", 
                DataProductVersionValidationState.PENDING, 
                DataProductValidationState.APPROVED);
        DataProductVersionApproveCommand command = new DataProductVersionApproveCommand(dataProductVersion);

        when(persistencePort.findByUuid(dataProductVersion.getUuid())).thenReturn(dataProductVersion);
        when(persistencePort.save(any(DataProductVersion.class))).thenReturn(dataProductVersion);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductVersionApprover approver = new DataProductVersionApprover(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        // When
        approver.execute();

        // Then
        verify(presenter).presentDataProductVersionApproved(argThat(presentedDataProductVersion -> {
            assertThat(presentedDataProductVersion)
                    .usingRecursiveComparison()
                    .isEqualTo(dataProductVersion);
            return true;
        }));
    }

    @Test
    void whenDataProductIsNotApprovedThenThrowBadRequestException() {
        // Given
        DataProductVersion dataProductVersion = createDataProductVersionWithDataProduct(
                "test-uuid-123", "Test Version", "v1.0.0", 
                DataProductVersionValidationState.PENDING, 
                DataProductValidationState.PENDING);
        DataProductVersionApproveCommand command = new DataProductVersionApproveCommand(dataProductVersion);

        when(persistencePort.findByUuid(dataProductVersion.getUuid())).thenReturn(dataProductVersion);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductVersionApprover approver = new DataProductVersionApprover(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(approver::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Data Product Version Test Version v1.0.0 must be associated to an APPROVED Data Product in order to be approved.");

        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(persistencePort).findByUuid(dataProductVersion.getUuid());
        verifyNoInteractions(notificationsPort, presenter);
    }

}
