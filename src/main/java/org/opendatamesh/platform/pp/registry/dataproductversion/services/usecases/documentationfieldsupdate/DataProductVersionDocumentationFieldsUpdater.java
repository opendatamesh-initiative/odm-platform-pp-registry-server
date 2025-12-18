package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.documentationfieldsupdate;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.utils.usecases.TransactionalOutboundPort;
import org.opendatamesh.platform.pp.registry.utils.usecases.UseCase;
import org.springframework.util.StringUtils;

class DataProductVersionDocumentationFieldsUpdater implements UseCase {
    private final DataProductVersionDocumentationFieldsUpdateCommand command;
    private final DataProductVersionDocumentationFieldsUpdatePresenter presenter;

    private final DataProductVersionDocumentationFieldsUpdaterPersistenceOutboundPort persistencePort;
    private final TransactionalOutboundPort transactionalPort;

    DataProductVersionDocumentationFieldsUpdater(DataProductVersionDocumentationFieldsUpdateCommand command,
                                                 DataProductVersionDocumentationFieldsUpdatePresenter presenter,
                                                 DataProductVersionDocumentationFieldsUpdaterPersistenceOutboundPort persistencePort,
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
            DataProductVersion dataProductVersion = persistencePort.findByUuid(command.getUuid());

            // Extract fields from the res object and set them
            if (StringUtils.hasText(command.name())) {
                dataProductVersion.setName(command.name());
            }
            if (command.description() != null) {
                dataProductVersion.setDescription(command.description());
            }
            if (StringUtils.hasText(command.updatedBy())) {
                dataProductVersion.setUpdatedBy(command.updatedBy());
            }
            dataProductVersion = persistencePort.save(dataProductVersion);
            presenter.presentDataProductVersionDocumentationFieldsUpdated(dataProductVersion);
        });
    }

    private void validateCommand(DataProductVersionDocumentationFieldsUpdateCommand command) {
        if (command == null) {
            throw new BadRequestException("DataProductVersionDocumentationFieldsUpdateCommand cannot be null");
        }
        if (!StringUtils.hasText(command.getUuid())) {
            throw new BadRequestException("UUID is required for data product version documentation fields update");
        }
        if (!StringUtils.hasText(command.name())) {
            throw new BadRequestException("Version name is required for data product version documentation fields update");
        }
    }

}
