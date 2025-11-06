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
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductValidationState;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersionValidationState;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
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
        dataProductVersion.setCreatedBy("creationUser");

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
        DataProductVersionDocumentationFieldsUpdateCommand command = null;
        DataProductVersionDocumentationFieldsUpdater updater = new DataProductVersionDocumentationFieldsUpdater(command, presenter, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(updater::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("DataProductVersionDocumentationFieldsUpdateCommand cannot be null");

        verifyNoInteractions(persistencePort, presenter, transactionalPort);
    }

    @Test
    void whenDataProductVersionIsNullThenThrowBadRequestException() {
        // Given
        DataProductVersionDocumentationFieldsUpdateCommand command = new DataProductVersionDocumentationFieldsUpdateCommand(null);
        DataProductVersionDocumentationFieldsUpdater updater = new DataProductVersionDocumentationFieldsUpdater(command, presenter, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(updater::execute)
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
        dataProductVersion.setSpec("opendatamesh");
        dataProductVersion.setSpecVersion("1.0.0");
        dataProductVersion.setCreatedBy("creationUser");

        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        dataProductVersion.setUuid(null);
        DataProductVersionDocumentationFieldsUpdateCommand command = new DataProductVersionDocumentationFieldsUpdateCommand(dataProductVersion);
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
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setDataProductUuid("data-product-uuid-123");
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setSpec("opendatamesh");
        dataProductVersion.setSpecVersion("1.0.0");
        dataProductVersion.setCreatedBy("creationUser");

        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        dataProductVersion.setUuid("");
        DataProductVersionDocumentationFieldsUpdateCommand command = new DataProductVersionDocumentationFieldsUpdateCommand(dataProductVersion);
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
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setDataProductUuid("data-product-uuid-123");
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setSpec("opendatamesh");
        dataProductVersion.setSpecVersion("1.0.0");
        dataProductVersion.setCreatedBy("creationUser");

        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        dataProductVersion.setUuid("   ");
        DataProductVersionDocumentationFieldsUpdateCommand command = new DataProductVersionDocumentationFieldsUpdateCommand(dataProductVersion);
        DataProductVersionDocumentationFieldsUpdater updater = new DataProductVersionDocumentationFieldsUpdater(command, presenter, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(updater::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("UUID is required for data product version documentation fields update");

        verifyNoInteractions(persistencePort, presenter, transactionalPort);
    }

    @Test
    void whenUpdatedByIsNullThenThrowBadRequestException() {
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setDataProductUuid("data-product-uuid-123");
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setSpec("opendatamesh");
        dataProductVersion.setSpecVersion("1.0.0");
        dataProductVersion.setCreatedBy("creationUser");

        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        dataProductVersion.setUpdatedBy(null);
        DataProductVersionDocumentationFieldsUpdateCommand command = new DataProductVersionDocumentationFieldsUpdateCommand(dataProductVersion);
        DataProductVersionDocumentationFieldsUpdater updater = new DataProductVersionDocumentationFieldsUpdater(command, presenter, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(updater::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("User is required for data product version documentation fields update");

        verifyNoInteractions(persistencePort, presenter, transactionalPort);
    }

    @Test
    void whenUpdatedByIsEmptyThenThrowBadRequestException() {
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setDataProductUuid("data-product-uuid-123");
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setSpec("opendatamesh");
        dataProductVersion.setSpecVersion("1.0.0");
        dataProductVersion.setCreatedBy("creationUser");

        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        dataProductVersion.setUpdatedBy("");
        DataProductVersionDocumentationFieldsUpdateCommand command = new DataProductVersionDocumentationFieldsUpdateCommand(dataProductVersion);
        DataProductVersionDocumentationFieldsUpdater updater = new DataProductVersionDocumentationFieldsUpdater(command, presenter, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(updater::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("User is required for data product version documentation fields update");

        verifyNoInteractions(persistencePort, presenter, transactionalPort);
    }

    @Test
    void whenUpdatedByIsBlankThenThrowBadRequestException() {
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setDataProductUuid("data-product-uuid-123");
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setSpec("opendatamesh");
        dataProductVersion.setSpecVersion("1.0.0");
        dataProductVersion.setCreatedBy("creationUser");

        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        dataProductVersion.setUpdatedBy("    ");
        DataProductVersionDocumentationFieldsUpdateCommand command = new DataProductVersionDocumentationFieldsUpdateCommand(dataProductVersion);
        DataProductVersionDocumentationFieldsUpdater updater = new DataProductVersionDocumentationFieldsUpdater(command, presenter, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(updater::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("User is required for data product version documentation fields update");

        verifyNoInteractions(persistencePort, presenter, transactionalPort);
    }

    @Test
    void whenNameIsNullThenThrowBadRequestException() {
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setDataProductUuid("data-product-uuid-123");
        dataProductVersion.setName(null);
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setSpec("opendatamesh");
        dataProductVersion.setSpecVersion("1.0.0");
        dataProductVersion.setCreatedBy("creationUser");

        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        dataProductVersion.setUpdatedBy("updateUser");
        DataProductVersionDocumentationFieldsUpdateCommand command = new DataProductVersionDocumentationFieldsUpdateCommand(dataProductVersion);
        DataProductVersionDocumentationFieldsUpdater updater = new DataProductVersionDocumentationFieldsUpdater(command, presenter, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(updater::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Version name is required for data product version documentation fields update");

        verifyNoInteractions(persistencePort, presenter, transactionalPort);
    }

    @Test
    void whenNameIsEmptyThenThrowBadRequestException() {
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setDataProductUuid("data-product-uuid-123");
        dataProductVersion.setName("");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setSpec("opendatamesh");
        dataProductVersion.setSpecVersion("1.0.0");
        dataProductVersion.setCreatedBy("creationUser");

        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        dataProductVersion.setUpdatedBy("updateUser");
        DataProductVersionDocumentationFieldsUpdateCommand command = new DataProductVersionDocumentationFieldsUpdateCommand(dataProductVersion);
        DataProductVersionDocumentationFieldsUpdater updater = new DataProductVersionDocumentationFieldsUpdater(command, presenter, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(updater::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Version name is required for data product version documentation fields update");

        verifyNoInteractions(persistencePort, presenter, transactionalPort);
    }

    @Test
    void whenNameIsBlankThenThrowBadRequestException() {
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setDataProductUuid("data-product-uuid-123");
        dataProductVersion.setName("   ");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setSpec("opendatamesh");
        dataProductVersion.setSpecVersion("1.0.0");
        dataProductVersion.setCreatedBy("creationUser");

        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        dataProductVersion.setUpdatedBy("updateUser");
        DataProductVersionDocumentationFieldsUpdateCommand command = new DataProductVersionDocumentationFieldsUpdateCommand(dataProductVersion);
        DataProductVersionDocumentationFieldsUpdater updater = new DataProductVersionDocumentationFieldsUpdater(command, presenter, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(updater::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Version name is required for data product version documentation fields update");

        verifyNoInteractions(persistencePort, presenter, transactionalPort);
    }

    @Test
    void whenTagIsNullThenThrowBadRequestException() {
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setDataProductUuid("data-product-uuid-123");
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag(null);
        dataProductVersion.setSpec("opendatamesh");
        dataProductVersion.setSpecVersion("1.0.0");
        dataProductVersion.setCreatedBy("creationUser");

        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        dataProductVersion.setUpdatedBy("updateUser");
        DataProductVersionDocumentationFieldsUpdateCommand command = new DataProductVersionDocumentationFieldsUpdateCommand(dataProductVersion);
        DataProductVersionDocumentationFieldsUpdater updater = new DataProductVersionDocumentationFieldsUpdater(command, presenter, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(updater::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Missing Data Product Version tag");

        verifyNoInteractions(persistencePort, presenter, transactionalPort);
    }

    @Test
    void whenTagIsEmptyThenThrowBadRequestException() {
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setDataProductUuid("data-product-uuid-123");
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("");
        dataProductVersion.setSpec("opendatamesh");
        dataProductVersion.setSpecVersion("1.0.0");
        dataProductVersion.setCreatedBy("creationUser");

        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        dataProductVersion.setUpdatedBy("updateUser");
        DataProductVersionDocumentationFieldsUpdateCommand command = new DataProductVersionDocumentationFieldsUpdateCommand(dataProductVersion);
        DataProductVersionDocumentationFieldsUpdater updater = new DataProductVersionDocumentationFieldsUpdater(command, presenter, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(updater::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Missing Data Product Version tag");

        verifyNoInteractions(persistencePort, presenter, transactionalPort);
    }

    @Test
    void whenTagIsBlankThenThrowBadRequestException() {
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setDataProductUuid("data-product-uuid-123");
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("   ");
        dataProductVersion.setSpec("opendatamesh");
        dataProductVersion.setSpecVersion("1.0.0");
        dataProductVersion.setCreatedBy("creationUser");

        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        dataProductVersion.setUpdatedBy("updateUser");
        DataProductVersionDocumentationFieldsUpdateCommand command = new DataProductVersionDocumentationFieldsUpdateCommand(dataProductVersion);
        DataProductVersionDocumentationFieldsUpdater updater = new DataProductVersionDocumentationFieldsUpdater(command, presenter, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(updater::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Missing Data Product Version tag");

        verifyNoInteractions(persistencePort, presenter, transactionalPort);
    }

    @Test
    void whenContentIsNullThenThrowBadRequestException() {
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setDataProductUuid("data-product-uuid-123");
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setSpec("opendatamesh");
        dataProductVersion.setSpecVersion("1.0.0");
        dataProductVersion.setCreatedBy("creationUser");

        dataProductVersion.setContent(null);
        dataProductVersion.setUpdatedBy("updateUser");
        DataProductVersionDocumentationFieldsUpdateCommand command = new DataProductVersionDocumentationFieldsUpdateCommand(dataProductVersion);
        DataProductVersionDocumentationFieldsUpdater updater = new DataProductVersionDocumentationFieldsUpdater(command, presenter, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(updater::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Missing Data Product Version content");

        verifyNoInteractions(persistencePort, presenter, transactionalPort);
    }

    @Test
    void whenNoExistingDataProductVersionThenThrowBadRequestException() {
        // Given
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setDataProductUuid("data-product-uuid-123");
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setSpec("opendatamesh");
        dataProductVersion.setSpecVersion("1.0.0");
        dataProductVersion.setCreatedBy("creationUser");
        dataProductVersion.setUpdatedBy("updateUser");
        dataProductVersion.setContent(objectMapper.createObjectNode()
                .put("name", "Test Version")
                .put("version", "1.0.0"));
        DataProductVersionDocumentationFieldsUpdateCommand command = new DataProductVersionDocumentationFieldsUpdateCommand(dataProductVersion);

        when(persistencePort.findByUuid(dataProductVersion.getUuid()))
                .thenReturn(null);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductVersionDocumentationFieldsUpdater updater = new DataProductVersionDocumentationFieldsUpdater(command, presenter, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(updater::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Data Product Version with UUID test-uuid-123 does not exist");

        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(persistencePort).findByUuid(dataProductVersion.getUuid());
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

        DataProductVersion commandDataProductVersion = new DataProductVersion();
        commandDataProductVersion.setUuid("test-uuid-123");
        commandDataProductVersion.setName("Updated Name");
        commandDataProductVersion.setDescription("Updated Description");
        commandDataProductVersion.setTag("v1.0.0");
        commandDataProductVersion.setUpdatedBy("updateUser");
        commandDataProductVersion.setContent(objectMapper.createObjectNode()
                .put("name", "Updated Name")
                .put("version", "1.0.0"));
        DataProductVersionDocumentationFieldsUpdateCommand command = new DataProductVersionDocumentationFieldsUpdateCommand(commandDataProductVersion);

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

        DataProductVersion commandDataProductVersion = new DataProductVersion();
        commandDataProductVersion.setUuid("test-uuid-123");
        commandDataProductVersion.setName("Updated Name");
        commandDataProductVersion.setDescription("Updated Description");
        commandDataProductVersion.setTag("v1.0.0");
        commandDataProductVersion.setUpdatedBy("updateUser");
        commandDataProductVersion.setContent(objectMapper.createObjectNode()
                .put("name", "Updated Name")
                .put("version", "1.0.0"));
        DataProductVersionDocumentationFieldsUpdateCommand command = new DataProductVersionDocumentationFieldsUpdateCommand(commandDataProductVersion);

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
