package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.init;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductValidationState;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.utils.usecases.TransactionalOutboundPort;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataProductInitializerTest {

    @Mock
    private DataProductInitPresenter presenter;

    @Mock
    private DataProductInitializerNotificationOutboundPort notificationsPort;

    @Mock
    private DataProductInitializerPersistenceOutboundPort persistencePort;

    @Mock
    private TransactionalOutboundPort transactionalPort;


    @Test
    void whenCommandIsNullThenThrowBadRequestException() {
        // Given
        DataProductInitCommand command = null;
        DataProductInitializer initializer = new DataProductInitializer(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(() -> initializer.execute())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("DataProductInitCommand cannot be null");

        verifyNoInteractions(persistencePort, notificationsPort, presenter, transactionalPort);
    }

    @Test
    void whenDataProductIsNullThenThrowBadRequestException() {
        // Given
        DataProductInitCommand command = new DataProductInitCommand(null);
        DataProductInitializer initializer = new DataProductInitializer(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(() -> initializer.execute())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("DataProduct cannot be null");

        verifyNoInteractions(persistencePort, notificationsPort, presenter, transactionalPort);
    }

    @Test
    void whenDataProductFqnIsNullThenThrowBadRequestException() {
        // Given
        DataProduct dataProduct = new DataProduct();
        dataProduct.setFqn(null);
        dataProduct.setName("Test Product");
        dataProduct.setDomain("test.domain");
        dataProduct.setDisplayName("Test Product Display Name");
        dataProduct.setDescription("Test Product Description");
        DataProductInitCommand command = new DataProductInitCommand(dataProduct);
        DataProductInitializer initializer = new DataProductInitializer(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(() -> initializer.execute())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("FQN is required for data product initialization");

        verifyNoInteractions(persistencePort, notificationsPort, presenter, transactionalPort);
    }

    @Test
    void whenDataProductFqnIsEmptyThenThrowBadRequestException() {
        // Given
        DataProduct dataProduct = new DataProduct();
        dataProduct.setFqn("");
        dataProduct.setName("Test Product");
        dataProduct.setDomain("test.domain");
        dataProduct.setDisplayName("Test Product Display Name");
        dataProduct.setDescription("Test Product Description");
        DataProductInitCommand command = new DataProductInitCommand(dataProduct);
        DataProductInitializer initializer = new DataProductInitializer(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(() -> initializer.execute())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("FQN is required for data product initialization");

        verifyNoInteractions(persistencePort, notificationsPort, presenter, transactionalPort);
    }

    @Test
    void whenDataProductFqnIsBlankThenThrowBadRequestException() {
        // Given
        DataProduct dataProduct = new DataProduct();
        dataProduct.setFqn("   ");
        dataProduct.setName("Test Product");
        dataProduct.setDomain("test.domain");
        dataProduct.setDisplayName("Test Product Display Name");
        dataProduct.setDescription("Test Product Description");
        DataProductInitCommand command = new DataProductInitCommand(dataProduct);
        DataProductInitializer initializer = new DataProductInitializer(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(() -> initializer.execute())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("FQN is required for data product initialization");

        verifyNoInteractions(persistencePort, notificationsPort, presenter, transactionalPort);
    }

    @Test
    void whenNoExistingDataProductThenInitializeSuccessfully() {
        // Given
        DataProduct dataProduct = new DataProduct();
        dataProduct.setFqn("test.domain:test-product");
        dataProduct.setName("Test Product");
        dataProduct.setDomain("test.domain");
        dataProduct.setDisplayName("Test Product Display Name");
        dataProduct.setDescription("Test Product Description");
        DataProductInitCommand command = new DataProductInitCommand(dataProduct);

        when(persistencePort.find(dataProduct)).thenReturn(Optional.empty());
        when(persistencePort.save(any(DataProduct.class))).thenReturn(dataProduct);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductInitializer initializer = new DataProductInitializer(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        // When
        initializer.execute();

        // Then
        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(persistencePort).find(dataProduct);
        verify(persistencePort).save(any(DataProduct.class));
        verify(notificationsPort).emitDataProductInitializationRequested(dataProduct);
        verify(presenter).presentDataProductInitialized(dataProduct);

        // Verify that validation state is set to PENDING
        verify(persistencePort).save(argThat(savedDataProduct ->
                DataProductValidationState.PENDING.equals(savedDataProduct.getValidationState())));
    }

    @Test
    void whenExistingDataProductIsPendingThenThrowBadRequestException() {
        // Given
        DataProduct dataProduct = new DataProduct();
        dataProduct.setFqn("test.domain:test-product");
        dataProduct.setName("Test Product");
        dataProduct.setDomain("test.domain");
        dataProduct.setDisplayName("Test Product Display Name");
        dataProduct.setDescription("Test Product Description");
        DataProductInitCommand command = new DataProductInitCommand(dataProduct);

        DataProduct existingDataProduct = new DataProduct();
        existingDataProduct.setFqn("test.domain:test-product");
        existingDataProduct.setValidationState(DataProductValidationState.PENDING);

        when(persistencePort.find(dataProduct)).thenReturn(Optional.of(existingDataProduct));

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductInitializer initializer = new DataProductInitializer(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(() -> initializer.execute())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Impossible to init a data product already existent and in PENDING validation state.");

        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(persistencePort).find(dataProduct);
        verifyNoMoreInteractions(persistencePort);
        verifyNoInteractions(notificationsPort, presenter);
    }

    @Test
    void whenExistingDataProductIsApprovedThenThrowBadRequestException() {
        // Given
        DataProduct dataProduct = new DataProduct();
        dataProduct.setFqn("test.domain:test-product");
        dataProduct.setName("Test Product");
        dataProduct.setDomain("test.domain");
        dataProduct.setDisplayName("Test Product Display Name");
        dataProduct.setDescription("Test Product Description");
        DataProductInitCommand command = new DataProductInitCommand(dataProduct);

        DataProduct existingDataProduct = new DataProduct();
        existingDataProduct.setFqn("test.domain:test-product");
        existingDataProduct.setValidationState(DataProductValidationState.APPROVED);

        when(persistencePort.find(dataProduct)).thenReturn(Optional.of(existingDataProduct));

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductInitializer initializer = new DataProductInitializer(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(() -> initializer.execute())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Impossible to init a data product already existent and APPROVED.");

        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(persistencePort).find(dataProduct);
        verifyNoMoreInteractions(persistencePort);
        verifyNoInteractions(notificationsPort, presenter);
    }

    @Test
    void whenExistingDataProductIsRejectedThenDeleteAndInitializeSuccessfully() {
        // Given
        DataProduct dataProduct = new DataProduct();
        dataProduct.setFqn("test.domain:test-product");
        dataProduct.setName("Test Product");
        dataProduct.setDomain("test.domain");
        dataProduct.setDisplayName("Test Product Display Name");
        dataProduct.setDescription("Test Product Description");
        DataProductInitCommand command = new DataProductInitCommand(dataProduct);

        DataProduct existingDataProduct = new DataProduct();
        existingDataProduct.setFqn("test.domain:test-product");
        existingDataProduct.setValidationState(DataProductValidationState.REJECTED);

        when(persistencePort.find(dataProduct)).thenReturn(Optional.of(existingDataProduct));
        when(persistencePort.save(any(DataProduct.class))).thenReturn(dataProduct);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductInitializer initializer = new DataProductInitializer(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        // When
        initializer.execute();

        // Then
        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(persistencePort).find(dataProduct);
        verify(persistencePort).delete(existingDataProduct);
        verify(persistencePort).save(any(DataProduct.class));
        verify(notificationsPort).emitDataProductInitializationRequested(dataProduct);
        verify(presenter).presentDataProductInitialized(dataProduct);

        // Verify that validation state is set to PENDING
        verify(persistencePort).save(argThat(savedDataProduct ->
                DataProductValidationState.PENDING.equals(savedDataProduct.getValidationState())));
    }

    @Test
    void whenExecuteThenAllOperationsHappenInTransaction() {
        // Given
        DataProduct dataProduct = new DataProduct();
        dataProduct.setFqn("test.domain:test-product");
        dataProduct.setName("Test Product");
        dataProduct.setDomain("test.domain");
        dataProduct.setDisplayName("Test Product Display Name");
        dataProduct.setDescription("Test Product Description");
        DataProductInitCommand command = new DataProductInitCommand(dataProduct);

        when(persistencePort.find(dataProduct)).thenReturn(Optional.empty());
        when(persistencePort.save(any(DataProduct.class))).thenReturn(dataProduct);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductInitializer initializer = new DataProductInitializer(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        // When
        initializer.execute();

        // Then
        verify(transactionalPort).doInTransaction(any(Runnable.class));

        // Verify that all persistence operations happen within the transaction
        verify(persistencePort).find(dataProduct);
        verify(persistencePort).save(any(DataProduct.class));
        verify(notificationsPort).emitDataProductInitializationRequested(dataProduct);
        verify(presenter).presentDataProductInitialized(dataProduct);
    }

    @Test
    void whenInitializeSuccessfullyThenPresenterReceivesCorrectDataProduct() {
        // Given
        DataProduct dataProduct = new DataProduct();
        dataProduct.setFqn("test.domain:test-product");
        dataProduct.setName("Test Product");
        dataProduct.setDomain("test.domain");
        dataProduct.setDisplayName("Test Product Display Name");
        dataProduct.setDescription("Test Product Description");
        DataProductInitCommand command = new DataProductInitCommand(dataProduct);

        when(persistencePort.find(dataProduct)).thenReturn(Optional.empty());
        when(persistencePort.save(any(DataProduct.class))).thenReturn(dataProduct);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductInitializer initializer = new DataProductInitializer(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        // When
        initializer.execute();

        // Then
        verify(presenter).presentDataProductInitialized(argThat(presentedDataProduct -> {
            assertThat(presentedDataProduct)
                    .usingRecursiveComparison()
                    .isEqualTo(dataProduct);
            return true;
        }));
    }
}
