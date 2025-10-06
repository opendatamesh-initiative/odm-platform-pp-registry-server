package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.approve;

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
class DataProductApproverTest {

    @Mock
    private DataProductApprovePresenter presenter;

    @Mock
    private DataProductApproverNotificationOutboundPort notificationsPort;

    @Mock
    private DataProductApproverPersistenceOutboundPort persistencePort;

    @Mock
    private TransactionalOutboundPort transactionalPort;

    @Test
    void whenCommandIsNullThenThrowBadRequestException() {
        // Given
        DataProductApproveCommand command = null;
        DataProductApprover approver = new DataProductApprover(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(approver::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("DataProductApproveCommand cannot be null");

        verifyNoInteractions(persistencePort, notificationsPort, presenter, transactionalPort);
    }

    @Test
    void whenDataProductIsNullThenThrowBadRequestException() {
        // Given
        DataProductApproveCommand command = new DataProductApproveCommand(null);
        DataProductApprover approver = new DataProductApprover(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(approver::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("DataProduct cannot be null");

        verifyNoInteractions(persistencePort, notificationsPort, presenter, transactionalPort);
    }

    @Test
    void whenDataProductFqnIsNullThenThrowBadRequestException() {
        // Given
        DataProduct dataProduct = new DataProduct();
        dataProduct.setFqn(null);
        DataProductApproveCommand command = new DataProductApproveCommand(dataProduct);
        DataProductApprover approver = new DataProductApprover(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(approver::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("FQN is required for data product approval");

        verifyNoInteractions(persistencePort, notificationsPort, presenter, transactionalPort);
    }

    @Test
    void whenDataProductFqnIsEmptyThenThrowBadRequestException() {
        // Given
        DataProduct dataProduct = new DataProduct();
        dataProduct.setFqn("");
        DataProductApproveCommand command = new DataProductApproveCommand(dataProduct);
        DataProductApprover approver = new DataProductApprover(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(approver::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("FQN is required for data product approval");

        verifyNoInteractions(persistencePort, notificationsPort, presenter, transactionalPort);
    }

    @Test
    void whenDataProductFqnIsBlankThenThrowBadRequestException() {
        // Given
        DataProduct dataProduct = new DataProduct();
        dataProduct.setFqn("   ");
        DataProductApproveCommand command = new DataProductApproveCommand(dataProduct);
        DataProductApprover approver = new DataProductApprover(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(approver::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("FQN is required for data product approval");

        verifyNoInteractions(persistencePort, notificationsPort, presenter, transactionalPort);
    }

    @Test
    void whenDataProductDoesNotExistThenThrowBadRequestException() {
        // Given
        DataProduct dataProduct = new DataProduct();
        dataProduct.setFqn("test.domain:test-product");
        DataProductApproveCommand command = new DataProductApproveCommand(dataProduct);
        DataProductApprover approver = new DataProductApprover(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        when(persistencePort.find(dataProduct)).thenReturn(Optional.empty());

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        // When & Then
        assertThatThrownBy(approver::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Impossible to approve a data product that does not exist yet. Data Product Fqn: test.domain:test-product");

        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(persistencePort).find(dataProduct);
        verifyNoInteractions(notificationsPort, presenter);
    }

    @Test
    void whenDataProductIsAlreadyApprovedThenThrowBadRequestException() {
        // Given
        DataProduct dataProduct = new DataProduct();
        dataProduct.setFqn("test.domain:test-product");
        dataProduct.setValidationState(DataProductValidationState.APPROVED);
        DataProductApproveCommand command = new DataProductApproveCommand(dataProduct);
        DataProductApprover approver = new DataProductApprover(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        when(persistencePort.find(dataProduct)).thenReturn(Optional.of(dataProduct));

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        // When & Then
        assertThatThrownBy(approver::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Data Product test.domain:test-product already approved");

        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(persistencePort).find(dataProduct);
        verifyNoInteractions(notificationsPort, presenter);
    }

    @Test
    void whenDataProductIsPendingThenApproveSuccessfully() {
        // Given
        DataProduct dataProduct = new DataProduct();
        dataProduct.setFqn("test.domain:test-product");
        dataProduct.setName("Test Product");
        dataProduct.setDomain("test.domain");
        dataProduct.setDisplayName("Test Product Display Name");
        dataProduct.setDescription("Test Product Description");
        dataProduct.setValidationState(DataProductValidationState.PENDING);
        DataProductApproveCommand command = new DataProductApproveCommand(dataProduct);

        when(persistencePort.find(dataProduct)).thenReturn(Optional.of(dataProduct));
        when(persistencePort.save(any(DataProduct.class))).thenReturn(dataProduct);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductApprover approver = new DataProductApprover(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        // When
        approver.execute();

        // Then
        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(persistencePort).find(dataProduct);
        verify(persistencePort).save(any(DataProduct.class));
        verify(notificationsPort).emitDataProductInitialized(dataProduct);
        verify(presenter).presentDataProductApproved(dataProduct);

        // Verify that validation state is set to APPROVED
        verify(persistencePort).save(argThat(savedDataProduct ->
                DataProductValidationState.APPROVED.equals(savedDataProduct.getValidationState())));
    }

    @Test
    void whenDataProductIsRejectedThenApproveSuccessfully() {
        // Given
        DataProduct dataProduct = new DataProduct();
        dataProduct.setFqn("test.domain:test-product");
        dataProduct.setName("Test Product");
        dataProduct.setDomain("test.domain");
        dataProduct.setDisplayName("Test Product Display Name");
        dataProduct.setDescription("Test Product Description");
        dataProduct.setValidationState(DataProductValidationState.REJECTED);
        DataProductApproveCommand command = new DataProductApproveCommand(dataProduct);

        when(persistencePort.find(dataProduct)).thenReturn(Optional.of(dataProduct));
        when(persistencePort.save(any(DataProduct.class))).thenReturn(dataProduct);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductApprover approver = new DataProductApprover(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        // When
        approver.execute();

        // Then
        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(persistencePort).find(dataProduct);
        verify(persistencePort).save(any(DataProduct.class));
        verify(notificationsPort).emitDataProductInitialized(dataProduct);
        verify(presenter).presentDataProductApproved(dataProduct);

        // Verify that validation state is set to APPROVED
        verify(persistencePort).save(argThat(savedDataProduct ->
                DataProductValidationState.APPROVED.equals(savedDataProduct.getValidationState())));
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
        dataProduct.setValidationState(DataProductValidationState.PENDING);
        DataProductApproveCommand command = new DataProductApproveCommand(dataProduct);

        when(persistencePort.find(dataProduct)).thenReturn(Optional.of(dataProduct));
        when(persistencePort.save(any(DataProduct.class))).thenReturn(dataProduct);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductApprover approver = new DataProductApprover(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        // When
        approver.execute();

        // Then
        verify(transactionalPort).doInTransaction(any(Runnable.class));

        // Verify that all persistence operations happen within the transaction
        verify(persistencePort).find(dataProduct);
        verify(persistencePort).save(any(DataProduct.class));
        verify(notificationsPort).emitDataProductInitialized(dataProduct);
        verify(presenter).presentDataProductApproved(dataProduct);
    }

    @Test
    void whenApproveSuccessfullyThenPresenterReceivesCorrectDataProduct() {
        // Given
        DataProduct dataProduct = new DataProduct();
        dataProduct.setFqn("test.domain:test-product");
        dataProduct.setName("Test Product");
        dataProduct.setDomain("test.domain");
        dataProduct.setDisplayName("Test Product Display Name");
        dataProduct.setDescription("Test Product Description");
        dataProduct.setValidationState(DataProductValidationState.PENDING);
        DataProductApproveCommand command = new DataProductApproveCommand(dataProduct);

        when(persistencePort.find(dataProduct)).thenReturn(Optional.of(dataProduct));
        when(persistencePort.save(any(DataProduct.class))).thenReturn(dataProduct);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductApprover approver = new DataProductApprover(
                command, presenter, notificationsPort, persistencePort, transactionalPort);

        // When
        approver.execute();

        // Then
        verify(presenter).presentDataProductApproved(argThat(presentedDataProduct -> {
            assertThat(presentedDataProduct)
                    .usingRecursiveComparison()
                    .isEqualTo(dataProduct);
            return true;
        }));
    }
}
