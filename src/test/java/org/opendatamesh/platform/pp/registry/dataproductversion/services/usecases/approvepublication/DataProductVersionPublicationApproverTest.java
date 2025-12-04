package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.approvepublication;

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
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;

@ExtendWith(MockitoExtension.class)
class DataProductVersionPublicationApproverTest {

    @Mock
    private DataProductVersionPublicationApproverPresenter presenter;

    @Mock
    private DataProductVersionPublicationApproverNotificationOutboundPort notificationsPort;

    @Test
    void whenCommandIsNullThenThrowBadRequestException() {
        // Given
        DataProductVersionPublicationApproverCommand command = null;
        DataProductVersionPublicationApprover approver = new DataProductVersionPublicationApprover(
                command, presenter, notificationsPort);

        // When & Then
        assertThatThrownBy(approver::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("DataProductVersionPublicationApproverCommand cannot be null");

        verifyNoInteractions(notificationsPort, presenter);
    }

    @Test
    void whenDataProductVersionIsNullThenThrowBadRequestException() {
        // Given
        DataProductVersionPublicationApproverCommand command = new DataProductVersionPublicationApproverCommand(null);
        DataProductVersionPublicationApprover approver = new DataProductVersionPublicationApprover(
                command, presenter, notificationsPort);

        // When & Then
        assertThatThrownBy(approver::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("DataProductVersion cannot be null");

        verifyNoInteractions(notificationsPort, presenter);
    }

    @Test
    void whenDataProductVersionUuidIsNullThenThrowBadRequestException() {
        // Given
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid(null);
        DataProductVersionPublicationApproverCommand command = new DataProductVersionPublicationApproverCommand(dataProductVersion);
        DataProductVersionPublicationApprover approver = new DataProductVersionPublicationApprover(
                command, presenter, notificationsPort);

        // When & Then
        assertThatThrownBy(approver::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("UUID is required for data product version initialization approval");

        verifyNoInteractions(notificationsPort, presenter);
    }

    @Test
    void whenDataProductVersionUuidIsEmptyThenThrowBadRequestException() {
        // Given
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("");
        DataProductVersionPublicationApproverCommand command = new DataProductVersionPublicationApproverCommand(dataProductVersion);
        DataProductVersionPublicationApprover approver = new DataProductVersionPublicationApprover(
                command, presenter, notificationsPort);

        // When & Then
        assertThatThrownBy(approver::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("UUID is required for data product version initialization approval");

        verifyNoInteractions(notificationsPort, presenter);
    }

    @Test
    void whenDataProductVersionUuidIsBlankThenThrowBadRequestException() {
        // Given
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("   ");
        DataProductVersionPublicationApproverCommand command = new DataProductVersionPublicationApproverCommand(dataProductVersion);
        DataProductVersionPublicationApprover approver = new DataProductVersionPublicationApprover(
                command, presenter, notificationsPort);

        // When & Then
        assertThatThrownBy(approver::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("UUID is required for data product version initialization approval");

        verifyNoInteractions(notificationsPort, presenter);
    }

    @Test
    void whenDataProductVersionIsValidThenEmitNotificationSuccessfully() {
        // Given
        DataProduct dataProduct = new DataProduct();
        dataProduct.setUuid("data-product-uuid-123");
        dataProduct.setFqn("test.domain:test-product");
        dataProduct.setName("Test Product");
        dataProduct.setDomain("test.domain");

        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-version-uuid-123");
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setSpec("opendatamesh");
        dataProductVersion.setSpecVersion("1.0.0");
        dataProductVersion.setDataProduct(dataProduct);
        DataProductVersionPublicationApproverCommand command = new DataProductVersionPublicationApproverCommand(dataProductVersion);
        DataProductVersionPublicationApprover approver = new DataProductVersionPublicationApprover(
                command, presenter, notificationsPort);

        // When
        approver.execute();

        // Then
        verify(notificationsPort).emitDataProductVersionInitializationApproved(dataProductVersion);
        verify(presenter).presentDataProductVersionInitializationApproved(dataProductVersion);
    }

    @Test
    void whenExecuteSuccessfullyThenPresenterReceivesCorrectDataProductVersion() {
        // Given
        DataProduct dataProduct = new DataProduct();
        dataProduct.setUuid("data-product-uuid-123");
        dataProduct.setFqn("test.domain:test-product");
        dataProduct.setName("Test Product");
        dataProduct.setDomain("test.domain");

        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-version-uuid-123");
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setSpec("opendatamesh");
        dataProductVersion.setSpecVersion("1.0.0");
        dataProductVersion.setDataProduct(dataProduct);
        DataProductVersionPublicationApproverCommand command = new DataProductVersionPublicationApproverCommand(dataProductVersion);
        DataProductVersionPublicationApprover approver = new DataProductVersionPublicationApprover(
                command, presenter, notificationsPort);

        // When
        approver.execute();

        // Then
        verify(presenter).presentDataProductVersionInitializationApproved(argThat(presentedDataProductVersion -> {
            assertThat(presentedDataProductVersion)
                    .usingRecursiveComparison()
                    .isEqualTo(dataProductVersion);
            return true;
        }));
    }

    @Test
    void whenExecuteSuccessfullyThenNotificationPortReceivesCorrectDataProductVersion() {
        // Given
        DataProduct dataProduct = new DataProduct();
        dataProduct.setUuid("data-product-uuid-123");
        dataProduct.setFqn("test.domain:test-product");
        dataProduct.setName("Test Product");
        dataProduct.setDomain("test.domain");

        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("test-version-uuid-123");
        dataProductVersion.setName("Test Version");
        dataProductVersion.setDescription("Test Version Description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setSpec("opendatamesh");
        dataProductVersion.setSpecVersion("1.0.0");
        dataProductVersion.setDataProduct(dataProduct);
        DataProductVersionPublicationApproverCommand command = new DataProductVersionPublicationApproverCommand(dataProductVersion);
        DataProductVersionPublicationApprover approver = new DataProductVersionPublicationApprover(
                command, presenter, notificationsPort);

        // When
        approver.execute();

        // Then
        verify(notificationsPort).emitDataProductVersionInitializationApproved(argThat(emittedDataProductVersion -> {
            assertThat(emittedDataProductVersion)
                    .usingRecursiveComparison()
                    .isEqualTo(dataProductVersion);
            return true;
        }));
    }
}

