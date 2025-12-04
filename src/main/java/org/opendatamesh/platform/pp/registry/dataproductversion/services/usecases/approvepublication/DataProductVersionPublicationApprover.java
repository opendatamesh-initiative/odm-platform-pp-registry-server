package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.approvepublication;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.utils.usecases.UseCase;
import org.springframework.util.StringUtils;

class DataProductVersionPublicationApprover implements UseCase {
    private final DataProductVersionPublicationApproverCommand command;
    private final DataProductVersionPublicationApproverPresenter presenter;
    private final DataProductVersionPublicationApproverNotificationOutboundPort notificationsPort;

    DataProductVersionPublicationApprover(DataProductVersionPublicationApproverCommand command,
                                         DataProductVersionPublicationApproverPresenter presenter,
                                         DataProductVersionPublicationApproverNotificationOutboundPort notificationsPort) {
        this.command = command;
        this.presenter = presenter;
        this.notificationsPort = notificationsPort;
    }

    @Override
    public void execute() {
        validateCommand(command);

        // TODO: findOne to check existence? Should not be necessary

        DataProductVersion dataProductVersion = command.dataProductVersion();
        notificationsPort.emitDataProductVersionInitializationApproved(dataProductVersion);
        presenter.presentDataProductVersionInitializationApproved(dataProductVersion);
    }

    private void validateCommand(DataProductVersionPublicationApproverCommand command) {
        if (command == null) {
            throw new BadRequestException("DataProductVersionPublicationApproverCommand cannot be null");
        }

        if (command.dataProductVersion() == null) {
            throw new BadRequestException("DataProductVersion cannot be null");
        }

        DataProductVersion dataProductVersion = command.dataProductVersion();

        // Validate uuid is present
        if (!StringUtils.hasText(dataProductVersion.getUuid())) {
            throw new BadRequestException("UUID is required for data product version initialization approval");
        }
    }
}

