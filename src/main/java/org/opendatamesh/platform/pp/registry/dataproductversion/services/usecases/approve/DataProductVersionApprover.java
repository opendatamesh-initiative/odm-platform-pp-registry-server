package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.approve;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductValidationState;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersionValidationState;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.utils.usecases.TransactionalOutboundPort;
import org.opendatamesh.platform.pp.registry.utils.usecases.UseCase;
import org.springframework.util.StringUtils;

import static org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersionValidationState.APPROVED;

class DataProductVersionApprover implements UseCase {
    private final DataProductVersionApproveCommand command;
    private final DataProductVersionApprovePresenter presenter;

    private final DataProductVersionApproverNotificationOutboundPort notificationsPort;
    private final DataProductVersionApproverPersistenceOutboundPort persistencePort;
    private final TransactionalOutboundPort transactionalPort;

    DataProductVersionApprover(DataProductVersionApproveCommand command,
                               DataProductVersionApprovePresenter presenter,
                               DataProductVersionApproverNotificationOutboundPort notificationsPort,
                               DataProductVersionApproverPersistenceOutboundPort persistencePort,
                               TransactionalOutboundPort transactionalPort) {
        this.command = command;
        this.presenter = presenter;
        this.notificationsPort = notificationsPort;
        this.persistencePort = persistencePort;
        this.transactionalPort = transactionalPort;
    }

    @Override
    public void execute() {
        validateCommand(command);

        transactionalPort.doInTransaction(() -> {
            DataProductVersion dataProductVersion = persistencePort.findByUuid(command.dataProductVersion().getUuid());

            if(!DataProductValidationState.APPROVED.equals(dataProductVersion.getDataProduct().getValidationState())){
                throw new BadRequestException(String.format("Data Product Version %s %s must be associated to an APPROVED Data Product in order to be approved.", command.dataProductVersion().getName(), command.dataProductVersion().getTag()));
            }

            if (!DataProductVersionValidationState.PENDING.equals(dataProductVersion.getValidationState())) {
                throw new BadRequestException(String.format("Data Product Version %s %s can be approved only if in PENDING state.", command.dataProductVersion().getName(), command.dataProductVersion().getTag()));
            }

            dataProductVersion.setValidationState(APPROVED);
            dataProductVersion = persistencePort.save(dataProductVersion);

            notificationsPort.emitDataProductVersionPublished(dataProductVersion);
            presenter.presentDataProductVersionApproved(dataProductVersion);
        });
    }

    private void validateCommand(DataProductVersionApproveCommand command) {
        if (command == null) {
            throw new BadRequestException("DataProductVersionApproveCommand cannot be null");
        }
        if (command.dataProductVersion() == null) {
            throw new BadRequestException("DataProductVersion cannot be null");
        }
        DataProductVersion dataProductVersion = command.dataProductVersion();
        if (!StringUtils.hasText(dataProductVersion.getUuid())) {
            throw new BadRequestException("UUID is required for data product version approval");
        }
    }
}
