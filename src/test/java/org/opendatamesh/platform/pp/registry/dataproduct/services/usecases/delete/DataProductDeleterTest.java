package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.delete;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.exceptions.NotFoundException;
import org.opendatamesh.platform.pp.registry.utils.usecases.TransactionalOutboundPort;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataProductDeleterTest {

    @Mock
    private DataProductDeletePresenter presenter;

    @Mock
    private DataProductDeleterNotificationOutboundPort notificationsPort;

    @Mock
    private DataProductDeleterPersistenceOutboundPort persistencePort;

    @Mock
    private TransactionalOutboundPort transactionalPort;

    @Test
    void whenCommandIsNullThenThrowBadRequestException() {
        // Given
        DataProductDeleteCommand command = null;
        DataProductDeleter deleter = new DataProductDeleter(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(deleter::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("DataProductDeleteCommand cannot be null");

        verifyNoInteractions(persistencePort, notificationsPort, presenter, transactionalPort);
    }

    @Test
    void whenBothUuidAndFqnAreNullThenThrowBadRequestException() {
        // Given
        DataProductDeleteCommand command = new DataProductDeleteCommand(null, null);
        DataProductDeleter deleter = new DataProductDeleter(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(deleter::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Either dataProductUuid or dataProductFqn must be provided");

        verifyNoInteractions(persistencePort, notificationsPort, presenter, transactionalPort);
    }

    @Test
    void whenBothUuidAndFqnAreEmptyThenThrowBadRequestException() {
        // Given
        DataProductDeleteCommand command = new DataProductDeleteCommand("", "");
        DataProductDeleter deleter = new DataProductDeleter(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(deleter::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Either dataProductUuid or dataProductFqn must be provided");

        verifyNoInteractions(persistencePort, notificationsPort, presenter, transactionalPort);
    }

    @Test
    void whenBothUuidAndFqnAreBlankThenThrowBadRequestException() {
        // Given
        DataProductDeleteCommand command = new DataProductDeleteCommand("   ", "   ");
        DataProductDeleter deleter = new DataProductDeleter(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(deleter::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Either dataProductUuid or dataProductFqn must be provided");

        verifyNoInteractions(persistencePort, notificationsPort, presenter, transactionalPort);
    }

    @Test
    void whenDataProductNotFoundByUuidThenThrowNotFoundException() {
        // Given
        String uuid = "test-uuid-123";
        DataProductDeleteCommand command = new DataProductDeleteCommand(uuid, null);
        DataProductDeleter deleter = new DataProductDeleter(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

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
        verifyNoInteractions(notificationsPort, presenter);
    }

    @Test
    void whenDataProductNotFoundByFqnThenThrowNotFoundException() {
        // Given
        String fqn = "test.domain:test-product";
        DataProductDeleteCommand command = new DataProductDeleteCommand(null, fqn);
        DataProductDeleter deleter = new DataProductDeleter(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        when(persistencePort.findByFqn(fqn)).thenReturn(Optional.empty());

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
        verify(persistencePort).findByFqn(fqn);
        verifyNoInteractions(notificationsPort, presenter);
    }

    @Test
    void whenDeleteWithUuidThenDeleteSuccessfully() {
        // Given
        DataProduct dataProduct = new DataProduct();
        dataProduct.setFqn("test.domain:test-product");
        dataProduct.setUuid("test-uuid-123");
        dataProduct.setName("Test Product");
        dataProduct.setDomain("test.domain");
        dataProduct.setDisplayName("Test Product Display Name");
        dataProduct.setDescription("Test Product Description");

        DataProductDeleteCommand command = new DataProductDeleteCommand(dataProduct.getUuid(), null);

        when(persistencePort.findByUuid(dataProduct.getUuid())).thenReturn(dataProduct);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductDeleter deleter = new DataProductDeleter(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        // When
        deleter.execute();

        // Then
        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(persistencePort).findByUuid(dataProduct.getUuid());
        verify(persistencePort).delete(dataProduct);
        verify(notificationsPort).emitDataProductDeleted(dataProduct);
        verify(presenter).presentDataProductDeleted(dataProduct);
    }

    @Test
    void whenDeleteWithFqnThenDeleteSuccessfully() {
        // Given
        DataProduct dataProduct = new DataProduct();
        dataProduct.setFqn("test.domain:test-product");
        dataProduct.setUuid("test-uuid-123");
        dataProduct.setName("Test Product");
        dataProduct.setDomain("test.domain");
        dataProduct.setDisplayName("Test Product Display Name");
        dataProduct.setDescription("Test Product Description");

        DataProductDeleteCommand command = new DataProductDeleteCommand(null, dataProduct.getFqn());

        when(persistencePort.findByFqn(dataProduct.getFqn())).thenReturn(Optional.of(dataProduct));

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductDeleter deleter = new DataProductDeleter(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        // When
        deleter.execute();

        // Then
        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(persistencePort).findByFqn(dataProduct.getFqn());
        verify(persistencePort).delete(dataProduct);
        verify(notificationsPort).emitDataProductDeleted(dataProduct);
        verify(presenter).presentDataProductDeleted(dataProduct);
    }

    @Test
    void whenBothUuidAndFqnProvidedThenPreferUuid() {
        // Given
        DataProduct dataProduct = new DataProduct();
        dataProduct.setFqn("test.domain:test-product");
        dataProduct.setUuid("test-uuid-123");
        dataProduct.setName("Test Product");
        dataProduct.setDomain("test.domain");
        dataProduct.setDisplayName("Test Product Display Name");
        dataProduct.setDescription("Test Product Description");

        DataProductDeleteCommand command = new DataProductDeleteCommand(dataProduct.getUuid(), dataProduct.getFqn());

        when(persistencePort.findByUuid(dataProduct.getUuid())).thenReturn(dataProduct);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductDeleter deleter = new DataProductDeleter(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        // When
        deleter.execute();

        // Then
        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(persistencePort).findByUuid(dataProduct.getUuid());
        verify(persistencePort, never()).findByFqn(anyString());
        verify(persistencePort).delete(dataProduct);
        verify(notificationsPort).emitDataProductDeleted(dataProduct);
        verify(presenter).presentDataProductDeleted(dataProduct);
    }

    @Test
    void whenExecuteThenAllOperationsHappenInTransaction() {
        // Given
        DataProduct dataProduct = new DataProduct();
        dataProduct.setFqn("test.domain:test-product");
        dataProduct.setUuid("test-uuid-123");
        dataProduct.setName("Test Product");
        dataProduct.setDomain("test.domain");
        dataProduct.setDisplayName("Test Product Display Name");
        dataProduct.setDescription("Test Product Description");

        DataProductDeleteCommand command = new DataProductDeleteCommand(dataProduct.getUuid(), null);

        when(persistencePort.findByUuid(dataProduct.getUuid())).thenReturn(dataProduct);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductDeleter deleter = new DataProductDeleter(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        // When
        deleter.execute();

        // Then
        verify(transactionalPort).doInTransaction(any(Runnable.class));

        // Verify that all persistence operations happen within the transaction
        verify(persistencePort).findByUuid(dataProduct.getUuid());
        verify(persistencePort).delete(dataProduct);
        verify(notificationsPort).emitDataProductDeleted(dataProduct);
        verify(presenter).presentDataProductDeleted(dataProduct);
    }

    @Test
    void whenDeleteSuccessfullyThenPresenterReceivesCorrectDataProduct() {
        // Given
        DataProduct dataProduct = new DataProduct();
        dataProduct.setFqn("test.domain:test-product");
        dataProduct.setUuid("test-uuid-123");
        dataProduct.setName("Test Product");
        dataProduct.setDomain("test.domain");
        dataProduct.setDisplayName("Test Product Display Name");
        dataProduct.setDescription("Test Product Description");

        DataProductDeleteCommand command = new DataProductDeleteCommand(dataProduct.getUuid(), null);

        when(persistencePort.findByUuid(dataProduct.getUuid())).thenReturn(dataProduct);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductDeleter deleter = new DataProductDeleter(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        // When
        deleter.execute();

        // Then
        verify(presenter).presentDataProductDeleted(argThat(presentedDataProduct -> {
            assertThat(presentedDataProduct)
                    .usingRecursiveComparison()
                    .isEqualTo(dataProduct);
            return true;
        }));
    }

    @Test
    void whenDeleteSuccessfullyThenNotificationReceivesCorrectDataProduct() {
        // Given
        DataProduct dataProduct = new DataProduct();
        dataProduct.setFqn("test.domain:test-product");
        dataProduct.setUuid("test-uuid-123");
        dataProduct.setName("Test Product");
        dataProduct.setDomain("test.domain");
        dataProduct.setDisplayName("Test Product Display Name");
        dataProduct.setDescription("Test Product Description");

        DataProductDeleteCommand command = new DataProductDeleteCommand(dataProduct.getUuid(), null);

        when(persistencePort.findByUuid(dataProduct.getUuid())).thenReturn(dataProduct);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductDeleter deleter = new DataProductDeleter(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        // When
        deleter.execute();

        // Then
        verify(notificationsPort).emitDataProductDeleted(argThat(emittedDataProduct -> {
            assertThat(emittedDataProduct)
                    .usingRecursiveComparison()
                    .isEqualTo(dataProduct);
            return true;
        }));
    }
}

