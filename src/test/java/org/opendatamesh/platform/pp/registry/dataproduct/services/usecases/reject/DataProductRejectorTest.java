package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.reject;

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
class DataProductRejectorTest {

    @Mock
    private DataProductRejectPresenter presenter;

    @Mock
    private DataProductRejectorPersistenceOutboundPort persistencePort;

    @Mock
    private TransactionalOutboundPort transactionalPort;

    @Test
    void whenCommandIsNullThenThrowBadRequestException() {
        // Given
        DataProductRejectCommand command = null;
        DataProductRejector rejector = new DataProductRejector(
                command, presenter, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(() -> rejector.execute())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("DataProductRejectCommand cannot be null");

        verifyNoInteractions(persistencePort, presenter, transactionalPort);
    }

    @Test
    void whenDataProductIsNullThenThrowBadRequestException() {
        // Given
        DataProductRejectCommand command = new DataProductRejectCommand(null);
        DataProductRejector rejector = new DataProductRejector(
                command, presenter, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(() -> rejector.execute())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("DataProduct cannot be null");

        verifyNoInteractions(persistencePort, presenter, transactionalPort);
    }

    @Test
    void whenDataProductFqnIsNullThenThrowBadRequestException() {
        // Given
        DataProduct dataProduct = new DataProduct();
        dataProduct.setFqn(null);
        DataProductRejectCommand command = new DataProductRejectCommand(dataProduct);
        DataProductRejector rejector = new DataProductRejector(
                command, presenter, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(() -> rejector.execute())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("FQN is required for data product rejection");

        verifyNoInteractions(persistencePort, presenter, transactionalPort);
    }

    @Test
    void whenDataProductFqnIsEmptyThenThrowBadRequestException() {
        // Given
        DataProduct dataProduct = new DataProduct();
        dataProduct.setFqn("");
        DataProductRejectCommand command = new DataProductRejectCommand(dataProduct);
        DataProductRejector rejector = new DataProductRejector(
                command, presenter, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(() -> rejector.execute())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("FQN is required for data product rejection");

        verifyNoInteractions(persistencePort, presenter, transactionalPort);
    }

    @Test
    void whenDataProductFqnIsBlankThenThrowBadRequestException() {
        // Given
        DataProduct dataProduct = new DataProduct();
        dataProduct.setFqn("   ");
        DataProductRejectCommand command = new DataProductRejectCommand(dataProduct);
        DataProductRejector rejector = new DataProductRejector(
                command, presenter, persistencePort, transactionalPort);

        // When & Then
        assertThatThrownBy(() -> rejector.execute())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("FQN is required for data product rejection");

        verifyNoInteractions(persistencePort, presenter, transactionalPort);
    }

    @Test
    void whenDataProductDoesNotExistThenThrowBadRequestException() {
        // Given
        DataProduct dataProduct = new DataProduct();
        dataProduct.setFqn("test.domain:test-product");
        DataProductRejectCommand command = new DataProductRejectCommand(dataProduct);
        DataProductRejector rejector = new DataProductRejector(
                command, presenter, persistencePort, transactionalPort);

        when(persistencePort.find(dataProduct)).thenReturn(Optional.empty());

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        // When & Then
        assertThatThrownBy(() -> rejector.execute())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Impossible to reject a data product that does not exist yet. Data Product Fqn: test.domain:test-product");

        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(persistencePort).find(dataProduct);
        verifyNoInteractions(presenter);
    }

    @Test
    void whenDataProductIsPendingThenRejectSuccessfully() {
        // Given
        DataProduct dataProduct = new DataProduct();
        dataProduct.setFqn("test.domain:test-product");
        dataProduct.setName("Test Product");
        dataProduct.setDomain("test.domain");
        dataProduct.setDisplayName("Test Product Display Name");
        dataProduct.setDescription("Test Product Description");
        dataProduct.setValidationState(DataProductValidationState.PENDING);
        DataProductRejectCommand command = new DataProductRejectCommand(dataProduct);

        when(persistencePort.find(dataProduct)).thenReturn(Optional.of(dataProduct));
        when(persistencePort.save(any(DataProduct.class))).thenReturn(dataProduct);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductRejector rejector = new DataProductRejector(
                command, presenter, persistencePort, transactionalPort);

        // When
        rejector.execute();

        // Then
        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(persistencePort).find(dataProduct);
        verify(persistencePort).save(any(DataProduct.class));
        verify(presenter).presentDataProductRejected(dataProduct);

        // Verify that validation state is set to REJECTED
        verify(persistencePort).save(argThat(savedDataProduct ->
                DataProductValidationState.REJECTED.equals(savedDataProduct.getValidationState())));
    }

    @Test
    void whenDataProductIsApprovedThenRejectSuccessfully() {
        // Given
        DataProduct dataProduct = new DataProduct();
        dataProduct.setFqn("test.domain:test-product");
        dataProduct.setName("Test Product");
        dataProduct.setDomain("test.domain");
        dataProduct.setDisplayName("Test Product Display Name");
        dataProduct.setDescription("Test Product Description");
        dataProduct.setValidationState(DataProductValidationState.APPROVED);
        DataProductRejectCommand command = new DataProductRejectCommand(dataProduct);

        when(persistencePort.find(dataProduct)).thenReturn(Optional.of(dataProduct));
        when(persistencePort.save(any(DataProduct.class))).thenReturn(dataProduct);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductRejector rejector = new DataProductRejector(
                command, presenter, persistencePort, transactionalPort);

        // When
        rejector.execute();

        // Then
        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(persistencePort).find(dataProduct);
        verify(persistencePort).save(any(DataProduct.class));
        verify(presenter).presentDataProductRejected(dataProduct);

        // Verify that validation state is set to REJECTED
        verify(persistencePort).save(argThat(savedDataProduct ->
                DataProductValidationState.REJECTED.equals(savedDataProduct.getValidationState())));
    }

    @Test
    void whenDataProductIsAlreadyRejectedThenRejectSuccessfully() {
        // Given
        DataProduct dataProduct = new DataProduct();
        dataProduct.setFqn("test.domain:test-product");
        dataProduct.setName("Test Product");
        dataProduct.setDomain("test.domain");
        dataProduct.setDisplayName("Test Product Display Name");
        dataProduct.setDescription("Test Product Description");
        dataProduct.setValidationState(DataProductValidationState.REJECTED);
        DataProductRejectCommand command = new DataProductRejectCommand(dataProduct);

        when(persistencePort.find(dataProduct)).thenReturn(Optional.of(dataProduct));
        when(persistencePort.save(any(DataProduct.class))).thenReturn(dataProduct);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductRejector rejector = new DataProductRejector(
                command, presenter, persistencePort, transactionalPort);

        // When
        rejector.execute();

        // Then
        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(persistencePort).find(dataProduct);
        verify(persistencePort).save(any(DataProduct.class));
        verify(presenter).presentDataProductRejected(dataProduct);

        // Verify that validation state is set to REJECTED
        verify(persistencePort).save(argThat(savedDataProduct ->
                DataProductValidationState.REJECTED.equals(savedDataProduct.getValidationState())));
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
        DataProductRejectCommand command = new DataProductRejectCommand(dataProduct);

        when(persistencePort.find(dataProduct)).thenReturn(Optional.of(dataProduct));
        when(persistencePort.save(any(DataProduct.class))).thenReturn(dataProduct);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductRejector rejector = new DataProductRejector(
                command, presenter, persistencePort, transactionalPort);

        // When
        rejector.execute();

        // Then
        verify(transactionalPort).doInTransaction(any(Runnable.class));

        // Verify that all persistence operations happen within the transaction
        verify(persistencePort).find(dataProduct);
        verify(persistencePort).save(any(DataProduct.class));
        verify(presenter).presentDataProductRejected(dataProduct);
    }

    @Test
    void whenRejectSuccessfullyThenPresenterReceivesCorrectDataProduct() {
        // Given
        DataProduct dataProduct = new DataProduct();
        dataProduct.setFqn("test.domain:test-product");
        dataProduct.setName("Test Product");
        dataProduct.setDomain("test.domain");
        dataProduct.setDisplayName("Test Product Display Name");
        dataProduct.setDescription("Test Product Description");
        dataProduct.setValidationState(DataProductValidationState.PENDING);
        DataProductRejectCommand command = new DataProductRejectCommand(dataProduct);

        when(persistencePort.find(dataProduct)).thenReturn(Optional.of(dataProduct));
        when(persistencePort.save(any(DataProduct.class))).thenReturn(dataProduct);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductRejector rejector = new DataProductRejector(
                command, presenter, persistencePort, transactionalPort);

        // When
        rejector.execute();

        // Then
        verify(presenter).presentDataProductRejected(argThat(presentedDataProduct -> {
            assertThat(presentedDataProduct)
                    .usingRecursiveComparison()
                    .isEqualTo(dataProduct);
            return true;
        }));
    }
}
