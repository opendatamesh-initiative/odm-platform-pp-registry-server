package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.reject;

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
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersionValidationState;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.exceptions.NotFoundException;
import org.opendatamesh.platform.pp.registry.utils.usecases.TransactionalOutboundPort;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class DataProductVersionRejectorTest {

    @Mock
    private DataProductVersionRejectPresenter presenter;

    @Mock
    private DataProductVersionRejectorPersistenceOutboundPort persistencePort;

    @Mock
    private TransactionalOutboundPort transactionalPort;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void whenCommandIsNullThenThrowBadRequestException() {
        // Given
        DataProductVersionRejectCommand command = null;
        DataProductVersionRejector rejector = new DataProductVersionRejector(
                command, presenter, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(() -> rejector.execute())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("DataProductVersionRejectCommand cannot be null");

        verifyNoInteractions(persistencePort, presenter, transactionalPort);
    }

    @Test
    void whenDataProductVersionIsNullThenThrowBadRequestException() {
        // Given
        DataProductVersionRejectCommand command = new DataProductVersionRejectCommand(null);
        DataProductVersionRejector rejector = new DataProductVersionRejector(
                command, presenter, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(() -> rejector.execute())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("DataProductVersion cannot be null");

        verifyNoInteractions(persistencePort, presenter, transactionalPort);
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
        dataProductVersion.setSpec("dpds");
        dataProductVersion.setSpecVersion("1.0.0");
        
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        dataProductVersion.setUuid(null);
        DataProductVersionRejectCommand command = new DataProductVersionRejectCommand(dataProductVersion);
        DataProductVersionRejector rejector = new DataProductVersionRejector(
                command, presenter, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(() -> rejector.execute())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("UUID is required for data product version rejection");

        verifyNoInteractions(persistencePort, presenter, transactionalPort);
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
        dataProductVersion.setSpec("dpds");
        dataProductVersion.setSpecVersion("1.0.0");
        
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        dataProductVersion.setUuid("");
        DataProductVersionRejectCommand command = new DataProductVersionRejectCommand(dataProductVersion);
        DataProductVersionRejector rejector = new DataProductVersionRejector(
                command, presenter, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(() -> rejector.execute())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("UUID is required for data product version rejection");

        verifyNoInteractions(persistencePort, presenter, transactionalPort);
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
        dataProductVersion.setSpec("dpds");
        dataProductVersion.setSpecVersion("1.0.0");
        
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        dataProductVersion.setUuid("   ");
        DataProductVersionRejectCommand command = new DataProductVersionRejectCommand(dataProductVersion);
        DataProductVersionRejector rejector = new DataProductVersionRejector(
                command, presenter, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(() -> rejector.execute())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("UUID is required for data product version rejection");

        verifyNoInteractions(persistencePort, presenter, transactionalPort);
    }

    @Test
    void whenDataProductVersionIsPendingThenRejectSuccessfully() {
        // Given
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setDataProductUuid("data-product-uuid-123");
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setSpec("dpds");
        dataProductVersion.setSpecVersion("1.0.0");
        
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        dataProductVersion.setValidationState(DataProductVersionValidationState.PENDING);
        DataProductVersionRejectCommand command = new DataProductVersionRejectCommand(dataProductVersion);

        when(persistencePort.findByUuid(dataProductVersion.getUuid())).thenReturn(dataProductVersion);
        when(persistencePort.save(any(DataProductVersion.class))).thenReturn(dataProductVersion);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductVersionRejector rejector = new DataProductVersionRejector(
                command, presenter, persistencePort, transactionalPort);

        // When
        rejector.execute();

        // Then
        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(persistencePort).findByUuid(dataProductVersion.getUuid());
        verify(persistencePort).save(any(DataProductVersion.class));
        verify(presenter).presentDataProductVersionRejected(dataProductVersion);

        // Verify that validation state is set to REJECTED
        verify(persistencePort).save(argThat(savedDataProductVersion ->
                DataProductVersionValidationState.REJECTED.equals(savedDataProductVersion.getValidationState())));
    }

    @Test
    void whenDataProductVersionIsApprovedThenThrowBadRequestException() {
        // Given
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setDataProductUuid("data-product-uuid-123");
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setSpec("dpds");
        dataProductVersion.setSpecVersion("1.0.0");
        
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        dataProductVersion.setValidationState(DataProductVersionValidationState.APPROVED);
        DataProductVersionRejectCommand command = new DataProductVersionRejectCommand(dataProductVersion);

        when(persistencePort.findByUuid(dataProductVersion.getUuid())).thenReturn(dataProductVersion);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductVersionRejector rejector = new DataProductVersionRejector(
                command, presenter, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(() -> rejector.execute())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Data Product Version Test Version v1.0.0 can be rejected only if in PENDING state.");

        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(persistencePort).findByUuid(dataProductVersion.getUuid());
        verifyNoInteractions(presenter);
    }

    @Test
    void whenDataProductVersionIsAlreadyRejectedThenThrowBadRequestException() {
        // Given
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setDataProductUuid("data-product-uuid-123");
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setSpec("dpds");
        dataProductVersion.setSpecVersion("1.0.0");
        
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        dataProductVersion.setValidationState(DataProductVersionValidationState.REJECTED);
        DataProductVersionRejectCommand command = new DataProductVersionRejectCommand(dataProductVersion);

        when(persistencePort.findByUuid(dataProductVersion.getUuid())).thenReturn(dataProductVersion);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductVersionRejector rejector = new DataProductVersionRejector(
                command, presenter, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(() -> rejector.execute())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Data Product Version Test Version v1.0.0 can be rejected only if in PENDING state.");

        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(persistencePort).findByUuid(dataProductVersion.getUuid());
        verifyNoInteractions(presenter);
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
        dataProductVersion.setSpec("dpds");
        dataProductVersion.setSpecVersion("1.0.0");
        
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        dataProductVersion.setValidationState(DataProductVersionValidationState.PENDING);
        DataProductVersionRejectCommand command = new DataProductVersionRejectCommand(dataProductVersion);

        when(persistencePort.findByUuid(dataProductVersion.getUuid())).thenReturn(dataProductVersion);
        when(persistencePort.save(any(DataProductVersion.class))).thenReturn(dataProductVersion);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductVersionRejector rejector = new DataProductVersionRejector(
                command, presenter, persistencePort, transactionalPort);

        // When
        rejector.execute();

        // Then
        verify(transactionalPort).doInTransaction(any(Runnable.class));

        // Verify that all persistence operations happen within the transaction
        verify(persistencePort).findByUuid(dataProductVersion.getUuid());
        verify(persistencePort).save(any(DataProductVersion.class));
        verify(presenter).presentDataProductVersionRejected(dataProductVersion);
    }

    @Test
    void whenRejectSuccessfullyThenPresenterReceivesCorrectDataProductVersion() {
        // Given
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setDataProductUuid("data-product-uuid-123");
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setSpec("dpds");
        dataProductVersion.setSpecVersion("1.0.0");
        
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        dataProductVersion.setValidationState(DataProductVersionValidationState.PENDING);
        DataProductVersionRejectCommand command = new DataProductVersionRejectCommand(dataProductVersion);

        when(persistencePort.findByUuid(dataProductVersion.getUuid())).thenReturn(dataProductVersion);
        when(persistencePort.save(any(DataProductVersion.class))).thenReturn(dataProductVersion);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductVersionRejector rejector = new DataProductVersionRejector(
                command, presenter, persistencePort, transactionalPort);

        // When
        rejector.execute();

        // Then
        verify(presenter).presentDataProductVersionRejected(argThat(presentedDataProductVersion -> {
            assertThat(presentedDataProductVersion)
                    .usingRecursiveComparison()
                    .isEqualTo(dataProductVersion);
            return true;
        }));
    }

    @Test
    void whenDataProductVersionDoesNotExistThenThrowNotFoundException() {
        // Given
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setDataProductUuid("data-product-uuid-123");
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setSpec("dpds");
        dataProductVersion.setSpecVersion("1.0.0");
        
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        dataProductVersion.setValidationState(DataProductVersionValidationState.PENDING);
        DataProductVersionRejectCommand command = new DataProductVersionRejectCommand(dataProductVersion);

        when(persistencePort.findByUuid(dataProductVersion.getUuid())).thenThrow(new NotFoundException("Resource with id=" + dataProductVersion.getUuid() + " not found"));

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductVersionRejector rejector = new DataProductVersionRejector(
                command, presenter, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(() -> rejector.execute())
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Resource with id=" + dataProductVersion.getUuid() + " not found");

        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(persistencePort).findByUuid(dataProductVersion.getUuid());
        verifyNoInteractions(presenter);
    }

}
