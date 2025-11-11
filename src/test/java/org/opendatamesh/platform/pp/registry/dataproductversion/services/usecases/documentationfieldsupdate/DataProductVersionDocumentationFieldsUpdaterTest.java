package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.documentationfieldsupdate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.exceptions.NotFoundException;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.documentationfieldsupdate.DataProductVersionDocumentationFieldsRes;
import org.opendatamesh.platform.pp.registry.utils.usecases.TransactionalOutboundPort;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)

class DataProductVersionDocumentationFieldsUpdaterTest {

    @Mock
    private DataProductVersionDocumentationFieldsUpdatePresenter presenter;

    @Mock
    private DataProductVersionDocumentationFieldsUpdaterPersistenceOutboundPort persistencePort;

    @Mock
    private TransactionalOutboundPort transactionalPort;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void whenCommandIsNullThenThrowBadRequestException() {
        DataProductVersionDocumentationFieldsUpdateCommand command = null;
        DataProductVersionDocumentationFieldsUpdater updater = new DataProductVersionDocumentationFieldsUpdater(command, presenter, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(updater::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("DataProductVersionDocumentationFieldsUpdateCommand cannot be null");

        verifyNoInteractions(persistencePort, presenter, transactionalPort);
    }

    @Test
    void whenDataProductVersionUuidIsNullThenThrowBadRequestException() {
        // Given
        DataProductVersionDocumentationFieldsRes dataProductVersion = new DataProductVersionDocumentationFieldsRes();
        dataProductVersion.setUuid(null);
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");

        dataProductVersion.setUuid(null);
        DataProductVersionDocumentationFieldsUpdateCommand command = new DataProductVersionDocumentationFieldsUpdateCommand(
                dataProductVersion.getUuid(),
                dataProductVersion.getName(),
                dataProductVersion.getDescription(),
                dataProductVersion.getUpdatedBy());
        DataProductVersionDocumentationFieldsUpdater updater = new DataProductVersionDocumentationFieldsUpdater(command, presenter, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(updater::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("UUID is required for data product version documentation fields update");

        verifyNoInteractions(persistencePort, presenter, transactionalPort);
    }

    @Test
    void whenDataProductVersionUuidIsEmptyThenThrowBadRequestException() {
        // Given
        DataProductVersionDocumentationFieldsRes dataProductVersion = new DataProductVersionDocumentationFieldsRes();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");

        dataProductVersion.setUuid("");
        DataProductVersionDocumentationFieldsUpdateCommand command = new DataProductVersionDocumentationFieldsUpdateCommand(
                dataProductVersion.getUuid(),
                dataProductVersion.getName(),
                dataProductVersion.getDescription(),
                dataProductVersion.getUpdatedBy());
        DataProductVersionDocumentationFieldsUpdater updater = new DataProductVersionDocumentationFieldsUpdater(command, presenter, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(updater::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("UUID is required for data product version documentation fields update");

        verifyNoInteractions(persistencePort, presenter, transactionalPort);
    }

    @Test
    void whenDataProductVersionUuidIsBlankThenThrowBadRequestException() {
        // Given
        DataProductVersionDocumentationFieldsRes dataProductVersion = new DataProductVersionDocumentationFieldsRes();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setUuid("   ");
        DataProductVersionDocumentationFieldsUpdateCommand command = new DataProductVersionDocumentationFieldsUpdateCommand(
                dataProductVersion.getUuid(),
                dataProductVersion.getName(),
                dataProductVersion.getDescription(),
                dataProductVersion.getUpdatedBy());
        DataProductVersionDocumentationFieldsUpdater updater = new DataProductVersionDocumentationFieldsUpdater(command, presenter, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(updater::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("UUID is required for data product version documentation fields update");

        verifyNoInteractions(persistencePort, presenter, transactionalPort);
    }

    @Test
    void whenNameIsNullThenThrowBadRequestException() {
        DataProductVersionDocumentationFieldsRes dataProductVersion = new DataProductVersionDocumentationFieldsRes();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setName(null);
        dataProductVersion.setDescription("Test Version Description");

        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");

        dataProductVersion.setUpdatedBy("updateUser");
        DataProductVersionDocumentationFieldsUpdateCommand command = new DataProductVersionDocumentationFieldsUpdateCommand(
                dataProductVersion.getUuid(),
                dataProductVersion.getName(),
                dataProductVersion.getDescription(),
                dataProductVersion.getUpdatedBy());
        DataProductVersionDocumentationFieldsUpdater updater = new DataProductVersionDocumentationFieldsUpdater(command, presenter, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(updater::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Version name is required for data product version documentation fields update");

        verifyNoInteractions(persistencePort, presenter, transactionalPort);
    }

    @Test
    void whenNameIsEmptyThenThrowBadRequestException() {
        DataProductVersionDocumentationFieldsRes dataProductVersion = new DataProductVersionDocumentationFieldsRes();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setName("");
        dataProductVersion.setDescription("Test Version Description");

        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");

        dataProductVersion.setUpdatedBy("updateUser");
        DataProductVersionDocumentationFieldsUpdateCommand command = new DataProductVersionDocumentationFieldsUpdateCommand(
                dataProductVersion.getUuid(),
                dataProductVersion.getName(),
                dataProductVersion.getDescription(),
                dataProductVersion.getUpdatedBy());
        DataProductVersionDocumentationFieldsUpdater updater = new DataProductVersionDocumentationFieldsUpdater(command, presenter, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(updater::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Version name is required for data product version documentation fields update");

        verifyNoInteractions(persistencePort, presenter, transactionalPort);
    }

    @Test
    void whenNameIsBlankThenThrowBadRequestException() {
        DataProductVersionDocumentationFieldsRes dataProductVersion = new DataProductVersionDocumentationFieldsRes();
        dataProductVersion.setUuid("test-uuid-123");

        dataProductVersion.setName("   ");
        dataProductVersion.setDescription("Test Version Description");

        dataProductVersion.setUpdatedBy("updateUser");
        DataProductVersionDocumentationFieldsUpdateCommand command = new DataProductVersionDocumentationFieldsUpdateCommand(
                dataProductVersion.getUuid(),
                dataProductVersion.getName(),
                dataProductVersion.getDescription(),
                dataProductVersion.getUpdatedBy());
        DataProductVersionDocumentationFieldsUpdater updater = new DataProductVersionDocumentationFieldsUpdater(command, presenter, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(updater::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Version name is required for data product version documentation fields update");

        verifyNoInteractions(persistencePort, presenter, transactionalPort);
    }

    @Test
    void whenNoExistingDataProductVersionThenThrowNotFoundException() {
        // Given - trying to update a version with a wrong UUID that doesn't exist
        String wrongUuid = "test-uuid-error";
        DataProductVersionDocumentationFieldsRes dataProductVersion = new DataProductVersionDocumentationFieldsRes();
        dataProductVersion.setUuid(wrongUuid);
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setUpdatedBy("updateUser");

        // Mock: when trying to find the version by UUID, it doesn't exist
        when(persistencePort.findByUuid(wrongUuid))
                .thenThrow(new NotFoundException("Resource with id=" + wrongUuid + " not found"));

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductVersionDocumentationFieldsUpdateCommand command = new DataProductVersionDocumentationFieldsUpdateCommand(
                dataProductVersion.getUuid(),
                dataProductVersion.getName(),
                dataProductVersion.getDescription(),
                dataProductVersion.getUpdatedBy());

        DataProductVersionDocumentationFieldsUpdater updater = new DataProductVersionDocumentationFieldsUpdater(command, presenter, persistencePort, transactionalPort);

        // When - trying to update a version with wrong UUID
        // Then - should throw NotFoundException
        assertThatThrownBy(updater::execute)
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Resource with id=" + wrongUuid + " not found");

        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(persistencePort).findByUuid(wrongUuid);
        verifyNoInteractions(presenter);
    }

    @Test
    void whenUpdateSuccessfullyThenPresenterReceivesCorrectDataProductVersion() {
        // Given
        DataProductVersion existingDataProductVersion = new DataProductVersion();
        existingDataProductVersion.setUuid("test-uuid-123");
        existingDataProductVersion.setDataProductUuid("data-product-uuid-123");
        existingDataProductVersion.setName("Original Name");
        existingDataProductVersion.setDescription("Original Description");
        existingDataProductVersion.setTag("v1.0.0");
        existingDataProductVersion.setSpec("opendatamesh");
        existingDataProductVersion.setSpecVersion("1.0.0");
        existingDataProductVersion.setCreatedBy("creationUser");
        existingDataProductVersion.setContent(objectMapper.createObjectNode()
                .put("name", "Original Name")
                .put("version", "1.0.0"));

        DataProductVersion updatedDataProductVersion = new DataProductVersion();
        updatedDataProductVersion.setUuid("test-uuid-123");
        updatedDataProductVersion.setDataProductUuid("data-product-uuid-123");
        updatedDataProductVersion.setName("Updated Name");
        updatedDataProductVersion.setDescription("Updated Description");
        updatedDataProductVersion.setTag("v1.0.0");
        updatedDataProductVersion.setSpec("opendatamesh");
        updatedDataProductVersion.setSpecVersion("1.0.0");
        updatedDataProductVersion.setCreatedBy("creationUser");
        updatedDataProductVersion.setUpdatedBy("updateUser");
        updatedDataProductVersion.setContent(objectMapper.createObjectNode()
                .put("name", "Updated Name")
                .put("version", "1.0.0"));

        DataProductVersionDocumentationFieldsRes commandDataProductVersion = new DataProductVersionDocumentationFieldsRes();
        commandDataProductVersion.setUuid("test-uuid-123");
        commandDataProductVersion.setName("Updated Name");
        commandDataProductVersion.setDescription("Updated Description");
        commandDataProductVersion.setUpdatedBy("updateUser");

        DataProductVersionDocumentationFieldsUpdateCommand command = new DataProductVersionDocumentationFieldsUpdateCommand(
                commandDataProductVersion.getUuid(),
                commandDataProductVersion.getName(),
                commandDataProductVersion.getDescription(),
                commandDataProductVersion.getUpdatedBy());

        when(persistencePort.findByUuid(existingDataProductVersion.getUuid()))
                .thenReturn(existingDataProductVersion);
        when(persistencePort.save(any(DataProductVersion.class))).thenReturn(updatedDataProductVersion);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductVersionDocumentationFieldsUpdater updater = new DataProductVersionDocumentationFieldsUpdater(command, presenter, persistencePort, transactionalPort);

        // When
        updater.execute();

        // Then
        verify(presenter).presentDataProductVersionDocumentationFieldsUpdated(argThat(presentedDataProductVersion -> {
            assertThat(presentedDataProductVersion)
                    .usingRecursiveComparison()
                    .isEqualTo(updatedDataProductVersion);
            // Explicitly verify updatedBy field is set correctly
            assertThat(presentedDataProductVersion.getUpdatedBy()).isEqualTo("updateUser");
            return true;
        }));
    }

    @Test
    void whenExecuteThenAllOperationsHappenInTransaction() {
        // Given
        DataProductVersion existingDataProductVersion = new DataProductVersion();
        existingDataProductVersion.setUuid("test-uuid-123");
        existingDataProductVersion.setDataProductUuid("data-product-uuid-123");
        existingDataProductVersion.setName("Original Name");
        existingDataProductVersion.setDescription("Original Description");
        existingDataProductVersion.setTag("v1.0.0");
        existingDataProductVersion.setSpec("opendatamesh");
        existingDataProductVersion.setSpecVersion("1.0.0");
        existingDataProductVersion.setCreatedBy("creationUser");
        existingDataProductVersion.setContent(objectMapper.createObjectNode()
                .put("name", "Original Name")
                .put("version", "1.0.0"));

        DataProductVersion updatedDataProductVersion = new DataProductVersion();
        updatedDataProductVersion.setUuid("test-uuid-123");
        updatedDataProductVersion.setDataProductUuid("data-product-uuid-123");
        updatedDataProductVersion.setName("Updated Name");
        updatedDataProductVersion.setDescription("Updated Description");
        updatedDataProductVersion.setTag("v1.0.0");
        updatedDataProductVersion.setSpec("opendatamesh");
        updatedDataProductVersion.setSpecVersion("1.0.0");
        updatedDataProductVersion.setCreatedBy("creationUser");
        updatedDataProductVersion.setUpdatedBy("updateUser");
        updatedDataProductVersion.setContent(objectMapper.createObjectNode()
                .put("name", "Updated Name")
                .put("version", "1.0.0"));

        DataProductVersionDocumentationFieldsRes commandDataProductVersion = new DataProductVersionDocumentationFieldsRes();
        commandDataProductVersion.setUuid("test-uuid-123");
        commandDataProductVersion.setName("Updated Name");
        commandDataProductVersion.setDescription("Updated Description");

        commandDataProductVersion.setUpdatedBy("updateUser");

        DataProductVersionDocumentationFieldsUpdateCommand command = new DataProductVersionDocumentationFieldsUpdateCommand(
                commandDataProductVersion.getUuid(),
                commandDataProductVersion.getName(),
                commandDataProductVersion.getDescription(),
                commandDataProductVersion.getUpdatedBy());

        when(persistencePort.findByUuid(existingDataProductVersion.getUuid()))
                .thenReturn(existingDataProductVersion);
        when(persistencePort.save(any(DataProductVersion.class))).thenReturn(updatedDataProductVersion);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductVersionDocumentationFieldsUpdater updater = new DataProductVersionDocumentationFieldsUpdater(command, presenter, persistencePort, transactionalPort);

        // When
        updater.execute();

        // Then
        verify(transactionalPort).doInTransaction(any(Runnable.class));

        // Verify that all persistence operations happen within the transaction
        verify(persistencePort).findByUuid(existingDataProductVersion.getUuid());
        verify(persistencePort).save(any(DataProductVersion.class));
        verify(presenter).presentDataProductVersionDocumentationFieldsUpdated(updatedDataProductVersion);
    }

}
