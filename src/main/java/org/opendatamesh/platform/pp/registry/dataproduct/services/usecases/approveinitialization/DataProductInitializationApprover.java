package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.approveinitialization;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.utils.usecases.UseCase;
import org.springframework.util.StringUtils;

class DataProductInitializationApprover implements UseCase {
    private final DataProductInitializationApproverCommand command;
    private final DataProductInitializationApproverPresenter presenter;
    private final DataProductInitializationApproverNotificationOutboundPort notificationsPort;

    DataProductInitializationApprover(DataProductInitializationApproverCommand command,
                                      DataProductInitializationApproverPresenter presenter,
                                      DataProductInitializationApproverNotificationOutboundPort notificationsPort) {
        this.command = command;
        this.presenter = presenter;
        this.notificationsPort = notificationsPort;
    }

    @Override
    public void execute() {
        validateCommand(command);

        // TODO: findOne to check existence? Should not be necessary

        DataProduct dataProduct = command.dataProduct();
        notificationsPort.emitDataProductInitializationApproved(dataProduct);
        presenter.presentDataProductInitializationApproved(dataProduct);
    }

    private void validateCommand(DataProductInitializationApproverCommand command) {
        if (command == null) {
            throw new BadRequestException("DataProductInitializationApproveCommand cannot be null");
        }

        if (command.dataProduct() == null) {
            throw new BadRequestException("DataProduct cannot be null");
        }

        DataProduct dataProduct = command.dataProduct();

        // Validate uuid is present
        if (!StringUtils.hasText(dataProduct.getUuid())) {
            throw new BadRequestException("UUID is required for data product initialization approval");
        }
    }
}

