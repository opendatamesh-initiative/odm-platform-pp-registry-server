package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.approveinitialization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;

@ExtendWith(MockitoExtension.class)
class DataProductInitializationApproverTest {

    @Mock
    private DataProductInitializationApproverPresenter presenter;

    @Mock
    private DataProductInitializationApproverNotificationOutboundPort notificationsPort;

    @Test
    void whenCommandIsNullThenThrowBadRequestException() {
        // Given
        DataProductInitializationApproverCommand command = null;
        DataProductInitializationApprover approver = new DataProductInitializationApprover(
                command, presenter, notificationsPort);

        // When & Then
        assertThatThrownBy(approver::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("DataProductInitializationApproveCommand cannot be null");

        verifyNoInteractions(notificationsPort, presenter);
    }

    @Test
    void whenDataProductIsNullThenThrowBadRequestException() {
        // Given
        DataProductInitializationApproverCommand command = new DataProductInitializationApproverCommand(null);
        DataProductInitializationApprover approver = new DataProductInitializationApprover(
                command, presenter, notificationsPort);

        // When & Then
        assertThatThrownBy(approver::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("DataProduct cannot be null");

        verifyNoInteractions(notificationsPort, presenter);
    }

    @Test
    void whenDataProductUuidIsNullThenThrowBadRequestException() {
        // Given
        DataProduct dataProduct = new DataProduct();
        dataProduct.setUuid(null);
        DataProductInitializationApproverCommand command = new DataProductInitializationApproverCommand(dataProduct);
        DataProductInitializationApprover approver = new DataProductInitializationApprover(
                command, presenter, notificationsPort);

        // When & Then
        assertThatThrownBy(approver::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("UUID is required for data product initialization approval");

        verifyNoInteractions(notificationsPort, presenter);
    }

    @Test
    void whenDataProductUuidIsEmptyThenThrowBadRequestException() {
        // Given
        DataProduct dataProduct = new DataProduct();
        dataProduct.setUuid("");
        DataProductInitializationApproverCommand command = new DataProductInitializationApproverCommand(dataProduct);
        DataProductInitializationApprover approver = new DataProductInitializationApprover(
                command, presenter, notificationsPort);

        // When & Then
        assertThatThrownBy(approver::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("UUID is required for data product initialization approval");

        verifyNoInteractions(notificationsPort, presenter);
    }

    @Test
    void whenDataProductUuidIsBlankThenThrowBadRequestException() {
        // Given
        DataProduct dataProduct = new DataProduct();
        dataProduct.setUuid("   ");
        DataProductInitializationApproverCommand command = new DataProductInitializationApproverCommand(dataProduct);
        DataProductInitializationApprover approver = new DataProductInitializationApprover(
                command, presenter, notificationsPort);

        // When & Then
        assertThatThrownBy(approver::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("UUID is required for data product initialization approval");

        verifyNoInteractions(notificationsPort, presenter);
    }

    @Test
    void whenDataProductIsValidThenEmitNotificationSuccessfully() {
        // Given
        DataProduct dataProduct = new DataProduct();
        dataProduct.setFqn("test.domain:test-product");
        dataProduct.setUuid("test-uuid-123");
        dataProduct.setName("Test Product");
        dataProduct.setDomain("test.domain");
        dataProduct.setDisplayName("Test Product Display Name");
        dataProduct.setDescription("Test Product Description");
        DataProductInitializationApproverCommand command = new DataProductInitializationApproverCommand(dataProduct);
        DataProductInitializationApprover approver = new DataProductInitializationApprover(
                command, presenter, notificationsPort);

        // When
        approver.execute();

        // Then
        verify(notificationsPort).emitDataProductInitializationApproved(dataProduct);
        verify(presenter).presentDataProductInitializationApproved(dataProduct);
    }

    @Test
    void whenExecuteSuccessfullyThenPresenterReceivesCorrectDataProduct() {
        // Given
        DataProduct dataProduct = new DataProduct();
        dataProduct.setFqn("test.domain:test-product");
        dataProduct.setUuid("test-uuid-123");
        dataProduct.setName("Test Product");
        dataProduct.setDomain("test.domain");
        dataProduct.setDisplayName("Test Product Display Name");
        dataProduct.setDescription("Test Product Description");
        DataProductInitializationApproverCommand command = new DataProductInitializationApproverCommand(dataProduct);
        DataProductInitializationApprover approver = new DataProductInitializationApprover(
                command, presenter, notificationsPort);

        // When
        approver.execute();

        // Then
        verify(presenter).presentDataProductInitializationApproved(argThat(presentedDataProduct -> {
            assertThat(presentedDataProduct)
                    .usingRecursiveComparison()
                    .isEqualTo(dataProduct);
            return true;
        }));
    }

    @Test
    void whenExecuteSuccessfullyThenNotificationPortReceivesCorrectDataProduct() {
        // Given
        DataProduct dataProduct = new DataProduct();
        dataProduct.setFqn("test.domain:test-product");
        dataProduct.setUuid("test-uuid-123");
        dataProduct.setName("Test Product");
        dataProduct.setDomain("test.domain");
        dataProduct.setDisplayName("Test Product Display Name");
        dataProduct.setDescription("Test Product Description");
        DataProductInitializationApproverCommand command = new DataProductInitializationApproverCommand(dataProduct);
        DataProductInitializationApprover approver = new DataProductInitializationApprover(
                command, presenter, notificationsPort);

        // When
        approver.execute();

        // Then
        verify(notificationsPort).emitDataProductInitializationApproved(argThat(emittedDataProduct -> {
            assertThat(emittedDataProduct)
                    .usingRecursiveComparison()
                    .isEqualTo(dataProduct);
            return true;
        }));
    }
}

