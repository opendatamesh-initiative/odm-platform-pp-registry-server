package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.delete;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.dataproduct.services.core.DataProductsService;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.exceptions.NotFoundException;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductSearchOptions;
import org.opendatamesh.platform.pp.registry.utils.usecases.TransactionalOutboundPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataProductVersionDeleterTest {

    @Mock
    private DataProductVersionDeletePresenter presenter;

    @Mock
    private DataProductVersionDeleterNotificationOutboundPort notificationsPort;

    @Mock
    private DataProductVersionDeleterPersistenceOutboundPort persistencePort;

    @Mock
    private TransactionalOutboundPort transactionalPort;

    @Mock
    private DataProductsService dataProductsService;

    @Test
    void whenCommandIsNullThenThrowBadRequestException() {
        // Given
        DataProductVersionDeleteCommand command = null;
        DataProductVersionDeleter deleter = new DataProductVersionDeleter(
                command, presenter, notificationsPort, persistencePort, transactionalPort, dataProductsService);

        // When & Then
        assertThatThrownBy(deleter::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("DataProductVersionDeleteCommand cannot be null");

        verifyNoInteractions(persistencePort, notificationsPort, presenter, transactionalPort, dataProductsService);
    }

    @Test
    void whenAllFieldsAreNullThenThrowBadRequestException() {
        // Given
        DataProductVersionDeleteCommand command = new DataProductVersionDeleteCommand(null, null, null);
        DataProductVersionDeleter deleter = new DataProductVersionDeleter(
                command, presenter, notificationsPort, persistencePort, transactionalPort, dataProductsService);

        // When & Then
        assertThatThrownBy(deleter::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Either dataProductVersionUuid or both dataProductFqn and dataProductVersionTag must be provided");

        verifyNoInteractions(persistencePort, notificationsPort, presenter, transactionalPort, dataProductsService);
    }

    @Test
    void whenAllFieldsAreEmptyThenThrowBadRequestException() {
        // Given
        DataProductVersionDeleteCommand command = new DataProductVersionDeleteCommand("", "", "");
        DataProductVersionDeleter deleter = new DataProductVersionDeleter(
                command, presenter, notificationsPort, persistencePort, transactionalPort, dataProductsService);

        // When & Then
        assertThatThrownBy(deleter::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Either dataProductVersionUuid or both dataProductFqn and dataProductVersionTag must be provided");

        verifyNoInteractions(persistencePort, notificationsPort, presenter, transactionalPort, dataProductsService);
    }

    @Test
    void whenAllFieldsAreBlankThenThrowBadRequestException() {
        // Given
        DataProductVersionDeleteCommand command = new DataProductVersionDeleteCommand("   ", "   ", "   ");
        DataProductVersionDeleter deleter = new DataProductVersionDeleter(
                command, presenter, notificationsPort, persistencePort, transactionalPort, dataProductsService);

        // When & Then
        assertThatThrownBy(deleter::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Either dataProductVersionUuid or both dataProductFqn and dataProductVersionTag must be provided");

        verifyNoInteractions(persistencePort, notificationsPort, presenter, transactionalPort, dataProductsService);
    }

    @Test
    void whenOnlyFqnProvidedThenThrowBadRequestException() {
        // Given
        DataProductVersionDeleteCommand command = new DataProductVersionDeleteCommand(null, "test.domain:test-product", null);
        DataProductVersionDeleter deleter = new DataProductVersionDeleter(
                command, presenter, notificationsPort, persistencePort, transactionalPort, dataProductsService);

        // When & Then
        assertThatThrownBy(deleter::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Either dataProductVersionUuid or both dataProductFqn and dataProductVersionTag must be provided");

        verifyNoInteractions(persistencePort, notificationsPort, presenter, transactionalPort, dataProductsService);
    }

    @Test
    void whenOnlyTagProvidedThenThrowBadRequestException() {
        // Given
        DataProductVersionDeleteCommand command = new DataProductVersionDeleteCommand(null, null, "v1.0.0");
        DataProductVersionDeleter deleter = new DataProductVersionDeleter(
                command, presenter, notificationsPort, persistencePort, transactionalPort, dataProductsService);

        // When & Then
        assertThatThrownBy(deleter::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Either dataProductVersionUuid or both dataProductFqn and dataProductVersionTag must be provided");

        verifyNoInteractions(persistencePort, notificationsPort, presenter, transactionalPort, dataProductsService);
    }

    @Test
    void whenDataProductVersionNotFoundByUuidThenThrowNotFoundException() {
        // Given
        String uuid = "test-uuid-123";
        DataProductVersionDeleteCommand command = new DataProductVersionDeleteCommand(uuid, null, null);
        DataProductVersionDeleter deleter = new DataProductVersionDeleter(
                command, presenter, notificationsPort, persistencePort, transactionalPort, dataProductsService);

        when(persistencePort.findByUuid(uuid)).thenThrow(new NotFoundException("Resource with id=" + uuid + " not found"));

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        // When & Then
        assertThatThrownBy(deleter::execute)
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Resource with id=" + uuid + " not found");

        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(persistencePort).findByUuid(uuid);
        verifyNoInteractions(notificationsPort, presenter, dataProductsService);
    }

    @Test
    void whenDataProductNotFoundByFqnThenThrowNotFoundException() {
        // Given
        String fqn = "test.domain:test-product";
        String tag = "v1.0.0";
        DataProductVersionDeleteCommand command = new DataProductVersionDeleteCommand(null, fqn, tag);
        DataProductVersionDeleter deleter = new DataProductVersionDeleter(
                command, presenter, notificationsPort, persistencePort, transactionalPort, dataProductsService);

        Page<DataProduct> emptyPage = new PageImpl<>(List.of(), Pageable.ofSize(1), 0);
        when(dataProductsService.findAllFiltered(any(Pageable.class), any(DataProductSearchOptions.class)))
                .thenReturn(emptyPage);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        // When & Then
        assertThatThrownBy(deleter::execute)
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Data Product with FQN 'test.domain:test-product' not found");

        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(dataProductsService).findAllFiltered(any(Pageable.class), any(DataProductSearchOptions.class));
        verifyNoInteractions(notificationsPort, presenter, persistencePort);
    }

    @Test
    void whenDataProductVersionNotFoundByFqnAndTagThenThrowNotFoundException() {
        // Given
        String fqn = "test.domain:test-product";
        String tag = "v1.0.0";
        DataProductVersionDeleteCommand command = new DataProductVersionDeleteCommand(null, fqn, tag);
        DataProductVersionDeleter deleter = new DataProductVersionDeleter(
                command, presenter, notificationsPort, persistencePort, transactionalPort, dataProductsService);

        DataProduct dataProduct = new DataProduct();
        dataProduct.setUuid("data-product-uuid-123");
        dataProduct.setFqn(fqn);

        Page<DataProduct> dataProductPage = new PageImpl<>(List.of(dataProduct), Pageable.ofSize(1), 1);
        when(dataProductsService.findAllFiltered(any(Pageable.class), any(DataProductSearchOptions.class)))
                .thenReturn(dataProductPage);
        when(persistencePort.findByDataProductUuidAndTag(dataProduct.getUuid(), tag))
                .thenReturn(Optional.empty());

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        // When & Then
        assertThatThrownBy(deleter::execute)
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Data Product Version with tag 'v1.0.0' not found for Data Product with FQN 'test.domain:test-product'");

        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(dataProductsService).findAllFiltered(any(Pageable.class), any(DataProductSearchOptions.class));
        verify(persistencePort).findByDataProductUuidAndTag(dataProduct.getUuid(), tag);
        verifyNoInteractions(notificationsPort, presenter);
    }

    @Test
    void whenDeleteWithUuidThenDeleteSuccessfully() {
        // Given
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setName("Test Version");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setDescription("Test Version Description");

        DataProductVersionDeleteCommand command = new DataProductVersionDeleteCommand(dataProductVersion.getUuid(), null, null);

        when(persistencePort.findByUuid(dataProductVersion.getUuid())).thenReturn(dataProductVersion);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductVersionDeleter deleter = new DataProductVersionDeleter(
                command, presenter, notificationsPort, persistencePort, transactionalPort, dataProductsService);

        // When
        deleter.execute();

        // Then
        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(persistencePort).findByUuid(dataProductVersion.getUuid());
        verify(persistencePort).delete(dataProductVersion);
        verify(notificationsPort).emitDataProductVersionDeleted(dataProductVersion);
        verify(presenter).presentDataProductVersionDeleted(dataProductVersion);
    }

    @Test
    void whenDeleteWithFqnAndTagThenDeleteSuccessfully() {
        // Given
        String fqn = "test.domain:test-product";
        String tag = "v1.0.0";
        
        DataProduct dataProduct = new DataProduct();
        dataProduct.setUuid("data-product-uuid-123");
        dataProduct.setFqn(fqn);
        dataProduct.setName("Test Product");
        dataProduct.setDomain("test.domain");

        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setName("Test Version");
        dataProductVersion.setTag(tag);
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setDataProduct(dataProduct);

        DataProductVersionDeleteCommand command = new DataProductVersionDeleteCommand(null, fqn, tag);

        Page<DataProduct> dataProductPage = new PageImpl<>(List.of(dataProduct), Pageable.ofSize(1), 1);
        when(dataProductsService.findAllFiltered(any(Pageable.class), any(DataProductSearchOptions.class)))
                .thenReturn(dataProductPage);
        when(persistencePort.findByDataProductUuidAndTag(dataProduct.getUuid(), tag))
                .thenReturn(Optional.of(dataProductVersion));

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductVersionDeleter deleter = new DataProductVersionDeleter(
                command, presenter, notificationsPort, persistencePort, transactionalPort, dataProductsService);

        // When
        deleter.execute();

        // Then
        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(dataProductsService).findAllFiltered(any(Pageable.class), any(DataProductSearchOptions.class));
        verify(persistencePort).findByDataProductUuidAndTag(dataProduct.getUuid(), tag);
        verify(persistencePort).delete(dataProductVersion);
        verify(notificationsPort).emitDataProductVersionDeleted(dataProductVersion);
        verify(presenter).presentDataProductVersionDeleted(dataProductVersion);
    }

    @Test
    void whenBothUuidAndFqnTagProvidedThenPreferUuid() {
        // Given
        String uuid = "test-uuid-123";
        String fqn = "test.domain:test-product";
        String tag = "v1.0.0";

        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid(uuid);
        dataProductVersion.setName("Test Version");
        dataProductVersion.setTag(tag);
        dataProductVersion.setDescription("Test Version Description");

        DataProductVersionDeleteCommand command = new DataProductVersionDeleteCommand(uuid, fqn, tag);

        when(persistencePort.findByUuid(uuid)).thenReturn(dataProductVersion);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductVersionDeleter deleter = new DataProductVersionDeleter(
                command, presenter, notificationsPort, persistencePort, transactionalPort, dataProductsService);

        // When
        deleter.execute();

        // Then
        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(persistencePort).findByUuid(uuid);
        verify(persistencePort, never()).findByDataProductUuidAndTag(anyString(), anyString());
        verify(dataProductsService, never()).findAllFiltered(any(Pageable.class), any(DataProductSearchOptions.class));
        verify(persistencePort).delete(dataProductVersion);
        verify(notificationsPort).emitDataProductVersionDeleted(dataProductVersion);
        verify(presenter).presentDataProductVersionDeleted(dataProductVersion);
    }

    @Test
    void whenExecuteThenAllOperationsHappenInTransaction() {
        // Given
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setName("Test Version");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setDescription("Test Version Description");

        DataProductVersionDeleteCommand command = new DataProductVersionDeleteCommand(dataProductVersion.getUuid(), null, null);

        when(persistencePort.findByUuid(dataProductVersion.getUuid())).thenReturn(dataProductVersion);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductVersionDeleter deleter = new DataProductVersionDeleter(
                command, presenter, notificationsPort, persistencePort, transactionalPort, dataProductsService);

        // When
        deleter.execute();

        // Then
        verify(transactionalPort).doInTransaction(any(Runnable.class));

        // Verify that all persistence operations happen within the transaction
        verify(persistencePort).findByUuid(dataProductVersion.getUuid());
        verify(persistencePort).delete(dataProductVersion);
        verify(notificationsPort).emitDataProductVersionDeleted(dataProductVersion);
        verify(presenter).presentDataProductVersionDeleted(dataProductVersion);
    }

    @Test
    void whenDeleteSuccessfullyThenPresenterReceivesCorrectDataProductVersion() {
        // Given
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setName("Test Version");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setDescription("Test Version Description");

        DataProductVersionDeleteCommand command = new DataProductVersionDeleteCommand(dataProductVersion.getUuid(), null, null);

        when(persistencePort.findByUuid(dataProductVersion.getUuid())).thenReturn(dataProductVersion);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductVersionDeleter deleter = new DataProductVersionDeleter(
                command, presenter, notificationsPort, persistencePort, transactionalPort, dataProductsService);

        // When
        deleter.execute();

        // Then
        verify(presenter).presentDataProductVersionDeleted(argThat(presentedDataProductVersion -> {
            if (presentedDataProductVersion == null) {
                return false;
            }
            assertThat(presentedDataProductVersion)
                    .usingRecursiveComparison()
                    .isEqualTo(dataProductVersion);
            return true;
        }));
    }

    @Test
    void whenDeleteSuccessfullyThenNotificationReceivesCorrectDataProductVersion() {
        // Given
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-uuid-123");
        dataProductVersion.setName("Test Version");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setDescription("Test Version Description");

        DataProductVersionDeleteCommand command = new DataProductVersionDeleteCommand(dataProductVersion.getUuid(), null, null);

        when(persistencePort.findByUuid(dataProductVersion.getUuid())).thenReturn(dataProductVersion);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductVersionDeleter deleter = new DataProductVersionDeleter(
                command, presenter, notificationsPort, persistencePort, transactionalPort, dataProductsService);

        // When
        deleter.execute();

        // Then
        verify(notificationsPort).emitDataProductVersionDeleted(argThat(emittedDataProductVersion -> {
            if (emittedDataProductVersion == null) {
                return false;
            }
            assertThat(emittedDataProductVersion)
                    .usingRecursiveComparison()
                    .isEqualTo(dataProductVersion);
            return true;
        }));
    }
}

