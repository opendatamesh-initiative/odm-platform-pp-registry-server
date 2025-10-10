package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.reject;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersionValidationState;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.utils.usecases.TransactionalOutboundPort;
import org.opendatamesh.platform.pp.registry.utils.usecases.UseCase;
import org.springframework.util.StringUtils;

import static org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersionValidationState.REJECTED;

class DataProductVersionRejector implements UseCase {
    private final DataProductVersionRejectCommand command;
    private final DataProductVersionRejectPresenter presenter;
    private final DataProductVersionRejectorPersistenceOutboundPort persistencePort;
    private final TransactionalOutboundPort transactionalPort;

    DataProductVersionRejector(DataProductVersionRejectCommand command,
                               DataProductVersionRejectPresenter presenter,
                               DataProductVersionRejectorPersistenceOutboundPort persistencePort,
                               TransactionalOutboundPort transactionalPort) {
        this.command = command;
        this.presenter = presenter;
        this.persistencePort = persistencePort;
        this.transactionalPort = transactionalPort;
    }

    @Override
    public void execute() {
        validateCommand(command);

        transactionalPort.doInTransaction(() -> {
            DataProductVersion dataProductVersion = persistencePort.findByUuid(command.dataProductVersion().getUuid());

            if (!DataProductVersionValidationState.PENDING.equals(dataProductVersion.getValidationState())) {
                throw new BadRequestException(String.format("Data Product Version %s %s can be rejected only if in PENDING state.", command.dataProductVersion().getName(), command.dataProductVersion().getTag()));
            }

            dataProductVersion.setValidationState(REJECTED);
            dataProductVersion = persistencePort.save(dataProductVersion);

            presenter.presentDataProductVersionRejected(dataProductVersion);
        });
    }

    private void validateCommand(DataProductVersionRejectCommand command) {
        if (command == null) {
            throw new BadRequestException("DataProductVersionRejectCommand cannot be null");
        }
        if (command.dataProductVersion() == null) {
            throw new BadRequestException("DataProductVersion cannot be null");
        }
        DataProductVersion dataProductVersion = command.dataProductVersion();
        if (!StringUtils.hasText(dataProductVersion.getUuid())) {
            throw new BadRequestException("UUID is required for data product version rejection");
        }
    }
}
