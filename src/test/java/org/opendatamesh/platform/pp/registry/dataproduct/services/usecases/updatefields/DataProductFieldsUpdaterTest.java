package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.updatefields;

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
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.exceptions.NotFoundException;
import org.opendatamesh.platform.pp.registry.utils.usecases.TransactionalOutboundPort;

@ExtendWith(MockitoExtension.class)
class DataProductFieldsUpdaterTest {

    @Mock
    private DataProductFieldsUpdatePresenter presenter;

    @Mock
    private DataProductFieldsUpdaterPersistenceOutboundPort persistencePort;

    @Mock
    private TransactionalOutboundPort transactionalPort;

    @Test
    void whenCommandIsNullThenThrowBadRequestException() {
        DataProductFieldsUpdateCommand command = null;
        DataProductFieldsUpdater updater = new DataProductFieldsUpdater(command, presenter, persistencePort, transactionalPort);

        assertThatThrownBy(updater::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("DataProductFieldsUpdateCommand cannot be null");

        verifyNoInteractions(persistencePort, presenter, transactionalPort);
    }

    @Test
    void whenUuidIsNullThenThrowBadRequestException() {
        DataProductFieldsUpdateCommand command = new DataProductFieldsUpdateCommand(null, "Display", "Desc", null);
        DataProductFieldsUpdater updater = new DataProductFieldsUpdater(command, presenter, persistencePort, transactionalPort);

        assertThatThrownBy(updater::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("UUID is required for data product fields update");

        verifyNoInteractions(persistencePort, presenter, transactionalPort);
    }

    @Test
    void whenUuidIsEmptyThenThrowBadRequestException() {
        DataProductFieldsUpdateCommand command = new DataProductFieldsUpdateCommand("", "Display", "Desc", null);
        DataProductFieldsUpdater updater = new DataProductFieldsUpdater(command, presenter, persistencePort, transactionalPort);

        assertThatThrownBy(updater::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("UUID is required for data product fields update");

        verifyNoInteractions(persistencePort, presenter, transactionalPort);
    }

    @Test
    void whenUuidIsBlankThenThrowBadRequestException() {
        DataProductFieldsUpdateCommand command = new DataProductFieldsUpdateCommand("   ", "Display", "Desc", null);
        DataProductFieldsUpdater updater = new DataProductFieldsUpdater(command, presenter, persistencePort, transactionalPort);

        assertThatThrownBy(updater::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("UUID is required for data product fields update");

        verifyNoInteractions(persistencePort, presenter, transactionalPort);
    }

    @Test
    void whenNoExistingDataProductThenThrowNotFoundException() {
        String wrongUuid = "test-uuid-error";
        DataProductFieldsUpdateCommand command = new DataProductFieldsUpdateCommand(wrongUuid, "Display", "Desc", null);

        when(persistencePort.findByUuid(wrongUuid))
                .thenThrow(new NotFoundException("Resource with id=" + wrongUuid + " not found"));

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductFieldsUpdater updater = new DataProductFieldsUpdater(command, presenter, persistencePort, transactionalPort);

        assertThatThrownBy(updater::execute)
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Resource with id=" + wrongUuid + " not found");

        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(persistencePort).findByUuid(wrongUuid);
        verifyNoInteractions(presenter);
    }

    @Test
    void whenUpdateSuccessfullyThenPresenterReceivesCorrectDataProduct() {
        DataProduct existingDataProduct = new DataProduct();
        existingDataProduct.setUuid("test-uuid-123");
        existingDataProduct.setFqn("domain:name");
        existingDataProduct.setName("name");
        existingDataProduct.setDomain("domain");
        existingDataProduct.setDisplayName("Original Display");
        existingDataProduct.setDescription("Original Description");

        DataProduct updatedDataProduct = new DataProduct();
        updatedDataProduct.setUuid("test-uuid-123");
        updatedDataProduct.setFqn("domain:name");
        updatedDataProduct.setName("name");
        updatedDataProduct.setDomain("domain");
        updatedDataProduct.setDisplayName("Updated Display");
        updatedDataProduct.setDescription("Updated Description");

        DataProductFieldsUpdateCommand command = new DataProductFieldsUpdateCommand(
                "test-uuid-123",
                "Updated Display",
                "Updated Description",
                null);

        when(persistencePort.findByUuid("test-uuid-123")).thenReturn(existingDataProduct);
        when(persistencePort.save(any(DataProduct.class))).thenReturn(updatedDataProduct);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductFieldsUpdater updater = new DataProductFieldsUpdater(command, presenter, persistencePort, transactionalPort);

        updater.execute();

        verify(presenter).presentDataProductFieldsUpdated(argThat(dataProduct ->
                dataProduct.getDisplayName().equals("Updated Display") && dataProduct.getDescription().equals("Updated Description")
        ));
    }

    @Test
    void whenExecuteThenAllOperationsHappenInTransaction() {
        DataProduct existingDataProduct = new DataProduct();
        existingDataProduct.setUuid("test-uuid-123");
        existingDataProduct.setFqn("domain:name");
        existingDataProduct.setName("name");
        existingDataProduct.setDomain("domain");
        existingDataProduct.setDisplayName("Original");
        existingDataProduct.setDescription("Original");

        DataProduct savedDataProduct = new DataProduct();
        savedDataProduct.setUuid("test-uuid-123");
        savedDataProduct.setDisplayName("Updated");
        savedDataProduct.setDescription("Updated");

        DataProductFieldsUpdateCommand command = new DataProductFieldsUpdateCommand(
                "test-uuid-123", "Updated", "Updated", null);

        when(persistencePort.findByUuid("test-uuid-123")).thenReturn(existingDataProduct);
        when(persistencePort.save(any(DataProduct.class))).thenReturn(savedDataProduct);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        DataProductFieldsUpdater updater = new DataProductFieldsUpdater(command, presenter, persistencePort, transactionalPort);

        updater.execute();

        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(persistencePort).findByUuid("test-uuid-123");
        verify(persistencePort).save(any(DataProduct.class));
        verify(presenter).presentDataProductFieldsUpdated(savedDataProduct);
    }
}
